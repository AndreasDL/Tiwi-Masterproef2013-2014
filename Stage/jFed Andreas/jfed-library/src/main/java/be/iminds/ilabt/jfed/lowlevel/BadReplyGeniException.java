package be.iminds.ilabt.jfed.lowlevel;

/**
 * BadReplyGeniException GeniException (= exception with call info) thrown when an API reply does not
 * conform to the specification.
 */
public class BadReplyGeniException extends GeniException {
    public BadReplyGeniException(String s, Exception ex, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super(s, ex, xmlRpcResult, geniResponseCode);
    }

    public BadReplyGeniException(Exception ex, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super("Reply is not conform the specifications", ex, xmlRpcResult, geniResponseCode);
    }

    public BadReplyGeniException(String s, Exception ex) {
        super(s, ex);
    }

    public BadReplyGeniException(String s, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super(s, xmlRpcResult, geniResponseCode);
    }

    public BadReplyGeniException(XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super("Reply is not conform the specifications", xmlRpcResult, geniResponseCode);
    }

    public BadReplyGeniException(String s) {
        super(s);
    }
}
