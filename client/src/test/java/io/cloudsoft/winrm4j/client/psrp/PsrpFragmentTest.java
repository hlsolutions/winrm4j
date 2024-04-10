package io.cloudsoft.winrm4j.client.psrp;

import static org.testng.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.testng.annotations.Test;

public class PsrpFragmentTest {

	@Test
	public void testSimple() {
		final var fragment = new PsrpFragment(
				42,
				"hello world".getBytes(StandardCharsets.UTF_8),
				1,
				true,
				true
		);
		final var fragment2 = PsrpFragment.read(fragment.bytes());
		assertEquals(fragment2.objectId(), fragment.objectId(), "invalid fragment.objectId");
		assertEquals(fragment2.fragmentId(), fragment.fragmentId(), "invalid fragment.fragmentId");
		assertEquals(fragment2.firstFragment(), fragment.firstFragment(), "invalid fragment.firstFragment");
		assertEquals(fragment2.lastFragment(), fragment.lastFragment(), "invalid fragment.lastFragment");
		assertEquals(fragment2.blob(), fragment.blob(), "invalid fragment.blob");
	}

	//@Test
	public void forDebugging() {
		PsrpFragment.read(Base64.getDecoder().decode("AAAAAAAAAAEAAAAAAAAAAAMAAADKAQAAAAIAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAO+7vzxPYmogUmVmSWQ9IjAiPjxNUz48VmVyc2lvbiBOPSJwcm90b2NvbHZlcnNpb24iPjIuMzwvVmVyc2lvbj48VmVyc2lvbiBOPSJQU1ZlcnNpb24iPjIuMDwvVmVyc2lvbj48VmVyc2lvbiBOPSJTZXJpYWxpemF0aW9uVmVyc2lvbiI+MS4xLjAuMTwvVmVyc2lvbj48L01TPjwvT2JqPg=="));
	}

}