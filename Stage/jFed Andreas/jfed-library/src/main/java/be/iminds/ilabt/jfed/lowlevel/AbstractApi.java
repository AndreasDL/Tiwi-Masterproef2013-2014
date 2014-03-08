package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.CommonsHttpClientXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.XmlRpcPrintUtil;
import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcClientException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * AbstractApi
 */
public abstract class AbstractApi {
    /**
     * @return a human readable name for the implemented API
     */
    abstract public String getName();

    private ServerType serverType;

    /* debug will print out a info during the call */
    private boolean debugMode;
    /** debug mode will print out a info during the call */
    public boolean isDebugMode() {
        return debugMode;
    }
    /** debug mode will print out a info during the call */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    /**
     * @param logger the logger to use. May be null if no logger is used.
     * @param autoRetryBusy whether or not to retry when a "busy" reply is received.
     */
    public AbstractApi(Logger logger, boolean autoRetryBusy, ServerType serverType) {
        this.logger = logger;
        this.autoRetryBusy = autoRetryBusy;
        this.serverType = serverType;
        this.debugMode = false;
    }
    /** auto retrying when busy enabled */
    public AbstractApi(Logger logger, ServerType serverType) {
        this(logger, true, serverType);
    }

    public ServerType getServerType() {
        return serverType;
    }

    private Logger logger;
    public Logger getLogger() {
        return logger;
    }
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private boolean autoRetryBusy;
    public boolean isAutoRetryBusy() {
        return autoRetryBusy;
    }




    public static String apiSpecifiesNullableString(Object o) throws BadReplyGeniException {
        if (o == null) return null;
        if (o instanceof String)
            return (String) o;
        else
            throw new BadReplyGeniException("The API specified a String was expected here, but a "+o.getClass().getName()+" was found instead. value="+o);
    }
    public static String apiSpecifiesNonNullString(Object o) throws BadReplyGeniException {
        if (o != null && o instanceof String)
            return (String) o;
        else
            throw new BadReplyGeniException("The API specified a String was expected here, but a "+o == null ? null : o.getClass().getName()+" was found instead. value="+o);
    }
    public static Vector<String> apiSpecifiesVectorOfString(Object o) throws BadReplyGeniException {
        if (o != null && o instanceof Vector) {
            for (Object el : (Vector)o)
                if (el == null || !(el instanceof String))
                    throw new BadReplyGeniException("The API specified a Vector of Strings was expected here, but at least one element was "+el == null ? null : el.getClass().getName()+". elementValue="+el+" vectorValue="+o);
            return (Vector<String>) o;
        }
        else
            throw new BadReplyGeniException("The API specified a Vector of Strings was expected here, but a "+o == null ? null : o.getClass().getName()+" was found instead. value="+o);
    }
    public static <T> Vector<T> apiSpecifiesVectorOfT(Class<T> tClass, Object o) throws BadReplyGeniException {
        if (o != null && o instanceof Vector) {
            for (Object el : (Vector)o)
                if (el == null || !tClass.isInstance(el))
                    throw new BadReplyGeniException("The API specified a Vector of "+tClass.getName()+" was expected here, but at least one element was "+el == null ? null : el.getClass().getName()+". elementValue="+el+" vectorValue="+o);
            return (Vector<T>) o;
        }
        else
            throw new BadReplyGeniException("The API specified a Vector of Strings was expected here, but a "+o == null ? null : o.getClass().getName()+" was found instead. value="+o);
    }
    public static Hashtable<String, Object> apiSpecifiesHashtableStringToObject(Object o) throws BadReplyGeniException {
        if (o != null && o instanceof Hashtable) {
            for (Object entryO : ((Hashtable)o).entrySet()) {
                Map.Entry entry = (Map.Entry) entryO;
                if (entry.getKey() == null || !(entry.getKey() instanceof String))
                    throw new BadReplyGeniException("The API specified a Hashtable mapping Strings to Objects was expected here, but at least one element has a key of type  "+entry.getKey() == null ? null : entry.getKey().getClass().getName()+". keyValue="+entry.getKey()+" hashtableValue="+o);
            }
            return (Hashtable<String, Object>) o;
        }
        else
            throw new BadReplyGeniException("The API specified a Hashtable mapping Strings to Objects was expected here, but a "+o == null ? null : o.getClass().getName()+" was found instead. value="+o);
    }
    public static Hashtable<String, String> apiSpecifiesHashtableStringToString(Object o) throws BadReplyGeniException {
        if (o != null && o instanceof Hashtable) {
            for (Object entryO : ((Hashtable)o).entrySet()) {
                Map.Entry entry = (Map.Entry) entryO;
                if (entry.getKey() == null || !(entry.getKey() instanceof String))
                    throw new BadReplyGeniException("The API specified a Hashtable mapping Strings to String was expected here, but at least one element has a key of type  "+entry.getKey() == null ? null : entry.getKey().getClass().getName()+". keyValue="+entry.getKey()+" hashtableValue="+o);
                if (entry.getValue() == null || !(entry.getValue() instanceof String))
                    throw new BadReplyGeniException("The API specified a Hashtable mapping Strings to String was expected here, but at least one element has a value of type  "+entry.getValue() == null ? null : entry.getValue().getClass().getName()+". valueValue="+entry.getKey()+" hashtableValue="+o);
            }
            return (Hashtable<String, String>) o;
        }
        else
            throw new BadReplyGeniException("The API specified a Hashtable mapping Strings to String was expected here, but a "+o == null ? null : o.getClass().getName()+" was found instead. value="+o);
    }
    public static int apiSpecifiesInt(Object o) throws BadReplyGeniException {
        if (o != null && o instanceof String)
            return (Integer) o;
        else
            throw new BadReplyGeniException("The API specified a Integer was expected here, but a "+o == null ? null : o.getClass().getName()+" was found instead. value="+o);
    }


    /** see AMv3 specification */
    public static Vector createCredentialsVectorWithTypeAndVersion(List<GeniCredential> credentialList) {
        Vector credentialsVector = new Vector();
        credentialsVector.setSize(credentialList.size());
        for (int i = 0; i < credentialList.size(); i++) {
            GeniCredential c = credentialList.get(i);
            if (c == null)
                throw new IllegalArgumentException("Illegal arguments: no GeniCredential is allowed to be null in credentialList: "+credentialList);
            credentialsVector.set(i, c.getGeniV3Hashtable());
        }
        return credentialsVector;
    }


    private be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails lastXmlRpcResult;
    private ApiCallReply lastApiCallReply;

    /**
     * @param xmlRpcRes
     * @param reply may be null
     * @param javaMethodName may be null
     * @param geniMethodName
     * @param con
     * @return
     */
    protected ApiCallReply log(XMLRPCCallDetails xmlRpcRes,
                               ApiCallReply reply,
                               String javaMethodName,
                               String geniMethodName,
                               GeniConnection con,
                               Map<String, Object> methodParameters) {
        lastXmlRpcResult = xmlRpcRes;
        lastApiCallReply = reply;

        if (logger == null)
            return reply;

        SfaAuthority authority = con.getGeniAuthority();
//        assert authority != null; //not guaranteed anymore
        String serverUrl = con.getServerUrl();
        String apiName = getName();
        String httpRequest;
        String httpReply;
        if (xmlRpcRes == null) {
            if (!con.isFakeForDebugging()) {
                httpRequest = con.getXmlRpcTransportFactory().getHttpSentHistory();
                httpReply = con.getXmlRpcTransportFactory().getHttpReceivedHistory();
            } else {
                httpRequest = "No HTTP details: fake debugging connection";
                httpReply = "No HTTP details: fake debugging connection";
            }
        }
        else {
            httpRequest = xmlRpcRes.getRequestHttpContent();
            httpReply = xmlRpcRes.getResultHttpContent();
        }
        ApiCallDetails res = new ApiCallDetails(authority,  serverUrl,
                        apiName, javaMethodName, geniMethodName,
                        reply,
                        httpRequest, httpReply,
                        xmlRpcRes == null ? null : xmlRpcRes.getRequest(),
                        xmlRpcRes == null ? null : xmlRpcRes.getResult(),
                        methodParameters,
                        xmlRpcRes == null ? null : xmlRpcRes.getStartTime(),
                        xmlRpcRes == null ? null : xmlRpcRes.getStopTime(),
                        xmlRpcRes == null ? null : xmlRpcRes.getException()
                );
        getLogger().fireResult(res);

        return reply;
    }
    public be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails getLastXmlRpcResult() {
        return lastXmlRpcResult;
    }

    protected static void checkNonNullArgument(Object o, String error_optname) throws GeniException {
        if (o == null)
            throw new IllegalArgumentException("Illegal arguments: "+error_optname+" is not allowed to be null");
    }

    protected abstract boolean isBusyReply(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res);


    protected static Map<String, Object> makeMethodParameters(String name, Object val) {
        Map<String, Object> res = new HashMap<String, Object>();
        res.put(name, val);
        return res;
    }
    protected static Map<String, Object> makeMethodParameters(String name1, Object val1, String name2, Object val2) {
        Map<String, Object> res = makeMethodParameters(name1, val1);
        res.put(name2, val2);
        return res;
    }
    protected static Map<String, Object> makeMethodParameters(String name1, Object val1, String name2, Object val2, String name3, Object val3) {
        Map<String, Object> res = makeMethodParameters(name1, val1, name2, val2);
        res.put(name3, val3);
        return res;
    }
    protected static Map<String, Object> makeMethodParameters(String name1, Object val1, String name2, Object val2, String name3, Object val3, String name4, Object val4) {
        Map<String, Object> res = makeMethodParameters(name1, val1, name2, val2, name3, val3);
        res.put(name4, val4);
        return res;
    }

    protected XMLRPCCallDetailsGeni executeXmlRpcCommandGeni(GeniConnection con, String command, Vector argv, Map<String, Object> methodParameters) throws GeniException {
        XMLRPCCallDetails res = executeXmlRpcCommand(con, command, argv, methodParameters, true);
        if (!(res instanceof XMLRPCCallDetailsGeni)) {
            XMLRPCCallDetailsGeni convertedres = new XMLRPCCallDetailsGeni(
                    res.getServerUrl(), res.getRequestHttpContent(), res.getResultHttpContent(), res.getRequest(),
                    (Hashtable) res.getResult(), res.getStartTime(), res.getStopTime(), res.getException()
            );
            return convertedres;
        } else
            return (XMLRPCCallDetailsGeni) res;
    }
    protected XMLRPCCallDetails executeXmlRpcCommand(GeniConnection con, String command, Vector argv, Map<String, Object> methodParameters, boolean geni) throws GeniException {
        if (con.isFakeForDebugging()) {
            return new XMLRPCCallDetailsGeneral(con.getServerUrl(), "fake connection: no HTTP details", "fake connection: no HTTP details", argv, null, new Date() , new Date(), null);
        }

        Date startTime = null;
        Date stopTime = null;
        try {
            while (true) { //loop is broken by return of an answer (optionally a non "Busy") or an exception.
                XmlRpcClient xmlRpcClient = con.getXmlRpcClient();
                lastXmlRpcResult = null;

                startTime = new Date();
                stopTime = null;

                Object rc = null;
                try {
                    rc = xmlRpcClient.execute(command, argv);
                    stopTime = new Date();
                }
//                catch (InterruptedException ie) {
//                    would be nice if this handled interruptedExceptions
//                    however, it seems that execute does not handle them...
//                }
                catch (Exception e) {
                    XMLRPCCallDetails res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                            con.getXmlRpcTransportFactory().getHttpSentHistory(),
                            con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                            argv, null, startTime, stopTime, e);
//                    log(res, null, null, command, con, methodParameters);

                    throw new GeniException("XML RPC call for command \""+command+"\" with argv size="+argv.size()+" threw exception. Is URL correct? \""+con.getServerUrl()+"\"",
                            e,
                            res,
                            GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                }
                if (rc == null) {
                    //This mostly occurs if the server returns an empty answer, and for emulab that can happen for some wrong URLs
                    XMLRPCCallDetails res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                            con.getXmlRpcTransportFactory().getHttpSentHistory(),
                            con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                            argv, null, startTime, stopTime, null);
//                    log(res, null, null, command, con, methodParameters);
                    throw new GeniException("XML RPC call for command \""+command+"\" with argv size="+argv.size()+" returned null. Is URL correct? \""+con.getServerUrl()+"\"",
                            res,
                            GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                }

                if (rc.getClass().equals(org.apache.xmlrpc.XmlRpcException.class)) {
                    org.apache.xmlrpc.XmlRpcException ex = (org.apache.xmlrpc.XmlRpcException) rc;
                    boolean busy = false;
                    if (ex instanceof XmlRpcClientException && ex.getCause() != null) {
                        //See if any cause of this exception, (no matter how deep), is a HttpServerBusyException. In that case, don't throw the error.
                        Throwable recurseEx = ex;
                        while (recurseEx != null) {
                            recurseEx = recurseEx.getCause();
                            if (recurseEx != null && recurseEx instanceof CommonsHttpClientXmlRpcTransportFactory.HttpServerErrorException) {
                                busy = true;
                            }
                        }
                    }

                    if (!busy) {
                        //this is likely the server returning an error using XmlRpc
                        //TODO: it would be nice if we had access to the result XmlRpc Hashtable, so we could report it.
                        //      but sadly, we don't
                        XMLRPCCallDetails res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                                con.getXmlRpcTransportFactory().getHttpSentHistory(),
                                con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                                argv, null, startTime, stopTime, null);
//                        log(res, null, null, command, con, methodParameters);
                        throw new GeniException("XML RPC call returned error: "+ex.getMessage(), ex, res, GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                    }
                    else  {
                        //log in case of busy
                        XMLRPCCallDetails res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                                con.getXmlRpcTransportFactory().getHttpSentHistory(),
                                con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                                argv, null, startTime, stopTime, null);
                        log(res, null, null, command, con, methodParameters);
                        System.out.flush();
                        System.err.println("Received \"Busy\" reply from server (HTTP status 500). Waiting 5 seconds, and trying again...");
                        System.err.flush();
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            //user propably interrupted us.
                                //Note: this will probably result in double logging...
                            throw new GeniException("Aborted waiting after busy", e, res, GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                        }
                    }
                } else {
                    if (geni) {
                        if (rc.getClass().equals(Hashtable.class)) {
                            Hashtable r = (Hashtable) rc;
                            XMLRPCCallDetails res = new XMLRPCCallDetailsGeni(con.getServerUrl(),
                                    con.getXmlRpcTransportFactory().getHttpSentHistory(),
                                    con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                                    argv, r, startTime, stopTime, null);
                            lastXmlRpcResult = res;

                            if (!autoRetryBusy || !isBusyReply(res)) {
                                //                            System.err.println("AbstactAPI.executeXmlRpcCommand answer");

                                //no need to log, caller has to do that (we can't because we can't create the APICallReply)
                                return res;
                            }
                            else {
                                //log in case of busy
                                log(res, null, null, command, con, methodParameters);
                                System.out.flush();
                                System.err.println("Received \"Busy\" reply from server (GeniResponseCode 14 or similar). Waiting 5 seconds, and trying again...");
                                System.err.flush();
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    //user propably interrupted us.
                                //Note: this will probably result in double logging...
                                    throw new GeniException("Aborted waiting after busy", e, res, GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                                }
                            }
                        } else {
                            XMLRPCCallDetails res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                                    con.getXmlRpcTransportFactory().getHttpSentHistory(),
                                    con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                                    argv, rc, startTime, stopTime, null);
//                            log(res, null, null, command, con, methodParameters);
                            throw new GeniException("XMLRPC execute result class unexpected: "+rc.getClass().getName(), res, GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                        }
                    } else {
                        XMLRPCCallDetails res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                                con.getXmlRpcTransportFactory().getHttpSentHistory(),
                                con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                                argv, rc, startTime, stopTime, null);
                        lastXmlRpcResult = res;

                        if (!autoRetryBusy || !isBusyReply(res)) {
                            //                            System.err.println("AbstactAPI.executeXmlRpcCommand answer");

                            //no need to log, caller has to do that (we can't because we can't create the APICallReply)
                            return res;
                        }
                        else {
                            //log in case of busy
                            log(res, null, null, command, con, methodParameters);
                            System.out.flush();
                            System.err.println("Received \"Busy\" reply from server. Waiting 5 seconds, and trying again...");
                            System.err.flush();
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                //user propably interrupted us.
                                //Note: this will probably result in double logging...
                                throw new GeniException("Aborted waiting after busy", e, res, GeniAMResponseCode.INTERNAL_NONGENI_ERROR);
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
//            System.err.println("AbstactAPI.executeXmlRpcCommand exception: "+e.getMessage());
            XMLRPCCallDetails res;
            if (!(e instanceof GeniException) || ((GeniException)e).getXmlRpcResult() == null)
                res = new XMLRPCCallDetailsGeneral(con.getServerUrl(),
                    con.getXmlRpcTransportFactory().getHttpSentHistory(),
                    con.getXmlRpcTransportFactory().getHttpReceivedHistory(),
                    argv, null, startTime, stopTime, e);
            else
                res = ((GeniException)e).getXmlRpcResult();

            lastXmlRpcResult = res;
            log(res, new ApiCallReply<String>() {
                @Override public GeniAMResponseCode getGeniResponseCode() { return null; }
                @Override public String getValue() { return null; }
                @Override public String getOutput() { return "CLIENT SIDE ERROR while executing command: "+e.getMessage()+"\n"; }
                @Override public Hashtable getRawResult() { return null; }
            }, null, command, con, methodParameters);
            con.markError();
            throw new GeniException("Exception in XML RPC call: "+e.getMessage(), e, res, GeniAMResponseCode.SERVER_REPLY_ERROR);
        }
    }

    /**
     * XMLRPCCallDetails contains all info about an XmlRpc call on XmlRpc level and below.
     * This means it offers access to the actual sent/received HTTP and XmlRpc data.
     * It also offers access to interpreted results and request of the call (as Vector and Hashtable)
     *
     * XMLRPCCallDetailsGeni assumes the result is a hashtable with "code" "value" and "error" fields.
     * It does not assume anything about the type of "code"
     * */
    public static class XMLRPCCallDetailsGeni implements be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails {
        private final String requestHttpContent;
        private final String resultHttpContent;

        private final String resultXmlRpcString;
        private final String requestXmlRpcString;

        private final Vector request;
        private final Hashtable result;

        private final String serverUrl;

        private final Date startTime;
        private final Date stopTime;

        private final Throwable exception;

        /**
         * @param serverUrl
         * @param requestHttpContent
         * @param resultHttpContent
         * @param request
         * @param result may be null
         */
        public XMLRPCCallDetailsGeni(String serverUrl, String requestHttpContent, String resultHttpContent, Vector request,
                                     Hashtable result, Date startTime, Date stopTime, Throwable exception) {
            this.requestHttpContent = requestHttpContent;
            this.resultHttpContent = resultHttpContent;
            if (result != null)
                this.resultXmlRpcString = XmlRpcPrintUtil.printXmlRpcResultObject(result);
            else
                this.resultXmlRpcString = "No result";
            this.requestXmlRpcString = XmlRpcPrintUtil.printXmlRpcResultObject(request);
            this.request = request;
            this.result = result;
            this.serverUrl = serverUrl;

            this.startTime = startTime;
            this.stopTime = stopTime;

            this.exception = exception;
        }

        @Override
        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public String getRequestHttpContent() {
            return requestHttpContent;
        }

        @Override
        public String getResultHttpContent() {
            return resultHttpContent;
        }

        @Override
        public String getResultXmlRpcString() {
            return resultXmlRpcString;
        }

        @Override
        public String getRequestXmlRpcString() {
            return requestXmlRpcString;
        }

        public Vector getRequest() {
            return request;
        }

        public Hashtable getResult() {
            return result;
        }

        @Override
        public Throwable getException() {
            return exception;
        }

        @Override
        public String getResultValueString() {
            if (result == null) return "";
            Object value = result.get("value");
            if (value == null) return "";
            return value.toString();
        }
        @Override
        public Object getResultValueObject() {
            if (result == null) return null;
            Object value = result.get("value");
            return value;
        }
        //        @Override
        public Object getResultCode() {
            if (result == null) return null;
            return result.get("code");
        }
        //        @Override
        public String getResultOutput() {
            if (result == null) return "";
            String output = ((String)result.get("output"));
            return output;
        }


        public Date getStartTime() {
            return startTime;
        }

        public Date getStopTime() {
            return stopTime;
        }
    }
    /**
     * XMLRPCCallDetails contains all info about an XmlRpc call on XmlRpc level and below.
     * This means it offers access to the actual sent/received HTTP and XmlRpc data.
     * It also offers access to interpreted results and request of the call (as Vector and Hashtable)
     * */
    public static class XMLRPCCallDetailsGeneral implements be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails {
        private final String requestHttpContent;
        private final String resultHttpContent;

        private final String resultXmlRpcString;
        private final String requestXmlRpcString;

        private final Vector request;
        private final Object result;

        private final String serverUrl;

        private final Date startTime;
        private final Date stopTime;

        private final Throwable exception;

        /**
         * @param serverUrl
         * @param requestHttpContent
         * @param resultHttpContent
         * @param request
         * @param result may be null
         */
        public XMLRPCCallDetailsGeneral(String serverUrl, String requestHttpContent, String resultHttpContent,
                                        Vector request, Object result, Date startTime, Date stopTime, Throwable exception) {
            this.requestHttpContent = requestHttpContent;
            this.resultHttpContent = resultHttpContent;
            if (result != null)
                this.resultXmlRpcString = XmlRpcPrintUtil.printXmlRpcResultObject(result);
            else
                this.resultXmlRpcString = "No result";
            this.requestXmlRpcString = XmlRpcPrintUtil.printXmlRpcResultObject(request);
            this.request = request;
            this.result = result;
            this.serverUrl = serverUrl;

            this.startTime = startTime;
            this.stopTime = stopTime;

            this.exception = exception;
        }

        @Override
        public String getServerUrl() {
            return serverUrl;
        }

        @Override
        public String getRequestHttpContent() {
            return requestHttpContent;
        }

        @Override
        public String getResultHttpContent() {
            return resultHttpContent;
        }

        @Override
        public String getResultXmlRpcString() {
            return resultXmlRpcString;
        }

        @Override
        public String getRequestXmlRpcString() {
            return requestXmlRpcString;
        }

        @Override
        public Vector getRequest() {
            return request;
        }

        @Override
        public Object getResult() {
            return result;
        }

        @Override
        public Throwable getException() {
            return exception;
        }

        @Override
        public String getResultValueString() {
            return result.toString();
        }
        @Override
        public Object getResultValueObject() {
            return result;
        }
        //        @Override
        public Object getResultCode() {
            return 0;
        }
        //        @Override
        public String getResultOutput() {
            return "";
        }



        public Date getStartTime() {
            return startTime;
        }

        public Date getStopTime() {
            return stopTime;
        }
    }
}
