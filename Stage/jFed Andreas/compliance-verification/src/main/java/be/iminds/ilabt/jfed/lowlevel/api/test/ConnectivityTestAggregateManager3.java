package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;

import java.util.Hashtable;

/**
 * SimpleTestAggregateManager3. One simple test of "Geni Aggregate Manager API v3"
 *
 * This is little more than a connectivity test and one basic test of XmlRpc and GetVersion.
 *
 * Use TestAggregateManager3 for a lot more tests of the "Geni Aggregate Manager API v3". (And also for more thorough tests of GetVersion)
 */
public class ConnectivityTestAggregateManager3 extends ApiTest {
    public String getTestDescription() {
        return "One simple \"Geni Aggregate Manager API v3\" Test (XmlRpc and GetVersion). This is mainly useful to test connectivity to the tested AM.";
    }

    private AggregateManager3 am3;
    private CommandExecutionContext testContext;

    public GeniConnection getAM3Connection() throws GeniException {
        return testContext.getConnectionProvider().getConnectionByAuthority(testContext.getGeniUser(), testContext.getTestedAuthority(), new ServerType(ServerType.GeniServerRole.AM, 3));
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
            //this is a connectivity test, we don't warn here, we just make a note of it.
            if (output == null)
                note("testAM2CorrectnessXmlRpcResult: while geni_code is success ("+genicodevalue+"), output == null. This is allowed but not recommended by jFed.");
            if (!(output instanceof String))
                note("testAM2CorrectnessXmlRpcResult: while geni_code is success ("+genicodevalue+"), output is not String (it is "+output.getClass().getName()+" with value \""+output.toString()+"\"). This is allowed but not recommended by jFed.");
        }
    }

    public void setUp(CommandExecutionContext testContext) {
        this.testContext = testContext;

        am3 = new AggregateManager3(testContext.getLogger());
    }


    private Hashtable versionRawResult = null;
    private AggregateManager3.VersionInfo versionInfo = null;
    @Test
    public void testGetVersionXmlRpcCorrectness() throws GeniException {
        AggregateManager3.AggregateManagerReply<AggregateManager3.VersionInfo> reply = am3.getVersion(getAM3Connection());

        testAM2CorrectnessXmlRpcResult(reply.getRawResult());

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
}
