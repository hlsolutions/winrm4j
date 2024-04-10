package io.cloudsoft.winrm4j.service;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.BindingType;

import io.cloudsoft.winrm4j.service.enumerate.EnumerateRequest;
import io.cloudsoft.winrm4j.service.enumerate.EnumerateResponse;
import io.cloudsoft.winrm4j.service.enumerate.PullRequest;
import io.cloudsoft.winrm4j.service.enumerate.PullResponse;
import io.cloudsoft.winrm4j.service.shell.CommandLine;
import io.cloudsoft.winrm4j.service.shell.Receive;
import io.cloudsoft.winrm4j.service.shell.ReceiveResponse;
import io.cloudsoft.winrm4j.service.shell.Send;
import io.cloudsoft.winrm4j.service.shell.SendResponse;
import io.cloudsoft.winrm4j.service.shell.Shell;
import io.cloudsoft.winrm4j.service.shell.SignalResponse;
import io.cloudsoft.winrm4j.service.config.ConfigResponse;
import io.cloudsoft.winrm4j.service.transfer.ResourceCreated;
import io.cloudsoft.winrm4j.service.wsman.CommandResponse;
import io.cloudsoft.winrm4j.service.wsman.Locale;
import io.cloudsoft.winrm4j.service.wsman.OptionSetType;
import io.cloudsoft.winrm4j.service.wsman.SelectorSetType;
import io.cloudsoft.winrm4j.service.wsman.Signal;

//https://msdn.microsoft.com/en-us/library/cc251731.aspx
//https://msdn.microsoft.com/en-us/library/cc251526.aspx

@WebService(targetNamespace="http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd")

// Can't use the annotation without adding an additional dependency, duplicating the built-in functionality
//@MemberSubmissionAddressing

// Soap 1.2 (uses the required application/soap+xml content type)
@BindingType("http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class WinRm {

    @WebMethod(operationName = "Send", action = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Send")
    @WebResult(name = "SendResponse", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
    @Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Send", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/SendResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public SendResponse send(
            @WebParam(name = "Send", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
            Send send,
            @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
            String resourceURI,
            @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
            String sessionId,
            @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
            int maxEnvelopeSize,
            @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
            String operationTimeout,
            @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
            Locale locale,
            @WebParam(name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
            SelectorSetType selectorSet,
            @WebParam(name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
            OptionSetType optionSet
    ) {
        return null;
    }

    @WebMethod(operationName = "Receive", action = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive")
    @WebResult(name="ReceiveResponse", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
    @Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/ReceiveResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public ReceiveResponse receive(
        @WebParam(name = "Receive", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
        Receive receive,
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        SelectorSetType selectorSet,
        @WebParam(name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        OptionSetType optionSet
    ) {
        return null;
    }

    @WebMethod(operationName = "Delete", action = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete")
    @WebResult(name = "DeleteResponse", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd")
    @Action(input = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete", output = "http://schemas.xmlsoap.org/ws/2004/09/transfer/DeleteResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void delete(
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        SelectorSetType selectorSet) {
    }

    @WebMethod(operationName = "Signal", action = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal")
    @WebResult(name = "SignalResponse", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd")
    @Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/SignalResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public SignalResponse signal(
        @WebParam(name = "Signal", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
        Signal signal,
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        SelectorSetType selectorSet
    ) {
        return null;
    }

    @WebMethod(operationName = "Command", action="http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command")
    @WebResult(name="CommandResponse", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
    @Action(input = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command", output = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell/CommandResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public CommandResponse command(
        @WebParam(name = "CommandLine", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
        CommandLine commandLine,
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "SelectorSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        SelectorSetType selectorSet,
        @WebParam(name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        OptionSetType optionSet
    ) {
        return null;
    }

    @WebMethod(operationName = "Create", action = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create")
    @Action(input = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Create", output = "http://schemas.xmlsoap.org/ws/2004/09/transfer/CreateResponse")
    @WebResult(name = "ResourceCreated", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/09/transfer", partName = "ResourceCreated")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public ResourceCreated create(
        @WebParam(name = "Shell", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell")
        Shell shell,
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        OptionSetType optionSet
    ) {

        return null;
    }

    @WebMethod(operationName = "Enumerate", action = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate")
    @Action(input = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Enumerate", output = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/EnumerateResponse")
    @WebResult(name = "EnumerateResponse", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/09/enumeration", partName = "EnumerateResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public EnumerateResponse enumerate(
        @WebParam(name = "Enumerate", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/09/enumeration")
        EnumerateRequest enumerate,
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        OptionSetType optionSet
    ) {
        return null;
    }

    @WebMethod(operationName = "EnumeratePull", action = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull")
    @Action(input = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/Pull", output = "http://schemas.xmlsoap.org/ws/2004/09/enumeration/PullResponse")
    @WebResult(name = "PullResponse", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/09/enumeration", partName = "PullResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public PullResponse enumeratePull(
        @WebParam(name = "Pull", targetNamespace = "http://schemas.xmlsoap.org/ws/2004/09/enumeration")
        PullRequest pull,
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI,
        @WebParam(partName = "SessionId", name = "SessionId", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/wsman.xsd", header = true)
        String sessionId,
        @WebParam(name = "MaxEnvelopeSize", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        int maxEnvelopeSize,
        @WebParam(name = "OperationTimeout", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String operationTimeout,
        @WebParam(name = "Locale", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        Locale locale,
        @WebParam(name = "OptionSet", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        OptionSetType optionSet
    ) {
        return null;
    }

    @WebMethod(operationName = "Config", action = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Get")
    @Action(input = "http://schemas.xmlsoap.org/ws/2004/09/transfer/Get", output = "http://schemas.xmlsoap.org/ws/2004/09/transfer/GetResponse")
    @WebResult(name = "ConfigResponse", targetNamespace = "http://schemas.microsoft.com/wbem/wsman/1/config", partName = "ConfigResponse")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public ConfigResponse getConfig(
        @WebParam(name = "ResourceURI", targetNamespace = "http://schemas.dmtf.org/wbem/wsman/1/wsman.xsd", header = true)
        String resourceURI
    ) {
        return null;
    }

}
