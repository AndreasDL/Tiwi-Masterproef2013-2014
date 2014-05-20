package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.history.UserInfo;
import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.Gid;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceId;
import be.iminds.ilabt.jfed.util.RFC3339Util;
import javafx.application.Platform;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EasyModelSliceAuthorityListener: this watches for SliceAuthority calls and fills in EasyModel using the info in them
 */
public class EasyModelSliceAuthorityListener extends EasyModelAbstractListener {

    public EasyModelSliceAuthorityListener(EasyModel model) {
        super(model);
    }

    private void onGetVersionResult(ApiCallDetails result) {
        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //not really anything to store
        }
    }

    private void onGetSliceCredentialResult(ApiCallDetails result) {
        ResourceId sliceId = noteSliceUrnInParameters(result);
        noteUserCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            if (sliceId.getType().equals("urn")) {
                String sliceUrn = sliceId.getValue();
                Slice slice = model.getSlice(sliceUrn);
                assert slice != null;
                slice.setCredential((GeniCredential) result.getReply().getValue());
//                model.fireSliceChanged(slice);
            }
        }
    }

    private void onGetCredentialResult(ApiCallDetails result) {
        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //store user credential
            GeniCredential userCredential = (GeniCredential)result.getReply().getValue();
            model.setUserCredential(userCredential);
        }
    }

    private void onResolveSliceResult(ApiCallDetails result) throws GeniException {
        ResourceId sliceId = noteSliceUrnInParameters(result);
        noteUserCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            if (sliceId.getType().equals("urn")) {
                Hashtable sliceInfo = (Hashtable) result.getReply().getValue();

                String sliceUrn = sliceId.getValue();
                Slice slice = model.getSlice(sliceUrn);
                assert slice != null;

                String resolveUrn = sliceInfo.get("urn").toString();
                if (!resolveUrn.equals(sliceUrn))
                    throw new GeniException("URN in Resolve answer (\""+resolveUrn+"\") is NOT the same as urn of slice (\""+sliceUrn+"\").");

                slice.uuidProperty().setValue(sliceInfo.get("uuid").toString());
                slice.creator_uuidProperty().setValue(sliceInfo.get("creator_uuid").toString());
                slice.creator_urnProperty().setValue(sliceInfo.get("creator_urn").toString());
                slice.setGid(new Gid(sliceInfo.get("gid").toString()));


                //log existence and non existence of slivers
                if ( sliceInfo.get("component_managers") instanceof Vector) {
                    Vector v = (Vector) sliceInfo.get("component_managers");
                    List<String> componentManagerUrns = new ArrayList<String>();
                    for (Object cm : v) {
                        String cmUrn = (String) cm;
                        componentManagerUrns.add(cmUrn);

                        AuthorityInfo ai = model.getAuthorityList().get(cmUrn);
                        if (ai == null) continue;
                        SfaAuthority auth = ai.getGeniAuthority();
                        assert auth != null;

                        //log existence of sliver
                        //note that can we know anything for sure? we don't know if sliver is on AM with geni_single true...
                        model.logExistSliver(sliceUrn, auth);
                    }
                    List<Sliver> slivers = slice.getSlivers();
                    for (Sliver sliver : slivers) {
                        if (!componentManagerUrns.contains(sliver.getAuthority().getUrn()))
                            model.logNotExistSliverGeniSingle(slice.getUrn(), sliver.getAuthority());
                    }
                }
            }
        }
    }

    private void onResolveUserResult(ApiCallDetails result) {
        noteUserCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            assert result.getAuthority() != null;
            //store user info
            UserInfo userinfo = new UserInfo((Hashtable) result.getReply().getValue());
            model.setUserInfo(userinfo);

            //log slice names
            for (String sliceUrn : userinfo.getSlices()) {
                model.logExistSliceUrn(result.getAuthority(), sliceUrn);
            }
        }
    }

    private void onBindToSliceResult(ApiCallDetails result) {
        noteSliceCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //currently, we don't store slice sharing info
        }
    }

    private void onRegisterResult(ApiCallDetails result) {
        noteUserCredentialInParameters(result);
        ResourceId sliceId = noteSliceUrnInParameters(result);
        //we could also check other failures, for example, when calling register when a slice already exists.
        //but it's not really needed

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            if (sliceId.getType().equals("urn")) {
                String sliceUrn = sliceId.getValue();
                Slice slice = model.getSlice(sliceUrn);
                assert slice != null;
                slice.setCredential((GeniCredential) result.getReply().getValue());
//                model.fireSliceChanged(slice);
            }
        }
    }

    private void onRenewSliceResult(ApiCallDetails result) throws ParseException {
        ResourceId sliceId = noteSliceCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //register expiration_rfc3339
            String expiration_rfc3339 = (String) result.getReply().getValue();
            String sliceUrn = sliceId.getValue();
            Slice slice = model.getSlice(sliceUrn);
            assert slice != null;
            slice.setExpirationDate(RFC3339Util.rfc3339StringToDate(expiration_rfc3339));
//            model.fireSliceChanged(slice);
        }
    }

    private void onShutdownResult(ApiCallDetails result) {
        ResourceId sliceId = noteSliceCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //todo: shutdown is currently handled as if the slice does not exists
            model.logNotExistSlice(sliceId.getValue());
        }
    }

    private void onGetKeysResult(ApiCallDetails result) {
        if (result.getReply().getGeniResponseCode().isSuccess()) {
            List keyStrings = (List) result.getReply().getValue();

            List<String> keys = new ArrayList<String>();
            for (Object k : keyStrings) keys.add((String) k);

            model.setUserKeys(keys);
        }
    }

    private void onRemoveResult(ApiCallDetails result) {
        ResourceId sliceId = noteSliceCredentialInParameters(result);

        if (result.getReply().getGeniResponseCode().isSuccess()) {
            //on success, noteSliceCredentialInParameters will have inferred the slice existed.
            // That WAS correct at the time of the call, but by the end of the call, it doesn't exist anymore...
            model.logNotExistSlice(sliceId.getValue());
        }
    }

    @Override
    public void onResult(final ApiCallDetails details) {
        //Since this triggers updates to the GUI (because it changes Properties) this has to be executed on the JavaFX thread.

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
    public void onResultInJavaFXThread(ApiCallDetails result) {
        //ignore errors here
        if (result.getReply() == null || result.getJavaMethodName() == null)
            return;

        if (!result.getApiName().equals(SliceAuthority.getApiName()))
            return;

        try {
            if (result.getJavaMethodName().equals("getVersion"))
                onGetVersionResult(result);

            if (result.getJavaMethodName().equals("getSliceCredential"))
                onGetSliceCredentialResult(result);

            if (result.getJavaMethodName().equals("getCredential"))
                onGetCredentialResult(result);

            if (result.getJavaMethodName().equals("resolveSlice"))
                onResolveSliceResult(result);

            if (result.getJavaMethodName().equals("resolveUser"))
                onResolveUserResult(result);

            if (result.getJavaMethodName().equals("bindToSlice"))
                onBindToSliceResult(result);

            if (result.getJavaMethodName().equals("register"))
                onRegisterResult(result);

            if (result.getJavaMethodName().equals("renewSlice"))
                onRenewSliceResult(result);

            if (result.getJavaMethodName().equals("shutdown"))
                onShutdownResult(result);

            if (result.getJavaMethodName().equals("getKeys"))
                onGetKeysResult(result);

            if (result.getJavaMethodName().equals("remove"))
                onRemoveResult(result);
        } catch (Exception e) {
            System.err.println("WARNING: Exception when processing SliceAuthority reply for EasyModel. This will be ignored, but it is most likely a bug. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
