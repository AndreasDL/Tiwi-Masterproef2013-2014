package example;

import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.UserLoginModel;
import be.iminds.ilabt.jfed.util.DialogUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ClientApiExample {
    public static void main(String[] args) throws Exception {
        try {
            if (args.length != 1) {
                System.err.println("Note: currently example is fixed to iminds wall3 testbed only");
                System.err.println("Usage: ClientApiExample <username>");
                System.exit(-1);
            }

            String username = args[0];

            String url_ch = "https://www.emulab.net/protogeni/xmlrpc/ch";
            String url_sa = "https://www.wall3.test.ibbt.be/protogeni/xmlrpc/sa";
            String url_cm = "https://www.wall3.test.ibbt.be/protogeni/xmlrpc/cm";
            String url_am = "https://www.wall3.test.ibbt.be/protogeni/xmlrpc/am";
            String url_am2 = "https://www.wall3.test.ibbt.be/protogeni/xmlrpc/am/2.0";
            GeniAuthority wall3 = null;
            try {
                Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
                urls.put(new ServerType(ServerType.GeniServerRole.SA, 1), new URL(url_sa));
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 1), new URL(url_am));
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL(url_am2));
                wall3 = new GeniAuthority("urn:publicid:IDN+wall3.test.ibbt.be+authority+cm", "wall3.test.ibbt.be",
                        urls, null/*gid*/, "emulab");
            } catch (MalformedURLException e) {
                throw new RuntimeException("TODO: MalformedURLException unhandled", e);
            }
            String pemKeyAndCert = UserLoginModel.loadPemFile(new File(System.getProperty("user.home") + File.separator + ".ssl" + File.separator + "geni_cert.pem"));
            final GeniUser geniContext = new SimpleGeniUser(wall3, username, pemKeyAndCert, DialogUtils.getPassword("Password for " + wall3.getName() + " user " + username));

            GeniConnectionProvider connectionProvider = new GeniConnectionPool();
            GeniAuthorityProvider authorityProvider = new GeniAuthorityProvider() {
                @Override
                public GeniAuthority getGeniAuthority() { return geniContext.getUserAuthority(); }
            };
            GeniUserProvider contextProvider = new GeniUserProvider() {
                @Override
                public GeniUser getLoggedInGeniUser() {
                    return geniContext;
                }
            };
            Logger logger = new Logger();
            CredentialAndUrnHistory history = new CredentialAndUrnHistory(logger);
            EasySliceAuthority sliceAuthority = new EasySliceAuthority(null, contextProvider, connectionProvider, authorityProvider, history, logger);

            boolean testSlices = DialogUtils.getYesNoAnswer("Test Slice methods? (Creation, Status, etc.)");

            System.out.println("Testing SliceAuthority: ");
            System.out.println("***************************************************************************************\n");

            String version = sliceAuthority.getVersion();
            System.out.println("GetVersion result: " + version);
            System.out.println("***************************************************************************************\n");

            GeniCredential cred = sliceAuthority.getCredential();
            System.out.println("Credential for \""+username+"\": " + cred.getCredentialXml());
            System.out.println("***************************************************************************************\n");

            if (testSlices) {
                UserInfo userinfo = sliceAuthority.ResolveUser();
                System.out.println("UserInfo for \""+username+"\": " + userinfo.toString());
                System.out.println("***************************************************************************************\n");

                String sliceName = "ClientJavaApiExampleSlice"+Math.random();
                Slice slice = sliceAuthority.register(sliceName);
                System.out.println("Registerend slice \""+sliceName+"\": urn=" + slice.getUrn());
                System.out.println("***************************************************************************************\n");

                sliceAuthority.resolveSlice(slice);
                System.out.println("Resolved slice \""+slice.getName()+"\": " + slice);
                System.out.println("***************************************************************************************\n");

                //boolean succes = sliceAuthority.bindToSlice(wall3, "otheruser",  slice);

                GeniCredential origSliceCred = slice.getCredential();
                GeniCredential newSliceCred = sliceAuthority.getCredential(slice);
                boolean same = origSliceCred.getCredentialXml().equals(newSliceCred.getCredentialXml());
                System.out.println("GetCredential on slice \""+slice.getName()+"\". Same credential: " + same);
                System.out.println("***************************************************************************************\n");
                if (!same)
                    throw new RuntimeException("Slice credential returned differs from original");
            }

            System.out.println("***************************************************************************************");
            System.out.println("************************ Aggregate Manager v2 Test ************************************");
            System.out.println("***************************************************************************************");
            URL am2url = geniContext.getUserAuthority().getUrl(ServerType.GeniServerRole.AM, 2);
            System.out.println("Testing AggregateManager2 @ " + am2url +"\n");

            AggregateManager2 rawAggregateManager2 = new AggregateManager2(new Logger());
            GeniConnection am2Con = connectionProvider.getConnection(contextProvider.getLoggedInGeniUser(), authorityProvider.getGeniAuthority(), new ServerType(ServerType.GeniServerRole.AM, 2));;

            AggregateManager2.VersionInfo am2RawVersion = rawAggregateManager2.getVersion(am2Con).getValue();
            System.out.println("Raw GetVersion result: " + am2RawVersion);
            System.out.println("***************************************************************************************\n");

            EasyAggregateManager2 aggregateManager2 = new EasyAggregateManager2(contextProvider, connectionProvider, authorityProvider, null, logger);

            int am2Version = aggregateManager2.getVersion();
            System.out.println("Easy GetVersion result: " + am2Version);
            System.out.println("***************************************************************************************\n");

            Rspec adRspec = aggregateManager2.listResources(false);
            System.out.println("Easy ListResources result: " + adRspec);
            System.out.println("***************************************************************************************\n");



//        String keys = sliceAuthority.getKeys(cred);
//        System.out.println("GetKeys result:" + keys);
//        System.out.println("***************************************************************************************\n");
        } catch (GeniException e) {
            System.err.println("GeniException: "+e);
            e.printStackTrace();
            if (e.getXmlRpcResult() != null) {
                System.err.println("HTTP request:\n"+e.getXmlRpcResult().getRequestHttpContent());
                System.err.println("\nHTTP response:\n"+e.getXmlRpcResult().getResultHttpContent());
                System.err.println("\nXMLRPC request:\n"+e.getXmlRpcResult().getRequestXmlRpcString());
                System.err.println("\nXMLRPC response:\n"+e.getXmlRpcResult().getResultXmlRpcString());
            }
        }
    }
}
