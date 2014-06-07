package be.iminds.ilabt.jfed.lowlevel;

/**
 * GeniSpecificationNonconformityException is thrown when a server reply does not conform to the specification
 */
public class GeniSpecificationNonconformityException extends GeniException {
    private String methodName;
    private String apiName;
    private String specificationNoncomformityDetail;

    /**
     * @param geniResponseCode may be null
     * */
    public GeniSpecificationNonconformityException(String methodName, String apiName, String specificationNoncomformityDetail, Exception ex, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super(methodName+" reply is not conform the \""+apiName+"\" specification: "+specificationNoncomformityDetail , ex, xmlRpcResult, geniResponseCode);
        this.methodName = methodName;
        this.apiName = apiName;
        this.specificationNoncomformityDetail = specificationNoncomformityDetail;
    }
    public GeniSpecificationNonconformityException(String methodName, String apiName, String specificationNoncomformityDetail, Exception ex) {
        super(methodName+" reply is not conform the \""+apiName+"\" specification: "+specificationNoncomformityDetail, ex);
        this.methodName = methodName;
        this.apiName = apiName;
        this.specificationNoncomformityDetail = specificationNoncomformityDetail;
    }
    /**
     * @param geniResponseCode may be null
     * */
    public GeniSpecificationNonconformityException(String methodName, String apiName, String specificationNoncomformityDetail, XMLRPCCallDetails xmlRpcResult, GeniResponseCode geniResponseCode) {
        super(methodName+" reply is not conform the \""+apiName+"\" specification: "+specificationNoncomformityDetail, xmlRpcResult, geniResponseCode);
        this.methodName = methodName;
        this.apiName = apiName;
        this.specificationNoncomformityDetail = specificationNoncomformityDetail;
    }
    public GeniSpecificationNonconformityException(String methodName, String apiName, String specificationNoncomformityDetail) {
        super(methodName+" reply is not conform the \""+apiName+"\" specification: "+specificationNoncomformityDetail);
        this.methodName = methodName;
        this.apiName = apiName;
        this.specificationNoncomformityDetail = specificationNoncomformityDetail;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getApiName() {
        return apiName;
    }

    public String getSpecificationNoncomformityDetail() {
        return specificationNoncomformityDetail;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
