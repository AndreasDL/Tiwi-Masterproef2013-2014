package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.GeniUrn;

import java.lang.reflect.Method;
import java.util.*;

/**
 * UniformFederationMemberAuthorityApi
 */
public class UniformFederationMemberAuthorityApi extends AbstractUniformFederationApi {
    /**
     * A human readable name for the implemented API
     *
     * @return "Uniform Federation Member Authority API draft";
     */
    static public String getApiName() {
        return "Uniform Federation Member Authority API draft";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "Uniform Federation Member Authority API draft";
     */
    @Override
    public String getName() {
        return getApiName();
    }

    public UniformFederationMemberAuthorityApi(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.GENI_CH_MA, 0));
    }

    public UniformFederationMemberAuthorityApi(Logger logger) {
        this(logger, true);
    }

    @Override
    public List<GetVersionResult.FieldInfo> getMinimumFields(Method method) {
        List<GetVersionResult.FieldInfo> res = new ArrayList<GetVersionResult.FieldInfo>();
        res.add(new GetVersionResult.FieldInfo("MEMBER_URN", GetVersionResult.FieldInfo.FieldType.URN, true, GetVersionResult.FieldInfo.Protect.PUBLIC));
        res.add(new GetVersionResult.FieldInfo("MEMBER_UID", GetVersionResult.FieldInfo.FieldType.UID, true, GetVersionResult.FieldInfo.Protect.PUBLIC));
        res.add(new GetVersionResult.FieldInfo("MEMBER_FIRSTNAME", GetVersionResult.FieldInfo.FieldType.STRING, true, GetVersionResult.FieldInfo.Protect.IDENTIFYING));
        res.add(new GetVersionResult.FieldInfo("MEMBER_LASTNAME", GetVersionResult.FieldInfo.FieldType.STRING, true, GetVersionResult.FieldInfo.Protect.IDENTIFYING));
        res.add(new GetVersionResult.FieldInfo("MEMBER_USERNAME", GetVersionResult.FieldInfo.FieldType.STRING, true, GetVersionResult.FieldInfo.Protect.PUBLIC));
        res.add(new GetVersionResult.FieldInfo("MEMBER_EMAIL", GetVersionResult.FieldInfo.FieldType.STRING, false, GetVersionResult.FieldInfo.Protect.IDENTIFYING));
        return res;
    }

    @Override
    public List<String> getMinimumFieldNames(Method method) {
        List<String> keys = new ArrayList();
        keys.add("MEMBER_URN");
        keys.add("MEMBER_UID");
        keys.add("MEMBER_FIRSTNAME");
        keys.add("MEMBER_LASTNAME");
        keys.add("MEMBER_USERNAME");
        keys.add("MEMBER_EMAIL");
        return keys;
    }

    @Override
    public Map<String, String> getMinimumFieldsMap(Method method) {
        Map<String, String> res = new HashMap<String, String>();
        for (String key : getMinimumFieldNames(method))
            res.put(key, key+" value");
        return res;
    }


    @ApiMethod(hint="get_version call: Provide a structure detailing the version information as well as details of accepted options s for CH API calls.", unprotected=true)
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


    @ApiMethod(hint="lookup_public_member_info call: Lookup public information about members matching given criteria", unprotected=true)
    public ClearingHouseReply<Hashtable<String, String>> lookupPublicMemberInfo(GeniConnection con,
                                                                                @ApiMethodParameter(name = "match", hint="", parameterType=ApiMethodParameterType.CH_API_MATCH)
                                                                                Map<String, String> match,
                                                                                @ApiMethodParameter(name = "filter", hint="", parameterType=ApiMethodParameterType.CH_API_FILTER)
                                                                                List<String> filter,
                                                                                @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                                Map<String, Object> extraOptions)  throws GeniException {
        return genericLookupCall(con, "lookupPublicMemberInfo", "lookup_public_member_info", null/*no credentialList*/, match, filter, extraOptions, null);
    }

    @ApiMethod(hint="lookup_private_member_info call: Lookup private (SSL/SSH key) information about members matching given criteria")
    public ClearingHouseReply<Hashtable<String, String>> lookupPrivateMemberInfo(GeniConnection con,
                                                                                 @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                                 List<GeniCredential> credentialList,
                                                                                 @ApiMethodParameter(name = "match", hint="", parameterType=ApiMethodParameterType.CH_API_MATCH)
                                                                                 Map<String, String> match,
                                                                                 @ApiMethodParameter(name = "filter", hint="", parameterType=ApiMethodParameterType.CH_API_FILTER)
                                                                                 List<String> filter,
                                                                                 @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                                 Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        return genericLookupCall(con, "lookupPrivateMemberInfo", "lookup_private_member_info", credentialList, match, filter, extraOptions, null);
    }

    @ApiMethod(hint="lookup_identifying_member_info call: Lookup identifying (e.g. name, email) info about matching members")
    public ClearingHouseReply<Hashtable<String, String>> lookupIdentifyingMemberInfo(GeniConnection con,
                                                                                     @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                                     List<GeniCredential> credentialList,
                                                                                     @ApiMethodParameter(name = "match", hint="", parameterType=ApiMethodParameterType.CH_API_MATCH)
                                                                                     Map<String, String> match,
                                                                                     @ApiMethodParameter(name = "filter", hint="", parameterType=ApiMethodParameterType.CH_API_FILTER)
                                                                                     List<String> filter,
                                                                                     @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                                     Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        return genericLookupCall(con, "lookupIdentifyingMemberInfo", "lookup_identifying_member_info", credentialList, match, filter, extraOptions, null);
    }


    @ApiMethod(hint="update_member_info call: Update information about given member public, private or identifying information")
    public ClearingHouseReply<String> updateMemberInfo(GeniConnection con,
                                                       @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                       List<GeniCredential> credentialList,
                                                       @ApiMethodParameter(name = "urn", hint="", parameterType=ApiMethodParameterType.USER_URN)
                                                       String urn,
                                                       @ApiMethodParameter(name = "fields", hint="", parameterType=ApiMethodParameterType.CH_API_FIELDS)
                                                       Map<String, String> fields,
                                                       @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                       Map<String, Object> extraOptions)  throws GeniException {
        return genericUpdateCall(con, "updateMemberInfo", "update_member_info", credentialList, urn, "member", fields, extraOptions);
    }


    /** normally returns nothing at all. Anything that might be returned anyway is converted to string, but normally a null Object will be returned. */
    @ApiMethod(hint="get_credentials call: Provide list of credentials (signed statements) for given member\n" +
            "This is member-specific information suitable for passing as credentials in an AM API call for aggregate authorization.")
    public ClearingHouseReply<String> getCredentials(GeniConnection con,
                                                     @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                     List<GeniCredential> credentialList,
                                                     @ApiMethodParameter(name = "memberUrn", hint="", parameterType=ApiMethodParameterType.USER_URN)
                                                     String memberUrn,
                                                     @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                     Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert memberUrn != null;

        GeniUrn geniUrn = GeniUrn.parse(memberUrn);
        if (geniUrn == null || !geniUrn.getResourceType().equals(memberUrn))
            System.err.println("WARNING: slice URN argument to getCredentials is not a valid member urn: \""+memberUrn+"\" (will be used anyway)");

        Vector args = new Vector(3);
        args.add(memberUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        if (extraOptions == null)
            args.add(new Hashtable<String, Object>());
        else
            args.add(new Hashtable<String, Object>(extraOptions));

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_credentials", args, null);
        ClearingHouseReply<String> r = null;
        try {
            //normally returns nothing at all. Anything that might be returned anyway is converted to string
            Object resultValueObject = res.getResultValueObject();
            String nothing = resultValueObject == null ? null : resultValueObject.toString();
            if (nothing != null && nothing.isEmpty())
                nothing = null;
            r = new ClearingHouseReply<String>(res, nothing);
        } catch (Throwable t) {
            System.err.println("Error parsing get_credentials reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<String>(res, null);
        log(res, r, "getCredentials", "get_credentials", con, null);
        return r;
    }
}
