//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.03.20 at 03:19:39 PM CET 
//


package be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for InterfaceContents complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InterfaceContents">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;choice>
 *           &lt;group ref="{http://www.geni.net/resources/rspec/3}AnyExtension"/>
 *           &lt;element ref="{http://www.geni.net/resources/rspec/3}ip"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/3}host"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.geni.net/resources/rspec/3}AnyExtension"/>
 *       &lt;attribute name="component_id" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="client_id" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="sliver_id" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="mac_address" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InterfaceContents", propOrder = {
    "anyOrIpOrHost"
})
public class InterfaceContents {

    @XmlElementRefs({
        @XmlElementRef(name = "host", namespace = "http://www.geni.net/resources/rspec/3", type = Host.class),
        @XmlElementRef(name = "ip", namespace = "http://www.geni.net/resources/rspec/3", type = JAXBElement.class)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> anyOrIpOrHost;
    @XmlAttribute(name = "component_id")
    @XmlSchemaType(name = "anySimpleType")
    protected String componentId;
    @XmlAttribute(name = "client_id", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String clientId;
    @XmlAttribute(name = "sliver_id")
    @XmlSchemaType(name = "anySimpleType")
    protected String sliverId;
    @XmlAttribute(name = "mac_address")
    @XmlSchemaType(name = "anySimpleType")
    protected String macAddress;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the anyOrIpOrHost property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the anyOrIpOrHost property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnyOrIpOrHost().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Host }
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link IpContents }{@code >}
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAnyOrIpOrHost() {
        if (anyOrIpOrHost == null) {
            anyOrIpOrHost = new ArrayList<Object>();
        }
        return this.anyOrIpOrHost;
    }

    /**
     * Gets the value of the componentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Sets the value of the componentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComponentId(String value) {
        this.componentId = value;
    }

    /**
     * Gets the value of the clientId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the value of the clientId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClientId(String value) {
        this.clientId = value;
    }

    /**
     * Gets the value of the sliverId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSliverId() {
        return sliverId;
    }

    /**
     * Sets the value of the sliverId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSliverId(String value) {
        this.sliverId = value;
    }

    /**
     * Gets the value of the macAddress property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets the value of the macAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMacAddress(String value) {
        this.macAddress = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}
