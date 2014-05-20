package be.iminds.ilabt.jfed.highlevel.api;

import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.highlevel.model.RSpecInfo;
import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.highlevel.model.Sliver;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager3;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.RFC3339Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * EasyAggregateManager3
 *
 * convenience methods for AMv3
 *
 * TODO: use versionInfo result and base besteffort and others on that.
 *
 * TODO: this should call SA methods and other needed methods whenever necessary
 *
 */
public class EasyAggregateManager3 {
    private GeniUserProvider geniUserProvider;
    private GeniConnectionProvider connectionProvider;
    private AuthorityProvider authorityProvider;
    private EasyModel easyModel;
    private AggregateManager3 am;

    public EasyAggregateManager3(Logger logger, EasyModel easyModel,
                                 AuthorityProvider authorityProvider) {
        this.geniUserProvider = easyModel.getGeniUserProvider();
        this.connectionProvider = easyModel.getHighLevelController().getConnectionProvider();
        this.authorityProvider = authorityProvider;
        this.easyModel = easyModel;
        this.am = new AggregateManager3(logger);
    }

    public EasyAggregateManager3(Logger logger, EasyModel easyModel,
                                 final SfaAuthority auth) {
        this(logger, easyModel, new AuthorityProvider() { @Override public SfaAuthority getAuthority() { return auth; } });
    }

    public XMLRPCCallDetails getLastLoggedResult() {
        return am.getLastXmlRpcResult();
    }

    private GeniUser getContext() {
        return geniUserProvider.getLoggedInGeniUser();
    }
    private GeniConnection getConnection() throws GeniException {
        return connectionProvider.getConnectionByAuthority(geniUserProvider.getLoggedInGeniUser(), authorityProvider.getAuthority(), new ServerType(ServerType.GeniServerRole.AM, 3));
    }

    public int getVersion() throws GeniException {
        AggregateManager3.AggregateManagerReply<AggregateManager3.VersionInfo> version = am.getVersion(getConnection());

        if (!version.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in GetVersion: "+version.getGeniResponseCode()+" ("+version.getOutput()+")", getLastLoggedResult(), version.getGeniResponseCode());

        return version.getValue().getApi();
    }
    public RSpecInfo listResources(boolean available) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();

        String rspecType = "geni";
        String rspecVersion = "3";

        AggregateManager3.AggregateManagerReply<String> res =
                am.listResources(getConnection(), creds,
                        rspecType, rspecVersion, available, true, null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in ListResources: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        RSpecInfo rSpecInfo = new RSpecInfo(res.getValue(), RSpecInfo.RspecType.ADVERTISEMENT, null /*slice*/, null /*slivers*/,
                        easyModel.getAuthorityList().get(getConnection().getGeniAuthority()));

        return rSpecInfo;
    }
    public RSpecInfo describe(Slice slice) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();

        String rspecType = "geni";
        String rspecVersion = "3";

        List<String> sliceUrnInList = new ArrayList<String>();
        sliceUrnInList.add(slice.getUrn());

        AggregateManager3.AggregateManagerReply<AggregateManager3.ManifestInfo> res =
                am.describe(getConnection(), sliceUrnInList, creds,
                        rspecType, rspecVersion, true/*compressed*/, null);


        if (!res.getGeniResponseCode().isSuccess())
                    throw new GeniException("Error in ListResources: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        RSpecInfo r = new RSpecInfo(res.getValue().getManifestRspec(), RSpecInfo.RspecType.MANIFEST, slice /*slice*/, null /*slivers*/,
                        easyModel.getAuthorityList().get(getConnection().getGeniAuthority()));
        return r;
    }

    public Sliver createSliver(Slice slice, RSpecInfo rspec) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        creds.add(slice.getCredential());

        List<UserSpec> users = new ArrayList<UserSpec>();

        List<String> sliceUrnInList = new ArrayList<String>();
        sliceUrnInList.add(slice.getUrn());

        String rspecType = "geni";
        String rspecVersion = "3";

        String userUrn = geniUserProvider.getLoggedInGeniUser().getUserUrn();
        if (easyModel.getUserKeys().isEmpty())
            users.add(new UserSpec(userUrn));
        else
            users.add(new UserSpec(userUrn, new Vector<String>(easyModel.getUserKeys())));

        AggregateManager3.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> resAllocate =
                am.allocate(getConnection(), creds, slice.getUrn(), rspec.getStringContent(), null/*optional endtime*/, null);
        if (!resAllocate.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in CreateSliver: "+resAllocate.getGeniResponseCode()+" ("+resAllocate.getOutput()+")",
                    getLastLoggedResult(), resAllocate.getGeniResponseCode());

        String sliverUrn = null;
        assert resAllocate.getValue().getSliverInfo().size() == 1;
        for (AggregateManager3.SliverInfo si : resAllocate.getValue().getSliverInfo()) {
            sliverUrn = si.getSliverUrn();
            if (!si.getAllocationStatus().equals("geni_allocated"))
                throw new GeniException("Error in CreateSliver: returned sliver \""+sliverUrn+"\" was not allocated. allocationStatus="+si.getAllocationStatus(),
                        getLastLoggedResult(), resAllocate.getGeniResponseCode());
        }
        assert sliverUrn != null;
        Sliver sliver = easyModel.getSliver(sliverUrn);

        //no need to wait   waitForStatus(true, "geni_allocated", true, null, sliceUrnInList, creds);

        AggregateManager3.AggregateManagerReply<AggregateManager3.AllocateAndProvisionInfo> resProvision =
                am.provision(getConnection(), sliceUrnInList, creds, rspecType, rspecVersion, null/*besteffort*/, rspec.getStringContent(), users, null);
        if (!resProvision.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in CreateSliver: "+resProvision.getGeniResponseCode()+" ("+resProvision.getOutput()+")", getLastLoggedResult(), resProvision.getGeniResponseCode());

        //wait until operational status is not "geni_pending_allocation" anymore
        waitForStatus(true, "geni_allocated", false, "geni_pending_allocation", sliceUrnInList, creds);

        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> resAction =
                am.performOperationalAction(getConnection(), sliceUrnInList, creds, "geni_start", null/*besteffort*/, null);
        if (!resAction.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in CreateSliver: "+resAction.getGeniResponseCode()+" ("+resAction.getOutput()+")", getLastLoggedResult(), resAction.getGeniResponseCode());

        //wait until operational status is "geni_ready"
        waitForStatus(true, "geni_allocated", true, "geni_ready", sliceUrnInList, creds);

        return sliver;
    }


    /**
     * Warning: asserts exactly 1 sliver in result
     *
     * targetAllocationStatus and targetOperationalStatus may be null -> in that case, that state won't be waited for
     * */
    protected void waitForStatus(boolean equalsAllocationStatus, String targetAllocationStatus,
                              boolean equalsOperationalStatus, String targetOperationalStatus,
                              List<String> urns, List<GeniCredential> creds) throws GeniException {
        long now = System.currentTimeMillis();
        long deadline = now + (20*60*1000);

        while (now < deadline) {
            AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> reply =
                am.status(getConnection(), urns, creds, null/*best effort*/, null);

            AggregateManager3.StatusInfo statusInfo = reply.getValue();
            List<AggregateManager3.SliverInfo> sliverInfos = statusInfo.getSliverInfo();
            assert sliverInfos.size() >= 1;
            assert sliverInfos.size() == 1;
            AggregateManager3.SliverInfo sliverInfo = sliverInfos.get(0);

            int ok = 0;
            if (targetAllocationStatus == null || sliverInfo.getAllocationStatus().equals(targetAllocationStatus) == equalsAllocationStatus)  {
                ok++;
            }
            if (targetOperationalStatus == null || sliverInfo.getOperationalStatus().equals(targetOperationalStatus) == equalsOperationalStatus)  {
                ok++;
            }
            if (ok == 2)
                return;

//            System.out.println("Trying again in 30 seconds...");
            try {
                Thread.sleep(30000 /*ms*/);
            } catch (InterruptedException e) { /* Ignore*/ }
            now = System.currentTimeMillis();
        }
        throw new RuntimeException("Sliver did not reach requested status within 20 minutes!");
    }

    public boolean delete(Sliver sliver) throws GeniException {
        String sliceUrn = sliver.getSlice().getUrn();
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        //creds.add(context.getUserCredential());
        creds.add(sliver.getSlice().getCredential());

        List<String> urns = new ArrayList<String>();
        urns.add(sliver.getSlice().getUrn());

        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> res =
                am.delete(getConnection(), urns, creds, null/*besteffort*/, null);

        if (res.getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED)) {
            Slice slice = sliver.getSlice();
            slice.removeSliver(sliver);
            return true;
        }

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in DeleteSliver: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        boolean success = false;
        for (AggregateManager3.SliverInfo si : res.getValue()) {
            String sliverUrn = si.getSliverUrn();
            if (si.getAllocationStatus().equals("geni_unallocated") && sliverUrn.equals(sliver.getUrn())) {
                sliver.getSlice().removeSliver(sliver);
                success = true;
            }
            else {
                //some other
            }
        }

        return success;
    }

    public Sliver updateStatus(Sliver sliver) throws GeniException {
        return updateStatus(sliver.getSlice());
    }
    public Sliver updateStatus(Slice slice) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        creds.add(slice.getCredential());

        List<String> sliceUrnInList = new ArrayList<String>();
        sliceUrnInList.add(slice.getUrn());

        AggregateManager3.AggregateManagerReply<AggregateManager3.StatusInfo> res =
                am.status(getConnection(), sliceUrnInList, creds, null/*besteffort*/, null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in SliverStatus: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        List<Sliver> slivers = slice.findSlivers(authorityProvider.getAuthority());
        if (slivers.isEmpty())
            return null;
        assert slivers.size() == 1;
        return slivers.get(0);
    }

    public boolean renewSliver(Slice slice, Date expirationTime) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        //creds.add(context.getUserCredential());
        creds.add(slice.getCredential());

        List<String> sliceUrnInList = new ArrayList<String>();
        sliceUrnInList.add(slice.getUrn());

        String dateString = RFC3339Util.dateToRFC3339String(expirationTime);

        AggregateManager3.AggregateManagerReply<List<AggregateManager3.SliverInfo>> res =
                am.renew(getConnection(), sliceUrnInList, creds, dateString, null/*besteffor*/, null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in RenewSliver: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        if (res.getValue().size() != 1) {
            System.err.println("Warning: EasyAggregateManager3.renewSliver could not handle response correctly. Assuming failure to set new expiration time...");
            return false;
        }

        return res.getValue().get(0).getExpires().equals(dateString);
    }

    public boolean shutdown(Slice slice) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        //creds.add(context.getUserCredential());
        creds.add(slice.getCredential());

        AggregateManager3.AggregateManagerReply<Boolean> res =
                am.shutdown(getConnection(), creds, slice.getUrn(), null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in Shutdown: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }
}
