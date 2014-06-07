package be.iminds.ilabt.jfed.highlevel.api;

import be.iminds.ilabt.jfed.highlevel.model.EasyModel;
import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.history.UserInfo;
import be.iminds.ilabt.jfed.log.Logger;
import be.iminds.ilabt.jfed.lowlevel.*;
import be.iminds.ilabt.jfed.lowlevel.api.SliceAuthority;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;

import java.util.Hashtable;
import java.util.List;

/**
 * This is a convenience wrapper for the methods specified in the ProtoGeni Slice Authority API
 * Not all functionality is accessible, but the use of this class is easier, and results are processed more.
 */
public class EasySliceAuthority {
    private GeniUserProvider geniUserProvider;
    private GeniConnectionProvider connectionProvider;
    private AuthorityProvider authorityProvider;

    private EasyModel easyModel;

    private boolean checkCredentialValidity;
    private SliceAuthority sa;


    public EasySliceAuthority(Logger logger, EasyModel easyModel, AuthorityProvider authorityProvider,
                              boolean checkCredentialValidity) {
        this.geniUserProvider = easyModel.getGeniUserProvider();
        this.connectionProvider = easyModel.getHighLevelController().getConnectionProvider();
        this.authorityProvider = authorityProvider;
        this.easyModel = easyModel;

        this.checkCredentialValidity = checkCredentialValidity;
        this.sa = new SliceAuthority(logger);
    }

    public EasySliceAuthority(Logger logger, EasyModel easyModel, AuthorityProvider authorityProvider) {
        this(logger, easyModel, authorityProvider, false);
    }
    public EasySliceAuthority(Logger logger, EasyModel easyModel, final SfaAuthority authority) {
        this(logger, easyModel, new AuthorityProvider() {
            @Override
            public SfaAuthority getAuthority() {
                return authority;
            }
        }, false);
    }

    public XMLRPCCallDetails getLastLoggedResult() {
        return sa.getLastXmlRpcResult();
    }

    private GeniUser getContext() {
        return geniUserProvider.getLoggedInGeniUser();
    }
    private GeniConnection getConnection() throws GeniException {
        return connectionProvider.getConnectionByAuthority(geniUserProvider.getLoggedInGeniUser(), authorityProvider.getAuthority(), new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
    }

    public String getVersion() throws GeniException {
        SliceAuthority.SliceAuthorityReply<String> res = sa.getVersion(getConnection());

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in GetVersion: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }

    private void optionallyCheckCredential(GeniCredential cred) throws GeniException {
        //todo optionally check credential
        if (checkCredentialValidity && !cred.check())
           throw new GeniException("Credential received is not valid");
    }

    public GeniCredential getCredential() throws GeniException {
        SliceAuthority.SliceAuthorityReply<GeniCredential> res =
                sa.getCredential(getConnection());

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in GetCredential: code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        GeniCredential signedCredential = res.getValue();
        optionallyCheckCredential(signedCredential);
        return signedCredential;
    }

    public GeniCredential getCredential(Slice slice) throws GeniException {
        SliceAuthority.SliceAuthorityReply<GeniCredential> res =
                sa.getSliceCredential(getConnection(), easyModel.getUserCredential(), new ResourceUrn(slice.getUrn()));

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in GetCredential (Slice): code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        GeniCredential sliceCredential = res.getValue();
        optionallyCheckCredential(sliceCredential);

        slice.setCredential(sliceCredential);

        return sliceCredential;
    }

    /** cannot give back slice: EasyModelAggregateManager2Listener is changing in another thread */
    public SliceAuthority.SliceAuthorityReply<Hashtable> resolveSlice(Slice slice) throws GeniException {
        return resolveSlice(slice.getUrn());
    }
    /** cannot give back slice: EasyModelAggregateManager2Listener is changing in another thread */
    public SliceAuthority.SliceAuthorityReply<Hashtable> resolveSlice(String sliceUrn) throws GeniException {
        SliceAuthority.SliceAuthorityReply<Hashtable> res =
                sa.resolveSlice(getConnection(), easyModel.getUserCredential(), new ResourceUrn(sliceUrn));

//        if (!res.getGeniResponseCode().isSuccess())
//            throw new GeniException("Error in Resolve (Slice): code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        //cannot give back slice: EasyModelAggregateManager2Listener is changing in another thread
        return res;
    }

    public UserInfo ResolveUser() throws GeniException {
        return resolveUser(getContext().getUserUrn());
    }

    /** example user_urn: "urn:publicid:IDN+"+authority.getNameForUrn()+"+user+"+userName */
    public UserInfo resolveUser(String userUrn) throws GeniException {
        SliceAuthority.SliceAuthorityReply<Hashtable> res =
                sa.resolveUser(getConnection(), easyModel.getUserCredential(), new ResourceUrn(userUrn));

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in Resolve (User): userUrn=\""+userUrn+"\" code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        UserInfo userinfo = new UserInfo(res.getValue());

        if (!userinfo.getUrn().equals(userUrn))
            throw new GeniException("Resolving user  returned urn=\""+userinfo.getUrn()+"\" but I expected \""+userUrn+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        return userinfo;
    }

    public boolean bindToSlice(SfaAuthority auth, String username, Slice slice) throws GeniException {
        String userUrn = "urn:publicid:IDN+"+auth.getNameForUrn()+"+user+"+username;

        SliceAuthority.SliceAuthorityReply<Boolean> res =
                sa.bindToSlice(getConnection(), slice.getCredential(), new ResourceUrn(userUrn));

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in BindToSlice (Slice): code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }

    public Slice register(SfaAuthority authority, String sliceName) throws GeniException {
        Slice res = register("urn:publicid:IDN+"+authority.getNameForUrn()+"+slice+"+sliceName);
        return res;
    }

    /**
     *
     * @param sliceUrn Urn of the new slice.  example "urn:publicid:IDN+"+authority.getNameForUrn()+"+slice+"+sliceName;
     * @return the created slice
     * @throws GeniException
     */
    public Slice register(String sliceUrn) throws GeniException {
        if (!sliceUrn.startsWith("urn:"))
            throw new GeniException("Error in Register: sliceUrn argument is not an URN: \""+sliceUrn+"\"");

        SliceAuthority.SliceAuthorityReply<GeniCredential> res =
                sa.register(getConnection(), easyModel.getUserCredential(), new ResourceUrn(sliceUrn));

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in Register: code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        GeniCredential sliceCredential = res.getValue();
        optionallyCheckCredential(sliceCredential);

        //certain slice will exist, since easyModel will have seen this call and its result
        Slice slice = easyModel.getSlice(sliceUrn);
        assert slice != null;
        return slice;
    }


    public List<String> getSshKeys() throws GeniException {
        SliceAuthority.SliceAuthorityReply<List<String>> res =
                sa.getKeys(getConnection(), easyModel.getUserCredential());

        if (!res.getGeniResponseCode().isSuccess())
            throw new GeniException("Error in GetKeys: code="+res.getGeniResponseCode()+" output=\""+res.getOutput()+"\"", getLastLoggedResult(), res.getGeniResponseCode());

        return res.getValue();
    }
}
