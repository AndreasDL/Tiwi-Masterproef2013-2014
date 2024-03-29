package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.GeniUrn;
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
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * TestAggregateManager2
 */
public class TestAggregateManager2 extends ApiTest {
    public String getTestDescription() {
        return "Many Aggregate Manager (Geni AM API v2) Tests. 2 slices and a sliver will be created during the tests. "+
                "The sliver will be deleted. "+
                "This will not test ListResources when requesting an advertisement request "+
                "(takes too long, use TestAggregateManager2AllListResources).";
    }

    private SliceAuthority sa;
    private AggregateManager2 am2;

    private CommandExecutionContext testContext;

    private GeniCredential userCredential;
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
        return testContext.getConnectionProvider().getConnectionByAuthority(testContext.getGeniUser(), testContext.getTestedAuthority(), new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }
    public GeniConnection getAM2Connection() throws GeniException {
        return testContext.getConnectionProvider().getConnectionByAuthority(testContext.getGeniUser(), testContext.getTestedAuthority(), new ServerType(ServerType.GeniServerRole.AM, 2));
    }

    private SSHKeyHelper sshKeyHelper;

    /** @param type is allowed to be null if no type to test */
    private void assertValidUrn(String urn, String type) {
        GeniUrn geniUrn = GeniUrn.parse(urn);
        assertTrue(geniUrn != null, "Urn is not valid: \"" + urn + "\"");
        if (geniUrn == null) return;
        if (type != null) assertEquals(geniUrn.getResourceType(), type, "Urn type is not correct in urn=\"" + urn + "\"");
        assertFalse(geniUrn.getTopLevelAuthority().isEmpty(), "Urn authority is empty in urn=\"" + urn + "\"");
        assertFalse(geniUrn.getResourceName().isEmpty(), "Urn resource name is empty in urn=\"" + urn + "\"");
    }

    String sshUsername;
    String sshHostname;
    int sshPort;
    private boolean isValidGeni3ManifestRspec(String rspec) {
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
            JAXBElement<be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents> doc = (JAXBElement<be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents>) u.unmarshal( new StringReader(rspec));
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
                                                    //TODO add support for missing username?

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
//                    if (el.getValue() instanceof LinkContents) {
//                        LinkContents link = (LinkContents) el.getValue();
//
//                        String linkname = link.getClientId();
//
//                        for (Object linkElO : link.getAnyOrPropertyOrLinkType()) {
//                            if (linkElO instanceof JAXBElement) {
//                                JAXBElement linkEl = (JAXBElement) linkElO;
//                                if (linkEl.getValue() instanceof InterfaceRefContents) {
//                                    InterfaceRefContents ic = (InterfaceRefContents) linkEl.getValue();
//                                    String interfaceName = ic.getClientId();
//                                }
//                            }
//                        }
//                    }
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

            sshUsername = username;
            sshHostname= hostname;
            sshPort = port;
        } else {
            note("Found no node service login in manifest RSpec.");
        }

        return true;
    }

    private boolean isEmptyRspec(String rspec) {
        //no real good test
        return rspec.length() < 30;
    }

    /** returns an RSpec for one node, for the given authority. */
    private static String getOneNodeRSpec(SfaAuthority auth) {
        if (auth.getType().equals("emulab")) {
            return "<rspec type=\"request\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">\n" +
                    "  <node client_id=\"PC\" component_manager_id=\""+auth.getUrn()+"\" exclusive=\"true\">\n" +
                    "    <sliver_type name=\"raw-pc\"/>\n" +
                    "  </node>\n" +
                    "</rspec>\n";
        }
        if (auth.getType().equals("planetlab")) {
            return "<rspec type=\"request\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">\n" +
                    "    <node component_id=\"urn:publicid:IDN+ple:unistra+node+planetlab1.u-strasbg.fr\" component_manager_id=\"urn:publicid:IDN+ple+authority+cm\" component_name=\"planetlab1.u-strasbg.fr\" exclusive=\"false\">\n" +
                    "           <hardware_type name=\"plab-pc\"/>\n" +
                    "           <hardware_type name=\"pc\"/>\n" +
                    "           <location country=\"unknown\" latitude=\"48.5237\" longitude=\"7.73833\"/>\n" +
                    "           <sliver_type name=\"plab-vserver\">\n" +
                    "           </sliver_type>\n" +
                    "       </node>\n" +
                    "   </rspec>";
        }
        return null;
    }


    private void assertValidStatus(String status) {
        List<String> apiPossibleStatus = new ArrayList<String>();
        apiPossibleStatus.add("ready");
        apiPossibleStatus.add("configuring");
        apiPossibleStatus.add("failed");
        apiPossibleStatus.add("unknown");

        List<String> otherPossibleStatus = new ArrayList<String>();
        //not valid! but it does occur a lot in practice, so we will not fail but warn for it
        otherPossibleStatus.add("changing");
        otherPossibleStatus.add("notready");

        List<String> possibleStatus = new ArrayList<String>();
        possibleStatus.addAll(apiPossibleStatus);
        possibleStatus.addAll(otherPossibleStatus);

        assertTrue(possibleStatus.contains(status),
                "Invalid geni_status: \"" + status + "\" is not one of " + possibleStatus);

        if (!apiPossibleStatus.contains(status))
            warn("SliverStatus returns invalid geni_status: \"" + status + "\" is not one of " + apiPossibleStatus);
    }

    /**
     * Check for correctness of a AM2 XmlRpc result. Should be tested for each reply received.
     * */
    public void testAM2CorrectnessXmlRpcResult(Hashtable res) {
        assertNotNull(res, "testAM2CorrectnessXmlRpcResult res is null");
        Object code = res.get("code");
        Object value = res.get("value");
        Object output = res.get("output");
        assertNotNull(code, "testAM2CorrectnessXmlRpcResult code == null in " + res);
        assertNotNull(value, "testAM2CorrectnessXmlRpcResult value == null in " + res);
        assertEquals(code.getClass(), Hashtable.class, "testAM2CorrectnessXmlRpcResult code is not Hashtable in " + res);
        Hashtable codeHt = (Hashtable) code;
        Object genicode = codeHt.get("geni_code");
        assertNotNull(genicode, "testAM2CorrectnessXmlRpcResult code does not contain \"geni_code\" in " + res);
        assertEquals(genicode.getClass(), Integer.class, "testAM2CorrectnessXmlRpcResult code.geni_code is not int in " + res);

        int genicodevalue = (Integer) genicode;
        //output should be present if code is not 0
        if (genicodevalue != 0) {
            assertNotNull(output, "testAM2CorrectnessXmlRpcResult: while geni_code is non success ("+genicodevalue+"), output == null in "+res);
            assertEquals(output.getClass(), String.class, "testAM2CorrectnessXmlRpcResult: while geni_code is non success ("+genicodevalue+"), output is not String (it is "+output.getClass().getName()+" with value \""+output.toString()+"\") in \"+res+\"");
        }
        else {
            if (output == null)
                warn("testAM2CorrectnessXmlRpcResult: while geni_code is success ("+genicodevalue+"), output == null. This is allowed but not recommended by jFed.");
            if (!(output instanceof String))
                warn("testAM2CorrectnessXmlRpcResult: while geni_code is success ("+genicodevalue+"), output is not String (it is "+output.getClass().getName()+" with value \""+output.toString()+"\"). This is allowed but not recommended by jFed.");
        }
    }


//    @DataProvider(name = "authorities")
//    public Object[][] testAllAuthorites() {
//        List<GeniAuthority> authorities = BuiltinAuthorityList.getBuiltinAuthorities();
//        Object[][] res = new Object[authorities.size()][1];
//        int i = 0;
//        for (GeniAuthority auth: authorities) {
//            res[i++][0] = auth;
//        }
//        return res;
//    }

    public void setUp(CommandExecutionContext testContext) {
        this.testContext = testContext;

        sa = new SliceAuthority(testContext.getLogger());
        am2 = new AggregateManager2(testContext.getLogger());

        //user credential needs to be retrieved from SA for most tests
        System.out.println("Fetching User credential needed for AM tests");
        SliceAuthority.SliceAuthorityReply<GeniCredential> res = null;
        try {
            res = sa.getCredential(getSAConnection());
        } catch (GeniException e) {
            throw new RuntimeException(e);
        }
        assert(res.getGeniResponseCode().isSuccess());
        assert(res.getValue() != null);
        userCredential = res.getValue();
    }


    private Hashtable versionRawResult = null;
    private AggregateManager2.VersionInfo versionInfo = null;
    @Test(groups = {"getversion"})
    public void testGetVersionXmlRpcCorrectness() throws GeniException {
        AggregateManager2.AggregateManagerReply<AggregateManager2.VersionInfo> reply = am2.getVersion(getAM2Connection());

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        //GetVersion is the only method where we have to check for something extra in the raw XMLRPC result
        versionRawResult = reply.getRawResult();
        assertNotNull(versionRawResult);

        Object ver = versionRawResult.get("geni_api"); //this is here for backward compatibility with v1. GetVersion returns this on all versions.
        assert(ver != null);
        assert(ver instanceof Integer);
        int v = ((Integer) ver).intValue();
        assert(v == 2);

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS, "GeniResponse code is not SUCCESS (0)");

        versionInfo = reply.getValue();
        assertNotNull(versionInfo);
    }
    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" } )
    public void testGetVersionResultCorrectness() throws GeniException {
        Hashtable value = (Hashtable) versionRawResult.get("value");

        //check if all required fields are present
        assertEquals(value.get("geni_api"), new Integer(2));

        assertNotNull(value.get("geni_api_versions"), "geni_api_versions not in version result");
        assertTrue(value.get("geni_api_versions") instanceof Hashtable, "geni_api_versions is not a Hashtable but a " + value.get("geni_api_versions").getClass().getName());

        String [] rspecVersionListnames = { "geni_request_rspec_versions", "geni_ad_rspec_versions" };
        for (String name : rspecVersionListnames) {
            assertNotNull(value.get(name), name + " not in version result");
            assertTrue(value.get(name) instanceof Vector, name + " is not a Vector but a " + value.get(name).getClass().getName());

            Vector v = (Vector) value.get(name);
            assertTrue(v.size() > 0, name + " array is empty");
            for (Object o : v) {
                assertTrue(o instanceof Hashtable, name + " array should contain only Hashtable not a " + o.getClass().getName());
                Hashtable t = (Hashtable) o;
                assertHashTableContainsNonemptyString(t, "type");
                assertHashTableContainsNonemptyString(t, "version");
                assertHashTableContainsString(t, "schema");
                assertHashTableContainsString(t, "namespace");
                Object extensionsO = t.get("extensions");
                assertNotNull(extensionsO);
                assertTrue(extensionsO instanceof Vector, "value for extensions of " + name + " is not a String but a " + extensionsO.getClass().getName());
                Vector extensions = (Vector) extensionsO;
                for (Object e : extensions)
                    assertTrue(e instanceof String, "an extension of \"+name+\" is not a string but a " + e.getClass().getName());
            }
        }
    }
    @Test(hardDepends = { "testGetVersionResultCorrectness" } )
    public void testGetVersionResultApiVersionsCorrectness() throws GeniException, MalformedURLException {
        Hashtable value = (Hashtable) versionRawResult.get("value");
        Hashtable versions = (Hashtable) value.get("geni_api_versions");

        //test if contains self reference
        Object o = versions.get(new Integer(2));
        if (o == null) {
            warn("geni_api_versions for Integer 2 is null. (Note that for String \"2\" it is \"" + versions.get("2") + "). This test will accept String instead of int, but the API specifies it should be int.");
            o = versions.get("2");
        }
        assertNotNull(o, "geni_api_versions for value 2 is null (tried with both int 2 and string \"2\").");
        assertTrue(o instanceof String, "value for 2 is not a String but a " + o.getClass().getName());

        //test that no url's are correct and none have localhost as hostname
        for (Object key : versions.keySet()) {
            assertTrue(key instanceof Integer || key instanceof String, "geni_api_versions keys should be Integer or String, not " + key.getClass().getName());
            int versionNr;
            if (key instanceof Integer)
                versionNr = (Integer) key;
            else {
                warn("geni_api_versions contains String key \"" + key + "\". This test will accept String instead of int, but the API specifies it should be int.");
                versionNr = Integer.parseInt((String) key);
            }
            Object val = versions.get(key);
            assertTrue(val instanceof String, "geni_api_versions values should be String, not " + val.getClass().getName());
            String urlS = (String) val;
            //check URL
            URL url = new URL(urlS);
            String host = url.getHost();
            assertFalse(host.equals("localhost"), "Illegal host in URL: " + url + " (host in URL should not be the non-global \"" + host + "\")");
            assertFalse(host.equals("127.0.0.1"), "Illegal host in URL: " + url + " (host in URL should not be the non-global \"" + host + "\")");
        }
    }
    @Test(hardDepends = { "testGetVersionResultCorrectness" }, softDepends = {"testGetVersionResultApiVersionsCorrectness"})
    public void testGetVersionResultNoDuplicates() throws GeniException {
        //Check if RSpecs are unique
        List<AggregateManager2.VersionInfo.RspecVersion> adRspecVersions = new ArrayList<AggregateManager2.VersionInfo.RspecVersion>();
        for (AggregateManager2.VersionInfo.RspecVersion rspecVer : versionInfo.getAdRspecVersions()) {
            for (AggregateManager2.VersionInfo.RspecVersion other : adRspecVersions)
                assertFalse(other.equalTypeAndVersion(rspecVer), "VersionInfo Result invalid: Duplicate Rspec type/version pair in supported Advertisement RSpec:" +
                        "type=" + other.getType() + " version=" + other.getVersion() + " VS " +
                        "type=" + rspecVer.getType() + " version=" + rspecVer.getVersion() + " ");
            adRspecVersions.add(rspecVer);
        }

        List<AggregateManager2.VersionInfo.RspecVersion> reqRspecVersions = new ArrayList<AggregateManager2.VersionInfo.RspecVersion>();
        for (AggregateManager2.VersionInfo.RspecVersion rspecVer : versionInfo.getRequestRspecVersions()) {
            for (AggregateManager2.VersionInfo.RspecVersion other : reqRspecVersions)
                assertFalse(other.equalTypeAndVersion(rspecVer), "VersionInfo Result invalid: Duplicate Rspec type/version pair in supported Request RSpec" +
                        "type=" + other.getType() + " version=" + other.getVersion() + " VS " +
                        "type=" + rspecVer.getType() + " version=" + rspecVer.getVersion() + " ");
            reqRspecVersions.add(rspecVer);
        }
    }

    @Test(hardDepends = {"testGetVersionXmlRpcCorrectness"}, softDepends = {"testGetVersionResultNoDuplicates"})
    public void testListResourcesBadCredential() throws GeniException {
        //Test without credentials. Should fail.
        AggregateManager2.AggregateManagerReply<String> reply = am2.listResources(
                getAM2Connection(),
                new ArrayList<GeniCredential>(),
                "geni",
                "3",
                true,
                true,
                null,
                null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assert ! reply.getGeniResponseCode().isSuccess();
    }

    @Test(hardDepends = {"testGetVersionXmlRpcCorrectness"}, softDepends = {"testGetVersionResultNoDuplicates"})
    public void testSliverStatusBadSlice() throws GeniException {
        String badSliceUrn = "urn:publicid:IDN+"+testContext.getUserAuthority().getNameForUrn()+"+slice+NonExisting";

        AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> reply =
                am2.sliverStatus(getAM2Connection(), getUserCredentialList(), badSliceUrn, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertFalse(reply.getGeniResponseCode().isSuccess(), "SliverStatus reply GeniResponse code is SUCCESS (0) when given a non existing slice \"" + badSliceUrn + "\"");
    }

    private String sliceUrnStrNoSliver;
    private GeniCredential sliceCredentialNoSliver;
    @Test(hardDepends = {"testGetVersionXmlRpcCorrectness"}, softDepends = {"testGetVersionResultNoDuplicates"})
    public void testCreateSliceNoSliver() throws GeniException {
        //create a slice to use in AM tests
        String sliceName = "ns"+System.currentTimeMillis();
        sliceUrnStrNoSliver = "urn:publicid:IDN+"+testContext.getUserAuthority().getNameForUrn()+"+slice+"+sliceName;
        ResourceUrn sliceUrn = new ResourceUrn(sliceUrnStrNoSliver);
        SliceAuthority.SliceAuthorityReply<GeniCredential> registerRes;
        registerRes = sa.register(getSAConnection(), userCredential, sliceUrn);
        TestSliceAuthority.testSACorrectnessXmlRpcResult(registerRes.getRawResult());
        assert registerRes.getGeniResponseCode().isSuccess() : "Register call was not successful in creating \""+sliceUrn.getValue()+"\": "+registerRes.getGeniResponseCode()+" output="+registerRes.getOutput();
        sliceCredentialNoSliver = registerRes.getValue();
        assert sliceCredentialNoSliver != null;
        assert sliceCredentialNoSliver.getCredentialXml() != null;
        assert sliceCredentialNoSliver.getCredentialXml().length() > 10 : "credential too short "+ sliceCredentialNoSliver.getCredentialXml();
    }

    @Test(hardDepends = {"testCreateSliceNoSliver"} )
    public void testSliverStatusNoSliverSlice() throws GeniException {
        assert sliceUrnStrNoSliver != null : "sliceUrnStrNoSliver is null";
        assert sliceUrnStrNoSliver.startsWith("urn:publicid:IDN+");
        assert sliceCredentialNoSliver != null : "sliceCredentialNoSliver is null";

        //then check if SliverStatus reports not found as it should
        AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> reply =
                am2.sliverStatus(getAM2Connection(), toCredentialList(sliceCredentialNoSliver), sliceUrnStrNoSliver, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertFalse(reply.getGeniResponseCode().isSuccess(),
                "SliverStatus reply GeniResponse code is SUCCESS (0) when given a slice \"" + sliceUrnStrNoSliver + "\" that has no sliver. (should fail because no sliver found for the slice)");

        assertNotNull(reply.getRawResult());
        Object rawValueResult = reply.getRawResult().get("value");
        if (rawValueResult != null) {
            //it does not need to have a result, but when it does, it should be a valid one.
            warnIfNot(rawValueResult instanceof Hashtable, "SliverStatus result is not null, which is allowed on failure,"+
                    " but in that case it is expected to be a valid response, which would be a struct. However, it is a "+
                    rawValueResult.getClass().getSimpleName()+" -> value=\""+rawValueResult.toString()+"\"");
        }
        warnIfNot((rawValueResult != null) == (reply.getValue() != null),
                "Mismatch: SliverStatus processed result value is "+
                (reply.getValue() != null ? "not null" : "null")+" but RAW SliverStatus result value is "+
                (rawValueResult != null ? "not null" : "null"));
  }

    @Test(hardDepends = {"testCreateSliceNoSliver"} )
    public void testListResourcesNoSliverSlice() throws GeniException {
        assert sliceUrnStrNoSliver != null : "sliceUrnStrNoSliver is null";
        assert sliceUrnStrNoSliver.startsWith("urn:publicid:IDN+");
        assert sliceCredentialNoSliver != null : "sliceCredentialNoSliver is null";

        //check if SliverStatus reports not found as it should
        //check without available option
        AggregateManager2.AggregateManagerReply<String> reply =
                am2.listResources(getAM2Connection(), toCredentialList(sliceCredentialNoSliver), "geni", "3", null/*available*/, true/*compressed*/, sliceUrnStrNoSliver, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        String specsText = "ListResources returns an RSpec in GENI standard schema. \n" +
                "When a valid geni_slice_urn option is supplied, the returned RSpec will be a Manifest RSpec of the type corresponding to geni_rspec_version, but in manifest format. \n" +
                "If no resources are allocated to the indicated slice by this aggregate, an empty RSpec should be returned -- aggregates should NOT return an error.";

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "ListResources reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=unspecified). (according to the specs:\"" + specsText + "\")");

        String rspec = reply.getValue();
        assertNotNull(rspec,
                "ListResources reply is completely empty when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=unspecified). (according to the specs:\"" + specsText + "\")");

        assertTrue(isValidGeni3ManifestRspec(rspec),
                "ListResources reply when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=unspecified) is not an valid RSpec (according to the specs:\"" + specsText + "\"): " + rspec);

        assertTrue(isEmptyRspec(rspec),
                "ListResources reply when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=unspecified) is not an empty RSpec (according to the specs:\"" + specsText + "\"): " + rspec);

        //TODO check if it is a manifest and not a request or advertisement RSpec
    }

    /* same as testListResourcesNoSliverSlice but with available option added. should make no difference. */
    @Test(hardDepends = {"testCreateSliceNoSliver"} )
    public void testListResourcesNoSliverSliceAvailable() throws GeniException {
        assert sliceUrnStrNoSliver != null : "sliceUrnStrNoSliver is null";
        assert sliceUrnStrNoSliver.startsWith("urn:publicid:IDN+");
        assert sliceCredentialNoSliver != null : "sliceCredentialNoSliver is null";

        String specsText = "ListResources returns an RSpec in GENI standard schema. \n" +
                "When a valid geni_slice_urn option is supplied, the returned RSpec will be a Manifest RSpec of the type corresponding to geni_rspec_version, but in manifest format. \n" +
                "If no resources are allocated to the indicated slice by this aggregate, an empty RSpec should be returned -- aggregates should NOT return an error.";

        //Test with available TRUE
        AggregateManager2.AggregateManagerReply<String> reply =
                am2.listResources(getAM2Connection(), toCredentialList(sliceCredentialNoSliver), "geni", "3", true/*available*/, true/*compressed*/, sliceUrnStrNoSliver, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "ListResources reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=true). (according to the specs:\"" + specsText + "\")");

        String rspec = reply.getValue();
        assertNotNull(rspec,
                "ListResources reply is completely empty when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=unspecified). (according to the specs:\"" + specsText + "\")");

        assertTrue(isValidGeni3ManifestRspec(rspec),
                "ListResources reply  when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=true) is not an valid RSpec (according to the specs:\"" + specsText + "\"): " + rspec);

        assertTrue(isEmptyRspec(rspec),
                "ListResources reply  when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=true) is not an empty RSpec (according to the specs:\"" + specsText + "\"): " + rspec);


        //Test with available FALSE
        reply = am2.listResources(getAM2Connection(), toCredentialList(sliceCredentialNoSliver), "geni", "3", false/*available*/, true/*compressed*/, sliceUrnStrNoSliver, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "ListResources reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=false). (according to the specs:\"" + specsText + "\")");

        rspec = reply.getValue();
        assertNotNull(rspec,
                "ListResources reply is completely empty when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=unspecified). (according to the specs:\"" + specsText + "\")");

        assertTrue(isValidGeni3ManifestRspec(rspec),
                "ListResources reply  when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=false) is not an valid RSpec (according to the specs:\"" + specsText + "\"): " + rspec);

        assertTrue(isEmptyRspec(rspec),
                "ListResources reply  when given a slice \"" + sliceUrnStrNoSliver + "\" that has" +
                        " no sliver (available=false) is not an empty RSpec (according to the specs:\"" + specsText + "\"): " + rspec);
    }






    private String sliceUrnStrSliver;
    private GeniCredential sliceCredentialSliver;
    @Test(softDepends = {"testGetVersionResultApiVersionsCorrectness", "testGetVersionResultNoDuplicates"}, hardDepends = {"testGetVersionXmlRpcCorrectness"} )
    public void testCreateSliceSliver() throws GeniException {
        //create a slice to use in AM tests
        String sliceName = "s"+System.currentTimeMillis();
        sliceUrnStrSliver = "urn:publicid:IDN+"+testContext.getUserAuthority().getNameForUrn()+"+slice+"+sliceName;
        assertValidUrn(sliceUrnStrSliver, "slice");
        ResourceUrn sliceUrn = new ResourceUrn(sliceUrnStrSliver);
        SliceAuthority.SliceAuthorityReply<GeniCredential> registerRes;
        registerRes = sa.register(getSAConnection(), userCredential, sliceUrn);
        TestSliceAuthority.testSACorrectnessXmlRpcResult(registerRes.getRawResult());
        assert registerRes.getGeniResponseCode().isSuccess() : "Register call was not successful in creating \""+sliceUrn.getValue()+"\": "+registerRes.getGeniResponseCode()+" output="+registerRes.getOutput();
        sliceCredentialSliver = registerRes.getValue();
        assert sliceCredentialSliver != null;
        assert sliceCredentialSliver.getCredentialXml() != null;
        assert sliceCredentialSliver.getCredentialXml().length() > 10 : "credential too short "+ sliceCredentialSliver.getCredentialXml();
    }

    @Test(hardDepends = {"testCreateSliceSliver"} )
    public void testCreateSliver() throws GeniException, NoSuchAlgorithmException {
        assert sliceUrnStrSliver != null : "sliceUrnStrSliver is null";
        assertValidUrn(sliceUrnStrSliver, "slice");
        assert sliceCredentialSliver != null : "sliceCredentialSliver is null";

        sshKeyHelper = new SSHKeyHelper();

        String requestRspec = getOneNodeRSpec(testContext.getTestedAuthority());
        if (requestRspec == null) skip("testCreateSliver skipped, because no RSpec example known for type=\""+testContext.getTestedAuthority().getType()+"\"");

        //CreateSliver needs to generate it's own SSH keypair and use the public key.
        String sshKey = "todo";
        Vector<String> sshKeys = new Vector<String>();
        sshKeys.add(sshKeyHelper.getSshPublicKeyString());
        UserSpec user = new UserSpec(testContext.getGeniUser().getUserUrn(), sshKeys);
        List<UserSpec> users = new ArrayList<UserSpec>();
        users.add(user);
        AggregateManager2.AggregateManagerReply<String> reply = am2.createSliver(getAM2Connection(), toCredentialList(sliceCredentialSliver), sliceUrnStrSliver, requestRspec, users, null);

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS, "CreateSliver did not succeed");

        String manifestRspec = reply.getValue();
        assertTrue(isValidGeni3ManifestRspec(manifestRspec),
                "CreateSliver did not return a valid RSpec" + manifestRspec);

        assertFalse(isEmptyRspec(manifestRspec),
                "CreateSliver returned and empty RSpec: " + manifestRspec);

        System.out.println("Created sliver for \"" + sliceUrnStrSliver + "\" manifestRspec=" + manifestRspec);
    }

    public void validSuccesStatus(AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> reply) {
        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "SliverStatus reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrSliver + "\" that has a sliver.");

        assertEquals(reply.getRawResult().get("value").getClass(), Hashtable.class,
                "SliverStatus value should be a Hashtable. Not: " + reply.getRawResult().get("value"));

        Hashtable rawValue = (Hashtable) reply.getRawResult().get("value");
        String geniUrn = assertHashTableContainsNonemptyString(rawValue, "geni_urn");
        assertValidUrn(geniUrn, null); //type slice or sliver allowed
        String urntype = GeniUrn.parse(geniUrn).getResourceType();
        if (!urntype.equals("sliver")) {
            if (urntype.equals("slice"))
                warn("URN type in SliverStatus is \"slice\", but expecting \"sliver\". This is not in compliance with API.");
            else
                throw new RuntimeException("urntype is "+urntype+" in "+geniUrn+" but expecting \"sliver\"");
        }
        String geniStatus = assertHashTableContainsNonemptyString(rawValue, "geni_status");
        assertValidStatus(geniStatus);
        Vector geniResources = assertHashTableContainsVector(rawValue, "geni_resources");

        for (Object geniResource : geniResources) {
            assertNotNull(geniResource);
            assertEquals(geniResource.getClass(), Hashtable.class, "one of the geni_resources is not a Hashtable but a " + geniResource.getClass().getName());
            Hashtable ht = (Hashtable) geniResource;
            String resourceUrn = assertHashTableContainsNonemptyString(ht, "geni_urn");
            assertValidUrn(resourceUrn, null);
            String resourceStatus = assertHashTableContainsNonemptyString(ht, "geni_status");
            assertValidStatus(resourceStatus);
            assertHashTableContainsString(ht, "geni_error");
        }
    }

    @Test(hardDepends = {"testCreateSliver"} )
    public void testSliverStatusExistingSliver() throws GeniException {
        //Test SliverStatus with created sliver
        assert sliceUrnStrSliver != null : "sliceUrnStrSliver is null";
        assertValidUrn(sliceUrnStrSliver, "slice");
        assert sliceCredentialSliver != null : "sliceCredentialSliver is null";

        AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> reply =
                am2.sliverStatus(getAM2Connection(), toCredentialList(sliceCredentialSliver), sliceUrnStrSliver, null);

        validSuccesStatus(reply);
    }

    @Test(hardDepends = {"testCreateSliver"} )
    public void testListResourcesExistingSliver() throws GeniException {
        //Test ListResources on created sliver
        assert sliceUrnStrSliver != null : "sliceUrnStrSliver is null";
        assertValidUrn(sliceUrnStrSliver, "slice");
        assert sliceCredentialSliver != null : "sliceCredentialSliver is null";

        AggregateManager2.AggregateManagerReply<String> reply =
                am2.listResources(getAM2Connection(), toCredentialList(sliceCredentialSliver), "geni", "3", null/*available*/, true/*compressed*/, sliceUrnStrSliver, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "ListResources reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrSliver + "\" that has a sliver.");

        String rspec = reply.getValue();
        assertTrue(isValidGeni3ManifestRspec(rspec),
                "ListResources reply  when given a slice \"" + sliceUrnStrSliver + "\" that has" +
                        " a sliver is not an valid RSpec: " + rspec);

        assertFalse(isEmptyRspec(rspec),
                "ListResources reply  when given a slice \"" + sliceUrnStrSliver + "\" that has" +
                        " a sliver is an empty RSpec: " + rspec);
    }

    @Test(hardDepends = { "testCreateSliver" }, softDepends = { "testSliverStatusExistingSliver", "testListResourcesExistingSliver" }, groups = {"createsliver"} )
    public void testCreatedSliverBecomesReady() throws GeniException {
        //Test if the sliver ever becomes ready. We wait for maximum 20 minutes.
        assert sliceUrnStrSliver != null : "sliceUrnStrSliver is null";
        assertValidUrn(sliceUrnStrSliver, "slice");
        assert sliceCredentialSliver != null : "sliceCredentialSliver is null";

        long now = System.currentTimeMillis();
        long deadline = now + (20*60*1000);
        while (now < deadline) {
            AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> reply =
                    am2.sliverStatus(getAM2Connection(), toCredentialList(sliceCredentialSliver), sliceUrnStrSliver, null);

            validSuccesStatus(reply);
            Hashtable rawValue = (Hashtable) reply.getRawResult().get("value");
            String geniStatus = assertHashTableContainsNonemptyString(rawValue, "geni_status");
            if (geniStatus.equals("ready")) {
                System.out.println("testCreatedSliverBecomesReady -> sliver ready: "+rawValue);
                return;
            }

            System.out.println("testCreatedSliverBecomesReady -> sliver not ready: "+geniStatus+".  Trying again in 30 seconds...");
            try {
                Thread.sleep(30000 /*ms*/);
            } catch (InterruptedException e) { /* Ignore*/ }
            now = System.currentTimeMillis();
        }
        throw new RuntimeException("Sliver did not become ready within 20 minutes!");
    }

    @Test(hardDepends = {"testCreatedSliverBecomesReady"}, groups = {"nodelogin"} )
    public void testNodeLogin() throws GeniException, IOException {
        if (sshHostname == null)
            skip("No node / service / login in manifest RSpec, so SSH login cannot be tested.");

        //Test node login using SSH private key

        Connection conn = new Connection(sshHostname, sshPort);
        conn.connect();
        String privateKeyToPrint = new String(sshKeyHelper.getPEMPrivateKey());
        if (privateKeyToPrint.length() > 50) privateKeyToPrint = privateKeyToPrint.substring(0, 50);
        note("Trying to log in with PEM private key:\n" + privateKeyToPrint);
        boolean isAuthenticated = conn.authenticateWithPublicKey(sshUsername, sshKeyHelper.getPEMRsaPrivateKey(), "nopass");
        assertTrue(isAuthenticated, "Could not login to host. " + sshHostname + "@" + sshHostname + ":" + sshPort);

        Session session = conn.openSession();
        BufferedReader sout = new BufferedReader(new InputStreamReader(session.getStdout()));
        BufferedReader serr = new BufferedReader(new InputStreamReader(session.getStderr()));
        //session.execCommand("/usr/bin/who");
        session.execCommand("ls /");
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
//        note("\"who\" command on "+sshHostname+"@"+sshHostname+":"+sshPort+" result: \""+result.trim()+"\". (stderr is: \"" + err + "\")");
//        assertTrue(result.contains(sshUsername), "I executed \"/usr/bin/who\" on the remote host, and expected a return containing my username (\"" + sshUsername + "\"). Instead I got: \"" + result.trim() + "\". (stderr is: \"" + err + "\")");

        note("\"ls /\" command on " + sshHostname + "@" + sshHostname + ":" + sshPort + " result: \"" + result.trim() + "\". (stderr is: \"" + err + "\")");
        assertTrue(result.length() > 5, "I executed \"ls /\" on the remote host, and expected some reply. Instead I got: \"" + result.trim() + "\". (stderr is: \"" + err + "\")");

        session.close();

        conn.close();
    }

    @Test(hardDepends = {"testCreatedSliverBecomesReady"}, softDepends = {"testNodeLogin"} )
    public void testRenewSliver() throws GeniException {
        assert sliceUrnStrSliver != null : "sliceUrnStrSliver is null";
        assertValidUrn(sliceUrnStrSliver, "slice");
        assert sliceCredentialSliver != null : "sliceCredentialSliver is null";

        //Test Renew Sliver. Renew so it expires in 5 minutes
        Date expirationTime = new Date(System.currentTimeMillis() + (5 * 60 * 1000));
        AggregateManager2.AggregateManagerReply<Boolean> reply = am2.renewSliver(getAM2Connection(), toCredentialList(sliceCredentialSliver), sliceUrnStrSliver, expirationTime, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "RenewSliver reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrSliver + "\" that has a sliver.");

        assertTrue(reply.getValue(),
                "RenewSliver reply's value (which means success) is not true when given a slice \"" + sliceUrnStrSliver + "\" that has a sliver.");

    }

    /* Test must run after tests that use sliver. Will always run, to cleanup any created sliver. */
    @Test(softDepends = {"testNodeLogin", "testRenewSliver", "testCreatedSliverBecomesReady"}, groups = {"createsliver","nodelogin"})
    public void testDeleteSliver() throws GeniException {
        if (sliceUrnStrSliver == null) skip("DEBUG: Skipped because other test created nothing to delete");

        assert sliceUrnStrSliver != null : "sliceUrnStrSliver is null";
        assertValidUrn(sliceUrnStrSliver, "slice");
        assert sliceCredentialSliver != null : "sliceCredentialSliver is null";

        System.out.println("DeleteSliver for slice " + sliceUrnStrSliver);

        //Test Delete Sliver
        AggregateManager2.AggregateManagerReply<Boolean> reply = am2.deleteSliver(getAM2Connection(), toCredentialList(sliceCredentialSliver), sliceUrnStrSliver, null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "DeleteSliver reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceUrnStrSliver + "\" that has a sliver.");

        assertTrue(reply.getValue(),
                "DeleteSliver reply's value (which means success) is not true when given a slice \"" + sliceUrnStrSliver + "\" that has a sliver.");
    }

    //TODO we could also test if DeleteSliver worked
}
