package io.cloudsoft.winrm4j.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import jakarta.xml.ws.soap.SOAPFaultException;

import io.cloudsoft.winrm4j.client.shell.CommandLine;
import io.cloudsoft.winrm4j.client.shell.CommandStateType;
import io.cloudsoft.winrm4j.client.shell.DesiredStreamType;
import io.cloudsoft.winrm4j.client.shell.Receive;
import io.cloudsoft.winrm4j.client.shell.ReceiveResponse;
import io.cloudsoft.winrm4j.client.shell.Send;
import io.cloudsoft.winrm4j.client.shell.StreamType;
import io.cloudsoft.winrm4j.client.wsman.CommandResponse;
import io.cloudsoft.winrm4j.client.wsman.Locale;
import io.cloudsoft.winrm4j.client.wsman.OptionSetType;
import io.cloudsoft.winrm4j.client.wsman.OptionType;
import io.cloudsoft.winrm4j.client.wsman.SelectorSetType;
import io.cloudsoft.winrm4j.client.wsman.SelectorType;
import io.cloudsoft.winrm4j.client.wsman.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

public class InteractiveShellCommand implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(InteractiveShellCommand.class.getName());

    private static final String COMMAND_STATE_DONE = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Done";
    private static final String COMMAND_STATE_RUNNING = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Running";
    private static final String COMMAND_STATE_PENDING = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandState/Pending";

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

    private WinRm winrm;
    private SelectorSetType shellSelector;

    private String operationTimeout;
    /**
     * Define if a new Receive request will be send when the server returns a fault with the code
     * {@link #WSMAN_FAULT_CODE_OPERATION_TIMEOUT_EXPIRED}.
     */
    private Predicate<String> retryReceiveAfterOperationTimeout;
    private final Locale locale;

    private int numberOfReceiveCalls;

    public InteractiveShellCommand(WinRm winrm, String shellId, String operationTimeout, Predicate<String> retryReceiveAfterOperationTimeout,
                                   Locale locale) {
        this.winrm = winrm;
        this.shellSelector = createShellSelector(shellId);
        this.operationTimeout = operationTimeout;
        this.retryReceiveAfterOperationTimeout = retryReceiveAfterOperationTimeout;
        this.locale = locale;
    }

    private SelectorSetType createShellSelector(String shellId) {
        SelectorSetType shellSelector = new SelectorSetType();
        SelectorType sel = new SelectorType();
        sel.setName("ShellId");
        sel.getContent().add(shellId);
        shellSelector.getSelector().add(sel);
        return shellSelector;
    }

    public interface Session extends Closeable {

        int execute(String command, Writer out, Writer err);

        @Override
        void close() throws SOAPFaultException;
    }

    public Session openCmd() {
        return openCmd(Writer.nullWriter(), Writer.nullWriter());
    }

    public Session openCmd(Writer out, Writer err) {
        final CommandLine cmdLine = new CommandLine();
        cmdLine.setCommand("cmd");
        final OptionSetType optSetCmd = new OptionSetType();
        OptionType optConsolemodeStdin = new OptionType();
        optConsolemodeStdin.setName("WINRS_CONSOLEMODE_STDIN");
        optConsolemodeStdin.setValue("TRUE");
        optSetCmd.getOption().add(optConsolemodeStdin);
        OptionType optSkipCmdShell = new OptionType();
        optSkipCmdShell.setName("WINRS_SKIP_CMD_SHELL");
        optSkipCmdShell.setValue("FALSE");
        optSetCmd.getOption().add(optSkipCmdShell);

        numberOfReceiveCalls = 0;

        CommandResponse cmdResponse = winrm.command(cmdLine, WinRmClient.RESOURCE_URI, null, WinRmClient.MAX_ENVELOPER_SIZE, operationTimeout, locale, shellSelector, optSetCmd);
        String commandId = cmdResponse.getCommandId();
        // reduce the prompt to a simple ">"
        sendCommand(commandId, "@echo off");
        sendCommand(commandId, "prompt $g");
        receiveCommand(commandId,
                false,
                null,
                null,
                out,
                err);

        return new Session() {
            @Override
            public int execute(String command, Writer out, Writer err) {
                String marker = UUID.randomUUID().toString();
                String cmd = command +
                        " & " +
                        "(echo | set /p x=marker=) & echo " + marker;
                sendCommand(commandId, cmd);
                receiveCommand(commandId,
                        true,
                        ">",
                        "marker=" + marker,
                        out,
                        err);
                // extract status code
                sendCommand(commandId, "echo %ErrorLevel%");
                int exitCode = 0;
                {
                    final var writer = new StringWriter();
                    receiveCommand(commandId,
                            true,
                            ">",
                            null,
                            writer,
                            Writer.nullWriter());
                    try {
                        exitCode = Integer.parseInt(writer.toString().strip());
                    } catch (final Exception ignored) {
                        // ignored
                    }
                }
                return exitCode;
            }

            @Override
            public void close() throws SOAPFaultException {
                try {
                    sendCommand(commandId, "exit");
                    receiveCommand(commandId,
                            false,
                            null,
                            null,
                            Writer.nullWriter(),
                            Writer.nullWriter());
                } catch (SOAPFaultException soapFault) {
                    assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
                }
                try {
                    releaseCommand(commandId);
                } catch (SOAPFaultException soapFault) {
                    assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
                }
            }
        };
    }

    public Session openPs() {
        return openPs(Writer.nullWriter(), Writer.nullWriter());
    }

    public Session openPs(Writer out, Writer err) {
        final CommandLine cmdLine = new CommandLine();
        cmdLine.setCommand("powershell");
        final OptionSetType optSetCmd = new OptionSetType();
        OptionType optConsolemodeStdin = new OptionType();
        optConsolemodeStdin.setName("WINRS_CONSOLEMODE_STDIN");
        optConsolemodeStdin.setValue("TRUE");
        optSetCmd.getOption().add(optConsolemodeStdin);
        OptionType optSkipCmdShell = new OptionType();
        optSkipCmdShell.setName("WINRS_SKIP_CMD_SHELL");
        optSkipCmdShell.setValue("FALSE");
        optSetCmd.getOption().add(optSkipCmdShell);

        numberOfReceiveCalls = 0;

        CommandResponse cmdResponse = winrm.command(cmdLine, WinRmClient.RESOURCE_URI, null, WinRmClient.MAX_ENVELOPER_SIZE, operationTimeout, locale, shellSelector, optSetCmd);
        String commandId = cmdResponse.getCommandId();
        // reduce the prompt to a simple ">"
        sendCommand(commandId, "Function Prompt {\">\"}");
        receiveCommand(commandId,
                false,
                null,
                null,
                out,
                err);

        return new Session() {
            @Override
            public int execute(String command, Writer out, Writer err) {
                String marker = UUID.randomUUID().toString();
                String cmd = command +
                        "; Write-Host -NoNewline \"marker=\"; Write-Host \"" + marker + "\"";
                sendCommand(commandId, cmd);
                receiveCommand(commandId,
                        true,
                        ">",
                        "marker=" + marker,
                        out,
                        err);
                // extract status code
                sendCommand(commandId, "Write-Output \"exit=$? code=$LastExitCode\"");
                int exitCode = 0;
                {
                    final var writer = new StringWriter();
                    receiveCommand(commandId,
                            true,
                            ">",
                            null,
                            writer,
                            Writer.nullWriter());
                    for (String str : writer.toString().strip().split(" ")) {
                        String[] val = str.split("=", 2);
                        if (val.length != 2) {
                            continue;
                        }
                        switch (val[0]) {
                            case "exit" -> {
                                if (Boolean.parseBoolean(val[1])) {
                                    exitCode = 0;
                                } else {
                                    exitCode = 1;
                                }
                            }
                            case "code" -> {
                                try {
                                    exitCode = Integer.parseInt(val[1]);
                                } catch (final Exception ignored) {
                                    // ignored
                                }
                            }
                        }
                    }
                }
                return exitCode;
            }

            @Override
            public void close() throws SOAPFaultException {
                try {
                    sendCommand(commandId, "exit");
                    receiveCommand(commandId,
                            false,
                            null,
                            null,
                            Writer.nullWriter(),
                            Writer.nullWriter());
                } catch (SOAPFaultException soapFault) {
                    assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
                }
                try {
                    releaseCommand(commandId);
                } catch (SOAPFaultException soapFault) {
                    assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
                }
            }
        };
    }

    private void sendCommand(String commandId, String data) {
        final Send send = new Send();
        StreamType stream = new StreamType();
        stream.setCommandId(commandId);
        stream.setName("stdin");
        stream.setValue((data + "\r\n").getBytes(StandardCharsets.UTF_8));
        send.setStream(stream);
        final OptionSetType optSetCmd = new OptionSetType();
        OptionType optKeepalive = new OptionType();
        optKeepalive.setName("WSMAN_CMDSHELL_OPTION_KEEPALIVE");
        optKeepalive.setValue("TRUE");
        optSetCmd.getOption().add(optKeepalive);
        try {
            LOG.trace("Sending command data {}: {}", commandId, data);
            winrm.send(send, WinRmClient.RESOURCE_URI, null, WinRmClient.MAX_ENVELOPER_SIZE, operationTimeout, locale, shellSelector, optSetCmd);
        } catch (SOAPFaultException soapFault) {
            assertFaultCode(soapFault, WSMAN_FAULT_CODE_OPERATION_TIMEOUT_EXPIRED,
                    retryReceiveAfterOperationTimeout);
        }
    }

    /**
     * Process the received stream outputs for boilerplate and verbose data.
     *
     * @param stdout       received stdout
     * @param stderr       received stderr
     * @param skipFirstOut skip the first line of stdout (used because of the function prompt)
     * @param promptPrefix ignore lines which starts with this prompt
     * @param waitFor      marker until the output should be read
     * @param out          destination writer for stdout
     * @param err          destination writer for stderr
     */
    static void processStreamResults(String stdout,
                                     String stderr,
                                     boolean skipFirstOut,
                                     String promptPrefix,
                                     String waitFor,
                                     Writer out,
                                     Writer err) {

        if (!stdout.isEmpty()) {
            try {
                String[] split = stdout.split("\r?\n");
                for (int i = 0; i < split.length; i++) {
                    if (skipFirstOut && i == 0) {
                        continue;
                    }
                    String str = split[i];
                    if (promptPrefix != null && str.startsWith(promptPrefix)) {
                        continue;
                    }
                    if (waitFor != null && str.contains(waitFor)) {
                        return;
                    }
                    out.write(str + "\r\n");
                }
                out.flush();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        if (!stderr.isEmpty()) {
            try {
                err.write(stderr);
                err.flush();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

    }

    private Optional<Integer> receiveCommand(String commandId,
                                             boolean skipFirstOut,
                                             String promptPrefix,
                                             String waitFor,
                                             Writer out,
                                             Writer err) {

        final var tempOut = new StringWriter();
        final var tempErr = new StringWriter();

        while (true) {
            final Receive receive = new Receive();
            DesiredStreamType stream = new DesiredStreamType();
            stream.setCommandId(commandId);
            stream.setValue("stdout stderr");
            receive.setDesiredStream(stream);
            final OptionSetType optSetCmd = new OptionSetType();
            OptionType optKeepalive = new OptionType();
            optKeepalive.setName("WSMAN_CMDSHELL_OPTION_KEEPALIVE");
            optKeepalive.setValue("TRUE");
            optSetCmd.getOption().add(optKeepalive);

            try {
                numberOfReceiveCalls++;
                // Fetch the next batch of command's response (maybe not the last one)...
                ReceiveResponse receiveResponse = winrm.receive(receive, WinRmClient.RESOURCE_URI, null, WinRmClient.MAX_ENVELOPER_SIZE, operationTimeout, locale, shellSelector, optSetCmd);
                // ... and process the result into the temp writers.
                getStreams(receiveResponse, tempOut, tempErr);

                CommandStateType state = receiveResponse.getCommandState();
                // https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-wsmv/bd5802af-51ad-4f1b-9a5c-7aa499d5eee9
                // either Done, Running, or Pending
                // Done means the command/shell is done (completed)
                // Pending means the command/shell is busy
                // Running means the command/shell is ready (which means the interactive command probably finished)
                if (COMMAND_STATE_DONE.equals(state.getState())) {
                    processStreamResults(">" + tempOut.toString(),
                            tempErr.toString(),
                            skipFirstOut,
                            promptPrefix,
                            waitFor,
                            out,
                            err);
                    LOG.trace("Received final stdout: " + tempOut.toString());
                    return Optional.of(state.getExitCode().intValue());
                }
                if (COMMAND_STATE_RUNNING.equals(state.getState())) {
                    if (waitFor != null && !tempOut.toString().contains(waitFor)) {
                        continue;
                    }
                    processStreamResults(">" + tempOut.toString(),
                            tempErr.toString(),
                            skipFirstOut,
                            promptPrefix,
                            waitFor,
                            out,
                            err);
                    LOG.trace("Received final stdout: " + tempOut.toString());
                    return Optional.empty();
                } else {
                    LOG.debug("{} is not done. Response it received: {} / {}", this, state.getState(), receiveResponse);
                }
            } catch (SOAPFaultException soapFault) {
                /**
                 * If such Exception which has a code 2150858793 the client is expected to again trigger immediately a receive request.
                 * https://msdn.microsoft.com/en-us/library/cc251676.aspx
                 */
                assertFaultCode(soapFault, WSMAN_FAULT_CODE_OPERATION_TIMEOUT_EXPIRED,
                        retryReceiveAfterOperationTimeout);
            }
        }
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

    /**
     * @deprecated since 0.6.0. Implementation detail, access will be removed in future versions
     */
    @Deprecated
    public int getNumberOfReceiveCalls() {
        return numberOfReceiveCalls;
    }

    /**
     * This reads the streams of the given response and applies their values to the
     * specified writers. Supported are streams of the names <code>stdout</code> and <code>stderr</code>.
     *
     * @param receiveResponse the received response containing the shells stream outputs
     * @param out             destination stdout writer
     * @param err             destination stderr writer
     */
    private void getStreams(final ReceiveResponse receiveResponse, final Writer out, final Writer err) {
        final List<StreamType> streams = receiveResponse.getStream();
        boolean alreadySkippedOut = false;
        for (final StreamType s : streams) {
            byte[] value = s.getValue();
            if (value == null) continue;
            if (out != null && "stdout".equals(s.getName())) {
                try {
                    //TODO use passed locale?
                    if (value.length > 0) {
                        final var str = new String(value);
                        LOG.trace("received part stdout: {}", str);
                        out.write(str);
                        out.flush();
                    }
                    if (Boolean.TRUE.equals(s.isEnd())) {
                        out.close();
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            if (err != null && "stderr".equals(s.getName())) {
                try {
                    //TODO use passed locale?
                    if (value.length > 0) {
                        final var str = new String(value);
                        LOG.trace("received part stderr: {}", str);
                        err.write(str);
                        err.flush();
                    }
                    if (Boolean.TRUE.equals(s.isEnd())) {
                        err.close();
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private void releaseCommand(String commandId) {
        final Signal signal = new Signal();
        signal.setCommandId(commandId);
        signal.setCode("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/signal/terminate");

        winrm.signal(signal, WinRmClient.RESOURCE_URI, null, WinRmClient.MAX_ENVELOPER_SIZE, operationTimeout, locale, shellSelector);
    }


    @Override
    public void close() {
        try {
            winrm.delete(WinRmClient.RESOURCE_URI, null, WinRmClient.MAX_ENVELOPER_SIZE, operationTimeout, locale, shellSelector);
        } catch (SOAPFaultException soapFault) {
            assertFaultCode(soapFault, WSMAN_FAULT_CODE_SHELL_WAS_NOT_FOUND);
        }
    }
}
