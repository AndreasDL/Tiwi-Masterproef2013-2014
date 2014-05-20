package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.RFC3339Util;
import javafx.application.Platform;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * EasyModelSliceAuthorityListener: this watches for SliceAuthority calls and fills in EasyModel using the info in them
 */
public class EasyModelAggregateManager3Listener extends EasyModelAbstractListener {
    public EasyModelAggregateManager3Listener(EasyModel model) {
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

    private List<String> getSliverUrnsFromSliverInfo(List<AggregateManager3.SliverInfo> sliverinfos) {
        List<String> res = new ArrayList<String>();
        for (AggregateManager3.SliverInfo si : sliverinfos)
            res.add(si.getSliverUrn());
        return res;
    }


    private void onGetVersionResult(ApiCallDetails result) {
        if (result.getReply().getGeniResponseCode().isSuccess()) {
           //TODO
        }
    }

    public void onStatusResult(AggregateManager3.StatusInfo statusInfo) {
        model.logExistSlice(statusInfo.getSliceUrn());

        onListOfSliverInfoResult(statusInfo.getSliceUrn(), statusInfo.getSliverInfo());
    }

    /**
     * @param sliceUrn urn of the slice the sliver belongs to, may be null if unknown
     * */
    public void onListOfSliverInfoResult(String sliceUrn, List<AggregateManager3.SliverInfo> sliverinfos) {
        for (AggregateManager3.SliverInfo si : sliverinfos) {

            if (si.getAllocationStatus().equals("geni_unallocated")) {
                //sliver is deleted
                model.logNotExistSliver(sliceUrn, si.getSliverUrn());
            } else {
                Sliver sliver = model.logExistSliver(sliceUrn, si.getSliverUrn(), false/*geni_single  todo amv3, so we need to check getversion for this*/);
                sliver.setAllocationStatus(si.getAllocationStatus());
                sliver.setOperationalStatus(si.getOperationalStatus());
                try {
                    sliver.setExpires(RFC3339Util.rfc3339StringToDate(si.getExpires()));
                } catch (ParseException e) {
                    System.err.println("Invalid RFC3339 date in sliver expires: " + si.getExpires());
                    e.printStackTrace();
                }
            }
        }
    }

    public void onAllocateAndProvisionInfoResult(String sliceUrn, AggregateManager3.AllocateAndProvisionInfo info) {
        onListOfSliverInfoResult(sliceUrn, info.getSliverInfo());
    }

    public void onManifestInfo(SfaAuthority auth, AggregateManager3.ManifestInfo manifestInfo) {
        seeManifestRspec(auth, manifestInfo.getSliceUrn(), null, manifestInfo.getManifestRspec());

        onListOfSliverInfoResult(manifestInfo.getSliceUrn(), manifestInfo.getSliverInfos());
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

    public void seeManifestRspec(SfaAuthority auth, String sliceUrn, List<String> sliverUrns, String rspec) {
        model.getRSpecList().seeManifestRspec(auth, sliceUrn, sliverUrns, rspec);
    }

    @Override
    public void onResult(final ApiCallDetails details) {
//        //Since this triggers updates to the GUI (because it changes Properties) this has to be executed on the JavaFX thread.
//
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
        //ignore errors here
        if (details.getReply() == null || details.getJavaMethodName() == null)
            return;

        if (!details.getApiName().equals(AggregateManager3.getApiName()))
            return;

        try {
            if (details.getJavaMethodName().equals("getVersion")) //returns VersionInfo
                onGetVersionResult(details);

            if (details.getJavaMethodName().equals("listResources")) { //returns String (rspec)
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    String rspec = (String) details.getReply().getValue();
                    boolean available = false;
                    if (details.getMethodParameters().get("available") != null)
                        available = (Boolean) details.getMethodParameters().get("available");
                    seeAdvertisementRspec(details.getAuthority(), available, rspec);
                }
            }

            if (details.getJavaMethodName().equals("describe")) { //returns ManifestInfo
                onManifestInfo(details.getAuthority(), (AggregateManager3.ManifestInfo) details.getReply().getValue());
            }

            if (details.getJavaMethodName().equals("allocate")) { //returns AllocateAndProvisionInfo
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    AggregateManager3.AllocateAndProvisionInfo info = (AggregateManager3.AllocateAndProvisionInfo) details.getReply().getValue();
                    String sliceUrn = (String) details.getMethodParameters().get("sliceUrn");
                    model.logExistSlice(sliceUrn);
                    List<String> sliverUrns = getSliverUrnsFromSliverInfo(info.getSliverInfo());
                    seeRequestRspec(details.getAuthority(), sliceUrn, sliverUrns, (String) details.getMethodParameters().get("rspec"));
                    seeEchoRequestRspec(details.getAuthority(), sliceUrn, sliverUrns, info.getRspec());
                    onAllocateAndProvisionInfoResult(sliceUrn, info);
                }
            }

            if (details.getJavaMethodName().equals("provision")) { //returns AllocateAndProvisionInfo
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    AggregateManager3.AllocateAndProvisionInfo info = (AggregateManager3.AllocateAndProvisionInfo) details.getReply().getValue();
                    List<String> urns = (List<String>) details.getMethodParameters().get("urns");
                    List<String> sliceUrns = getSliceUrns(urns);
                    String sliceUrn = sliceUrns.size() != 1 ? null : sliceUrns.get(0);
                    model.logExistSlice(sliceUrn);

                    //  alternative, get from result instead of arguments:
                    List<String> sliverUrns = getSliverUrnsFromSliverInfo(info.getSliverInfo());
                    //  alternative: get from arguments instead of result:
                    //List<String> sliverUrns = getSliverUrns(urns);

                    seeManifestRspec(details.getAuthority(), sliceUrn, sliverUrns, info.getRspec());
                    onAllocateAndProvisionInfoResult(sliceUrn, info);
                }
            }

            if (details.getJavaMethodName().equals("status")) // returns StatusInfo
            {
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    //not much to know from urn argument?   List urns = (List) details.getMethodParameters().get("urns");
                    //TODO if single slice URN, and success and at least one sliverinfo, that slice exists...
                    onStatusResult((AggregateManager3.StatusInfo) details.getReply().getValue());
                }
            }

            if (details.getJavaMethodName().equals("performOperationalAction")) { //returns List<SliverInfo>
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    List<AggregateManager3.SliverInfo> si = (List<AggregateManager3.SliverInfo>) details.getReply().getValue();
                    List<String> urns = (List<String>) details.getMethodParameters().get("urns");
                    List<String> sliceUrns = getSliceUrns(urns);
                    String sliceUrn = sliceUrns.size() != 1 ? null : sliceUrns.get(0);
                    onListOfSliverInfoResult(sliceUrn, si);
                }
            }

            if (details.getJavaMethodName().equals("delete")) { //returns List<SliverInfo>
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    List<AggregateManager3.SliverInfo> si = (List<AggregateManager3.SliverInfo>) details.getReply().getValue();
                    List<String> urns = (List<String>) details.getMethodParameters().get("urns");
                    List<String> sliceUrns = getSliceUrns(urns);
                    String sliceUrn = sliceUrns.size() != 1 ? null : sliceUrns.get(0);
                    onListOfSliverInfoResult(sliceUrn, si);
                }
            }

            if (details.getJavaMethodName().equals("renew")) { //returns List<SliverInfo>
                if (details.getReply().getGeniResponseCode().isSuccess()) {
                    List<AggregateManager3.SliverInfo> si = (List<AggregateManager3.SliverInfo>) details.getReply().getValue();
                    List<String> urns = (List<String>) details.getMethodParameters().get("urns");
                    List<String> sliceUrns = getSliceUrns(urns);
                    String sliceUrn = sliceUrns.size() != 1 ? null : sliceUrns.get(0);
                    onListOfSliverInfoResult(sliceUrn, si);
                }
            }

            if (details.getJavaMethodName().equals("shutdown")) { //returns Boolean
                //ignored
            }
        } catch (Exception e) {
            System.err.println("WARNING: Exception when processing AggregateManager2 reply for EasyModel. This will be ignored, but it is most likely a bug. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
