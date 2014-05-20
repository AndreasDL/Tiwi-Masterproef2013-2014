package be.iminds.ilabt.jfed.lowlevel;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

/**
 * XMLRPCCallDetails
 */
public interface XMLRPCCallDetails {
    public String getServerUrl();
    public String getRequestHttpContent();
    public String getResultHttpContent();
    public String getResultXmlRpcString();
    public String getRequestXmlRpcString();
    public String getResultValueString();
    public Object getResultValueObject();

    public Date getStartTime();
    public Date getStopTime();

//    public Object getResultCode();
//    public String getResultOutput();

    public Vector getRequest();
    public Object getResult();

    public Throwable getException();
}
