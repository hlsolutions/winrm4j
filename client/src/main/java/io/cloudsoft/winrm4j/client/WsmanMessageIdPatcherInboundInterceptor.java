package io.cloudsoft.winrm4j.client;

import java.util.Set;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.w3c.dom.Element;

/**
 * This pre-protocol interceptor patches ingoing soap messages and rewrites the header <code>MessageID</code>
 * with the prefix "urn:". This includes also the related header <code>RelatesTo</code>.
 * <p>
 * This ensures compatibility with the PSRP via wsman integration
 *
 * @see WsmanMessageIdPatcherOutboundInterceptor
 */
public class WsmanMessageIdPatcherInboundInterceptor extends AbstractPhaseInterceptor<SoapMessage> {

	static final Set<String> HEADER_NAMES = Set.of("MessageID", "RelatesTo");

	public WsmanMessageIdPatcherInboundInterceptor() {
		super(Phase.PRE_PROTOCOL);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		for (final var header : message.getHeaders()) {
			final var headerName = header.getName().getLocalPart();
			if (HEADER_NAMES.contains(headerName)) {
				if (header.getObject() instanceof Element el) {
					if (el.getTextContent().startsWith("uuid:")) {
						el.setTextContent("urn:" + el.getTextContent());
					}
				}
			}
		}
	}
}
