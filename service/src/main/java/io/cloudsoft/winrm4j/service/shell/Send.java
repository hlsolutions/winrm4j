
package io.cloudsoft.winrm4j.service.shell;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Send", propOrder = {
    "stream"
})
public class Send {

    @XmlElement(name = "Stream", required = true)
    protected StreamType stream;

    /**
     * Gets the value of the stream property.
     * 
     * @return
     *     possible object is
     *     {@link StreamType }
     *     
     */
    public StreamType getStream() {
        return stream;
    }

    /**
     * Sets the value of the stream property.
     * 
     * @param value
     *     allowed object is
     *     {@link StreamType }
     *     
     */
    public void setStream(StreamType value) {
        this.stream = value;
    }

}
