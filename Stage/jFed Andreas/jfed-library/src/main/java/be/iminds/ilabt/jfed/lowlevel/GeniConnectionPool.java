package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.lowlevel.api.*;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.GeniTrustStoreHelper;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * GeniConnectionPool
 */
public class GeniConnectionPool implements GeniConnectionProvider {
    private class AuthTypePair {
        private SfaAuthority authority;
        private ServerType serverType;

        private AuthTypePair(SfaAuthority authority, ServerType serverType) {
            if (authority == null) throw new RuntimeException("authority == null");
            this.authority = authority;
            this.serverType = serverType;
        }

        public SfaAuthority getAuthority() {
            return authority;
        }

        public ServerType getServerType() {
            return serverType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuthTypePair that = (AuthTypePair) o;

            if (!authority.equals(that.authority)) return false;
            if (serverType != that.serverType) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = authority.hashCode();
            result = 31 * result + serverType.hashCode();
            return result;
        }
    }
    private Map<AuthTypePair, GeniConnection> conPool;
    private GeniConnection chCon;
    private String chServerUrl;



    /* debug will print out a info during the call */
    private boolean debugMode;
    /** debug mode will print out a info during the call */
    public boolean isDebugMode() {
        return debugMode;
    }
    /** debug mode will print out a info during the call */
    public void setDebugMode(boolean debugMode) {
        boolean prevMode = this.debugMode;
        this.debugMode = debugMode;

        if (debugMode != prevMode) {
            //TODO close connections
            conPool.clear();
            chCon = null;
        }
    }



    public GeniConnectionPool() {
        this.chServerUrl = "https://www.emulab.net/protogeni/xmlrpc/ch";
        conPool = new HashMap<AuthTypePair, GeniConnection>();
        chCon = null;
    }

    @Override
    public GeniConnection getConnectionByAuthority(GeniUser user, SfaAuthority authority, Class targetClass) throws GeniException {
//        assert user != null;
        assert authority != null;
        assert targetClass != null;

        if (authority == null) throw new RuntimeException("authority == null");
        if (targetClass.equals(AggregateManager3.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.AM, 3));
        if (targetClass.equals(AggregateManager2.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.AM, 2));
        if (targetClass.equals(SliceAuthority.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1));
        if (targetClass.equals(ProtoGeniClearingHouse1.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.PROTOGENI_CH, 1));
        if (targetClass.equals(PlanetlabSfaRegistryInterface.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1));
        if (targetClass.equals(StitchingComputationService.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.SCS, 1));
        if (targetClass.equals(UniformFederationRegistryApi.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.GENI_CH, 0));
        if (targetClass.equals(UniformFederationMemberAuthorityApi.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.GENI_CH_MA, 0));
        if (targetClass.equals(UniformFederationSliceAuthorityApi.class)) return getConnectionByAuthority(user, authority, new ServerType(ServerType.GeniServerRole.GENI_CH_SA, 0));
        throw new GeniException("Cannot get connection for "+targetClass.getName());
    }

    public GeniConnection getConnectionByUserAuthority(GeniUser user, ServerType serverType) throws GeniException {
        assert serverType != null;
        assert user != null;

        assert user.getUserAuthority() != null : "getConnectionByUserAuthority requires that the user's authority is known";
        SfaAuthority authority = user.getUserAuthority();
        assert authority != null;

        return getConnectionByAuthority(user, authority, serverType);
    }


    /** TODO: not cached yet */
    @Override
    public GeniConnection getConnectionByUrl(GeniUser user, URL serverUrl, ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback) throws GeniException {
        assert serverUrl != null;

        GeniConnection con;

        if (serverUrl.getProtocol().equals("https")) {
            assert user != null : "HTTPS connections require that a user is logged in";

            con = new GeniSslConnection(null, serverUrl.toString(), user.getCertificate(), user.getPrivateKey(), debugMode, handleUntrustedCallback);
        }
        else
            con = new GeniPlainConnection(null, serverUrl.toString(), debugMode);

        return con;
    }

    @Override
    public GeniConnection getConnectionByAuthority(GeniUser user, SfaAuthority authority, ServerType serverType) throws GeniException {
        assert serverType != null;
        if (authority == null) throw new RuntimeException("authority == null");
        assert authority != null;

        AuthTypePair pair = new AuthTypePair(authority, serverType);
        GeniConnection con = conPool.get(pair);

        if (con != null && con.isError()) {
            //something went wrong, we'll kill the connection
            conPool.remove(pair);
            con = null;
        }

        if (con == null) {
            URL serverUrl = authority.getUrl(serverType);
            if (serverUrl == null)
                throw new RuntimeException("Connection type has no URL: "+serverType);

            if (serverUrl.getProtocol().equals("https")) {
                assert user != null : "HTTPS connections require that a user is logged in";
                if (authority.getPemSslTrustCert() != null) {
                    if (authority.getPemSslTrustCert() != null)
                        GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(authority.getPemSslTrustCert());
                    if (user.getUserAuthority() != null && user.getUserAuthority().getPemSslTrustCert() != null)
                        GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(user.getUserAuthority().getPemSslTrustCert());
                }
                con = new GeniSslConnection(authority, serverUrl.toString(), user.getCertificate(), user.getPrivateKey(), debugMode, null/*handleUntrustedCallback*/);
            }
            else
                con = new GeniPlainConnection(authority, serverUrl.toString(), debugMode);

            //reuse connection next time, if allowed
            if (!authority.isReconnectEachTime())
                conPool.put(pair, con);
        }

        return con;
    }
}
