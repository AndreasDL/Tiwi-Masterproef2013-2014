package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.GeniUrn;

import java.lang.reflect.Method;
import java.util.*;

/**
 * UniformFederationSliceAuthorityApi
 */
public class UniformFederationSliceAuthorityApi extends AbstractUniformFederationApi {
    /**
     * A human readable name for the implemented API
     *
     * @return "Uniform Federation Slice Authority API draft";
     */
    static public String getApiName() {
        return "Uniform Federation Slice Authority API draft";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "Uniform Federation Slice Authority API draft";
     */
    @Override
    public String getName() {
        return getApiName();
    }

    public UniformFederationSliceAuthorityApi(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.GENI_CH_SA, 0));
    }

    public UniformFederationSliceAuthorityApi(Logger logger) {
        this(logger, true);
    }

    public static class GetVersionSAResult extends GetVersionResult {
        private List<String> services;//only for Slice Authority
        private List<String> roles;//only for Slice Authority
        public GetVersionSAResult(Hashtable<String, Object> versionInfo) throws BadReplyGeniException {
            super(versionInfo);
            services = new ArrayList(apiSpecifiesVectorOfString(versionInfo.get("SERVICES")));

            //roles is optional
            if (versionInfo.get("ROLES") != null)
                roles = new ArrayList(apiSpecifiesVectorOfString(versionInfo.get("ROLES")));
            else
                roles = new ArrayList<String>();
        }

        public List<String> getServices() {
            return services;
        }

        public List<String> getRoles() {
            return roles;
        }

        @Override
        public String toString() {
            return "GetVersionSAResult{" +
                    "version='" + version + '\'' +
                    ", supportedCredentialTypes=" + supportedCredentialTypes +
                    ", fieldInfos=" + fieldInfos +
                    ", services=" + services +
                    ", roles=" + roles +
                    '}';
        }
    }

    @ApiMethod(hint="get_version call: Provide a structure detailing the version information as well as details of accepted options s for CH API calls.", unprotected=true)
    public ClearingHouseReply<GetVersionSAResult> getVersion(GeniConnection con)  throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_version", new Vector(), null);
        ClearingHouseReply<GetVersionSAResult> r = null;
        try {
            r = new ClearingHouseReply<GetVersionSAResult>(res, new GetVersionSAResult(apiSpecifiesHashtableStringToObject(res.getResultValueObject())));
        } catch (Throwable e) {
            System.err.println("Error parsing get_version reply: "+e);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<GetVersionSAResult>(res, null);
        log(res, r, "getVersion", "get_version", con, null);
        return r;
    }

    @Override
    public List<GetVersionResult.FieldInfo> getMinimumFields(Method method) {
        List<GetVersionResult.FieldInfo> res = new ArrayList<GetVersionResult.FieldInfo>();
        //String name, FieldType fieldType, CreationAllowed creationAllowed, boolean match, boolean updateable, Protect protect, String object

        if (method.getName().equals("createSlice") ||
                method.getName().equals("lookupSlices") ||
                method.getName().equals("updateSlice")) {
            res.add(new GetVersionResult.FieldInfo("SLICE_URN", GetVersionResult.FieldInfo.FieldType.URN, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, true, false));
            res.add(new GetVersionResult.FieldInfo("SLICE_UID", GetVersionResult.FieldInfo.FieldType.UID, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, true, false));
            res.add(new GetVersionResult.FieldInfo("SLICE_CREATION", GetVersionResult.FieldInfo.FieldType.DATETIME, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, false, false));
            res.add(new GetVersionResult.FieldInfo("SLICE_EXPIRATION", GetVersionResult.FieldInfo.FieldType.DATETIME, GetVersionResult.FieldInfo.CreationAllowed.ALLOWED, false, true));
            res.add(new GetVersionResult.FieldInfo("EXPIRED", GetVersionResult.FieldInfo.FieldType.BOOLEAN, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, true, false));
            res.add(new GetVersionResult.FieldInfo("SLICE_NAME", GetVersionResult.FieldInfo.FieldType.STRING, GetVersionResult.FieldInfo.CreationAllowed.REQUIRED, false, false));
            res.add(new GetVersionResult.FieldInfo("SLICE_DESCRIPTION", GetVersionResult.FieldInfo.FieldType.STRING, GetVersionResult.FieldInfo.CreationAllowed.ALLOWED, false, true));
        }
        if (method.getName().equals("createProject") ||
                method.getName().equals("lookupProjects") ||
                method.getName().equals("updateProject")) {
            res.add(new GetVersionResult.FieldInfo("PROJECT_URN", GetVersionResult.FieldInfo.FieldType.URN, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, true, false));
            res.add(new GetVersionResult.FieldInfo("PROJECT_UID", GetVersionResult.FieldInfo.FieldType.UID, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, true, false));
            res.add(new GetVersionResult.FieldInfo("PROJECT_CREATION", GetVersionResult.FieldInfo.FieldType.DATETIME, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, false, false));
            res.add(new GetVersionResult.FieldInfo("PROJECT_EXPIRATION", GetVersionResult.FieldInfo.FieldType.DATETIME, GetVersionResult.FieldInfo.CreationAllowed.ALLOWED, false, true));
            res.add(new GetVersionResult.FieldInfo("EXPIRED", GetVersionResult.FieldInfo.FieldType.BOOLEAN, GetVersionResult.FieldInfo.CreationAllowed.NOT_ALLOWED, true, false));
            res.add(new GetVersionResult.FieldInfo("PROJECT_NAME", GetVersionResult.FieldInfo.FieldType.STRING, GetVersionResult.FieldInfo.CreationAllowed.REQUIRED, false, false));
            res.add(new GetVersionResult.FieldInfo("PROJECT_DESCRIPTION", GetVersionResult.FieldInfo.FieldType.STRING, GetVersionResult.FieldInfo.CreationAllowed.ALLOWED, false, true));
        }
        return res;
    }

    @Override
    public List<String> getMinimumFieldNames(Method method) {
        List<String> keys = new ArrayList();
        if (method.getName().equals("createSlice") ||
                method.getName().equals("lookupSlices") ||
                method.getName().equals("updateSlice")) {
            keys.add("SLICE_URN"        );
            keys.add("SLICE_UID"        );
            keys.add("SLICE_CREATION"   );
            keys.add("SLICE_EXPIRATION" );
            keys.add("EXPIRED"     );
            keys.add("SLICE_NAME"       );
            keys.add("SLICE_DESCRIPTION");
        }
        if (method.getName().equals("createProject") ||
                method.getName().equals("lookupProjects") ||
                method.getName().equals("updateProject")) {
            keys.add("PROJECT_URN");
            keys.add("PROJECT_UID");
            keys.add("PROJECT_CREATION");
            keys.add("PROJECT_EXPIRATION");
            keys.add("EXPIRED");
            keys.add("PROJECT_NAME");
            keys.add("PROJECT_DESCRIPTION");
        }
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


    @ApiMethod(hint="create_slice call:")
    public ClearingHouseReply<Hashtable<String, String>> createSlice(GeniConnection con,
                                                                     @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                     List<GeniCredential> credentialList,
                                                                     @ApiMethodParameter(name = "sliceName", hint="", parameterType=ApiMethodParameterType.STRING)
                                                                     String sliceName,
                                                                     @ApiMethodParameter(name = "fields", hint="", parameterType=ApiMethodParameterType.CH_API_FIELDS)
                                                                     Map<String, String> fields,
                                                                     @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                        Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert fields != null;
        assert !fields.isEmpty();

        Vector args = new Vector(2);
        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        Hashtable allFields = new Hashtable<String, String>(fields);
        assert !allFields.contains("SLICE_NAME") : "Add SLICE_NAME with the sliceName option, not directly in the \"fields\" argument";
        allFields.put("SLICE_NAME", sliceName);
        options.put("fields", allFields);
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "create_slice", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<Hashtable<String, String>> r = null;
        try {
            Hashtable<String, String> resHashtable = apiSpecifiesHashtableStringToString(resultValueObject);
            r = new ClearingHouseReply<Hashtable<String, String>>(res, resHashtable);
        } catch (Throwable t) {
            System.err.println("Error parsing create_slice reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<Hashtable<String, String>>(res, null);
        log(res, r, "createSlice", "create_slice", con, null);
        return r;
    }



    @ApiMethod(hint="lookup_slices call:")
    public ClearingHouseReply<Hashtable<String, String>> lookupSlices(GeniConnection con,
                                                                      @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                      List<GeniCredential> credentialList,
                                                                      @ApiMethodParameter(name = "match", hint="", parameterType=ApiMethodParameterType.CH_API_MATCH)
                                                                      Map<String, String> match,
                                                                      @ApiMethodParameter(name = "filter", hint="", parameterType=ApiMethodParameterType.CH_API_FILTER)
                                                                      List<String> filter,
                                                                      @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                      Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        return genericLookupCall(con, "lookupSlices", "lookup_slices", credentialList, match, filter, extraOptions, null);
    }



    @ApiMethod(hint="update_slice call:")
    /** normally returns nothing at all. Anything that might be returned anyway is converted to string, but normally a null Object will be returned. */
    public ClearingHouseReply<String> updateSlice(GeniConnection con,
                                                  @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                  List<GeniCredential> credentialList,
                                                  @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                  String sliceUrn,
                                                  @ApiMethodParameter(name = "fields", hint="", parameterType=ApiMethodParameterType.CH_API_FIELDS)
                                                  Map<String, String> fields,
                                                  @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                  Map<String, Object> extraOptions)  throws GeniException {
        return genericUpdateCall(con, "updateSlice", "update_slice", credentialList, sliceUrn, "slice", fields, extraOptions);
    }

    @ApiMethod(hint="get_credentials call:")
    public ClearingHouseReply<List<GeniCredential>> getSliceCredentials(GeniConnection con,
                                                @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                List<GeniCredential> credentialList,
                                                @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                String sliceUrn,
                                                @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                Map<String, String> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert sliceUrn != null;

        GeniUrn geniUrn = GeniUrn.parse(sliceUrn);
        if (geniUrn == null || !geniUrn.getResourceType().equals("slice"))
            System.err.println("WARNING: slice URN argument to getSliceCredentials is not a valid slice urn: \""+sliceUrn+"\" (will be used anyway)");

        Vector args = new Vector(3);
        args.add(sliceUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        if (extraOptions != null)
            args.add(new Hashtable<String, String>(extraOptions));
        else
            args.add(new Hashtable<String, String>());

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_credentials", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<GeniCredential>> r = null;
        try {
            List<GeniCredential> resList = new ArrayList<GeniCredential>();
            Vector<String> stringCredList = apiSpecifiesVectorOfString(resultValueObject);
            for (String sCred : stringCredList)
                resList.add(new GeniCredential("GeniClearingHouseSliceAuthority getSliceCredentials", sCred));
            r = new ClearingHouseReply<List<GeniCredential>>(res, resList);
        } catch (Throwable t) {
            System.err.println("Error parsing get_credentials reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<List<GeniCredential>>(res, null);
        log(res, r, "getSliceCredentials", "get_credentials", con, null);
        return r;
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Slice Member Service Methods /////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
    Modify slice membership, adding, removing and changing roles of members with respect to given slice
    Arguments:
      Slice_urn: URN of slice for which to modify membership
      members_to_add: List of member_urn/role tuples for members to add to slice
      members_to_remove: List of member_urn of members to remove from slice
      members_to_change: List of member_urn/role tuples for members whose role should change as specified for given slice
    Return:
       None

    function modify_slice_membership (slice_urn, members_to_add, members_to_remove, members_to_modify, credentials, options)
    */

    public static class MemberTuple {
        public final GeniUrn memberUrn;
        public final String role;
        public MemberTuple(GeniUrn memberUrn, String role) {
            this.memberUrn = memberUrn;
            this.role = role;
        }
        public MemberTuple(String memberUrn, String role) {
            this.memberUrn = GeniUrn.parse(memberUrn);
            this.role = role;
        }
        public Vector toVector() {
            Vector v = new Vector(2);
            v.add(memberUrn.toString());
            v.add(role);
            return v;
        }

        @Override
        public String toString() {
            return "Member{" +
                    "urn=" + memberUrn +
                    ", role='" + role + '\'' +
                    '}';
        }
    }
    @ApiMethod(hint="modify_slice_membership call:")
    public ClearingHouseReply<String> modifySliceMembership(GeniConnection con,
                                                            @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                            String sliceUrn,
                                                            @ApiMethodParameter(name = "membersToAdd", hint="", parameterType=ApiMethodParameterType.CH_API_LIST_MEMBER_TUPLES)
                                                            List<MemberTuple> membersToAdd,
                                                            @ApiMethodParameter(name = "membersToRemove", hint="", parameterType=ApiMethodParameterType.LIST_OF_URN_STRING)
                                                            List<GeniUrn> membersToRemove,
                                                            @ApiMethodParameter(name = "membersToChange", hint="", parameterType=ApiMethodParameterType.CH_API_LIST_MEMBER_TUPLES)
                                                            List<MemberTuple> membersToChange,
                                                            @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                            List<GeniCredential> credentialList,
                                                            @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                            Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert sliceUrn != null;

        Vector args = new Vector(6);
        args.add(sliceUrn);

        Vector membersToAddVector = new Vector();
        for (MemberTuple mt : membersToAdd)
            membersToAddVector.add(mt.toVector());
        args.add(membersToAddVector);

        Vector membersToRemoveVector = new Vector();
        for (GeniUrn u : membersToRemove)
            membersToRemoveVector.add(u.toString());
        args.add(membersToRemoveVector);

        Vector membersToChangeVector = new Vector();
        for (MemberTuple mt : membersToChange)
            membersToChangeVector.add(mt.toVector());
        args.add(membersToChangeVector);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "modify_slice_membership", args, null);
        Object resultValueObject = res.getResultValueObject();
        //should return nothing at all, but if there is anything, we convert it to string and return it
        ClearingHouseReply<String> r = new ClearingHouseReply<String>(res, resultValueObject == null ? null : resultValueObject+"");
        log(res, r, "modifySliceMembership", "modify_slice_membership", con, null);
        return r;
    }

    /*
   Lookup members of given slice and their roles within that slice
   Arguments:
     slice_urn: URN of slice for which to provide current members and roles
   Return:
      Dictionary of member_urn/role pairs {‘SLICE_MEMBER’: member_urn, ‘SLICE_ROLE’: role } where ‘role’ is a string of the role name

    function lookup_slice_members (slice_urn, credentials, options)
    */
    @ApiMethod(hint="lookup_slice_members call:")
    public ClearingHouseReply<List<MemberTuple>> lookupSliceMembers(GeniConnection con,
                                                                    @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                                    String sliceUrn,
                                                                    @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                    List<GeniCredential> credentialList,
                                                                    Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;

        Vector args = new Vector();
        args.add(sliceUrn);
        if (credentialList != null) {
            Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
            args.add(credentials);
        } else
            args.add(new Vector());
        if (extraOptions != null)
            args.add(extraOptions);
        else
            args.add(new Hashtable());

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "lookup_slice_members", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<MemberTuple>> r = null;
        try {
            Hashtable<String, String> resHash = apiSpecifiesHashtableStringToString(resultValueObject);
            List<MemberTuple> resList = new ArrayList<MemberTuple>();
            for (Map.Entry<String, String> e : resHash.entrySet()) {
                resList.add(new MemberTuple(e.getKey(), e.getValue()));
            }
            r = new ClearingHouseReply<List<MemberTuple>>(res, resList);
        } catch (Throwable t) {
            System.err.println("Error parsing lookup_slice_members reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<List<MemberTuple>>(res, null);
        log(res, r, "lookupSliceMembers", "lookup_slice_members", con, null);
        return r;
    }

    /*
    Lookup slices for which the given member belongs
    Arguments:
      Member_urn: The member for whom to find slices to which it belongs
    Return:
       Dictionary of slice_urn/role pairs (‘SLICE_URN’ : slice_urn, ‘SLICE_ROLE’ : role} where role is a string of the role name

       function lookup_slices_for_member(member_urn, credentials, options)
    */
    @ApiMethod(hint="lookup_slices_for_member call:")
    public ClearingHouseReply<Hashtable<String, String>> lookupSlicesForMember(GeniConnection con,
                                                                               @ApiMethodParameter(name = "memberUrn", hint="", parameterType=ApiMethodParameterType.URN)
                                                                               String memberUrn,
                                                                               @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                               List<GeniCredential> credentialList,
                                                                               @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                               Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;

        Vector args = new Vector();
        args.add(memberUrn);
        if (credentialList != null) {
            Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
            args.add(credentials);
        } else
            args.add(new Vector());
        if (extraOptions != null)
            args.add(extraOptions);
        else
            args.add(new Hashtable());

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "lookup_slices_for_member", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<Hashtable<String, String>> r = null;
        try {
            Hashtable<String, String> resHash = apiSpecifiesHashtableStringToString(resultValueObject);
            r = new ClearingHouseReply<Hashtable<String, String>>(res, resHash);
        } catch (Throwable t) {
            System.err.println("Error parsing lookup_slices_for_member reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<Hashtable<String, String>>(res, null);
        log(res, r, "lookupSlicesForMember", "lookup_slices_for_member", con, null);
        return r;
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Sliver Info Service API /////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 Associate an aggregate as having sliver information in a given slice
 Expected to be called by an aggregate as an asynchronous (not critical-path) part of the resource allocation process.

Arguments:
  slice_urn: URN of slice for which to associate aggregate
  aggregate_url : URL of aggregate for which slivers may exist for the given slice
Return:
  None

function register_aggregate(slice_urn, aggregate_url, credentials, options)
*/
    @ApiMethod(hint="register_aggregate call:")
    public ClearingHouseReply<String> registerAggregate(GeniConnection con,
                                                        @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                        String sliceUrn,
                                                        @ApiMethodParameter(name = "aggregateUrn", hint="", parameterType=ApiMethodParameterType.URN)
                                                        String aggregateUrn,
                                                        @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                        List<GeniCredential> credentialList,
                                                        @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                        Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert sliceUrn != null;
        assert aggregateUrn != null;

        Vector args = new Vector(4);
        args.add(sliceUrn);
        args.add(aggregateUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "register_aggregate", args, null);
        Object resultValueObject = res.getResultValueObject();
        //should return nothing at all, but if there is anything, we convert it to string and return it
        ClearingHouseReply<String> r = new ClearingHouseReply<String>(res, resultValueObject == null ? null : resultValueObject+"");
        log(res, r, "registerAggregate", "register_aggregate", con, null);
        return r;
    }

    /*
     Dis-associate an aggregate as having sliver information in a given slice
     Expected to be called by an aggregate as an asynchronous (not critical-path) part of the resource de-allocation process.
    Arguments:
      slice_urn: URN of slice for which to associate aggregate
      aggregate_url : URL of aggregate for which slivers may exist for the given slice
    Return:
      None

    function remove_aggregate(slice_urn, aggregate_url, credentials, options)
    */
    @ApiMethod(hint="remove_aggregate call:")
    public ClearingHouseReply<String> removeAggregate(GeniConnection con,
                                                      @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                      String sliceUrn,
                                                      @ApiMethodParameter(name = "aggregateUrn", hint="", parameterType=ApiMethodParameterType.URN)
                                                      String aggregateUrn,
                                                      @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                      List<GeniCredential> credentialList,
                                                      @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                      Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert sliceUrn != null;
        assert aggregateUrn != null;

        Vector args = new Vector(4);
        args.add(sliceUrn);
        args.add(aggregateUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "remove_aggregate", args, null);
        Object resultValueObject = res.getResultValueObject();
        //should return nothing at all, but if there is anything, we convert it to string and return it
        ClearingHouseReply<String> r = new ClearingHouseReply<String>(res, resultValueObject == null ? null : resultValueObject+"");
        log(res, r, "removeAggregate", "remove_aggregate", con, null);
        return r;
    }


    /*
  Provide a list of URLs of all aggregates that have been registered as having resources allocated with a given slice.
NB: This list is not definitive in that the aggregate may not have called the register_aggregate call, and that the slivers may no longer be at the aggregate. But it is provided as a convenience for tools to know where to go for sliver information (rather than querying every aggregate in the CH).
Arguments:
  slice_urn: URN of slice for which to return associated aggregates
Return:
  List of URL’s of aggregates for which slivers may exist for given slice.

function get_aggregates(slice_urn, credentials, options)
    */
    @ApiMethod(hint="get_aggregates call:")
    public ClearingHouseReply<List<String>> getAggregates(GeniConnection con,
                                                          @ApiMethodParameter(name = "sliceUrn", hint="", parameterType=ApiMethodParameterType.SLICE_URN)
                                                          String sliceUrn,
                                                          @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                          List<GeniCredential> credentialList,
                                                          @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                          Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert sliceUrn != null;

        Vector args = new Vector(3);
        args.add(sliceUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "get_aggregates", args, null);
        Object resultValueObject = res.getResultValueObject();

        ClearingHouseReply<List<String>> r = null;

        try {
            List<String> ListOfString = apiSpecifiesVectorOfString(resultValueObject);
            r = new ClearingHouseReply<List<String>>(res, ListOfString);
        } catch (Throwable t) {
            System.err.println("Error parsing get_aggregates reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<List<String>>(res, null);

        log(res, r, "getAggregates", "get_aggregates", con, null);
        return r;
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Project Service Methods /////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
Create project with given details. See generic create_* description above.
Arguments:
   Options: Dictionary of name/value pairs for newly created project.
Return:
   Dictionary of name/value pairs of newly created project including urn

function create_project(credentials, options)
*/
    @ApiMethod(hint="create_project call:")
    public ClearingHouseReply<Hashtable<String, String>> createProject(GeniConnection con,
                                                                       @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                       List<GeniCredential> credentialList,
                                                                       @ApiMethodParameter(name = "projectName", required=false, hint="", parameterType=ApiMethodParameterType.STRING)
                                                                       String projectName,
                                                                       @ApiMethodParameter(name = "fields", hint="", parameterType=ApiMethodParameterType.CH_API_FIELDS)
                                                                       Map<String, String> fields,
                                                                       @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                       Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert fields != null;
        assert !fields.isEmpty();

        Vector args = new Vector(2);
        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable<String, String> fieldsHashtable = new Hashtable<String, String>(fields);
        assert fieldsHashtable.contains("PROJECT_NAME") == (projectName == null) : "Either specify projectName (non-null) or add \"PROJECT_NAME\" to \"fields\". (exactly 1 is required)";
        if (projectName != null)
            fieldsHashtable.put("PROJECT_NAME", projectName);
        Hashtable optionsHashtable = new Hashtable();
        optionsHashtable.put("fields", fieldsHashtable);
        if (extraOptions != null)
            optionsHashtable.putAll(extraOptions);
        args.add(optionsHashtable);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "create_project", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<Hashtable<String, String>> r = null;
        try {
            Hashtable<String, String> resHashtable = apiSpecifiesHashtableStringToString(resultValueObject);
            r = new ClearingHouseReply<Hashtable<String, String>>(res, resHashtable);
        } catch (Throwable t) {
            System.err.println("Error parsing create_project reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<Hashtable<String, String>>(res, null);
        log(res, r, "createProject", "create_project", con, null);
        return r;
    }



/*
Lookup project detail for projects matching ‘match options.
‘filter options indicate what detail to provide.
Arguments:
options: What details to provide (filter options) for which members (match options)
Return: Dictionary of name/value pairs from ‘filter’ options for each project matching ‘match’ option criteria.

function lookup_projects (credentials, options)
*/

    @ApiMethod(hint="lookup_projects call:")
    public ClearingHouseReply<Hashtable<String, String>> lookupProjects(GeniConnection con,
                                                                        @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                        List<GeniCredential> credentialList,
                                                                        @ApiMethodParameter(name = "match", hint="", parameterType=ApiMethodParameterType.CH_API_MATCH)
                                                                        Map<String, String> match,
                                                                        @ApiMethodParameter(name = "filter", hint="", parameterType=ApiMethodParameterType.CH_API_FILTER)
                                                                        List<String> filter,
                                                                        @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                        Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        return genericLookupCall(con, "lookupProjects", "lookup_projects", credentialList, match, filter, extraOptions, null);
    }

    /*
    Update fields in given project object, as allowed in Get_version advertisement. See generic update_* description above.
    Arguments:
    project_urn: URN of project to update
    Options: What details to update (key/value pairs)
    Return: None
    function update_project(project_urn, credentials, options)
    */
    @ApiMethod(hint="update_project call:")
    /** normally returns nothing at all. Anything that might be returned anyway is converted to string, but normally a null Object will be returned. */
    public ClearingHouseReply<String> updateProject(GeniConnection con,
                                                    @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                    List<GeniCredential> credentialList,
                                                    @ApiMethodParameter(name = "projectUrn", hint="", parameterType=ApiMethodParameterType.URN)
                                                    String projectUrn,
                                                    @ApiMethodParameter(name = "fields", hint="", parameterType=ApiMethodParameterType.CH_API_FIELDS)
                                                    Map<String, String> fields,
                                                    @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                    Map<String, Object> extraOptions)  throws GeniException {
        return genericUpdateCall(con, "updateProject", "update_project", credentialList, projectUrn, "project", fields, extraOptions);
    }

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////// Project Member Service Methods /////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
Modify project membership, adding, removing and changing roles of members with respect to given project
Arguments:
  project_urn: Name of project for which to modify membership
  members_to_add: List of member_urn/role tuples for members to add to project
  members_to_remove: List of member_urn of members to remove from project
  members_to_change: List of member_urn/role tuples for members whose role should change as specified for given project
Return:
   None

    function modify_project_membership (project_urn, members_to_add, members_to_remove, members_to_modify, credentials, options)
*/

    @ApiMethod(hint="modify_project_membership call:")
    public ClearingHouseReply<String> modifyProjectMembership(GeniConnection con,
                                                              @ApiMethodParameter(name = "projectUrn", hint="", parameterType=ApiMethodParameterType.URN)
                                                              String projectUrn,
                                                              @ApiMethodParameter(name = "membersToAdd", hint="", parameterType=ApiMethodParameterType.CH_API_LIST_MEMBER_TUPLES)
                                                              List<MemberTuple> membersToAdd,
                                                              @ApiMethodParameter(name = "membersToRemove", hint="", parameterType=ApiMethodParameterType.LIST_OF_URN_STRING)
                                                              List<GeniUrn> membersToRemove,
                                                              @ApiMethodParameter(name = "membersToChange", hint="", parameterType=ApiMethodParameterType.CH_API_LIST_MEMBER_TUPLES)
                                                              List<MemberTuple> membersToChange,
                                                              @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                              List<GeniCredential> credentialList,
                                                              @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                              Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;
        assert projectUrn != null;

        Vector args = new Vector(6);
        args.add(projectUrn);

        Vector membersToAddVector = new Vector();
        for (MemberTuple mt : membersToAdd)
            membersToAddVector.add(mt.toVector());
        args.add(membersToAddVector);

        Vector membersToRemoveVector = new Vector();
        for (GeniUrn u : membersToRemove)
            membersToRemoveVector.add(u.toString());
        args.add(membersToRemoveVector);

        Vector membersToChangeVector = new Vector();
        for (MemberTuple mt : membersToChange)
            membersToChangeVector.add(mt.toVector());
        args.add(membersToChangeVector);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "modify_project_membership", args, null);
        Object resultValueObject = res.getResultValueObject();
        //should return nothing at all, but if there is anything, we convert it to string and return it
        ClearingHouseReply<String> r = new ClearingHouseReply<String>(res, resultValueObject == null ? null : resultValueObject+"");
        log(res, r, "modifyProjectMembership", "modify_project_membership", con, null);
        return r;
    }
    /*
    Lookup members of given project and their roles within that project
    Arguments:
      project_urn: project_urn for which to provide current members and roles
    Return:
       Dictionary of member_urn/role pairs

        function lookup_project_members (project_urn, credentials, options)
    */
    @ApiMethod(hint="lookup_project_members call: Lookup members of given project and their roles within that project")
    public ClearingHouseReply<List<MemberTuple>> lookupProjectMembers(GeniConnection con,
                                                                              @ApiMethodParameter(name = "projectUrn", hint="project_urn for which to provide current members and roles", parameterType=ApiMethodParameterType.URN)
                                                                              String projectUrn,
                                                                              @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                              List<GeniCredential> credentialList,
                                                                              @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                              Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;

        Vector args = new Vector();
        args.add(projectUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        if (extraOptions != null)
            args.add(extraOptions);
        else
            args.add(new Hashtable());

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "lookup_project_members", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<List<MemberTuple>> r = null;
        try {
            Hashtable<String, String> resHash = apiSpecifiesHashtableStringToString(resultValueObject);
            List<MemberTuple> resList = new ArrayList<MemberTuple>();
            for (Map.Entry<String, String> e : resHash.entrySet()) {
                resList.add(new MemberTuple(e.getKey(), e.getValue()));
            }
            r = new ClearingHouseReply<List<MemberTuple>>(res, resList);
        } catch (Throwable t) {
            System.err.println("Error parsing lookup_project_members reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<List<MemberTuple>>(res, null);
        log(res, r, "lookupProjectMembers", "lookup_project_members", con, null);
        return r;
    }
    
    
    /*
    Lookup projects for which the given member belongs
    Arguments:
      Member_urn: The member for whom to find projects to which it belongs
    Return:
       Dictionary of project_urn/role pairs (‘PROJECT_URN’ : project_urn, ‘PROJECT_ROLE’ : role} where role is a string of the role name

       function lookup_projects_for_member(member_urn, credentials, options)
    */
    @ApiMethod(hint="lookup_projects_for_member call:")
    public ClearingHouseReply<Hashtable<String, String>> lookupProjectsForMember(GeniConnection con,
                                                                               @ApiMethodParameter(name = "memberUrn", hint="", parameterType=ApiMethodParameterType.URN)
                                                                               String memberUrn,
                                                                               @ApiMethodParameter(name = "credentialList", hint="", parameterType=ApiMethodParameterType.LIST_OF_CREDENTIAL)
                                                                               List<GeniCredential> credentialList,
                                                                               @ApiMethodParameter(name = "extraOptions", hint="extra options", required = false, guiDefaultOptional = false, parameterType = ApiMethodParameterType.GENI_EXTRA_OPTIONS)
                                                                               Map<String, Object> extraOptions)  throws GeniException {
        assert credentialList != null;

        Vector args = new Vector();
        args.add(memberUrn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        if (extraOptions != null)
            args.add(extraOptions);
        else
            args.add(new Hashtable());

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "lookup_projects_for_member", args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<Hashtable<String, String>> r = null;
        try {
            Hashtable<String, String> resHash = apiSpecifiesHashtableStringToString(resultValueObject);
            r = new ClearingHouseReply<Hashtable<String, String>>(res, resHash);
        } catch (Throwable t) {
            System.err.println("Error parsing lookup_projects_for_member reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<Hashtable<String, String>>(res, null);
        log(res, r, "lookupProjectsForMember", "lookup_projects_for_member", con, null);
        return r;
    }

}
