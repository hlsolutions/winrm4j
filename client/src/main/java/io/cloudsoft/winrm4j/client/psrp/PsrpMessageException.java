package io.cloudsoft.winrm4j.client.psrp;

import java.io.Serial;

public class PsrpMessageException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2366717440074916394L;

    public PsrpMessageException() {
    }

    public PsrpMessageException(String message) {
        super(message);
    }

    public PsrpMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public PsrpMessageException(Throwable cause) {
        super(cause);
    }

    public PsrpMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
