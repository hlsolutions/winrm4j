package io.cloudsoft.winrm4j.client.psrp;

public enum PsrpDestination {
    CLIENT(1),
    SERVER(2);

    private final int value;

    public static PsrpDestination resolveByValue(final int value) {
        for (final var item : PsrpDestination.values()) {
            if (item.value() == value) {
                return item;
            }
        }
        throw new IllegalStateException("Unexpected value: " + value);
    }

    PsrpDestination(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}
