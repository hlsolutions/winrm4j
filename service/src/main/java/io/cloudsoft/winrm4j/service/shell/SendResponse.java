
package io.cloudsoft.winrm4j.service.shell;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SendResponse", namespace = "http://schemas.microsoft.com/wbem/wsman/1/windows/shell", propOrder = {
})
public class SendResponse {

    @XmlElement(name = "DesiredStream")
    protected StreamType desiredStream;

    public StreamType getDesiredStream() {
        return desiredStream;
    }

    public void setDesiredStream(StreamType desiredStream) {
        this.desiredStream = desiredStream;
    }
}
