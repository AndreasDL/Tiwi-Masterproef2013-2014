//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.02.20 at 04:22:13 PM CET 
//


package be.iminds.ilabt.jfed.rspec.request.geni_rspec_3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;


/**
 * <p>Java class for LinkContents complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LinkContents">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;choice>
 *           &lt;group ref="{http://www.geni.net/resources/rspec/3}AnyExtension"/>
 *           &lt;element ref="{http://www.geni.net/resources/rspec/3}property"/>
 *           &lt;element ref="{http://www.geni.net/resources/rspec/3}link_type"/>
 *           &lt;element name="interface_ref" type="{http://www.geni.net/resources/rspec/3}InterfaceRefContents"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element ref="{http://www.geni.net/resources/rspec/3}component_manager"/>
 *           &lt;element ref="{http://www.geni.net/resources/rspec/3}component_hop"/>
 *         &lt;/choice>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.geni.net/resources/rspec/3}AnyExtension"/>
 *       &lt;attribute name="client_id" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LinkContents", propOrder = {
    "anyOrPropertyOrLinkType"
})
@XmlRootElement(name = "link")
public class LinkContents {

    @XmlElementRefs({
        @XmlElementRef(name = "component_manager", namespace = "http://www.geni.net/resources/rspec/3", type = ComponentManager.class),
        @XmlElementRef(name = "link_type", namespace = "http://www.geni.net/resources/rspec/3", type = LinkType.class),
        @XmlElementRef(name = "component_hop", namespace = "http://www.geni.net/resources/rspec/3", type = JAXBElement.class),
        @XmlElementRef(name = "property", namespace = "http://www.geni.net/resources/rspec/3", type = JAXBElement.class),
        @XmlElementRef(name = "interface_ref", namespace = "http://www.geni.net/resources/rspec/3", type = JAXBElement.class)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> anyOrPropertyOrLinkType;
    @XmlAttribute(name = "client_id", required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String clientId;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the anyOrPropertyOrLinkType property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the anyOrPropertyOrLinkType property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnyOrPropertyOrLinkType().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.ComponentManager }
     * {@link org.w3c.dom.Element }
     * {@link LinkType }
     * {@link javax.xml.bind.JAXBElement }{@code <}{@link be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.ComponentHopContents }{@code >}
     * {@link javax.xml.bind.JAXBElement }{@code <}{@link LinkPropertyContents }{@code >}
     * {@link javax.xml.bind.JAXBElement }{@code <}{@link be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.InterfaceRefContents }{@code >}
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAnyOrPropertyOrLinkType() {
        if (anyOrPropertyOrLinkType == null) {
            anyOrPropertyOrLinkType = new ArrayList<Object>();
        }
        return this.anyOrPropertyOrLinkType;
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
