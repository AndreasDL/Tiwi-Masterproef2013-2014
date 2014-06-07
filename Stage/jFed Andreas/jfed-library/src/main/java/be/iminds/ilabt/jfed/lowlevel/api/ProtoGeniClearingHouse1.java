package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.lowlevel.ApiMethod;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.log.Logger;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

/**
 * <p>This is a simple wrapper for the methods specified in the {@link <a href="http://www.protogeni.net/ProtoGeni/wiki/ClearingHouseAPI1">ProtoGeni Clearing House API v1</a>}.
  * <p>All functionality provided by the API is available in this implementation, and little processing of results is done.
  * This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
  * Only a few methods are implemented, as most do not make sense for a client. (You can use the existing methods as a template and example to extend this class if other methods are needed)
  *
  * <p>All methods require a {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} argument and return a {@link ProtoGeniClearingHouse1.ClearingHouseReply} which wraps the actual return value of the call, if any.
  *
  * @see <a href="http://www.protogeni.net/ProtoGeni/wiki/ClearingHouseAPI1">ProtoGeni Clearing House API v1</a>
  *
  * @author Wim Van de Meerssche
 */
public class ProtoGeniClearingHouse1 extends AbstractApi {
    /**
     * A human readable name for the implemented API
     *
     * @return "ProtoGeni Clearing House API v1";
     */
    static public String getApiName() {
        return "ProtoGeni Clearing House API v1";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "ProtoGeni Clearing House API v1";
     */
    @Override
    public String getName() {
        return getApiName();
    }

    /**
     * Construct a new ClearingHouse1, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
     *
     * @param logger the logger to use. May be null if no logger is used.
     * @param autoRetryBusy whether or not to retry when a "busy" reply is received.
     */
    public ProtoGeniClearingHouse1(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.PROTOGENI_CH, 1));
    }
    /**
     * Construct a new ClearingHouse1, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
     *
     * <p>This constructor sets autoRetryBusy to true, so "busy" replies are retried.
     *
     * @param logger the logger to use. May be null if no logger is used.
     */
    public ProtoGeniClearingHouse1(Logger logger) {
        super(logger, true, new ServerType(ServerType.GeniServerRole.PROTOGENI_CH, 1));
    }

    @Override
    protected boolean isBusyReply(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res) {
        if (res instanceof XMLRPCCallDetailsGeni) {
            XMLRPCCallDetailsGeni geniDetails = (XMLRPCCallDetailsGeni) res;
            int code = (Integer)geniDetails.getResultCode();
            return GeniAMResponseCode.getByCode(code).isBusy();
        }
        return false;
    }

    public static class ClearingHouseReply<T> implements ApiCallReply<T> {
        private GeniAMResponseCode genicode;
        private T val;
        /* output is typically set only on error*/
        private String output;
        private Hashtable rawResult;

        public int getCode() {
            return genicode.getCode();
        }
        public GeniAMResponseCode getGeniResponseCode() {
            return genicode;
        }

        public T getValue() {
            return val;
        }

        public String getOutput() {
            return output;
        }

        @Override
        public Hashtable getRawResult() {
            return rawResult;
        }

        public ClearingHouseReply(XMLRPCCallDetailsGeni res) {
            this.rawResult = res.getResult();
            int intCode = (Integer)res.getResultCode();
            this.genicode = GeniAMResponseCode.getByCode(intCode);
            try {
                this.val = (T) res.getResultValueObject();
            } catch (Exception e) {
                this.val = null;
            }
            this.output = res.getResultOutput();
        }
        public ClearingHouseReply(XMLRPCCallDetailsGeni res, T val) {
            this.rawResult = res.getResult();
            int intCode = (Integer)res.getResultCode();
            this.genicode = GeniAMResponseCode.getByCode(intCode);
            this.val = val;
            this.output = res.getResultOutput();
        }
    }


    /**
     * Returns an integer corresponding to the revision of this API supported by the clearing house.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a clearinghouse.
     * @return an integer corresponding to the revision of this API supported by the clearing house.
     */
    @ApiMethod
    public ClearingHouseReply<Integer> getVersion(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetVersion", new Vector(), null);
        ClearingHouseReply<Integer> r = new ClearingHouseReply<Integer>(res);
        log(res, r, "getVersion", "GetVersion", con, null);
        return r;
    }

    /**
     * Request a credential for accessing certain protected parts of the clearinghouse API.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a clearinghouse.
     * @return a credential for accessing certain protected parts of the clearinghouse API.
     */
    @ApiMethod
    public ClearingHouseReply<GeniCredential> getCredential(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetCredential", new Vector(), null);
        GeniCredential signedCredential = new GeniCredential("ClearingHouse getCredential", res.getResultValueObject().toString());
        ClearingHouseReply<GeniCredential> r = new ClearingHouseReply<GeniCredential>(res, signedCredential);
        log(res, r, "getCredential", "GetCredential", con, null);
        return r;
    }

    public static class ComponentInfo {
        private Gid gid;
        private String hrn, url, urn;

        public ComponentInfo(Hashtable ht) {
            this.gid = new Gid(ht.get("gid").toString());
            this.hrn = ht.get("hrn").toString();
            this.url = ht.get("url").toString();
            this.urn = ht.get("urn").toString();
        }

        public Gid getGid() {
            return gid;
        }

        public String getHrn() {
            return hrn;
        }

        public String getUrl() {
            return url;
        }

        public String getUrn() {
            return urn;
        }

        @Override
        public String toString() {
            return "ComponentInfo{" +
                    "gid='" + gid + '\'' +
                    ", hrn='" + hrn + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }
    }

    /**
     * Return a list of all component managers. This is typically used by a client to find out where the managers are,
     * so that it can ask the managers about the specific resources it manages.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a clearinghouse.
     * @param credential a valid user credential. This is typically the credential one receives when calling
     *                  {@link SliceAuthority#getCredential(be.iminds.ilabt.jfed.lowlevel.GeniConnection)}
     * @return a list of aggregate manager info.
     */
    @ApiMethod
    public ClearingHouseReply<Vector<ComponentInfo>> listComponents(GeniConnection con, @ApiMethodParameter(name = "userCredential") GeniCredential credential) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", credential);

        Vector args = new Vector();
        Hashtable params = new Hashtable();
        params.put("credential", credential.getCredentialXml());
        args.setSize(1);
        args.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "ListComponents", args, methodParams);

        Vector<ComponentInfo> compList = new Vector<ComponentInfo>();

        if (res.getResultValueObject() instanceof Vector) {
            Vector resList = (Vector) res.getResultValueObject();
            for (Object r : resList) {
                if (r instanceof Hashtable)
                    compList.add(new ComponentInfo((Hashtable)r));
                else
                    break;
            }
        }

        ClearingHouseReply<Vector<ComponentInfo>> r = new ClearingHouseReply<Vector<ComponentInfo>>(res, compList);
        log(res, r, "listComponents", "ListComponents", con, methodParams);
        return r;
    }


    //These are not interesting to implement on the client

//    /**
//     * Register a principle object.
//     *    Note: Slice authorities register the slices they create, as well as the users who create those slices.
//     *          SAs and CMs are registered with the clearinghouse as part of the process of joining the federation.
//     *          Component Managers (will, but do not yet) register the components they manage.
//     * @param con
//     * @param credential the credential returned by GetCredential()
//     * @param gid the gid (public key) of the object to be registered,
//     * @param type one of SA|CM|Component|Slice|User
//     * @param info an array of additional arguments specific to the principle being registered
//     * @return success
//     * @throws GeniException
//     */
//    @ApiMethod
//    public ClearingHouseReply<Boolean> register(GeniConnection con, GeniCredential credential, String gid, String type, Hashtable info) throws GeniException {
//        Vector args = new Vector();
//        args.setSize(4);
//        args.set(0, credential.getCredentialXml());
//        args.set(1, gid);
//        args.set(2, type);
//        args.set(3, info);
//        XMLRPCHelper.XMLRPCHelperResult res = XMLRPCHelper.execute(con, "GetCredential", args);
//        log(res);
//        return new ClearingHouseReply<Boolean>(res);
//    }
//
//
//
//    private ClearingHouseReply<Hashtable> resolve(GeniConnection con, GeniCredential credential, String resource, String type) throws GeniException {
//        XMLRPCHelper.XMLRPCHelperResult res = XMLRPCHelper.execute(con, "GetCredential", new Vector());
//        log(res);
//        return new ClearingHouseReply<Hashtable>(res);
//    }
//
//    @ApiMethod
//    public ClearingHouseReply<Hashtable> resolve(GeniConnection con, GeniCredential credential, UuidResource uuid, String type) throws GeniException {
//        return resolve(con, credential, uuid.getValue(), type);
//    }
//
//    @ApiMethod
//    public ClearingHouseReply<Hashtable> resolve(GeniConnection con, GeniCredential credential, HrnResource hrn, String type) throws GeniException {
//        return resolve(con, credential, hrn.getValue(), type);
//    }
//TODO Remove(credential, uuid, type)
//TODO Shutdown(credential, uuid);
//TODO PostCRL(credential, certificate);
//TODO List(credential,type);

}
