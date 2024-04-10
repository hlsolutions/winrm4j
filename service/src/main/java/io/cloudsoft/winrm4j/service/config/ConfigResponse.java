
package io.cloudsoft.winrm4j.service.config;

import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Config", namespace = "http://schemas.microsoft.com/wbem/wsman/1/config", propOrder = {
        "maxEnvelopeSizeKb",
        "any"
})
public class ConfigResponse {

    @XmlElement(name = "MaxEnvelopeSizekb", namespace = "http://schemas.microsoft.com/wbem/wsman/1/config")
    protected int maxEnvelopeSizeKb;

    @XmlAnyElement(lax = true)
    protected List<Element> any;

    public int getMaxEnvelopeSizeKb() {
        return maxEnvelopeSizeKb;
    }

    public void setMaxEnvelopeSizeKb(int maxEnvelopeSizeKb) {
        this.maxEnvelopeSizeKb = maxEnvelopeSizeKb;
    }

    public List<Element> getAny() {
        return any;
    }

    public void setAny(List<Element> any) {
        this.any = any;
    }

}
