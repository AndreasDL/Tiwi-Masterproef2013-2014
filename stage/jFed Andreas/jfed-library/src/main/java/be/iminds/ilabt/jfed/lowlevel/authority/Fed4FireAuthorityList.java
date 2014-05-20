package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.util.GeniTrustStoreHelper;
import be.iminds.ilabt.jfed.util.IOUtils;
import be.iminds.ilabt.jfed.util.KeyUtil;
import be.iminds.ilabt.jfed.util.TextUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/*
   Example https://www.emulab.net/protogeni/authorities/salist.txt:

   urn:publicid:IDN+uml.emulab.net+authority+sa https://boss.uml.emulab.net/protogeni/xmlrpc/sa
   urn:publicid:IDN+emulab.larc.usp.br+authority+sa https://www.emulab.larc.usp.br:12369/protogeni/xmlrpc/sa
   urn:publicid:IDN+emulab.net+authority+sa https://www.emulab.net:12369/protogeni/xmlrpc/sa
....
   urn:publicid:IDN+wall3.test.ibbt.be+authority+sa https://www.wall3.test.ibbt.be:12369/protogeni/xmlrpc/sa
 */

/**
 * Read jFed compatible XML representation of authorities
 *
 * URL:  https://flsmonitor.fed4fire.eu/testbeds.xml
 */
public class Fed4FireAuthorityList {
    private static final String FED4FIRE_TESTBED_DIRECTORY_URL = "https://flsmonitor.fed4fire.eu/testbeds.xml";

    private static final String FED4FIRE_TESTBED_DIRECTORY_SELFSIGNED_CERTIFICATE = "-----BEGIN CERTIFICATE-----\n" +
            "MIICwjCCAaqgAwIBAgIJAOE0tjy49mo/MA0GCSqGSIb3DQEBBQUAMBkxFzAVBgNV\n" +
            "BAMTDmFydGVtaXM2NS50ZXN0MB4XDTEzMDUzMTEzNDU1NFoXDTIzMDUyOTEzNDU1\n" +
            "NFowGTEXMBUGA1UEAxMOYXJ0ZW1pczY1LnRlc3QwggEiMA0GCSqGSIb3DQEBAQUA\n" +
            "A4IBDwAwggEKAoIBAQCqnqUQyHM6cFSe0pngvKMWRrHyJg9rpz1yH7bPeUvyQ+QR\n" +
            "67XHw0eO8tBRzdz0k2JiCdTHBL/zayD9TWdg03iFZ6ctRmSu/zqS7anEpGeFfRoW\n" +
            "Ofrs8m+cwG4VR2p9IUOXEVFAhdptr0BI4zWxrmuj7QK2Xav+2sOdZ3XrZSkT462O\n" +
            "FhunuITWS7SBjrH09cPqrjSDHA4pmdSTziRSS+DR+Nr1zW7UVE0Iz7QWk216kugy\n" +
            "x+o4uVWMrOwKM6GK+EiBsV3ov0lfA56JRsCk93l2cuzbelsr3vRbe3h5e0IPHKyv\n" +
            "LQlBMbzOZiuNPCzxPNk9W7ehSB14GfUVsjwUuSibAgMBAAGjDTALMAkGA1UdEwQC\n" +
            "MAAwDQYJKoZIhvcNAQEFBQADggEBAIx9t64rhBMcpGuB8oQqvIOK4Zka3x5phTXn\n" +
            "FBhgQdSYWU1BFGGZ1u69aD9QMgT3+gNNpvybEpsHwPBxjXhIkQgz7Zrm0GvGLyOr\n" +
            "7/xDgMbnvDae9/2VycfIXzSVt3KJIlC0gHJ3uyDXHgpYkK62XnDZa9kszJPQGGL4\n" +
            "4QmHniH7ZR23lnWCpCijerueg+bjoZuMLW8kJikstAeJ6Wtqc6PeHp1FU7CnFBkM\n" +
            "sWOwDjISeCIK0YRGmXH7VHgPueWuiQq9XMvO2r321CwRnYTtja/zAbB6hAGrfjB0\n" +
            "mQl6oeKERad87dSPo07CJKep9/IbO8Db9BxXaNEjz/jN+vpIVJI=\n" +
            "-----END CERTIFICATE-----";

    private static final String FED4FIRE_TESTBED_DIRECTORY_SELFSIGNED_CERTIFICATE_ALLOWED_ALIAS = "artemis65.test";

    private Fed4FireAuthorityList() {    }

    /* returns whether successful or not */
    public static boolean load(AuthorityListModel authorityListModel) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000); //read timeout in ms (default: 0 = infinite)
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); //connection timeout in ms (default: 0 = infinite)

        DefaultHttpClient httpClient = new DefaultHttpClient(params);
        X509Certificate cert = KeyUtil.pemToX509Certificate(FED4FIRE_TESTBED_DIRECTORY_SELFSIGNED_CERTIFICATE);
        KeyStore trustStore = null;
        try {
            final URL serverUrl = new URL(FED4FIRE_TESTBED_DIRECTORY_URL);

            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load( null , "any_pass".toCharArray());
            trustStore.setCertificateEntry("singleCert", cert);

            String algo = SSLSocketFactory.TLS;
            java.security.SecureRandom random = new SecureRandom();
            org.apache.http.conn.ssl.X509HostnameVerifier hostnameVerifier = new org.apache.http.conn.ssl.AbstractVerifier(){
                private BrowserCompatHostnameVerifier base = new BrowserCompatHostnameVerifier();

                /**
                 * @param host the hostname of the url to check.
                 * @param cns the CN's of the certificate
                 * @param subjectAlts the aliases fot the CN's that the certificate allows
                 * @throws javax.net.ssl.SSLException
                 */
                public final void verify(java.lang.String host, String[] cns, String[] subjectAlts) throws javax.net.ssl.SSLException {
                    List<String> modifiedSubjectAlts = new ArrayList<String>();
                    if (subjectAlts != null)
                        for (String subjectAlt : subjectAlts)
                            modifiedSubjectAlts.add(subjectAlt);
                    List<String> modifiedCns = new ArrayList<String>();
                    if (cns != null)
                        for (String cn : cns)
                            modifiedCns.add(cn);

                    //force add of host itself, this makes any certificate we have stored match
                    if (modifiedCns.contains(FED4FIRE_TESTBED_DIRECTORY_SELFSIGNED_CERTIFICATE_ALLOWED_ALIAS) || modifiedSubjectAlts.contains(FED4FIRE_TESTBED_DIRECTORY_SELFSIGNED_CERTIFICATE_ALLOWED_ALIAS)) {
                        modifiedSubjectAlts.add(serverUrl.getHost());
                    }
                    String[] newSubjectAlts = new String[modifiedSubjectAlts.size()];
                    for (int i = 0; i < modifiedSubjectAlts.size(); i++)
                        newSubjectAlts[i] = modifiedSubjectAlts.get(i);
                    base.verify(host, cns, newSubjectAlts);
                }
            };
            SSLSocketFactory socketFactory = new SSLSocketFactory(algo, null, null, trustStore, random, hostnameVerifier);
            Scheme sch1 = new Scheme("https", 443, socketFactory); //port is the default port for the given protocol
            httpClient.getConnectionManager().getSchemeRegistry().register(sch1);
        } catch (Exception e) {
            trustStore = null;
            throw new RuntimeException("Could not create httpClient for Fed4FireAuthorityList: "+e, e);
//            return false;
        }


        HttpGet httpget = new HttpGet(FED4FIRE_TESTBED_DIRECTORY_URL);
System.err.println("Fed4fire testbed directory url: "+FED4FIRE_TESTBED_DIRECTORY_URL);
        try {
            HttpResponse response = httpClient.execute(httpget);

            if (response.getStatusLine() == null)
                throw new RuntimeException("Error getting url \""+FED4FIRE_TESTBED_DIRECTORY_URL+"\" => "+response);
            if (response.getStatusLine().getStatusCode() != 200)
                throw new RuntimeException("Error getting url \""+FED4FIRE_TESTBED_DIRECTORY_URL+"\" ("+response.getStatusLine().getStatusCode()+") => "+response.getStatusLine()+" => "+response);

            HttpEntity entity = response.getEntity();
            StoredAuthorityList.load(entity.getContent(), authorityListModel);
        } catch (javax.net.ssl.SSLPeerUnverifiedException ex) {
            System.err.println("Fed4FireAuthorityList ERROR: SSLPeerUnverifiedException, The server certificate for '"+httpget.getURI()+"' could not be verified: it is not in our trust store: '"+ex.getMessage()+"'");
//            throw new RuntimeException("The server certificate for '"+httpget.getURI()+"' could not be verified: it is not in our trust store: '"+ex.getMessage()+"'", ex);
            return false;
        } catch (ClientProtocolException e) {
            System.err.println("Fed4FireAuthorityList ClientProtocolException: "+e.getMessage());
//            throw new RuntimeException("ClientProtocolException: "+e.getMessage(), e);
            return false;
        } catch (IOException e) {
            System.err.println("Fed4FireAuthorityList IOException: "+e.getMessage());
//            throw new RuntimeException("IOException: "+e.getMessage(), e);
            return false;
        }

        authorityListModel.fireChange();
        return true;
    }
}
