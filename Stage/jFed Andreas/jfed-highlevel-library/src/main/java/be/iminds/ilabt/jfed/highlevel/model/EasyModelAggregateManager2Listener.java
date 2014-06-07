package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.lowlevel.GeniAMResponseCode;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;
import be.iminds.ilabt.jfed.util.GeniUrn;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyModelSliceAuthorityListener: this watches for SliceAuthority calls and fills in EasyModel using the info in them
 */
public class EasyModelAggregateManager2Listener extends EasyModelAbstractListener {
    private static boolean debug = false;

    public EasyModelAggregateManager2Listener(EasyModel model) {
        super(model);
    }

    private List<String> getSliceUrns(List<String> urns) {
        List<String> res = new ArrayList<String>();
        for (String u : urns) {
            GeniUrn geniUrn = GeniUrn.parse(u);
            if (geniUrn != null && geniUrn.getResourceType().equals("slice"))
                res.add(u);
        }
        return res;
    }

    private List<String> getSliverUrns(List<String> urn) {
        List<String> res = new ArrayList<String>();
        for (String u : urn) {
            GeniUrn geniUrn = GeniUrn.parse(u);
            if (geniUrn != null && geniUrn.getResourceType().equals("sliver"))
                res.add(u);
        }
        return res;
    }


    private void onGetVersionResult(ApiCallDetails result) {
        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //TODO
        }
    }

    public void onStatusResult(String sliceUrn, AggregateManager2.SliverStatus statusInfo, SfaAuthority auth) {
        Slice slice = model.logExistSlice(sliceUrn);
        assert slice != null;

//        Sliver sliver = model.logExistSliver(sliceUrn, statusInfo.getUrn(), true/*geni_single is true for AMv2*/); //since success is returned, the sliver must exist  //note: the urn in the status info is not (always) a sliver urn
        Sliver sliver = model.logExistSliverGeniSingle(sliceUrn, auth); //since success is returned, the sliver must exist
        assert sliver != null;

        assert sliver.getAuthority() == auth : "sliver.getAuthority()="+sliver.getAuthority().getUrn()+" != auth="+auth.getUrn();

        if (debug) System.out.println("DEBUG: EasyModelAggregateManager2Listener.onStatusResult is changing slice "+slice.getName()+" sliver @ "+auth.getName()+" status from "+sliver.getStatusString()+" to "+statusInfo.getStatus());

        assert statusInfo.getStatus() != null;
        sliver.setStatusString(statusInfo.getStatus());
        boolean knownStatus = false;
        if (statusInfo.getStatus().equals("configuring")) { sliver.setStatus(Status.CHANGING); knownStatus = true; }
        if (statusInfo.getStatus().equals("unknown")) { sliver.setStatus(Status.UNKNOWN); knownStatus = true; }
        if (statusInfo.getStatus().equals("changing")) { sliver.setStatus(Status.CHANGING); knownStatus = true; }
        if (statusInfo.getStatus().equals("notready")) { sliver.setStatus(Status.CHANGING); knownStatus = true; }
        if (statusInfo.getStatus().equals("ready")) { sliver.setStatus(Status.READY); knownStatus = true; }
        if (statusInfo.getStatus().equals("failed")) { sliver.setStatus(Status.FAIL); knownStatus = true; }
        if (!knownStatus) {
            System.err.println("WARNING: unknown status received from AM: "+statusInfo.getStatus());
            sliver.setStatus(Status.UNKNOWN);
        }
    }



    public void seeAdvertisementRspec(SfaAuthority auth, boolean available, String rspec) {
        AuthorityInfo ai = model.getAuthorityList().get(auth);
        ai.setAdvertisementRspec(available, rspec);
    }

    public void seeRequestRspec(SfaAuthority auth, String sliceUrn, List<String> sliverUrns, String rspec) {
        model.getRSpecList().seeRequestRspec(auth, sliceUrn, sliverUrns, rspec);
    }

    public void seeEchoRequestRspec(SfaAuthority auth, String sliceUrn, List<String> sliverUrns, String rspec) {
        model.getRSpecList().seeEchoRequestRspec(auth, sliceUrn, sliverUrns, rspec);
    }

    /** seeing a manifest rspec also means that the sliver exists. */
    public void seeManifestRspec(SfaAuthority auth, String sliceUrn, String manifestRspecString) {
        Slice slice = model.getSlice(sliceUrn);
        if (slice == null) return;
        Sliver sliver = model.logExistSliverGeniSingle(sliceUrn, auth); //since success is returned, the sliver must exist
        List<Sliver> sliverList = new ArrayList<Sliver>();
        sliverList.add(sliver);

        RSpecInfo rSpecInfo = new RSpecInfo(manifestRspecString, RSpecInfo.RspecType.MANIFEST, slice, sliverList, model.getAuthorityList().get(auth));
        if (debug) System.out.println("EasyModelAggregateManager2Listener seeManifestRspec is setting manifest for "+slice+" -> "+sliceUrn+"  "+sliver+" -> "+sliver.getUrn());
        sliver.setManifestRspec(rSpecInfo);

        //sliver exists on all cm's mentioned in manifest. (even if unallocated on others)
        for (String cmUrn : rSpecInfo.getRSpec().getAllComponentManagerUrns()) {
            AuthorityInfo ai = model.getAuthorityList().get(cmUrn);
            if (ai == null) {
                System.err.println("Error: manifest contained an unknown authority urn: \""+cmUrn+"\"");
                continue;
            }
            SfaAuthority rspecAuth = ai.getGeniAuthority();
            model.logExistSliverGeniSingle(sliceUrn, rspecAuth); //it is in manifest, so the sliver must exist (even if it is unallocated)
        }
    }

    /** process the result, in the JavaFX thread. And wait for it in this thread. */
    @Override
    public void onResult(final ApiCallDetails details) {
//        final Lock lock = new ReentrantLock();
//        final Condition waiter = lock.newCondition();
//
//        Platform.runLater(new Runnable() { @Override public void run() {
//            onResultInJavaFXThread(details);
//            lock.lock();
//            waiter.signalAll();
//            lock.unlock();
//        } });
//
//        lock.lock();
//        try {
//            waiter.await();
//        } catch (InterruptedException e) {
//            //TODO: handle?
//            e.printStackTrace();
//        }
//        lock.unlock();

        assert Platform.isFxApplicationThread();
        onResultInJavaFXThread(details);
    }
    public void onResultInJavaFXThread(ApiCallDetails details) {
        if (debug) System.out.println("EasyModelAggregateManager2Listener onResultInJavaFXThread SfaCommand=\""+details.getGeniMethodName()+"\" javaCommand=\""+details.getJavaMethodName()+"\"");

        //ignore errors here
        if (details.getReply() == null || details.getJavaMethodName() == null)
            return;

        if (!details.getApiName().equals(AggregateManager2.getApiName()))
            return;

        try {
            if (details.getJavaMethodName().equals("getVersion")) //returns VersionInfo
                onGetVersionResult(details);

            if (details.getJavaMethodName().equals("listResources") && !details.getMethodParameters().containsKey("sliceUrn")) { //returns String (rspec)
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    String rspec = (String) details.getReply().getValue();
                    boolean available = false;
                    if (details.getMethodParameters().get("available") != null)
                        available = (Boolean) details.getMethodParameters().get("available");
                    seeAdvertisementRspec(details.getAuthority(), available, rspec);
                }
            }

            if (details.getJavaMethodName().equals("listResources") && details.getMethodParameters().containsKey("sliceUrn")) { //returns String (rspec)
                String sliceUrn = (String) details.getMethodParameters().get("sliceUrn");
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    String rspec = (String) details.getReply().getValue();
                    boolean available = false;
                    if (details.getMethodParameters().get("available") != null)
                        available = (Boolean) details.getMethodParameters().get("available");
                    model.logExistSlice(sliceUrn); //since success is returned, the slice must exist
                    model.logExistSliverGeniSingle(sliceUrn, details.getAuthority()); //since success is returned, the sliver must exist
                    seeManifestRspec(details.getAuthority(), sliceUrn, rspec);
                } else {
                    if (details.getReply().getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED)) {
                        //since searchfailed is returned, the sliver must not exist
                        model.logNotExistSliverGeniSingle(sliceUrn, details.getAuthority());
                    }
                }
            }

            if (details.getJavaMethodName().equals("createSliver")) { //returns AllocateAndProvisionInfo
                if (debug) System.out.println("EasyModelAggregateManager2Listener onResultInJavaFXThread CreateSliver success="+details.getReply().getGeniResponseCode().isSuccess());
                String sliceUrn = (String) details.getMethodParameters().get("sliceUrn");
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    Slice slice = model.logExistSlice(sliceUrn);
                    Sliver sliver = model.logExistSliverGeniSingle(sliceUrn, details.getAuthority()); //since success is returned, the sliver must exist

                    //also process request Rspec. Just create the slivers that exist in it.
                    if (details.getMethodParameters().get("rspec") != null) {
                        String rspecRequestString = details.getMethodParameters().get("rspec").toString();
                        Rspec rspecRequest = Rspec.fromGeni3RequestRspecXML(rspecRequestString);

                        List<Sliver> slivers = new ArrayList<Sliver>();
                        for (String cmUrn :  rspecRequest.getAllComponentManagerUrns()) {
                            SfaAuthority rspecAuth = model.getAuthorityList().get(cmUrn).getGeniAuthority();
                            Sliver requestSliver = model.logExistSliverGeniSingle(sliceUrn, rspecAuth); //it is in request, so the sliver must exist (even if it is unallocated)
                            slivers.add(requestSliver);
                        }
                        RSpecInfo rspecRequestInfo = new RSpecInfo(
                                rspecRequestString,
                                RSpecInfo.RspecType.REQUEST,
                                slice, slivers,
                                null /*authority */,
                                rspecRequest);
                        for (Sliver requestSliver : slivers)
                            requestSliver.setRequestRspec(rspecRequestInfo);
                    }

                    String manifestRspec = (String) details.getReply().getValue();
                    seeManifestRspec(details.getAuthority(), sliceUrn, manifestRspec);

                    sliver.setStatus(Status.UNKNOWN); //no specific status known. May be changing, failed or ready
                    sliver.setStatusString("CreateSliver successful");
                } else {
                    //allocate failed!
//                    Slice slice = model.logExistSlice(sliceUrn);
//                    assert slice != null;
//                    List<Sliver> slivers = slice.findSlivers(details.getAuthority());
//
//                    if (slivers.isEmpty()) {
                    //sliver creation failed
                    Sliver sliver = model.logExistSliverGeniSingle(sliceUrn, details.getAuthority()); //since success is returned, the sliver must exist

                    sliver.setStatus(Status.FAIL);
                    sliver.setStatusString("CreateSliver failed");
//                    } else {
//                       //sliver creation failed because sliver already existed?
//                    }
                }
            }

            if (details.getJavaMethodName().equals("sliverStatus")) // returns SliverStatus
            {
                String sliceUrn = (String) details.getMethodParameters().get("sliceUrn");
                if (debug) System.out.println("EasyModelAggregateManager2Listener onResultInJavaFXThread sliverStatus slice="+sliceUrn+
                        " at "+details.getAuthority().getName()+
                        " success="+details.getReply().getGeniResponseCode().isSuccess());

                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    assert sliceUrn != null;

                    onStatusResult(sliceUrn, (AggregateManager2.SliverStatus) details.getReply().getValue(), details.getAuthority());
                } else {
                    if (details.getReply().getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED)) {
                        //since searchfailed is returned, the sliver must not exist
                        model.logNotExistSliverGeniSingle(sliceUrn, details.getAuthority());
                    }
                }
            }

            if (details.getGeniMethodName().equals("DeleteSliver") || details.getGeniMethodName().equals("Shutdown")) { //both return Boolean
                String sliceUrn = (String) details.getMethodParameters().get("sliceUrn");
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    Boolean isDeleted = (Boolean) details.getReply().getValue();
                    if (isDeleted)
                        model.logNotExistSliverGeniSingle(sliceUrn, details.getAuthority());
                }
                if (details.getReply().getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED))
                    model.logNotExistSliverGeniSingle(sliceUrn, details.getAuthority());
            }

            if (details.getJavaMethodName().equals("renew")) { //returns List<SliverInfo>
                String sliceUrn = (String) details.getMethodParameters().get("sliceUrn");
                if (details.getReply().getGeniResponseCode().isSuccess()) {
//                    List<AggregateManager2.SliverInfo> si = (List<AggregateManager2.SliverInfo>) details.getReply().getValue();
//                    List<String> urns = (List<String>) details.getMethodParameters().get("urns");
//                    List<String> sliceUrns = getSliceUrns(urns);
//                    String sliceUrn = sliceUrns.size() != 1 ? null : sliceUrns.get(0);
//                    onListOfSliverInfoResult(sliceUrn, si);
                }
                if (details.getReply().getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED))
                    model.logNotExistSliverGeniSingle(sliceUrn, details.getAuthority());
            }
        } catch (Exception e) {
            System.err.println("WARNING: Exception when processing AggregateManager2 reply for EasyModel. This will be ignored, but it is most likely a bug. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
