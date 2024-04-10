package io.cloudsoft.winrm4j.client.psrp;

public enum PsrpRunspacepoolState {

    BEFORE_OPEN,
    OPENING,
    OPENED,
    CLOSED,
    CLOSING,
    BROKEN,
    NEGOTIATION_SENT,
    NEGOTIATION_SUCCEEDED,
    CONNECTING,
    DISCONNECTED;

}
