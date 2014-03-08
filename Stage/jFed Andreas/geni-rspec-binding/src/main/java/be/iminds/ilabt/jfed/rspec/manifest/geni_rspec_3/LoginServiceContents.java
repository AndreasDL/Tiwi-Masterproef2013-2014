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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;


/**
 * <p>Java class for LoginServiceContents complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LoginServiceContents">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;group ref="{http://www.geni.net/resources/rspec/3}AnyExtension"/>
 *         &lt;element ref="{http://www.geni.net/resources/rspec/3}user"/>
 *       &lt;/choice>
 *       &lt;attGroup ref="{http://www.geni.net/resources/rspec/3}AnyExtension"/>
 *       &lt;attribute name="authentication" use="required" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="hostname" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="port" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="username" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LoginServiceContents", propOrder = {
    "anyOrUser"
})
public class LoginServiceContents {

    @XmlElementRef(name = "user", namespace = "http://www.geni.net/resources/rspec/3", type = User.class)
    @XmlAnyElement(lax = true)
    protected List<Object> anyOrUser;
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String authentication;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String hostname;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String port;
    @XmlAttribute
    @XmlSchemaType(name = "anySimpleType")
    protected String username;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the anyOrUser property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the anyOrUser property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAnyOrUser().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link User }
     * {@link Element }
     * 
     * 
     */
    public List<Object> getAnyOrUser() {
        if (anyOrUser == null) {
            anyOrUser = new ArrayList<Object>();
        }
        return this.anyOrUser;
    }

    /**
     * Gets the value of the authentication property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthentication() {
        return authentication;
    }

    /**
     * Sets the value of the authentication property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthentication(String value) {
        this.authentication = value;
    }

    /**
     * Gets the value of the hostname property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the value of the hostname property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHostname(String value) {
        this.hostname = value;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the value of the port property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPort(String value) {
        this.port = value;
    }

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
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
