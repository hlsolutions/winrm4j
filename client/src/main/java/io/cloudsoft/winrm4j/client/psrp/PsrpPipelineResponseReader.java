package io.cloudsoft.winrm4j.client.psrp;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.cloudsoft.winrm4j.client.WinRm;
import io.cloudsoft.winrm4j.client.shell.Receive;
import io.cloudsoft.winrm4j.client.shell.ReceiveResponse;
import io.cloudsoft.winrm4j.client.wsman.Locale;
import io.cloudsoft.winrm4j.client.wsman.OptionSetType;
import io.cloudsoft.winrm4j.client.wsman.SelectorSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsrpPipelineResponseReader {

	private static final Logger LOG = LoggerFactory.getLogger(PsrpPipelineResponseReader.class);

	private static final String POWERSHELL_RESOURCE_URI = "http://schemas.microsoft.com/powershell/Microsoft.PowerShell";
	private static final String COMMAND_STATE_DONE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done";

	private final WinRm winrm;
	private final Supplier<SelectorSetType> shellSelectorSupplier;
	private final Supplier<Integer> maxEnvelopeSizeSupplier;
	private final Supplier<String> operationTimeoutSupplier;
	private final Supplier<Locale> localeSupplier;
	private final Supplier<String> sessionIdSupplier;

	record StreamResponse(String stream,
						  byte[] data) {
	}

	public PsrpPipelineResponseReader(final WinRm winrm,
									  final Supplier<SelectorSetType> shellSelectorSupplier,
									  final Supplier<Integer> maxEnvelopeSizeSupplier,
									  final Supplier<String> operationTimeoutSupplier,
									  final Supplier<Locale> localeSupplier,
									  final Supplier<String> sessionIdSupplier) {
		this.winrm = winrm;
		this.shellSelectorSupplier = shellSelectorSupplier;
		this.maxEnvelopeSizeSupplier = maxEnvelopeSizeSupplier;
		this.operationTimeoutSupplier = operationTimeoutSupplier;
		this.localeSupplier = localeSupplier;
		this.sessionIdSupplier = sessionIdSupplier;
	}

	public List<PsrpMessage> read(final Receive receive,
								  final OptionSetType optionSetType,
								  final boolean waitForDoneState) {
		LOG.trace("Receive PSRP output");
		final var defragmenter = new PsrpMessageDefragmenter();
		final var messages = read0(receive, optionSetType, waitForDoneState)
				.flatMap(response -> response
						.getStream()
						.stream()
						.filter(s -> s.getValue() != null)
						.map(stream -> new StreamResponse(stream.getName(), stream.getValue()))
				)
				.map(StreamResponse::data)
				.flatMap(bytes -> PsrpFragment.readMulti(bytes).stream())
				.peek(fragment -> LOG.trace("Read PSRP fragment: {}", fragment))
				.flatMap(fragment -> defragmenter.defragment(fragment).stream())
				.peek(message -> LOG.trace("Read PSRP message: {}", message))
				.toList();
		if (defragmenter.hasFragments()) {
			LOG.debug("Incomplete inbound PSRP message fragments left");
		}
		return messages;
	}

	Stream<ReceiveResponse> read0(final Receive receive,
								  final OptionSetType optionSetType,
								  final boolean waitForDoneState) {
		final var result = new ArrayList<ReceiveResponse>();
		ReceiveResponse response = null;
		while (response == null || (waitForDoneState && (response.getCommandState() == null || !COMMAND_STATE_DONE.equals(response.getCommandState().getState())))) {
			response = winrm.receive(
					receive,
					POWERSHELL_RESOURCE_URI,
					sessionIdSupplier.get(),
					maxEnvelopeSizeSupplier.get(),
					operationTimeoutSupplier.get(),
					localeSupplier.get(),
					shellSelectorSupplier.get(),
					optionSetType
			);
			if (response != null) {
				result.add(response);
			}
		}
		return result.stream();
	}

}
