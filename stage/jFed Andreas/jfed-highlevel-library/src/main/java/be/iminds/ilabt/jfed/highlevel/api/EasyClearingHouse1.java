package be.iminds.ilabt.jfed.highlevel.api;


/**
 * EasyCleaningHouse1
 */
public class EasyClearingHouse1 {

    //TODO needs to be brought up to date with current working:
     //  - use easymodel instead of CredentialAndUrnHistory
     //  - do not use EasyContext
     //  etc

//    private GeniUserProvider geniUserProvider;
//    private GeniConnectionProvider connectionProvider;
//    private AuthorityProvider authorityProvider;
//    private ClearingHouse1 ch;
//    private EasyContext easyContext;
//
//    public EasyClearingHouse1(EasyContext easyContext, GeniUserProvider geniUserProvider, GeniConnectionProvider connectionProvider,
//                              AuthorityProvider authorityProvider,
//                              Logger logger) {
//
//        this.easyContext = easyContext;
//        this.geniUserProvider = geniUserProvider;
//        this.connectionProvider = connectionProvider;
//        this.authorityProvider = authorityProvider;
//        this.ch = new ClearingHouse1(logger);
//    }
//
//    public XMLRPCCallDetails getLastLoggedResult() {
//        return ch.getLastXmlRpcResult();
//    }
//
//    private GeniUser getContext() {
//        return geniUserProvider.getLoggedInGeniUser();
//    }
//    private GeniConnection getConnection() throws GeniException {
//        return connectionProvider.getChConnection(geniUserProvider.getLoggedInGeniUser());
//    }
//
//
//
//    public int getVersion() throws GeniException {
//        ClearingHouse1.ClearingHouseReply<Integer> version = ch.getVersion(getConnection());
//
//        if (!version.getGeniResponseCode().isSuccess())
//            throw new GeniException("Error in GetVersion: "+version.getGeniResponseCode()+" ("+version.getOutput()+")", getLastLoggedResult(), version.getGeniResponseCode());
//
//        return version.getValue();
//    }
//
//    /**
//     *
//     * @param authList the authlist in which to to modify existing GeniAuthorities, and add new GeniAuthorities.
//     *                 May NOT be null.
//     * @return list with all GeniAuthorities found. This does not include any authority that was already in authList but
//     *         was not seen in ListComponents.
//     * @throws GeniException
//     */
//    public List<SfaAuthority> listComponents(AuthorityListModel authList) throws GeniException {
//        GeniCredential userCredential = model.
//        ClearingHouse1.ClearingHouseReply<Vector<ClearingHouse1.ComponentInfo>> res = ch.listComponents(getConnection(), userCredential);
//
//        if (!res.getGeniResponseCode().isSuccess())
//            throw new GeniException("Error in ListComponents: "+res.getGeniResponseCode()+" ("+res.getOutput()+")", getLastLoggedResult(), res.getGeniResponseCode());
//
//        SfaAuthority oldSelectedAuthority = null;
//        SfaAuthority newSelectedAuthority = null;
//        if (easyContext != null) {
//            oldSelectedAuthority = easyContext.getSelectedAuthority();
//        }
//
//        List<SfaAuthority> r = new ArrayList<SfaAuthority>();
//        for (ClearingHouse1.ComponentInfo ci : res.getValue()) {
//            try {
//                /*
//                * Urn's have form:    urn:publicid:IDN+emulab.net+authority+cm
//                * URL's have form:    https://www.emulab.net:12369/protogeni/xmlrpc/cm
//                * */
//
//                SfaAuthority auth = authList.getByUrn(ci.getUrn());
//                if (auth == null)
//                    auth = new SfaAuthority(ci.getUrn());
//
//                String url = ci.getUrl();
//                if (!url.contains("/protogeni/xmlrpc/cm"))
//                    throw new GeniException("GeniAuthority url is not a known emulab format: \""+url+"\"");
//                String baseUrl = url.replace("protogeni/xmlrpc/cm", "protogeni/xmlrpc");
//                try {
//                    URL saUrl = new URL(baseUrl + "/sa");
//                    URL amUrl = new URL(baseUrl + "/am");
//                    URL am2Url = new URL(baseUrl + "/am/2.0");
//                    URL am3Url = new URL(baseUrl + "/am/3.0");
//
//                    auth.updateUrl(new ServerType(ServerType.GeniServerRole.SA, 1), saUrl);
//                    auth.updateUrl(new ServerType(ServerType.GeniServerRole.AM, 1), amUrl);
//                    auth.updateUrl(new ServerType(ServerType.GeniServerRole.AM, 2), am2Url);
//                    auth.updateUrl(new ServerType(ServerType.GeniServerRole.AM, 3), am3Url);
//                } catch (MalformedURLException e) { throw new GeniException("GeniAuthority url is not correct: \""+url+"\"", e); }
//
//                if (ci.getGid().hasError())
//                    System.err.println("WARNING: Ignoring error in gid for GeniAuthority: "+auth);
//
//                //Keep selection fixed: this works both if we modify the old one and if we make a new one
//                if (oldSelectedAuthority != null && auth.equals(oldSelectedAuthority))
//                    newSelectedAuthority = auth;
//
//                r.add(auth);
//            } catch (GeniException e) {
//                System.err.println("Warning: Ignoring a received GeniAuthority: Error while processing GeniAuthority info: " + ci);
//                e.printStackTrace();
//            }
//
//            authList.fireChange();
//        }
//
//        if (easyContext != null)
//            easyContext.setAuthorityList(r, newSelectedAuthority);
//
//        return r;
//    }
}
