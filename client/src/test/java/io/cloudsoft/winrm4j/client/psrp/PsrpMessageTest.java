package io.cloudsoft.winrm4j.client.psrp;

import static org.testng.Assert.assertEquals;

import java.util.Base64;
import java.util.UUID;

import org.testng.annotations.Test;

public class PsrpMessageTest {

	@Test
	public void testSimple() {
		final var message = new PsrpMessage(
				UUID.fromString("7a11f081-15f4-409c-b81f-79d18711cd32"),
				UUID.fromString("345d80ee-c3fb-4df9-9e74-eaa71b2ca755"),
				PsrpMessageType.INIT_RUNSPACEPOOL,
				PsrpDestination.SERVER,
				"hello world"
		);
		final var message2 = PsrpMessage.read(message.bytes());
		assertEquals(message2, message, "invalid message");
	}

	//@Test
	public void forDebugging() {
		final var fragment = PsrpFragment.read(Base64.getDecoder().decode("AAAAAAAAAAEAAAAAAAAAAAMAAADKAQAAAAIAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAO+7vzxPYmogUmVmSWQ9IjAiPjxNUz48VmVyc2lvbiBOPSJwcm90b2NvbHZlcnNpb24iPjIuMzwvVmVyc2lvbj48VmVyc2lvbiBOPSJQU1ZlcnNpb24iPjIuMDwvVmVyc2lvbj48VmVyc2lvbiBOPSJTZXJpYWxpemF0aW9uVmVyc2lvbiI+MS4xLjAuMTwvVmVyc2lvbj48L01TPjwvT2JqPg=="));
		final var message = PsrpMessage.read(fragment.blob());
		System.out.println(message.data());
	}

}