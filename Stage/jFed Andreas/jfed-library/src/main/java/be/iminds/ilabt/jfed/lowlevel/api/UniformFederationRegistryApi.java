package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.GeniUrn;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * UniformFederationRegistryApi
 */
public class UniformFederationRegistryApi extends AbstractUniformFederationApi {
    /**
     * A human readable name for the implemented API
     *
     * @return "Uniform Federation Registry API draft";
     */
    static public String getApiName() {
        return "Uniform Federation Registry API draft";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "Uniform Federation Registry API draft";
     */
    @Override
    public String getName() {
        return getApiName();
    }

    public UniformFederationRegistryApi(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.GENI_CH, 0));
    }

    public UniformFederationRegistryApi(Logger logger) {
        this(logger, true);
    }

    @ApiMethod(hint="get_version call: Provide a structure detailing the version information as well as details of accepted options for CH API calls.", unprotected=true)
    public ClearingHouseReply<GetVersionResult> getVersion(GeniConnection con)  throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_version", new Vector(), null);
        ClearingHouseReply<GetVersionResult> r = null;
        try {
            r = new ClearingHouseReply<GetVersionResult>(res, new GetVersionResult(apiSpecifiesHashtableStringToObject(res.getResultValueObject())));
        } catch (Throwable e) {
            System.err.println("Error parsing get_version reply: "+e);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<GetVersionResult>(res, null);
        log(res, r, "getVersion", "get_version", con, null);
        return r;
    }

    @Override
    public List<GetVersionResult.FieldInfo> getMinimumFields(Method method) {
        List<GetVersionResult.FieldInfo> res = new ArrayList<GetVersionResult.FieldInfo>();
        //String name, FieldType fieldType, CreationAllowed creationAllowed, boolean updateable, Protect protect, String object
        res.add(new GetVersionResult.FieldInfo("SERVICE_URN", GetVersionResult.FieldInfo.FieldType.URN));
        res.add(new GetVersionResult.FieldInfo("SERVICE_URL", GetVersionResult.FieldInfo.FieldType.URL));
        res.add(new GetVersionResult.FieldInfo("SERVICE_CERTIFICATE", GetVersionResult.FieldInfo.FieldType.CERTIFICATE));
        res.add(new GetVersionResult.FieldInfo("SERVICE_NAME", GetVersionResult.FieldInfo.FieldType.STRING));
        res.add(new GetVersionResult.FieldInfo("SERVICE_DESCRIPTION", GetVersionResult.FieldInfo.FieldType.STRING));
        return res;
    }

    @Override
    public List<String> getMinimumFieldNames(Method method) {
        List<String> keys = new ArrayList();
        keys.add("SERVICE_URN");
        keys.add("SERVICE_URL");
        keys.add("SERVICE_CERTIFICATE");
        keys.add("SERVICE_NAME");
        keys.add("SERVICE_DESCRIPTION");
        return keys;
    }

    @Override
    public Map<String, String> getMinimumFieldsMap(Method method) {
        Map<String, String> res = new HashMap<String, String>();
        for (String key : getMinimumFieldNames(method)) {
            res.put(key, key+" value");
        }
        return res;
    }

    public class ServiceDetails {
        private ServerType.GeniServerRole serverRole;
        private GeniUrn urn;
        private URL url;
        private String certificate;
        private String name;
        private String description;

        private Map<String, String> extraFields = new HashMap<String, String>();

        public ServiceDetails(ServerType.GeniServerRole serverRole, Hashtable fields) {
            this.serverRole = serverRole;
            for (Object entryO : fields.entrySet()) {
                Map.Entry entry = (Map.Entry) entryO;
                assert entry.getKey() instanceof String : "not String in in ServiceDetails fields="+fields;
                String key = (String) entry.getKey();
                assert entry.getValue() instanceof String : "not String in in ServiceDetails fields="+fields;
                String value = (String) entry.getValue();

                boolean known = false;

                if (key.equals("SERVICE_URN")) {
                    urn = GeniUrn.parse(value);
                    known = true;
                }
                if (key.equals("SERVICE_URL")){
                    try {
                        url = new URL(value);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        url = null;
                    }
                    known = true;
                }
                if (key.equals("SERVICE_CERTIFICATE")) {
                    certificate = value;
                    known = true;
                }
                if (key.equals("SERVICE_NAME")) {
                    name = value;
                    known = true;
                }
                if (key.equals("SERVICE_DESCRIPTION")) {
                    description = value;
                    known = true;
                }
                if (!known)
                    extraFields.put(key, value);
            }
        }

        public ServerType.GeniServerRole getServerRole() {
            return serverRole;
        }

        public GeniUrn getUrn() {
            return urn;
        }

        public URL getUrl() {
            return url;
        }

        public String getCertificate() {
            return certificate;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return "ServiceDetails{" +
                    "serverRole=" + serverRole +
                    ", urn=" + urn +
                    ", url=" + url +
                    ", certificate='" + certificate + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", extraFields=" + extraFields +
                    '}';
        }
    }

    @ApiMethod(hint="get_aggregates call: Return information about all aggregates associated with the Federation", unprotected=true)
    public ClearingHouseReply<List<ServiceDetails>> getAggregates(GeniConnection con,
                               @ApiMethodParameter(name = "resultFieldNames", hint="fields names included in reply. If omitted: all fields.",
                                       required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.CH_API_FILTER)
                                                            List<String> resultFieldNames,
                               @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                            Map<String, Object> extraOptions)  throws GeniException {
        Vector args = new Vector(1);
        Hashtable options = new Hashtable();
        if (resultFieldNames != null || !resultFieldNames.isEmpty())
            options.put("filter", new Vector(resultFieldNames));
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_aggregates", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<ServiceDetails>> r = null;
        if (resultValueObject instanceof Vector)
            try {
                Vector serviceDetailsList = (Vector) resultValueObject;
                List<ServiceDetails> resList = new ArrayList<ServiceDetails>();
                for (Object serviceDetailsObject : serviceDetailsList) {
                    if (serviceDetailsObject instanceof Hashtable)
                        resList.add(new ServiceDetails(ServerType.GeniServerRole.AM, (Hashtable) serviceDetailsObject));
                    else {
                        System.err.println("ServiceDetail could not be parsed from type="+serviceDetailsObject.getClass().getName()+" value-"+serviceDetailsObject);
                        resList = null;
                        break;
                    }
                }
                r = new ClearingHouseReply<List<ServiceDetails>>(res, resList);
            } catch (Throwable t) {
                System.err.println("Error parsing CH reply: "+t);
                r = null;
            }

        if (r == null)
            r = new ClearingHouseReply<List<ServiceDetails>>(res, null);
        log(res, r, "getAggregates", "get_aggregates", con, null);
        return r;
    }

    @ApiMethod(hint="get_member_authorities call: Return information about all MA’s associated with the Federation", unprotected=true)
    public ClearingHouseReply<List<ServiceDetails>> getMemberAuthorities(GeniConnection con,
                                                                   @ApiMethodParameter(name = "resultFieldNames", hint="fields names included in reply. If omitted: all fields.", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.CH_API_FILTER)
                                                                   List<String> resultFieldNames,
                                                                   @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                   Map<String, Object> extraOptions)  throws GeniException {
        Vector args = new Vector(1);
        Hashtable options = new Hashtable();
        if (resultFieldNames != null || !resultFieldNames.isEmpty())
            options.put("filter", new Vector(resultFieldNames));
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_member_authorities", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<ServiceDetails>> r = null;
        if (resultValueObject instanceof Vector)
                    try {
                        Vector serviceDetailsList = (Vector) resultValueObject;
                        List<ServiceDetails> resList = new ArrayList<ServiceDetails>();
                        for (Object serviceDetailsObject : serviceDetailsList) {
                            if (serviceDetailsObject instanceof Hashtable)
                                resList.add(new ServiceDetails(ServerType.GeniServerRole.GENI_CH_MA, (Hashtable) serviceDetailsObject));
                            else {
                                System.err.println("ServiceDetail could not be parsed from type="+serviceDetailsObject.getClass().getName()+" value-"+serviceDetailsObject);
                                resList = null;
                                break;
                            }
                        }
                        r = new ClearingHouseReply<List<ServiceDetails>>(res, resList);
                    } catch (Throwable t) {
                        System.err.println("Error parsing CH reply: "+t);
                        r = null;
                    }

        if (r == null)
            r = new ClearingHouseReply<List<ServiceDetails>>(res, null);
        log(res, r, "getMemberAuthorities", "get_member_authorities", con, null);
        return r;
    }

    @ApiMethod(hint="get_slice_authorities call: Return information about all SA’s associated with the Federation", unprotected=true)
    public ClearingHouseReply<List<ServiceDetails>> getSliceAuthorities(GeniConnection con,
                                                                  @ApiMethodParameter(name = "resultFieldNames", hint="fields names included in reply. If omitted: all fields.", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.CH_API_FILTER)
                                                                  List<String> resultFieldNames,
                                                                  @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                  Map<String, Object> extraOptions)  throws GeniException {
        Vector args = new Vector(1);
        Hashtable options = new Hashtable();
        if (resultFieldNames != null || !resultFieldNames.isEmpty())
            options.put("filter", new Vector(resultFieldNames));
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_slice_authorities", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<ServiceDetails>> r = null;
        if (resultValueObject instanceof Vector)
                    try {
                        Vector serviceDetailsList = (Vector) resultValueObject;
                        List<ServiceDetails> resList = new ArrayList<ServiceDetails>();
                        for (Object serviceDetailsObject : serviceDetailsList) {
                            if (serviceDetailsObject instanceof Hashtable)
                                resList.add(new ServiceDetails(ServerType.GeniServerRole.GENI_CH_SA, (Hashtable) serviceDetailsObject));
                            else {
                                System.err.println("ServiceDetail could not be parsed from type="+serviceDetailsObject.getClass().getName()+" value-"+serviceDetailsObject);
                                resList = null;
                                break;
                            }
                        }
                        r = new ClearingHouseReply<List<ServiceDetails>>(res, resList);
                    } catch (Throwable t) {
                        System.err.println("Error parsing CH reply: "+t);
                        r = null;
                    }

        if (r == null)
            r = new ClearingHouseReply<List<ServiceDetails>>(res, null);
        log(res, r, "getSliceAuthorities", "get_slice_authorities", con, null);
        return r;
    }

    @ApiMethod(hint="lookup_authorities_for_urns call: Lookup the authorities for a given URNs", unprotected=true)
    public ClearingHouseReply<List<URL>> lookupAuthoritiesForUrns(GeniConnection con,@
            ApiMethodParameter(name = "urns", hint="URNs of entities for which the authority is requested", parameterType = ApiMethodParameterType.LIST_OF_URN_STRING)
    List<GeniUrn> urns)  throws GeniException {
        Vector args = new Vector(1);
        Vector stringUrns = new Vector();
        for (GeniUrn gu : urns)
            stringUrns.add(gu.toString());
        args.add(stringUrns);
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "lookup_authorities_for_urns", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<URL>> r = null;
        if (resultValueObject instanceof List) {
            List resList = (List) resultValueObject;
            List<URL> urlList = new ArrayList<URL>();

            for (Object o : resList) {
                if (o instanceof String) {
                    try {
                        URL url = new URL((String)o);
                        urlList.add(url);
                    } catch (MalformedURLException e) {
                        System.err.println("Error processing lookup_authorities_for_urns result ("+e.getMessage()+" == not an url: "+o+"): "+resultValueObject);
                        urlList = null;
                        break;
                        //e.printStackTrace();
                    }
                } else {
                    System.err.println("Error processing lookup_authorities_for_urns result (not an string: "+o+"): "+resultValueObject);
                    urlList = null;
                    break;
                }
            }
            r = new ClearingHouseReply<List<URL>>(res, urlList);
        }

        if (r == null)
            r = new ClearingHouseReply<List<URL>>(res, null);
        log(res, r, "lookupAuthoritiesForUrns", "lookup_authorities_for_urns", con, null);
        return r;
    }

    @ApiMethod(hint="get_trust_roots call: Return list of trust roots (certificates) associated with this CH. " +
            "Often this concatenates of the trust roots of the included authorities.", unprotected=true)
    public ClearingHouseReply<List<String>> getTrustRoots(GeniConnection con)  throws GeniException {
        Vector args = new Vector();
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_trust_roots", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<String>> r = null;
        try {
            r = new ClearingHouseReply<List<String>>(res, new ArrayList<String>(apiSpecifiesVectorOfString(resultValueObject)));
        } catch (Throwable t) {
            System.err.println("Error parsing get_trust_roots reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<List<String>>(res, null);
        log(res, r, "getTrustRoots", "get_trust_roots", con, null);
        return r;
    }
}
