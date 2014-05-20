package be.iminds.ilabt.jfed.lowlevel.stitching;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
* Hop
*/
class RspecStitchingHop {
    private final HopId hopId;
    private final String hopElId;
    private final String linkIdUrn;
    private final String vlanRangeAvailability;
    private final String suggestedVLANRange;
    private final String fullHopXml;
    private final Element element;


    private static Element quickDomChildHelper(Element e, String tagName) {
        NodeList resList = e.getElementsByTagName(tagName);
        assert resList.getLength() == 1;
        Node resNode = resList.item(0);
        assert resNode != null;
        assert resNode.getNodeType() == Node.ELEMENT_NODE;
        Element resElement = (Element) resNode;
        return resElement;
    }

    public RspecStitchingHop(String linkName, Element hopElement) {
        this.element = hopElement;

        Element linkEl = quickDomChildHelper(hopElement, "link");
        Element scDescriptorEl = quickDomChildHelper(linkEl, "switchingCapabilityDescriptor");
        Element scSpecificInfoEl = quickDomChildHelper(scDescriptorEl, "switchingCapabilitySpecificInfo");
        Element scSpecificInfo_L2scEl = quickDomChildHelper(scSpecificInfoEl, "switchingCapabilitySpecificInfo_L2sc");
        Element vlanRangeAvailabilityEl = quickDomChildHelper(scSpecificInfo_L2scEl, "vlanRangeAvailability");
        Element suggestedVLANRangeEl = quickDomChildHelper(scSpecificInfo_L2scEl, "suggestedVLANRange");

        this.hopElId = hopElement.getAttribute("id");
        this.linkIdUrn = linkEl.getAttribute("id");
        this.vlanRangeAvailability = vlanRangeAvailabilityEl.getTextContent();
        this.suggestedVLANRange = suggestedVLANRangeEl.getTextContent();

        hopId = new HopId(linkName, hopElId, linkIdUrn);

        String xmlString = "";
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(hopElement), new StreamResult(buffer));
            xmlString = buffer.toString();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            xmlString = "ERROR: "+e.getMessage();
        } catch (TransformerException e) {
            xmlString = "ERROR: "+e.getMessage();
        }
        this.fullHopXml = xmlString;

        if (vlanRangeAvailability == null) {
            System.out.flush();
            System.err.flush();
            System.out.println("VlanInfoParsing DEBUG: ");
            System.out.println("       linkEl == null                  -> " + (linkEl == null));
            System.out.println("       scDescriptorEl == null          -> "+(scDescriptorEl == null));
            System.out.println("       scSpecificInfoEl == null        -> "+(scSpecificInfoEl == null));
            System.out.println("       scSpecificInfo_L2scEl == null   -> "+(scSpecificInfo_L2scEl == null));
            System.out.println("       vlanRangeAvailabilityEl == null -> "+(vlanRangeAvailabilityEl == null));
            System.out.println("       suggestedVLANRangeEl == null    -> "+(suggestedVLANRangeEl == null));
            System.out.println("         vlanRangeAvailability == null -> "+(vlanRangeAvailability == null));
            System.out.println("         suggestedVLANRange == null    -> "+(suggestedVLANRange == null));
            System.out.println("ElementXml -> " + fullHopXml);
            System.out.flush();
            System.err.flush();
        }

        assert linkIdUrn != null : "No HopUrn in "+fullHopXml;
        assert vlanRangeAvailability != null : "No vlanRangeAvailability in "+fullHopXml;;
        assert suggestedVLANRange != null : "No suggestedVLANRange in "+fullHopXml;;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RspecStitchingHop vlanInfo = (RspecStitchingHop) o;

        if (!fullHopXml.equals(vlanInfo.fullHopXml)) return false;

        return true;
    }

    public HopId getHopId() {
        return hopId;
    }

    public String getHopElId() {
        return hopElId;
    }

    public String getLinkIdUrn() {
        return linkIdUrn;
    }

    public String getVlanRangeAvailability() {
        return vlanRangeAvailability;
    }

    public String getSuggestedVLANRange() {
        return suggestedVLANRange;
    }

    public String getFullHopXml() {
        return fullHopXml;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public int hashCode() {
        return fullHopXml.hashCode();
    }

    @Override
    public String toString() {
        return "RspecStitchingHop{" +
                "hopId='" + hopId + '\'' +
                ", suggestedVLANRange='" + suggestedVLANRange + '\'' +
                ", vlanRangeAvailability='" + vlanRangeAvailability + '\'' +
                '}';
    }
}
