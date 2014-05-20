package be.iminds.ilabt.jfed.util;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;


/**
 * Non encrypted HTTP connection
 */
public class ClientPlainAuthenticationXmlRpcTransportFactory extends CommonsHttpClientXmlRpcTransportFactory {
    /**
     * @param serverUrl URL of server to connect to
     */
    public ClientPlainAuthenticationXmlRpcTransportFactory(String serverUrl, boolean debugMode) throws GeniException {
        super(serverUrl, getHttpClient(serverUrl, debugMode), debugMode);
    }


    private static DefaultHttpClient getHttpClient(String serverUrlStr, boolean debugMode) throws GeniException {
        if (serverUrlStr == null) throw new RuntimeException("serverUrlStr == null");

        final URL serverUrl;
        try {
            serverUrl = new URL(serverUrlStr);
        } catch (MalformedURLException e) {
            System.err.print("ERROR: MalformedURLException url=\""+serverUrlStr+"\"");
            e.printStackTrace();
            return null;
        }

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 120000); //read timeout in ms (default: 0 = infinite)   (long timeout, as some calls take time to finish)
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); //connection timeout in ms (default: 0 = infinite)

        return new DefaultHttpClient(params);
    }
}
