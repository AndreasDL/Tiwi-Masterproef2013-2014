package be.iminds.ilabt.jfed.highlevel.api;

import be.iminds.ilabt.jfed.highlevel.model.*;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.api.UserSpec;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.AggregateManager2;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * EasyAggregateManager2
 *
 * Important note: the model is being built by listening to logger data in another thread,
 * so model updates are not guaranteed to have completed when these calls return. These calls can also not return
 * any model object reliably for that reason.
 */
public class EasyAggregateManager2 {
    private GeniUserProvider geniUserProvider;
    private GeniConnectionProvider connectionProvider;
    private AuthorityProvider authorityProvider;
    private AggregateManager2 am;

    private final EasyModel easyModel;

    public EasyAggregateManager2(Logger logger, EasyModel easyModel, AuthorityProvider authorityProvider) {
        this.easyModel = easyModel;

        this.geniUserProvider = easyModel.getGeniUserProvider();
        this.connectionProvider = easyModel.getHighLevelController().getConnectionProvider();
        this.authorityProvider = authorityProvider;
        this.am = new AggregateManager2(logger);
    }

    public EasyAggregateManager2(Logger logger, EasyModel easyModel, final SfaAuthority auth) {
        this(logger, easyModel, new AuthorityProvider() { @Override public SfaAuthority getAuthority() { return auth; } });
    }

    public XMLRPCCallDetails getLastLoggedResult() {
        return am.getLastXmlRpcResult();
    }

    private GeniUser getContext() {
        return geniUserProvider.getLoggedInGeniUser();
    }
    private GeniConnection getConnection() throws GeniException {
        return connectionProvider.getConnectionByAuthority(geniUserProvider.getLoggedInGeniUser(), authorityProvider.getAuthority(), new ServerType(ServerType.GeniServerRole.AM, 2));
    }

    public int getVersion() throws GeniException {
        AggregateManager2.AggregateManagerReply<AggregateManager2.VersionInfo> version = am.getVersion(getConnection());

        if (!version.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in GetVersion: "+version.getGeniResponseCode()+" ("+version.getOutput()+")", getLastLoggedResult(), version.getGeniResponseCode());

        return version.getValue().getApi();
    }
    public RSpecInfo listResources(boolean available) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        assert easyModel.getUserCredential() != null;
        creds.add(easyModel.getUserCredential());

        String rspecType = "geni";
        String rspecVersion = "3";

        AggregateManager2.AggregateManagerReply<String> res =
                am.listResources(getConnection(), creds,
                        rspecType, rspecVersion, available, true, null, null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in ListResources: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        return new RSpecInfo(res.getValue(), RSpecInfo.RspecType.ADVERTISEMENT, null /*slice*/, null /*slivers*/,
                easyModel.getAuthorityList().get(getConnection().getGeniAuthority()));
    }
    public RSpecInfo listSliceResources(Slice slice) throws GeniException {
        if (slice.getCredential() == null)
            throw new RuntimeException("Slice credential not known");

        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        creds.add(slice.getCredential());

        String rspecType = "geni";
        String rspecVersion = "3";

        AggregateManager2.AggregateManagerReply<String> res =
                am.listResources(getConnection(), creds,
                        rspecType, rspecVersion, false/*available*/, true/*compressed*/, slice.getUrn(), null);


        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in ListResources: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        RSpecInfo r = new RSpecInfo(res.getValue(), RSpecInfo.RspecType.MANIFEST, slice, null /*slivers*/,
                easyModel.getAuthorityList().get(getConnection().getGeniAuthority()));

        return r;
    }

    /*returns success*/
    public boolean createSliver(Slice slice, RSpecInfo rspec, List<String> userUrns/*, boolean useContextKey*/) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        creds.add(slice.getCredential());

        List<UserSpec> users = new ArrayList<UserSpec>();
        for (String userUrn : userUrns) {
            System.out.println("DEBUG EasyAggregateManager2 createSliver -> userUrn="+userUrn+"  getContext().getUserUrn()="+getContext().getUserUrn());
            if (userUrn.equals(getContext().getUserUrn())) {
                System.out.println("DEBUG EasyAggregateManager2 createSliver -> easyModel.getUserKeys()="+easyModel.getUserKeys());
                if (easyModel.getUserKeys() != null && easyModel.getUserKeys().size() > 0) {
                    users.add(new UserSpec(userUrn, new Vector<String>(easyModel.getUserKeys())));
                } else
                    users.add(new UserSpec(userUrn));
            } else
                users.add(new UserSpec(userUrn));
        }

        AggregateManager2.AggregateManagerReply<String> res =
                am.createSliver(getConnection(), creds, slice.getUrn(), rspec.getStringContent(), users, null);

        return res.getGeniResponseCode().isSuccess();
        //if (!res.getGeniResponseCode().isSuccess())
            //throw new GeniException("Error in CreateSliver: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());
    }

    public boolean deleteSliver(Sliver sliver) throws GeniException {
        //because AMv2 is always geni_single
        return deleteSliver(sliver.getSlice());
    }

    public boolean deleteSliver(Slice slice) throws GeniException {
        String sliceUrn = slice.getUrn();
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        creds.add(slice.getCredential());

        AggregateManager2.AggregateManagerReply<Boolean> res =
                am.deleteSliver(getConnection(), creds, sliceUrn, null);

        //if sliver doesn't exist, also just return success
        if (res.getGeniResponseCode().equals(GeniAMResponseCode.GENIRESPONSE_SEARCHFAILED))
            return true;

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in DeleteSliver: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }

    public AggregateManager2.SliverStatus sliverStatus(Sliver sliver) throws GeniException {
        return sliverStatus(sliver.getSlice());
    }
    public AggregateManager2.SliverStatus sliverStatus(Slice slice) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        //creds.add(context.getUserCredential());
        creds.add(slice.getCredential());

        AggregateManager2.AggregateManagerReply<AggregateManager2.SliverStatus> res =
                am.sliverStatus(getConnection(), creds, slice.getUrn(), null);

        if (!res.getGeniResponseCode().isSuccess())
            return null;

        AggregateManager2.SliverStatus am2SliverStatus = res.getValue();

        return am2SliverStatus;
    }

    public boolean renewSliver(Slice slice, Date expirationTime) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        //creds.add(context.getUserCredential());
        creds.add(slice.getCredential());

        AggregateManager2.AggregateManagerReply<Boolean> res =
                am.renewSliver(getConnection(), creds, slice.getUrn(), expirationTime, null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in RenewSliver: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }

    public boolean shutdown(Slice slice) throws GeniException {
        List<GeniCredential> creds = new ArrayList<GeniCredential>();
        //creds.add(context.getUserCredential());
        creds.add(slice.getCredential());

        AggregateManager2.AggregateManagerReply<Boolean> res =
                am.shutdown(getConnection(), creds, slice.getUrn(), null);

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in Shutdown: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }
}
