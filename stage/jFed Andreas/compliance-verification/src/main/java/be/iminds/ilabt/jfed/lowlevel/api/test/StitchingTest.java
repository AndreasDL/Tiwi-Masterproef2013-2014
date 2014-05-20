package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.*;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityListModel;
import be.iminds.ilabt.jfed.lowlevel.authority.JFedAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.lowlevel.stitching.StitchingDirector;
import be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.SSHKeyHelper;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StitchingTest:
 *   - create a stitched RSpec, contact SCS and do all needed CreateSlivers.
 *   - Then test login on node and connectivity using ping.
 *   - finally, Delete everything
 */
public class StitchingTest extends ApiTest {
    @Override
    public List<String> getRequiredConfigKeys() {
        List<String> res = new ArrayList<String>();
        res.add("stitchedAuthorityUrns"); //space seperated list
        return res;
    }

    @Override
    public List<String> getOptionalConfigKeys() {
        List<String> res = new ArrayList<String>();
        res.add("topology");
        return res;
    }

    @Override
    public String getTestDescription() {
        return "Test Stitching between multiple authorities, including node login and ping test between nodes";
    }

    String scsUrl;
    String scsUrn;
    List<String> stitchedAuthorityUrns;
    @Override
    public void setUp(CommandExecutionContext testContext) {
        stitchedAuthorityUrns = getStitchedAuthorityUrns();

        sa = new SliceAuthority(testContext.getLogger());
        am2 = new AggregateManager2(testContext.getLogger());

        //TODO get these from config if specified
        scsUrl = "http://oingo.dragon.maxgigapop.net:8081/geni/xmlrpc";
        scsUrn = "urn:publicid:IDN+oingo.dragon.maxgigapop.net+auth+am";

        logger = getTestContext().getLogger();
        authorityListModel = JFedAuthorityList.getAuthorityListModel();
        assert authorityListModel != null;
    }

    public List<String> getStitchedAuthorityUrns() {
        List<String> res = new ArrayList<String>();
        String urnList = getTestConfig().getProperty("stitchedAuthorityUrns");
        if (urnList == null) throw new RuntimeException("StitchingTest required config key stitchedAuthorityUrns is not present.");
        String[] urnsArray = urnList.split(" ");
        for (String urn : urnsArray)
            res.add(urn);
        if (res.isEmpty()) throw new RuntimeException("StitchingTest required config key stitchedAuthorityUrns (space seperated list) does not contain urns: "+urnList);
        if (res.size() == 1) throw new RuntimeException("StitchingTest required config key stitchedAuthorityUrns (space seperated list) contains only 1 urn: "+urnList);
        return res;
    }

    private SliceAuthority sa;
    private AggregateManager2 am2;

    private List<GeniCredential> getUserCredentialList() {
        List<GeniCredential> res = new ArrayList<GeniCredential>();
        res.add(userCredential);
        return res;
    }
    private static List<GeniCredential> toCredentialList(GeniCredential c) {
        List<GeniCredential> res = new ArrayList<GeniCredential>();
        res.add(c);
        return res;
    }

    public GeniConnection getSAConnection() throws GeniException {
        return getTestContext().getConnectionProvider().getConnectionByAuthority(getTestContext().getGeniUser(), getTestContext().getUserAuthority(), new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }
//    public GeniConnection getAM2Connection() throws GeniException {
//        return getTestContext().getConnectionProvider().getConnectionByAuthority(getTestContext().getGeniUser(), getTestContext().getTestedAuthority(), new ServerType(ServerType.GeniServerRole.AM, 2));
//    }


    private class SshInfo {
        String sshUsername;
        String sshHostname;
        int sshPort;

        @Override
        public String toString() {
            return "SshInfo{" +
                    "sshUsername='" + sshUsername + '\'' +
                    ", sshHostname='" + sshHostname + '\'' +
                    ", sshPort=" + sshPort +
                    '}';
        }
    }
    private Map<SfaAuthority, SshInfo> sshInfos = new HashMap<SfaAuthority, SshInfo>();
    private boolean extractSshInfo(SfaAuthority authority, String rspec) {
        if (rspec == null) return false;
        assertNotNull(rspec, "Rspec is null");

        String hostname = null;
        int port = 22;
        String username = null;

        int nodeCount = 0;
        int loginCount = 0;

        //parse the RSpec XML
        try {
            Class docClass = be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents.class;
            String packageName = docClass.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<RSpecContents> doc = (JAXBElement<be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents>) u.unmarshal( new StringReader(rspec));
            be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents c = doc.getValue();

            be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RspecTypeContents typ = c.getType();
            assertNotNull(typ, "Received manifest RSpec does not specify a type: " + rspec);
            assertEquals(typ.value(), "manifest", "Received manifest RSpec is not a manifest: " + rspec);

            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    if (el.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents) {
                        be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents node = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.NodeContents) el.getValue();

                        String nodeName = node.getClientId();
                        nodeCount++;

                        for (Object nodeElO : node.getAnyOrRelationOrLocation()) {
                            if (nodeElO instanceof JAXBElement) {
                                JAXBElement nodeEl = (JAXBElement) nodeElO;
//                                if (nodeEl.getValue() instanceof InterfaceContents) {
//                                    InterfaceContents ic = (InterfaceContents) nodeEl.getValue();
//                                    String interfaceName = ic.getClientId();
//                                }
                                if (nodeEl.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ServiceContents) {
                                    be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ServiceContents serviceC = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.ServiceContents) nodeEl.getValue();
                                    for (Object serviceElO : serviceC.getAnyOrLoginOrInstall()) {
                                        if (serviceElO instanceof JAXBElement) {
                                            JAXBElement serviceEl = (JAXBElement) serviceElO;
                                            if (serviceEl.getValue() instanceof be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LoginServiceContents) {
                                                be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LoginServiceContents loginSC = (be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.LoginServiceContents) serviceEl.getValue();

                                                String auth = loginSC.getAuthentication();

                                                String aHostname = loginSC.getHostname();
                                                int aPort = 0;
                                                if (loginSC.getPort() != null)
                                                    aPort = Integer.parseInt(loginSC.getPort());
                                                String aUsername = loginSC.getUsername();

                                                if (auth.equals("ssh-keys")) {
                                                    assertEquals("ssh-keys", auth, "service login authentication must be ssh-keys for node " + nodeName);
                                                    assertNotNull(aHostname, "no hostname in service login for node " + nodeName);
                                                    assertNotNull(aUsername, "no username in service login for node " + nodeName);
                                                    //TODO add support for missing username? (a fallback username can be derived from user urn)

                                                    hostname = aHostname;
                                                    port = aPort;
                                                    username = aUsername;

                                                    loginCount++;
                                                } else {
                                                    note("Unsupported auth in manifest RSpec service login: " + auth);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JAXBException e) {
            throw new RuntimeException("Exception parsing manifest RSpec Xml: "+e.getMessage(), e);
        }

        if (loginCount > 0)
        {
            assertTrue(loginCount > 0, "no service login found (" + loginCount + " service logins for " + nodeCount + " nodes)");
            assertNotNull(hostname);
            assertNotNull(username);

            if (loginCount > 1)
                note("Found multiple node logins in manifest rspec (" + loginCount + " service logins for " + nodeCount + " nodes), using last one.");

            SshInfo sshInfo = new SshInfo();
            sshInfo.sshUsername = username;
            sshInfo.sshHostname= hostname;
            sshInfo.sshPort = port;
            sshInfos.put(authority, sshInfo);
            return true;
        } else {
            note("Found no node service login in manifest RSpec.");
            return false;
        }
    }







    //Note on dependencies: you can do SCS call and init without actually creating the slice.
    //                      Only the sliceName is needed for these calls
    //                      CreateSlivers does require that the slice is really created

    private String sliceName;
    private String sliceUrnStr;
    @Test(groups = {"init"})
    public void createSliceName() throws GeniException {
//        sliceName = "stitchTest";  //DEBUGREUSEPREVTEST this is for specific debugging only, replace by commented line below
        sliceName = "stitch"+(System.currentTimeMillis()/1000);
        sliceUrnStr = "urn:publicid:IDN+"+getTestContext().getUserAuthority().getNameForUrn()+"+slice+"+sliceName;
    }


    private GeniCredential userCredential;
    @Test()
    public void getUserCredential() throws GeniException {
        SliceAuthority.SliceAuthorityReply<GeniCredential> res = null;
        try {
            res = sa.getCredential(getSAConnection());
        } catch (GeniException e) {
            throw new RuntimeException(e);
        }
        assertTrue(res.getGeniResponseCode().isSuccess(), "Get (User) Credential call is not successful: " + res.getGeniResponseCode());
        assertNotNull(res.getValue(), "Get (User) Credential call returned no credential (empty result value)");
        userCredential = res.getValue();
    }

    private GeniCredential sliceCredential;
    @Test(hardDepends = {"createSliceName", "getUserCredential"}, groups = {"createslice"})
    public void createSlice() throws GeniException {
        ResourceUrn sliceUrn = new ResourceUrn(sliceUrnStr);
        SliceAuthority.SliceAuthorityReply<GeniCredential> registerRes;

//        registerRes = sa.getSliceCredential(getSAConnection(), userCredential, sliceUrn);  //DEBUGREUSEPREVTEST this is for specifc debugging only, replace by commented line below
        registerRes = sa.register(getSAConnection(), userCredential, sliceUrn);

        TestSliceAuthority.testSACorrectnessXmlRpcResult(registerRes.getRawResult());
        assert registerRes.getGeniResponseCode().isSuccess() : "Register call was not successful in creating \""+sliceUrn.getValue()+"\": "+registerRes.getGeniResponseCode()+" output="+registerRes.getOutput();
        sliceCredential = registerRes.getValue();
        assert sliceCredential != null;
        assert sliceCredential.getCredentialXml() != null;
        assert sliceCredential.getCredentialXml().length() > 10 : "credential too short "+ sliceCredential.getCredentialXml();
    }

    private String rspecXmlStr;
    private List<String> allIps() {
        List<String> res = new ArrayList<String>();
        for (Node node : nodes)
            for (Iface iface : node.ifaces)
                res.add(iface.ip);
        return res;
    }
    private List<String> localIps(SfaAuthority auth) {
        List<String> res = new ArrayList<String>();
        for (Node node : nodes)
            if (node.authUrn.equals(auth.getUrn()))
                for (Iface iface : node.ifaces)
                    res.add(iface.ip);
        return res;
    }
    private List<String> pingTargetsForAuth(SfaAuthority auth) {
        List<String> res = new ArrayList<String>();
        for (Node node : nodes)
            if (node.authUrn.equals(auth.getUrn()))
                for (Iface iface : node.ifaces)
                    for (Iface otherSideIface : iface.link.otherIfaces(iface))
                        res.add(otherSideIface.ip);
        return res;
    }
    /** returns true if the authority is one of the authorities with nodes, and not just one of the stitching link auths */
    private boolean isNodeAuth(SfaAuthority auth) {
        return stitchedAuthorityUrns.contains(auth.getUrn());
    }
    private class Node {
        private final String name;
        private final String authUrn;
        private List<Iface> ifaces = new ArrayList<Iface>();
        private Node(String authUrn) {
            this.name = "node"+nodes.size();
            this.authUrn = authUrn;
            nodes.add(this);
        }
    }
    private class Iface {
        private final Node node;
        private final Link link;
        private String ip;
        private Iface(Node node, Link link) {
            this.node = node;
            this.link = link;
            this.node.ifaces.add(this);
            this.link.ifaces.add(this);
            this.ip = link.subnet+"."+(link.ifaces.indexOf(this)+1);
        }
        private String getName() {
            return node.name+":if"+node.ifaces.indexOf(this);
        }
    }
    private class Link {
        private final String name;
        private List<Iface> ifaces = new ArrayList<Iface>();
        private final String subnet;
        private Link() {
            this.name = "link"+links.size();
            this.subnet = "192.168."+(4+links.size());
            links.add(this);
        }
        private Link(Node a, Node b) {
            this();
            new Iface(a, this);
            new Iface(b, this);
        }
        private List<String> componentManagerUrns() {
            List<String> res = new ArrayList<String>();
            for (Iface i : ifaces) {
                Node n = i.node;
                res.add(n.authUrn);
            }
            return res;
        }
        private List<Iface> otherIfaces(Iface iface) {
            List<Iface> res = new ArrayList<Iface>();
            for (Iface i : ifaces)
                if (i != iface)
                    res.add(i);
            return res;
        }
    }
    private List<Node> nodes = new ArrayList<Node>();
    private List<Link> links = new ArrayList<Link>();
    @Test(groups = {"init"})
    public void generateRspec() throws GeniException {
        assert authorityListModel != null;
        assert stitchedAuthorityUrns != null;
        assert stitchedAuthorityUrns.size() > 1;

        //create nodes
        for (String authUrn : stitchedAuthorityUrns) {
            Node n = new Node(authUrn);
        }

        //create link(s)
        assert nodes.size() > 1;
        if (nodes.size() == 2) {
            //create link if there are only 2 nodes
            new Link(nodes.get(0), nodes.get(1));
        } else {
            //create links (ring topo)
            Node firstNode = nodes.get(0);
            Node lastNode = nodes.get(nodes.size()-1);
            Node prevNode = lastNode;
            for (Node n : nodes) {
                new Link(n, prevNode);
                prevNode = n;
            }
        }

        //create Rspec
        String nodesXml = "";
        String linksXml = "";
        for (Node node : nodes) {
            String nodeXml = "  <node client_id=\""+node.name+"\" component_manager_id=\""+node.authUrn+"\" exclusive=\"false\">\n"+
                    "    <sliver_type name=\"emulab-openvz\"/>\n";

            for (Iface iface : node.ifaces)
                nodeXml +=   "    <interface client_id=\""+iface.getName()+"\">\n" +
                        "      <ip address=\""+iface.ip+"\" netmask=\"255.255.255.0\" type=\"ipv4\"/>\n" +
                        "    </interface>\n";

            nodeXml +=      "  </node>\n";
            nodesXml += nodeXml;
        }

        for (Link link : links) {
            String linkXml = "  <link client_id=\""+link.name+"\">\n";
            for (String urn : link.componentManagerUrns())
                linkXml += "    <component_manager name=\""+urn+"\"/>\n";
            for (Iface iface : link.ifaces)
                linkXml += "    <interface_ref client_id=\""+iface.getName()+"\"/>\n";


            for (int ifaceIndex1 = 0; ifaceIndex1 < link.ifaces.size(); ifaceIndex1++)
                for (int ifaceIndex2 = ifaceIndex1+1; ifaceIndex2 < link.ifaces.size(); ifaceIndex2++) {
                    Iface iface1 = link.ifaces.get(ifaceIndex1);
                    Iface iface2 = link.ifaces.get(ifaceIndex2);
                    //capacity is in demo rspec, and is probably needed:
                    //    I got an error with text "*** ERROR: vtopgen: Cannot mix trivial_ok|emulated with * bw" on emulab.net when not using it
                    //       (but that could also have other cause)
                    linkXml += "    <property source_id=\""+iface1.getName()+"\" dest_id=\""+iface2.getName()+"\" capacity=\"100000\"/>\n" +
                            "    <property source_id=\""+iface2.getName()+"\" dest_id=\""+iface1.getName()+"\" capacity=\"100000\"/>\n";
//                    linkXml += "    <property source_id=\""+iface1.getName()+"\" dest_id=\""+iface2.getName()+"\"/>\n" +
//                               "    <property source_id=\""+iface2.getName()+"\" dest_id=\""+iface1.getName()+"\"/>\n";
                }
            linkXml += "  </link>\n";

            linksXml += linkXml;
        }

        rspecXmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rspec type=\"request\" \n" +
                "    xmlns=\"http://www.geni.net/resources/rspec/3\" \n" +
                "    xmlns:planetlab=\"http://www.planet-lab.org/resources/sfa/ext/planetlab/1\" \n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "       xmlns:stitch=\"http://hpn.east.isi.edu/rspec/ext/stitch/0.1/\" \n" +
                "    xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 \n" +
                "    http://www.geni.net/resources/rspec/3/request.xsd\">  \n" +
                nodesXml+
                linksXml+
                "</rspec>";

        System.out.println("Generated Rspec: " + rspecXmlStr);
        note("Generated Rspec: "+rspecXmlStr);
    }

    private Logger logger;
    private AuthorityListModel authorityListModel;
    private StitchingDirector director;
    private SSHKeyHelper sshKeyHelper;
    private List<UserSpec> users;
    SfaAuthority scsAuth;
    @Test(hardDepends = {"createSliceName", "generateRspec"}, softDepends = {"createSlice"}, groups = {"init"})
    public void initStitching() throws GeniException, NoSuchAlgorithmException, MalformedURLException {
        director = new StitchingDirector(authorityListModel);

        sshKeyHelper = new SSHKeyHelper();

        users = new ArrayList<UserSpec>();
        UserSpec userSpec = new UserSpec(getTestContext().getGeniUser().getUserUrn(), sshKeyHelper.getSshPublicKeyString());
        users.add(userSpec);

        //create stitching auth if doesn't exist
        scsAuth = null;
        if (authorityListModel.getByUrn(scsUrn) == null) {
            Map< ServerType, URL> urlMap = new HashMap< ServerType, URL>();
            urlMap.put(new ServerType(ServerType.GeniServerRole.SCS, 1), new URL(scsUrl));
            scsAuth = new SfaAuthority(scsUrn, "Stitching Test SCS", urlMap, null, "scs");
            authorityListModel.addAuthority(scsAuth);
            authorityListModel.fireChange();

            assert authorityListModel.getByUrn(scsUrn) != null;
        } else
            scsAuth = authorityListModel.getByUrn(scsUrn);
    }


    @Test(hardDepends = {"initStitching"}, groups = {"scs"})
    public void callSCS() throws GeniException {
        assert sliceUrnStr != null;
        assert rspecXmlStr != null;

        //contact stitcher
        StitchingComputationService scs = new StitchingComputationService(logger);
        GeniConnection con = new GeniPlainConnection(scsAuth, scsAuth.getUrl(ServerType.GeniServerRole.SCS, 1).toExternalForm(), false/*debug*/);

        StitchingComputationService.SCSReply<StitchingComputationService.ComputePathResult> scsReply =
                scs.computePath(con, sliceUrnStr, rspecXmlStr, null);

        assertNotNull(scsReply);
        assertNotEquals(scsReply.getCode(), 0);
        assertNotNull(scsReply.getValue());

        StitchingComputationService.ComputePathResult computePathResult = scsReply.getValue();

        director.setComputePathResult(computePathResult);
    }

    private GeniConnection getAm2Connection(SfaAuthority auth) throws URISyntaxException, GeniException {
        GeniUser user = getTestContext().getGeniUser();
        String url = auth.getUrl(ServerType.GeniServerRole.AM, 2).toURI().toString();
        GeniConnection con = null;
        if (url.startsWith("http://")) {
            note("WARNING: Connection URL is http instead of https! " +
                    "This is unsecure, so this connection protocol will never used. " +
                    "Will try using https instead, maybe that works.");
            url = url.replaceFirst("http", "https");
        }
        if (url.startsWith("https://"))
            con = new GeniSslConnection(auth,
                    url,
                    user.getCertificate(),
                    user.getPrivateKey(),
                    false/*debug*/,
                    handleUntrustedCallback);
        if (url.startsWith("http://"))
            con = new GeniPlainConnection(auth,
                    url,
                    false/*debug*/);
        assert con != null : "URL has unsupported protocol: "+url;
        return con;
    }


    private ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback = null;
    public void setInsecure() {
        handleUntrustedCallback = new ClientSslAuthenticationXmlRpcTransportFactory.INSECURE_TRUSTALL_HandleUntrustedCallback();
    }
    @Test(hardDepends = {"callSCS", "createSlice"}, groups = {"createsliver"})
    public void callCreateSlivers() throws GeniException, URISyntaxException {
        setInsecure();


        List<StitchingDirector.ReadyHopDetails> readyHops = director.getReadyHops();
        while (!readyHops.isEmpty()) {
            String readyHopString = "";
            for (StitchingDirector.ReadyHopDetails hop : readyHops)
                readyHopString+=" "+hop.getAuthority().getUrn();
            note("Hops ready: " + readyHops.size()+": "+readyHopString);

            for (StitchingDirector.ReadyHopDetails hop : readyHops) {
                SfaAuthority auth = hop.getAuthority();
                String requestRspec = hop.getRequestRspec();

                note("   CreateSliver call for " + auth.getUrn());
                System.out.println("   CreateSliver call for " + auth.getUrn());

                GeniConnection con = getAm2Connection(auth);

//                AbstractGeniAggregateManager.AggregateManagerReply<String> reply = am2.listResources(
//                        con, toCredentialList(sliceCredential), "geni", "3",
//                        true/*available*/, true/*compressed*/, sliceUrnStr,
//                        null);   //DEBUGREUSEPREVTEST this is for specifc debugging only, replace by commented lines below
                AbstractGeniAggregateManager.AggregateManagerReply<String> reply = am2.createSliver(
                        con, toCredentialList(sliceCredential), sliceUrnStr, requestRspec, users,
                        null);

                assertTrue(reply.getGeniResponseCode().isSuccess(), "Call not successfull: "+reply.getGeniResponseCode());

                String manifestRspec = reply.getValue();
                assert manifestRspec != null;

                director.processManifest(hop, manifestRspec);

                extractSshInfo(auth, manifestRspec);
            }

            readyHops = director.getReadyHops();
        }
    }

    @Test(hardDepends = {"callCreateSlivers"}, groups = {"createsliver"})
    public void waitForAllReady() throws GeniException, URISyntaxException {
        List<SfaAuthority> sliverAuths = director.getInvolvedAuthorities();

        long now = System.currentTimeMillis();
        long deadline = now + (20*60*1000);

        assertTrue(sliverAuths.size() > 0);

        //Test if the slivers ever becomes ready. We wait for maximum 20 minutes.
        while (now < deadline) {
            List<SfaAuthority> sliverAuthsToCheck = new ArrayList<SfaAuthority>(sliverAuths);
            assertTrue(sliverAuthsToCheck.size() > 0);

            for (SfaAuthority auth : sliverAuthsToCheck) {
                GeniConnection con = getAm2Connection(auth);

                now = System.currentTimeMillis();
                System.out.println("   Calling SliverStatus on "+auth.getUrn()+". Deadline in: "+(deadline-now)+" ms");

                AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> reply =
                        am2.sliverStatus(con, toCredentialList(sliceCredential), sliceUrnStr, null);

                Hashtable rawValue = (Hashtable) reply.getRawResult().get("value");
                String geniStatus = assertHashTableContainsNonemptyString(rawValue, "geni_status");
                if (geniStatus.equals("ready")) {
                    note("testCreatedSliverBecomesReady -> sliver ready: "+geniStatus);
                    sliverAuths.remove(auth);
                    if (sliverAuths.isEmpty()) {
                        note("testCreatedSliverBecomesReady -> all slivers ready");
                        return;
                    }
                    continue;
                } else {
                    note("testCreatedSliverBecomesReady -> sliver not ready: "+geniStatus+".");
                    System.out.println("      Sliver not ready: \""+geniStatus+"\". Deadline in: "+(deadline-now)+" ms");
                }
            }

            now = System.currentTimeMillis();
            note("testCreatedSliverBecomesReady -> At least one sliver still not ready.  Trying again in 30 seconds...");
            System.out.println("   At least one sliver still not ready. Sleeping for 30 seconds. Deadline in: "+(deadline-now)+" ms");

            try {
                Thread.sleep(30000 /*ms*/);
            } catch (InterruptedException e) { /* Ignore*/ }

            now = System.currentTimeMillis();
        }
        errorFatal("Some slivers did not become ready within 20 minutes: " + sliverAuths);
    }

    @Test(hardDepends = {"waitForAllReady"}, groups = {"nodelogin"})
    public void loginAndPing() throws GeniException, IOException {
        List<SfaAuthority> sliverAuths = director.getInvolvedAuthorities();

        boolean allLoginOk = true;

        //only try login on authorities with nodes.
        List<SfaAuthority> authsWithNode = new ArrayList<SfaAuthority>();
        for (SfaAuthority auth : sliverAuths)
            if (isNodeAuth(auth))
                authsWithNode.add(auth);

        assertTrue(authsWithNode.size() > 1, "Too few authorities with nodes ("+authsWithNode.size()+"): "+authsWithNode);

        for (SfaAuthority auth : authsWithNode) {
            SshInfo sshInfo = sshInfos.get(auth);
            if (sshInfo == null) {
                errorNonFatal("No SSH login info was extracted for " + auth.getUrn());
                allLoginOk = false;
                continue;
            }
            System.out.println("Login to " + auth.getUrn()+"  "+sshInfo);
            note("Login to "+auth.getUrn()+"  "+sshInfo);

            if (sshInfo.sshHostname == null) {
                errorNonFatal("No node / service / login in manifest RSpec, so SSH login cannot be tested for " + auth.getUrn());
                allLoginOk = false;
                continue;
            }

            //Test node login using SSH private key

            Connection conn = new Connection(sshInfo.sshHostname, sshInfo.sshPort);
            try {
                conn.connect();
            } catch (IOException e) {
                errorNonFatal("Could not connect to node \""+sshInfo.sshHostname+"\" at port "+sshInfo.sshPort);
                allLoginOk = false;
                continue;
            }
            String privateKeyToPrint = new String(sshKeyHelper.getPEMPrivateKey());
            if (privateKeyToPrint.length() > 50) privateKeyToPrint = privateKeyToPrint.substring(0, 50);
            note("Trying to log in with PEM private key:\n" + privateKeyToPrint);
            boolean isAuthenticated = conn.authenticateWithPublicKey(sshInfo.sshUsername, sshKeyHelper.getPEMRsaPrivateKey(), "nopass");
            assertTrue(isAuthenticated, "Could not login to host. " + sshInfo.sshHostname + "@" + sshInfo.sshHostname + ":" + sshInfo.sshPort);

            Session session = conn.openSession();
            BufferedReader sout = new BufferedReader(new InputStreamReader(session.getStdout()));
            BufferedReader serr = new BufferedReader(new InputStreamReader(session.getStderr()));

            List<String> pingTargets = pingTargetsForAuth(auth);
            for (String targetIP : pingTargets) {
                note("Trying to ping "+targetIP);
                /*
                Ping options on linux:
                       -c count
                              Stop after sending count ECHO_REQUEST packets. With deadline option, ping waits for count ECHO_REPLY packets, until the timeout expires.
                       -n     Numeric output only.  No attempt will be made to lookup symbolic names for host addresses.
                       -w deadline
                              Specify a timeout, in seconds, before ping exits regardless of how many packets have been sent or received. In this case ping does not stop after count packet are sent, it
                              waits either for deadline expire or until count probes are answered or for some error notification from network.
                       -W timeout
                              Time to wait for a response, in seconds. The option affects only timeout in absense of any responses, otherwise ping waits for two RTTs.
                * */

                String command = "ping -c 5 -n -w 30 "+targetIP;
                session.execCommand(command);
                String result = "";
                String err = "";
                String line = sout.readLine();
                while (line != null) {
                    result += line;
                    line = sout.readLine();
                }
                line = serr.readLine();
                while (line != null) {
                    err += line;
                    line = serr.readLine();
                }
                note("\""+"ping -c 5 -n -w 30 "+targetIP+"\" command on " + sshInfo.sshHostname + "@" + sshInfo.sshHostname + ":" + sshInfo.sshPort + " result: \"" + result.trim() + "\". (stderr is: \"" + err + "\")");
                assertTrue(result.length() > 5, "I executed \"ping\" on the remote host, and expected some reply. Instead I got: \"" + result.trim() + "\". (stderr is: \"" + err + "\")");

                //example reply:
                //   5 packets transmitted, 3 received
                Pattern patternTrans = Pattern.compile("([0-9]*) packets transmitted");
                Matcher matcherTrans = patternTrans.matcher(result);
                assertTrue (matcherTrans.find(), "Did not find \"[0-9]* packets transmitted\" in result");
                String sent = matcherTrans.group(1);

                Pattern patternRecv = Pattern.compile("([0-9]*) received");
                Matcher matcherRecv = patternRecv.matcher(result);
                assertTrue (matcherRecv.find(), "Did not find \"[0-9]* received\" in result");
                String recv = matcherRecv.group(1);

                assertEquals("5", sent, "Packets sent count is not 5: \""+sent+"\"  (recv=\""+recv+"\")");
                assertEquals("5", recv, "Packets recveived count is not 5: \""+recv+"\"");
            }

            session.close();

            conn.close();
        }

        assertTrue(allLoginOk);
    }


//DEBUGREUSEPREVTEST comment  callDelete() to reuse previous test. uncomment for normal test
    @Test(softDepends = {"loginAndPing", "callCreateSlivers", "waitForAllReady"}, hardDepends = {"createSlice"}, groups = {"createsliver","nodelogin"})
    public void callDeletes() throws GeniException {
        GeniUser user = getTestContext().getGeniUser();

        for (SfaAuthority authorityInfo: director.getInvolvedAuthorities()) {
            //do delete call
            try {
                //TODO handle errors
                String url = authorityInfo.getUrl(ServerType.GeniServerRole.AM, 2).toURI().toString();
                GeniConnection con = null;
                if (url.startsWith("http://")) {
                    note("WARNING: Connection URL is http instead of https! This is unsecure, so connection will not be tried. I will try using https instead, maybe that works.");
                    url = url.replaceFirst("http", "https");
                }
                if (url.startsWith("https://"))
                    con = new GeniSslConnection(authorityInfo,
                            url,
                            user.getCertificate(),
                            user.getPrivateKey(),
                            false/*debug*/,
                            handleUntrustedCallback);
                if (url.startsWith("http://"))
                    con = new GeniPlainConnection(authorityInfo,
                            url,
                            false/*debug*/);
                assert con != null : "URL has unsupported protocol: "+url;

                AbstractGeniAggregateManager.AggregateManagerReply<Boolean> reply =
                        am2.deleteSliver(con,  toCredentialList(sliceCredential), sliceUrnStr, null);

                note("Called delete on " + authorityInfo.getUrn() + " results: " + reply.getValue());
            } catch (GeniException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
