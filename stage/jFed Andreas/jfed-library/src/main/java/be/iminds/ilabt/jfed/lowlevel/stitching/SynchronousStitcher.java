package be.iminds.ilabt.jfed.lowlevel.stitching;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AbstractGeniAggregateManager;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;

import java.net.URISyntaxException;
import java.util.*;

/**
 * SynchronousStitcher does synchonouse calls needed to make the stiched topology ready.
 * Calls are not executed in parallel, and there is no error recovery.
 *
 * There is also no "Status" call functionality (yet)
 *
 * This is mostly useful as an example to make a Stitcher that does parallel calls with failure handling and Status calls.
 */
public class SynchronousStitcher {
    private final Logger logger;
    private final StitchingDirector director;
    private ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback = null;

    /**
     *
     * */
    public SynchronousStitcher(Logger logger, StitchingDirector director) {
        this.logger = logger;
        this.director = director;
    }

    public void setInsecure() {
        handleUntrustedCallback = new ClientSslAuthenticationXmlRpcTransportFactory.INSECURE_TRUSTALL_HandleUntrustedCallback();
    }

    public void setHandleUntrustedCallback(ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback) {
        this.handleUntrustedCallback = handleUntrustedCallback;
    }


    public void stitchCreate(List<GeniCredential> credentialList, String sliceUrn, GeniUser user) {
        String stichSshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAAAgQDXtL+xSHe2HF4MDGjSFb1rTi9VtYTXEzVQWzNTxeUu96zGqB3O++PQzPoqgPdwqry77WQ7b568bdc+8nXxgZWbRYjzZHd09rnoqZa3MJxzNYSI2aoy5hpFVW5yVL6SjBeclPHUMnZeFoyxNjHIpj3etZVY+EzCsl3BqPkV4ZXA+w==";
        List<UserSpec> users = new ArrayList<UserSpec>();
        UserSpec userSpec = new UserSpec(user.getUserUrn(), stichSshKey);
        users.add(userSpec);

        stitchCreate(credentialList, sliceUrn, user, users);
    }

    /**
     * Assumes the StitchingDirector has ready hops. (= it has received the computation result)
     *
     * does all CreateSliver calls needed to create the stitched topology
     * */
    public void stitchCreate(List<GeniCredential> credentialList, String sliceUrn, GeniUser user, List<UserSpec> users) {
        System.out.println("Calling createSliver on all aggregates");

        List<StitchingDirector.ReadyHopDetails> readyHops = director.getReadyHops();
        while (!readyHops.isEmpty()) {
            for (StitchingDirector.ReadyHopDetails hop : readyHops) {
                SfaAuthority auth = hop.getAuthority();
                String requestRspec = hop.getRequestRspec();

                System.out.println("\n\n***************************************************************************************");
                System.out.println("   CreateSliver call for " + auth.getUrn());

                //do createsliver call
                AggregateManager2 am2 = new AggregateManager2(logger, true);
                String manifestRspec = null;
                try {
                    //TODO handle errors (adding retry etc)
                    String url = auth.getUrl(ServerType.GeniServerRole.AM, 2).toURI().toString();
                    GeniConnection con = null;
                    if (url.startsWith("http://")) {
                        System.err.println("WARNING: Connection URL is http instead of https! "+
                                "This is unsecure, so this connection protocol will never used. "+
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

                    System.out.println("Calling CreateSliver...");
                    AbstractGeniAggregateManager.AggregateManagerReply<String> reply = am2.createSliver(
                            con, credentialList, sliceUrn, requestRspec, users,
                            null);
                    System.out.println("Call returned "+reply != null ? reply.getGeniResponseCode() : "null");
                    assert reply.getGeniResponseCode().isSuccess() : "Call not successfull: "+reply.getGeniResponseCode();
                    manifestRspec = reply.getValue();
                } catch (GeniException e) {
                    e.printStackTrace();
                    System.err.println("ABORTING: GeniException in CreateSliver call: " + e.getMessage());
                    return;
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    System.err.println("ABORTING: URISyntaxException in CreateSliver call: " + e.getMessage());
                    return;
                }
                assert manifestRspec != null;

                director.processManifest(hop, manifestRspec);
            }

            readyHops = director.getReadyHops();
        }

        System.out.println("Calls for stitching done");
    }

    public void deleteAll(List<GeniCredential> credentialList, String sliceUrn, GeniUser user) {
        AggregateManager2 am2 = new AggregateManager2(logger, true);

        for (SfaAuthority authorityInfo: director.getInvolvedAuthorities()) {
            //do delete call
            try {
                System.out.println("Calling delete on "+authorityInfo.getUrn());

                //TODO handle errors
                String url = authorityInfo.getUrl(ServerType.GeniServerRole.AM, 2).toURI().toString();
                GeniConnection con = null;
                if (url.startsWith("http://")) {
                    System.err.println("WARNING: Connection URL is http instead of https! This is unsecure, so connection will not be tried. I will try using https instead, maybe that works.");
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
                        am2.deleteSliver(con, credentialList, sliceUrn, null);

                System.out.println("Called delete on " + authorityInfo.getUrn() + " results: " + reply.getValue());
            } catch (GeniException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }
}
