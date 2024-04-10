
package io.cloudsoft.winrm4j.service.shell;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CommandLine", propOrder = {
    "commandId",
    "command",
    "arguments"
})
public class CommandLine {

    @XmlAttribute(name = "CommandId")
    protected String commandId;

    @XmlElement(name = "Command")
    protected String command;

    @XmlElement(name = "Arguments")
    protected List<String> arguments;

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String value) {
        this.command = value;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public List<String> getArguments() {
        if (arguments == null) {
            arguments = new ArrayList<String>();
        }
        return this.arguments;
    }

}
