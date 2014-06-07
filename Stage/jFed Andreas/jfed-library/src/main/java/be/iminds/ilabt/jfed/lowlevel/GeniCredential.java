package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.util.GeniTrustStoreHelper;
import be.iminds.ilabt.jfed.util.X509KeySelector;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * <p>GeniCredential is a wrapper for an XML Geni Credential.
 *
 * <p>It parses some data from the credential.
 * More data parsing and support for the different type of Geni Credentials should be implemented
 *
 * <p>A method to check the validity of the credential is provided.
 *
 * <p>A name is stored alongside the actual credential, which is useful for listing the credential in GUI lists and comboboxes.
 *
 * @see <a href="http://groups.geni.net/geni/wiki/GeniApiCredentials">Geni API Credentials</a>
 * @see <a href="http://www.protogeni.net/ProtoGeni/wiki/Credentials">ProtoGeni Credentials</a>
 */
public class GeniCredential {
    private String name;
    private String credentialXml;

    //see Geni Aggregate Manager API v3: http://groups.geni.net/geni/wiki/GAPI_AM_API_V3/CommonConcepts#credentials
    private String type;
    private String version;

    /** Pre Geni API v3 credentials: type=geni_sfa version=2 */
    public GeniCredential(String name, String credentialXml) {
        this(name, credentialXml, "geni_sfa", "2");
    }
    public GeniCredential(String name, String credentialXml, String type, String version) {
        this.name = name;
        this.credentialXml = credentialXml;
        this.type = type;
        this.version = version;

        if (credentialXml == null) throw new RuntimeException("GeniCredential credentialXml may not be null");

        parseXml();
    }

    private String ownerUrn, targetUrn;
    private String ownerGid, targetGid;
    private void parseXml() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(credentialXml)));

            NodeList nl = doc.getElementsByTagName("credential");
            if (nl.getLength() == 0) {
                throw new Exception("Cannot find signed-credential element!");
            }
            if (nl.getLength() > 1) {
                throw new Exception("No support for multiple credentials implemented.");
            }

            Element credentialEl = (Element) nl.item(0);

            Element ownerUrnEl = (Element) credentialEl.getElementsByTagName("owner_urn").item(0);
            ownerUrn = ownerUrnEl.getTextContent();

            Element targetUrnEl = (Element) credentialEl.getElementsByTagName("target_urn").item(0);
            targetUrn = targetUrnEl.getTextContent();

            if (credentialEl.getElementsByTagName("owner_gid").getLength() > 0) {
                Element ownerGidEl = (Element) credentialEl.getElementsByTagName("owner_gid").item(0);
                ownerGid = ownerGidEl.getTextContent();
            }

            if (credentialEl.getElementsByTagName("target_gid").getLength() > 0) {
                Element targetGidEl = (Element) credentialEl.getElementsByTagName("target_gid").item(0);
                targetGid = targetGidEl.getTextContent();
            }
        } catch (Exception e) {
            //TODO
            throw new RuntimeException("Error parsing credential XML: "+e.getMessage(), e);
        }
    }

    public String getCredentialXml() {
        assert credentialXml != null;
        return credentialXml;
    }

    public String getTargetUrn() {
        return targetUrn;
    }

    public String getOwnerUrn() {
        return ownerUrn;
    }

    public String getOwnerGid() {
        return ownerGid;
    }

    public String getTargetGid() {
        return targetGid;
    }

    public String getType() {
        return type;
    }

    public String getVersion() {
        return version;
    }

    public Hashtable getGeniV3Hashtable() {
        Hashtable res = new Hashtable();
        res.put("geni_type", type);
        res.put("geni_version", version);
        assert credentialXml != null;
        res.put("geni_value", credentialXml);
        return res;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return credentialXml;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeniCredential that = (GeniCredential) o;

        if (!credentialXml.equals(that.credentialXml)) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + credentialXml.hashCode();
        return result;
    }

    /*
   * Example from http://groups.geni.net/geni/wiki/GeniApiCredentials:
   *
   <?xml version="1.0"?>
   <signed-credential>
           <credential xml:id="ref0">
                   <type>privilege</type>
                   <serial>8</serial>
                   <owner_gid>certificate here</owner_gid>
                   <owner_urn>urn:publicid:IDN+plc:gpo:site2+user+jkarlin</owner_urn>
                   <target_gid>certificate here</target_gid>
                   <target_urn>urn:publicid:IDN+plc:gpo:site2+user+jkarlin</target_urn>
                   <uuid/>
                   <expires>2012-07-14T19:52:08Z</expires>
                   <privileges>
                           <privilege>
                                   <name>refresh</name>
                                   <can_delegate>true</can_delegate>
                           </privilege>
                   </privileges>
           </credential>

           <signatures>
                   signature information here
           </signatures>
   </signed-credential>

   <Signature xml:id="Sig_ref0" xmlns="http://www.w3.org/2000/09/xmldsig#">
       <SignedInfo>
         <CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/>
         <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
         <Reference URI="#ref0">
         <Transforms>
           <Transform Algorithm="http://www.w3.org/2000/09/xmldsig#enveloped-signature" />
         </Transforms>
         <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
         <DigestValue></DigestValue>
         </Reference>
       </SignedInfo>
       <SignatureValue />
         <KeyInfo>
           <X509Data>
             <X509SubjectName/>
             <X509IssuerSerial/>
             <X509Certificate/>
           </X509Data>
         <KeyValue />
         </KeyInfo>
       </Signature>
   * */

    /**
     * Check if the credential is valid.
     * @return {@code true} if the credential is valid
     */
     public boolean check() {
        try {
            // Step 1
            String providerName = System.getProperty("jsr105Provider",
                    "org.apache.jcp.xml.dsig.internal.dom.XMLDSigRI");
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
                    (java.security.Provider) Class.forName(providerName).newInstance());

            // Step 2
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            org.w3c.dom.Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(credentialXml)));

            // Step 3
            NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS ,"Signature");
            if (nl.getLength() == 0) {
                throw new Exception("Cannot find Signature element!");
            }

            // Step 4
            X509KeySelector x509ks = new X509KeySelector(GeniTrustStoreHelper.getFullTrustStore());
            DOMValidateContext signatureContext = new DOMValidateContext(x509ks, nl.item(0));

            //It seems that DOM does NOT see xml:id as an attribute of type xs:id as it should! So we manually set it so for any <credential>
            Element rootEl = doc.getDocumentElement();
            NodeList credentialNl = rootEl.getElementsByTagName("credential");
            for (int j = 0; j < credentialNl.getLength(); j++) {
                Node n = credentialNl.item(j);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element credentialEl = (Element) n;

                    NamedNodeMap l = credentialEl.getAttributes();
                    for (int i = 0; i < l.getLength(); i++) {
                        Attr a = (Attr)l.item(i);
                        if (a.getName().equals("xml:id")) {
                            System.out.println("Marking <credential> Attribute as id: "+a);
                            credentialEl.setIdAttributeNode(a, true);
                        } else
                            System.out.println("<credential> Attribute is not id: '"+a.getNamespaceURI()+"' : '"+a.getName()+"' -> "+a);
                    }
                }
            }

//            DOMValidateContext docContext = new DOMValidateContext(x509ks, rootEl);

            // Step 5
            XMLSignature signature = fac.unmarshalXMLSignature(signatureContext);
//            XMLSignature signature = fac.unmarshalXMLSignature(new DOMStructure(nl.item(0)));

            // Step 6
            boolean coreValidity = signature.validate(signatureContext);
            // Check core validation status
            if (coreValidity == false) {
                System.err.println("Signature failed core validation!");
                boolean sv = signature.getSignatureValue().validate(signatureContext);
                System.out.println("Signature validation status: " + sv);
                // Check the validation status of each Reference
                Iterator i = signature.getSignedInfo().getReferences().iterator();
                for (int j = 0; i.hasNext(); j++) {
                    boolean refValid = ((Reference) i.next()).validate(signatureContext);
                    System.out.println("Reference (" + j + ") validation status: "
                            + refValid);
                }
                return false;
            } else {
                //System.out.println("Signature passed core validation!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error during checkSignedCredential: "+e);
            e.printStackTrace();
            return false;
        }
    }
}
