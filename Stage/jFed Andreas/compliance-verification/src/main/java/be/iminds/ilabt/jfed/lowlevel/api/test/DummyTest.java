package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.GeniConnection;
import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;

import java.util.Hashtable;

/**
 * SimpleTestSliceAuthority: One simple test of "ProtoGeni Slice Authority API v1"
 *
 * This is little more than a connectivity test and one basic test of XmlRpc and GetVersion.
 *
 * Use TestSliceAuthority for a lot more tests of the "ProtoGeni Slice Authority API v1".
 */
public class DummyTest extends ApiTest {
    public String getTestDescription() {
        return "A dummy test generating some fake results. This is useful to test the testing functionality. It also does 1 GetVersion test.";
    }

    public void setUp(CommandExecutionContext testContext) {
        note("Note in Setup");
        sa = new SliceAuthority(testContext.getLogger());
    }

    @Test(description = "Test A. Should be successful.", softDepends={"testGetVersion"})
    public void testA() throws GeniException {
        note("testA started");
        assertTrue(true);
    }

    @Test(description = "Test B, should be a warning", hardDepends={"testA"})
    public void testB() {
        assertTrue(true);
        warn("warning in testB");
    }

    @Test(description = "Test C, should be error", hardDepends={"testB", "testA"})
    public void testC() throws GeniException {
        note("testC started");
        assertTrue(true);
        errorNonFatal("error in testC");
        warn("warning in testC");
        note("test continues after error");
    }

    @Test(description = "Test D, should be error due to exception", hardDepends={"testA"}, softDepends={"testB", "testC"})
    public void testD() {
        note("testD started");
        assertTrue(true);
        if (true)
            throw new RuntimeException("Faked error in testD");
        warn("warning in testD");
        note("note and warning never reached");
    }

    @Test(description = "Test E, should be skipped because test D failed", hardDepends={"testD"})
    public void testE() throws GeniException {
        note("testE should never be started");
        assertTrue(false);
        warn("warning in testE");
        note("note and warning never reached");
    }

    @Test(description = "Test F, should be skipped because it wants to be skipped itselfd", softDepends={"testE"})
    public void testF() {
        note("testF started");
        skip("testF requests to be skipped for this DummyTest");
        errorNonFatal("error in testF");
        note("never reached");
        assertTrue(false);
    }

    private SliceAuthority sa;

    public GeniConnection getConnection() throws GeniException {
        assert getTestContext() != null;
        assert getTestContext().getConnectionProvider() != null;
        assert getTestContext().getGeniUser() != null;
        assert getTestContext().getTestedAuthority() != null;
        return getTestContext().getConnectionProvider().getConnectionByAuthority(getTestContext().getGeniUser(), getTestContext().getTestedAuthority(), new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }

    /**
     * Check for correctness of a SA XmlRpc result. Should be tested for each reply received.
     * */
    public static void testSACorrectnessXmlRpcResult(Hashtable res) {
        Object code = res.get("code");
        Object value = res.get("value");
        Object output = res.get("output");
        assertNotNull(code);
        assertNotNull(value);
        assertNotNull(output);
        assertInstanceOf(code, Integer.class);
        assertInstanceOf(output, String.class);
    }

    @Test
    public void testGetVersion() throws GeniException {
//        System.out.println("GetVersion test starting");
        SliceAuthority.SliceAuthorityReply<String> reply = sa.getVersion(getConnection());
        testSACorrectnessXmlRpcResult(reply.getRawResult());
        assertTrue(reply.getGeniResponseCode().isSuccess());
//        System.out.println("GetVersion test finished");
    }
}
