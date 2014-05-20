package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
   Example https://www.emulab.net/protogeni/authorities/salist.txt:

   urn:publicid:IDN+uml.emulab.net+authority+sa https://boss.uml.emulab.net/protogeni/xmlrpc/sa
   urn:publicid:IDN+emulab.larc.usp.br+authority+sa https://www.emulab.larc.usp.br:12369/protogeni/xmlrpc/sa
   urn:publicid:IDN+emulab.net+authority+sa https://www.emulab.net:12369/protogeni/xmlrpc/sa
....
   urn:publicid:IDN+wall3.test.ibbt.be+authority+sa https://www.wall3.test.ibbt.be:12369/protogeni/xmlrpc/sa
 */

/**
 * Get list of authorities from the Utah Clearinghouse. Including all self signed server certificates of the authorities.
 *
 * @see <a href="http://www.protogeni.net/wiki/SimpleAuthentication">ProtoGeni SimpleAuthentication page</a>
 * @see <a href="https://www.emulab.net/rootca.bundle">Emulab Clearinghouse root certificate bundle file</a>
 * @see <a href="https://www.emulab.net/genica.bundle">Emulab Clearinghouse Geni authorities self-signed certificate bundle file</a>
 * @see <a href="https://www.emulab.net/protogeni/authorities/salist.txt">Emulab Clearinghouse Geni authorities list</a>
 */
public class UtahClearingHouseAuthorityList {
    private UtahClearingHouseAuthorityList() {    }

    private static SfaAuthority emulabAuthority(String saUrn, String saUrl) throws GeniException {
        try {
            String url_am = saUrl.replace("/protogeni/xmlrpc/sa", "/protogeni/xmlrpc/am");
            String url_am2 = saUrl.replace("/protogeni/xmlrpc/sa", "/protogeni/xmlrpc/am/2.0");
            String url_am3 = saUrl.replace("/protogeni/xmlrpc/sa", "/protogeni/xmlrpc/am/3.0");

        //       note: urn:publicid:IDN+wall3.test.ibbt.be+authority+sa is used in https://www.emulab.net/protogeni/authorities/salist.txt
        // but urn is: urn:publicid:IDN+wall3.test.ibbt.be+authority+cm
            String urn = saUrn.replace("+authority+sa", "+authority+cm");
            String name = saUrn.replace("urn:publicid:IDN+", "").replace("+authority+sa", "");

            Map<ServerType, URL> urls = new HashMap<ServerType, URL>();
            urls.put(new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1), new URL(saUrl));
            urls.put(new ServerType(ServerType.GeniServerRole.AM, 1), new URL(url_am));
            urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL(url_am2));
            urls.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL(url_am3));

            SfaAuthority emulabauth = new SfaAuthority(urn, name, urls, null/*gid*/, "emulab");
            emulabauth.setSource(SfaAuthority.InfoSource.UTAH_CLEARINGHOUSE);

            return emulabauth;
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL malformed: "+saUrl, e);
        }
    }

    private static String downloadFile(String url) {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000); //read timeout in ms (default: 0 = infinite)
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); //connection timeout in ms (default: 0 = infinite)

        DefaultHttpClient httpClient = new DefaultHttpClient(params);

        HttpGet httpget = new HttpGet(url);

        try {
            HttpResponse response = httpClient.execute(httpget);

            if (response.getStatusLine() == null)
                throw new RuntimeException("Error getting url \""+url+"\" => "+response);
            if (response.getStatusLine().getStatusCode() != 200)
                throw new RuntimeException("Error getting url \""+url+"\" ("+response.getStatusLine().getStatusCode()+") => "+response.getStatusLine()+" => "+response);

            HttpEntity entity = response.getEntity();
            return IOUtils.streamToString(entity.getContent());
        } catch (javax.net.ssl.SSLPeerUnverifiedException ex) {
            throw new RuntimeException("The server certificate for '"+httpget.getURI()+"' could not be verified: it is not in our trust store: '"+ex.getMessage()+"'", ex);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("ClientProtocolException: "+e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("IOException: "+e.getMessage(), e);
        }
    }

//    private static List<GeniAuthority> retreivedAuthorities = null;
//    public static List<GeniAuthority> getAuthorities() {
//        if (retreivedAuthorities != null) return retreivedAuthorities;

    public static void load(AuthorityListModel authorityListModel) {
//        retreivedAuthorities = new ArrayList<GeniAuthority>();

        String saList = downloadFile("https://www.emulab.net/protogeni/authorities/salist.txt");

        for (String sa : TextUtil.getLines(saList)) {
            if (sa.length() < 3) continue;

            String [] parts = sa.trim().split(" ");

            if (parts.length != 2 || !parts[0].startsWith("urn:publicid:IDN+") || !parts[0].endsWith("+authority+sa")
                    || !parts[1].startsWith("https://") || !parts[1].endsWith("/protogeni/xmlrpc/sa")) {
                System.err.println("Skipping illegal line in https://www.emulab.net/protogeni/authorities/salist.txt: \"" + sa.trim() + "\"");
                continue;
            }
            
            assert parts.length == 2 : "Incorrect parts count: "+parts.length+" in \""+sa+"\"";
            assert parts[0].startsWith("urn:publicid:IDN+") && parts[0].endsWith("+authority+sa") : "Incorrect parts[0]: in \""+sa+"\"";
            assert parts[1].startsWith("https://") && parts[1].endsWith("/protogeni/xmlrpc/sa") : "Incorrect parts[1]: in \""+sa+"\"";

            try{
                SfaAuthority auth = emulabAuthority(parts[0], parts[1]);
                //auth.setPemSslTrustCert(cert);

                authorityListModel.mergeOrAdd(auth);
            } catch (GeniException e) {
                System.err.println("WARNING: Error in authority retrieved from Utah CleaingHouse (=> ignoring authority): line=\""+sa+"\" errmsg="+e.getMessage());
                //e.printStackTrace();
            }
//            retreivedAuthorities.add(auth);
        }

        authorityListModel.fireChange();
//        return retreivedAuthorities;
    }

    private static boolean retrieveCertificatesDone = false;
    public static void retrieveCertificates() {
        //do this only once.   TODO: get rid of this static mess, so the can be updated if needed. (note that removing next line will add instead of update!)
        if (retrieveCertificatesDone) return;
        retrieveCertificatesDone = true;

        String rootCertList = downloadFile("https://www.emulab.net/rootca.bundle");
        String geniCertList = downloadFile("https://www.emulab.net/genica.bundle");

        List<Certificate> rootCertificates = KeyUtil.parseAllPEMCertificates(rootCertList);
        List<Certificate> geniCertificates = KeyUtil.parseAllPEMCertificates(geniCertList);

        for (Certificate c : rootCertificates)
            GeniTrustStoreHelper.addTrustedCertificate(c);
//        System.out.println("Added "+rootCertificates.size()+" root certificates from emulab clearing house.");

        for (Certificate c : geniCertificates)
            GeniTrustStoreHelper.addTrustedCertificate(c);
//        System.out.println("Added "+geniCertificates.size()+" Geni certificates from emulab clearing house.");
    }

//    public static GeniAuthority getByName(String authorityName) {
//        for (GeniAuthority auth : getAuthorities()) {
//            if (auth.getName().equals(authorityName))
//                return auth;
//        }
//        return null;
//    }
}
