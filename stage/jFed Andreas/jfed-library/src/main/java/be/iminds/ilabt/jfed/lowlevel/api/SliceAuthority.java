package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.lowlevel.ApiMethod;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;

import java.util.*;

/**
 * <p>This is a simple wrapper for the methods specified in the {@link <a href="http://www.protogeni.net/ProtoGeni/wiki/SliceAuthorityAPI">ProtoGeni Slice Authority API</a>}.
 * <p>All functionality provided by the API is available in this implementation, and little processing of results is done.
 * This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
 *
 * <p>All methods require a {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} argument and return a {@link SliceAuthority.SliceAuthorityReply} which wraps the actual return value of the call, if any.
 *
 * @see <a href="http://www.protogeni.net/ProtoGeni/wiki/SliceAuthorityAPI">ProtoGeni Slice Authority API</a>
 * @author Wim Van de Meerssche
 */
public class SliceAuthority extends AbstractApi {
    /**
     * A human readable name for the implemented API
     *
     * @return "ProtoGeni Slice Authority API v1"
     */
    static public String getApiName() {
        return "ProtoGeni Slice Authority API v1";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "ProtoGeni Slice Authority API v1"
     */
    @Override
    public String getName() {
        return getApiName();
    }

    /**
     * Construct a new SliceAuthority, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
     *
     * @param logger the logger to use. May be null if no logger is used.
     * @param autoRetryBusy whether or not to retry when a "busy" reply is received.
     */
    public SliceAuthority(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }
    /**
     * Construct a new SliceAuthority, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
     *
     * <p>This constructor sets autoRetryBusy to true, so "busy" replies are retried.
     *
     * @param logger the logger to use. May be null if no logger is used.
     */
    public SliceAuthority(Logger logger) {
        super(logger, true, new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
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


    public static class SliceAuthorityReply<T> implements ApiCallReply<T> {
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


        public Hashtable getRawResult() {
            return rawResult;
        }

        public T getValue() {
            return val;
        }

        public String getOutput() {
            return output;
        }

        public SliceAuthorityReply(XMLRPCCallDetailsGeni res) {
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
        public SliceAuthorityReply(XMLRPCCallDetailsGeni res, T val) {
            this.rawResult = res.getResult();

            int intCode = (Integer)res.getResultCode();
            this.genicode = GeniAMResponseCode.getByCode(intCode);
            this.val = val;
            this.output = res.getResultOutput();
        }
    }

    /**
     * GetVersion call: Get info about the version of the API supported at the server.
     *
     * @param con the {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} to use for this call. Must be a connection to a slice authority.
     * @return The version as a String, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="GetVersion call: Get info about the version of the API supported at the server.")
    public SliceAuthorityReply<String> getVersion(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetVersion", new Vector(), null);
        SliceAuthorityReply<String> r = new SliceAuthorityReply<String>(res);
        log(res, r, "getVersion", "GetVersion", con, null);
        return r;
    }

    /**
     * GetCredential call, without arguments: Get the credential of the current user (= the user used to authorize the {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection}).
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @return the {@code GeniCredential} for the current user, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="GetCredential call, without arguments: Get the credential of the current user.")
    public SliceAuthorityReply<GeniCredential> getCredential(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetCredential", new Vector(), null);
        SliceAuthorityReply<GeniCredential> r = null;

        int intCode = (Integer)res.getResultCode();
        GeniResponseCode genicode = GeniAMResponseCode.getByCode(intCode);
        if (genicode.isSuccess()) {
            try {
                GeniCredential signedCredential = new GeniCredential("SliceAuthority getCredential", res.getResultValueObject().toString());
                r = new SliceAuthorityReply<GeniCredential>(res, signedCredential);
            } catch (Exception e) {
                log(res, null, "getCredential", "GetCredential", con, null);
                throw new GeniException("Exception retrieving Credential for getCredential call.", e, res, genicode);
            }
        } else {
            r = new SliceAuthorityReply<GeniCredential>(res, null);
        }
        log(res, r, "getCredential", "GetCredential", con, null);
        return r;

    }

    /**
     * GetCredential call, with a slice ID arguments: Get the credential of the specified slice.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential for the user requesting the slice credential
     * @param slice a slice ID
     * @return the {@code GeniCredential } for the requested slice, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="GetCredential call, with a slice ID arguments: Get the credential of the specified slice.")
    public SliceAuthorityReply<GeniCredential> getSliceCredential(GeniConnection con,
                                                                  @ApiMethodParameter(name = "userCredential", hint="the credential for the user requesting the slice credential")
                                                                        GeniCredential userCredential,
                                                                  @ApiMethodParameter(name = "slice", hint="a slice ID (typically the slice URN).")
                                                                        ResourceId slice) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "slice", slice);

        Hashtable params = new Hashtable();

        params.put(slice.getType(), slice.getValue());
        params.put("type", "Slice");
        params.put("credential", userCredential.getCredentialXml());

        Vector argv = new Vector();
        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetCredential", argv, methodParams);

        int intCode = (Integer)res.getResultCode();
        GeniResponseCode genicode = GeniAMResponseCode.getByCode(intCode);

        SliceAuthorityReply<GeniCredential> r = null;
        if (genicode.isSuccess())
            try {
                GeniCredential sliceCredential = new GeniCredential("SliceAuthority getSliceCredential", res.getResultValueObject().toString());
                r = new SliceAuthorityReply<GeniCredential>(res, sliceCredential);
            } catch (Exception e) {
                log(res, null, "getSliceCredential", "GetCredential", con, methodParams);
                throw new GeniException("Error parsing getSliceCredential result credential:"+e.getMessage(), e, res, genicode);
            }
        else {
            r = new SliceAuthorityReply<GeniCredential>(res, null);
        }
        log(res, r, "getSliceCredential", "GetCredential", con, methodParams);
        return r;
    }
    /**
     * GetCredential call, with any type of argument: Get the credential of the specified type.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential for the user requesting the slice credential
     * @param type the type of object for which to request a credential
     * @param slice a slice ID
     * @return the {@code GeniCredential } for the requested slice, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="GetCredential call, with any type: Get the credential for an object of the specified type.")
    public SliceAuthorityReply<GeniCredential> getAnyCredential(GeniConnection con,
                                                                  @ApiMethodParameter(name = "userCredential", hint="the credential for the user requesting the slice credential")
                                                                        GeniCredential userCredential,
                                                                  @ApiMethodParameter(name = "type", hint="the type of object for which to request a credential (for example \"Slice\")", guiDefault = "Slice")
                                                                        String type,
                                                                  @ApiMethodParameter(name = "slice", hint="an ID (typically an URN or UUID).")
                                                                        ResourceId slice) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "slice", slice);

        Hashtable params = new Hashtable();

        params.put(slice.getType(), slice.getValue());
        params.put("type", type);
        params.put("credential", userCredential.getCredentialXml());

        Vector argv = new Vector();
        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetCredential", argv, methodParams);

        int intCode = (Integer)res.getResultCode();
        GeniResponseCode genicode = GeniAMResponseCode.getByCode(intCode);

        SliceAuthorityReply<GeniCredential> r = null;
        if (genicode.isSuccess())
            try {
                GeniCredential sliceCredential = new GeniCredential("SliceAuthority getAnyCredential for type "+type, res.getResultValueObject().toString());
                r = new SliceAuthorityReply<GeniCredential>(res, sliceCredential);
            } catch (Exception e) {
                log(res, null, "getAnyCredential", "GetCredential", con, methodParams);
                throw new GeniException("Error parsing getSliceCredential result credential:"+e.getMessage(), e, res, genicode);
            }
        else {
            r = new SliceAuthorityReply<GeniCredential>(res, null);
        }
        log(res, r, "getAnyCredential", "GetCredential", con, methodParams);
        return r;
    }

    /**
     * Resolve call, with a slice ID arguments: Get info about the specified slice.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential for the user requesting the info
     * @param slice a slice ID
     * @return a Hashtable containing info about the slice, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Resolve call, with a slice ID arguments: Get info about the specified slice.")
    public SliceAuthorityReply<Hashtable> resolveSlice(GeniConnection con,
                                                       @ApiMethodParameter(name="userCredential", hint="the credential for the user requesting the info")
                                                            GeniCredential userCredential,
                                                       @ApiMethodParameter(name="slice", hint="a slice ID (typically the slice URN).")
                                                            ResourceId slice) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "slice", slice);

        Hashtable params = new Hashtable();
        params.put(slice.getType(), slice.getValue());
        params.put("credential", userCredential.getCredentialXml());
        params.put("type", "Slice");

        Vector argv = new Vector();
        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Resolve", argv, methodParams);

        SliceAuthorityReply<Hashtable> r = null;

        Object resVal = res.getResultValueObject();
        if (! (resVal instanceof Hashtable)) {
            r = new SliceAuthorityReply<Hashtable>(res, null);
        } else {
            Hashtable sliceTable = (Hashtable) resVal;
            r = new SliceAuthorityReply<Hashtable>(res, sliceTable);
        }
        log(res, r, "resolveSlice", "Resolve", con, methodParams);
        return r;
    }

    /**
     * Resolve call, with a user ID arguments: Get info about the specified user.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential for the user requesting the info
     * @param user a user ID
     * @return a Hashtable containing info about the user, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Resolve call, with a user ID arguments: Get info about the specified user.")
    public SliceAuthorityReply<Hashtable> resolveUser(GeniConnection con,
                                                      @ApiMethodParameter(name="userCredential", hint="the credential for the user requesting the info")
                                                            GeniCredential userCredential,
                                                      @ApiMethodParameter(name="user", hint="a user ID (typically the user URN).")
                                                            ResourceId user) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "user", user);

        Hashtable params = new Hashtable();
        params.put(user.getType(), user.getValue());
        params.put("credential", userCredential.getCredentialXml());
        params.put("type", "User");

        Vector argv = new Vector();
        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Resolve", argv, methodParams);

        SliceAuthorityReply<Hashtable> r = null;

        Object resVal = res.getResultValueObject();
        if (! (resVal instanceof Hashtable)) {
            r = new SliceAuthorityReply<Hashtable>(res, null);
        } else {
            Hashtable userTable = (Hashtable) resVal;
            r = new SliceAuthorityReply<Hashtable>(res, userTable);
        }
        log(res, r, "resolveUser", "Resolve", con, methodParams);
        return r;
    }

    /**
     * Resolve call, with a any types: Get info about an object of the specified type.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential for the user requesting the info
     * @param user a user ID
     * @return a Hashtable containing info about the user, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Resolve call, with a user ID arguments: Get info about the specified user.")
    public SliceAuthorityReply<Hashtable> resolveAny(GeniConnection con,
                                                      @ApiMethodParameter(name="userCredential", hint="the credential for the user requesting the info")
                                                            GeniCredential userCredential,
                                                      @ApiMethodParameter(name="user", hint="a user ID (typically the user URN).")
                                                            ResourceId user) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "user", user);

        Hashtable params = new Hashtable();
        params.put(user.getType(), user.getValue());
        params.put("credential", userCredential.getCredentialXml());
        params.put("type", "User");

        Vector argv = new Vector();
        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Resolve", argv, methodParams);

        SliceAuthorityReply<Hashtable> r = null;

        Object resVal = res.getResultValueObject();
        if (! (resVal instanceof Hashtable)) {
            r = new SliceAuthorityReply<Hashtable>(res, null);
        } else {
            Hashtable userTable = (Hashtable) resVal;
            r = new SliceAuthorityReply<Hashtable>(res, userTable);
        }
        log(res, r, "resolveUser", "Resolve", con, methodParams);
        return r;
    }

    /**
     * BindToSlice call: give another user permission to use a slice.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param sliceCredential the credential for the slice to be shared
     * @param user the ID of the user which will get permission to use this slice. (urn format: {@code urn:publicid:IDN+<AUTHORITY>+user+<USER>})
     * @return true if successfull, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="BindToSlice call: give another user permission to use a slice.")
    public SliceAuthorityReply<Boolean> bindToSlice(GeniConnection con,
                                                           @ApiMethodParameter(name="sliceCredential", hint="the credential for the slice to be shared")
                                                                GeniCredential sliceCredential,
                                                           @ApiMethodParameter(name="user", hint="the ID of the user which will get permission to use this slice (typically the other user's URN).")
                                                                ResourceId user) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("sliceCredential", sliceCredential, "user", user);

        Vector argv = new Vector();

        Hashtable params = new Hashtable();
        params.put(user.getType(), user.getValue());
        params.put("credential", sliceCredential.getCredentialXml());

        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "BindToSlice", argv, methodParams);

        SliceAuthorityReply<Boolean> r = null;
        try {
            Integer success = (Integer)res.getResultValueObject();
            r = new SliceAuthorityReply<Boolean>(res, (!success.equals(0)));
        } catch (Exception e) {
            log(res, null, "bindToSlice", "BindToSlice", con, methodParams);
            throw new GeniException("Error parsing BindToSlice result credential:"+e.getMessage(), e);
        }
        log(res, r, "bindToSlice", "BindToSlice", con, methodParams);
        return r;
    }

    /**
     * Register call: create a new slice.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential of the user creating this slice.
     * @param slice the URN of the new slice (urn format: {@code urn:publicid:IDN+<AUTHORITY>+slice+<SLICENAME>})
     * @return the {@code GeniCredential} for the created slice, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Register call: create a new slice.")
    public SliceAuthorityReply<GeniCredential> register(GeniConnection con,
                                                        @ApiMethodParameter(name="userCredential", hint="the credential of the user creating this slice.")
                                                                GeniCredential userCredential,
                                                        @ApiMethodParameter(name="slice", hint="the URN of the new slice (urn syntax: urn:publicid:IDN+<AUTHORITY>+slice+<SLICENAME>)")
                                                        ResourceUrn slice) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "slice", slice);

        Vector argv = new Vector();

        if (!slice.getType().equals("urn"))
            throw new GeniException("Bad type for ResourceId slice (\""+slice.getType()+"\"). Only \"urn\" is supported here");

        Hashtable params = new Hashtable();
        params.put("type", "Slice");
        params.put("credential", userCredential.getCredentialXml());
        params.put(slice.getType(), slice.getValue());

        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Register", argv, methodParams);

        SliceAuthorityReply<GeniCredential> r = null;
        int intCode = (Integer)res.getResultCode();
        GeniResponseCode genicode = GeniAMResponseCode.getByCode(intCode);
        if (genicode.isSuccess()) {
            try {
                GeniCredential sliceCredential = new GeniCredential("SliceAuthority register "+slice.getValue(), res.getResultValueObject().toString());
                r = new SliceAuthorityReply<GeniCredential>(res, sliceCredential);
            } catch (Exception e) {
                log(res, null, "register", "Register", con, methodParams);
                throw new GeniException("Error parsing BindToSlice result credential:"+e.getMessage(), e);
            }
        } else
            r = new SliceAuthorityReply<GeniCredential>(res, null);

        log(res, r, "register", "Register", con, methodParams);
        return r;
    }

    /**
     * RenewSlice call: Change the expiration date of a slice.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param sliceCredential the credential for the slice to be renewed
     * @param expiration_rfc3339 a string with the new expiration date, in RFC3339 format. See {@link be.iminds.ilabt.jfed.util.RFC3339Util} for help converting to the correct format.
     * @return the {@code GeniCredential} for the renewed slice, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     * @see  <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     */
    @ApiMethod(hint="RenewSlice call: Change the expiration date of a slice.")
    public SliceAuthorityReply<GeniCredential> renewSlice(GeniConnection con,
                                                          @ApiMethodParameter(name="sliceCredential", hint="the credential for the slice to be renewed")
                                                                GeniCredential sliceCredential,
                                                          @ApiMethodParameter(name="expiration_rfc3339", hint="A string with the new experiation date, in RFC3339 format.")
                                                                String expiration_rfc3339) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("sliceCredential", sliceCredential, "expiration_rfc3339", expiration_rfc3339);
        Vector argv = new Vector();

        Hashtable params = new Hashtable();
        params.put("expiration", expiration_rfc3339);
        params.put("credential", sliceCredential.getCredentialXml());

        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "RenewSlice", argv, methodParams);

        SliceAuthorityReply<GeniCredential> r = null;
        int intCode = (Integer)res.getResultCode();
        GeniResponseCode genicode = GeniAMResponseCode.getByCode(intCode);
        if (genicode.isSuccess()) {
            try {
                GeniCredential newSliceCredential = new GeniCredential("SliceAuthority renewSlice", res.getResultValueObject().toString());
                r = new SliceAuthorityReply<GeniCredential>(res, newSliceCredential);
            } catch (Exception e) {
                log(res, null, "register", "Register", con, methodParams);
                throw new GeniException("Error parsing BindToSlice result credential:"+e.getMessage(), e);
            }
        } else
            r = new SliceAuthorityReply<GeniCredential>(res, null);

        log(res, r, "renewSlice", "RenewSlice", con, methodParams);
        return r;
    }

    /**
     * <p>Shutdown call:
     * <p>
     * <p>API description:
     * <p>Perform an emergency shutdown on a slice, by asking the SA (for that slice) to do an emergency shutdown. Operationally, the request is forwarded to the ClearingHouse which knows the full set of Component Managers. The call returns once the ClearingHouse is notified; the ClearingHouse will process the request asynchronously.
     *
     * <p>
     * <p>
     * <p>Clearinghouse API description:
     * <p>Perform an emergency shutdown on a slice. This is typically invoked by the Slice Authority for the slice, but may be invoked by anyone with a clearinghouse credential (this needs to change to allow anyone with a valid slice credential). As it stands, anyone with a slice credential can contact the Slice Authority for the slice, and ask it to do the shutown operation.
     * <p>Since the Clearinghouse must contact each Component Manager to tell it to shutdown the slice, this call will return immediately. There is currently no facility to find out if/when the shutdown has completed.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param sliceCredential the credential for the slice to be shut down
     * @return a boolean (success or failure), wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="API description:\n" +
            "Perform an emergency shutdown on a slice, by asking the SA (for that slice) to do an emergency shutdown. Operationally, the request is forwarded to the ClearingHouse which knows the full set of Component Managers. The call returns once the ClearingHouse is notified; the ClearingHouse will process the request asynchronously.\n" +
            "\n" +
            "Clearinghouse API description:\n" +
            "Perform an emergency shutdown on a slice. This is typically invoked by the Slice Authority for the slice, but may be invoked by anyone with a clearinghouse credential (this needs to change to allow anyone with a valid slice credential). As it stands, anyone with a slice credential can contact the Slice Authority for the slice, and ask it to do the shutown operation.\n" +
            "Since the Clearinghouse must contact each Component Manager to tell it to shutdown the slice, this call will return immediately. There is currently no facility to find out if/when the shutdown has completed.\n")
    public SliceAuthorityReply<Boolean> shutdown(GeniConnection con,
                                                 @ApiMethodParameter(name="sliceCredential", hint="The credential of the slice to shut down.")
                                                    GeniCredential sliceCredential) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("sliceCredential", sliceCredential);

        Vector argv = new Vector();

        Hashtable params = new Hashtable();
        params.put("credential", sliceCredential.getCredentialXml());

        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Shutdown", argv, methodParams);

        SliceAuthorityReply<Boolean> r = new SliceAuthorityReply<Boolean>(res);
        log(res, r, "shutdown", "Shutdown", con, methodParams);
        return r;
    }

    /**
     * GetKeys call: Get the public SSH key()s of a user.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential of the user retrieving ssh keys
     * @return a list of keys, wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint="GetKeys call: Get the public SSH key()s of a user.")
    public SliceAuthorityReply<List<String>> getKeys(GeniConnection con,
                                               @ApiMethodParameter(name="userCredential", hint="the credential of the user retrieving ssh keys")
                                                    GeniCredential userCredential) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential);

        Vector argv = new Vector();

        Hashtable params = new Hashtable();
        params.put("credential", userCredential.getCredentialXml());

        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetKeys", argv, methodParams);

        List<String> keyList = new ArrayList<String>();
        try {
            Vector keys = (Vector) res.getResultValueObject();
            for (Object key : keys) {
                if (key instanceof String)
                    keyList.add((String) key);
                else {
                    if (key instanceof Hashtable) {
                        Hashtable t = (Hashtable) key;
                        String type = (String) t.get("type");
                        if (!type.equals("ssh"))
                            System.err.println("Warning: key is hashtable with non 'ssh' type: "+t);
                        keyList.add((String) t.get("key"));
                    } else
                        throw new GeniException("key is of type "+key.getClass().getName()+" val="+key.toString());
                }
            }
        } catch (Exception e) {
            System.err.println("Warning (ignored): getKeys did not return a Vector<string> but a "+res.getResultValueObject().getClass()+" -> "+e.getMessage());
            System.err.println("res.getResultValueObject()="+res.getResultValueObject());
            e.printStackTrace();
        }
        SliceAuthorityReply<List<String>> r = new SliceAuthorityReply<List<String>>(res, keyList);
        log(res, r, "getKeys", "GetKeys", con, methodParams);
        return r;
    }

    /**
     * Remove call: remove a slice.
     *
     * Note: at the time of writing this documentation, remove was not implemented at the server.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to a slice authority.
     * @param userCredential the credential of the user removing a slice.
     * @param slice the ID of the slice to be removed.
     * @return a boolean (success or failure), wrapped in a {@code SliceAuthority.SliceAuthorityReply}
     * @throws GeniException
     */
    @ApiMethod(hint=" Remove call: remove a slice.\n\nNote: at the time of writing this documentation, remove was not implemented at the server.")
    public SliceAuthorityReply<Boolean> remove(GeniConnection con,
                                               @ApiMethodParameter(name="userCredential", hint="The credential of the user removing a slice.")
                                                    GeniCredential userCredential,
                                               @ApiMethodParameter(name="slice", hint="The ID of the slice to be removed (typically the slice URN).")
                                                    ResourceId slice) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("userCredential", userCredential, "slice", slice);

        Vector argv = new Vector();

        Hashtable params = new Hashtable();
        params.put("type", "Slice");
        params.put("credential", userCredential.getCredentialXml());
        params.put(slice.getType(), slice.getValue());

        argv.setSize(1);
        argv.set(0, params);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Remove", argv, methodParams);

        SliceAuthorityReply<Boolean> r = new SliceAuthorityReply<Boolean>(res);
        log(res, r, "remove", "Remove", con, methodParams);
        return r;
    }


    //TODO: add DiscoverResources  (ignored because same as ClearingHouse ListComponents)
}
