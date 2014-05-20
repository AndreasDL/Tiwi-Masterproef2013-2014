package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.lowlevel.ApiMethod;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.DataConversionUtils;
import be.iminds.ilabt.jfed.util.RFC3339Util;
import be.iminds.ilabt.jfed.util.XmlRpcPrintUtil;

import java.util.*;

/**
 * <p>This is a simple wrapper for the methods specified in the {@link <a href="http://groups.geni.net/geni/wiki/GAPI_AM_API_V2">Geni Aggregate Manager API v2</a>}.
 * <p>All functionality provided by the API is available in this implementation, and little processing of results is done.
 * This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
 *
 * <p>All methods require a {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} argument and return a {@link AggregateManager2.AggregateManagerReply} which wraps the actual return value of the call, if any.
 *
 * @see <a href="http://groups.geni.net/geni/wiki/GAPI_AM_API_V2">Geni Aggregate Manager API v2</a>
 *
 * @author Wim Van de Meerssche
 */
public class AggregateManager2 extends AbstractGeniAggregateManager {
    /**
     * A human readable name for the implemented API
     *
     * @return "Geni Aggregate Manager API v2";
     */
    static public String getApiName() {
        return "Geni Aggregate Manager API v2";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "Geni Aggregate Manager API v2";
     */
    @Override
    public String getName() {
        return getApiName();
    }

    /**
     * Construct a new AggregateManager2, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
     *
     * @param logger the logger to use. May be null if no logger is used.
     * @param autoRetryBusy whether or not to retry when a "busy" reply is received.
     */
    public AggregateManager2(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.AM, 2));
    }
    /**
     * Construct a new AggregateManager2, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link Logger} if it is non null.
     *
     * <p>This constructor sets autoRetryBusy to true, so "busy" replies are retried.
     *
     * @param logger the logger to use. May be null if no logger is used.
     */
    public AggregateManager2(Logger logger) {
        super(logger, true, new ServerType(ServerType.GeniServerRole.AM, 2));
    }

    public static class VersionInfo {
        public static class VersionPair {
            private String url;
            private String versionNr;

            private VersionPair(String versionNr, String url) {
                this.versionNr = versionNr;
                this.url = url;
            }
            public String getUrl() {
                return url;
            }
            public String getVersionNr() {
                return versionNr;
            }

            @Override
            public String toString() {
                return "VersionPair{" +
                        "url='" + url + '\'' +
                        ", versionNr='" + versionNr + '\'' +
                        '}';
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                VersionPair that = (VersionPair) o;

                if (url != null ? !url.equals(that.url) : that.url != null) return false;
                if (versionNr != null ? !versionNr.equals(that.versionNr) : that.versionNr != null) return false;

                return true;
            }
            @Override
            public int hashCode() {
                int result = url != null ? url.hashCode() : 0;
                result = 31 * result + (versionNr != null ? versionNr.hashCode() : 0);
                return result;
            }
        }
        public static class RspecVersion {
            private String type;
            private String version;
            private String schema;
            private String namespace;
            private Vector extensions;

            private RspecVersion(Hashtable ht) {
                type = (String) ht.get("type");
                version = (String) ht.get("version");
                schema = (String) ht.get("schema");
                namespace = (String) ht.get("namespace");
                extensions = (Vector) ht.get("extensions");
            }
            private RspecVersion(String type, String version, String schema, String namespace, Vector<String> extensions) {
                this.type = type;
                this.version = version;
                this.schema = schema;
                this.namespace = namespace;
                this.extensions = extensions;
            }
            public String getType() {
                return type;
            }
            public String getVersion() {
                return version;
            }
            public String getSchema() {
                return schema;
            }
            public String getNamespace() {
                return namespace;
            }
            public List<String> getExtensions() {
                List<String> res = new ArrayList<String>();
                for (Object o : extensions)
                    res.add((String) o);
                return res;
            }

            @Override
            public String toString() {
                return "RspecVersion{" +
                        "type='" + type + '\'' +
                        ", version='" + version + '\'' +
                        ", schema='" + schema + '\'' +
                        ", namespace='" + namespace + '\'' +
                        ", extensions=" + extensions +
                        '}';
            }

            public boolean equalTypeAndVersion(RspecVersion o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                RspecVersion that = (RspecVersion) o;

                if (type != null ? !type.equals(that.type) : that.type != null) return false;
                if (version != null ? !version.equals(that.version) : that.version != null) return false;

                return true;
            }
        }
        private int api;
        private List<VersionPair> apiVersions;
        private List<RspecVersion> requestRspecVersions;
        private List<RspecVersion> adRspecVersions;
        private Hashtable raw;

        public VersionInfo(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res) throws GeniException {
            Hashtable xmlRpcReplyValue = (Hashtable) res.getResultValueObject();
            raw = xmlRpcReplyValue;

            api = (Integer) xmlRpcReplyValue.get("geni_api");

            if (!(xmlRpcReplyValue.get("geni_api_versions") instanceof Hashtable))
                throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "geni_api_versions is not a struct", res, null);
            if (!(xmlRpcReplyValue.get("geni_request_rspec_versions") instanceof Vector))
                throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "geni_request_rspec_versions is not a list of structs", res, null);
            if (!(xmlRpcReplyValue.get("geni_ad_rspec_versions") instanceof Vector))
                throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "geni_ad_rspec_versions is not a list of structs", res, null);
            Hashtable apiVersionHT = (Hashtable) xmlRpcReplyValue.get("geni_api_versions");
            Vector requestRspecVersionsV = (Vector) xmlRpcReplyValue.get("geni_request_rspec_versions");
            Vector adRspecVersionsV = (Vector) xmlRpcReplyValue.get("geni_ad_rspec_versions");

            this.apiVersions = new ArrayList<VersionPair>();
            this.requestRspecVersions = new ArrayList<RspecVersion>();
            this.adRspecVersions = new ArrayList<RspecVersion>();

            for (Object k : apiVersionHT.keySet()) {
                try {
                    String url = (String) k;
                    String nr = (String) apiVersionHT.get(k);
                    apiVersions.add(new VersionPair(url, nr));
                } catch (Exception e) {
                    throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "rspec geni_api_versions struct does not contain all needed fields", e, res, null);
                }
            }
            for (Object o : requestRspecVersionsV) {
                if (!(o instanceof Hashtable))
                    throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "geni_request_rspec_versions is a list but contains a non struct: "+o.getClass(), res, null);
                Hashtable h = (Hashtable) o;
                try {
                    requestRspecVersions.add(new RspecVersion(h));
                } catch (Exception e) {
                    throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "rspec geni_request_rspec_versions struct does not contain all needed fields", e, res, null);
                }
            }
            for (Object o : adRspecVersionsV) {
                if (!(o instanceof Hashtable))
                    throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "geni_ad_rspec_versions is a list but contains a non struct: "+o.getClass(), res, null);
                Hashtable h = (Hashtable) o;

                try {
                    adRspecVersions.add(new RspecVersion(h));
                } catch (Exception e) {
                    throw new GeniSpecificationNonconformityException("GetVersion", getApiName(), "rspec geni_ad_rspec_versions struct does not contain all needed fields", e, res, null);
                }
            }
        }

        /**
         * @return extra field included in GetVersion reply (or null if not present)
         * Examples of fields: "hrn" and "urn"
         * */
        public String getExtra(String fieldname) {
            if (raw == null) throw new RuntimeException("no raw XmlRpc data in GetVersion result.");
            if (raw.get(fieldname) == null) return null;
            return raw.get(fieldname).toString();
        }

        public int getApi() {
            return api;
        }
        public List<VersionPair> getApiVersions() {
            return apiVersions;
        }
        public List<RspecVersion> getRequestRspecVersions() {
            return requestRspecVersions;
        }
        public List<RspecVersion> getAdRspecVersions() {
            return adRspecVersions;
        }

        @Override
        public String toString() {
            return "VersionInfo{" +
                    "api=" + api +
                    ", apiVersions=" + apiVersions +
                    ", requestRspecVersions=" + requestRspecVersions +
                    ", adRspecVersions=" + adRspecVersions +
                    '}';
        }
    }

    /**
     *  Get static version and configuration information about this aggregate. Return includes:
     *
     *  The version of the GENI Aggregate Manager API supported by this aggregate manager instance
     *  URLs for other versions of this API supported by this aggregate
     *  The RSpec formats accepted at this aggregate
     *  Other information about the configuration of this aggregate.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 2".
     * @return the version info of the aggregate manager, wrapped in a {@code AggregateManager2.AggregateManagerReply}
     * */
    @ApiMethod(hint="Get static version and configuration information about this aggregate. Return includes:\n\n" +
            "- The version of the GENI Aggregate Manager API supported by this aggregate manager instance\n" +
            "- URLs for other versions of this API supported by this aggregate\n" +
            "- The RSpec formats accepted at this aggregate\n" +
            "- Other information about the configuration of this aggregate.")
    public AggregateManagerReply<VersionInfo> getVersion(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetVersion", new Vector(), null);
        AggregateManagerReply<VersionInfo> r = null;
        try {
            r = new AggregateManagerReply<VersionInfo>(res, new VersionInfo(res));
        } catch (Exception e) {
            System.err.println("Exception parsing AMv2 GetVersion response");
        }
        log(res, r, "getVersion", "GetVersion", con, null);
        return r;
    }

    private static void addHashtables(Hashtable target, Hashtable source) {
        if (source == null)
            return;
        for (Object o : source.keySet())
            target.put(o, source.get(o));
    }

    /**
     * Return a listing and description of available resources at this aggregate, or resources allocated to a named
     * slice at this aggregate. The resource listing and description provides sufficient information for clients to
     * select among available resources, or to use reserved resources. These listings are known as RSpecs.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 2".
     * @param credentialList mandatory list of credentials. For an advertisement rspec request, this must includea valid
     *                       user credential. For a slice manifest rspec request, this must include a valid credential
     *                       for the slice.
     * @param rspecType mandatory rspec type (example: "geni")
     * @param rspecVersion mandatory rspec version (example for type "geni": "3")
     * @param available optional (not specified = null): if true, show only available local resources, otherwise show
     *                  everything
     * @param compressed optional (not specified = null): if true compress result (RFC 1950).      NOTE: if compressed
     *                   is set, this will decompress the result.
     * @param sliceUrn optional (not specified = null): if set, show manifest rspec for the slice
     * @return requested rspec, wrapped in a {@code AggregateManager2.AggregateManagerReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Return a listing and description of available resources at this aggregate, or resources allocated to a named " +
            "slice at this aggregate. The resource listing and description provides sufficient information for clients to " +
            "select among available resources, or to use reserved resources. These listings are known as RSpecs.")
    public AggregateManagerReply<String> listResources(GeniConnection con,
                                                       @ApiMethodParameter(name="credentialList", hint="mandatory list of credentials. For an advertisement rspec request, this must includea valid user credential. For a slice manifest rspec request, this must include a valid credential for the slice.")
                                                            List<GeniCredential> credentialList,
                                                       @ApiMethodParameter(name="rspecType", guiDefault="geni", hint="mandatory rspec type (example: \"geni\")")
                                                            String rspecType,
                                                       @ApiMethodParameter(name="rspecVersion", guiDefault="3", hint="mandatory rspec version (example for type \"geni\": \"3\")")
                                                            String rspecVersion,
                                                       @ApiMethodParameter(name="available", required=false, hint="optional: if true, show only available local resources, otherwise show everything")
                                                            Boolean available,
                                                      @ApiMethodParameter(name="compressed", required=false, guiDefault="true", guiDefaultOptional=true, hint="optional: if true compress result (RFC 1950).\nNOTE: if compressed is set, this API will decompress the result automatically.")
                                                            Boolean compressed,
                                                      @ApiMethodParameter(name="sliceUrn", required=false, hint="optional: if set, show manifest RSpec for the slice instead of the advertisement RSpec for the AM.")
                                                            String sliceUrn,
                                                      Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "rspecType", rspecType, "rspecVersion", rspecVersion);
        if (available != null) methodParams.put("available", available);
        if (compressed != null) methodParams.put("compressed", compressed);
        if (sliceUrn != null) methodParams.put("sliceUrn", sliceUrn);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(rspecType, "rspecType");
        checkNonNullArgument(rspecVersion, "rspecVersion");

        Hashtable rspecVersionOpt = new Hashtable();
        rspecVersionOpt.put("type", rspecType);
        rspecVersionOpt.put("version", rspecVersion);

        Hashtable options = new Hashtable();
        options.put("geni_rspec_version", rspecVersionOpt);
        if (sliceUrn != null)
            options.put("geni_slice_urn", sliceUrn);
        if (available != null)
            options.put("geni_available", available.booleanValue());
        if (compressed != null)
            options.put("geni_compressed", compressed.booleanValue());
        addHashtables(options, extraOptions);

        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++) {
            GeniCredential c = credentialList.get(i);
            if (c == null)
                throw new IllegalArgumentException("Illegal arguments: no GeniCredential is allowed to be null in credentialList: "+credentialList);
            String credXml = c.getCredentialXml();
            if (credXml == null)
                throw new IllegalArgumentException("Illegal arguments: no GeniCredential is allowed to have null as XML in credentialList: "+credentialList);
            credentialsVector.set(i, credXml);
        }

        Vector argv = new Vector();
        argv.setSize(2);
        argv.set(0, credentialsVector);
        argv.set(1, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "ListResources", argv, methodParams);

        AggregateManagerReply<String> r;
        if (compressed != null && compressed)
        {
            String decompressed_value = DataConversionUtils.decompressFromBase64(res.getResultValueString());
            r = new AggregateManagerReply<String>(res, decompressed_value);
        }
        else
            r = new AggregateManagerReply<String>(res);
        log(res, r, "listResources", "ListResources", con, methodParams);
        return r;
    }

    /**
     * Allocate resources as described in a request RSpec argument to a slice with the named URN. This operation is
     * expected to start the allocated resources asynchronously after the operation has successfully completed.
     * Callers can check on the status of the resources using SliverStatus. Resources will be reserved until a
     * particular time, set by the aggregate according to policy. That expiration time will be no later than the
     * expiration time of the provided slice credential. This method returns a listing and description of the resources
     * reserved for the slice by this operation, in the form of a manifest RSpec.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 2".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param sliceUrn urn for the slice on which this sliver needs to be created
     * @param rspec An RSpec matching the GENI standard request RSpec schema containing the resources that the caller is
     *              requesting for allocation to the slice specified in slice_urn.
     * @param users An array of user structs, which contain information (typically SSH public keys) about needed to allow users to login on allocated nodes.
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return The manifest Rspec for the created sliver, wrapped in a {@code AggregateManager2.AggregateManagerReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Allocate resources as described in a request RSpec argument to a slice with the named URN. This operation is " +
            "expected to start the allocated resources asynchronously after the operation has successfully completed. " +
            "Callers can check on the status of the resources using SliverStatus. Resources will be reserved until a " +
            "particular time, set by the aggregate according to policy. That expiration time will be no later than the " +
            "expiration time of the provided slice credential. This method returns a listing and description of the resources " +
            "reserved for the slice by this operation, in the form of a manifest RSpec.")
    public AggregateManagerReply<String> createSliver(GeniConnection con,
                                                      @ApiMethodParameter(name="credentialList", hint="mandatory list of credentials. This must include a valid credential for the slice.")
                                                            List<GeniCredential> credentialList,
                                                      @ApiMethodParameter(name="sliceUrn", hint="urn for the slice on which this sliver needs to be created")
                                                            String sliceUrn,
                                                      @ApiMethodParameter(name="rspec", hint=" An RSpec matching the GENI standard request RSpec schema containing the resources that the caller is requesting for allocation to the slice specified in slice_urn.")
                                                            String rspec,
                                                      @ApiMethodParameter(name="UserSpecList", hint="An array of user structs, which contain information (typically SSH public keys) about needed to allow users to login on allocated nodes.")
                                                            List<UserSpec> users,
                                                      Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn, "rspec", rspec, "users", users);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(sliceUrn, "sliceUrn");
        checkNonNullArgument(rspec, "rspec");

        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++)
            credentialsVector.set(i, credentialList.get(i).getCredentialXml());

        Vector usersVector = new Vector();
        if (users != null) {
            usersVector.setSize(users.size());
            for (int i = 0; i < users.size(); i++) {
                UserSpec us = users.get(i);
                usersVector.set(i, us.getAsHashtable());
            }
        } else
            usersVector.setSize(0);

        Hashtable options = new Hashtable();
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(5);
        argv.set(0, sliceUrn);
        argv.set(1, credentialsVector);
        argv.set(2, rspec);
        argv.set(3, usersVector);
        argv.set(4, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "CreateSliver", argv, methodParams);
        AggregateManagerReply<String> r = new AggregateManagerReply<String>(res);
        log(res, r, "createSliver", "CreateSliver", con, methodParams);
        return r;
    }

    /**
     * Delete any slivers at the given aggregate belonging to the given slice, by stopping the resources if they are
     * still running, and then deallocating the resources associated with the slice. When complete, this slice will own
     * no resources on this aggregate - any such resources will have been stopped.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 2".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param sliceUrn urn for the slice of which delete the sliver at this aggregate
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return a boolean (success or failure), wrapped in a {@code AggregateManager2.AggregateManagerReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Delete any slivers at the given aggregate belonging to the given slice, by stopping the resources if they are " +
            "still running, and then deallocating the resources associated with the slice. When complete, this slice will own " +
            "no resources on this aggregate - any such resources will have been stopped.")
    public AggregateManagerReply<Boolean> deleteSliver(GeniConnection con,
                                                       @ApiMethodParameter(name="credentialList", hint="mandatory list of credentials. This must include a valid credential for the slice.")
                                                            List<GeniCredential> credentialList,
                                                       @ApiMethodParameter(name="sliceUrn", hint="urn for the slice of which delete the sliver at this aggregate")
                                                            String sliceUrn,
                                                       Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(sliceUrn, "sliceUrn");

        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++)
            credentialsVector.set(i, credentialList.get(i).getCredentialXml());

        Hashtable options = new Hashtable();
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, sliceUrn);
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "DeleteSliver", argv, methodParams);
        AggregateManagerReply<Boolean> r = new AggregateManagerReply<Boolean>(res);
        log(res, r, "deleteSliver", "DeleteSliver", con, methodParams);
        return r;
    }

    public static class SliverStatus {
        public static class ResourceStatus {
            private String urn;
            private String status;
            private String error;

            public ResourceStatus(String urn, String status, String error) {
                this.urn = urn;
                this.status = status;
                this.error = error;
            }

            public String getUrn() {
                return urn;
            }

            public String getStatus() {
                return status;
            }

            public String getError() {
                return error;
            }

            @Override
            public String toString() {
                return "ResourceStatus{" +
                        "urn='" + urn + '\'' +
                        ", status='" + status + '\'' +
                        ", error='" + error + '\'' +
                        '}';
            }
        }
        private String urn;
        private String status;
        private List<ResourceStatus> resources;

        private SliverStatus(Hashtable val) throws GeniException {
            urn = (String) val.get("geni_urn");
            status = (String) val.get("geni_status");
            resources = new ArrayList<ResourceStatus>();
            Vector vect = (Vector) val.get("geni_resources");
            if (vect == null) throw new GeniException("Malformed SliverStatus: no geni_resources in: "+ XmlRpcPrintUtil.printXmlRpcResultObject(val));
            for (int i = 0; i < vect.size(); i++) {
                Hashtable resourceHT = (Hashtable) vect.get(i);
                String u = (String) val.get("geni_urn");
                String s = (String) val.get("geni_status");
                String e = (String) val.get("geni_error");
                resources.add(new ResourceStatus(u, s, e));
            }
        }

        public String getUrn() {
            return urn;
        }

        public String getStatus() {
            return status;
        }

        public List<ResourceStatus> getResources() {
            return resources;
        }

        @Override
        public String toString() {
            return "SliverStatus{" +
                    "urn='" + urn + '\'' +
                    ", status='" + status + '\'' +
                    ", resources=" + resources +
                    '}';
        }
    }

    /**
     * Get the status of a sliver or slivers belonging to the given slice at the given aggregate. Status may include
     * other dynamic reservation or instantiation information as required by the resource type and aggregate. This
     * method is used to provide updates on the state of the resources after the completion of CreateSliver, which began
     * to asynchronously provision and start the resources.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 2".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param sliceUrn urn for the slice of which to request the status of the sliver at this aggregate
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return status of the slice, wrapped in a {@code AggregateManager2.AggregateManagerReply}
     * @throws GeniException
     */
    @ApiMethod(hint="Get the status of a sliver or slivers belonging to the given slice at the given aggregate. Status may include " +
            "other dynamic reservation or instantiation information as required by the resource type and aggregate. This " +
            "method is used to provide updates on the state of the resources after the completion of CreateSliver, which began " +
            "to asynchronously provision and start the resources.")
    public AggregateManagerReply<SliverStatus> sliverStatus(GeniConnection con,
                                                            @ApiMethodParameter(name="credentialList", hint="mandatory list of credentials. This must include a valid credential for the slice.")
                                                                    List<GeniCredential> credentialList,
                                                            @ApiMethodParameter(name="sliceUrn", hint="URN for the slice of which to request the status of the sliver at this aggregate")
                                                                    String sliceUrn,
                                                            Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(sliceUrn, "sliceUrn");

        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++)
            credentialsVector.set(i, credentialList.get(i).getCredentialXml());

        Hashtable options = new Hashtable();
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, sliceUrn);
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "SliverStatus", argv, methodParams);

        try {
            SliverStatus resStatus = null;
            if (AggregateManagerReply.isSuccess(res) && res.getResultValueObject() == null)
                throw new GeniSpecificationNonconformityException("SliverStatus", getApiName(), "success but result is completely empty");
            if (AggregateManagerReply.isSuccess(res) && !(res.getResultValueObject() instanceof Hashtable))
                throw new GeniSpecificationNonconformityException("SliverStatus", getApiName(), "success but result is not a struct but "+res.getResultValueObject().getClass().getSimpleName());

            //if not succesfull. SliverStatus on emulab gives an integer... that is wrong! But it should not be handled here
//            if (res.getResultValueObject() != null && !(res.getResultValueObject() instanceof Hashtable))
//                throw new GeniSpecificationNonconformityException("SliverStatus", getApiName(), "result is not a struct but "+res.getResultValueObject().getClass().getSimpleName()+" -> value=\""+res.getResultValueObject()+"\"");

            //if result is not present or NOT a hashtable, do not create a SliverStatus return object
            if (res.getResultValueObject() != null && (res.getResultValueObject() instanceof Hashtable))
                resStatus = new SliverStatus((Hashtable) res.getResultValueObject());

            AggregateManagerReply<SliverStatus> r = new AggregateManagerReply<SliverStatus>(res, resStatus);
            log(res, r, "sliverStatus", "SliverStatus", con, methodParams);
            return r;
        } catch (GeniException e) {
            log(res, new AggregateManagerReply<SliverStatus>(res, null), "sliverStatus", "SliverStatus", con, methodParams);
            throw e;
        }
    }

    /**
     * Renews the resources in all slivers at this aggregate belonging to the given slice until the given time,
     * extending the lifetime of the slice. Aggregates may limit how long reservations may be extended. Initial sliver
     * expiration is set by aggregate policy, no later than the slice credential expiration time.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 2".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param sliceUrn urn for the slice which sliver should be renewed
     * @param expirationTimeRfc3339 new time at which the sliver is to expire, in RFC3339 format. See {@link be.iminds.ilabt.jfed.util.RFC3339Util} for help converting to the correct format.
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return a boolean (success or failure), wrapped in a {@code AggregateManager2.AggregateManagerReply}
     * @throws GeniException
     * @see <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     */
    @ApiMethod(hint="Renews the resources in all slivers at this aggregate belonging to the given slice until the given time, " +
            "extending the lifetime of the slice. Aggregates may limit how long reservations may be extended. Initial sliver " +
            "expiration is set by aggregate policy, no later than the slice credential expiration time.")
    public AggregateManagerReply<Boolean> renewSliver(GeniConnection con,
                                                      @ApiMethodParameter(name="credentialList", hint="mandatory list of credentials. This must include a valid credential for the slice.")
                                                            List<GeniCredential> credentialList,
                                                      @ApiMethodParameter(name="sliceUrn", hint="URN for the slice which sliver should be renewed")
                                                            String sliceUrn,
                                                      @ApiMethodParameter(name="expirationTime", hint="New time at which the sliver is to expire, in RFC3339 format.")
                                                            String expirationTimeRfc3339,
                                                      Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn, "expirationTimeRfc3339", expirationTimeRfc3339);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(expirationTimeRfc3339, "expirationTime");
        checkNonNullArgument(sliceUrn, "sliceUrn");

        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++)
            credentialsVector.set(i, credentialList.get(i).getCredentialXml());

        Hashtable options = new Hashtable();
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(4);
        argv.set(0, sliceUrn);
        argv.set(1, credentialsVector);
        argv.set(2, expirationTimeRfc3339); //date in RFC 3339
        argv.set(3, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "RenewSliver", argv, methodParams);
        AggregateManagerReply<Boolean> r = new AggregateManagerReply<Boolean>(res);
        log(res, r, "renewSliver", "RenewSliver", con, methodParams);
        return r;
    }

    /**
     * Convenience method calling {@link AggregateManager2#renewSliver(be.iminds.ilabt.jfed.lowlevel.GeniConnection, java.util.List, String, String, java.util.Hashtable)},
     * The expiration data is a Java data, which is automatically converted to RFC3339.
     *
     * @param expirationTime a Java data, which is automatically converted to RFC3339
     * @see AggregateManager2#renewSliver(be.iminds.ilabt.jfed.lowlevel.GeniConnection, java.util.List, String, String, java.util.Hashtable)
     */
    public AggregateManagerReply<Boolean> renewSliver(GeniConnection con,
                                                      @ApiMethodParameter(name="credentialList", hint="")
                                                            List<GeniCredential> credentialList,
                                                      @ApiMethodParameter(name="sliceUrn", hint="")
                                                            String sliceUrn,
                                                      @ApiMethodParameter(name="expirationTime", hint="")
                                                            Date expirationTime,
                                                      Hashtable extraOptions) throws GeniException {
        checkNonNullArgument(expirationTime, "expirationTime");

        String expirationTimeRfc3339 = RFC3339Util.dateToRFC3339String(expirationTime);

        return renewSliver(con, credentialList, sliceUrn, expirationTimeRfc3339, extraOptions);
    }

    /**
      * Perform an emergency shut down of a sliver or slivers at this aggregate belonging to the given slice. This
      * operation is intended for administrative use. The sliver is shut down but remains available for further forensics.
      *
      * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
      *            "GENI Aggregate Manager API Version 2".
      * @param credentialList mandatory list of credentials, containing a credential authorizing this shutdown.
      * @param sliceUrn urn for the slice of which shutdown the slivers at this aggregate
      * @param extraOptions optional (not specified = null): Hashtable with extra options.
      * @return a boolean (success or failure), wrapped in a {@code AggregateManager2.AggregateManagerReply}
      * @throws GeniException
      */
    @ApiMethod(hint="Perform an emergency shut down of a sliver or slivers at this aggregate belonging to the given slice. This " +
            "operation is intended for administrative use. The sliver is shut down but remains available for further forensics.")
    public AggregateManagerReply<Boolean> shutdown(GeniConnection con,
                                                   @ApiMethodParameter(name="credentialList", hint="Mandatory list of credentials, containing a credential authorizing this shutdown.")
                                                        List<GeniCredential> credentialList,
                                                   @ApiMethodParameter(name="sliceUrn", hint="URN for the slice of which shutdown the slivers at this aggregate")
                                                        String sliceUrn,
                                                   Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(sliceUrn, "sliceUrn");

        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++)
            credentialsVector.set(i, credentialList.get(i).getCredentialXml());

        Hashtable options = new Hashtable();
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, sliceUrn);
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Shutdown", argv, methodParams);
        AggregateManagerReply<Boolean> r = new AggregateManagerReply<Boolean>(res);
        log(res, r, "shutdown", "Shutdown", con, methodParams);
        return r;
    }

}
