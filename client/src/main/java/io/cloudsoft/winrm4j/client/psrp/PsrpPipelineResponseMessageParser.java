package io.cloudsoft.winrm4j.client.psrp;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PsrpPipelineResponseMessageParser {

	public static final PsrpPipelineResponseMessageParser INSTANCE = new PsrpPipelineResponseMessageParser();

	private final DocumentBuilderFactory dbf;
	private final XPath xPath;

	public PsrpPipelineResponseMessageParser() {
		dbf = DocumentBuilderFactory.newInstance();
		xPath = XPathFactory.newInstance().newXPath();
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		} catch (final ParserConfigurationException e) {
			throw new IllegalStateException("Failed to initialize DocumentBuilderFactory", e);
		}
	}

	Document parseDocument(String xml) throws ParserConfigurationException, IOException, SAXException {
		final var db = dbf.newDocumentBuilder();
		try (final var reader = new StringReader(xml)) {
			return db.parse(new InputSource(reader));
		}
	}

	public Optional<PsrpRunspacepoolState> extractRunspacePoolState(final String xml) {
		try {
			final var doc = parseDocument(xml);
			final var value = (String) xPath.compile("/Obj/MS/I32[@N='RunspaceState']/text()").evaluate(doc, XPathConstants.STRING);

			if (value != null && !value.isEmpty()) {
				return Optional.of(PsrpRunspacepoolState.values()[Integer.parseInt(value)]);
			}
		} catch (final Exception e) {
			return Optional.empty();
		}
		return Optional.empty();
	}

	public Optional<PsrpPipelineState> extractPipelineState(final String xml) {
		try {
			final var doc = parseDocument(xml);
			final var value = (String) xPath.compile("/Obj/MS/I32[@N='PipelineState']/text()").evaluate(doc, XPathConstants.STRING);

			if (value != null && !value.isEmpty()) {
				return Optional.of(PsrpPipelineState.values()[Integer.parseInt(value)]);
			}
		} catch (final Exception e) {
			return Optional.empty();
		}
		return Optional.empty();
	}

	public Optional<Integer> extractExitCode(final PsrpMessage message) {
		if (message.type() != PsrpMessageType.PIPELINE_HOST_CALL) {
			return Optional.empty();
		}
		try {
			final var doc = parseDocument(message.data());
			if (xPath.compile("/Obj/MS/Obj[@RefId = '1'][ToString/text() = 'SetShouldExit']").evaluate(doc, XPathConstants.NODE) != null) {
				final var value = (String) xPath.compile("/Obj/MS/Obj[@RefId = '2']/LST/I32/text()").evaluate(doc, XPathConstants.STRING);
				if (value != null && !value.isEmpty()) {
					return Optional.of(Integer.parseInt(value.strip()));
				}
			}
		} catch (final Exception e) {
			return Optional.empty();
		}
		return Optional.empty();
	}

	public Optional<String> extractText(final PsrpMessage message) {
		return switch (message.type()) {
			case PIPELINE_OUTPUT -> Optional.of(decodePipelineOutput(message));
			case RUNSPACEPOOL_HOST_CALL, PIPELINE_HOST_CALL -> Optional.of(decodeHostCall(message));
			case ERROR_RECORD -> Optional.of(decodeErrorRecord(message));
			case PIPELINE_STATE -> extractPipelineState(message.data())
					.filter(PsrpPipelineState.FAILED::equals)
					.map(x -> decodePipelineFailure(message));
			default -> Optional.empty();
		};
	}

	String decodePipelineOutput(final PsrpMessage message) {
		if (message.data() == null || message.data().isEmpty()) {
			return "";
		}
		try {
			final var doc = parseDocument(message.data());
			final var items = (NodeList) xPath.compile("//S").evaluate(doc, XPathConstants.NODESET);
			final var sb = new StringBuilder();
			for (int i = 0; i < items.getLength(); i++) {
				Optional.ofNullable(items.item(i).getTextContent())
						.ifPresent(sb::append);
				sb.append("\r\n");
			}
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalStateException("Failed decoding pipeline output", e);
		}
	}

	String decodeHostCall(final PsrpMessage message) {
		// not yet implemented
		return "";
	}

	String decodePipelineFailure(final PsrpMessage message) {
		if (message.data() == null || message.data().isEmpty()) {
			return "";
		}
		try {
			final var doc = parseDocument(message.data());
			final var errorRecord = new ErrorRecordParser(doc, xPath).parse();
			return """
					%s
						+ CategoryInfo          : %s
						+ FullyQualifiedErrorId : %s
					""".formatted(
					errorRecord.exception().get("message"),
					errorRecord.errorCategoryMessage(),
					errorRecord.fullQualifiedErrorId()
			);
		} catch (Exception e) {
			throw new IllegalStateException("Failed decoding pipeline output", e);
		}
	}

	String decodeErrorRecord(final PsrpMessage message) {
		if (message.data() == null || message.data().isEmpty()) {
			return "";
		}
		try {
			final var doc = parseDocument(message.data());
			final var errorRecord = new ErrorRecordParser(doc, xPath).parse();
			return decodeErrorRecord(errorRecord);
		} catch (Exception e) {
			throw new IllegalStateException("Failed decoding pipeline output", e);
		}
	}

	String decodeErrorRecord(final ErrorRecord errorRecord) {
		return switch (errorRecord.fullQualifiedErrorId()) {
			case "Microsoft.PowerShell.Commands.WriteErrorException" -> """
					%s : %s
						+ CategoryInfo          : %s
						+ FullyQualifiedErrorId : %s
					""".formatted(
					errorRecord.invocationInfo().get("line"),
					errorRecord.exception().get("message"),
					errorRecord.errorCategoryMessage(),
					errorRecord.fullQualifiedErrorId()
			);
			case "NativeCommandError" -> """
					%s : %s
						+ CategoryInfo          : %s
						+ FullyQualifiedErrorId : %s
					""".formatted(
					errorRecord.invocationInfo().get("myCommand"),
					errorRecord.exception().get("message"),
					errorRecord.errorCategoryMessage(),
					errorRecord.fullQualifiedErrorId()
			);
			case "NativeCommandErrorMessage" -> errorRecord.exception().get("message");
			default -> """
					%s
					%s
						+ CategoryInfo          : %s
						+ FullyQualifiedErrorId : %s
					""".formatted(
					errorRecord.exception().get("message"),
					errorRecord.invocationInfo().get("positionMessage"),
					errorRecord.errorCategoryMessage(),
					errorRecord.fullQualifiedErrorId()
			);
		};
	}

	record ErrorRecord(
			Map<String, String> exception,
			String fullQualifiedErrorId,
			Map<String, String> invocationInfo,
			String errorCategoryMessage,
			String errorDetailsScriptStackTrace
	) {
	}

	static class ErrorRecordParser {

		private final Document doc;
		private final XPath xPath;

		public ErrorRecordParser(Document doc, XPath xPath) {
			this.doc = doc;
			this.xPath = xPath;
		}

		String resolveProperty(final String propertyName) {
			try {
				final var items = (NodeList) xPath.compile("//*[@N='" + propertyName + "']").evaluate(doc, XPathConstants.NODESET);
				if (items.getLength() < 1) {
					return null;
				}
				return items.item(0).getTextContent();

			} catch (final XPathExpressionException e) {
				throw new IllegalStateException("Failed to resolve property", e);
			}
		}

		Map<String, String> resolvePropertiesMap(final String propertyName) {
			try {
				final var items = (NodeList) xPath.compile("//*[@N='" + propertyName + "']/Props").evaluate(doc, XPathConstants.NODESET);
				if (items.getLength() < 1) {
					return Map.of();
				}
				final var result = new HashMap<String, String>();
				final var node = items.item(0); // first only
				final var name = Optional.ofNullable(node.getAttributes().getNamedItem("N"))
						.map(Node::getNodeValue)
						.orElse("");
				Optional.ofNullable(node.getTextContent())
						.ifPresent(text -> result.put(name, text));
				return Map.copyOf(result);

			} catch (final XPathExpressionException e) {
				throw new IllegalStateException("Failed to resolve properties map", e);
			}
		}

		public ErrorRecord parse() {
			return new ErrorRecord(
					resolvePropertiesMap("Exception"),
					resolveProperty("FullyQualifiedErrorId"),
					resolvePropertiesMap("InvocationInfo"),
					resolveProperty("ErrorCategory_Message"),
					resolveProperty("ErrorDetails_ScriptStackTrace")
			);
		}

	}

}
