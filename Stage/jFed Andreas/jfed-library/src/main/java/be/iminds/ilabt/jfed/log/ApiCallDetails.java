package be.iminds.ilabt.jfed.log;

import be.iminds.ilabt.jfed.lowlevel.ApiCallReply;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.util.*;

/**
 * ApiCallDetails contains everything about an API call.
 * This includes HTTP and XmlRpc sent and received data,
 * the interpreted APICallReply object, info about hte method that did the call,
 * including it's parameters, etc...
 */
public class ApiCallDetails {
    private final SfaAuthority authority;
    private final String serverUrl;

    private final String httpRequest;
    private final String httpReply;

    private final Vector xmlRpcRequest;
    private final Object xmlRpcReply;

    private final String apiName;
    private final String javaMethodName;
    private final String geniMethodName;
    private final Map<String,Object> methodParameters;

    private final ApiCallReply reply;

    private final Date startTime;
    private final Date stopTime;

    private final Throwable exception;

    /**
     * @param authority (if applicable, otherwise null)
     * @param serverUrl
     * @param apiName
     * @param javaMethodName may be null
     * @param geniMethodName
     * @param reply may be null
     * @param httpRequest
     * @param httpReply
     */
    public ApiCallDetails(SfaAuthority authority, String serverUrl, String apiName, String javaMethodName, String geniMethodName, ApiCallReply reply,
                          String httpRequest, String httpReply, Vector xmlRpcRequest, Object xmlRpcReply,
                          Map<String, Object> methodParameters, Date startTime, Date stopTime, Throwable exception) {
        assert httpRequest != null;
        assert httpReply != null;
//        assert authority != null; //not guaranteed anymore

        this.authority = authority;
        this.serverUrl = serverUrl;
        this.apiName = apiName;
        this.javaMethodName = javaMethodName;
        this.geniMethodName = geniMethodName;
        this.reply = reply;
        this.httpRequest = httpRequest;
        this.httpReply = httpReply;
        this.xmlRpcRequest = xmlRpcRequest;
        this.xmlRpcReply = xmlRpcReply;
        if (methodParameters == null)
            this.methodParameters = new HashMap<String, Object>();
        else
            this.methodParameters = new HashMap<String, Object>(methodParameters);
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.exception = exception;
    }

    /**
     * @return GeniAuthority corresponding to the server this call was made to. Can be null
     */
    public SfaAuthority getAuthority() {
        return authority;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getApiName() {
        return apiName;
    }

    public Object getXmlRpcRequest() {
        return xmlRpcRequest;
    }

    public Object getXmlRpcReply() {
        return xmlRpcReply;
    }

    public Map<String, Object> getMethodParameters() {
        return Collections.unmodifiableMap(methodParameters);
    }

    /**
     * @return may return null
     */
    public String getJavaMethodName() {
        return javaMethodName;
    }

    public String getGeniMethodName() {
        return geniMethodName;
    }

    /**
     * @return may return null
     */
    public ApiCallReply getReply() {
        return reply;
    }

    public String getHttpRequest() {
        return httpRequest;
    }

    public String getHttpReply() {
        return httpReply;
    }

    @Override
    public String toString() {
        String responseCode = "no reply";
        if (reply != null && reply.getGeniResponseCode() != null)
            responseCode = reply.getGeniResponseCode().toString();
        return "Result{" + '\n' +
                "serverUrl='" + serverUrl + '\'' + '\n' +
                ", apiName='" + apiName + '\'' + '\n' +
                ", javaMethodName='" + javaMethodName + '\'' + '\n' +
                ", geniMethodName='" + geniMethodName + '\'' + '\n' +
                ", reply.getGeniResponseCode()=" + responseCode + '\n' +
                ", httpRequest='" + httpRequest + '\'' + '\n' +
                ", httpReply='" + httpReply + '\'' + '\n' +
                '}';
    }


//    public void setStartTime(Date startTime) {
//        this.startTime = startTime;
//    }
//
//    public void setStopTime(Date stopTime) {
//        this.stopTime = stopTime;
//    }


    public Date getStartTime() {
        return startTime;
    }

    public Date getStopTime() {
        return stopTime;
    }

    public Throwable getException() {
        return exception;
    }
}
