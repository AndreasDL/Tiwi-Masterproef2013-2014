package be.iminds.ilabt.jfed.ui.rspeceditor.model;

import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.*;
import be.iminds.ilabt.jfed.ui.rspeceditor.rspec_ext.binding.Location;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.xerces.dom.ElementNSImpl;

import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class Rspec {
    private ReadOnlyListWrapper<RspecNode> nodes = new ReadOnlyListWrapper(FXCollections.observableArrayList());
    private ReadOnlyListWrapper<RspecLink> links = new ReadOnlyListWrapper(FXCollections.observableArrayList());

//    /** Copy constructor */
//    public Rspec(Rspec rspec) {
//        //copy of nodes will not include interfaces yet
//        for (RspecNode node : nodes)
//            nodes.add(new RspecNode(node));
//        //copy of link will create interfaces and link them to nodes
//        for (RspecLink link : links)
//            links.add(new RspecLink(this, link));
//    }

    public ReadOnlyListProperty<RspecNode> getNodes() {
        return nodes.getReadOnlyProperty();
    }

    public ReadOnlyListProperty<RspecLink> getLinks() {
        return links.getReadOnlyProperty();
    }


    public RspecNode getNodeById(String id) {
        for (RspecNode node : nodes)
            if (node.getId().equals(id))
                return node;
        return null;
    }
    public RspecLink getLinkById(String id) {
        for (RspecLink link : links)
            if (link.getId().equals(id))
                return link;
        return null;
    }

    public Rspec() {
        //empty Rspec
    }


    public void deleteNode(RspecNode node) {
        List<RspecLink> linksToDelete = new ArrayList<RspecLink>();
        List<RspecInterface> ifacesToDelete = new ArrayList<RspecInterface>();
        for (RspecInterface iface : node.getInterfaces()) {
            RspecLink link = iface.getLink();
            if (link != null && link.getInterfaces().size() <= 2)
                linksToDelete.add(link);
            ifacesToDelete.add(iface);
        }
        for (RspecInterface iface : ifacesToDelete)
            iface.delete();
        nodes.remove(node);
        for (RspecLink link : linksToDelete)
            deleteLink(link);
    }

    public void deleteLink(RspecLink link) {
        AbstractList<RspecInterface> ifacesToDelete = new ArrayList<RspecInterface>(link.getInterfaces());
        for (RspecInterface iface : ifacesToDelete)
            iface.delete();
        links.remove(link);
    }

    public static Rspec dummy() {
        Rspec res = new Rspec();
        //dummy
        RspecNode n1 = new RspecNode(res.nextNodeName());
        res.nodes.add(n1);
        RspecNode n2 = new RspecNode(res.nextNodeName());
        res.nodes.add(n2);
        RspecNode n3 = new RspecNode(res.nextNodeName());
        res.nodes.add(n3);
        RspecNode n4 = new RspecNode(res.nextNodeName());
        res.nodes.add(n4);
        n1.setOsImage("DUMMY");
        n2.setOsImage("DUMMY");
        n3.setOsImage("DUMMY");
        n4.setOsImage("DUMMY");

        RspecLink l1 = new RspecLink(res, res.nextLinkName());
        RspecInterface i1 = new RspecInterface(n1, l1, res.nextIfaceName(n1));
        RspecInterface i2 = new RspecInterface(n2, l1, res.nextIfaceName(n2));
        RspecInterface i3 = new RspecInterface(n3, l1, res.nextIfaceName(n3));
        i1.setId("if1");
        i2.setId("if2");
        i3.setId("if3");
        res.links.add(l1);

        RspecLink l2 = new RspecLink(res, res.nextLinkName());
        RspecInterface i4 = new RspecInterface(n1, l2, res.nextIfaceName(n1));
        RspecInterface i5 = new RspecInterface(n4, l2, res.nextIfaceName(n4));
        l2.setId("gigalink2");
        res.links.add(l2);

        return res;
    }

    public static String toXml(JAXBElement element) {
        try {
            JAXBContext jc = JAXBContext.newInstance(element.getValue().getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(element, baos);
            return baos.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between fromGeni3RequestRspecXML and fromGeni3ManifestRspecXML and fromGeni3AdvertisementRspecXML! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Rspec fromGeni3RequestRspecXML(String inputRspec) {
        Rspec res = new Rspec();

        //handle empty String
        if (inputRspec == null || inputRspec.equals(""))
            return res;

        try {
//            Class docClass = RSpecContents.class;
//            String packageName = docClass.getPackage().getName();
//            JAXBContext jc = JAXBContext.newInstance(packageName);
            JAXBContext jc = JAXBContext.newInstance(RSpecContents.class, Location.class);
            Unmarshaller u = jc.createUnmarshaller();
            //            JAXBElement<RSpecContents> doc = (JAXBElement<RSpecContents>) u.unmarshal( new FileInputStream(rspecFile));
            JAXBElement<RSpecContents> doc = (JAXBElement<RSpecContents>) u.unmarshal( new StringReader(inputRspec));
            RSpecContents c = doc.getValue();

            assert c != null;

            //Note: getType() will return null if the type is specified but not "request".
            //      This is because the xsd specifies type as a choice from a list containing only "request".
            if (c != null &&
                    c.getType() != null &&
                    c.getType().value() != null) {
                if (!c.getType().value().equals("request"))
                    System.err.println("WANRING: Parsing an RSpec "+c.getType().value()+" as an RSpec request!");
            } else {
                System.err.println("WANRING: Parsed RSpec has no type (note: this also occurs if the type is not \"request\"): "+inputRspec);
            }

            Map<String, RspecInterface> nameToIface = new HashMap<String, RspecInterface>();
            Map<String, RspecNode> ifaceNameToRspecNode = new HashMap<String, RspecNode>();

            //first nodes
            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    if (el.getValue() instanceof NodeContents) {
                        NodeContents node = (NodeContents) el.getValue();

                        String id = node.getClientId();
                        //in an advertisement, client ID is typically empty, but component name and ID are set
                        if (id == null) id = node.getComponentName();
                        if (id == null) id = node.getComponentId();
                        //if all else fails, we assign a name ourselves
                        if (id == null) id = res.nextNodeName();

                        assert id != null : "Rspec has a node without any ID: "+toXml(el);
                        RspecNode resNode = new RspecNode(id);
                        assert resNode.getId() != null;
                        resNode.setPropertiesFromGeni3RequestRspec(node);

                        //check if node with same name is not already added
                        for (RspecNode existingNode : res.getNodes()) {
                            assert resNode != null;
                            assert existingNode != null;
                            assert resNode.getId() != null;
                            assert existingNode.getId() != null;
                            if (existingNode == resNode || resNode.getId().equals(existingNode.getId()))
                                throw new RuntimeException("Duplicate nodes in XML Rspec: id=\""+node.getClientId()+"\"");
                        }
                        res.getNodes().add(resNode);

                        for (Object nodeElO : node.getAnyOrRelationOrLocation()) {
                            if (nodeElO instanceof JAXBElement) {
                                JAXBElement nodeEl = (JAXBElement) nodeElO;
                                if (nodeEl.getValue() instanceof InterfaceContents) {
                                    InterfaceContents ic = (InterfaceContents) nodeEl.getValue();
                                    assert ic != null;

                                    //for a request, this is mandatory, but we use the same code to parse the advertisement
                                    //assert ic.getClientId() != null : "InterfaceContents has no clientId: "+ic;
                                    if (ic.getClientId() != null) {
                                        //check if iface with same name is not already added
                                        for (String existingIfaceName : ifaceNameToRspecNode.keySet())
                                            if (ic.getClientId().equals(existingIfaceName))
                                                throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIfaceName+"\"");

                                        ifaceNameToRspecNode.put(ic.getClientId(), resNode);
                                    } else {
                                        System.err.println("WARNING: ignoring that InterfaceContents has no clientId");
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //then the rest
            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    boolean handled = false;
                    if (el.getValue() instanceof NodeContents)
                        handled = true;
                    if (el.getValue() instanceof LinkContents) {
                        handled = true;
                        LinkContents link = (LinkContents) el.getValue();

                        String id = link.getClientId();
                        //in an advertisement, client ID is typically empty, but component name and ID are set
                        if (id == null) id = link.getOtherAttributes().get("component_name");
                        if (id == null) id = link.getOtherAttributes().get("component_id");
                        //if all else fails, we assign a name ourselves
                        if (id == null) id = res.nextLinkName();

                        assert id != null : "Rspec has a link without any ID: "+toXml(el);

                        //check if all interfaces of link are known. We will ignore the link if they are not.
                        boolean hasUnknownInterfaces = false;
                        for (Object linkElO : link.getAnyOrPropertyOrLinkType()) {
                            if (linkElO instanceof JAXBElement) {
                                JAXBElement linkEl = (JAXBElement) linkElO;
                                if (linkEl.getValue() instanceof InterfaceRefContents) {
                                    InterfaceRefContents ic = (InterfaceRefContents) linkEl.getValue();

                                    RspecInterface iface = nameToIface.get(ic.getClientId());
                                    if (iface == null) {
                                        RspecNode rspecNode = ifaceNameToRspecNode.get(ic.getClientId());

                                        if (rspecNode == null) {
                                            hasUnknownInterfaces = true;
                                            System.err.println("WARNING: Interface has not been defined in a <node>, but is referred to in a <link>: "+ic.getClientId());
//                                          throw new RuntimeException("Interface has not been defined in a <node>, but is referred to in a <link>: "+ic.getClientId());
                                        }
                                    }
                                }
                            }
                        }

                        if (hasUnknownInterfaces) {
                            System.err.println("WARNING: because link has one or more unknown interfaces, it will be ignored!");
                        } else {
                            RspecLink resLink = new RspecLink(res, id);
                            //check if iface with same name is not already added
                            for (RspecLink existingLink : res.getLinks()) {
                                assert resLink != null;
                                assert existingLink != null;
                                assert resLink.getId() != null;
                                assert existingLink.getId() != null;
                                if (existingLink == resLink || resLink.getId().equals(existingLink.getId()))
                                    throw new RuntimeException("Duplicate link in XML Rspec: id=\""+resLink.getId()+"\"");
                            }
                            res.getLinks().add(resLink);

                            for (Object linkElO : link.getAnyOrPropertyOrLinkType()) {
                                if (linkElO instanceof JAXBElement) {
                                    JAXBElement linkEl = (JAXBElement) linkElO;
                                    if (linkEl.getValue() instanceof InterfaceRefContents) {
                                        InterfaceRefContents ic = (InterfaceRefContents) linkEl.getValue();

                                        RspecInterface iface = nameToIface.get(ic.getClientId());
                                        if (iface == null) {
                                            RspecNode rspecNode = ifaceNameToRspecNode.get(ic.getClientId());

                                            if (rspecNode != null) {
                                                iface = new RspecInterface(rspecNode, resLink, ic.getClientId());
                                                nameToIface.put(iface.getId(), iface);
                                            }
                                        }

                                        if (iface == null) {
                                            //should not happen here anymore, since we handle it above!
                                            throw new RuntimeException("Interface has not been defined in a <node>, but is referred to in a <link>: "+ic.getClientId());
                                        }

                                        //adding interface here would be a bug, because interface should not be added here! RspecInterface constructor already adds to link
//                                    //check if iface with same name is not already added
//                                    for (RspecInterface existingIface : resLink.getInterfaces())
//                                        if (existingIface == iface || iface.getId().equals(existingIface.getId()))
//                                            throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIface.getId()+"\"");
//                                    resLink.getInterfaces().add(iface);

                                        //fill in iface details
                                    }
                                }
                            }

                            //set properties AFTER interfaces have been added
                            resLink.setPropertiesFromGeni3RequestRspec(link);
                        }
                    }
                    if (!handled)
                        System.out.println("Unhandled rspec JAXBElement value class: " + el.getValue().getClass().getName());
                } else {
                    if (o instanceof ElementNSImpl) {
                        ElementNSImpl elementNS = (ElementNSImpl) o;
                        System.out.println("elementNS "+elementNS.toString()+" ");
                    } else {
                        System.out.println("Unhandled rspec element class: "+o.getClass().getName());
                    }
                }
            }


//            System.out.println("rspec type "+c.getType().value()+" ");

            return res;
        } catch (Throwable e) {
            System.err.println("WARNING: Error reading Rspec XML (will ignore rspec): "+e.getMessage());
            e.printStackTrace();
            return new Rspec();
        }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between fromGeni3RequestRspecXML and fromGeni3ManifestRspecXML and fromGeni3AdvertisementRspecXML! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static Rspec fromGeni3ManifestRspecXML(String inputRspec) {
        Rspec res = new Rspec();

        //handle empty String
        if (inputRspec == null || inputRspec.equals(""))
            return res;

        //quick HACK to allow the planetlab Europe rspec manifest
        if (inputRspec.contains("<RSpec")) {
            System.out.println("HACK: rewriting received planetlab manifest XML to make it parsable xml");
            inputRspec = inputRspec.replace("<RSpec type=\"SFA\"", "<rspec xmlns=\"http://www.geni.net/resources/rspec/3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" type=\"manifest\"");
            inputRspec = inputRspec.replace("<RSpec ", "<rspec xmlns=\"http://www.geni.net/resources/rspec/3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" type=\"manifest\"");
            inputRspec = inputRspec.replace("</RSpec>", "</rspec>");
            inputRspec = inputRspec.replace("<network name=\"ple\">", "");
            inputRspec = inputRspec.replace("</network>", "");
            System.out.println("HACK: rewriten xml: "+inputRspec);
        }

        try {
//            Class docClass = RSpecContents.class;
//            String packageName = docClass.getPackage().getName();
//            JAXBContext jc = JAXBContext.newInstance(packageName);
            JAXBContext jc = JAXBContext.newInstance(be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents.class, Location.class);
            Unmarshaller u = jc.createUnmarshaller();
            //            JAXBElement<RSpecContents> doc = (JAXBElement<RSpecContents>) u.unmarshal( new FileInputStream(rspecFile));
            JAXBElement<be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents> doc = (JAXBElement<be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents>) u.unmarshal( new StringReader(inputRspec));
            be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents c = doc.getValue();

            assert c != null;

            if (c != null &&
                    c.getType() != null &&
                    c.getType().value() != null) {
                if (!c.getType().value().equals("manifest"))
                    System.err.println("WANRING: Parsing an RSpec "+c.getType().value()+" as an RSpec manifest!");
            } else {
                System.err.println("WANRING: Parsed RSpec has no type: "+inputRspec);
            }

            Map<String, RspecInterface> nameToIface = new HashMap<String, RspecInterface>();
            Map<String, RspecNode> ifaceNameToRspecNode = new HashMap<String, RspecNode>();

            //first nodes
            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents) {
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents node = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents) el.getValue();

                        String id = node.getClientId();
                        //in an advertisement, client ID is typically empty, but component name and ID are set
                        if (id == null) id = node.getComponentName();
                        if (id == null) id = node.getComponentId();
                        //if all else fails, we assign a name ourselves
                        if (id == null) id = res.nextNodeName();

                        assert id != null : "Rspec has a node without any ID: "+toXml(el);
                        RspecNode resNode = new RspecNode(id);
                        assert resNode.getId() != null;
                        resNode.setPropertiesFromGeni3ManifestRspec(node);

                        //check if node with same name is not already added
                        for (RspecNode existingNode : res.getNodes()) {
                            assert resNode != null;
                            assert existingNode != null;
                            assert resNode.getId() != null;
                            assert existingNode.getId() != null;
                            if (existingNode == resNode || resNode.getId().equals(existingNode.getId()))
                                throw new RuntimeException("Duplicate nodes in XML Rspec: id=\""+node.getClientId()+"\"");
                        }
                        res.getNodes().add(resNode);

                        for (Object nodeElO : node.getAnyOrRelationOrLocation()) {
                            if (nodeElO instanceof JAXBElement) {
                                JAXBElement nodeEl = (JAXBElement) nodeElO;
                                if (nodeEl.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceContents) {
                                    be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceContents ic = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceContents) nodeEl.getValue();

                                    //check if iface with same name is not already added
                                    for (String existingIfaceName : ifaceNameToRspecNode.keySet())
                                        if (ic.getClientId().equals(existingIfaceName))
                                            throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIfaceName+"\"");

                                    ifaceNameToRspecNode.put(ic.getClientId(), resNode);

                                    //fill in iface details
                                }
                            }
                        }
                    }
                }
            }

            //then the rest
            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    boolean handled = false;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents)
                        handled = true;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkContents) {
                        handled = true;
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkContents link = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LinkContents) el.getValue();

                        String id = link.getClientId();
                        //in an advertisement, client ID is typically empty, but component name and ID are set
                        if (id == null) id = link.getOtherAttributes().get("component_name");
                        if (id == null) id = link.getOtherAttributes().get("component_id");
                        //if all else fails, we assign a name ourselves
                        if (id == null) id = res.nextLinkName();

                        assert id != null : "Rspec has a link without any ID: "+toXml(el);

                        RspecLink resLink = new RspecLink(res, id);
                        //check if iface with same name is not already added
                        for (RspecLink existingLink : res.getLinks()) {
                            assert resLink != null;
                            assert existingLink != null;
                            assert resLink.getId() != null;
                            assert existingLink.getId() != null;
                            if (existingLink == resLink || resLink.getId().equals(existingLink.getId()))
                                throw new RuntimeException("Duplicate link in XML Rspec: id=\""+resLink.getId()+"\"");
                        }
                        res.getLinks().add(resLink);

                        for (Object linkElO : link.getAnyOrPropertyOrLinkType()) {
                            if (linkElO instanceof JAXBElement) {
                                JAXBElement linkEl = (JAXBElement) linkElO;
                                if (linkEl.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceRefContents) {
                                    be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceRefContents ic = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.InterfaceRefContents) linkEl.getValue();

                                    RspecInterface iface = nameToIface.get(ic.getClientId());
                                    if (iface == null) {
                                        RspecNode rspecNode = ifaceNameToRspecNode.get(ic.getClientId());

                                        if (rspecNode != null) {
                                            iface = new RspecInterface(rspecNode, resLink, ic.getClientId());
                                            nameToIface.put(iface.getId(), iface);
                                        }
                                    }

                                    if (iface == null) {
                                        throw new RuntimeException("Interface has not been defined in a <node>, but is referred to in a <link>: "+ic.getClientId());
                                    }

                                    //adding interface here would be a bug, because interface should not be added here! RspecInterface constructor already adds to link
//                                    //check if iface with same name is not already added
//                                    for (RspecInterface existingIface : resLink.getInterfaces())
//                                        if (existingIface == iface || iface.getId().equals(existingIface.getId()))
//                                            throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIface.getId()+"\"");
//                                    resLink.getInterfaces().add(iface);

                                    //fill in iface details
                                }
                            }
                        }

                        //set properties AFTER interfaces have been added
                        resLink.setPropertiesFromGeni3ManifestRspec(link);

                    }
                    if (!handled)
                        System.out.println("Unhandled rspec JAXBElement value class: " + el.getValue().getClass().getName());
                } else {
                    if (o instanceof ElementNSImpl) {
                        ElementNSImpl elementNS = (ElementNSImpl) o;
                        System.out.println("elementNS "+elementNS.toString()+" ");
                    } else {
                        System.out.println("Unhandled rspec element class: "+o.getClass().getName());
                    }
                }
            }


//            System.out.println("rspec type "+c.getType().value()+" ");

            return res;
        } catch (Exception e) {
            System.err.println("WARNING: Error reading Rspec XML (will ignore rspec): "+e.getMessage());
            e.printStackTrace();
            return new Rspec();
        }
    }  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between fromGeni3RequestRspecXML and fromGeni3ManifestRspecXML and fromGeni3AdvertisementRspecXML! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    public static Rspec fromGeni3AdvertisementRspecXML(String inputRspec) {
//        //TODO: add specific advertisement parsing?
//        System.err.println("WANRING: Using manifest parsing for advertisement RSpec. This will generate a warning.");
//        return fromGeni3ManifestRspecXML(inputRspec);
//    }
    public static Rspec fromGeni3AdvertisementRspecXML(String inputRspec) {
        Rspec res = new Rspec();

        //handle empty String
        if (inputRspec == null || inputRspec.equals(""))
            return res;

        try {
//            Class docClass = RSpecContents.class;
//            String packageName = docClass.getPackage().getName();
//            JAXBContext jc = JAXBContext.newInstance(packageName);
            JAXBContext jc = JAXBContext.newInstance(be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.RSpecContents.class, Location.class);
            Unmarshaller u = jc.createUnmarshaller();
            //            JAXBElement<RSpecContents> doc = (JAXBElement<RSpecContents>) u.unmarshal( new FileInputStream(rspecFile));
            JAXBElement<be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.RSpecContents> doc = (JAXBElement<be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.RSpecContents>) u.unmarshal( new StringReader(inputRspec));
            be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.RSpecContents c = doc.getValue();

            assert c != null;

            if (c != null && c.getType() != null ) {
                if (!c.getType().equals("advertisement"))
                    System.err.println("WARNING: Parsing an RSpec "+c.getType()+" as an RSpec advertisement!");
            } else {
                System.err.println("WANRING: Parsed RSpec has no type: "+inputRspec);
            }

            Map<String, RspecInterface> nameToIface = new HashMap<String, RspecInterface>();
            Map<String, RspecNode> ifaceNameToRspecNode = new HashMap<String, RspecNode>();

            //first nodes
            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents) {
                        be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents node = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents) el.getValue();

                        String id = node.getComponentName();
                        //in an advertisement, client ID does not exist empty, but component name and ID are always set
                        if (id == null) id = node.getComponentId();
                        //if all else fails, we assign a name ourselves
                        if (id == null) id = res.nextNodeName();

                        assert id != null : "Rspec has a node without any ID: "+toXml(el);
                        RspecNode resNode = new RspecNode(id);
                        assert resNode.getId() != null;
                        resNode.setPropertiesFromGeni3AdvertisementRspec(node);

                        //check if node with same name is not already added
                        for (RspecNode existingNode : res.getNodes()) {
                            assert resNode != null;
                            assert existingNode != null;
                            assert resNode.getId() != null;
                            assert existingNode.getId() != null;
                            if (existingNode == resNode || resNode.getId().equals(existingNode.getId()))
                                throw new RuntimeException("Duplicate nodes in XML Rspec: id=\""+node.getComponentName()+"\"");
                        }
                        res.getNodes().add(resNode);

                        for (Object nodeElO : node.getAnyOrRelationOrLocation()) {
                            if (nodeElO instanceof JAXBElement) {
                                JAXBElement nodeEl = (JAXBElement) nodeElO;
                                if (nodeEl.getValue() instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceContents) {
                                    be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceContents ic = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceContents) nodeEl.getValue();

                                    //check if iface with same name is not already added
                                    for (String existingIfaceName : ifaceNameToRspecNode.keySet())
                                        if (ic.getComponentId().equals(existingIfaceName))
                                            throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIfaceName+"\"");

                                    ifaceNameToRspecNode.put(ic.getComponentId(), resNode);

                                    //fill in iface details
                                }
                            }
                        }
                    }
                }
            }

            //then the rest
            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    boolean handled = false;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.NodeContents)
                        handled = true;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkContents) {
                        handled = true;
                        be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkContents link = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.LinkContents) el.getValue();

                        String id = link.getComponentName();
                        //in an advertisement, client ID does not exist empty, but component name and ID are always set
                        if (id == null) id = link.getComponentId();
                        //if all else fails, we assign a name ourselves
                        if (id == null) id = res.nextLinkName();

                        assert id != null : "Rspec has a link without any ID: "+toXml(el);

                        RspecLink resLink = new RspecLink(res, id);
                        //check if iface with same name is not already added
                        for (RspecLink existingLink : res.getLinks()) {
                            assert resLink != null;
                            assert existingLink != null;
                            assert resLink.getId() != null;
                            assert existingLink.getId() != null;
                            if (existingLink == resLink || resLink.getId().equals(existingLink.getId()))
                                throw new RuntimeException("Duplicate link in XML Rspec: id=\""+resLink.getId()+"\"");
                        }
                        res.getLinks().add(resLink);

                        for (Object linkElO : link.getAnyOrPropertyOrLinkType()) {
                            if (linkElO instanceof JAXBElement) {
                                JAXBElement linkEl = (JAXBElement) linkElO;
                                if (linkEl.getValue() instanceof be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceRefContents) {
                                    be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceRefContents ic = (be.iminds.ilabt.jfed.rspec.advertisement.geni_rspec_3.InterfaceRefContents) linkEl.getValue();

                                    RspecInterface iface = nameToIface.get(ic.getComponentId());
                                    if (iface == null) {
                                        RspecNode rspecNode = ifaceNameToRspecNode.get(ic.getComponentId());

                                        if (rspecNode != null) {
                                            iface = new RspecInterface(rspecNode, resLink, ic.getComponentId());
                                            nameToIface.put(iface.getId(), iface);
                                        }
                                    }

                                    if (iface == null) {
                                        throw new RuntimeException("Interface has not been defined in a <node>, but is referred to in a <link>: "+ic.getComponentId());
                                    }

                                    //adding interface here would be a bug, because interface should not be added here! RspecInterface constructor already adds to link
//                                    //check if iface with same name is not already added
//                                    for (RspecInterface existingIface : resLink.getInterfaces())
//                                        if (existingIface == iface || iface.getId().equals(existingIface.getId()))
//                                            throw new RuntimeException("Duplicate interfaces in XML Rspec: id=\""+existingIface.getId()+"\"");
//                                    resLink.getInterfaces().add(iface);

                                    //fill in iface details
                                }
                            }
                        }

                        //set properties AFTER interfaces have been added
                        resLink.setPropertiesFromGeni3AdvertisementRspec(link);

                    }
                    if (!handled)
                        System.out.println("Unhandled rspec JAXBElement value class: " + el.getValue().getClass().getName());
                } else {
                    if (o instanceof ElementNSImpl) {
                        ElementNSImpl elementNS = (ElementNSImpl) o;
                        System.out.println("elementNS "+elementNS.toString()+" ");
                    } else {
                        System.out.println("Unhandled rspec element class: "+o.getClass().getName());
                    }
                }
            }


//            System.out.println("rspec type "+c.getType().value()+" ");

            return res;
        } catch (Exception e) {
            System.err.println("WARNING: Error reading Rspec XML (will ignore rspec): "+e.getMessage());
            e.printStackTrace();
            return new Rspec();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// WARNING: very dirty cut and paste code reuse between fromGeni3RequestRspecXML and fromGeni3ManifestRspecXML and fromGeni3AdvertisementRspecXML! /////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String toGeni3RequestRspec() {
//        System.out.println("Rspec.toRspec()  nr of nodes in topology: "+nodes.size());

        RSpecContents res = new RSpecContents();
        res.setType(RspecTypeContents.REQUEST);
        res.setGeneratedBy("Experimental jFed Rspec Editor");
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = null;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Could not create DataFactory needed to get current time as XMLGregorianCalendar: "+e.getMessage(), e);
        }
        XMLGregorianCalendar now = datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        res.setGenerated(now);

        for (RspecNode node : nodes) {
            assert ! node.getId().isEmpty();

            NodeContents nodeContents = new NodeContents();
            node.writePropertiesToGeni3RequestRspec(nodeContents);
            assert nodeContents.getClientId() != null;

            List<RspecInterface> ifaces = node.getInterfaces();
            for (RspecInterface iface : ifaces) {
                assert ! iface.getId().isEmpty() : "iface.getId() is empty for node "+node.getId();

                InterfaceContents interfaceContents = new InterfaceContents();
                interfaceContents.setClientId(iface.getId());
                nodeContents.getAnyOrRelationOrLocation().add(interfaceContents);
            }

            res.getAnyOrNodeOrLink().add(nodeContents);
        }
        for (RspecLink link : links) {
            LinkContents linkContents = new LinkContents();
            link.writePropertiesToGeni3RequestRspec(linkContents);
            assert linkContents.getClientId() != null;

            List<RspecInterface> ifaces = link.getInterfaces();
            for (RspecInterface iface : ifaces) {
                assert ! iface.getId().isEmpty();
                InterfaceRefContents interfaceContents = new InterfaceRefContents();
                interfaceContents.setClientId(iface.getId());
                linkContents.getAnyOrPropertyOrLinkType().add(interfaceContents);
            }

            assert ! link.getId().isEmpty();

            res.getAnyOrNodeOrLink().add(linkContents);
        }

        StringWriter sw = new StringWriter();
        try {
            JAXBContext context = JAXBContext.newInstance(RSpecContents.class, Location.class);
            Marshaller m = context.createMarshaller();


            /*
            * Note: annotation in Location package-info.java is:
            *
                @javax.xml.bind.annotation.XmlSchema(
                namespace = "http://jfed.iminds.be/rspec/ext/jfed/1"
                , xmlns = {
                    @javax.xml.bind.annotation.XmlNs(prefix = "jFed", namespaceURI = "http://jfed.iminds.be/rspec/ext/jfed/1") }
                , elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)


                annotation in RspecConet's pacakge-info is:
@javax.xml.bind.annotation.XmlSchema(namespace = "http://www.geni.net/resources/rspec/3"
        , xmlns = {
                    @javax.xml.bind.annotation.XmlNs(prefix = "", namespaceURI = "http://www.geni.net/resources/rspec/3") }
        , elementFormDefault = javax.xml.bind.annotation.XmlNsForm.QUALIFIED)


                    the prefix is set to "" there, so that it becomes the default namespace. (it is silly one cannot do this in code when creating the context...)
            * */

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(res, sw);
            return sw.getBuffer().toString();
        } catch (JAXBException e) {
            //TODO: this should be handled better
            //just dump the error to the output
            e.printStackTrace();
            return "Error converting to Rspec"+ e;
        }
    }

    public List<String> getAllComponentManagerUrns() {
        Set<String> urns = new HashSet<>();
        for (RspecNode node : nodes)
            if (node.getComponentManagerId() != null)
                urns.add(node.getComponentManagerId());
        for (RspecLink link : links)
            for (String cmUrn : link.getComponentManagerUrns())
                urns.add(cmUrn);

        return new ArrayList<>(urns);
    }


    public String nextLinkName() {
        List<String> names = new ArrayList<String>();
        for (RspecLink n : links)
            names.add(n.getId());

        int c = nodes.size();
        String res = "link"+c;
        while (names.contains(res))
            res = "link"+(++c);

        return res;
    }

    public String nextNodeName() {
        List<String> names = new ArrayList<String>();
        for (RspecNode n : nodes)
            names.add(n.getId());

        int c = nodes.size();
        String res = "node"+c;
        while (names.contains(res))
            res = "node"+(++c);

        return res;
    }

    public String nextIfaceName(RspecNode node) {
        List<String> names = new ArrayList<String>();
        for (RspecNode n : nodes)
            for (RspecInterface i : n.getInterfaces())
                names.add(i.getId());

//        int c = nodes.size();
        int c = node.getInterfaces().size();
        String res = node.getId()+":if"+c;
        while (names.contains(res))
            res = node.getId()+":if"+(++c);

        return res;
    }
}
