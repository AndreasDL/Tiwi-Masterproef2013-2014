package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.ResultListener;
import be.iminds.ilabt.jfed.lowlevel.GeniAMResponseCode;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.lowlevel.GeniResponseCode;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;

/**
 * EasyModelAbstractListener
 */
public abstract class EasyModelAbstractListener implements ResultListener {
    protected EasyModel model;

    public EasyModelAbstractListener(EasyModel model) {
        this.model = model;
    }

    /**
     * helper: GENIRESPONSE_SEARCHFAILED means not exist, success means exist. anything else is ignored.
     * */
    private void deriveSliceExistence(ResourceId sliceId, GeniResponseCode geniResponseCode) {
        if (geniResponseCode.equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED))
            if (sliceId.getType().equals("urn"))
                model.logNotExistSlice(sliceId.getValue());

        if (geniResponseCode.isSuccess())
            if (sliceId.getType().equals("urn"))
                model.logExistSlice(sliceId.getValue());
    }

    /**
     * helper that derives that a user credential is valid for the user: if it is used as argument and the call is
     * successful it is valid */
    protected void noteUserCredentialInParameters(ApiCallDetails result) {
        if (result.getReply().getGeniResponseCode().isSuccess()) {
            GeniCredential userCredential = (GeniCredential) result.getMethodParameters().get("userCredential");
            model.setUserCredential(userCredential);
        }
    }

    /** helper that derives that a slice credential is valid for the user: if it is used as argument and the call is
         * successful it is valid */
    protected ResourceId noteSliceCredentialInParameters(ApiCallDetails result) {
        GeniCredential sliceCredential = (GeniCredential) result.getMethodParameters().get("sliceCredential");
        String sliceUrn = sliceCredential.getTargetUrn();
        ResourceUrn sliceId = new ResourceUrn(sliceUrn);
        deriveSliceExistence(sliceId, result.getReply().getGeniResponseCode());

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            Slice slice = model.getSlice(sliceUrn);
            assert slice != null;
            if (slice.getCredential() == null) {
                slice.setCredential(sliceCredential);
//                model.fireSliceChanged(slice);
            }
        }

        return sliceId;
    }

    /** helper  */
    protected ResourceId noteSliceUrnInParameters(ApiCallDetails result) {
        ResourceId id = (ResourceId) result.getMethodParameters().get("slice");
        deriveSliceExistence(id, result.getReply().getGeniResponseCode());
        return id;
    }
}
