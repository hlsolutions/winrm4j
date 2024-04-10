package io.cloudsoft.winrm4j.client.psrp;

public enum PsrpMessageType {

    SESSION_CAPABILITY(0x00010002),
    INIT_RUNSPACEPOOL(0x00010004),
    CREATE_PIPELINE(0x00021006),
    CONNECT_RUNSPACEPOOL(0x00010008),
    RUNSPACE_INIT_DATA(0x0002100b),
    RESET_RUNSPACE_STATE(0x0002100c),
    SET_MAX_RUNSPACES(0x00021002),
    SET_MIN_RUNSPACES(0x00021003),
    RUNSPACE_AVAILABILITY(0x00021004),
    RUNSPACEPOOL_STATE(0x00021005),
    GET_AVAILABLE_RUNSPACES(0x00021007),
    USER_EVENT(0x00021008),
    APPLICATION_PRIVATE_DATA(0x00021009),
    GET_COMMAND_METADATA(0x0002100a),
    RUNSPACEPOOL_HOST_CALL(0x00021100),
    RUNSPACEPOOL_HOST_RESPONSE(0x00021101),
    PIPELINE_INPUT(0x00041002),
    END_OF_PIPELINE_INPUT(0x00041003),
    PIPELINE_OUTPUT(0x00041004),
    ERROR_RECORD(0x00041005),
    PIPELINE_STATE(0x00041006),
    DEBUG_RECORD(0x00041007),
    VERBOSE_RECORD(0x00041008),
    WARNING_RECORD(0x00041009),
    PROGRESS_RECORD(0x00041010),
    INFORMATION_RECORD(0x00041011),
    PIPELINE_HOST_CALL(0x00041100),
    PIPELINE_HOST_RESPONSE(0x00041101);

    private final int value;

    public static PsrpMessageType resolveByValue(final int value) {
        for (final var item : PsrpMessageType.values()) {
            if (item.value() == value) {
                return item;
            }
        }
        throw new IllegalStateException("Unexpected value: " + value);
    }

    PsrpMessageType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
