package be.iminds.ilabt.jfed.lowlevel.stitching;

import be.iminds.ilabt.jfed.lowlevel.api.StitchingComputationService;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

/**
 * Stitcher
 */
public class StitchingDirector {
    private final AuthorityListModel authList;
    private boolean insecure;

    private StitchingComputationService.ComputePathResult computePathResult;
    private Set<SfaAuthority> involvedAuthorities = new HashSet<SfaAuthority>();


    public StitchingDirector(AuthorityListModel authList) {
        this.authList = authList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////// Process workflow Info /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private StitchingWorkflowHop createStitchingWorkflowHop(Hashtable hop) {
        String hopUrn = apiStringInHashtable(hop, "hop_urn");
        String aggUrn = apiStringInHashtable(hop, "aggregate_urn");
        String aggUrl = apiStringInHashtable(hop, "aggregate_url");
        boolean importVlans = apiBooleanInHashtable(hop, "import_vlans");
        StitchingWorkflowHop res = new StitchingWorkflowHop(authList, hopUrn, aggUrn, aggUrl, importVlans, hop);
        return res;
    }
    private static void addStitchingWorkflowHopDeps(Map<String, StitchingWorkflowHop> hopsByUrn, StitchingWorkflowHop hop) {
        if (!hop.getSource().containsKey("dependencies"))
            return;

        Vector subDeps = apiVectorInHashtable(hop.getSource(), "dependencies");
        for (Object d : subDeps) {
            apiAssert(d instanceof Hashtable, "dependency is not hop (class="+d.getClass().getName()+")");
            Hashtable dep = (Hashtable) d;
            String depUrn = apiStringInHashtable(dep, "hop_urn");
            StitchingWorkflowHop depHop = hopsByUrn.get(depUrn);
            if (depHop == null)
                throw new RuntimeException("Error, dependency with URN "+depUrn+" not found");
            else
                hop.addDependency(depHop);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Extract, manipulate and insert Rspec Stitching Info ////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //    /*
//    * Checks if there are links that require stitching.
//    *
//    * Rules:
//    *   - the link has more than one componentManager
//    *   - the link is not a GRE tunnel
//    * */
//    private boolean needsStitching(String rspecXml) {
//        //not going to do this. We have a Rspec object  available where we'd need this, so info can be derived more easily there. See HighLevelController
//
//    }



    /*
    * Uses simple DOM to read the rspec stitching info
    * */
    private Map<HopId, RspecStitchingHop> extractVlanInfo(String rspecXml) {
        Map<HopId, RspecStitchingHop> res = new HashMap<HopId, RspecStitchingHop>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(rspecXml)));

            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            //rspec - stitching - path - hop (MULTIPLE) - link - switchingCapabilityDescriptor - switchingCapabilitySpecificInfo - switchingCapabilitySpecificInfo_L2sc -
            //          vlanRangeAvailability
            //          suggestedVLANRange

            String rspecNamespace = "http://www.geni.net/resources/rspec/3";
            String stitchingNamespace = "http://hpn.east.isi.edu/rspec/ext/stitch/0.1/";

//            NodeList rspecList = doc.getElementsByTagNameNS(rspecNamespace, "rspec");
            NodeList rspecListNoNS = doc.getElementsByTagName("rspec");
//            assert rspecList.getLength() == 1 : "not a single rspec element, but "+rspecList.getLength()+" (without NS "+rspecListNoNS.getLength()+") in "+rspecXml;
            assert rspecListNoNS.getLength() == 1 : "not a single rspec element, but "+rspecListNoNS.getLength()+" in "+rspecXml;
            Node rspecNode = rspecListNoNS.item(0);
            assert rspecNode != null;
            assert rspecNode.getNodeType() == Node.ELEMENT_NODE;
            Element rspecElement = (Element) rspecNode;

//            NodeList stitchingList = rspecElement.getElementsByTagNameNS(stitchingNamespace, "stitching");
            NodeList stitchingListNoNS = rspecElement.getElementsByTagName("stitching");
//            assert stitchingList.getLength() == 1  : "not a single stitching element, but "+stitchingList.getLength()+" (without NS "+stitchingListNoNS.getLength()+") in "+rspecXml;
            assert stitchingListNoNS.getLength() == 1  : "not a single stitching element, but "+stitchingListNoNS.getLength()+" in "+rspecXml;
            Node stitchingNode = stitchingListNoNS.item(0);
            assert stitchingNode != null;
            assert stitchingNode.getNodeType() == Node.ELEMENT_NODE;
            Element stitchingElement = (Element) stitchingNode;

            List<Element> pathEls = quickDomChildrenHelper(stitchingElement, "path");
            for (Element pathEl : pathEls) {
                String linkName = pathEl.getAttribute("id");
                assert linkName != null : "<stitching> has <path> element without id attribute: "+pathEl;
                NodeList hopList = pathEl.getElementsByTagName("hop");

//            System.out.println("DEBUG: extractVlanInfo found "+hopList.getLength()+" hops");

                for (int i = 0; i < hopList.getLength(); i++) {
                    Node hopNode = hopList.item(i);
                    assert hopNode != null;
                    assert hopNode.getNodeType() == Node.ELEMENT_NODE;
                    Element hopElement = (Element) hopNode;

                    RspecStitchingHop stitchingWorkflowHop = new RspecStitchingHop(linkName, hopElement);
                    res.put(stitchingWorkflowHop.getHopId(), stitchingWorkflowHop);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("DEBUG: extractVlanInfo returing "+res.size()+" hops");
        return res;
    }

    private Map<HopId, RspecStitchingHop> findChangedVlanInfo(Map<HopId, RspecStitchingHop> origVlanInfo, Map<HopId, RspecStitchingHop> newVlanInfo) {
        Map<HopId, RspecStitchingHop> res = new HashMap<HopId, RspecStitchingHop>();

        for (HopId hopId : newVlanInfo.keySet()) {
            RspecStitchingHop origInfo = origVlanInfo.get(hopId);
            RspecStitchingHop newInfo = newVlanInfo.get(hopId);
            if (origInfo == null)
                res.put(newInfo.getHopId(), newInfo);
            else {
                if (!newInfo.equals(origInfo))
                    res.put(newInfo.getHopId(), newInfo);
            }
        }

        return res;
    }

    private Map<HopId, RspecStitchingHop> mergeVlanInfo(Map<HopId, RspecStitchingHop> origVlanInfo, Map<HopId, RspecStitchingHop> changedVlanInfo) {
        Map<HopId, RspecStitchingHop> res = new HashMap<HopId, RspecStitchingHop>();

        HashSet<HopId> allKeys = new HashSet();
        allKeys.addAll(origVlanInfo.keySet());
        allKeys.addAll(changedVlanInfo.keySet());

        for (HopId hopId : allKeys) {
            RspecStitchingHop origInfo = origVlanInfo.get(hopId);
            RspecStitchingHop newInfo = changedVlanInfo.get(hopId);
            if (origInfo == null) {
                assert newInfo != null : "How did \""+hopId+"\" get into allKeys?";
                res.put(newInfo.getHopId(), newInfo);
            } else {
                if (newInfo == null) {
                    res.put(origInfo.getHopId(), origInfo);
                } else {
                    //merge = take new info
                    res.put(newInfo.getHopId(), newInfo);
                }
            }
        }

        return res;
    }

    private static String debugPrintVlanInfos(Map<HopId, RspecStitchingHop> lastVlanInfo) {
        String res = "";
        for (RspecStitchingHop vlanInfo : lastVlanInfo.values()) {
            res += "   "+vlanInfo.toString() + "\n";
        }
        return res;
    }

    private String mergeVlanInfoIntoRspec(String serviceRspec, Map<HopId, RspecStitchingHop> lastVlanInfo) {
        //rewrite rspec, so that vlanInfo is used instead of what is in the rspec (requires DOM + transformation)

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(serviceRspec)));

            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            //rspec - stitching - path - hop (MULTIPLE) - link - switchingCapabilityDescriptor - switchingCapabilitySpecificInfo - switchingCapabilitySpecificInfo_L2sc -
            //          vlanRangeAvailability
            //          suggestedVLANRange

            String rspecNamespace = "http://www.geni.net/resources/rspec/3";
            String stitchingNamespace = "http://hpn.east.isi.edu/rspec/ext/stitch/0.1/";

//            NodeList rspecList = doc.getElementsByTagNameNS(rspecNamespace, "rspec");
            NodeList rspecListNoNS = doc.getElementsByTagName("rspec");
//            assert rspecList.getLength() == 1 : "not a single rspec element, but "+rspecList.getLength()+" (without NS "+rspecListNoNS.getLength()+") in "+rspecXml;
            assert rspecListNoNS.getLength() == 1 : "not a single rspec element, but "+rspecListNoNS.getLength()+" in "+serviceRspec;
            Node rspecNode = rspecListNoNS.item(0);
            assert rspecNode != null;
            assert rspecNode.getNodeType() == Node.ELEMENT_NODE;
            Element rspecElement = (Element) rspecNode;

//            NodeList stitchingList = rspecElement.getElementsByTagNameNS(stitchingNamespace, "stitching");
            NodeList stitchingListNoNS = rspecElement.getElementsByTagName("stitching");
//            assert stitchingList.getLength() == 1  : "not a single stitching element, but "+stitchingList.getLength()+" (without NS "+stitchingListNoNS.getLength()+") in "+rspecXml;
            assert stitchingListNoNS.getLength() == 1  : "not a single stitching element, but "+stitchingListNoNS.getLength()+" in "+serviceRspec;
            Node stitchingNode = stitchingListNoNS.item(0);
            assert stitchingNode != null;
            assert stitchingNode.getNodeType() == Node.ELEMENT_NODE;
            Element stitchingElement = (Element) stitchingNode;

            List<Element> pathEls = quickDomChildrenHelper(stitchingElement, "path");
            for (Element pathEl : pathEls) {
                String linkName = pathEl.getAttribute("id");
                assert linkName != null : "<stitching> has <path> element without id attribute: "+pathEl;

                List<Element> origHopElements = quickDomChildrenHelper(pathEl, "hop");

                //remove original hop elements
                for (Element hopElement : origHopElements) {
//                    Element linkEl = quickDomChildHelper(hopElement, "link");
                    hopElement.getParentNode().removeChild(hopElement);
                }

                //keep only hops for this path
                List<RspecStitchingHop> filteredVlanInfos = new ArrayList<RspecStitchingHop>(lastVlanInfo.values());
                for (ListIterator<RspecStitchingHop> it = filteredVlanInfos.listIterator(); it.hasNext(); ) {
                    RspecStitchingHop hop = it.next();

                    if (!hop.getHopId().getLinkName().equals(linkName))
                        it.remove();
                }

                //sort according to hop id (seems to be needed!)
                List<RspecStitchingHop> sortedVlanInfos = new ArrayList<RspecStitchingHop>(filteredVlanInfos);
                Collections.sort(sortedVlanInfos, new Comparator<RspecStitchingHop>() {
                    @Override
                    public int compare(RspecStitchingHop o1, RspecStitchingHop o2) {
                        return o1.getHopElId().compareTo(o2.getHopElId());
                    }
                });

                //add updated hop elements
                for (RspecStitchingHop vlanInfo : sortedVlanInfos) {
                    Node importedNode = doc.importNode(vlanInfo.getElement(), true/*deep*/);
                    //                System.out.println("DEBUG: mergeVlanInfoIntoRspec adding hop   "+vlanInfo.getHopUrn());
                    pathEl.appendChild(importedNode);
                }
            }

            try {
                TransformerFactory transFactory = TransformerFactory.newInstance();
                Transformer transformer = transFactory.newTransformer();
                StringWriter buffer = new StringWriter();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.transform(new DOMSource(rspecNode), new StreamResult(buffer));
                return buffer.toString();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * extract link names from Rspec
     * */
    private List<String> linkNamesFromRspec() {
        throw new RuntimeException("TODO");
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private List<StitchingWorkflowHop> allHops = new ArrayList<StitchingWorkflowHop>();
    private Map<String, StitchingWorkflowHop> hopsByUrn = new HashMap<String, StitchingWorkflowHop>();
    public synchronized void setComputePathResult(StitchingComputationService.ComputePathResult computePathResult) {
        this.computePathResult = computePathResult;

        assert computePathResult != null;

        assert computePathResult.getWorkflowData() != null;
        Hashtable allLinks =  computePathResult.getWorkflowData();
        List<String> linkNames = new ArrayList<String>(allLinks.keySet());

        for (String linkName : linkNames) {
            Hashtable link = apiHashtableInHashtable(computePathResult.getWorkflowData(), linkName);
            Vector hopsToProcessVector = apiVectorInHashtable(link, "dependencies");
            List hopsToProcess = new ArrayList(hopsToProcessVector);
            for (int i = 0; i < hopsToProcess.size(); i++) {
                Object d = hopsToProcess.get(i);
                //            System.out.println("Processing hop "+i+" / "+hopsToProcess.size());

                apiAssert(d instanceof Hashtable, "dependency is not hop (class="+d.getClass().getName()+")");
                Hashtable dep = (Hashtable) d;
                StitchingWorkflowHop newHop = createStitchingWorkflowHop(dep);
                System.out.println("  StitchingWorkflowHop ("+i+" / "+hopsToProcess.size()+"): " + newHop.getHopUrn());
                if (!hopsByUrn.containsKey(newHop.getHopUrn())) {
                    //                System.out.println("     Adding");
                    allHops.add(newHop);
                    hopsByUrn.put(newHop.getHopUrn(), newHop);

                    if (dep.containsKey("dependencies")) {
                        Vector subDeps = apiVectorInHashtable(dep, "dependencies");
                        //                    System.out.println("        found "+subDeps.size()+" deps");
                        hopsToProcess.addAll(subDeps);
                    }
                }
                //            else  System.out.println("     Skip: already added");
            }
        }

        System.out.println("Hopcount="+ allHops.size()+"");
        //        System.out.println("Hops with deps (count="+hops.size()+"):");
        for (StitchingWorkflowHop hop : allHops) {
            addStitchingWorkflowHopDeps(hopsByUrn, hop);
            //            System.out.println("" + hop);
        }

        hopsLeft = new LinkedList<StitchingWorkflowHop>(allHops);
        doneAuthorities = new ArrayList<SfaAuthority>();

        origVlanInfo = extractVlanInfo(computePathResult.getServiceRspec());
        currentVlanInfo = new HashMap<HopId, RspecStitchingHop>(origVlanInfo);

        System.out.println("***************************************************************************************");
        System.out.println("Original VlanInfos ("+origVlanInfo.size()+"):\n"+debugPrintVlanInfos(origVlanInfo));
    }

    public class ReadyHopDetails {
        private final SfaAuthority authority;
        private final String requestRspec;
        private final StitchingWorkflowHop hop; //not accessable outside of this class
        private final Map<HopId,RspecStitchingHop> origVlanInfo;

        public ReadyHopDetails(SfaAuthority authority, String requestRspec, StitchingWorkflowHop hop, Map<HopId,RspecStitchingHop> origVlanInfo) {
            this.authority = authority;
            this.requestRspec = requestRspec;
            this.hop = hop;
            this.origVlanInfo = new HashMap<HopId, RspecStitchingHop>(origVlanInfo);
        }

        public SfaAuthority getAuthority() {
            return authority;
        }

        public String getRequestRspec() {
            return requestRspec;
        }
    }

    private List<StitchingWorkflowHop> hopsLeft;
    private List<SfaAuthority> doneAuthorities;
    private Map<HopId, RspecStitchingHop> origVlanInfo;
    private Map<HopId, RspecStitchingHop> currentVlanInfo;
    public synchronized List<ReadyHopDetails> getReadyHops() {
        assert computePathResult != null : "Call setComputePathResult first";

        assert allHops  != null : "Call setComputePathResult first";
        assert hopsLeft  != null : "Call setComputePathResult first";
        assert hopsByUrn  != null : "Call setComputePathResult first";
        assert doneAuthorities  != null : "Call setComputePathResult first";
        assert hopsByUrn.size() > 0;

        List<ReadyHopDetails> res = new ArrayList<ReadyHopDetails>();

        List<SfaAuthority> returnedAuths = new ArrayList<SfaAuthority>();

        if (!hopsLeft.isEmpty()) {
            for (StitchingWorkflowHop hop : new ArrayList<StitchingWorkflowHop>(hopsLeft)) {
                assert !hop.isDone();

                if (hop.areAllDepsDone()) {
                    SfaAuthority auth = hop.getAuth();
                    if (!doneAuthorities.contains(auth)) {
                        if (!returnedAuths.contains(auth)) {
                            if (!involvedAuthorities.contains(auth))
                                involvedAuthorities.add(auth);

                            System.out.println("Merging "+currentVlanInfo.size()+" VlanInfo into Rspec");
                            String requestRspec = mergeVlanInfoIntoRspec(computePathResult.getServiceRspec(), currentVlanInfo);

                            //make Xml pretty. This makes debug a lot easier.
                            requestRspec = XmlUtil.formatXmlFromString(requestRspec);

                            ReadyHopDetails readDetails = new ReadyHopDetails(auth, requestRspec, hop, currentVlanInfo);
                            res.add(readDetails);
                            returnedAuths.add(auth);
                        }
                        //else //already contained authorities
                    } else {
                        System.out.println("assuming hop is done: same authority as ready hop");
                        //considered it done
                        hopsLeft.remove(hop);
                        hop.setDone(true);
                    }
                }
            }
        }

        return res;
    }
    /**
     * ready hops and hops waiting for deps. not hops that have returned manifest.
     * */
    public synchronized List<SfaAuthority> getHopsLeft() {
        assert computePathResult != null : "Call setComputePathResult first";

        assert allHops  != null : "Call setComputePathResult first";
        assert hopsLeft  != null : "Call setComputePathResult first";
        assert hopsByUrn  != null : "Call setComputePathResult first";
        assert doneAuthorities  != null : "Call setComputePathResult first";
        assert hopsByUrn.size() > 0;

        List<SfaAuthority> res = new ArrayList<SfaAuthority>();


        List<SfaAuthority> returnedAuthorities = new ArrayList<SfaAuthority>(doneAuthorities);

        if (!hopsLeft.isEmpty()) {
            for (StitchingWorkflowHop hop : new ArrayList<StitchingWorkflowHop>(hopsLeft)) {
                SfaAuthority auth = hop.getAuth();
                if (!returnedAuthorities.contains(auth)) {
                    res.add(auth);
                    returnedAuthorities.add(auth);
                }
            }
        }

        return res;
    }

    public synchronized void processFailure(ReadyHopDetails readyHopDetails) {
        //TODO? (do we need this in the first place?)
    }

    public synchronized void processManifest(ReadyHopDetails readyHopDetails, String manifestRspec) {
        StitchingWorkflowHop hop = readyHopDetails.hop;
        //register done for this hop
        doneAuthorities.add(readyHopDetails.getAuthority());
        hopsLeft.remove(hop);
        hop.setDone(true);

        assert manifestRspec != null;

        //extract and process VLAN info from manifest
        System.out.println("\n***************************************************************************************");
        Map<HopId, RspecStitchingHop> extractedVlanInfo = extractVlanInfo(manifestRspec);
        System.out.println("\n\nManifest VlanInfos:\n"+debugPrintVlanInfos(extractedVlanInfo));

        Map<HopId, RspecStitchingHop> changedVlanInfo = findChangedVlanInfo(readyHopDetails.origVlanInfo, extractedVlanInfo);
        System.out.println("\nChanged VlanInfos:\n"+debugPrintVlanInfos(changedVlanInfo));

        currentVlanInfo = mergeVlanInfo(currentVlanInfo, changedVlanInfo);
        System.out.println("\nMerged VlanInfos:\n"+debugPrintVlanInfos(currentVlanInfo));
        System.out.println("\n***************************************************************************************");
    }

    /** List of all involved authorities. Mostly useful for calling Delete */
    public synchronized List<SfaAuthority> getInvolvedAuthorities() {
        return new ArrayList<SfaAuthority>(involvedAuthorities);
    }






    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Element quickDomChildHelper(Element e, String tagName) {
        NodeList resList = e.getElementsByTagName(tagName);
        assert resList.getLength() == 1;
        Node resNode = resList.item(0);
        assert resNode != null;
        assert resNode.getNodeType() == Node.ELEMENT_NODE;
        Element resElement = (Element) resNode;
        return resElement;
    }
    private static List<Element> quickDomChildrenHelper(Element e, String tagName) {
        NodeList resList = e.getElementsByTagName(tagName);
        List<Element> resElements = new ArrayList<Element>();
        for (int i = 0 ; i < resList.getLength(); i++) {
            Node resNode = resList.item(i);
            assert resNode != null;
            assert resNode.getNodeType() == Node.ELEMENT_NODE;
            Element resElement = (Element) resNode;
            resElements.add(resElement);
        }
        return resElements;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////// API helpers //////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private static void apiAssert(boolean cond, String msg) {
        if (!cond)
            throw new RuntimeException(msg);
    }
    private static String apiStringInHashtable(Hashtable h, String key) {
        assert h != null;
        Object val = h.get(key);
        apiAssert(val != null, "expected field "+key+" did not exist in: "+h);
        apiAssert(val instanceof String, "expected field "+key+" exists but is "+val.getClass().getName()+" instead of String in: "+h);
        return (String) val;
    }
    private static Hashtable apiHashtableInHashtable(Hashtable h, String key) {
        assert h != null;
        Object val = h.get(key);
        apiAssert(val != null, "expected field "+key+" did not exist in: "+h);
        apiAssert(val instanceof Hashtable, "expected field "+key+" exists but is "+val.getClass().getName()+" instead of Hashtable in: "+h);
        return (Hashtable) val;
    }
    private static Vector apiVectorInHashtable(Hashtable h, String key) {
        assert h != null;
        Object val = h.get(key);
        apiAssert(val != null, "expected field "+key+" did not exist in: "+h);
        apiAssert(val instanceof Vector, "expected field "+key+" exists but is "+val.getClass().getName()+" instead of Vector in: "+h);
        return (Vector) val;
    }
    private static Boolean apiBooleanInHashtable(Hashtable h, String key) {
        assert h != null;
        Object val = h.get(key);
        apiAssert(val != null, "expected field "+key+" did not exist in: "+h);
        apiAssert(val instanceof Boolean, "expected field "+key+" exists but is "+val.getClass().getName()+" instead of Boolean in: "+h);
        return (Boolean) val;
    }
}
