package io.cloudsoft.winrm4j.client;

import java.util.Set;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

/**
 * This post-marshal interceptor patches outgoing soap messages and rewrites the header <code>MessageID</code>
 * without the prefix "urn:".
 * <p>
 * This ensures compatibility with the PSRP via wsman integration
 *
 * @see WsmanMessageIdPatcherInboundInterceptor
 */
public class WsmanMessageIdPatcherOutboundInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

	static final Set<String> HEADER_NAMES = Set.of("MessageID");

	public WsmanMessageIdPatcherOutboundInterceptor() {
		super(Phase.POST_MARSHAL);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		try {
			final var header = message.getContent(SOAPMessage.class).getSOAPHeader();
			final var headerList = header.getChildNodes();
			for (int i = 0; i < headerList.getLength(); i++) {
				final var headerNode = headerList.item(i);
				final var headerName = headerNode.getNodeName();
				if (HEADER_NAMES.contains(headerName)) {
					if (headerNode.getTextContent().startsWith("urn:uuid:")) {
						headerNode.setTextContent(headerNode.getTextContent().substring(4));
					}
				}
			}
		} catch (SOAPException e) {
			throw new Fault(e);
		}
	}
}
