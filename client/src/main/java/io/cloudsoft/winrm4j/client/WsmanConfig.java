package io.cloudsoft.winrm4j.client;

import java.util.Objects;

public class WsmanConfig {

	private final long maxEnvelopeSize;

	public WsmanConfig(final int maxEnvelopeSize) {
		this.maxEnvelopeSize = maxEnvelopeSize * 1024L;
	}

	public long getMaxEnvelopeSize() {
		return maxEnvelopeSize;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WsmanConfig that = (WsmanConfig) o;
		return maxEnvelopeSize == that.maxEnvelopeSize;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(maxEnvelopeSize);
	}

}
