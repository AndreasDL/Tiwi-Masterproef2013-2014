package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.lowlevel.authority.DebuggingAuthorityList;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.CommonsHttpClientXmlRpcTransportFactory;
import be.iminds.ilabt.jfed.util.GeniTrustStoreHelper;
import org.apache.xmlrpc.XmlRpcClient;

import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.LinkedList;

/**
 * GeniConnection: XmlRpc over HTTPs
 */
public class GeniSslConnection implements GeniConnection {
    private String serverUrl;

    private CommonsHttpClientXmlRpcTransportFactory xmlRpcTransportFactory;
    private XmlRpcClient xmlRpcClient;
    private boolean error;


    /* debug will print out a info during the call */
    private boolean debugMode;
    private SfaAuthority geniAuthority;

    /** The entire connection is faked for debugging purposes. Nothing really happens */
    public boolean fakeForDebugging = false;

    /** debug mode will print out a info during the call */
    @Override
    public boolean isDebugMode() {
        return debugMode;
    }

    /** if isFakeForDebugging is true, the entire connection is faked for debugging purposes. Nothing really happens! */
    @Override
    public boolean isFakeForDebugging() {
        return fakeForDebugging;
    }

    /**
     * @param certificate X509Certificate to use for the SSL connection
     * @param privateKey PrivateKey to use for client authentication of the SSL connection
     * //@param allowedCertificateHostnameAliases may be null or empty
     * @throws GeniException
     */
    public GeniSslConnection(SfaAuthority geniAuthority, String serverUrl, X509Certificate certificate,
                             PrivateKey privateKey,
                             boolean debugMode, ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback) throws GeniException {
        error = true;
        this.debugMode = debugMode;

        this.geniAuthority = geniAuthority;

        if (geniAuthority != null && DebuggingAuthorityList.isDebuggingAuth(geniAuthority)) {
            fakeForDebugging = true;
            error = false;
            this.serverUrl = serverUrl;
            return;
        }

        if (serverUrl == null) throw new IllegalArgumentException("Illegal argument: serverURL == null");
        if (certificate == null) throw new IllegalArgumentException("Illegal argument: certificate == null");
        if (privateKey == null) throw new IllegalArgumentException("Illegal argument: privateKey == null");

        this.serverUrl = serverUrl;

        if (geniAuthority != null && geniAuthority.getPemSslTrustCert() != null)
            GeniTrustStoreHelper.addTrustedPemCertificateIfNotAdded(geniAuthority.getPemSslTrustCert());
        KeyStore trustStore = GeniTrustStoreHelper.getFullTrustStore();

        xmlRpcTransportFactory = new ClientSslAuthenticationXmlRpcTransportFactory(
                certificate,
                privateKey,
                serverUrl,
                trustStore,
                geniAuthority == null ? new LinkedList<String>() : geniAuthority.getAllowedCertificateHostnameAliases(),
                debugMode, handleUntrustedCallback);
        try {
            xmlRpcClient = new XmlRpcClient(new URL(serverUrl), xmlRpcTransportFactory);
            error = false;
        } catch (Exception e) {
            throw new GeniException("Error creating XmlRpcClient in GeniConnection constructor: "+e.getMessage(), e);
        }
    }

    @Override
    public CommonsHttpClientXmlRpcTransportFactory getXmlRpcTransportFactory() {
        assert !fakeForDebugging : "Error: This connection is a fake debugging connection, yet the CommonsHttpClientXmlRpcTransportFactory was requested";
        return xmlRpcTransportFactory;
    }

    @Override
    public XmlRpcClient getXmlRpcClient() {
        assert !fakeForDebugging : "Error: This connection is a fake debugging connection, yet the XmlRpcClient was requested";
        return xmlRpcClient;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public void markError() {
        error = true;
    }

    @Override
    public boolean isError() {
        return error;
    }

    /**
     * @return the GeniAuthority this connection is connecting to, or null if not applicable
     * */
    @Override
    public SfaAuthority getGeniAuthority() {
        return geniAuthority;
    }
}