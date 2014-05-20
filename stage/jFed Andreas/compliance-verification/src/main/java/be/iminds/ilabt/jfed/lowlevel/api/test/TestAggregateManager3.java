package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.RFC3339Util;
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
import java.text.ParseException;
import java.util.*;

/**
 * TestAggregateManager3
 */
public class TestAggregateManager3 extends ApiTest {
    public String getTestDescription() {
        return "Many Aggregate Manager (Geni AM API v3) Tests. 2 slices and a sliver will be created during the tests. "+
                "The sliver will be deleted. This will not test ListResources.";
    }

    private SliceAuthority sa;
    private AggregateManager3 am3;
    private CommandExecutionContext testContext;

    public GeniConnection getAM3Connection() throws GeniException {
        return testContext.getConnectionProvider().getConnectionByAuthority(testContext.getGeniUser(), testContext.getTestedAuthority(), new ServerType(ServerType.GeniServerRole.AM, 3));
    }
    public GeniConnection getSAConnection() throws GeniException {
        return testContext.getConnectionProvider().getConnectionByAuthority(testContext.getGeniUser(), testContext.getTestedAuthority(), new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }

    /**
     * Check for correctness of a AM3 XmlRpc result. Should be tested for each reply received.
     * */
    public void testAM3CorrectnessXmlRpcResult(Hashtable res) {
        Object code = res.get("code");
        Object value = res.get("value");
        Object output = res.get("output");
        assertNotNull(code, "testAM3CorrectnessXmlRpcResult code == null in "+res);
        assertNotNull(value, "testAM3CorrectnessXmlRpcResult value == null in "+res);
        assertEquals(code.getClass(), Hashtable.class, "testAM3CorrectnessXmlRpcResult code is not Hashtable in "+res);
        Hashtable codeHt = (Hashtable) code;
        Object genicode = codeHt.get("geni_code");
        assertNotNull(genicode, "testAM3CorrectnessXmlRpcResult code does not contain \"geni_code\" in "+res);
        assertEquals(genicode.getClass(), Integer.class, "testAM3CorrectnessXmlRpcResult code.geni_code is not int in "+res);

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

    public void setUp(CommandExecutionContext testContext) {
        this.testContext = testContext;

        am3 = new AggregateManager3(testContext.getLogger());
        sa = new SliceAuthority(testContext.getLogger());

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




    /* if warnString is null, will not warn but be strict about requiring an integer instead! */
    private int assertContainsInteger(Hashtable t, String key, String warnString) {
        Object o = t.get(key);
        assertNotNull(o, "value for "+key+" is null");
        if (warnString == null || o.getClass().equals(Integer.class)) {
            assertEquals(o.getClass(), Integer.class, "value for " + key + " is not an Integer but a " + o.getClass().getName());
            Integer i = (Integer) o;
            return i;
        } else {
            assertEquals(o.getClass(), String.class, "value for " + key + " is not an Integer, and not even a String but a " + o.getClass().getName());
            warn(warnString);
            String i = (String) o;
            return Integer.parseInt(i);
        }
    }

    /** returns an RSpec for one node, for the given authority. */
    private static String getOneNodeRequestRSpec(SfaAuthority auth) {
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

    private SSHKeyHelper sshKeyHelper;
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
            JAXBElement<RSpecContents> doc = (JAXBElement<be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents>) u.unmarshal( new StringReader(rspec));
            be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RSpecContents c = doc.getValue();

            be.iminds.ilabt.jfed.rspec.manifest.geni_rspec_3.RspecTypeContents typ = c.getType();
            assertNotNull(typ, "Received manifest RSpec does not specify a type: "+rspec);
            assertEquals(typ.value(), "manifest", "Received manifest RSpec is not a manifest: "+rspec);

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
                                                    assertEquals("ssh-keys", auth, "service login authentication must be ssh-keys for node "+nodeName);
                                                    assertNotNull(aHostname, "no hostname in service login for node "+nodeName);
                                                    assertNotNull(aUsername, "no username in service login for node "+nodeName);
                                                    //TODO add support for missing username?

                                                    hostname = aHostname;
                                                    port = aPort;
                                                    username = aUsername;

                                                    loginCount++;
                                                } else {
                                                    note("Unsupported auth type in manifest RSpec service login: "+auth);
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
            assertTrue(loginCount > 0, "no service login found ("+loginCount+" service logins for "+nodeCount+" nodes)");
            assertNotNull(hostname);
            assertNotNull(username);

            if (loginCount > 1)
                note("Found multiple node logins in manifest rspec ("+loginCount+" service logins for "+nodeCount+" nodes), using last one.");

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

    /** @param type is allowed to be null if no type to test */
    private void assertValidUrn(String urn, String type) {
        GeniUrn geniUrn = GeniUrn.parse(urn);
        assertTrue(geniUrn != null, "Urn is not valid: \"" + urn + "\"");
        if (geniUrn == null) return;
        if (type != null) assertEquals(geniUrn.getResourceType(), type, "Urn type is not correct in urn=\"" + urn + "\"");
        assertFalse(geniUrn.getTopLevelAuthority().isEmpty(), "Urn authority is empty in urn=\"" + urn + "\"");
        assertFalse(geniUrn.getResourceName().isEmpty(), "Urn resource name is empty in urn=\"" + urn + "\"");
    }



    private static List<GeniCredential> toCredentialList(GeniCredential c) {
        List<GeniCredential> res = new ArrayList<GeniCredential>();
        res.add(c);
        return res;
    }
    private GeniCredential userCredential;
    private List<GeniCredential> getUserCredentialList() {
        return toCredentialList(userCredential);
    }


    private static List<String> toStringList(String s) {
        List<String> res = new ArrayList<String>();
        res.add(s);
        return res;
    }
    private static List<String> toStringList(String s1, String s2) {
        List<String> res = new ArrayList<String>();
        res.add(s1);
        res.add(s2);
        return res;
    }
    private static List<String> toStringList(String s1, String s2, String s3) {
        List<String> res = new ArrayList<String>();
        res.add(s1);
        res.add(s2);
        res.add(s3);
        return res;
    }




    private Hashtable versionRawResult = null;
    private AggregateManager3.VersionInfo versionInfo = null;
    @Test(groups = {"getversion"})
    public void testGetVersionXmlRpcCorrectness() throws GeniException {
        AggregateManager3.AggregateManagerReply<AggregateManager3.VersionInfo> reply = am3.getVersion(getAM3Connection());

        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        //GetVersion is the only method where we have to check for something extra in the raw XMLRPC result
        versionRawResult = reply.getRawResult();
        assertNotNull(versionRawResult);

        Object ver = versionRawResult.get("geni_api"); //this is here for backward compatibility with v1. GetVersion returns this on all versions.
        assert(ver != null);
        assert(ver instanceof Integer);
        int v = ((Integer) ver).intValue();
        assertEquals(v, 3, "AM (backward compatibility \"geni_api\" field) is not version 3");


        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS, "GeniResponse code is not SUCCESS (0)");

        versionInfo = reply.getValue();
        assertNotNull(versionInfo);
        assertEquals(versionInfo.getApi(), 3, "AM is not version 3");
    }

    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" }, groups = {"getversion"})
    public void testGetVersionResultCorrectness() throws GeniException {
        Hashtable value = (Hashtable) versionRawResult.get("value");

        //check if all required fields are present
        assertEquals(value.get("geni_api"), new Integer(3));

        assertNotNull(value.get("geni_api_versions"), "geni_api_versions not in version result");
        assertTrue(value.get("geni_api_versions") instanceof Hashtable, "geni_api_versions is not a Hashtable but a "+value.get("geni_api_versions").getClass().getName());

        String [] rspecVersionListnames = { "geni_request_rspec_versions", "geni_ad_rspec_versions" };
        for (String name : rspecVersionListnames) {
            assertNotNull(value.get(name), name+" not in GetVersion result");
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
        String [] credentialTypeListnames = { "geni_credential_types" };
        for (String name : credentialTypeListnames) {
            assertNotNull(value.get(name), name+" not in GetVersion result");
            assertTrue(value.get(name) instanceof Vector, name+" is not a Vector but a "+value.get(name).getClass().getName());

            Vector v = (Vector) value.get(name);
            assertTrue(v.size() > 0, name+" array is empty");
            for (Object o : v) {
                assertTrue(o instanceof Hashtable, name+" array should contain only Hashtable not a "+o.getClass().getName());
                Hashtable t = (Hashtable) o;
                String typ = assertHashTableContainsNonemptyString(t, "geni_type");
                //version 3 actually requires a String! But that string should contain an integer.
                //TODO support actual integer as well, but with warning
//                int ver = assertContainsInteger(t, "geni_version", "WARNING: GetVersion result - \"geni_credential_types\" - \"geni_version\" is a String, "+
//                        "but it should have been an Integer. (will parse String to Integer and continue)");
                String ver = assertHashTableContainsNonemptyString(t, "geni_version");
            }
        }
    }
    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" }, softDepends = {"testGetVersionResultCorrectness"}, groups = {"getversion"})
    public void testGetVersionResultApiVersionsCorrectness() throws GeniException, MalformedURLException {
        Hashtable value = (Hashtable) versionRawResult.get("value");
        Hashtable versions = (Hashtable) value.get("geni_api_versions");

        //test if contains self reference
        Object o = versions.get(new Integer(3));
        if (o == null) {
            o = versions.get("3");
            if (o != null)
                warn("geni_api_versions for key=3 (Integer) is null. (Note that for key=\"3\" (String) it is \"" + versions.get("3") + "). This test will accept String instead of int, but the API specifies it should be int.");
        }
        assertNotNull(o, "geni_api_versions for key=3 is null (tried with both int 3 and string \"3\").");
        assertTrue(o instanceof String, "value for key=3 is not a String but a " + o.getClass().getName());

        //test that no url's are incorrect and none have localhost as hostname
        for (Object key : versions.keySet()) {
            assertTrue(key instanceof Integer || key instanceof String, "geni_api_versions keys should be Integer (or String), not "+key.getClass().getName());
            int versionNr;
            if (key instanceof Integer)
                versionNr = (Integer) key;
            else {
                warn("geni_api_versions contains a key of type String with value \""+key+"\". This test will accept keys of type String instead of int, but the API specifies it should be int!");
                versionNr = Integer.parseInt((String) key);
            }
            Object val = versions.get(key);
            assertTrue(val instanceof String, "geni_api_versions values should be String, not "+val.getClass().getName());
            String urlS = (String) val;
            //check URL
            URL url = new URL(urlS);
            String host = url.getHost();
            assertFalse(host.equals("localhost"), "Illegal host in URL: "+url+" (host in URL should not be the non-global \""+host+"\")");
            assertFalse(host.equals("127.0.0.1"), "Illegal host in URL: "+url+" (host in URL should not be the non-global \""+host+"\")");

        }
    }

    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" }, softDepends= { "testGetVersionResultApiVersionsCorrectness", "testGetVersionResultCorrectness" }, groups = {"getversion"})
    public void testGetVersionResultNoDuplicates() throws GeniException {
        //Check if RSpecs are unique
        List<AggregateManager3.VersionInfo.RspecVersion> adRspecVersions = new ArrayList<AggregateManager3.VersionInfo.RspecVersion>();
        for (AggregateManager3.VersionInfo.RspecVersion rspecVer : versionInfo.getAdRspecVersions()) {
            for (AggregateManager3.VersionInfo.RspecVersion other : adRspecVersions)
                assertFalse(other.equalTypeAndVersion(rspecVer), "VersionInfo Result invalid: Duplicate Rspec type/version pair in supported Advertisement RSpec:"+
                        "type="+other.getType()+" version="+other.getVersion()+" VS "+
                        "type="+rspecVer.getType()+" version="+rspecVer.getVersion()+" ");
            adRspecVersions.add(rspecVer);
        }

        List<AggregateManager3.VersionInfo.RspecVersion> reqRspecVersions = new ArrayList<AggregateManager3.VersionInfo.RspecVersion>();
        for (AggregateManager3.VersionInfo.RspecVersion rspecVer : versionInfo.getRequestRspecVersions()) {
            for (AggregateManager3.VersionInfo.RspecVersion other : reqRspecVersions)
                assertFalse(other.equalTypeAndVersion(rspecVer), "VersionInfo Result invalid: Duplicate Rspec type/version pair in supported Request RSpec"+
                        "type="+other.getType()+" version="+other.getVersion()+" VS "+
                        "type="+rspecVer.getType()+" version="+rspecVer.getVersion()+" ");
            reqRspecVersions.add(rspecVer);
        }

        List<AggregateManager3.VersionInfo.CredentialType> credentialTypes = new ArrayList<AggregateManager3.VersionInfo.CredentialType>();
        for (AggregateManager3.VersionInfo.CredentialType credentialType : versionInfo.getCredentialTypes()) {
            for (AggregateManager3.VersionInfo.CredentialType other : credentialTypes)
                assertFalse(other.equals(credentialType), "VersionInfo Result invalid: Duplicate CredentialType type/version pair in supported Request RSpec"+
                        "type="+other.getType()+" version="+other.getVersion()+" VS "+
                        "type="+credentialType.getType()+" version="+credentialType.getVersion()+" ");
            credentialTypes.add(credentialType);
        }
    }





    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" }, softDepends= {"testGetVersionResultNoDuplicates"} )
    public void testListResourcesBadCredential() throws GeniException {
        //Test without credentials. Should fail.
        AggregateManager3.AggregateManagerReply<String> reply = am3.listResources(
                getAM3Connection(),
                new ArrayList<GeniCredential>(),
                "geni",
                "3",
                true,
                true,
                null);

        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        assert ! reply.getGeniResponseCode().isSuccess();
        assert reply.getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_BADARGS);
    }

    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" }, softDepends= {"testGetVersionResultNoDuplicates"} )
    public void testStatusBadSlice() throws GeniException {
        String badSliceUrn = "urn:publicid:IDN+"+testContext.getUserAuthority().getNameForUrn()+"+slice+NonExisting";

        List<String> urns = new ArrayList<String>();
        urns.add(badSliceUrn);

        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply =
                am3.status(getAM3Connection(), urns, getUserCredentialList(), false/*besteffort*/, null/*extraoptions*/);

        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        assertFalse(reply.getGeniResponseCode().isSuccess(), "SliverStatus reply GeniResponse code is SUCCESS (0) when given a non existing slice \"" + badSliceUrn + "\"");
    }

    /*
    *
    * For testing:
    *   - slice0  is a slice that will have no sliver on the AM
    *   - sliceS  is a slice that will have a sliver on the AM
    *
    * */

    /* Create the slices needed in the next tests. Uses SA, but not AM */
    private String slice0UrnStr;
    private ResourceUrn slice0Urn;
    private GeniCredential slice0Credential;

    private String sliceSUrnStr;
    private ResourceUrn sliceSUrn;
    private GeniCredential sliceSCredential;
    @Test(hardDepends = { "testGetVersionXmlRpcCorrectness" }, softDepends= {"testGetVersionResultNoDuplicates"} )
    public void createTestSlices() throws GeniException {
        //create a slice to use in AM tests
        slice0UrnStr = "urn:publicid:IDN+"+testContext.getUserAuthority().getNameForUrn()+"+slice+"+"ns"+System.currentTimeMillis();
        sliceSUrnStr = "urn:publicid:IDN+"+testContext.getUserAuthority().getNameForUrn()+"+slice+"+"S"+System.currentTimeMillis();
        slice0Urn = new ResourceUrn(slice0UrnStr);
        sliceSUrn = new ResourceUrn(sliceSUrnStr);

        SliceAuthority.SliceAuthorityReply<GeniCredential> registerRes;
        registerRes = sa.register(getSAConnection(), userCredential, slice0Urn);
        TestSliceAuthority.testSACorrectnessXmlRpcResult(registerRes.getRawResult());
        assert registerRes.getGeniResponseCode().isSuccess() : "Register call was not successful in creating \""+slice0Urn.getValue()+"\": "+registerRes.getGeniResponseCode()+" output="+registerRes.getOutput();
        slice0Credential = registerRes.getValue();
        assert slice0Credential != null;
        assert slice0Credential.getCredentialXml() != null;
        assert slice0Credential.getCredentialXml().length() > 10 : "credential too short "+ slice0Credential.getCredentialXml();

        registerRes = sa.register(getSAConnection(), userCredential, sliceSUrn);
        TestSliceAuthority.testSACorrectnessXmlRpcResult(registerRes.getRawResult());
        assert registerRes.getGeniResponseCode().isSuccess() : "Register call was not successful in creating \""+sliceSUrn.getValue()+"\": "+registerRes.getGeniResponseCode()+" output="+registerRes.getOutput();
        sliceSCredential = registerRes.getValue();
        assert sliceSCredential != null;
        assert sliceSCredential.getCredentialXml() != null;
        assert sliceSCredential.getCredentialXml().length() > 10 : "credential too short "+ sliceSCredential.getCredentialXml();

        assert slice0UrnStr != null : "slice0UrnStr is null";
        assert slice0UrnStr.startsWith("urn:publicid:IDN+");
        assert slice0Credential != null : "slice0Credential is null";

        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assert sliceSUrnStr.startsWith("urn:publicid:IDN+");
        assert sliceSCredential != null : "sliceSCredential is null";
    }

    @Test(hardDepends = {"createTestSlices"} )
    public void testStatusNoSliverSlice() throws GeniException {
        //then check if SliverStatus reports not found as it should
        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply =
                am3.status(getAM3Connection(), toStringList(slice0UrnStr), toCredentialList(slice0Credential), false/*besteffort*/, null);

        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        assertFalse(reply.getGeniResponseCode().isSuccess(),
                "Status reply GeniResponse code is SUCCESS (0) when given a slice \""+ slice0UrnStr +"\" that has no sliver. (should fail because no sliver found for the slice)");
    }

    public void verifyDescribeReplyNoSliver(String urn, AggregateManager3.AggregateManagerReply<AggregateManager3.ManifestInfo> reply) {
        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        String specsText = "If a slice urn is supplied and there are no slivers in the given slice at this aggregate, " +
                "then geni_rspec shall be a valid manifest RSpec, containing zero (0) node or link elements - that is, specifying no resources. " +
                "geni_slivers may be an empty array, or may be an array of previous slivers that have since been deleted or expired. " +
                "Calling Describe on one or more sliver URNs that are unknown, deleted or expired shall result in an error (e.g. SEARCHFAILED, EXPIRED or ERROR geni_code). ";

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "Describe reply GeniResponse code is not SUCCESS (0) when given a slice \"" + urn + "\" that has" +
                        " no sliver (available=unspecified). (according to the specs:\""+specsText+"\")");

        AggregateManager3.ManifestInfo manifestInfo = reply.getValue();
        assertNotNull(manifestInfo,
                "Describe reply is completely empty when given a slice \"" + urn + "\" that has" +
                        " no sliver (available=unspecified). (according to the specs:\""+specsText+"\")");
        String rspec = manifestInfo.getManifestRspec();

        assertTrue(isValidGeni3ManifestRspec(rspec),
                "Describe reply when given a slice \""+ urn +"\" that has"+
                        " no sliver (available=unspecified) is not an valid RSpec (according to the specs:\""+specsText+"\"): "+rspec);

        assertTrue(isEmptyRspec(rspec),
                "Describe reply when given a slice \""+ urn +"\" that has"+
                        " no sliver (available=unspecified) is not an empty RSpec (according to the specs:\""+specsText+"\"): "+rspec);
    }

    @Test(hardDepends = {"createTestSlices"} )
    public void testDescribeNoSliverSlice() throws GeniException {
        assert slice0UrnStr != null : "slice0UrnStr is null";
        assert slice0UrnStr.startsWith("urn:publicid:IDN+");
        assert slice0Credential != null : "slice0Credential is null";

        //check if SliverStatus reports not found as it should
        AggregateManager3.AggregateManagerReply<AggregateManager3.ManifestInfo> reply =
                am3.describe(getAM3Connection(), toStringList(slice0UrnStr), toCredentialList(slice0Credential), "geni", "3", true/*compressed*/, null);

        verifyDescribeReplyNoSliver(slice0UrnStr, reply);

        //check if SliverStatus reports not found as it should
        reply = am3.describe(getAM3Connection(), toStringList(slice0UrnStr), toCredentialList(slice0Credential), "geni", "3", false/*compressed*/, null);

        verifyDescribeReplyNoSliver(slice0UrnStr, reply);
    }


    private String sliverSUrnStr;
    private ResourceUrn sliverSUrn;
    @Test(hardDepends = {"createTestSlices"} )
    public void testAllocate() throws GeniException, NoSuchAlgorithmException, ParseException {
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";

        //TODO: make sure geni 3 is advertised in GetVersion. If not, use another RSpec request format!
        String requestRspec = getOneNodeRequestRSpec(testContext.getTestedAuthority());
        if (requestRspec == null) skip("Allocate skipped, because no RSpec example known for type=\""+testContext.getTestedAuthority().getType()+"\"");

        AggregateManager3.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> reply =
                am3.allocate(getAM3Connection(), toCredentialList(sliceSCredential), sliceSUrnStr, requestRspec, null/*endTime*/, null);

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS, "Allocate did not succeed");

        AggregateManager3.AllocateAndProvisionInfo allocateAndProvisionInfo = reply.getValue();
        String replyRequestRspec = allocateAndProvisionInfo.getRspec();
        assertNotNull(replyRequestRspec, "Allocate should return a request RSpec"+replyRequestRspec);
        //TODO check if it is a request rspec

        //TODO API question: Allocate returns a request RSpec, why? (also, in the specification, the return struct mentions a manifest rspec)

        List<AggregateManager3.SliverInfo> sliverInfos = allocateAndProvisionInfo.getSliverInfo();
        assertEquals(sliverInfos.size(), 1, "should be exactly 1 sliver allocated");
        AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);
        sliverSUrnStr = sliverInfo.getSliverUrn();
        assertNotNull(sliverSUrnStr);
        assertValidUrn(sliverSUrnStr, "sliver");
        sliverSUrn = new ResourceUrn(sliverSUrnStr);
        assertEquals(sliverInfo.getAllocationStatus(), "geni_allocated", "Allocated sliver should be in geni_allocated state");

        Date expireDate = RFC3339Util.rfc3339StringToDate(sliverInfo.getExpires());
        Date now = new Date();
        if (!now.before(expireDate))
            warn("newly allocated sliver does not expire in the future! now=\"+now+\" expireDate=\""+expireDate+"\"");
//        assertTrue(now.before(expireDate), "allocated sliver does not expire in the future! now="+now+" expireDate="+expireDate);

        System.out.println("Allocated sliver for \"" + sliceSUrnStr + "\" replyRequestRspec=" + replyRequestRspec);
    }

    @Test(hardDepends = {"testAllocate"} )
    public void testProvision() throws GeniException, NoSuchAlgorithmException, ParseException {
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";
        assert sliverSUrnStr != null : "sliverSUrnStr is null";
        assertValidUrn(sliverSUrnStr, "sliver");

        //check GetVersion geni_single_allocation  if it is true, we may only give a slice URN, no sliver URN(s)
        List<String> urns = toStringList(sliverSUrnStr);
        if (versionInfo.isSingleSliceAllocation()) {
            urns = toStringList(sliceSUrnStr);
        }

        //Note: currently Utah Emulab's current AM3 doesn't work without this optional option
        //      TODO we should also test without users to replicate this problem!

        sshKeyHelper = new SSHKeyHelper();
        Vector<String> sshKeys = new Vector<String>();
        sshKeys.add(sshKeyHelper.getSshPublicKeyString());
        UserSpec userSpec = new UserSpec(testContext.getGeniUser().getUserUrn(), sshKeys);
        List<UserSpec> users = new ArrayList<UserSpec>();
        users.add(userSpec);

        //TODO: make sure geni 3 is advertised in GetVersion. If not, use another RSpec manifest format!

        AggregateManager3.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> reply =
                am3.provision(getAM3Connection(), urns, toCredentialList(sliceSCredential),
                        "geni", "3", null/*bestEffort*/, null/*endtime*/, users/*users*/, null /*extraoptions*/);

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS, "Provision did not succeed");

        AggregateManager3.AllocateAndProvisionInfo allocateAndProvisionInfo = reply.getValue();
        String manifestRspec = allocateAndProvisionInfo.getRspec();
        assertNotNull(manifestRspec, "Provision should return a manifest RSpec");
        assertTrue(isValidGeni3ManifestRspec(manifestRspec),
                "Provision did not return a valid RSpec" + manifestRspec);
        assertFalse(isEmptyRspec(manifestRspec),
                "Provision returned and empty RSpec: " + manifestRspec);

        List<AggregateManager3.SliverInfo> sliverInfos = allocateAndProvisionInfo.getSliverInfo();
        assertEquals(sliverInfos.size(), 1, "should be exactly 1 sliver provisioned");
        AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);
        String resSliverSUrnStr = sliverInfo.getSliverUrn();
        assertNotNull(resSliverSUrnStr);
        assertValidUrn(resSliverSUrnStr, "sliver");
        assertEquals(resSliverSUrnStr, sliverSUrnStr, "Provisioned result has different URN than request");

//Note: State 3, geni_provisioned, is the state of the sliver allocation after the aggregate begins to instantiate the sliver.
        assertEquals(sliverInfo.getAllocationStatus(), "geni_provisioned", "according to specification, sliver should immediately be in geni_provisioned state after the Provision call");

        Date expireDate = RFC3339Util.rfc3339StringToDate(sliverInfo.getExpires());
        Date now = new Date();
        assertTrue(now.before(expireDate), "provisioned sliver does not expire in the future! now="+now+" expireDate="+expireDate);

        //TODO check if manifest rspec is valid

        //TODO check if users are in manifest rspec

        System.out.println("Provisioned sliver for \"" + sliceSUrnStr + "\" manifestRspec=" + manifestRspec);
    }

    @Test(hardDepends = {"testProvision"} )
        public void testSliverBecomesProvisioned() throws GeniException, ParseException {
            //Test if the sliver ever becomes provisioned. We wait for maximum 20 minutes.
            assert sliceSUrnStr != null : "sliceSUrnStr is null";
            assertValidUrn(sliceSUrnStr, "slice");
            assert sliceSCredential != null : "sliceSCredential is null";

            long now = System.currentTimeMillis();
            long deadline = now + (20*60*1000);
            while (now < deadline) {
                AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply =
                                am3.status(getAM3Connection(), toStringList(sliceSUrnStr),
                                toCredentialList(sliceSCredential), null/*best effort*/, null);

                validSuccesStatus(reply);

                AggregateManager3.StatusInfo statusInfo = reply.getValue();
                List<AggregateManager3.SliverInfo> sliverInfos = statusInfo.getSliverInfo();
                AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);

//Note: State 3, geni_provisioned, is the state of the sliver allocation after the aggregate begins to instantiate the sliver.
                assertNotEquals(sliverInfo.getAllocationStatus(), "geni_unallocated", "sliver became geni_unallocated while waiting for it to be provisioned.");
                assertNotEquals(sliverInfo.getAllocationStatus(), "geni_allocated", "sliver became geni_allocated while waiting for it to be provisioned.");
                assertEquals(sliverInfo.getAllocationStatus(), "geni_provisioned", "sliver is not in geni_provisioned while waiting for it to be provisioned.");

                //geni_failed is a state which will will see a a failure to become ready
                assertNotEquals(sliverInfo.getOperationalStatus(), "geni_failed", "sliver operational state became \"geni_failed\" while waiting for it to be provisioned. We assume this is a failure.");

                //When waiting for "geni_provisioned" to be completed, the sliver is always in state
                //      geni_pending_allocation
                //   typically, when the sliver is "geni_provisioned" it will become
                //      geni_notready   after which it can be started. (but other states are possible)
                //CONCLUSION: if the sliver is not in "geni_pending_allocation", it is ready
                if (!sliverInfo.getOperationalStatus().equals("geni_pending_allocation"))  {
                    System.out.println("testSliverBecomesReady -> sliver is now fully provisioned: operational_state="+sliverInfo.getOperationalStatus());
                    return;
                }

                System.out.println("testCreatedSliverBecomesReady -> sliver not fully provisioned: operational_state="+sliverInfo.getOperationalStatus()+".  Trying again in 30 seconds...");
                try {
                    Thread.sleep(30000 /*ms*/);
                } catch (InterruptedException e) { /* Ignore*/ }
                now = System.currentTimeMillis();
            }
            throw new RuntimeException("Sliver did not become ready within 20 minutes!");
        }

    @Test(hardDepends = {"testSliverBecomesProvisioned"} )
    public void testPerformOperationalAction() throws GeniException, NoSuchAlgorithmException, ParseException {
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";
        assert sliverSUrnStr != null : "sliverSUrnStr is null";
        assertValidUrn(sliverSUrnStr, "sliver");

        //check GetVersion geni_single_allocation  if it is true, we may only give a slice URN, no sliver URN
        List<String> urns = toStringList(sliverSUrnStr);
        if (versionInfo.isSingleSliceAllocation()) {
            urns = toStringList(sliceSUrnStr);
        }

        String action = "geni_start";

        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> reply =
                am3.performOperationalAction(getAM3Connection(), urns, toCredentialList(sliceSCredential),
                        action,
                        null/*bestEffort*/, null /*extraoptions*/);

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS, "PerformOperationalAction did not succeed");

        List<AggregateManager3.SliverInfo> sliverInfos = reply.getValue();
        assert sliverInfos != null : "invalid reply. class of XmlRpc result (should be Vector): "+reply.getRawResult().get("value").getClass();
        assertEquals(sliverInfos.size(), 1, "should be exactly 1 sliver returned for PerformOperationalAction");
        AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);
        String resSliverSUrnStr = sliverInfo.getSliverUrn();
        assertNotNull(resSliverSUrnStr);
        assertValidUrn(resSliverSUrnStr, "sliver");
        assertEquals(resSliverSUrnStr, sliverSUrnStr, "PerformOperationalAction result has different URN than request");
        sliverSUrn = new ResourceUrn(sliverSUrnStr);
//        assertEquals(sliverInfo.getAllocationStatus(), "geni_allocated", "Provisioned sliver should be in geni_allocated state");

        Date expireDate = RFC3339Util.rfc3339StringToDate(sliverInfo.getExpires());
        Date now = new Date();
        assertTrue(now.before(expireDate), "started sliver does not expire in the future! now="+now+" expireDate="+expireDate);

        System.out.println("PerformOperationalAction for \"" + sliceSUrnStr + "\" action=" + action);
    }

    public void validSuccesStatus(AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply) throws ParseException {
        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "SliverStatus reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceSUrnStr + "\" that has a sliver.");

        assertEquals(reply.getRawResult().get("value").getClass(), Hashtable.class,
                "SliverStatus value should be a Hashtable. Not: "+reply.getRawResult().get("value"));

        AggregateManager3.StatusInfo statusInfo = reply.getValue();
        assertNotNull(statusInfo);

        String resSliceUrn = statusInfo.getSliceUrn();
        assertNotNull(resSliceUrn);
        assertValidUrn(resSliceUrn, "slice");
        assertEquals(resSliceUrn, sliceSUrnStr, "Status slice urn is not correct slice");

        List<AggregateManager3.SliverInfo> sliverInfos = statusInfo.getSliverInfo();
        assertEquals(sliverInfos.size(), 1, "should be exactly 1 sliver status");
        AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);
        String resSliverSUrnStr = sliverInfo.getSliverUrn();
        assertNotNull(resSliverSUrnStr);
        assertValidUrn(resSliverSUrnStr, "sliver");
        assertEquals(resSliverSUrnStr, sliverSUrnStr, "Provisioned result has different URN than request");
        sliverSUrn = new ResourceUrn(sliverSUrnStr);

        //TODO check if allocation states are allowed values (note that RSpec can describe additional allowed states)
        assertNotNull(sliverInfo.getAllocationStatus());

        //TODO check if operational states are allowed values (note that RSpec can describe additional allowed states)
        assertNotNull(sliverInfo.getOperationalStatus());

        Date expireDate = RFC3339Util.rfc3339StringToDate(sliverInfo.getExpires());
        Date now = new Date();
        assertTrue(now.before(expireDate), "sliver does not expire in the future! now="+now+" expireDate="+expireDate);

        assertNotNull(sliverInfo.getExpires());
    }

    @Test(hardDepends = {"testPerformOperationalAction"} )
    public void testStatusExistingSliver() throws GeniException, ParseException {
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";
        assert sliverSUrnStr != null : "sliverSUrnStr is null";
        assertValidUrn(sliverSUrnStr, "sliver");

        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply =
                am3.status(getAM3Connection(), toStringList(sliceSUrnStr),
                toCredentialList(sliceSCredential), null/*best effort*/, null);

        validSuccesStatus(reply);
    }

    public void checkDescribeExistingSliver(AggregateManager3.AggregateManagerReply<AggregateManager3.ManifestInfo> reply, String sliceUrn, String sliverUrn) {
        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "Describe reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceSUrnStr + "\" that has a sliver.");

        AggregateManager3.ManifestInfo manifestInfo = reply.getValue();
        assertNotNull(manifestInfo,
                "Describe reply is completely empty when given a provisioned sliver \"" + sliverUrn + "\"");

        assertEquals(manifestInfo.getSliceUrn(), sliceUrn, "Describe reply is for another slice URN than requested");

        String rspec = manifestInfo.getManifestRspec();
        assertTrue(isValidGeni3ManifestRspec(rspec),
                "Describe reply rspec when given a provisioned sliver \""+ sliverUrn +"\" is not an valid RSpec");

        List<AggregateManager3.SliverInfo> sliverInfos = manifestInfo.getSliverInfos();
        assertEquals(sliverInfos.size(), 1, "should be exactly 1 sliver status");
        AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);
        String resSliverSUrnStr = sliverInfo.getSliverUrn();
        assertNotNull(resSliverSUrnStr);
        assertValidUrn(resSliverSUrnStr, "sliver");
        assertEquals(resSliverSUrnStr, sliverSUrnStr, "Describe reply sliver info has different sliver URN than request");
        sliverSUrn = new ResourceUrn(sliverSUrnStr);
        //TODO check if allocation states are allowed values (note that RSpec can describe additional allowed states)
        assertNotNull(sliverInfo.getAllocationStatus());
        //TODO check if operational states are allowed values (note that RSpec can describe additional allowed states)
        assertNotNull(sliverInfo.getOperationalStatus());
        //TODO check if expires is correct date according to RFC
        assertNotNull(sliverInfo.getExpires());
    }

    @Test(hardDepends = {"testPerformOperationalAction"} )
    public void testDescribeExistingSliver() throws GeniException {
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";
        assert sliverSUrnStr != null : "sliverSUrnStr is null";
        assertValidUrn(sliverSUrnStr, "sliver");

        AggregateManager3.AggregateManagerReply<AggregateManager3.ManifestInfo> reply =
                am3.describe(getAM3Connection(), toStringList(sliverSUrnStr), toCredentialList(sliceSCredential),
                        "geni", "3", true/*compressed*/, null);

        checkDescribeExistingSliver(reply, sliceSUrnStr, sliverSUrnStr);
    }

    @Test(hardDepends = { "testPerformOperationalAction" }, softDepends = { "testStatusExistingSliver", "testDescribeExistingSliver" }, groups = {"createsliver"} )
    public void testSliverBecomesStarted() throws GeniException, ParseException {
        //Test if the sliver ever becomes ready. We wait for maximum 20 minutes.
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";

        long now = System.currentTimeMillis();
        long deadline = now + (20*60*1000);
        while (now < deadline) {
            AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply =
                            am3.status(getAM3Connection(), toStringList(sliceSUrnStr),
                            toCredentialList(sliceSCredential), null/*best effort*/, null);

            validSuccesStatus(reply);

            AggregateManager3.StatusInfo statusInfo = reply.getValue();
            List<AggregateManager3.SliverInfo> sliverInfos = statusInfo.getSliverInfo();
            AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);

            assertNotEquals(sliverInfo.getAllocationStatus(), "geni_unallocated", "sliver became geni_unallocated while waiting for it to start.");
            assertNotEquals(sliverInfo.getAllocationStatus(), "geni_allocated", "sliver became geni_allocated while waiting for it to start.");
            assertEquals(sliverInfo.getAllocationStatus(), "geni_provisioned", "sliver is not in geni_provisioned while waiting for it to start.");

            //geni_failed is a state which will will see a a failure to become ready
            assertNotEquals(sliverInfo.getOperationalStatus(), "geni_failed", "sliver operational state became \"geni_failed\" while waiting for it to start.");

            //Note: this is not generic: not all AM's will support this state
            if (sliverInfo.getOperationalStatus().equals("geni_ready"))  {
                System.out.println("testSliverBecomesReady -> sliver ready: "+sliverInfo.getOperationalStatus());
                return;
            }

            System.out.println("testCreatedSliverBecomesReady -> sliver not ready: "+sliverInfo.getOperationalStatus()+".  Trying again in 30 seconds...");
            try {
                Thread.sleep(30000 /*ms*/);
            } catch (InterruptedException e) { /* Ignore*/ }
            now = System.currentTimeMillis();
        }
        throw new RuntimeException("Sliver did not become ready within 20 minutes!");
    }

    @Test(hardDepends = {"testSliverBecomesStarted"}, groups = {"nodelogin"} )
    public void testNodeLogin() throws GeniException, IOException {
        if (true)
            skip("Not yet fully reimplemented");
        if (sshHostname == null)
            skip("No node / service / login in manifest RSpec, so SSH login cannot be tested.");

        //Test node login using SSH private key

        Connection conn = new Connection(sshHostname, sshPort);
        conn.connect();
        String privateKeyToPrint = new String(sshKeyHelper.getPEMPrivateKey());
        if (privateKeyToPrint.length() > 50) privateKeyToPrint = privateKeyToPrint.substring(0, 50);
        note("Trying to log in with PEM private key:\n" + privateKeyToPrint);
        boolean isAuthenticated = conn.authenticateWithPublicKey(sshUsername, sshKeyHelper.getPEMRsaPrivateKey(), "nopass");
        assertTrue(isAuthenticated, "Could not login to host. "+sshHostname+"@"+sshHostname+":"+sshPort);

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

         note("\"ls /\" command on "+sshHostname+"@"+sshHostname+":"+sshPort+" result: \""+result.trim()+"\". (stderr is: \"" + err + "\")");
        assertTrue(result.length() > 5, "I executed \"ls /\" on the remote host, and expected some reply. Instead I got: \"" + result.trim() + "\". (stderr is: \"" + err + "\")");

        session.close();

        conn.close();
    }

    @Test(hardDepends = {"testSliverBecomesStarted"}, softDepends = {"testNodeLogin"} )
    public void testRenewSliver() throws GeniException, ParseException {
        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";



        //Get current expiration time
        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> replyStatus =
                am3.status(getAM3Connection(), toStringList(sliceSUrnStr),
                toCredentialList(sliceSCredential), null/*best effort*/, null);
        validSuccesStatus(replyStatus);
        assertEquals(replyStatus.getValue().getSliceUrn(), sliceSUrnStr);
        assertEquals(replyStatus.getValue().getSliverInfo().size(), 1);
        String startExpirationTimeString = replyStatus.getValue().getSliverInfo().get(0).getExpires();
        Date startExpirationTime = RFC3339Util.rfc3339StringToDate(startExpirationTimeString);



        //Test Renew Sliver. Renew so it expires 5 minutes later then before
        Date expirationTime = new Date(startExpirationTime.getTime() + (5 * 60 * 1000));
//        Date expirationTime = new Date(System.currentTimeMillis() + (5 * 60 * 1000));
        String expirationTimeString = RFC3339Util.dateToRFC3339String(expirationTime);
        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> reply = am3.renew(getAM3Connection(), toStringList(sliceSUrnStr), toCredentialList(sliceSCredential), expirationTimeString, null/*bestEffort*/, null);

        testAM3CorrectnessXmlRpcResult(reply.getRawResult());

        assertEquals(reply.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "RenewSliver reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceSUrnStr + "\" that has a sliver.");


        validSuccesStatus(replyStatus);
        assertEquals(replyStatus.getValue().getSliceUrn(), sliceSUrnStr);
        assertEquals(replyStatus.getValue().getSliverInfo().size(), 1);
        assertEquals(replyStatus.getValue().getSliverInfo().get(0).getExpires(), expirationTimeString, "Sliver expiration time has not been set to the data asked");


        //Get status and check if expiration time is indeed set
        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> replyStatus2 =
                am3.status(getAM3Connection(), toStringList(sliceSUrnStr),
                        toCredentialList(sliceSCredential), null/*best effort*/, null);
        validSuccesStatus(replyStatus2);
        assertEquals(replyStatus2.getValue().getSliceUrn(), sliceSUrnStr);
        assertEquals(replyStatus2.getValue().getSliverInfo().size(), 1);
        assertEquals(replyStatus2.getValue().getSliverInfo().get(0).getExpires(), expirationTimeString, "Sliver expiration time has not been set to the data asked");
    }

    /* Test must run after tests that use sliver. Will always run, to cleanup any created sliver. */
    @Test(softDepends = {"testNodeLogin", "testRenewSliver", "testSliverBecomesStarted"}, groups = {"createsliver","nodelogin"} )
    public void testDeleteSliver() throws GeniException {
        if (sliceSUrnStr == null) skip("DEBUG: Skipped because other test created nothing to delete");

        assert sliceSUrnStr != null : "sliceSUrnStr is null";
        assertValidUrn(sliceSUrnStr, "slice");
        assert sliceSCredential != null : "sliceSCredential is null";

        System.out.println("DeleteSliver for slice " + sliceSUrnStr);

//        //Test Delete Sliver by sliver URN
//        assert sliverSUrnStr != null : "sliverSUrnStr is null";
//        assertValidUrn(sliverSUrnStr, "sliver");
//        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> reply1 =
//                am3.delete(getAM3Connection(), toStringList(sliverSUrnStr), toCredentialList(sliceSCredential), null/*best effort*/, null);

        //Test Delete Sliver by slice URN
        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> reply2 =
                am3.delete(getAM3Connection(), toStringList(sliceSUrnStr), toCredentialList(sliceSCredential), null/*best effort*/, null);

//        testAM3CorrectnessXmlRpcResult(reply1.getRawResult());
        testAM3CorrectnessXmlRpcResult(reply2.getRawResult());

        assertEquals(reply2.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS,
                "DeleteSliver reply GeniResponse code is not SUCCESS (0) when given a slice \"" + sliceSUrnStr + "\" that has a sliver.");

        List<AggregateManager3.SliverInfo> sliverInfos = reply2.getValue();
        assertEquals(sliverInfos.size(), 1, "should be exactly 1 sliver status");
        AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);
        String resSliverSUrnStr = sliverInfo.getSliverUrn();
        assertNotNull(resSliverSUrnStr);
        assertValidUrn(resSliverSUrnStr, "sliver");
        assertEquals(resSliverSUrnStr, sliverSUrnStr, "Delete reply sliver info has different sliver URN than request");
        sliverSUrn = new ResourceUrn(sliverSUrnStr);
        //TODO check if allocation state is as expected after delete
        assertNotNull(sliverInfo.getAllocationStatus());
        //TODO check if operational state is as expected after delete
        assertNotNull(sliverInfo.getOperationalStatus());


        //test if DeleteSliver worked

        //validSuccesStatus(replyStatus);

        // Calling Status() on an unknown, deleted or expired sliver (by explicit URN) shall result in an error (e.g.
        // SEARCHFAILED, EXPIRED or ERROR) (unless geni_best_effort is true, in which case the method may succeed, but
        // return a geni_error for each sliver that failed).

        //TODO assert that status sliver URN fails


        // Attempting to get Status() for a slice (no slivers identified) with no current slivers at this aggregate may
        // return an empty list for geni_slivers, may return a list of previous slivers that have since been deleted,
        // or may even return an error (e.g. SEARCHFAILED or EXPIRED). Note therefore that geni_slivers may be an empty
        // list.
        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> replyStatus =
                am3.status(getAM3Connection(), toStringList(sliceSUrnStr),
                        toCredentialList(sliceSCredential), null/*best effort*/, null);
        testAM3CorrectnessXmlRpcResult(replyStatus.getRawResult());

        if (replyStatus.getGeniResponseCode().isSuccess()) {
            //list empty or with deleted slivers (geni_unallocated)

            assertEquals(replyStatus.getGeniResponseCode(), GeniAMResponseCode.GENIRESPONSE_SUCCESS);
            assertEquals(replyStatus.getRawResult().get("value").getClass(), Hashtable.class,
                    "SliverStatus value should be a Hashtable. Not: "+replyStatus.getRawResult().get("value"));
            AggregateManager3.StatusInfo statusInfo = replyStatus.getValue();
            assertNotNull(statusInfo);

            String resSliceUrn = statusInfo.getSliceUrn();
            assertNotNull(resSliceUrn);
            assertValidUrn(resSliceUrn, "slice");
            assertEquals(resSliceUrn, sliceSUrnStr, "Status slice urn is not correct slice");

            List<AggregateManager3.SliverInfo> sliverInfosStatus = statusInfo.getSliverInfo();

            if (sliverInfosStatus.isEmpty()) {
                //Ok, it has been deleted
                return;
            }

            //all slivers in this slice should be unallocated
            for (AggregateManager3.SliverInfo si : sliverInfosStatus) {
                String siSliverUrnStr = si.getSliverUrn();
                assertNotNull(siSliverUrnStr);
                assertValidUrn(siSliverUrnStr, "sliver");
                assertEquals(si.getAllocationStatus(), "geni_unallocated");
            }
            //Ok, it has been deleted
            return;
        } else {
            //not success
            //Ok, it has been deleted
            return;
        }

    }
    
}
