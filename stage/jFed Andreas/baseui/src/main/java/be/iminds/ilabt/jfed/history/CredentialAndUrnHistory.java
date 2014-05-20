package be.iminds.ilabt.jfed.history;

import be.iminds.ilabt.jfed.log.ApiCallDetails;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.log.ResultListener;
import be.iminds.ilabt.jfed.lowlevel.ApiCallReply;
import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;

import java.util.*;

/**
 * CredentialAndUrnHistory stores all rspecs, credentials, user, slice and sliver urn's
 *
 * It also stores all ApiCallDetails
 *
 * It notifies listeners of changes
 */
public class CredentialAndUrnHistory implements ResultListener {
    public List<String> sshKeys;
    public UserInfo userInfo;

    public List<GeniCredential> userCredentialList;
    public List<GeniCredential> sliceCredentialList;
    public List<GeniCredential> chCredentialList;

    //TODO make sure sliceUrnsList is filled in
    public List<String> sliceUrnlist;
    public List<String> sliverUrnlist;
    public List<String> userUrnlist;
    public List<String> allUrnlist;
    public List<String> rspecList;

    private Map<GeniUser, GeniCredential> lastUserCredentialByContext;

    private AggregateManager2.VersionInfo am2VersionInfo;
    private AggregateManager3.VersionInfo am3VersionInfo;

    public List<ApiCallDetails> resultList;

    public CredentialAndUrnHistory(Logger logger) {
        this.resultList = new ArrayList<ApiCallDetails>();
        sshKeys =  new ArrayList<String>();
        userInfo = null;

        userCredentialList = new ArrayList<GeniCredential>();
        sliceCredentialList = new ArrayList<GeniCredential>();
        chCredentialList = new ArrayList<GeniCredential>();

        sliceUrnlist = new ArrayList<String>();
        sliverUrnlist = new ArrayList<String>();
        allUrnlist = new ArrayList<String>();
        userUrnlist = new ArrayList<String>();
        rspecList = new ArrayList<String>();

        lastUserCredentialByContext = new HashMap<GeniUser, GeniCredential>();

        am2VersionInfo = null;
        am3VersionInfo = null;

        logger.addResultListener(this);
    }

    public boolean hasSshKeys() {
        return ! sshKeys.isEmpty();
    }
    public List<String> getSshKeys() {
        return sshKeys;
    }
    public void setSshKeys(List<String> sshKeys) {
        this.sshKeys = sshKeys;
    }
    public UserInfo getUserInfo() {
        return userInfo;
    }
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setAm2VersionInfo(AggregateManager2.VersionInfo version) {
        am2VersionInfo = version;
    }
    public AggregateManager2.VersionInfo getAm2VersionInfo() {
        return am2VersionInfo;
    }

    public void setAm3VersionInfo(AggregateManager3.VersionInfo version) {
        am3VersionInfo = version;
    }
    public AggregateManager3.VersionInfo getAm3VersionInfo() {
        return am3VersionInfo;
    }

    public void addSliceUrn(String urn) {
        if (!sliceUrnlist.contains(urn))
            sliceUrnlist.add(urn);
        addAllUrn(urn);
    }
    public void addSliverUrn(String urn) {
        if (!sliverUrnlist.contains(urn))
            sliverUrnlist.add(urn);
        addAllUrn(urn);
    }
    public List<String> getSliceUrnlist() {
        return Collections.unmodifiableList(sliceUrnlist);
    }
    public List<String> getSliverUrnlist() {
        return Collections.unmodifiableList(sliverUrnlist);
    }
    public void addAllUrn(String urn) {
        if (!allUrnlist.contains(urn))
            allUrnlist.add(urn);
    }
    public List<String> getAllUrnlist() {
        return Collections.unmodifiableList(allUrnlist);
    }

    //TODO not sure if storing this per context is needed. We should probably just store the history per context...
    //TODO   so check how it is all used and fix it
    public void setUserCredential(GeniUser context, GeniCredential credential) {
        lastUserCredentialByContext.put(context, credential);
    }
    public GeniCredential getLastUserCredential(GeniUser context) {
        return lastUserCredentialByContext.get(context);
    }
    public boolean hasUserCredential(GeniUser context) {
        return lastUserCredentialByContext.get(context) != null;
    }

    public List<GeniCredential> userAndSliceCredentialList() {
        List<GeniCredential> res = new ArrayList<GeniCredential>(userCredentialList);
        for (GeniCredential sliceCred : sliceCredentialList)
            if (!res.contains(sliceCred))
                res.add(sliceCred);
        return res;
    }
    public List<GeniCredential> allCredentialList() {
        List<GeniCredential> res = new ArrayList<GeniCredential>(userCredentialList);
        for (GeniCredential sliceCred : sliceCredentialList)
            if (!res.contains(sliceCred))
                res.add(sliceCred);
        for (GeniCredential chCredential : chCredentialList)
            if (!res.contains(chCredential))
                res.add(chCredential);
        return res;
    }

    public List<ApiCallDetails> getResultList() {
        return Collections.unmodifiableList(resultList);
    }

    @Override
    public void onResult(ApiCallDetails result) {
        if (result == null)
            return;

        resultList.add(result);

        try {

            String api = result.getApiName();
            String method = result.getJavaMethodName();

            ApiCallReply reply = result.getReply();
            if (reply == null || reply.getGeniResponseCode() == null || reply.getValue() == null)
                return;

            if (reply.getGeniResponseCode().isSuccess() && reply.getValue().getClass().equals(GeniCredential.class)) {
                GeniCredential cred = (GeniCredential) reply.getValue();
                if (api.contains("Clearing House"))
                    chCredentialList.add(cred);
                else {
                    if (api.equals(SliceAuthority.getApiName()) &&
                            (method.equals("getSliceCredential") || method.equals("bindToSlice") || method.equals("register") || method.equals("renewSlice")))
                        sliceCredentialList.add(cred);
                    else {
                        if (api.equals(SliceAuthority.getApiName()) && method.equals("getCredential"))
                            userCredentialList.add(cred);
                        else {
                            //otherwise, just add to both
                            System.err.println("Warning: Api returned credential of unknown type. api=" + api + " method=" + method + " cred=" + cred);
                            sliceCredentialList.add(cred);
                            userCredentialList.add(cred);
                        }
                    }
                }
            }

            //find slice urns
            if (result.getMethodParameters().containsKey("sliceUrn")) {
                String urn = (String) result.getMethodParameters().get("sliceUrn");
                addSliceUrn(urn);
            }
            if (result.getMethodParameters().containsKey("sliverUrn")) {
                String urn = (String) result.getMethodParameters().get("sliverUrn");
                addSliverUrn(urn);
            }
            if (result.getMethodParameters().containsKey("urns")) {
                List urns = (List) result.getMethodParameters().get("urns");
                for (Object urnO : urns)
                    addAllUrn((String) urnO);
            }
            if (reply.getGeniResponseCode().isSuccess() && reply.getValue().getClass().equals(AggregateManager3.AllocateAndProvisionInfo.class)) {
                AggregateManager3.AllocateAndProvisionInfo info = (AggregateManager3.AllocateAndProvisionInfo) reply.getValue();
                for (AggregateManager3.SliverInfo si : info.getSliverInfo()) {
                    String urn = si.getSliverUrn();
                    addSliverUrn(urn);
                }
            }
            if (reply.getGeniResponseCode().isSuccess() && reply.getValue().getClass().equals(AggregateManager2.SliverStatus.class)) {
                AggregateManager2.SliverStatus info = (AggregateManager2.SliverStatus) reply.getValue();
                String urn = info.getUrn();
                addSliverUrn(urn);
                for (AggregateManager2.SliverStatus.ResourceStatus rs : info.getResources()) {
                    addAllUrn(rs.getUrn());
                }
            }
            if (reply.getGeniResponseCode().isSuccess() && reply.getValue().getClass().equals(AggregateManager3.StatusInfo.class)) {
                AggregateManager3.StatusInfo info = (AggregateManager3.StatusInfo) reply.getValue();
                String sliceUrn = info.getSliceUrn();
                addSliceUrn(sliceUrn);
                for (AggregateManager3.SliverInfo si : info.getSliverInfo()) {
                    String urn = si.getSliverUrn();
                    addSliverUrn(urn);
                }
            }
            //List<SliverInfo>
            if (reply.getGeniResponseCode().isSuccess() && reply.getValue().getClass().equals(List.class) &&
                    ((List)reply.getValue()).size() > 0 && ((List)reply.getValue()).get(0).getClass().equals(AggregateManager3.SliverInfo.class)) {
                List infos = (List) reply.getValue();
                for (Object io : infos) {
                    AggregateManager3.SliverInfo info = (AggregateManager3.SliverInfo) infos;
                    String urn = info.getSliverUrn();
                    addSliverUrn(urn);
                }
            }
//        if (result.getMethodParameters().containsKey("userUrn")) {
//            String urn = (String) result.getMethodParameters().get("userUrn");
//            addUserUrn(urn);
//        }


            //TODO update specList with createSliver and listResources result

        } catch (Exception e) {
            System.out.println("Exception in CredentialAndUrnHistory onResult. This is a bug, but is not fatal and does not have an effect on API calls (only on GUI).\n");
            e.printStackTrace();
        }
    }
}
