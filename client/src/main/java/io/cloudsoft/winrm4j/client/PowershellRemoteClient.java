package io.cloudsoft.winrm4j.client;

import static io.cloudsoft.winrm4j.client.WinRmClient.MAX_ENVELOPER_SIZE;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import jakarta.xml.ws.soap.SOAPFaultException;

import io.cloudsoft.winrm4j.client.psrp.PsrpFragment;
import io.cloudsoft.winrm4j.client.psrp.PsrpMessageFactory;
import io.cloudsoft.winrm4j.client.psrp.PsrpMessageFragmenter;
import io.cloudsoft.winrm4j.client.psrp.PsrpMessageType;
import io.cloudsoft.winrm4j.client.psrp.PsrpPipelineResponseMessageParser;
import io.cloudsoft.winrm4j.client.psrp.PsrpPipelineResponseReader;
import io.cloudsoft.winrm4j.client.psrp.PsrpPipelineState;
import io.cloudsoft.winrm4j.client.psrp.PsrpRunspacepoolState;
import io.cloudsoft.winrm4j.client.psrp.PsrpUtils;
import io.cloudsoft.winrm4j.client.shell.CommandLine;
import io.cloudsoft.winrm4j.client.shell.DesiredStreamType;
import io.cloudsoft.winrm4j.client.shell.Receive;
import io.cloudsoft.winrm4j.client.shell.Send;
import io.cloudsoft.winrm4j.client.shell.Shell;
import io.cloudsoft.winrm4j.client.shell.StreamType;
import io.cloudsoft.winrm4j.client.transfer.ResourceCreated;
import io.cloudsoft.winrm4j.client.wsman.Locale;
import io.cloudsoft.winrm4j.client.wsman.OptionSetType;
import io.cloudsoft.winrm4j.client.wsman.OptionType;
import io.cloudsoft.winrm4j.client.wsman.SelectorSetType;
import io.cloudsoft.winrm4j.client.wsman.SelectorType;
import io.cloudsoft.winrm4j.client.wsman.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PowershellRemoteClient implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(PowershellRemoteClient.class.getName());

	public static final String WSMAN_CONFIG_RESOURCE_URI = "http://schemas.microsoft.com/wbem/wsman/1/config";
	public static final String POWERSHELL_RESOURCE_URI = "http://schemas.microsoft.com/powershell/Microsoft.PowerShell";

	private static final String COMMAND_STATE_DONE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done";

	/**
	 * If no output is available before the wsman:OperationTimeout expires, the server MUST return a WSManFault with the Code attribute equal to "2150858793"
	 * https://msdn.microsoft.com/en-us/library/cc251676.aspx
	 */
	static final String WSMAN_FAULT_CODE_OPERATION_TIMEOUT_EXPIRED = "2150858793";

	/**
	 * Example response:
	 * [truncated]The request for the Windows Remote Shell with ShellId xxxx-yyyy-ccc... failed because the shell was not found on the server.
	 * Possible causes are: the specified ShellId is incorrect or the shell no longer exi
	 */
	private static final String WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND = "2150858843";

	public interface RunspacePoolSession extends Closeable {

		int execute(String command, Writer out, Writer err);

		@Override
		void close() throws SOAPFaultException;
	}

	interface ParamsResolver {

		int maxFragmentBlobSize();

		int maxEnvelopeSize();

		int emptyPipelineEnvelopeSize();

	}

	private final WinRm winrm;
	private final String operationTimeout;

	/**
	 * Define if a new Receive request will be send when the server returns a fault with the code
	 * {@link #WSMAN_FAULT_CODE_OPERATION_TIMEOUT_EXPIRED}.
	 */
	private final Predicate<String> retryReceiveAfterOperationTimeout;
	private final Locale locale;

	private PsrpMessageFragmenter fragmenter;

	private final ParamsResolver params;

	private final Set<Closeable> closeables = new HashSet<>();
	private final Set<SessionId> closedSessions = new HashSet<>();

	public PowershellRemoteClient(final WinRm winrm,
								  final String operationTimeout,
								  final Predicate<String> retryReceiveAfterOperationTimeout,
								  final Locale locale) {
		this.winrm = winrm;
		this.operationTimeout = operationTimeout;
		this.retryReceiveAfterOperationTimeout = retryReceiveAfterOperationTimeout;
		this.locale = locale;
		this.params = new ParamsResolverImpl(winrm);
	}

	PsrpMessageFragmenter getFragmenter() {
		if (fragmenter == null) {
			fragmenter = new PsrpMessageFragmenter(params.maxFragmentBlobSize());
		}
		return fragmenter;
	}

	/**
	 * Creates and initialize a new RunspacePool ready for commands (pipelines) of a PSRP context.
	 *
	 * @return session
	 */
	public RunspacePoolSession initializeRunspacePool() {

		final var sessionId = new SessionId("uuid:" + UUID.randomUUID().toString().toUpperCase());
		final var runspacePoolId = UUID.randomUUID();
		final var shellId = createRunspacePoolId(sessionId, runspacePoolId);
		this.closeables.add(() -> closeShell(sessionId, shellId));

		final var shellSelector = createShellSelector(shellId);
		final var responseReader = new PsrpPipelineResponseReader(
				winrm,
				() -> shellSelector,
				params::maxEnvelopeSize,
				() -> operationTimeout,
				() -> locale,
				sessionId::getValue
		);

		waitForRunning(responseReader);

		return new SessionImpl(sessionId, shellId, responseReader) {
			@Override
			public int execute(final String command, final Writer out, final Writer err) {
				final var commandId = createPipeline(
						sessionId,
						shellId,
						runspacePoolId,
						UUID.randomUUID(),
						command
				);
				try {
					return receiveCommandOutput(
							sessionId,
							shellId,
							commandId,
							responseReader,
							out,
							err
					);
				} catch (Exception e) {
					try {
						releaseCommand(sessionId, shellId, commandId);
					} catch (SOAPFaultException soapFault) {
						assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
					}
					throw e;
				}
			}

			@Override
			public void close() throws SOAPFaultException {
				try {
					closeShell(sessionId, shellId);
				} catch (SOAPFaultException soapFault) {
					assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
				}
			}
		};
	}

	@Override
	public void close() {
		for (var closable : closeables) {
			try {
				closable.close();
			} catch (final Exception e) {
				LOG.warn("Failed to close resources", e);
			}
		}
		closeables.clear();

	}

	ShellId createRunspacePoolId(final SessionId sessionId, final UUID runspacePoolId) {
		LOG.trace("Initiate PSRP RunspacePool with id = '{}'", runspacePoolId);
		final Shell shell = new Shell();
		shell.setName("Runspace");
		shell.setShellId(runspacePoolId.toString().toUpperCase());
		shell.getInputStreams().add("stdin");
		shell.getInputStreams().add("pr");
		shell.getOutputStreams().add("stdout");

		final OptionSetType optSetCreate = new OptionSetType();
		// OptionSet.mustUnderstand=true is REQUIRED
		optSetCreate.setMustUnderstand(true);
		OptionType optProtocolVersion = new OptionType();
		optProtocolVersion.setName("protocolversion");
		optProtocolVersion.setValue("2.3");
		// Option.MustComply=true is REQUIRED
		optProtocolVersion.setMustComply(true);
		optSetCreate.getOption().add(optProtocolVersion);

		shell.setCreationXml(PsrpUtils.buildOpenShellPayload(runspacePoolId, getFragmenter()));

		ResourceCreated resourceCreated = null;

		try {
			resourceCreated = winrm.create(
					shell,
					POWERSHELL_RESOURCE_URI,
					sessionId.getValue(),
					params.maxEnvelopeSize(),
					operationTimeout,
					locale,
					optSetCreate
			);
		} catch (RuntimeException e) {
			RetryingProxyHandler.checkForRootErrorAuthorizationLoopAndPropagateAnnotated(e);
			throw e;
		}
		return new ShellId(extractShellId(resourceCreated));
	}

	void waitForRunning(final PsrpPipelineResponseReader responseReader) {

		LOG.trace("Wait for running PSRP RunspacePool for state OPENED...");

		final Receive receive = new Receive();
		//receive.setSequenceId(BigDecimal.ZERO);
		DesiredStreamType stream = new DesiredStreamType();
		stream.setValue("stdout");
		receive.setDesiredStream(stream);
		final OptionSetType optSetCmd = new OptionSetType();
		optSetCmd.setMustUnderstand(true);
		OptionType optKeepalive = new OptionType();
		optKeepalive.setName("WSMAN_CMDSHELL_OPTION_KEEPALIVE");
		optKeepalive.setValue("TRUE");
		optSetCmd.getOption().add(optKeepalive);

		var state = PsrpRunspacepoolState.OPENING;
		while (state != PsrpRunspacepoolState.OPENED) {
			// receive all new messages, but don't wait for command/pipeline done
			final var messages = responseReader.read(receive, optSetCmd, false);
			for (final var message : messages) {
				// skip all messages until RunspacePool event
				if (message.type() == PsrpMessageType.RUNSPACEPOOL_STATE) {
					state = PsrpPipelineResponseMessageParser.INSTANCE.extractRunspacePoolState(message.data())
							.orElse(state);
				} else {
					LOG.trace("While waiting for running PSRP pipeline, skip message of type '{}'", message.type());
				}
			}
		}

		LOG.trace("Received PSRP RunspacePool state = '{}'", state);

	}

	String createPipeline(final SessionId sessionId,
						  final ShellId shellId,
						  final UUID runspacePoolId,
						  final UUID commandId,
						  final String data) {

		LOG.trace("Create PSRP pipeline (commandId {}): {}", commandId, data);

		final var serverCommandIdHolder = new AtomicReference<String>();
		final var fragments = getFragmenter().fragment(PsrpMessageFactory.createPipeline(runspacePoolId, commandId, data));
		for (final var fragment : fragments) {

			LOG.trace("Sending PSRP pipeline fragment: {}", fragment);

			try {

				if (fragment.firstFragment()) {
					final CommandLine cmdLine = new CommandLine();
					cmdLine.setCommandId(commandId.toString().toUpperCase());
					cmdLine.setCommand("");
					cmdLine.getArguments().add(PsrpUtils.base64Encode(fragment.bytes()));
					final OptionSetType optSetCmd = new OptionSetType();
					optSetCmd.setMustUnderstand(true);
					OptionType optSkipCmdShell = new OptionType();
					optSkipCmdShell.setName("WINRS_SKIP_CMD_SHELL");
					optSkipCmdShell.setValue("FALSE");
					optSetCmd.getOption().add(optSkipCmdShell);
					final var response = winrm.command(
							cmdLine,
							POWERSHELL_RESOURCE_URI,
							sessionId.getValue(),
							params.maxEnvelopeSize(),
							operationTimeout,
							locale,
							createShellSelector(shellId),
							optSetCmd
					);
					serverCommandIdHolder.set(response.getCommandId());
				} else {
					final var send = new Send();
					final var stream = new StreamType();
					stream.setCommandId(commandId.toString().toUpperCase());
					stream.setName("stdin");
					stream.setValue(fragment.bytes());
					send.setStream(stream);
					winrm.send(
							send,
							POWERSHELL_RESOURCE_URI,
							sessionId.getValue(),
							params.maxEnvelopeSize(),
							operationTimeout,
							locale,
							createShellSelector(shellId),
							null
					);
				}

			} catch (SOAPFaultException soapFault) {
				assertFaultCode(
						soapFault,
						WSMAN_FAULT_CODE_OPERATION_TIMEOUT_EXPIRED,
						retryReceiveAfterOperationTimeout
				);
			}
		}

		return serverCommandIdHolder.get();
	}

	int receiveCommandOutput(final SessionId sessionId,
							 final ShellId shellId,
							 final String commandId,
							 final PsrpPipelineResponseReader responseReader,
							 final Writer outWriter,
							 final Writer errWriter) {

		final Receive receive = new Receive();
		DesiredStreamType stream = new DesiredStreamType();
		stream.setValue("stdout");
		stream.setCommandId(commandId);
		receive.setDesiredStream(stream);
		final OptionSetType optSetCmd = new OptionSetType();
		OptionType optKeepalive = new OptionType();
		optKeepalive.setName("WSMAN_CMDSHELL_OPTION_KEEPALIVE");
		optKeepalive.setValue("TRUE");
		optSetCmd.getOption().add(optKeepalive);

		enum StreamType {
			out,
			err
		}

		final var exitCode = new AtomicInteger(0);

		for (final var message : responseReader.read(receive, optSetCmd, true)) {

			PsrpPipelineResponseMessageParser.INSTANCE.extractExitCode(message).ifPresent(exitCode::set);

			final var type = switch (message.type()) {
				case PIPELINE_STATE -> {
					final var state = PsrpPipelineResponseMessageParser.INSTANCE.extractPipelineState(message.data())
							.orElse(PsrpPipelineState.FAILED);
					if (state == PsrpPipelineState.FAILED) {
						yield StreamType.err;
					} else {
						yield StreamType.out;
					}
				}
				case PIPELINE_HOST_CALL -> {
					if (message.data().contains("WriteError")) {
						yield StreamType.err;
					} else {
						yield StreamType.out;
					}
				}
				case ERROR_RECORD -> StreamType.err;
				default -> StreamType.out;
			};

			PsrpPipelineResponseMessageParser.INSTANCE.extractText(message)
					.ifPresent(text -> {
						try {
							switch (type) {
								case err -> errWriter.write(text);
								case out -> outWriter.write(text);
							}
						} catch (final IOException e) {
							throw new IllegalStateException("Failed writing to command output writer", e);
						}
					});

		}
		return exitCode.get();
	}

	private void assertFaultCode(SOAPFaultException soapFault, String code, Predicate<String> retry) {
		try {
			NodeList faultDetails = soapFault.getFault().getDetail().getChildNodes();
			for (int i = 0; i < faultDetails.getLength(); i++) {
				if (faultDetails.item(i).getLocalName().equals("WSManFault")) {
					if (faultDetails.item(i).getAttributes().getNamedItem("Code").getNodeValue().equals(code)
							&& retry.test(code)) {
						LOG.trace("winrm client {} received error 500 response with code {}, response {}", this, code, soapFault);
						return;
					} else {
						throw soapFault;
					}
				}
			}
			throw soapFault;
		} catch (NullPointerException e) {
			LOG.debug("Error reading Fault Code {}", soapFault.getFault());
			throw soapFault;
		}
	}

	private void assertFaultCode(SOAPFaultException soapFault, String code) {
		assertFaultCode(soapFault, code, x -> true);
	}

	private void releaseCommand(final SessionId sessionId, final ShellId shellId, final String commandId) {
		final Signal signal = new Signal();
		signal.setCommandId(commandId);
		signal.setCode("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");

		winrm.signal(signal, POWERSHELL_RESOURCE_URI, sessionId.getValue(), MAX_ENVELOPER_SIZE, operationTimeout, locale, createShellSelector(shellId));
	}

	private void closeShell(final SessionId sessionId, final ShellId shellId) {
		if (!closedSessions.contains(sessionId)) {
			winrm.delete(POWERSHELL_RESOURCE_URI, sessionId.getValue(), params.maxEnvelopeSize(), operationTimeout, locale, createShellSelector(shellId));
			closedSessions.add(sessionId);
		}
	}

	private static String extractShellId(ResourceCreated resourceCreated) {
		XPath xpath = XPathFactory.newInstance().newXPath();
		for (Element el : resourceCreated.getAny()) {
			String shellId;
			try {
				shellId = xpath.evaluate("//*[local-name()='Selector' and @Name='ShellId']", el);
			} catch (XPathExpressionException e) {
				throw new IllegalStateException(e);
			}
			if (shellId != null && !shellId.isEmpty()) {
				shellId = shellId.toUpperCase();
				LOG.trace("Extract shellId '{}'", shellId);
				return shellId;
			}
		}
		throw new IllegalStateException("Shell ID not found in " + resourceCreated);
	}

	static SelectorSetType createShellSelector(final ShellId shellId) {
		SelectorSetType shellSelector = new SelectorSetType();
		SelectorType sel = new SelectorType();
		sel.setName("ShellId");
		sel.getContent().add(shellId.getValue());
		shellSelector.getSelector().add(sel);
		return shellSelector;
	}

	static class SessionId {

		private final String value;

		public SessionId(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

	static class ShellId {

		private final String value;

		public ShellId(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

	}

	static abstract class SessionImpl implements RunspacePoolSession {

		private final SessionId sessionId;
		private final ShellId shellId;
		private final PsrpPipelineResponseReader responseReader;

		public SessionImpl(final SessionId sessionId,
						   final ShellId shellId,
						   final PsrpPipelineResponseReader responseReader) {
			this.sessionId = sessionId;
			this.shellId = shellId;
			this.responseReader = responseReader;
		}

		public SessionId getSessionId() {
			return sessionId;
		}

		public ShellId getShellId() {
			return shellId;
		}

		public PsrpPipelineResponseReader getResponseReader() {
			return responseReader;
		}
	}

	static class ParamsResolverImpl implements ParamsResolver {

		private final WinRm winrm;
		private Integer maxFragmentBlobSize;
		private Integer maxEnvelopeSize;
		private byte[] emptyPipelineEnvelope;

		public ParamsResolverImpl(WinRm winrm) {
			this.winrm = winrm;
		}

		@Override
		public int maxFragmentBlobSize() {
			if (maxFragmentBlobSize == null) {
				// size physical (gross)
				final var maxBytes = maxEnvelopeSize() - emptyPipelineEnvelopeSize();
				// size logical (net)
				// - base64 encoding requires 33% more
				// - fragment headers
				maxFragmentBlobSize = (maxBytes / 4 * 3) - PsrpFragment.HEADER_LENGTH;
			}
			return maxFragmentBlobSize;
		}

		@Override
		public int maxEnvelopeSize() {
			if (maxEnvelopeSize == null) {
				try {
					final var config = winrm.config(WSMAN_CONFIG_RESOURCE_URI);
					maxEnvelopeSize = config.getMaxEnvelopeSizekb() * 1024;
					LOG.trace("Dynamically evaluated maxEnvelopeSize = '{}'", maxEnvelopeSize);
				} catch (final SOAPFaultException e) {
					if ("Access is denied.".equals(e.getMessage())) {
						LOG.debug("Failed to request winrm config for maxEnvelopeSizeKb, switching to default", e);
						maxEnvelopeSize = PsrpMessageFragmenter.DEFAULT_BLOB_LENGTH;
					} else {
						throw e;
					}
				}
			}
			return maxEnvelopeSize;
		}

		@Override
		public int emptyPipelineEnvelopeSize() {
			// FIXME
			return 10_000;
		}
	}

}
