package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;

import java.util.Hashtable;
import java.util.Vector;

/**
 * AbstractGeniAggregateManager
 */
public abstract class AbstractGeniAggregateManager extends AbstractApi {
    public AbstractGeniAggregateManager(Logger logger, boolean autoRetryBusy, ServerType serverType) {
        super(logger, autoRetryBusy, serverType);
    }

    @Override
    protected boolean isBusyReply(be.iminds.ilabt.jfed.lowlevel.XMLRPCCallDetails res) {
        if (res instanceof XMLRPCCallDetailsGeni) {
            XMLRPCCallDetailsGeni geniDetails = (XMLRPCCallDetailsGeni) res;
            Hashtable codeStruct = (Hashtable) geniDetails.getResultCode();
            int code = (Integer) codeStruct.get("geni_code");
            return GeniAMResponseCode.getByCode(code).isBusy();
        }
        return false;
    }

    public static class AggregateManagerReply<T> implements ApiCallReply<T> {
        private GeniAMResponseCode genicode;
        private T val;
        /* output is typically set only on error*/
        private String output;

        //optional
        private boolean hasAmTypeBool;
        private boolean hasAmCodeBool;
        private String amType;
        private int amCode;

        private Hashtable rawResult;

        public int getCode() {
            return genicode.getCode();
        }
        public GeniAMResponseCode getGeniResponseCode() {
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
                Hashtable codeStruct = (Hashtable) geniDetails.getResultCode();
                int code = (Integer) codeStruct.get("geni_code");
                GeniResponseCode genicode = GeniAMResponseCode.getByCode(code);
                return genicode.isSuccess();
            }
            return false; //not a geni reply, so not successful
        }
        public AggregateManagerReply(XMLRPCCallDetailsGeni res) {
            this.rawResult = res.getResult();

            Hashtable r = res.getResult();
            try {
                this.val = (T) r.get("value");
            } catch (/*ClassCast*/Exception e) {
                //ignore
                this.val = null;
            }
            Hashtable codeStruct = (Hashtable) res.getResultCode();
            int code = codeStruct == null ? GeniAMResponseCode.SERVER_REPLY_ERROR.getCode() : (Integer) codeStruct.get("geni_code");
            this.genicode = GeniAMResponseCode.getByCode(code);
            this.output = null; //this should be a string, but we allow more. This may be null on success.
            if (r != null && r.get("output") != null) {
                this.output = r.get("output").toString();
                //any empty structur eis interpreted as an empty string
                if (r.get("output") instanceof Hashtable && ((Hashtable)r.get("output")).isEmpty() )
                    this.output = "";
                if (r.get("output") instanceof Vector && ((Vector)r.get("output")).isEmpty() )
                    this.output = "";
            }
            this.hasAmTypeBool = codeStruct == null ? false : codeStruct.contains("am_type");
            this.hasAmCodeBool = codeStruct == null ? false : codeStruct.contains("am_code");
            if (hasAmTypeBool)
                this.amType = (String) codeStruct.get("am_type");
            if (hasAmCodeBool)
                this.amCode = (Integer) codeStruct.get("am_code");
        }

        public AggregateManagerReply(XMLRPCCallDetailsGeni res, T val) {
            this(res);
            this.val = val;
        }

        public Hashtable getRawResult() {
            return rawResult;
        }

        public boolean hasAmType() {
            return hasAmTypeBool;
        }

        public boolean hasAmCode() {
            return hasAmCodeBool;
        }

        public String getAmType() {
            if (!hasAmTypeBool)
                throw new RuntimeException("AggregateManagerReply does not have am_type");
            return amType;
        }

        public void setAmType(String amType) {
            hasAmTypeBool = true;
            this.amType = amType;
        }

        public int getAmCode() {
            if (!hasAmCodeBool)
                throw new RuntimeException("AggregateManagerReply does not have am_code");
            return amCode;
        }

        public void setAmCode(int amCode) {
            hasAmCodeBool = true;
            this.amCode = amCode;
        }
    }
}
