package io.cloudsoft.winrm4j.client;

import static io.cloudsoft.winrm4j.client.InteractiveShellCommand.processStreamResults;
import static org.testng.Assert.assertEquals;

import java.io.StringWriter;

import org.testng.annotations.Test;

public class InteractiveShellCommandTest {

    @Test
    public void includeFirstLineAndWaitFor() {
        final var stdout = new StringWriter();
        final var stderr = new StringWriter();
        processStreamResults(
                """
                        Function Prompt {">"}\r
                        Hello World\r
                        marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09\r
                        """,
                "",
                false,
                null,
                "marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09",
                stdout,
                stderr
        );
        assertEquals(
                stdout.toString(),
                """
                        Function Prompt {">"}\r
                        Hello World\r
                        """,
                "invalid stdout"
        );
    }

    @Test
    public void skipFirstLineAndWaitFor() {
        final var stdout = new StringWriter();
        final var stderr = new StringWriter();
        processStreamResults(
                """
                        Function Prompt {">"}\r
                        Hello World\r
                        marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09\r
                        """,
                "",
                true,
                null,
                "marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09",
                stdout,
                stderr
        );
        assertEquals(
                stdout.toString(),
                """
                        Hello World\r
                        """,
                "invalid stdout"
        );
    }

    @Test
    public void skipFirstLineNotHavingPrompt() {
        final var stdout = new StringWriter();
        final var stderr = new StringWriter();
        processStreamResults(
                """
                        Function Prompt {">"}\r
                        Hello World\r
                        marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09\r
                        """,
                "",
                true,
                ">",
                "marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09",
                stdout,
                stderr
        );
        assertEquals(
                stdout.toString(),
                """
                        Hello World\r
                        """,
                "invalid stdout"
        );
    }

    @Test
    public void skipFirstLineIgnorePromptAndWaitFor() {
        final var stdout = new StringWriter();
        final var stderr = new StringWriter();
        processStreamResults(
                """
                        Function Prompt {">"}\r
                        > Write-Host -NoNewline "Hello"\r
                        > Write-Host " World"; Write-Host -NoNewline "marker="; Write-Host "3908f554-a5f7-48e6-8ca0-5201bcaecf09"\r
                        Hello World\r
                        marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09\r
                        """,
                "",
                true,
                ">",
                "marker=3908f554-a5f7-48e6-8ca0-5201bcaecf09",
                stdout,
                stderr
        );
        assertEquals(
                stdout.toString(),
                """
                        Hello World\r
                        """,
                "invalid stdout"
        );
    }

}