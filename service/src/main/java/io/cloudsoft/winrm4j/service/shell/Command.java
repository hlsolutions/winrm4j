package io.cloudsoft.winrm4j.service.shell;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Command", propOrder = {
})
public class Command {

    @XmlValue
    protected String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
