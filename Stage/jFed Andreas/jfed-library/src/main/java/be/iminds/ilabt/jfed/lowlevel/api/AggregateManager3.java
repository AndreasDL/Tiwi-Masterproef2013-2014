package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.lowlevel.ApiMethod;
import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameter;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.DataConversionUtils;
import be.iminds.ilabt.jfed.util.RFC3339Util;

import java.util.*;

/**
 * <p>This is a simple wrapper for the methods specified in the {@link <a href="http://groups.geni.net/geni/wiki/GAPI_AM_API_V3">Geni Aggregate Manager API v3</a>}.
 * <p>All functionality provided by the API is available in this implementation, and little processing of results is done.
 * This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link be.iminds.ilabt.jfed.log.Logger} if it is non null.
 *
 * <p>All methods require a {@link be.iminds.ilabt.jfed.lowlevel.GeniSslConnection} argument and return a {@link be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3.AggregateManagerReply} which wraps the actual return value of the call, if any.
 *
 * @see <a href="http://groups.geni.net/geni/wiki/GAPI_AM_API_V3">Geni Aggregate Manager API v3</a>
 *
 * @author Wim Van de Meerssche
 */
public class AggregateManager3 extends AbstractGeniAggregateManager {
    /**
     * A human readable name for the implemented API
     *
     * @return "Geni Aggregate Manager API v2";
     */
    static public String getApiName() {
        return "Geni Aggregate Manager API v3";
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
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link be.iminds.ilabt.jfed.log.Logger} if it is non null.
     *
     * @param logger the logger to use. May be null if no logger is used.
     * @param autoRetryBusy whether or not to retry when a "busy" reply is received.
     */
    public AggregateManager3(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.AM, 3));
    }
    /**
     * Construct a new AggregateManager2, with a certain logger.
     *
     * <p>This class stores no internal state, except for the logger used. It sends all results and debug info to this {@link be.iminds.ilabt.jfed.log.Logger} if it is non null.
     *
     * <p>This constructor sets autoRetryBusy to true, so "busy" replies are retried.
     *
     * @param logger the logger to use. May be null if no logger is used.
     */
    public AggregateManager3(Logger logger) {
        super(logger, true, new ServerType(ServerType.GeniServerRole.AM, 3));
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
        public static class CredentialType {
            private String type;    //example "geni_sfa"
            private String version; //example "3"

            private CredentialType(Hashtable ht) {
                type = (String) ht.get("geni_type");
                version = (String) ht.get("geni_version");
            }
            private CredentialType(String type, String version) {
                this.type = type;
                this.version = version;
            }

            public String getType() {
                return type;
            }

            public String getVersion() {
                return version;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                CredentialType that = (CredentialType) o;

                if (type != null ? !type.equalsIgnoreCase(that.type) : that.type != null) return false;
                if (version != null ? !version.equalsIgnoreCase(that.version) : that.version != null) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = type != null ? type.toLowerCase().hashCode() : 0;
                result = 31 * result + (version != null ? version.toLowerCase().hashCode() : 0);
                return result;
            }
        }

        private int api;
        private List<VersionPair> apiVersions;
        private List<RspecVersion> requestRspecVersions;
        private List<RspecVersion> adRspecVersions;
        private List <CredentialType> credentialTypes;

        /** can operate on individual slivers */
        private boolean singleSliceAllocation; //geni_single_allocation option, false if ommited
        /**  Can do multiple Allocates */
        private String allocate; //geni_allocate option, "geni_single" if ommited


        public VersionInfo(Hashtable xmlRpcReplyValue) throws GeniException {
            if (xmlRpcReplyValue.get("geni_api") == null) throw new GeniException("GetVersion reply is missing \"geni_api\" integer");
            api = (Integer) xmlRpcReplyValue.get("geni_api");

            Hashtable apiVersionHT = (Hashtable) xmlRpcReplyValue.get("geni_api_versions");
            Vector<Hashtable> requestRspecVersionsV = (Vector<Hashtable>) xmlRpcReplyValue.get("geni_request_rspec_versions");
            Vector<Hashtable> adRspecVersionsV = (Vector<Hashtable>) xmlRpcReplyValue.get("geni_ad_rspec_versions");
            Vector<Hashtable> credentialTypesV = (Vector<Hashtable>) xmlRpcReplyValue.get("geni_credential_types");
            if (apiVersionHT == null) throw new GeniException("GetVersion reply is missing \"geni_api_versions\" list");
            if (requestRspecVersionsV == null) throw new GeniException("GetVersion reply is missing \"geni_request_rspec_versions\" list");
            if (adRspecVersionsV == null) throw new GeniException("GetVersion reply is missing \"geni_ad_rspec_versions\" list");
            if (credentialTypesV == null) throw new GeniException("GetVersion reply is missing \"geni_credential_types\" list");

            this.apiVersions = new ArrayList<VersionPair>();
            this.requestRspecVersions = new ArrayList<RspecVersion>();
            this.adRspecVersions = new ArrayList<RspecVersion>();
            this.credentialTypes = new ArrayList<CredentialType>();

            for (Object k : apiVersionHT.keySet()) {
                String url = (String) k;
                String nr = (String) apiVersionHT.get(k);
                apiVersions.add(new VersionPair(url, nr));
            }
            for (Hashtable h : requestRspecVersionsV)
                requestRspecVersions.add(new RspecVersion(h));
            for (Hashtable h : adRspecVersionsV)
                adRspecVersions.add(new RspecVersion(h));
            for (Hashtable h : credentialTypesV)
                credentialTypes.add(new CredentialType(h));

            Object singleSliceAllocationO = xmlRpcReplyValue.get("geni_single_allocation");
            if (singleSliceAllocationO != null) {
                if (singleSliceAllocationO instanceof Boolean)
                    singleSliceAllocation = (Boolean) singleSliceAllocationO;
                if (singleSliceAllocationO instanceof Integer)
                    singleSliceAllocation = ((Integer) singleSliceAllocationO) == 1;
                if (singleSliceAllocationO instanceof String)
                    singleSliceAllocation = ((String) singleSliceAllocationO).equals("1");
            }

            Object allocateO = xmlRpcReplyValue.get("geni_allocate");
            if (allocateO != null)
                allocate = (String) allocateO;
        }

        public int getApi() {
            return api;
        }
        public List<VersionPair> getApiVersions() {
            return Collections.unmodifiableList(apiVersions);
        }
        public List<RspecVersion> getRequestRspecVersions() {
            return Collections.unmodifiableList(requestRspecVersions);
        }
        public List<RspecVersion> getAdRspecVersions() {
            return Collections.unmodifiableList(adRspecVersions);
        }
        public List<CredentialType> getCredentialTypes() {
            return Collections.unmodifiableList(credentialTypes);
        }
        /**
         * Specification:
         *
         * geni_single_allocation: <XML-RPC boolean 1/0, default 0>: When true (not default),
         * and performing one of (Describe, Allocate, Renew, Provision, Delete), such an AM requires you to
         * include either the slice urn or the urn of all the slivers in the same state. If you attempt to
         * run one of those operations on just some slivers in a given state, such an AM will return an error.
         *
         * For example, at an AM where geni_single_allocation is true you must Provision all geni_allocated slivers
         * at once. If you supply a list of sliver URNs to Provision that is only 'some' of the geni_allocated
         * slivers for this slice at this AM, then the AM will return an error. Similarly, such an aggregate would
         * return an error from Describe if you request a set of sliver URNs that is only some of the geni_provisioned
         * slivers.
         * */
        public boolean isSingleSliceAllocation() {
            return singleSliceAllocation;
        }

        /**
         * Specification:
         *
         * geni_allocate: A case insensitive string, one of fixed set of possible values. Default is geni_single.
         * This option defines whether this AM allows adding slivers to slices at an AM (i.e. calling Allocate()
         * multiple times, without first deleting the allocated slivers). Possible values:
         *       geni_single: Performing multiple Allocates without a delete is an error condition because the
         *                    aggregate only supports a single sliver per slice or does not allow incrementally
         *                    adding new slivers. This is the AM API v2 behavior.
         *       geni_disjoint: Additional calls to Allocate must be disjoint from slivers allocated with previous
         *                      calls (no references or dependencies on existing slivers). The topologies must be
         *                      disjoint in that there can be no connection or other reference from one topology to
         *                      the other.
         *       geni_many: Multiple slivers can exist and be incrementally added, including those which connect or
         *                  overlap in some way. New aggregates should strive for this capability.
         */
        public String getAllocate() {
            return allocate;
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

    /** this is a test, this system could be used for all methods.
     * It guarantees that enough info is logged in the exception and the logger
     *
     * TODO improve this system somehow, so that in all cases, a maximum of debug data is preserved
     * */
    private GeniException handleException(Exception e,
                                          XMLRPCCallDetailsGeni res,
                                          String javaMethodName,
                                          String geniMethodName,
                                          GeniConnection con) {
        GeniException resEx;

        if (e instanceof GeniException) {
            GeniException ge = (GeniException) e;
            ge.setXmlRpcResult(res);
            resEx = ge;
        } else {
            resEx = new GeniException(e.getMessage(), e, res, null);
        }

        try {
            log(res, null, javaMethodName, geniMethodName, con, null);
        } catch (Exception logEx) {
            System.err.println("Exception when logging in handleException (will be completely ignored): "+logEx.getMessage());
        }

        return resEx;
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
     *            "GENI Aggregate Manager API Version 3".
     * @return the version info of the aggregate manager, wrapped in a {@code AggregateManager3.AggregateManagerReply}
     * */
    @ApiMethod(hint="Query static configuration information about this aggregate manager implementation, " +
            "such as API and RSpec versions supported.\n\n" +
            " The result is an XML-RPC struct with at least the following members:\n" +
            "\n" +
            "{\n" +
            "  int geni_api;\n" +
            "  struct code = {\n" +
            "       int geni_code;\n" +
            "       [optional: string am_type;]\n" +
            "       [optional: int am_code;]\n" +
            "         }\n" +
            "  struct value\n" +
            "      {\n" +
            "        int geni_api;\n" +
            "        struct geni_api_versions {\n" +
            "             URL <this API version #>; # value is a URL, name (key) is a number represented as a string\n" +
            "             [optional: other supported API versions and the URLs where they run]\n" +
            "        }\n" +
            "        array geni_request_rspec_versions of {\n" +
            "             string type; # case insensitive\n" +
            "             string version; # case insensitive\n" +
            "             string schema;\n" +
            "             string namespace;\n" +
            "             array extensions of string;\n" +
            "        };\n" +
            "        array geni_ad_rspec_versions of {\n" +
            "             string type; # case insensitive\n" +
            "             string version; # case insensitive\n" +
            "             string schema;\n" +
            "             string namespace;\n" +
            "             array extensions of string;\n" +
            "        };\n" +
            "        array geni_credential_types of {\n" +
            "             string geni_type <case insensitive>;\n" +
            "             string geni_version <containing an integer>;\n" +
            "       };\n" +
            "       [optional: boolean geni_single_allocation; <optional only if using default of 0>]\n" +
            "       [optional: string geni_allocate; <case insensitive. optional only if using default of geni_single>]\n" +
            "      }\n" +
            "  string output;\n" +
            "}\n")
    public AggregateManagerReply<VersionInfo> getVersion(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetVersion", new Vector(), null);

        try {
            AggregateManagerReply<VersionInfo> r;
            if (!AggregateManagerReply.isSuccess(res))
                r = new AggregateManagerReply<VersionInfo>(res, null);
            else
                r = new AggregateManagerReply<VersionInfo>(res, new VersionInfo((Hashtable) res.getResultValueObject()));
            log(res, r, "getVersion", "GetVersion", con, null);
            return r;
        } catch (Exception e) {
            GeniException newE = handleException(e, res, "getVersion", "GetVersion", con);
            throw newE;
        }
    }

    private static void addHashtables(Hashtable target, Hashtable source) {
        if (source == null)
            return;
        for (Object o : source.keySet())
            target.put(o, source.get(o));
    }

    /**
     * Return a listing and description of available resources at this aggregate. The resource listing and description
     * provides sufficient information for clients to select among available resources. These listings are known as
     * advertisement RSpecs.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 3".
     * @param credentialList mandatory list of credentials. This must include a valid user credential.
     * @param rspecType mandatory rspec type (example: "geni")
     * @param rspecVersion mandatory rspec version (example for type "geni": "3")
     * @param available optional (not specified = null): if true, show only available local resources, otherwise show
     *                  everything
     * @param compressed optional (not specified = null): if true compress result (RFC 1950).      NOTE: if compressed
     *                   is set, this method will decompress the result automatically.
     * @return requested rspec, wrapped in a {@code AggregateManager3.AggregateManagerReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="Return a listing and description of available resources at this aggregate. " +
            "The resource listing and description provides sufficient information for clients to select among " +
            "available resources. These listings are known as advertisement RSpecs.\n\n" +
            "Return: On success, the value field of the return struct will contain: A geni.rspec advertisment RSpec.")
    public AggregateManagerReply<String> listResources(GeniConnection con,
                                                       @ApiMethodParameter(name="credentialList",
                                                               hint="When using SFA style credentials, this list must include a valid user credential, granting rights to the caller of the method.")
                                                                    List<GeniCredential> credentialList,
                                                       @ApiMethodParameter(name="rspecType", guiDefault="geni", hint="Required. An XML-RPC struct indicating the type and version of Advertisement RSpec to return. The struct contains 2 members, type and version. type and version are case-insensitive strings, matching those in geni_ad_rspec_versions as returned by GetVersion at this aggregate. This option is required, and aggregates are expected to return a geni_code of 1 (BADARGS) if it is missing. Aggregates should return a geni_code of 4 (BADVERSION) if the requested RSpec version is not one advertised as supported in GetVersion.")
                                                                String rspecType,
                                                       @ApiMethodParameter(name="rspecVersion", guiDefault="3", hint="See help for \"rspecType\"")
                                                                String rspecVersion,
                                                       @ApiMethodParameter(name="available", required=false,
                                                               hint="Optional. An XML-RPC boolean value indicating whether the caller is interested in all resources or available resources. If this value is true (1), the result should contain only available resources. If this value is false (0) or unspecified, both available and allocated resources should be returned. The Aggregate Manager is free to limit visibility of certain resources based on the credentials parameter. ")
                                                                Boolean available,
                                                       @ApiMethodParameter(name="compressed", required=false, guiDefaultOptional=true, guiDefault="true", hint="Optional. An XML-RPC boolean value indicating whether the caller would like the result to be compressed. If the value is true (1), the returned resource list will be compressed according to RFC 1950. If the value is false (0) or unspecified, the return will be text.   Note that this software client implementation automatically decompresses the result when this option is used.")
                                                                Boolean compressed,
                                                       Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "rspecType", rspecType, "rspecVersion", rspecVersion);
        if (available != null) methodParams.put("available", available);
        if (compressed != null) methodParams.put("compressed", compressed);


        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(rspecType, "rspecType");
        checkNonNullArgument(rspecVersion, "rspecVersion");

        Hashtable rspecVersionOpt = new Hashtable();
        rspecVersionOpt.put("type", rspecType);
        rspecVersionOpt.put("version", rspecVersion);

        Hashtable options = new Hashtable();
        options.put("geni_rspec_version", rspecVersionOpt);
        if (available != null)
            options.put("geni_available", available.booleanValue());
        if (compressed != null)
            options.put("geni_compressed", compressed.booleanValue());
        addHashtables(options, extraOptions);

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Vector argv = new Vector();
        argv.setSize(2);
        argv.set(0, credentialsVector);
        argv.set(1, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "ListResources", argv, methodParams);

        AggregateManagerReply<String> r;
        if (compressed != null && compressed) {
            String decompressed_value = DataConversionUtils.decompressFromBase64(res.getResultValueString());
            r = new AggregateManagerReply<String>(res, decompressed_value);
        }
        else
            r = new AggregateManagerReply<String>(res);
        log(res, r, "listResources", "ListResources", con, methodParams);
        return r;
    }

    private static void sliverInfoGeniSpecificationNonconformityCheck(Hashtable ht, String key, Class type, boolean optional) throws GeniSpecificationNonconformityException {
        Object h = ht.get(key);
        if (h == null && optional) return;
        if (h == null)
            throw new GeniSpecificationNonconformityException("Result SliverInfo", getApiName(),
                    "\"geni_rspec\" must be String, but is empty");
        if (!(h instanceof String))
            throw new GeniSpecificationNonconformityException("Result SliverInfo", getApiName(),
                    "\"geni_rspec\" must be String, not "+ h.getClass().getName());
    }

    public class SliverInfo {
        private String sliverUrn, expires, allocationStatus, operationalStatus, error;
        public SliverInfo(Hashtable sliverHt) throws GeniSpecificationNonconformityException {
            sliverInfoGeniSpecificationNonconformityCheck(sliverHt, "geni_sliver_urn", String.class, false);
            sliverInfoGeniSpecificationNonconformityCheck(sliverHt, "geni_expires", String.class, false);
            sliverInfoGeniSpecificationNonconformityCheck(sliverHt, "geni_allocation_status", String.class, false);
            sliverInfoGeniSpecificationNonconformityCheck(sliverHt, "geni_operational_status", String.class, true);
            sliverInfoGeniSpecificationNonconformityCheck(sliverHt, "geni_error", String.class, true);

            sliverUrn = (String) sliverHt.get("geni_sliver_urn");
            expires = (String) sliverHt.get("geni_expires"); //rfc3339 date
            allocationStatus = (String) sliverHt.get("geni_allocation_status");   //sliver state - e.g. geni_allocated or geni_provisioned

            if (sliverHt.get("geni_operational_status") != null)
                operationalStatus = (String) sliverHt.get("geni_operational_status");  //sliver operational state
            else
                operationalStatus = null;

            if (sliverHt.get("geni_error") != null)
                error = (String) sliverHt.get("geni_error"); //optional
            else
                error = null;
        }
        public String getSliverUrn() {
            return sliverUrn;
        }
        public String getExpires() {
            return expires;
        }
        public String getAllocationStatus() {
            return allocationStatus;
        }
        public String getOperationalStatus() {
            return operationalStatus;
        }
        public String getError() {
            return error;
        }
    }

    public class ManifestInfo {
        private String manifestRspec;
        private String sliceUrn;
        private List<SliverInfo> sliverInfos;

        public ManifestInfo(Hashtable ht, Boolean compressed) throws GeniSpecificationNonconformityException {
            if (ht.get("geni_urn") != null) {
                sliceUrn = (String) ht.get("geni_urn");
                manifestRspec = (String) ht.get("geni_rspec");

                if (compressed != null && compressed) {
                    String decompressed_value = DataConversionUtils.decompressFromBase64(manifestRspec);
                    manifestRspec = decompressed_value;
                }
            } else {
                sliceUrn = null;
                manifestRspec = null;
            }

            sliverInfos = new ArrayList<SliverInfo>();
            Vector sliverInfos = (Vector) ht.get("geni_slivers");
            for (Object sliverInfo : sliverInfos) {
                Hashtable sliverHt = (Hashtable) sliverInfo;
                SliverInfo si = new SliverInfo(sliverHt);
                sliverInfos.add(si);
            }
        }

        public String getManifestRspec() {
            return manifestRspec;
        }
        public String getSliceUrn() {
            return sliceUrn;
        }
        public List<SliverInfo> getSliverInfos() {
            return Collections.unmodifiableList(sliverInfos);
        }
    }
    /**
     *Retrieve a manifest RSpec describing the resources contained by the named entities, e.g. a single slice or a set
     * of the slivers in a slice. This listing and description should be sufficiently descriptive to allow experimenters
     * to use the resources.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 3".
     * @param urns urns of which manfiest RSpec is requested
     * @param credentialList mandatory list of credentials. This should a valid slice credential for the urns provided
     * @param rspecType mandatory rspec type (example: "geni")
     * @param rspecVersion mandatory rspec version (example for type "geni": "3")
     * @param compressed optional (not specified = null): if true compress result (RFC 1950).      NOTE: if compressed
     *                   is set, this method will decompress the result automatically.
     * @return requested rspec, wrapped in a {@code AggregateManager3.AggregateManagerReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="Retrieve a manifest RSpec describing the resources contained by the named entities, e.g. a single " +
            "slice or a set of the slivers in a slice. This listing and description should be sufficiently descriptive " +
            "to allow experimenters to use the resources. \n\nThis method is part of what ListResources used to do in " +
            "previous versions of the AM API, and is similar to ProtoGENI `Resolve`.\n\n" +
            "The geni_single_allocation return from GetVersion advertises whether or not a client may invoke this method " +
            "on only some of the slivers in a given geni_allocation_state in a given slice (default is false - the client " +
            "may operate on only some of the slivers in a given state).\n\n" +
            "Note that the manifest RSpec for allocated slivers may contain less detail than for provisioned slivers. " +
            "Aggregates are expected to combine the manifests of all requested slivers into a single manifest RSpec. " +
            "Note that a manifest returned here for only some of the slivers in a slice at this aggregate may contain " +
            "references to resources not described in this manifest because they are in other slivers.\n" +
            "\n" +
            "Manifests are not necessarily static. In general, the manifest of a given sliver should be static once it " +
            "has reached the operational state geni_ready (e.g., fully booted). However, this API does not require that " +
            "to be true.\n\n" +
            "Return: On success, the value field of the return struct will contain a struct:\n" +
            "\n" +
            "{\n" +
            "   geni_rspec: <geni.rspec, a Manifest RSpec>\n" +
            "   geni_urn: <string slice urn of the containing slice>\n" +
            "   geni_slivers: [\n" +
            "               {\n" +
            "                  geni_sliver_urn: <string sliver urn>\n" +
            "                  geni_expires: <dateTime.rfc3339 allocation expiration string, as in geni_expires from SliversStatus>,\n" +
            "                  geni_allocation_status: <string sliver state - e.g. geni_allocated or geni_provisioned >,\n" +
            "                  geni_operational_status: <string sliver operational state>,\n" +
            "                  geni_error: <optional string. The field may be omitted entirely but may not be null/None, explaining any failure for a sliver.>\n" +
            "               },\n" +
            "               ...\n" +
            "         ]\n" +
            "}\n")
    public AggregateManagerReply<ManifestInfo> describe(GeniConnection con,
                                                        @ApiMethodParameter(name="urns", hint="The entities to be described, e.g. a single slice or a set of the slivers in a slice.\n\nIf a slice urn is supplied and there are no slivers in the given slice at this aggregate, then geni_rspec shall be a valid manifest RSpec, containing zero (0) node or link elements - that is, specifying no resources. geni_slivers may be an empty array, or may be an array of previous slivers that have since been deleted or expired. Calling Describe on one or more sliver URNs that are unknown, deleted or expired shall result in an error (e.g. SEARCHFAILED, EXPIRED or ERROR geni_code). "+"Several methods take some URNs to identify what to operate on. These methods are defined as accepting a list of arbitrary strings called URNs, which follow the GENI identifier rules. This API defines two kinds of URNs that may be supplied here, slice URNs and sliver URNs (see the GENI identifiers page). Some aggregates may understand other URNs, but these are not defined or required here. Aggregates that accept only URNs defined by this API will return an error when given URNs not in one of those forms. This API requires that aggregates accept either a single slice URN, or 1 or more sliver URNs that all belong to the same slice. Aggregates are not required to accept both a slice URN and sliver URNs, 2 or more slice URNs, or a set of sliver URNs that crosses multiple slices. Some aggregates may choose to accept other such combinations of URNs. Aggregates that accept only arguments defined by this API will return an error when given more than 1 slice URN, a combination of both slice and sliver URNs, or a set of sliver URNs that belong to more than 1 slice.\n\nIf the urns[] list includes a set of sliver URNs, then the AM shall apply the method to all listed slivers. If the operation fails on one or more of the slivers for any reason, then the whole method fails with an appropriate error code, unless geni_best_effort is true and supported.")
                                                                List<String> urns,
                                                        @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                                List<GeniCredential> credentialList,
                                                        @ApiMethodParameter(name="rspecType", guiDefault="geni", hint="Required. An XML-RPC struct indicating the type and version of Advertisement RSpec to return. The struct contains 2 members, type and version. type and version are case-insensitive strings, matching those in geni_ad_rspec_versions as returned by GetVersion at this aggregate. This option is required, and aggregates are expected to return a geni_code of 1 (BADARGS) if it is missing. Aggregates should return a geni_code of 4 (BADVERSION) if the requested RSpec version is not one advertised as supported in GetVersion.")
                                                                String rspecType,
                                                        @ApiMethodParameter(name="rspecVersion", guiDefault="3", hint="see \"rspecType\"")
                                                                String rspecVersion,
                                                        @ApiMethodParameter(name="compressed", required=false, guiDefaultOptional=true, guiDefault="true", hint="Optional. An XML-RPC boolean value indicating whether the caller would like the result to be compressed. If the value is true (1), the returned resource list will be compressed according to RFC 1950. If the value is false (0) or unspecified, the return will be text.   Note that this software client implementation automatically decompresses the result when this option is used.")
                                                                Boolean compressed,
                                                        Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "rspecType", rspecType, "rspecVersion", rspecVersion);
        methodParams.put("urns", urns);
        if (compressed != null) methodParams.put("compressed", compressed);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(rspecType, "rspecType");
        checkNonNullArgument(rspecVersion, "rspecVersion");

        Hashtable rspecVersionOpt = new Hashtable();
        rspecVersionOpt.put("type", rspecType);
        rspecVersionOpt.put("version", rspecVersion);

        Hashtable options = new Hashtable();
        options.put("geni_rspec_version", rspecVersionOpt);
        if (compressed != null)
            options.put("geni_compressed", compressed.booleanValue());
        addHashtables(options, extraOptions);

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, new Vector<String>(urns));
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Describe", argv, methodParams);

        AggregateManagerReply<ManifestInfo> nullRes = new AggregateManagerReply<ManifestInfo>(res, null);
        if (!nullRes.getGeniResponseCode().isSuccess()) {
            log(res, nullRes, "describe", "Describe", con, methodParams);
            return nullRes;
        }

        ManifestInfo manifestInfo = new ManifestInfo((Hashtable) res.getResultValueObject(), compressed);
        AggregateManagerReply<ManifestInfo> r = new AggregateManagerReply<ManifestInfo>(res, manifestInfo);
        log(res, r, "describe", "Describe", con, methodParams);
        return r;
    }


    public class AllocateAndProvisionInfo {
        private String rspec;
        private List<SliverInfo> sliverInfo;
        public AllocateAndProvisionInfo(Hashtable ht) throws GeniSpecificationNonconformityException {

            Object h = ht.get("geni_rspec");
            if (h == null || !(h instanceof String))
                throw new GeniSpecificationNonconformityException("Result AllocateAndProvisionInfo", getApiName(),
                        "\"geni_rspec\" must be String, not "+(h == null ? "null" : h.getClass().getName()));

            rspec = (String) h;

            h = ht.get("geni_slivers");
            Vector sliverV = null;
            if (h != null) {
                if (!(h instanceof Vector))
                    throw new GeniSpecificationNonconformityException("Result AllocateAndProvisionInfo", getApiName(), "\"geni_slivers\" must be Vector, not "+h.getClass().getName());
                sliverInfo = new ArrayList<SliverInfo>();
                sliverV = (Vector) h;
                for (Object o : sliverV) {
                    if (o instanceof Hashtable) {
                        Hashtable sht = (Hashtable) o;
                        SliverInfo si = new SliverInfo(sht);
                        sliverInfo.add(si);
                    } else
                        throw new GeniSpecificationNonconformityException("Result AllocateAndProvisionInfo", getApiName(), "Vector must contain Hashtable, not "+o.getClass().getName());
                }
            } else
                throw new GeniSpecificationNonconformityException("Result AllocateAndProvisionInfo", getApiName(), "there is no vector \"geni_slivers\"");
        }

        public String getRspec() {
            return rspec;
        }
        public List<SliverInfo> getSliverInfo() {
            return Collections.unmodifiableList(sliverInfo);
        }
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
     *            "GENI Aggregate Manager API Version 3".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param sliceUrn urn for the slice on which this sliver needs to be created
     * @param rspec An RSpec matching the GENI standard request RSpec schema containing the resources that the caller is
     *              requesting for allocation to the slice specified in slice_urn.
     * @param endTime optional (= may be null) end time in RFC 3339 format
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return The manifest Rspec for the created sliver, wrapped in a {@code AggregateManager3.AggregateManagerReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     * @see <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     */
    @ApiMethod(hint="Allocate resources as described in a request RSpec argument to a slice with the named URN. " +
            "On success, one or more slivers are allocated, containing resources satisfying the request, and assigned " +
            "to the given slice. This method returns a listing and description of the resources reserved for the slice " +
            "by this operation, in the form of a manifest RSpec. Allocated slivers are held for an aggregate-determined " +
            "period. Clients must Renew or Provision slivers before the expiration time (given in the return struct), " +
            "or the aggregate will automatically Delete them. Aggregates should implement Allocate() as quick, cheap, " +
            "and not impacting provisioned resources, such that it can be readily undone. Allocate is an all or nothing " +
            "request: if the aggregate cannot completely satisfy the request RSpec, it should fail the request entirely.\n\n" +
            "This is the first part of what CreateSliver used to do in previous versions of the AM API. The second " +
            "part is now done by Provision, and the final part is done by PerformOperationalAction. See above for an " +
            "overview of this process.\n\n" +
            "This operation is similar to ProtoGENI's GetTicket operation.\n\n" +
            "The geni_allocate return from \"GetVersion\" advertises when a client may legally call Allocate (only " +
            "once at a time per slice, whenever desired, or multiple times only if the requested resources do not interact).\n\n" +
            "Return: On success, the value field of the return struct will contain a struct:\n" +
            "\n" +
            "{\n" +
            " geni_rspec: <geni.rspec manifest of newly allocated slivers>,\n" +
            " geni_slivers: [\n" +
            "        {\n" +
            "                  geni_sliver_urn: <string sliver urn>\n" +
            "                  geni_expires: <dateTime.rfc3339 allocation expiration string, as in geni_expires from Status>,\n" +
            "                  geni_allocation_status: <string sliver state - e.g. geni_allocated>\n" +
            "        },\n" +
            "        ...\n" +
            "    ]\n" +
            "}\n")
    public AggregateManagerReply<AllocateAndProvisionInfo> allocate(GeniConnection con,
                                                                    @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                                            List<GeniCredential> credentialList,
                                                                    @ApiMethodParameter(name="sliceUrn", hint="The URN of the slice to which the resources specified in rspec will be allocated.")
                                                                            String sliceUrn,
                                                                    @ApiMethodParameter(name="rspec", hint="An RSpec matching the GENI standard request RSpec schema containing the resources that the caller is requesting for allocation to the slice specified in slice_urn.")
                                                                            String rspec,
                                                                    @ApiMethodParameter(name="endTime", required=false, hint="Requested expiration of all new slivers, may be ignored by aggregates. Date must be in RFC3339 format.")
                                                                            String endTime,
                                                                    Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn, "rspec", rspec);
        if (endTime != null) methodParams.put("endTime", endTime);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(sliceUrn, "sliceUrn");
        checkNonNullArgument(rspec, "rspec");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Hashtable options = new Hashtable();
        if (endTime != null)
            options.put("geni_end_time", endTime);
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(4);
        argv.set(0, sliceUrn);
        argv.set(1, credentialsVector);
        argv.set(2, rspec);
        argv.set(3, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Allocate", argv, methodParams);

        AggregateManagerReply<AllocateAndProvisionInfo> nullRes = new AggregateManagerReply<AllocateAndProvisionInfo>(res, null);
        if (!nullRes.getGeniResponseCode().isSuccess()) {
            log(res, nullRes, "allocate", "Allocate", con, methodParams);
            return nullRes;
        }

        AllocateAndProvisionInfo allocateInfo = new AllocateAndProvisionInfo((Hashtable) res.getResultValueObject());
        AggregateManagerReply<AllocateAndProvisionInfo> r = new AggregateManagerReply<AllocateAndProvisionInfo>(res, allocateInfo);
        log(res, r, "allocate", "Allocate", con, methodParams);
        return r;
    }

    /**
     * Request that the named geni_allocated slivers be made geni_provisioned, instantiating or otherwise realizing the
     * resources, such that they have a valid geni_operational_status and may possibly be made geni_ready for
     * experimenter use. This operation is synchronous, but may start a longer process, such as creating and imaging
     * a virtual machine.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 3".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param endTime optional (= may be null) end time in RFC 3339 format
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return The manifest Rspec for the created sliver, wrapped in a {@code AggregateManager3.AggregateManagerReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     * @see <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     */
    @ApiMethod(hint=" Request that the named geni_allocated slivers be made geni_provisioned, instantiating or " +
            "otherwise realizing the resources, such that they have a valid geni_operational_status and may possibly " +
            "be made geni_ready for experimenter use. This operation is synchronous, but may start a longer process, " +
            "such as creating and imaging a virtual machine.\n\n" +
            "This operation is part of what CreateSliver used to do. The first part of what CreateSliver did is now " +
            "in Allocate(). Note that resources are not necessarily ready for experimenter use after the work that " +
            "this function initiates finally completes. Consult the geni_operational_status, and the advertised " +
            "operational state machine. Consider calling PerformOperationalAction, e.g. with the command name " +
            "geni_start.\n\n" +
            "The operation is similar to ProtoGENI's RedeemTicket method.\n\n" +
            "Note that at some aggregates and for some resource types, this operation may be a no-op. At other " +
            "aggregates, this operation starts a long running process (e.g. loading an image on a machine and booting " +
            "it). Tools should monitor the sliver status (by calling Status), looking for an operational state other " +
            "than geni_pending_allocation. Depending on the resource type, that next state may differ. See the " +
            "advertisement RSpec for the resource type specific operational states and actions.\n\n" +
            "As with the Allocate method, some aggregates may not support provisioning only some reserved resources. " +
            "Also as with the Allocate method, experimenters may request a sliver expiration time; aggregates may " +
            "allow the operation while ignoring the requested expiration time or granting a different expiration time.\n\n" +
            "Note that previously allocated slivers may have expired (been deleted) by the time you call Provision.\n\n" +
            "Return: On success, the value field of the return struct will contain a struct:\n" +
            "\n" +
            "  geni_rspec: <geni.rspec, RSpec manifest>,\n" +
            "  geni_slivers: \n" +
            "  [\n" +
            "    {\n" +
            "     geni_sliver_urn: <string>,\n" +
            "     geni_allocation_status: <string>,\n" +
            "     geni_operational_status: <string>,\n" +
            "     geni_expires <dateTime.rfc3339 when the sliver expires from its current state>,\n" +
            "     geni_error: <optional string. The field may be omitted entirely but may not be null/None, explaining any failure to Provision this sliver.>\n" +
            "    },\n" +
            "    ...\n" +
            "  ],\n" +
            "\n" +
            "The returned manifest must be in the format specified by the geni_rspec_version option. The returned manifest covers only newly provisioned slivers. Use Describe to get a manifest of all provisioned slivers. When geni_best_effort is true, all requested slivers are returned, but some slivers may have failed (geni_allocation_status will remain geni_allocated). Check geni_error for details. Attempting to Provision an unknown or expired sliver when geni_best_effort is false shall result in an error (SEARCHFAILED or EXPIRED or ERROR geni_code). Attempting to Provision a slice (no slivers identified) with no current slivers at this aggregate shall return an error (SEARCHFAILED).\n" +
            "\n")
    public AggregateManagerReply<AllocateAndProvisionInfo> provision(GeniConnection con,
                                                                     @ApiMethodParameter(name="urns", hint="When only a slice URN is supplied (no specific sliver URNs), this method applies only to the slivers currently in the geni_allocated allocation state. "+"Several methods take some URNs to identify what to operate on. These methods are defined as accepting a list of arbitrary strings called URNs, which follow the GENI identifier rules. This API defines two kinds of URNs that may be supplied here, slice URNs and sliver URNs (see the GENI identifiers page). Some aggregates may understand other URNs, but these are not defined or required here. Aggregates that accept only URNs defined by this API will return an error when given URNs not in one of those forms. This API requires that aggregates accept either a single slice URN, or 1 or more sliver URNs that all belong to the same slice. Aggregates are not required to accept both a slice URN and sliver URNs, 2 or more slice URNs, or a set of sliver URNs that crosses multiple slices. Some aggregates may choose to accept other such combinations of URNs. Aggregates that accept only arguments defined by this API will return an error when given more than 1 slice URN, a combination of both slice and sliver URNs, or a set of sliver URNs that belong to more than 1 slice.\n\nIf the urns[] list includes a set of sliver URNs, then the AM shall apply the method to all listed slivers. If the operation fails on one or more of the slivers for any reason, then the whole method fails with an appropriate error code, unless geni_best_effort is true and supported.")
                                                                            List<String> urns,
                                                                     @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                                            List<GeniCredential> credentialList,
                                                                     @ApiMethodParameter(name="rspecType", guiDefault="geni", hint="Required. An XML-RPC struct indicating the type and version of Advertisement RSpec to return. The struct contains 2 members, type and version. type and version are case-insensitive strings, matching those in geni_ad_rspec_versions as returned by GetVersion at this aggregate. This option is required, and aggregates are expected to return a geni_code of 1 (BADARGS) if it is missing. Aggregates should return a geni_code of 4 (BADVERSION) if the requested RSpec version is not one advertised as supported in GetVersion.")
                                                                            String rspecType,
                                                                     @ApiMethodParameter(name="rspecVersion", guiDefault="3", hint="see \"rspecType\"")
                                                                            String rspecVersion,
                                                                     @ApiMethodParameter(name="bestEffort", required=false, hint="Do all slivers fail if any single sliver fails?")
                                                                            Boolean bestEffort,
                                                                     @ApiMethodParameter(name="endTime", required=false, hint="Requested sliver expiration time. Date must be in RFC3339 format.")
                                                                            String endTime,
                                                                     @ApiMethodParameter(name="users", required=false, hint="Resource login information.  Clients may omit this option. Aggregates should honor this option for any resource that accepts the provided login keys, and ignore it for other resources. This option is an array of user structs, which contain information about the users that might login to the sliver that the AM needs to know about. For example, this option is the mechanism by which users supply their SSH public keys, permitting SSH login to allocated nodes. In such cases, the corresponding manifest RSpec will contain the ssh-users element on each such node, showing the login username and applicable public keys. Aggregates accepting this option for a resource are expected to install all supplied SSH keys - creating separate login accounts for each supplied user if possible. When this option is supplied, each struct must include the key keys, which is an array of strings and can be empty. The struct must also include the key urn, which is the user’s URN string. For example:\n\n[\n  {\n    urn: urn:publicid:IDN+geni.net:gcf+user+alice\n    keys: [<ssh key>, <ssh key>]\n  },\n  {\n    urn: urn:publicid:IDN+geni.net:gcf+user+bob\n    keys: [<ssh key>]\n  }\n]\n")
                                                                            Collection<UserSpec> users,
                                                                     Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "rspecType", rspecType, "rspecVersion", rspecVersion);
        methodParams.put("urns", urns);
        if (bestEffort != null) methodParams.put("bestEffort", bestEffort);
        if (endTime != null) methodParams.put("endTime", endTime);
        if (users != null) methodParams.put("users", users);

        checkNonNullArgument(urns, "urns");
        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(rspecType, "rspecType");
        checkNonNullArgument(rspecVersion, "rspecVersion");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);


        Hashtable options = new Hashtable();
        if (endTime != null)
            options.put("geni_end_time", endTime);
        if (bestEffort != null)
            options.put("geni_best_effort", bestEffort);
        if (users != null && !users.isEmpty()) {
            Vector userVect = new Vector();
            for (UserSpec u : users)
                userVect.add(u.getAsHashtable());
            options.put("geni_users", userVect);
        }
        Hashtable geniRspecVersion = new Hashtable();
        geniRspecVersion.put("type", rspecType);
        geniRspecVersion.put("version", rspecVersion);
        options.put("geni_rspec_version", geniRspecVersion);
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, new Vector(urns));
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Provision", argv, methodParams);

        AggregateManagerReply<AllocateAndProvisionInfo> nullRes = new AggregateManagerReply<AllocateAndProvisionInfo>(res, null);
        if (!nullRes.getGeniResponseCode().isSuccess()) {
            log(res, nullRes, "provision", "Provision", con, methodParams);
            return nullRes;
        }

        AllocateAndProvisionInfo allocateInfo = new AllocateAndProvisionInfo((Hashtable) res.getResultValueObject());
        AggregateManagerReply<AllocateAndProvisionInfo> r = new AggregateManagerReply<AllocateAndProvisionInfo>(res, allocateInfo);
        log(res, r, "provision", "Provision", con, methodParams);
        return r;
    }

    public class StatusInfo {
        private String sliceUrn;
        private List<SliverInfo> sliverInfo;
        public StatusInfo(Hashtable ht) throws GeniSpecificationNonconformityException {
            sliceUrn = (String) ht.get("geni_urn");

            sliverInfo = new ArrayList<SliverInfo>();
            Vector sliverV = (Vector) ht.get("geni_slivers");
            for (Object o : sliverV) {
                Hashtable sht = (Hashtable) o;
                SliverInfo si = new SliverInfo(sht);
                sliverInfo.add(si);
            }
        }

        public String getSliceUrn() {
            return sliceUrn;
        }
        public List<SliverInfo> getSliverInfo() {
            return Collections.unmodifiableList(sliverInfo);
        }
    }
    @ApiMethod(hint=" Get the dynamic status of a sliver or slivers belonging to a single slice at the given aggregate. Status may include other dynamic reservation or instantiation information as required by the resource type and aggregate. This method is used to provide updates on the state of the resources after the completion of Provision, which began to asynchronously provision the resources. This should be relatively dynamic data, not descriptive data as returned in the manifest RSpec.\n\n" +
            "In contrast to Describe, Status is used to query dynamic state information about slivers. Aggregates may include detailed configuration information at their own discretion. " +
            "This operation used to be called SliverStatus in earlier versions of the AM API. geni_slivers has replaced geni_resources and geni_sliver_urn replaces geni_urn. geni_status is replaced with 2 fields, geni_allocation_status and geni_operational_status.\n\n" +
            "This method has no required options.\n\n" +
            "Return: On success, the value field of the return struct will contain a struct:\n\n" +
            "{\n" +
            "  geni_urn: <slice URN>\n" +
            "  geni_slivers: [ \n" +
            "                    { geni_sliver_urn: <sliver URN>\n" +
            "                      geni_allocation_status: <string, eg provisioned>\n" +
            "                      geni_operational_status: <string, eg ready>\n" +
            "                      geni_expires: <dateTime.rfc3339 of individual sliver expiration>\n" +
            "                      geni_error: <string, eg '' - not null/None and not optional>,\n" +
            "                     },\n" +
            "                    { geni_sliver_urn: <sliver URN>\n" +
            "                      geni_allocation_status: <string, eg provisioned>\n" +
            "                      geni_operational_status: <string, eg ready>\n" +
            "                      geni_expires: <dateTime.rfc3339 of individual sliver expiration>\n" +
            "                      geni_error: <string, eg '' - not null/None and not optional>,\n" +
            "                      }\n" +
            "                  ]\n" +
            "}\n\n" +
            "Note that aggregates may return other information, such as details on sliver contents, etc.\n\n" +
            "Calling Status() on an unknown, deleted or expired sliver (by explicit URN) shall result in an error (e.g. SEARCHFAILED, EXPIRED or ERROR) (unless geni_best_effort is true, in which case the method may succeed, but return a geni_error for each sliver that failed). Attempting to get Status() for a slice (no slivers identified) with no current slivers at this aggregate may return an empty list for geni_slivers, may return a list of previous slivers that have since been deleted, or may even return an error (e.g. SEARCHFAILED or EXPIRED). Note therefore that geni_slivers may be an empty list. ")
    public AggregateManagerReply<StatusInfo> status(GeniConnection con,
                                                    @ApiMethodParameter(name="urns", hint="The target of which to retrieve the status. This can be either the URNs of one or more slivers in a given slice, or the URN of the slice itself (to get the status of all slivers)."+"Several methods take some URNs to identify what to operate on. These methods are defined as accepting a list of arbitrary strings called URNs, which follow the GENI identifier rules. This API defines two kinds of URNs that may be supplied here, slice URNs and sliver URNs (see the GENI identifiers page). Some aggregates may understand other URNs, but these are not defined or required here. Aggregates that accept only URNs defined by this API will return an error when given URNs not in one of those forms. This API requires that aggregates accept either a single slice URN, or 1 or more sliver URNs that all belong to the same slice. Aggregates are not required to accept both a slice URN and sliver URNs, 2 or more slice URNs, or a set of sliver URNs that crosses multiple slices. Some aggregates may choose to accept other such combinations of URNs. Aggregates that accept only arguments defined by this API will return an error when given more than 1 slice URN, a combination of both slice and sliver URNs, or a set of sliver URNs that belong to more than 1 slice.\n\nIf the urns[] list includes a set of sliver URNs, then the AM shall apply the method to all listed slivers. If the operation fails on one or more of the slivers for any reason, then the whole method fails with an appropriate error code, unless geni_best_effort is true and supported.")
                                                            List<String> urns,
                                                    @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                            List<GeniCredential> credentialList,
                                                    @ApiMethodParameter(name="bestEffort", required=false, hint="Calling Status() on an unknown, deleted or expired sliver (by explicit URN) shall result in an error (e.g. SEARCHFAILED, EXPIRED or ERROR) (unless geni_best_effort is true, in which case the method may succeed, but return a geni_error for each sliver that failed). Attempting to get Status() for a slice (no slivers identified) with no current slivers at this aggregate may return an empty list for geni_slivers, may return a list of previous slivers that have since been deleted, or may even return an error (e.g. SEARCHFAILED or EXPIRED). Note therefore that geni_slivers may be an empty list. ")
                                                            Boolean bestEffort,
                                                    Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "urns", urns);
        if (bestEffort != null) methodParams.put("bestEffort", bestEffort);

        checkNonNullArgument(urns, "urns");
        checkNonNullArgument(credentialList, "credentialList");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Hashtable options = new Hashtable();
        if (bestEffort != null)
            options.put("geni_best_effort", bestEffort);
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, new Vector(urns));
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Status", argv, methodParams);

        AggregateManagerReply<StatusInfo> nullRes = new AggregateManagerReply<StatusInfo>(res, null);
        if (!nullRes.getGeniResponseCode().isSuccess()) {
            log(res, nullRes, "status", "Status", con, methodParams);
            return nullRes;
        }

        StatusInfo statusInfo = new StatusInfo((Hashtable) res.getResultValueObject());
        AggregateManagerReply<StatusInfo> r = new AggregateManagerReply<StatusInfo>(res, statusInfo);
        log(res, r, "status", "Status", con, methodParams);
        return r;
    }

    @ApiMethod(hint=" Perform the named operational action on the named slivers, possibly changing the geni_operational_status of the named slivers. E.G. 'start' a VM. For valid operations and expected states, consult the state diagram advertised in the aggregate's advertisement RSpec.\n" +
            "\n" +
            "This operation is similar to ProtoGENI functions like StartSliver, StopSliver, and RestartSliver in the PG CMv2 API.\n" +
            "\n" +
            "Aggregate Managers SHOULD return an error code of 13 (UNSUPPORTED) if they do not support a given action for a given resource. An AM SHOULD constrain actions based on the current operational state of the resource. This is a fast synchronous operation, and MAY start long-running sliver transitions whose status can be queried using Status. This method should only be called, and is only valid, when the sliver is fully allocated (operational status is not geni_pending_allocation).\n" +
            "\n" +
            "While the action argument may be aggregate and sliver type specific (none are required for all aggregates and sliver types), this API does define three common actions that AMs should support if possible: geni_start, geni_stop, and geni_restart. Calling PerformOperationalAction with the action geni_start corresponds to the final part of what CreateSliver did in AM API v2.\n" +
            "\n" +
            "Return: On success, the value field of the return struct will contain a list of structs:\n\n" +
            "[ {\n" +
            "        geni_sliver_urn : <string>,\n" +
            "        geni_allocation_status: <string, eg geni_provisioned>,\n" +
            "        geni_operational_status : <string>,\n" +
            "        geni_expires: <dateTime.rfc3339 of individual sliver expiration>,\n" +
            "        [optional: 'geni_resource_status' : string with resource-specific status in more detail than operational_status; may be omitted],\n" +
            "        [optional: 'geni_error': string explanation of operation failure for this sliver. The field may be omitted but if present may not be null/None.]\n" +
            "        }, \n" +
            "        ... \n" +
            "]\n" +
            ";\n\n" +
            "Note that PerformOperationalAction may return an empty list, if no slivers were in the request or in the specified slice. However, the method may instead return an error (e.g. SEARCHFAILED). Calling this method on a specific sliver that is unknown, expired, or deleted shall result in an error (SEARCHFAILED or EXPIRED or ERROR), unless geni_best_effort is true.\n" +
            "\n" +
            "The optional geni_resource_status field MAY be returned for each sliver which contains a resource-specific status that may be more nuanced than the options for geni_operational_status. ")
    public AggregateManagerReply<List<SliverInfo>> performOperationalAction(GeniConnection con,
                                                                            @ApiMethodParameter(name="urns", hint="The target on which to perform the action. "+"Several methods take some URNs to identify what to operate on. These methods are defined as accepting a list of arbitrary strings called URNs, which follow the GENI identifier rules. This API defines two kinds of URNs that may be supplied here, slice URNs and sliver URNs (see the GENI identifiers page). Some aggregates may understand other URNs, but these are not defined or required here. Aggregates that accept only URNs defined by this API will return an error when given URNs not in one of those forms. This API requires that aggregates accept either a single slice URN, or 1 or more sliver URNs that all belong to the same slice. Aggregates are not required to accept both a slice URN and sliver URNs, 2 or more slice URNs, or a set of sliver URNs that crosses multiple slices. Some aggregates may choose to accept other such combinations of URNs. Aggregates that accept only arguments defined by this API will return an error when given more than 1 slice URN, a combination of both slice and sliver URNs, or a set of sliver URNs that belong to more than 1 slice.\n\nIf the urns[] list includes a set of sliver URNs, then the AM shall apply the method to all listed slivers. If the operation fails on one or more of the slivers for any reason, then the whole method fails with an appropriate error code, unless geni_best_effort is true and supported.")
                                                                                    List<String> urns,
                                                                            @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                                                    List<GeniCredential> credentialList,
                                                                            @ApiMethodParameter(name="action", guiDefault="geni_start", hint=" Operational actions are commands that the aggregate exposes, allowing an experimenter tool to modify or act on a sliver from outside of the sliver (i.e. without logging in to a machine), without modifying the sliver reservation. Actions may cause changes to sliver operational state.\n" +
                                                                                    "\n" +
                                                                                    "The API defines a few operational actions: these need not be supported. AMs are encouraged to support these if possible, but only if they can be supported following the defined semantics.\n" +
                                                                                    "\n" +
                                                                                    "AMs may have their own operational states/state-machine internally. AMs are however required to advertise such states and actions that experimenters may see or use, by using an advertisement RSpec extension (if an AM does not advertise operational states, then tools can not know whether any actions are available). Operational states which the experimenter never sees, need not be advertised. Operational states and actions are generally by resource type. The standard RSpec extension attaches such definitions to the sliver_type element of RSpecs.\n" +
                                                                                    "\n" +
                                                                                    "Tools must use the operational states and actions advertisement to determine what operational actions to offer to experimenters, and what actions to perform for the experimenter. Tools may choose to offer actions which the tool itself does not understand, relying on the experimenter to understand the meaning of the new action.\n" +
                                                                                    "\n" +
                                                                                    "Any operational action may fail. When this happens, the API method should return an error code. The sliver may remain in the original state. In some cases, the sliver may transition to the geni_failed state.\n" +
                                                                                    "\n" +
                                                                                    "Operational actions immediately change the sliver operational state (if any change will occur). Long running actions therefore require a 'wait' state, while the action is completing.\n" +
                                                                                    "\n" +
                                                                                    "GENI defined operational actions:\n" +
                                                                                    "\n" +
                                                                                    "    geni_start: This action results in the sliver becoming geni_ready eventually. The operation may fail (move to geni_failed), or move through some number of transition states. For example, booting a VM.\n" +
                                                                                    "    geni_restart: This action results in the sliver becoming geni_ready eventually. The operation may fail (move to geni_failed), or move through some number of transition states. During this operation, the resource may or may not remain accessible. Dynamic state associated with this resource may be lost by performing this operation. For example, re-booting a VM.\n" +
                                                                                    "    geni_stop: This action results in the sliver becoming geni_notready eventually. The operation may fail (move to geni_failed), or move through some number of transition states. For example, powering down a VM. ")
                                                                                    String action,
                                                                            @ApiMethodParameter(name="bestEffort", required=false, hint="Default is false (action applies to all slivers equally or none; the method returns an error code without changing the operational state if any sliver fails).")
                                                                                    Boolean bestEffort,
                                                                            Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "urns", urns);
        if (bestEffort != null) methodParams.put("bestEffort", bestEffort);

        checkNonNullArgument(urns, "urns");
        checkNonNullArgument(credentialList, "credentialList");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Hashtable options = new Hashtable();
        if (bestEffort != null)
            options.put("geni_best_effort", bestEffort);
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(4);
        argv.set(0, new Vector(urns));
        argv.set(1, credentialsVector);
        argv.set(2, action);
        argv.set(3, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "PerformOperationalAction", argv, methodParams);

        AggregateManagerReply<List<SliverInfo>> r = null;
        try {
            r = new AggregateManagerReply<List<SliverInfo>>(res, null);
            if (r.getGeniResponseCode().isSuccess()) {
                r = new AggregateManagerReply<List<SliverInfo>>(res, null);
            } else {
                Vector sliverInfoHTs = (Vector) res.getResultValueObject();
                List<SliverInfo> sliverinfos = new ArrayList<SliverInfo>();
                for (Object o : sliverInfoHTs) {
                    Hashtable ht = (Hashtable) o;
                    sliverinfos.add(new SliverInfo(ht));
                }
                r = new AggregateManagerReply<List<SliverInfo>>(res, sliverinfos);
            }
        } catch (Exception e) {
            log(res, null, "performOperationalAction", "PerformOperationalAction", con, methodParams);
            throw new GeniException("error in performOperationalAction: "+e.getMessage(), e);
        }
        log(res, r, "performOperationalAction", "PerformOperationalAction", con, methodParams);
        return r;
    }

    @ApiMethod(hint=" Delete the named slivers, making them geni_unallocated. Resources are stopped if necessary, and both de-provisioned and de-allocated. No further AM API operations may be performed on slivers that have been deleted.\n" +
            "\n" +
            "This operation used to be called DeleteSliver in earlier versions of this API. To get the functionality of DeleteSliver, call Delete with the slice URN.\n" +
            "\n" +
            "This operation is similar to ProtoGENI's DeleteSliver operation and to SFA's DeleteSlice operation (sec. 6.2.3).\n" +
            "\n" +
            "The geni_single_allocation return from GetVersion advertises whether or not a client may invoke this method on only some of the slivers in a given geni_allocation_state in a given slice (default is false - the client may operate on only some of the slivers in a given state).\n" +
            "\n" +
            "Return: On success, the value field of the return struct will contain a list of structs:\n" +
            "\n" +
            "[\n" +
            "  {\n" +
            "   geni_sliver_urn: <string>,\n" +
            "   geni_allocation_status: <string>,\n" +
            "   geni_expires: <dateTime.rfc3339 when the sliver expires from its current state>,\n" +
            "   [optional: 'geni_error': string indicating any AM failure deleting the sliver. The field may be omitted but may not be null/None.]\n" +
            "  },\n" +
            "  ...\n" +
            "]\n" +
            "\n" +
            "Note that this method should return a struct for each deleted sliver, with the URN of the deleted sliver, the allocation state geni_unallocated, and the time when the sliver was previously set to expire. This method may also return an empty list, if no slivers are at this aggregate in the specified slice.\n" +
            "\n" +
            "Note that aggregates will automatically delete slivers whose expiration time is reached.\n" +
            "\n" +
            "Calling Delete() on an unknown, expired or deleted sliver (by explicit URN) shall result in an error (e.g. SEARCHFAILED, EXPIRED, or ERROR) (unless geni_best_effort is true, in which case the method may succeed and return a geni_error for each sliver that failed). Attempting to Delete a slice (no slivers identified) with no current slivers at this aggregate may return an empty list of slivers, may return a list of previous slivers that have since been deleted, or may even return an error (e.g. SEARCHFAILED or `EXPIRED); details are aggregate specific. ")
    public AggregateManagerReply<List<SliverInfo>> delete(GeniConnection con,
                                                          @ApiMethodParameter(name="urns", hint="Several methods take some URNs to identify what to operate on. These methods are defined as accepting a list of arbitrary strings called URNs, which follow the GENI identifier rules. This API defines two kinds of URNs that may be supplied here, slice URNs and sliver URNs (see the GENI identifiers page). Some aggregates may understand other URNs, but these are not defined or required here. Aggregates that accept only URNs defined by this API will return an error when given URNs not in one of those forms. This API requires that aggregates accept either a single slice URN, or 1 or more sliver URNs that all belong to the same slice. Aggregates are not required to accept both a slice URN and sliver URNs, 2 or more slice URNs, or a set of sliver URNs that crosses multiple slices. Some aggregates may choose to accept other such combinations of URNs. Aggregates that accept only arguments defined by this API will return an error when given more than 1 slice URN, a combination of both slice and sliver URNs, or a set of sliver URNs that belong to more than 1 slice.\n\nIf the urns[] list includes a set of sliver URNs, then the AM shall apply the method to all listed slivers. If the operation fails on one or more of the slivers for any reason, then the whole method fails with an appropriate error code, unless geni_best_effort is true and supported.")
                                                                List<String> urns,
                                                          @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                                List<GeniCredential> credentialList,
                                                          @ApiMethodParameter(name="bestEffort", required=false, hint="Calling Delete() on an unknown, expired or deleted sliver (by explicit URN) shall result in an error (e.g. SEARCHFAILED, EXPIRED, or ERROR) (unless geni_best_effort is true, in which case the method may succeed and return a geni_error for each sliver that failed). Attempting to Delete a slice (no slivers identified) with no current slivers at this aggregate may return an empty list of slivers, may return a list of previous slivers that have since been deleted, or may even return an error (e.g. SEARCHFAILED or `EXPIRED); details are aggregate specific.")
                                                                Boolean bestEffort,
                                                          Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "urns", urns);
        if (bestEffort != null) methodParams.put("bestEffort", bestEffort);

        checkNonNullArgument(urns, "urns");
        checkNonNullArgument(credentialList, "credentialList");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Hashtable options = new Hashtable();
        if (bestEffort != null)
            options.put("geni_best_effort", bestEffort);
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(3);
        argv.set(0, new Vector(urns));
        argv.set(1, credentialsVector);
        argv.set(2, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Delete", argv, methodParams);

        AggregateManagerReply<List<SliverInfo>> nullRes = new AggregateManagerReply<List<SliverInfo>>(res, null);
        if (!nullRes.getGeniResponseCode().isSuccess()) {
            log(res, nullRes, "delete", "Delete", con, methodParams);
            return nullRes;
        }

        Vector sliverInfoHTs = (Vector)res.getResultValueObject();
        List<SliverInfo> sliverinfos = new ArrayList<SliverInfo>();
        for (Object o : sliverInfoHTs) {
            Hashtable ht = (Hashtable) o;
            sliverinfos.add(new SliverInfo(ht));
        }
        AggregateManagerReply<List<SliverInfo>> r = new AggregateManagerReply<List<SliverInfo>>(res, sliverinfos);
        log(res, r, "delete", "Delete", con, methodParams);
        return r;
    }



//    @ApiMethod(hint="")
//    public AggregateManagerReply<Boolean> renewSliver(GeniConnection con,
//                                                      @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
//                                                            List<GeniCredential> credentialList,
//                                                      @ApiMethodParameter(name="sliceUrn", hint="")
//                                                            String sliceUrn,
//                                                      @ApiMethodParameter(name="expirationTime", hint="")
//                                                            String expirationTimeRfc3339,
//                                                      Hashtable extraOptions) throws GeniException {
//        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn, "expirationTimeRfc3339", expirationTimeRfc3339);
//
//        checkNonNullArgument(credentialList, "credentialList");
//        checkNonNullArgument(expirationTimeRfc3339, "expirationTime");
//        checkNonNullArgument(sliceUrn, "sliceUrn");
//
//        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);
//
//        Hashtable options = new Hashtable();
//        addHashtables(options, extraOptions);
//
//        Vector argv = new Vector();
//        argv.setSize(4);
//        argv.set(0, sliceUrn);
//        argv.set(1, credentialsVector);
//        argv.set(2, expirationTimeRfc3339); //date in RFC 3339
//        argv.set(3, options);
//
//        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "RenewSliver", argv, methodParams);
//        AggregateManagerReply<Boolean> r = new AggregateManagerReply<Boolean>(res);
//        log(res, r, "renewSliver", "RenewSliver", con, methodParams);
//        return r;
//    }

    public AggregateManagerReply<List<SliverInfo>> renew(GeniConnection con,
                                                @ApiMethodParameter(name="urns", hint="")
                                                        List<String> urns,
                                                @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                        List<GeniCredential> credentialList,
                                                @ApiMethodParameter(name="expirationTime", hint="")
                                                        Date expirationTime,
                                                @ApiMethodParameter(name="bestEffort", required=false, hint="")
                                                        Boolean bestEffort,
                                                Hashtable extraOptions) throws GeniException {
        checkNonNullArgument(urns, "urns");
        checkNonNullArgument(expirationTime, "expirationTime");
        checkNonNullArgument(credentialList, "credentialList");

        String expirationTimeRfc3339 = RFC3339Util.dateToRFC3339String(expirationTime);

        return renew(con, urns, credentialList, expirationTimeRfc3339, bestEffort, extraOptions);
    }
    /**
     * Renews the resources in all slivers at this aggregate belonging to the given slice until the given time,
     * extending the lifetime of the slice. Aggregates may limit how long reservations may be extended. Initial sliver
     * expiration is set by aggregate policy, no later than the slice credential expiration time.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 3".
     * @param credentialList mandatory list of credentials. This must include a valid credential for the slice.
     * @param expirationTimeRfc3339 new time at which the sliver is to expire, in RFC3339 format. See
     *              {@link be.iminds.ilabt.jfed.util.RFC3339Util} for help converting to the correct format.
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     * @see <a href="https://tools.ietf.org/html/rfc3339">RFC 3339</a>
     */
    @ApiMethod(hint=" Request that the named slivers be renewed, with their expiration extended. If possible, the aggregate should extend the slivers to the requested expiration time, or to a sooner time if policy limits apply. This method applies to slivers that are geni_allocated or to slivers that are geni_provisioned, though different policies may apply to slivers in the different states, resulting in much shorter max expiration times for geni_allocated slivers.\n" +
            "\n"+
            "This operation used to be called RenewSliver. Use Renew(<slice_urn>) to get the equivalent functionality.\n" +
            "\n" +
            "The geni_single_allocation return from GetVersion advertises whether or not a client may invoke this method on only some of the slivers in a given geni_allocation_state in a given slice (default is false - the client may operate on only some of the slivers in a given state).\n" +
            "\n" +
            "Return: On success, the value field of the return struct will contain a list of structs:\n" +
            "\n" +
            "[\n" +
            "  {\n" +
            "   geni_sliver_urn: <string>,\n" +
            "   geni_allocation_status: <string>,\n" +
            "   geni_operational_status: <string>,\n" +
            "   geni_expires: <dateTime.rfc3339 when the sliver expires from its current state>,\n" +
            "   geni_error: <optional string. The field may be omitted entirely but may not be null/None, explaining any renewal failure for this sliver>\n" +
            "  },\n" +
            "  ...\n" +
            "]\n" +
            "\n" +
            "Calling Renew on an unknown, deleted or expired sliver (by explicit URN) shall result in an error (e.g. SEARCHFAILED, EXPIRED or ERROR geni_code) (unless geni_best_effort is true, in which case the method may succeed, but return a geni_error for each sliver that failed). Attempting to Renew a slice (no slivers identified) with no current slivers at this aggregate may return an empty list of slivers, may return a list of previous slivers that have since been deleted, or may even return an error (SEARCHFAILED or EXPIRED). Note therefore that an empty list is a valid return from this method.\n" +
            "\n" +
            "It is legal to attempt to renew a sliver to a sooner expiration time than the sliver was previously due to expire. Not all aggregates will support this however. ")
    public AggregateManagerReply<List<SliverInfo>> renew(GeniConnection con,
                                                         @ApiMethodParameter(name="urns", hint=""+"Several methods take some URNs to identify what to operate on. These methods are defined as accepting a list of arbitrary strings called URNs, which follow the GENI identifier rules. This API defines two kinds of URNs that may be supplied here, slice URNs and sliver URNs (see the GENI identifiers page). Some aggregates may understand other URNs, but these are not defined or required here. Aggregates that accept only URNs defined by this API will return an error when given URNs not in one of those forms. This API requires that aggregates accept either a single slice URN, or 1 or more sliver URNs that all belong to the same slice. Aggregates are not required to accept both a slice URN and sliver URNs, 2 or more slice URNs, or a set of sliver URNs that crosses multiple slices. Some aggregates may choose to accept other such combinations of URNs. Aggregates that accept only arguments defined by this API will return an error when given more than 1 slice URN, a combination of both slice and sliver URNs, or a set of sliver URNs that belong to more than 1 slice.\n\nIf the urns[] list includes a set of sliver URNs, then the AM shall apply the method to all listed slivers. If the operation fails on one or more of the slivers for any reason, then the whole method fails with an appropriate error code, unless geni_best_effort is true and supported.")
                                                                List<String> urns,
                                                         @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                                List<GeniCredential> credentialList,
                                                         @ApiMethodParameter(name="expirationTimeRfc3339", hint="The date-time string in RFC 3339 format in UTC when the reservation(s) should be extended until. \"It is legal to attempt to renew a sliver to a sooner expiration time than the sliver was previously due to expire. Not all aggregates will support this however.\n\nDepending on local aggregate configuration, the aggregate may only support Renew on all current slivers in the slice, or may permit renewing only some slivers. Local policy will dictate maximum expiration times.\n\nThese times are typically quite short (~ 10 minutes initially, ~120 minutes maximum) for reservations (geni_allocated), and longer for provisioned (geni_provisioned) slivers (~ 5-8 days initially). Since these expiration times are different, typically Renew is used only for slivers in the same allocation state.")
                                                                String expirationTimeRfc3339,
                                                         @ApiMethodParameter(name="bestEffort", required=false, hint="Specifies whether the client prefers all included slivers to be renewed or none, or wants a partial success if possible."+"When Renew is called with geni_best_effort false, the entire method will fail (return non-zero geni_code) if any requested sliver cannot be renewed to the requested time, and all slivers will keep their original expiration time. When Renew is called with geni_best_effort true, some slivers may fail to be renewed. In this case, the allocation state and expiration times do not change. geni_error may optionally be returned by the aggregate to explain this failure.")
                                                                Boolean bestEffort,
                                                         Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "urns", urns, "expirationTimeRfc3339", expirationTimeRfc3339);
        if (bestEffort != null) methodParams.put("bestEffort", bestEffort);

        checkNonNullArgument(urns, "urns");
        checkNonNullArgument(expirationTimeRfc3339, "expirationTimeRfc3339");
        checkNonNullArgument(credentialList, "credentialList");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

        Hashtable options = new Hashtable();
        if (bestEffort != null)
            options.put("geni_best_effort", bestEffort);
        addHashtables(options, extraOptions);

        Vector argv = new Vector();
        argv.setSize(4);
        argv.set(0, new Vector(urns));
        argv.set(1, credentialsVector);
        argv.set(2, expirationTimeRfc3339);
        argv.set(3, options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "Renew", argv, methodParams);

        AggregateManagerReply<List<SliverInfo>> nullRes = new AggregateManagerReply<List<SliverInfo>>(res, null);
        if (!nullRes.getGeniResponseCode().isSuccess()) {
            log(res, nullRes, "renew", "Renew", con, methodParams);
            return nullRes;
        }

        Vector sliverInfoHTs = (Vector)res.getResultValueObject();
        List<SliverInfo> sliverinfos = new ArrayList<SliverInfo>();
        for (Object o : sliverInfoHTs) {
            Hashtable ht = (Hashtable) o;
            sliverinfos.add(new SliverInfo(ht));
        }
        AggregateManagerReply<List<SliverInfo>> r = new AggregateManagerReply<List<SliverInfo>>(res, sliverinfos);
        log(res, r, "renew", "Renew", con, methodParams);
        return r;
    }


    /**
     * Perform an emergency shut down of a sliver or slivers at this aggregate belonging to the given slice. This
     * operation is intended for administrative use. The sliver is shut down but remains available for further forensics.
     *
     * @param con the {@code GeniConnection} to use for this call. Must be a connection to an aggregate implementing
     *            "GENI Aggregate Manager API Version 3".
     * @param credentialList mandatory list of credentials, containing a credential authorizing this shutdown.
     * @param sliceUrn urn for the slice of which shutdown the slivers at this aggregate
     * @param extraOptions optional (not specified = null): Hashtable with extra options.
     * @return a boolean (success or failure), wrapped in a {@code AggregateManager3.AggregateManagerReply}
     * @throws be.iminds.ilabt.jfed.lowlevel.GeniException
     */
    @ApiMethod(hint="This operation is for operator use, to stop a misbehaving resource. Once shut down, the slivers are not available for experimenter use. The underlying resources may be returned to the pool of available resources, depending on resource type and aggregate implementation.\n" +
            "\n" +
            "This method returns true (1), unless the resources remain running in the slice after this operation. ")
    public AggregateManagerReply<Boolean> shutdown(GeniConnection con,
                                                   @ApiMethodParameter(name="credentialList", hint="For this and other methods that take a slice URN or list of sliver URNs, when using SFA style credentials, this list must include a valid slice credential, granting rights to the caller of the method over the given slice.")
                                                        List<GeniCredential> credentialList,
                                                   @ApiMethodParameter(name="sliceUrn", hint="The URN of the slice whose slivers need to be shutdown.")
                                                        String sliceUrn,
                                                   Hashtable extraOptions) throws GeniException {
        Map<String, Object> methodParams = makeMethodParameters("credentialList", credentialList, "sliceUrn", sliceUrn);

        checkNonNullArgument(credentialList, "credentialList");
        checkNonNullArgument(sliceUrn, "sliceUrn");

        Vector credentialsVector = createCredentialsVectorWithTypeAndVersion(credentialList);

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
