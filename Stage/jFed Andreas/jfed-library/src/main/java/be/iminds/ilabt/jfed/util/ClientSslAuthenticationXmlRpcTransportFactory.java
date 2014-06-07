package be.iminds.ilabt.jfed.util;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;


/**
 * This XmlRpcTransportFactory will read a PEM certificate with a client key and certificate,
 * add the needed trust certificates, and setup an SSL connection. This SSL connection will be used to for the XmlRpc
 * calls.
 */
public class ClientSslAuthenticationXmlRpcTransportFactory extends CommonsHttpClientXmlRpcTransportFactory {


    /**
     * @param certificate X509Certificate to use for the SSL connection
     * @param privateKey PrivateKey to use for client authentication of the SSL connection
     * @param serverUrl URL of server to connect to
     * @param trustStore The trustStore with trusted roots and trusted self signed certificates, that will be used for the SSL connection
     */
    public ClientSslAuthenticationXmlRpcTransportFactory(X509Certificate certificate,
                                                         PrivateKey privateKey,
                                                         String serverUrl,
                                                         KeyStore trustStore,
                                                         List<String> allowedCertificateHostnameAliases,
                                                         boolean debugMode, HandleUntrustedCallback handleUntrustedCallback) throws GeniException {
        super(serverUrl, getHttpClient(certificate, privateKey, serverUrl,
                trustStore, allowedCertificateHostnameAliases, debugMode, handleUntrustedCallback), debugMode);
    }

    public static interface HandleUntrustedCallback {
        public boolean trust(SSLCertificateDownloader.SSLCertificateJFedInfo sSLCertificateJFedInfo);
    }
    public static class INSECURE_TRUSTALL_HandleUntrustedCallback implements HandleUntrustedCallback {
        public INSECURE_TRUSTALL_HandleUntrustedCallback() {
            System.err.println("SECURITY WARNING: constructing INSECURE_TRUSTALL_HandleUntrustedCallback");
        }
        @Override
        public boolean trust(SSLCertificateDownloader.SSLCertificateJFedInfo sSLCertificateJFedInfo) {
            return true;
        }
    }

    private static DefaultHttpClient getHttpClient(X509Certificate certificate,
                                                   PrivateKey privateKey,
                                                   String serverUrlStr,
                                                   KeyStore trustStore,
                                                   final List<String> allowedCertificateHostnameAliasesOrig,
                                                   boolean debugMode, final HandleUntrustedCallback handleUntrustedCallback) throws GeniException {
        if (certificate == null) throw new RuntimeException("certificate == null");
        if (privateKey == null) throw new RuntimeException("privateKey == null");
        if (serverUrlStr == null) throw new RuntimeException("serverUrlStr == null");

        final List<String> allowedCertificateHostnameAliases = new ArrayList<String>(allowedCertificateHostnameAliasesOrig);

        final URL serverUrl;
        try {
            serverUrl = new URL(serverUrlStr);
        } catch (MalformedURLException e) {
            System.err.print("ERROR: MalformedURLException url=\""+serverUrlStr+"\"");
            e.printStackTrace();
            return null;
        }

        try {
            String key_store_pass = "somepass"; //password for internal use. We don;t really need one as we'll never save this keystore.
            String privatekey_store_pass = "someotherpass"; //password for internal use. We don't really need one as we'll never save this keystore or share it with other objects

            KeyStore keyStore = KeyStore.getInstance("JKS", "SUN");
            keyStore.load( null , key_store_pass.toCharArray());

            // Create CertificateChain (just 1 certificate in this case)
            Certificate[] certs = new Certificate[1];
            certs[0] = certificate;

            // storing private key and certificate to keystore
            keyStore.setKeyEntry("authority", privateKey, privatekey_store_pass.toCharArray()/*password is used to encrypt the key inside the keystore*/, certs );



            /************ Init Trust Store *************/
            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 120000); //read timeout in ms (default: 0 = infinite)   (long timeout, as some calls take time to finish)
            params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); //connection timeout in ms (default: 0 = infinite)

            final DefaultHttpClient httpclient = new DefaultHttpClient(params);
            String algo = SSLSocketFactory.TLS;
            java.security.SecureRandom random = new SecureRandom();
            /**/
            org.apache.http.conn.ssl.X509HostnameVerifier hostnameVerifier;
//            if (acceptAllSelfSigned_INSECURE) {
//                System.out.println("SECURITY WARNING:     using AllowAllHostnameVerifier");
//                hostnameVerifier = new AllowAllHostnameVerifier();
//            }
//            else
                hostnameVerifier = new org.apache.http.conn.ssl.AbstractVerifier(){
                    private BrowserCompatHostnameVerifier base = new BrowserCompatHostnameVerifier();

                    /**
                     * @param host the hostname of the url to check.
                     * @param cns the CN's of the certificate
                     * @param subjectAlts the aliases fot the CN's that the certificate allows
                     * @throws javax.net.ssl.SSLException
                     */
                    @Override
                    public final void verify(java.lang.String host, String[] cns, String[] subjectAlts) throws javax.net.ssl.SSLException {
                        System.out.println("hostnameVerifier verify("+host+", "+cns+", "+subjectAlts+")");
                        List<String> modifiedSubjectAlts = new ArrayList<String>();
                        if (subjectAlts != null)
                            for (String subjectAlt : subjectAlts)
                                modifiedSubjectAlts.add(subjectAlt);
                        List<String> modifiedCns = new ArrayList<String>();
                        if (cns != null)
                            for (String cn : cns)
                                modifiedCns.add(cn);

                        //force add of host itself, this makes any certificate we have stored match
                        for (String allowedAlias : allowedCertificateHostnameAliases)
                            if (modifiedCns.contains(allowedAlias) || modifiedSubjectAlts.contains(allowedAlias)) {
                                modifiedSubjectAlts.add(serverUrl.getHost());
                                break;
                            }

//                        System.out.println("          allowedCertificateHostnameAliases="+allowedCertificateHostnameAliases);
//                        System.out.println("          modified names="+modifiedSubjectAlts);

                        String[] newSubjectAlts = new String[modifiedSubjectAlts.size()];
                        for (int i = 0; i < modifiedSubjectAlts.size(); i++)
                            newSubjectAlts[i] = modifiedSubjectAlts.get(i);
                        base.verify(host, cns, newSubjectAlts);
                    }
                };
            SSLSocketFactory socketFactory;
            if (handleUntrustedCallback != null) {
//                socketFactory = new SSLSocketFactory(TRUSTALL, new AllowAllHostnameVerifier()); //doesn't work as we need client authentication

                //didn't work for some reason. Probably it's missing client certificate (need to check apache source code of SSLSocketFactory to correctly write this)
//                socketFactory = new MyInsecureSSLAcceptAllSocketFactory(algo, keyStore, privatekey_store_pass/*password is used to encrypt the key inside the keystore*/, trustStore, random, hostnameVerifier);

                //Solved by connecting 2 times. Not best solution, but it works.
                SSLCertificateDownloader.SSLCertificateJFedInfo certInfo = SSLCertificateDownloader.getCertificateInfo(serverUrl);
                if (certInfo.isSelfSigned()) {
                    if (handleUntrustedCallback.trust(certInfo)) {
                        System.out.println("User trust certificate -> Adding server certificate to trust store, and connecting again.");
                        trustStore.setCertificateEntry("allTrustCert"+extraTrustCount, certInfo.getCert());
                        if (!certInfo.getSubjectMatchesHostname())
                            allowedCertificateHostnameAliases.add(certInfo.getSubject());
                    } else {
                        System.err.println("User does not trust certificate -> Not adding anything to trust store.");
                    }
                }

               socketFactory = new SSLSocketFactory(algo, keyStore, privatekey_store_pass/*password is used to encrypt the key inside the keystore*/, trustStore, random, hostnameVerifier);
            }
            else
                socketFactory = new SSLSocketFactory(algo, keyStore, privatekey_store_pass/*password is used to encrypt the key inside the keystore*/, trustStore, random, hostnameVerifier);
            Scheme sch1 = new Scheme("https", 443, socketFactory); //port is the default port for the given protocol
            httpclient.getConnectionManager().getSchemeRegistry().register(sch1);

            return httpclient;
        } catch (Exception e) {
            throw new GeniException("Error creating SSL connection to "+serverUrlStr, e);
        }
    }

    private static int extraTrustCount = 7000;

//    private static final TrustStrategy TRUSTALL = new TrustStrategy() {
//        @Override
//        public boolean isTrusted(X509Certificate[] chain, String authType) {
//            System.out.println("DEBUG:    TRUSTALL is trusting authType="+authType+" chain="+chain);
//            return true;
//        }
//    };
//
//    //Insecure, but can be useful for debugging
//    private static class InsecureTrustAllX509TrustManager implements X509TrustManager {
//        private final KeyStore trustStore;
//        private final X509TrustManager origX509TrustManager;
//        private InsecureTrustAllX509TrustManager(KeyStore trustStore, X509TrustManager origX509TrustManager) {
//            this.trustStore = trustStore;
//            this.origX509TrustManager = origX509TrustManager;
//            System.out.println("SECURITY WARNING:     constructing InsecureTrustAllX509TrustManager");
//
//        }
//        @Override
//        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            System.out.println("SECURITY WARNING:     InsecureTrustAllX509TrustManager is calling checkClientTrusted");
//            throw new UnsupportedOperationException();
//        }
//
//        static int count = 5000;
//        @Override
//        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            System.out.println("SECURITY WARNING:     InsecureTrustAllX509TrustManager is trusting server with authType="+authType+" chain="+chain);
//            try {
//                trustStore.setCertificateEntry("extraCert"+(count++), chain[0]);
//            } catch (KeyStoreException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//            origX509TrustManager.checkServerTrusted(chain, authType);
//        }
//
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            System.out.println("SECURITY WARNING:     InsecureTrustAllX509TrustManager is calling getAcceptedIssuers");
//            //                    System.err.println("WARNING: MyInsecureSSLAcceptAllSocketFactory.trustAll.getAcceptedIssuers() is called. (method not correctly implemented)");
//            //                    return null;
//            throw new UnsupportedOperationException();
//        }
//    }
//
//    //Insecure, but can be useful for debugging
//    private static class MyInsecureSSLAcceptAllSocketFactory extends SSLSocketFactory {
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//
//        public MyInsecureSSLAcceptAllSocketFactory(java.lang.String algorithm,
//                                                   java.security.KeyStore keystore,
//                                                   java.lang.String keystorePassword,
//                                                   java.security.KeyStore trustStore,
//                                                   java.security.SecureRandom random,
//                                                   org.apache.http.conn.ssl.X509HostnameVerifier hostnameVerifier) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
//            super(algorithm, keystore, keystorePassword, trustStore, random, hostnameVerifier);
//
//            System.out.println("SECURITY WARNING:     constructing MyInsecureSSLAcceptAllSocketFactory");
//
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            tmf.init(trustStore);
//            X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
//            TrustManager trustAll = new InsecureTrustAllX509TrustManager(trustStore, defaultTrustManager);
//
//            sslContext.init(null, new TrustManager[] { trustAll }, null);
//        }
//
//        @Override
//        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
//            System.out.println("DEBUG  MyInsecureSSLAcceptAllSocketFactory createSocket(Socket socket, String host, int port, boolean autoClose) called");
//            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
//        }
//
//        @Override
//        public Socket createSocket() throws IOException {
//            System.out.println("DEBUG  MyInsecureSSLAcceptAllSocketFactory createSocket() called");
//            return sslContext.getSocketFactory().createSocket();
//        }
//
//        public java.net.Socket createSocket(HttpParams params) throws java.io.IOException {
//            System.out.println("DEBUG  MyInsecureSSLAcceptAllSocketFactory createSocket(HttpParams params) called");
//            return sslContext.getSocketFactory().createSocket();
//        }
//
//
//        public java.net.Socket connectSocket(Socket socket,
//                                             InetSocketAddress remoteAddress,
//                                             InetSocketAddress localAddress,
//                                             HttpParams params) throws java.io.IOException,
//                java.net.UnknownHostException,
//                org.apache.http.conn.ConnectTimeoutException {
//            System.out.println("DEBUG  MyInsecureSSLAcceptAllSocketFactory connectSocket called");
//            return super.connectSocket(socket, remoteAddress, localAddress, params);
//        }
//
//        protected void prepareSocket(javax.net.ssl.SSLSocket socket) throws java.io.IOException {
//            throw new RuntimeException("Unsupported operation");
//        }
//        public java.net.Socket createLayeredSocket(Socket socket,
//                                                   String host,
//                                                   int port,
//                                                   HttpParams params) throws java.io.IOException, java.net.UnknownHostException {
//            throw new RuntimeException("Unsupported operation");
//        }
//
//    }
}
