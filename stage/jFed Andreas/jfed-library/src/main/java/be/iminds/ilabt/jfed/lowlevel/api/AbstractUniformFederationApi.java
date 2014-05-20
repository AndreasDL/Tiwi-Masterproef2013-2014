package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.util.GeniUrn;

import java.lang.reflect.Method;
import java.util.*;

/**
 * AbstractGeniClearingHouseApi
 */
public abstract class AbstractUniformFederationApi extends AbstractApi {
    public static class GetVersionResult {
        protected String version;
        protected List<String> supportedCredentialTypes;
        protected Map<String, FieldInfo> fieldInfos; //field name -> multiple attributes (attributes have name and value)

        public static class FieldInfo {
            public enum FieldType { URN, URL, UID, STRING, DATETIME, EMAIL, KEY,BOOLEAN, CERTIFICATE, CREDENTIAL_LIST };
            public enum CreationAllowed { REQUIRED, ALLOWED, NOT_ALLOWED };
            public enum Protect { PUBLIC, PRIVATE, IDENTIFYING };

            private Map<String, String> attributes; //all attributes
            private Map<String, String> extraAttributes; //only non recognized

            private final String name;
            //known fields with defaults
            private FieldType fieldType;
            private CreationAllowed creationAllowed = CreationAllowed.NOT_ALLOWED;
            private boolean match = true;
            private boolean updateable = false;
            private Protect protect = null;
            private String object = null;

            @Override
            public String toString() {
                return "FieldInfo{" +
                        "name=" + name +
                        ", fieldType=" + fieldType +
                        ", creationAllowed=" + creationAllowed +
                        ", match=" + match +
                        ", updateable=" + updateable +
                        ", protect=" + protect +
                        ", object='" + object + '\'' +
                        '}';
            }

            public FieldInfo(String name, FieldType fieldType) {
                this.name = name;
                this.fieldType = fieldType;
            }
            public FieldInfo(String name, FieldType fieldType, CreationAllowed creationAllowed, boolean match, boolean updateable) {
                this.name = name;
                this.fieldType = fieldType;
                this.creationAllowed = creationAllowed;
                this.match = match;
                this.updateable = updateable;
            }
            public FieldInfo(String name, FieldType fieldType, boolean match, Protect protect) {
                this.name = name;
                this.fieldType = fieldType;
                this.match = match;
                this.protect = protect;
            }
            public FieldInfo(String name, FieldType fieldType, CreationAllowed creationAllowed, boolean match, boolean updateable, Protect protect, String object) {
                this.name = name;
                this.fieldType = fieldType;
                this.creationAllowed = creationAllowed;
                this.match = match;
                this.updateable = updateable;
                this.protect = protect;
                this.object = object;
            }

            public FieldInfo(String name, Hashtable<String, String> attributesTable) throws BadReplyGeniException {
                this.name = name;
                for (Map.Entry<String, String> entry : attributesTable.entrySet()) {
                    boolean known = false;

                    if (entry.getKey().equals("TYPE")) {
                        fieldType = FieldType.valueOf(entry.getValue()); //TODO check valueOf returns null or throws an exception. If exception, handle!
                        if (fieldType == null)
                            throw new BadReplyGeniException("field TYPE with value \""+entry.getValue()+"\" is unknown");
                        known = true;
                    }

                    if (entry.getKey().equals("CREATE")) {
                        creationAllowed = CreationAllowed.valueOf(entry.getValue()); //TODO check valueOf returns null or throws an exception. If exception, handle!
                        if (creationAllowed == null)
                            throw new BadReplyGeniException("field CREATE with value \""+entry.getValue()+"\" is unknown");
                        known = true;
                    }

                    if (entry.getKey().equals("UPDATE")) {
                        if (entry.getValue().equalsIgnoreCase("true")) {
                            updateable = true;
                            known = true;
                        }
                        if (entry.getValue().equalsIgnoreCase("false")) {
                            updateable = false;
                            known = true;
                        }
                        if (!known)
                            throw new BadReplyGeniException("field UPDATE with value \""+entry.getValue()+"\" is unknown");
                    }

                    if (entry.getKey().equals("PROTECT")) {
                        protect = Protect.valueOf(entry.getValue()); //TODO check valueOf returns null or throws an exception. If exception, handle!
                        if (protect == null)
                            throw new BadReplyGeniException("field PROTECT with value \""+entry.getValue()+"\" is unknown");
                        known = true;
                    }

                    if (entry.getKey().equals("OBJECT")) {
                        object = entry.getValue();
                        known = true;
                    }

                    if (!known)
                        extraAttributes.put(entry.getKey(), entry.getValue());
                    attributes.put(entry.getKey(), entry.getValue());
                }


            }

            /** query all attributes, including the known ones (TYPE, CREATE, UPDATE, PROTECT). */
            public String getAttribute(String name) {
                return attributes.get(name);
            }

            /** query only unknown attributes, excluding the known ones (TYPE, CREATE, UPDATE, PROTECT). */
            public String getExtraAttribute(String name) {
                return extraAttributes.get(name);
            }

            public FieldType getType() {
                return fieldType;
            }

            public CreationAllowed getCreationAllowed() {
                return creationAllowed;
            }

            public boolean isUpdateable() {
                return updateable;
            }

            public Protect getProtect() {
                return protect;
            }

            public String getName() {
                return name;
            }

            /**
             * @return null if OBJECT not specified. In that case it is the default for the API
             *   (i.e. SLICE for Slice Authority, MEMBER for Member Authority, SERVICE for Clearinghouse) */
            public String getObject() {
                return object;
            }
        }

        public GetVersionResult(Hashtable<String, Object> versionInfo) throws BadReplyGeniException {
            version = apiSpecifiesNonNullString(versionInfo.get("VERSION"));

            supportedCredentialTypes = new ArrayList(apiSpecifiesVectorOfString(versionInfo.get("CREDENTIAL_TYPES")));

            Hashtable<String, Object> fields = apiSpecifiesHashtableStringToObject(versionInfo.get("FIELDS"));
            fieldInfos = new HashMap<String, FieldInfo>();
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                FieldInfo fieldInfo = new FieldInfo(entry.getKey(), apiSpecifiesHashtableStringToString(entry.getValue()));
                fieldInfos.put(entry.getKey(), fieldInfo);
            }
        }

        public String getVersion() {
            return version;
        }

        public List<String> getSupportedCredentialTypes() {
            return supportedCredentialTypes;
        }

        public Map<String, FieldInfo> getFields() {
            return fieldInfos;
        }

        @Override
        public String toString() {
            return "GetVersionResult{" +
                    "version='" + version + '\'' +
                    ", supportedCredentialTypes=" + supportedCredentialTypes +
                    ", fieldInfos=" + fieldInfos +
                    '}';
        }
    }

    public static class ClearingHouseReply<T> implements ApiCallReply<T> {
        private GeniCHResponseCode genicode;
        private T val;
        /* output is typically set only on error*/
        private String output;

        private Hashtable rawResult;

        public int getCode() {
            return genicode.getCode();
        }
        public GeniCHResponseCode getGeniResponseCode() {
            return genicode;
        }

        public T getValue() {
            return val;
        }

        /** can be null */
        public String getOutput() {
            return output;
        }


        public static boolean isSuccess(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res) {
            if (res instanceof XMLRPCCallDetailsGeni) {
                XMLRPCCallDetailsGeni geniDetails = (XMLRPCCallDetailsGeni) res;
                if (!(geniDetails.getResultCode() instanceof Integer))
                    return false;
                int code = (Integer) geniDetails.getResultCode();
                GeniResponseCode genicode = GeniAMResponseCode.getByCode(code);
                return genicode.isSuccess();
            }
            return false; //not a geni reply, so not successful
        }
        public ClearingHouseReply(XMLRPCCallDetailsGeni res) {
            this.rawResult = res.getResult();

            Hashtable r = res.getResult();
            try {
                this.val = (T) r.get("value");
            } catch (/*ClassCast*/Exception e) {
                //ignore
                this.val = null;
            }
            Object codeObject = res.getResultCode();
            int code = codeObject == null || !(codeObject instanceof Integer) ? GeniCHResponseCode.SERVER_REPLY_ERROR.getCode() : (Integer) codeObject;
            this.genicode = GeniCHResponseCode.getByCode(code);
            this.output = null; //this should be a string, but we allow more. This may be null on success.
            if (r != null && r.get("output") != null) {
                this.output = r.get("output").toString();
                //any empty structure is interpreted as an empty string
                if (r.get("output") instanceof Hashtable && ((Hashtable)r.get("output")).isEmpty() )
                    this.output = "";
                if (r.get("output") instanceof Vector && ((Vector)r.get("output")).isEmpty() )
                    this.output = "";
            }
        }

        public ClearingHouseReply(XMLRPCCallDetailsGeni res, T val) {
            this(res);
            this.val = val;
        }

        public Hashtable getRawResult() {
            return rawResult;
        }
    }

    @Override
    protected boolean isBusyReply(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res) {
        //TODO: see AbstractGeniAggregateManager
        return false;
    }

    public AbstractUniformFederationApi(Logger logger, boolean autoRetryBusy, ServerType serverType) {
        super(logger, autoRetryBusy, serverType);
    }


    public ClearingHouseReply<Hashtable<String, String>> genericLookupCall(GeniConnection con,
                                                                           String methodJavaName,
                                                                           String methodGeniName,
                                                        List<GeniCredential> credentialList,
                                                        Map<String, String> match,
                                                        List<String> filter,
                                                        Map<String, Object> extraOptions,
                                                        Vector extraArguments /* extraArguments are added in front of call*/)  throws GeniException {
        assert methodJavaName != null;
        assert methodGeniName != null;
        assert match != null;

        Vector args = new Vector();
        if (extraArguments != null)
            for (Object extraArgument : extraArguments)
                args.add(extraArgument);

        if (credentialList != null) {
            Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
            args.add(credentials);
        }

        Hashtable options = new Hashtable();
        options.put("match", new Hashtable<String, String>(match));
        if (filter != null && !filter.isEmpty())
            options.put("filter", new Vector<String>(filter));
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, methodGeniName, args, null);
        Object resultValueObject = res.getResultValueObject();
        ClearingHouseReply<Hashtable<String, String>> r = null;
        try {
            Hashtable<String, String> resHashtable = apiSpecifiesHashtableStringToString(resultValueObject);
            r = new ClearingHouseReply<Hashtable<String, String>>(res, resHashtable);
        } catch (Throwable t) {
            System.err.println("Error parsing "+methodGeniName+" reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<Hashtable<String, String>>(res, null);
        log(res, r, methodJavaName, methodGeniName, con, null);
        return r;
    }


    /** normally returns nothing at all. Anything that might be returned anyway is converted to string, but normally a null Object will be returned. */
    public ClearingHouseReply<String> genericUpdateCall(GeniConnection con,
                                                        String methodJavaName,
                                                        String methodGeniName,
                                                        List<GeniCredential> credentialList,
                                                        String urn,
                                                        String urnType,
                                                        Map<String, String> fields,
                                                        Map<String, Object> extraOptions)  throws GeniException {
        assert methodJavaName != null;
        assert methodGeniName != null;
        assert credentialList != null;
        assert urn != null;
        assert fields != null;
        assert !fields.isEmpty();

        GeniUrn geniUrn = GeniUrn.parse(urn);
        if (geniUrn == null || (urnType != null && !geniUrn.getResourceType().equals(urnType)))
            System.err.println("WARNING: slice URN argument to "+methodJavaName+" is not a valid "+(urnType == null ? "" : urnType)+" urn: \""+urn+"\" (will be used anyway)");

        Vector args = new Vector(3);
        args.add(urn);

        Vector credentials = createCredentialsVectorWithTypeAndVersion(credentialList);
        args.add(credentials);

        Hashtable options = new Hashtable();
        options.put("fields", new Hashtable<String, String>(fields));
        if (extraOptions != null)
            options.putAll(extraOptions);
        args.add(options);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, methodGeniName, args, null);
        ClearingHouseReply<String> r = null;
        try {
            //normally returns nothing at all. Anything that might be returned anyway is converted to string
            Object resultValueObject = res.getResultValueObject();
            String nothing = resultValueObject == null ? null : resultValueObject.toString();
            if (nothing != null && nothing.isEmpty())
                nothing = null;
            r = new ClearingHouseReply<String>(res, nothing);
        } catch (Throwable t) {
            System.err.println("Error parsing "+methodGeniName+" reply: "+t);
            r = null;
        }

        if (r == null)
            r = new ClearingHouseReply<String>(res, null);
        log(res, r, methodJavaName, methodGeniName, con, null);
        return r;
    }


    public abstract List<GetVersionResult.FieldInfo> getMinimumFields(Method method);
    public abstract List<String> getMinimumFieldNames(Method method);
    public abstract Map<String, String> getMinimumFieldsMap(Method method);
}
