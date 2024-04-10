package io.cloudsoft.winrm4j.client.psrp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PsrpMessageFactory {

	private static final Logger LOG = LoggerFactory.getLogger(PsrpMessageFactory.class);

	static final UUID NIL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private static final Map<String, String> TEMPLATES;

	static {
		TEMPLATES = Map.of(
				"session_capability", readXmlDirect("session_capability"),
				"init_runspace_pool", readXmlDirect("init_runspace_pool"),
				"create_pipeline", readXmlDirect("create_pipeline")
		);
	}

	public static byte[] combine(final PsrpMessageFragmenter fragmenter, final PsrpMessage... messages) {
		try (final var baos = new ByteArrayOutputStream()) {
			Arrays.stream(messages)
					.flatMap(message -> fragmenter.fragment(message).stream())
					.map(PsrpFragment::bytes)
					.forEach(bytes -> {
						try {
							baos.write(bytes);
						} catch (final IOException e) {
							// unlikely, because BAOS
							throw new PsrpMessageException("Failed to write PSRP message data", e);
						}
					});
			return baos.toByteArray();
		} catch (final IOException e) {
			// unlikely, because BAOS
			throw new PsrpMessageException("Failed to write PSRP message data", e);
		}
	}

	public static PsrpMessage createPipeline(final UUID runspacePoolId,
											 final UUID pipelineId,
											 String command) {
		return new PsrpMessage(
				runspacePoolId,
				pipelineId,
				PsrpMessageType.CREATE_PIPELINE,
				PsrpDestination.SERVER,
				TEMPLATES.get("create_pipeline").replace(":command", command)
		);
	}

	public static PsrpMessage sessionCapability(final UUID runspacePoolId) {
		return new PsrpMessage(
				runspacePoolId,
				NIL_UUID,
				PsrpMessageType.SESSION_CAPABILITY,
				PsrpDestination.SERVER,
				TEMPLATES.get("session_capability")
		);
	}

	public static PsrpMessage initRunspacePool(final UUID runspacePoolId) {
		return new PsrpMessage(
				runspacePoolId,
				NIL_UUID,
				PsrpMessageType.INIT_RUNSPACEPOOL,
				PsrpDestination.SERVER,
				TEMPLATES.get("init_runspace_pool")
		);
	}

	private static Document readDocument(final String templateName) throws ParserConfigurationException, IOException, SAXException {
		final var dbf = DocumentBuilderFactory.newInstance();
		final var db = dbf.newDocumentBuilder();
		final var path = "psrp/%s.xml".formatted(templateName);
		try (final var reader = new InputStreamReader(PsrpMessageFactory.class.getClassLoader().getResourceAsStream(path))) {
			return db.parse(new InputSource(reader));
		}
	}

	private static String readXmlDirect(final String templateName) {
		final var path = "psrp/%s.xml".formatted(templateName);
		try (final var reader = new InputStreamReader(PsrpMessageFactory.class.getClassLoader().getResourceAsStream(path));
			 final var writer = new StringWriter()) {
			reader.transferTo(writer);
			return writer.toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static String readXml(final String templateName) {
		try {
			final var doc = readDocument(templateName);
			removeEmptyTextNodes(doc);

			final var domSource = new DOMSource(doc);
			try (final var writer = new StringWriter()) {
				final var result = new StreamResult(writer);
				final var tf = TransformerFactory.newInstance();
				final var transformer = tf.newTransformer();
				//transformer.setOutputProperty(OutputKeys.INDENT, "no");
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.transform(domSource, result);
				return writer.toString();
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void removeEmptyTextNodes(final Document doc) throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		XPathExpression xpathExp = xpathFactory.newXPath().compile(
				"//text()[normalize-space(.) = '']");
		NodeList emptyTextNodes = (NodeList)
				xpathExp.evaluate(doc, XPathConstants.NODESET);

		// Remove each empty text node from document.
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
			Node emptyTextNode = emptyTextNodes.item(i);
			emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}
	}

}
