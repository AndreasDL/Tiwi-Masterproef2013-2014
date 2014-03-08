package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.rspec.request.geni_rspec_3.*;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.SSHKeyHelper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;

/**
 * TestAggregateManager2ListResources
 */
public class TestAggregateManager2ListResources extends ApiTest {
    public String getTestDescription() {
        return "Test ListResources call. ListResources can take some time and bandwidth. " +
                "This also tests all supported advertisement RSpec of an Aggregate Manager. A ListResources calls might be done for each.";
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


    private boolean isValidGeni3AdvertisementRspec(String rspec) {
        if (rspec == null) return false;
        assertNotNull(rspec, "Rspec is null");

        int nodeCount = 0;
        int loginCount = 0;

        //parse the RSpec XML
        try {
            Class docClass = RSpecContents.class;
            String packageName = docClass.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<RSpecContents> doc = (JAXBElement<RSpecContents>) u.unmarshal( new StringReader(rspec));
            RSpecContents c = doc.getValue();

            RspecTypeContents typ = c.getType();
            assertEquals(typ.value(), "manifest", "Received manifest RSpec is not a manifest.");

            for (Object o : c.getAnyOrNodeOrLink()) {
                if (o instanceof JAXBElement) {
                    JAXBElement el = (JAXBElement) o;
                    if (el.getValue() instanceof NodeContents) {
                        NodeContents node = (NodeContents) el.getValue();

                        String nodeName = node.getClientId();
                        nodeCount++;

                        for (Object nodeElO : node.getAnyOrRelationOrLocation()) {
                            if (nodeElO instanceof JAXBElement) {
                                JAXBElement nodeEl = (JAXBElement) nodeElO;
//                                if (nodeEl.getValue() instanceof InterfaceContents) {
//                                    InterfaceContents ic = (InterfaceContents) nodeEl.getValue();
//                                    String interfaceName = ic.getClientId();
//                                }
                                if (nodeEl.getValue() instanceof ServiceContents) {
                                    ServiceContents serviceC = (ServiceContents) nodeEl.getValue();
                                    for (Object serviceElO : serviceC.getAnyOrLoginOrInstall()) {
                                        if (serviceElO instanceof JAXBElement) {
                                            JAXBElement serviceEl = (JAXBElement) serviceElO;
                                            if (serviceEl.getValue() instanceof LoginServiceContents) {
                                                LoginServiceContents loginSC = (LoginServiceContents) serviceEl.getValue();

                                                String auth = loginSC.getAuthentication();

                                                String aHostname = loginSC.getHostname();
                                                int aPort = 0;
                                                if (loginSC.getPort() != null)
                                                    aPort = Integer.parseInt(loginSC.getPort());
                                                String aUsername = loginSC.getOtherAttributes().get(new QName("username"));

                                                if (auth.equals("ssh-keys")) {
                                                    assertEquals("ssh-keys", auth, "service login authentication must be ssh-keys for node "+nodeName);
                                                    assertNotNull(aHostname, "no hostname in service login for node "+nodeName);
                                                    assertNotNull(aUsername, "no username in service login for node "+nodeName);
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

        return true;
    }

    private boolean isEmptyRspec(String rspec) {
        //no real good test
        return rspec.length() < 30;
    }

    /**
     * Check for correctness of a AM2 XmlRpc result. Should be tested for each reply received.
     * */
    public void testAM2CorrectnessXmlRpcResult(Hashtable res) {
        Object code = res.get("code");
        Object value = res.get("value");
        Object output = res.get("output");
        assertNotNull(code, "testAM2CorrectnessXmlRpcResult code == null in "+res);
        assertNotNull(value, "testAM2CorrectnessXmlRpcResult value == null in "+res);
        assertEquals(code.getClass(), Hashtable.class, "testAM2CorrectnessXmlRpcResult code is not Hashtable in "+res);
        Hashtable codeHt = (Hashtable) code;
        Object genicode = codeHt.get("geni_code");
        assertNotNull(genicode, "testAM2CorrectnessXmlRpcResult code does not contain \"geni_code\" in "+res);
        assertEquals(genicode.getClass(), Integer.class, "testAM2CorrectnessXmlRpcResult code.geni_code is not int in "+res);

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

    public void setUp(CommandExecutionContext testContext)  {
        this.testContext = testContext;

        sa = new SliceAuthority(testContext.getLogger());
        am2 = new AggregateManager2(testContext.getLogger());

        //user credential needs to be retrieved from SA for most tests
        note("Fetching User credential needed for AM tests");
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
    @Test
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
        assertTrue(value.get("geni_api_versions") instanceof Hashtable, "geni_api_versions is not a Hashtable but a "+value.get("geni_api_versions").getClass().getName());

        String [] rspecVersionListnames = { "geni_request_rspec_versions", "geni_ad_rspec_versions" };
        for (String name : rspecVersionListnames) {
            assertNotNull(value.get(name), name+" not in version result");
            assertTrue(value.get(name) instanceof Vector, name+" is not a Vector but a "+value.get(name).getClass().getName());

            Vector v = (Vector) value.get(name);
            assertTrue(v.size() > 0, name+" array is empty");
            for (Object o : v) {
                assertTrue(o instanceof Hashtable, name+" array should contain only Hashtable not a "+o.getClass().getName());
                Hashtable t = (Hashtable) o;
                assertHashTableContainsNonemptyString(t, "type");
                assertHashTableContainsNonemptyString(t, "version");
                assertHashTableContainsString(t, "schema");
                assertHashTableContainsString(t, "namespace");
                Object extensionsO = t.get("extensions");
                assertNotNull(extensionsO);
                assertTrue(extensionsO instanceof Vector, "value for extensions of "+name+" is not a String but a " + extensionsO.getClass().getName());
                Vector extensions = (Vector) extensionsO;
                for (Object e : extensions)
                    assertTrue(e instanceof String, "an extension of \"+name+\" is not a string but a "+e.getClass().getName());
            }
        }
    }
    @Test(hardDepends = { "testGetVersionResultCorrectness" } )
    public void testGetVersionResultNoDuplicates() throws GeniException {
        //Check if RSpecs are unique
        List<AggregateManager2.VersionInfo.RspecVersion> adRspecVersions = new ArrayList<AggregateManager2.VersionInfo.RspecVersion>();
        for (AggregateManager2.VersionInfo.RspecVersion rspecVer : versionInfo.getAdRspecVersions()) {
            for (AggregateManager2.VersionInfo.RspecVersion other : adRspecVersions)
                assertFalse(other.equalTypeAndVersion(rspecVer), "VersionInfo Result invalid: Duplicate Rspec type/version pair in supported Advertisement RSpec:"+
                        "type="+other.getType()+" version="+other.getVersion()+" VS "+
                        "type="+rspecVer.getType()+" version="+rspecVer.getVersion()+" ");
            adRspecVersions.add(rspecVer);
        }

        List<AggregateManager2.VersionInfo.RspecVersion> reqRspecVersions = new ArrayList<AggregateManager2.VersionInfo.RspecVersion>();
        for (AggregateManager2.VersionInfo.RspecVersion rspecVer : versionInfo.getRequestRspecVersions()) {
            for (AggregateManager2.VersionInfo.RspecVersion other : reqRspecVersions)
                assertFalse(other.equalTypeAndVersion(rspecVer), "VersionInfo Result invalid: Duplicate Rspec type/version pair in supported Request RSpec"+
                                        "type="+other.getType()+" version="+other.getVersion()+" VS "+
                                        "type="+rspecVer.getType()+" version="+rspecVer.getVersion()+" ");
            reqRspecVersions.add(rspecVer);
        }
    }

    @DataProvider(name = "testListResourcesDataProvider")
    public Object[][] getTypeVersionList() {
        assert versionInfo != null;

        Object[][] res = new Object[versionInfo.getAdRspecVersions().size()][1];

        int i = 0;
        for (AggregateManager2.VersionInfo.RspecVersion rspecVersion : versionInfo.getAdRspecVersions()) {
            res[i++][0] = rspecVersion;
        }
        return res;
    }

    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness", "testGetVersionResultCorrectness", "testGetVersionResultNoDuplicates" },
            dataProvider = "testListResourcesDataProvider")
    public void testListAllResources(AggregateManager2.VersionInfo.RspecVersion rspecVersion) throws GeniException {
        note("Testing ListResources for type=" + rspecVersion.getType() + " version=" + rspecVersion.getVersion());

        //test with good credentials
        AggregateManager2.AggregateManagerReply<String> reply = am2.listResources(
                getAM2Connection(),
                getUserCredentialList(),
                rspecVersion.getType(),
                rspecVersion.getVersion(),
                true,
                true,
                null,
                null);

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

        assertTrue( reply.getGeniResponseCode().isSuccess() );
        if (rspecVersion.getType().equals("geni") && rspecVersion.getVersion().equals("3"))
            assertTrue(isValidGeni3AdvertisementRspec(reply.getValue()) );
        assertTrue( !isEmptyRspec(reply.getValue()) );

        //TODO test if same version as requested?
    }
}
