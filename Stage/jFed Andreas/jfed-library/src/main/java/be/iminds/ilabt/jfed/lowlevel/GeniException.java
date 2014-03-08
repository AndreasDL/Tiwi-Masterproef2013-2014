package be.iminds.ilabt.jfed.lowlevel;

/**
 * GeniException
 */
public class GeniException extends Exception {
    private XMLRPCCallDetails xmlRpcResult;
    private GeniResponseCode geniResponseCode;

    /**
     * @param geniResponseCode may be null
     * */
    public GeniException(String s, Exception ex, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super(s, ex);
        this.xmlRpcResult = xmlRpcResult;
        this.geniResponseCode = geniResponseCode;
    }
    public GeniException(String s, Exception ex) {
        super(s, ex);
        this.xmlRpcResult = null;
    }
    /**
     * @param geniResponseCode may be null
     * */
    public GeniException(String s, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super(s);
        this.xmlRpcResult = xmlRpcResult;
        this.geniResponseCode = geniResponseCode;
    }
    public GeniException(String s) {
        super(s);
        this.xmlRpcResult = null;
    }

    public XMLRPCCallDetails getXmlRpcResult() {
        return xmlRpcResult;
    }
    
    public GeniResponseCode getGeniResponseCode() {
        return geniResponseCode;
    }

    public void setXmlRpcResult(XMLRPCCallDetails xmlRpcResult) {
        this.xmlRpcResult = xmlRpcResult;
    }

    @Override
    public String toString() {
        if (xmlRpcResult != null)
            return super.toString() + " GeniException.XmlRpcResult=" + xmlRpcResult + '}';
        else
            return super.toString();
    }
}
