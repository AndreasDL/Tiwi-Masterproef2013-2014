package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;

import java.util.Hashtable;

/**
 * TestSliceAuthority
 */
public class TestSliceAuthority extends ApiTest {
    public String getTestDescription() {
        return "Slice Authority Tests. Currently only GetVersion is tested, this needs to be extended.";
    }

    private SliceAuthority sa;
    private CommandExecutionContext testContext;

    public GeniConnection getConnection() throws GeniException {
        return testContext.getConnectionProvider().getConnectionByAuthority(testContext.getGeniUser(), testContext.getTestedAuthority(), new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }

    public void setUp(CommandExecutionContext testContext) {
        this.testContext = testContext;
        sa = new SliceAuthority(testContext.getLogger());
    }

    /**
     * Check for correctness of a SA XmlRpc result. Should be tested for each reply received.
     * */
    public static void testSACorrectnessXmlRpcResult(Hashtable res) {
        Object code = res.get("code");
        Object value = res.get("value");
        Object output = res.get("output");
        assert(code != null);
        assert(value != null);
        assert(output != null);
        assert(code instanceof Integer);
        assert(output instanceof String);
    }

    @Test
    public void testGetVersion() throws GeniException {
        System.out.println("GetVersion test starting");
        SliceAuthority.SliceAuthorityReply<String> reply = sa.getVersion(getConnection());
        assert (reply.getGeniResponseCode().isSuccess());
        System.out.println("GetVersion test finished");
    }

}
