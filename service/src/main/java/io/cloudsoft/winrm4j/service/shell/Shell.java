
package io.cloudsoft.winrm4j.service.shell;

import java.util.ArrayList;
import java.util.List;
import javax.xml.datatype.Duration;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAnyElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Shell", propOrder = {
    "shellId",
    "name",
    "environment",
    "workingDirectory",
    "lifetime",
    "idleTimeout",
    "inputStreams",
    "outputStreams",
    "creationXml",
    "any"
})
public class Shell {

    @XmlAttribute(name = "ShellId")
    @XmlSchemaType(name = "anyURI")
    protected String shellId;
    @XmlAttribute(name = "Name")
    protected String name;
    @XmlElement(name = "Environment")
    protected EnvironmentVariableList environment;
    @XmlElement(name = "WorkingDirectory")
    protected String workingDirectory;
    @XmlElement(name = "Lifetime")
    protected Duration lifetime;
    @XmlElement(name = "IdleTimeout")
    protected Duration idleTimeout;
    @XmlList
    @XmlElement(name = "InputStreams")
    protected List<String> inputStreams;
    @XmlList
    @XmlElement(name = "OutputStreams")
    protected List<String> outputStreams;

    @XmlElement(name = "creationXml", namespace = "http://schemas.microsoft.com/powershell")
    protected byte[] creationXml;

    @XmlAnyElement(lax = true)
    protected List<Object> any;

    /**
     * Gets the value of the shellId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShellId() {
        return shellId;
    }

    /**
     * Sets the value of the shellId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShellId(String value) {
        this.shellId = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the value of the environment property.
     * 
     * @return
     *     possible object is
     *     {@link EnvironmentVariableList }
     *     
     */
    public EnvironmentVariableList getEnvironment() {
        return environment;
    }

    /**
     * Sets the value of the environment property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvironmentVariableList }
     *     
     */
    public void setEnvironment(EnvironmentVariableList value) {
        this.environment = value;
    }

    /**
     * Gets the value of the workingDirectory property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the value of the workingDirectory property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkingDirectory(String value) {
        this.workingDirectory = value;
    }

    /**
     * Gets the value of the lifetime property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getLifetime() {
        return lifetime;
    }

    /**
     * Sets the value of the lifetime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setLifetime(Duration value) {
        this.lifetime = value;
    }

    /**
     * Gets the value of the idleTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the value of the idleTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setIdleTimeout(Duration value) {
        this.idleTimeout = value;
    }

    /**
     * Gets the value of the inputStreams property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the inputStreams property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInputStreams().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getInputStreams() {
        if (inputStreams == null) {
            inputStreams = new ArrayList<String>();
        }
        return this.inputStreams;
    }

    /**
     * Gets the value of the outputStreams property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outputStreams property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOutputStreams().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getOutputStreams() {
        if (outputStreams == null) {
            outputStreams = new ArrayList<String>();
        }
        return this.outputStreams;
    }

    public byte[] getCreationXml() {
        return creationXml;
    }

    public void setCreationXml(byte[] creationXml) {
        this.creationXml = creationXml;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

}
