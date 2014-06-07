package be.iminds.ilabt.jfed.lowlevel.api.test;

import be.iminds.ilabt.jfed.lowlevel.GeniConnection;
import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.testing.base.ApiTest;
import be.iminds.ilabt.jfed.util.CommandExecutionContext;

/**
 * SimpleTestSliceAuthority: One simple test of "ProtoGeni Slice Authority API v1"
 *
 * This is little more than a connectivity test and one basic test of XmlRpc and GetVersion.
 *
 * Use TestSliceAuthority for a lot more tests of the "ProtoGeni Slice Authority API v1".
 */
public class ConnectivityTestSliceAuthority extends ApiTest {
    public String getTestDescription() {
        return "One simple \"ProtoGeni Slice Authority API v1\" Test (XmlRpc and GetVersion). This is mainly useful to test connectivity to the tested SA.";
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

    @Test
    public void testGetVersion() throws GeniException {
        System.out.println("GetVersion test starting");
        SliceAuthority.SliceAuthorityReply<String> reply = sa.getVersion(getConnection());
        assertTrue(reply.getGeniResponseCode().isSuccess());
        System.out.println("GetVersion test finished");
    }

}
