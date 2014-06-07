package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.KeyUtil;

import java.security.cert.X509Certificate;
import java.util.*;

/**
 * <p>This is a simple wrapper for the methods specified in the {@link <a href="http://bestsurfing1.appspot.com/svn.planet-lab.org/wiki/SfaRegistryInterface">SFA Registry Interface</a>}.
 * <p>Some functionality provided by the API is available in this implementation, and not much processing of results is done.
 * This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link be.iminds.ilabt.jfed.log.Logger} if it is non null.
 *
 * <p>All methods require a {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} argument and return a {@link PlanetlabSfaRegistryInterface.SimpleApiCallReply}
 * which wraps the actual return value of the call, if any.
 *
 * @see <a href="http://bestsurfing1.appspot.com/svn.planet-lab.org/wiki/SfaRegistryInterface">SFA Registry Interface</a>
 * @author Wim Van de Meerssche
 */
public class PlanetlabSfaRegistryInterface extends AbstractApi {
    /**
     * A human readable name for the implemented API
     *
     * @return "PlanetLab SFA Registry Interface"
     */
    static public String getApiName() {
        return "PlanetLab SFA Registry Interface";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "PlanetLab SFA Registry Interface"
     */
    @Override
    public String getName() {
        return getApiName();
    }

    /**
     * Construct a new PlanterlabSfaRegistryInterface, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link be.iminds.ilabt.jfed.log.Logger} if it is non null.
     *
     * @param logger the logger to use. May be null if no logger is used.
     * @param autoRetryBusy whether or not to retry when a "busy" reply is received.
     */
    public PlanetlabSfaRegistryInterface(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1));
    }
    /**
     * Construct a new PlanetlabSfaRegistryInterface, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link be.iminds.ilabt.jfed.log.Logger} if it is non null.
     *
     * <p>This constructor sets autoRetryBusy to true, so "busy" replies are retried.
     *
     * @param logger the logger to use. May be null if no logger is used.
     */
    public PlanetlabSfaRegistryInterface(Logger logger) {
        super(logger, true, new ServerType(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1));
    }

    @Override
    protected boolean isBusyReply(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res) {
        //API has no special reply to indicate "busy"
        return false;
    }

    public static class SimpleApiCallReply<T> implements ApiCallReply<T> {
        private T val;
        private Object rawResult;

        public Object getRawResult() {
            return rawResult;
        }

        public T getValue() {
            return val;
        }

        public SimpleApiCallReply(XMLRPCCallDetails res) {
            rawResult = res.getResult();
            try {
                this.val = (T) res.getResultValueObject();
            } catch (Exception e) {
                this.val = null;
            }
        }
        public SimpleApiCallReply(XMLRPCCallDetails res, T val) {
            this.rawResult = res.getResult();
            this.val = val;
        }



        //not used for this API
        @Override
        public GeniAMResponseCode getGeniResponseCode() {
            return GeniAMResponseCode.GENIRESPONSE_SUCCESS;

        }
        @Override
        public String getOutput() {
            return "";
        }
    }

    class GetVersionReply {
        private int geniApi;
        private int sfa;
        private String codeUrl;
        private String codeTag;
        private String hrn;
        private String urn;
        private Map<String,String> peers;

        public GetVersionReply(XMLRPCCallDetails reply) {
            Hashtable replyTable = (Hashtable) reply.getResult();
            geniApi = (Integer) replyTable.get("geni_api");
            sfa = (Integer) replyTable.get("sfa");
            codeUrl = (String) replyTable.get("code_url");
            codeTag = (String) replyTable.get("code_tag");
            hrn = (String) replyTable.get("hrn");
            urn = (String) replyTable.get("urn");
            Hashtable peersTable = (Hashtable) replyTable.get("peers");
            peers = new HashMap<String,String>();
            for (Object p : peersTable.keySet()) {
                String peer = (String) p;
                String peerUrl = (String) peersTable.get(p);
                peers.put(peer, peerUrl);
            }
        }

        public int getGeniApi() {
            return geniApi;
        }

        public int getSfa() {
            return sfa;
        }

        public String getCodeUrl() {
            return codeUrl;
        }

        public String getCodeTag() {
            return codeTag;
        }

        public String getHrn() {
            return hrn;
        }

        public String getUrn() {
            return urn;
        }

        public Map<String,String> getPeers() {
            return peers;
        }

        @Override
        public String toString() {
            return "GetVersionReply{" +
                    "geniApi=" + geniApi +
                    ", sfa=" + sfa +
                    ", codeUrl='" + codeUrl + '\'' +
                    ", codeTag='" + codeTag + '\'' +
                    ", hrn='" + hrn + '\'' +
                    ", urn='" + urn + '\'' +
                    ", peers=" + peers +
                    '}';
        }
    }

    /**
     * GetVersion call
     *
     * @param con the {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} to use for this call. Must be a connection to a SfaRegistry.
     * @return The version info, wrapped in a {@code PlanetlabSfaRegistryInterface.SimpleApiCallReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetVersion call: Get info about the version of the API supported at the server.")
    public SimpleApiCallReply<GetVersionReply> getVersion(GeniConnection con) throws GeniException {
        XMLRPCCallDetails res = executeXmlRpcCommand(con, "GetVersion", new Vector(), null, false);
        GetVersionReply r = new GetVersionReply(res);
        SimpleApiCallReply<GetVersionReply> wrappedres = new SimpleApiCallReply<GetVersionReply>(res, r);
        log(res, wrappedres, "getVersion", "GetVersion", con, null);
        return wrappedres;
    }

    /**
     * GetSelfCredential call:  A degenerate version of GetCredential used by a client to get his initial credential when de doesn't have one.

     The registry ensures that the client is the principal that is named by (type, name) by comparing the public key in the record's GID to
     the private key used to encrypt the client side of the HTTPS connection. Thus it is impossible for one principal to retrieve another
     principal's credential without having the appropriate private key.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a SfaRegistry.
     * @return the {@code GeniCredential} for the current user, wrapped in a {@code PlanetlabSfaRegistryInterface.SimpleApiCallReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetSelfCredential call: Get the credential of the current user.")
    public SimpleApiCallReply<GeniCredential> getSelfCredential(GeniConnection con,
               @ApiMethodParameter(name = "certificate", hint="The client's certificate.", multiLineString=true)
                    String certificate,
               @ApiMethodParameter(name = "xrn", hint="The client's URN or HRN.")
                    String xrn,
               @ApiMethodParameter(name = "type", hint="The client's type (if HRN is specified).", required=false)
                    String type) throws GeniException {
        Vector args = new Vector(3);
        args.add(certificate);
        args.add(xrn);
        if (type != null)
            args.add(type);
        else
            args.add("");

        XMLRPCCallDetails res = executeXmlRpcCommand(con, "GetSelfCredential", args, null, false);
        SimpleApiCallReply<GeniCredential> r = null;
        try {
            GeniCredential signedCredential = new GeniCredential("PlanetlabSfaRegistryInterface GetSelfCredential", res.getResultValueObject().toString());
            r = new SimpleApiCallReply<GeniCredential>(res, signedCredential);
        } catch (Exception e) {
            log(res, null, "getSelfCredential", "GetSelfCredential", con, null);
            throw new GeniException("Exception retrieving Credential for GetSelfCredential call.", e, res, null);
        }

        log(res, r, "getCredential", "GetCredential", con, null);
        return r;

    }

    /**
     * GetSelfCredential call:  A degenerate version of GetCredential used by a client to get his initial credential when de doesn't have one.

     The registry ensures that the client is the principal that is named by (type, name) by comparing the public key in the record's GID to
     the private key used to encrypt the client side of the HTTPS connection. Thus it is impossible for one principal to retrieve another
     principal's credential without having the appropriate private key.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a SfaRegistry.
     * @return the {@code GeniCredential} for the current user, wrapped in a {@code PlanetlabSfaRegistryInterface.SimpleApiCallReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetSelfCredential call, with automatic argument: Get the credential of the current user.")
    public SimpleApiCallReply<GeniCredential> getSelfCredential_AutomaticArguments(GeniConnection con, GeniUser geniUser) throws GeniException {
        X509Certificate cert = geniUser.getCertificate();
        String certificate = KeyUtil.x509certificateToPem(cert);
        String xrn = geniUser.getUserUrn();

        Vector args = new Vector(3);
        args.add(certificate);
        args.add(xrn);
        args.add("");

        XMLRPCCallDetails res = executeXmlRpcCommand(con, "GetSelfCredential", args, null, false);
        SimpleApiCallReply<GeniCredential> r = null;
        try {
            GeniCredential signedCredential = new GeniCredential("PlanetlabSfaRegistryInterface GetSelfCredential", res.getResultValueObject().toString());
            r = new SimpleApiCallReply<GeniCredential>(res, signedCredential);
        } catch (Exception e) {
            log(res, null, "getSelfCredential", "GetSelfCredential", con, null);
            throw new GeniException("Exception retrieving Credential for GetSelfCredential call.", e, res, null);
        }

        log(res, r, "getCredential", "GetCredential", con, null);
        return r;

    }

    /**
     * GetCredential call:  Retrieve a credential for an object.

     string GetCredential(string credentials[], string xrn, string type)

     @param credential  A single credential string or an array of credentials.
     @param xrn The objects URN or HRN.
     @param type The objects type (if HRN is specified).

     Returns a string representation of a credential object.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a SfaRegistry.
     * @return the {@code GeniCredential} for the current user, wrapped in a {@code PlanetlabSfaRegistryInterface.SimpleApiCallReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetCredential call, without arguments: Get the credential of the current user.")
    public SimpleApiCallReply<GeniCredential> getCredential(GeniConnection con,
               @ApiMethodParameter(name = "credential", hint="A single credential string or an array of credentials (jFed currently only supports a single credential here)")
                    GeniCredential credential,
               @ApiMethodParameter(name = "xrn", hint="The objects URN or HRN.")
                    String xrn,
               @ApiMethodParameter(name = "type", hint="The objects type (if HRN is specified).", required=false)
                    String type) throws GeniException {
//        boolean isHrn = !xrn.startsWith("urn:");
        //let the user decide
//        if (isHrn == (type != null)) {
//            throw new GeniException("Error, an XRN that is a HRN, implies that type is specified. XRN is HRN (\""+xrn+"\"): "+isHrn+" type present: "+(type != null));
//        }

        Vector args = new Vector(3);
        args.add(credential.getCredentialXml());
        args.add(xrn);
        if (type != null)
            args.add(type);
        else
            args.add("");

        XMLRPCCallDetails res = executeXmlRpcCommand(con, "GetCredential", args, null, false);
        SimpleApiCallReply<GeniCredential> r = null;
        try {
            GeniCredential signedCredential = new GeniCredential("PlanetlabSfaRegistryInterface GetCredential", res.getResultValueObject().toString());
            r = new SimpleApiCallReply<GeniCredential>(res, signedCredential);
        } catch (Exception e) {
            log(res, null, "getCredential", "GetCredential", con, null);
            throw new GeniException("Exception retrieving Credential for GetCredential call.", e, res, null);
        }

        log(res, r, "getCredential", "GetCredential", con, null);
        return r;

    }

//TODO add "Remove" from "SFA Registry Interface"

    /**
     * Resolve call:
     *
     * struct Resolve(string xrns[], string credentials[])
     *
     * @param credential  A single credential string or an array of credentials.
     * @param xrn A single object URN or HRN or an array of URNs/HRNs.
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a SfaRegistry.
     * @return a struct packed as a Hashtable (wrapped in a {@code PlanetlabSfaRegistryInterface.SimpleApiCallReply})
     *         with at least the following attributes:
     *
     *       {
     *         string hrn,
     *         string type,
     *         string date_created,
     *         string last_updated,
     *         string authority,
     *         string peer_authority,
     *         struct gid
     *         {
     *             string hrn,
     *             string urn,
     *             string uuid
     *         }
     *
     *       }
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetCredential call, without arguments: Get the credential of the current user.")
    public SimpleApiCallReply<Hashtable> resolve(GeniConnection con,
               @ApiMethodParameter(name = "credential", hint="A single credential string or an array of credentials (jFed currently only supports a single credential here)")
                    GeniCredential credential,
               @ApiMethodParameter(name = "xrn", hint="A single object URN or HRN or an array of URNs/HRNs. (jFed currently only supports a single object here)")
                    String xrn) throws GeniException {
        Vector args = new Vector( 2);
        args.add(xrn);
        args.add(credential.getCredentialXml());

        XMLRPCCallDetails res = executeXmlRpcCommand(con, "Resolve", args, null, false);
        SimpleApiCallReply<Hashtable> r = null;
        try {
            Hashtable table = (Hashtable) res.getResultValueObject();
            r = new SimpleApiCallReply<Hashtable>(res, table);
        } catch (Exception e) {
            log(res, null, "Resolve", "Resolve", con, null);
            throw new GeniException("Exception in Resolve call.", e, res, null);
        }

        log(res, r, "resolve", "Resolve", con, null);
        return r;
    }
    /**
     * List call: List the records in an authority.
     *
     * struct[] List(string xrn, string credentials[])
     *
     * @param credential An array of credentials. At least one credential must be a valid slice credential for the slice specified in slice_urn.
     * @param xrn The authority's URN or HRN.
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a SfaRegistry.
     * @return  Returns a list of structs where each struct represents a registry record. packed as a Vector of Hashtable (wrapped in a {@code PlanetlabSfaRegistryInterface.SimpleApiCallReply})
     *
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetCredential call, without arguments: Get the credential of the current user.")
    public SimpleApiCallReply<Vector> list(GeniConnection con,
               @ApiMethodParameter(name = "credential", hint="A single credential string or an array of credentials (jFed currently only supports a single credential here)")
                    GeniCredential credential,
               @ApiMethodParameter(name = "xrn", hint="A single object URN or HRN or an array of URNs/HRNs. (jFed currently only supports a single object here)")
                    String xrn) throws GeniException {
        Vector args = new Vector( 2);
        args.add(xrn);
        Vector creds = new Vector(1);
        creds.add(credential.getCredentialXml());
        args.add(creds);

        XMLRPCCallDetails res = executeXmlRpcCommand(con, "List", args, null, false);
        SimpleApiCallReply<Vector> r = null;
        try {
            Vector list = (Vector) res.getResultValueObject();
            r = new SimpleApiCallReply<Vector>(res, list);
        } catch (Exception e) {
            log(res, null, "List", "List", con, null);
            throw new GeniException("Exception in List call.", e, res, null);
        }

        log(res, r, "list", "List", con, null);
        return r;
    }


    //TODO add other methods from interface (register and update, but problem is they take a struct as argument)
}
