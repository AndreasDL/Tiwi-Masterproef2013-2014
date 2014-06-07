package be.iminds.ilabt.jfed.lowlevel.api;

import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;

import java.util.Hashtable;
import java.util.Vector;

/**
 * StitchingComputationService
 *
 * see https://dragon.maxgigapop.net/twiki/bin/view/GENI/NetworkStitchingAPI
 */
public class StitchingComputationService extends AbstractGeniAggregateManager {
    public StitchingComputationService(Logger logger) {
        this(logger, true);
    }
    public StitchingComputationService(Logger logger, boolean autoRetryBusy) {
        super(logger, autoRetryBusy, new ServerType(ServerType.GeniServerRole.SCS, 1));
    }

    /**
     * A human readable name for the implemented API
     *
     * @return "Stitching Computation Service";
     */
    static public String getApiName() {
        return "Stitching Computation Service";
    }
    /**
     * A human readable name for the implemented API
     *
     * @return "Stitching Computation Service";
     */
    @Override
    public String getName() {
        return getApiName();
    }

    public static class SCSReply<T> implements ApiCallReply<T> {
        private GeniAMResponseCode genicode;
        private T val;
        /* output is typically set only on error*/
        private String output;
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

        public String getOutput() {
            return output;
        }

        @Override
        public Hashtable getRawResult() {
            return rawResult;
        }

        public SCSReply(XMLRPCCallDetailsGeni res) {
            this.rawResult = res.getResult();
            Hashtable code = (Hashtable) res.getResultCode();
            int intCode = (Integer) code.get("geni_code");
            this.genicode = GeniAMResponseCode.getByCode(intCode);
            try {
                this.val = (T) res.getResultValueObject();
            } catch (Exception e) {
                this.val = null;
            }
            this.output = res.getResultOutput();
        }
        public SCSReply(XMLRPCCallDetailsGeni res, T val) {
            this.rawResult = res.getResult();
            Hashtable code = (Hashtable) res.getResultCode();
            int intCode = (Integer) code.get("geni_code");
            this.genicode = GeniAMResponseCode.getByCode(intCode);
            this.val = val;
            this.output = res.getResultOutput();
        }
    }

    @ApiMethod(hint="GetVersion call: Get static version and configuration information about this SCS. There is no documentation about this call, but it does exist.")
    public SCSReply<Hashtable> getVersion(GeniConnection con) throws GeniException {
        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "GetVersion", new Vector(), null);
        Object val = res.getResultValueObject();
        SCSReply<Hashtable> r = new SCSReply<Hashtable>(res);
        log(res, r, "getVersion", "GetVersion", con, null);
        return r;
    }

    public static class ComputePathResult {
        private final String serviceRspec;
        private final Hashtable workflowData;

        public ComputePathResult(Hashtable workflowData, String serviceRspec) {
            this.workflowData = workflowData;
            this.serviceRspec = serviceRspec;
        }

        public String getServiceRspec() {
            return serviceRspec;
        }

        public Hashtable getWorkflowData() {
            return workflowData;
        }

        @Override
        public String toString() {
            return "ComputePathResult{" +
                    "serviceRspec='" + serviceRspec + '\'' +
                    ", workflowData=" + workflowData +
                    '}';
        }
    }
    @ApiMethod(hint="ComputePath call: Compute a Network Stitching Path. See https://dragon.maxgigapop.net/twiki/bin/view/GENI/NetworkStitchingAPI")
    public SCSReply<ComputePathResult> computePath(GeniConnection con,
                                         @ApiMethodParameter(name="sliceUrn", hint="The slice on which everything needs to be created", parameterType=ApiMethodParameterType.SLICE_URN)
                                         String sliceUrn,
                                         @ApiMethodParameter(name="requestRspec", hint="request rspec with or without stitching extensions", parameterType=ApiMethodParameterType.RSPEC_STRING)
                                         String requestRspec,
                                         @ApiMethodParameter(name="requestOptions", required=false, hint="optional options")
                                         Hashtable requestOptions) throws GeniException {
        //this is documented
//        Vector args = new Vector(3);
//        args.add(sliceUrn);
//        args.add(requestRspec);
//        if (requestOptions == null)
//            args.add(new Hashtable());
//        else
//            args.add(requestOptions);

        //but this is what it really is
        Vector args = new Vector(1);
        Hashtable argsTable = new Hashtable();
        args.add(argsTable);
        argsTable.put("slice_urn", sliceUrn);
        argsTable.put("request_rspec", requestRspec);
        if (requestOptions == null)
            argsTable.put("request_options", new Hashtable());
        else
            argsTable.put("request_options", requestOptions);

        XMLRPCCallDetailsGeni res = executeXmlRpcCommandGeni(con, "ComputePath", args, null);
        SCSReply<ComputePathResult> r = null;
        if (res.getResult() != null && res.getResultValueObject() != null && res.getResultValueObject() instanceof Hashtable)
            try {
                Hashtable resultHashtable = (Hashtable) res.getResultValueObject();

                String rspec = null;
                Object serviceRspecObject = resultHashtable.get("service_rspec");
                if (serviceRspecObject != null) {
                    if (serviceRspecObject instanceof String)
                        rspec = (String) serviceRspecObject;
                    else
                        System.err.println("WARNING: SCS ComputePath service_rspec is of type "+serviceRspecObject.getClass().getName()+"  value=\""+serviceRspecObject+"\"");
                }

                Hashtable workFlowData = null;
                Object workFlowDataObject = resultHashtable.get("workflow_data");
                if (workFlowDataObject != null && workFlowDataObject instanceof Hashtable)
                    workFlowData = (Hashtable) workFlowDataObject;

                r = new SCSReply<ComputePathResult>(res, new ComputePathResult(workFlowData, rspec));
            } catch (Exception e) {
                System.err.println("Exception parsing SCS ComputePath response: "+e.getMessage());
                e.printStackTrace();
            }
        log(res, r, "ComputePath", "ComputePath", con, null);
        return r;
    }
}
