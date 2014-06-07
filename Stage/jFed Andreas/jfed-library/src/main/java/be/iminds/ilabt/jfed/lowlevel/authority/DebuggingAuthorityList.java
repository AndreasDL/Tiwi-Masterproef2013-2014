package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.ApiMethodParameterType;
import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.Gid;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.util.KeyUtil;
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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Returns the Debugging SfaAuthority or adds it to a list.
 *
 * This authority is completely fake and only useful for debugging.
 */
public class DebuggingAuthorityList {
    private DebuggingAuthorityList() {    }

    private static SfaAuthority debuggingAuth = null;

    public static final String getDebuggingAuthUrn() {
        return "urn:publicid:IDN+debug.jfed.iminds.be+authority+cm";
    }

    public static boolean isDebuggingAuth(SfaAuthority auth) {
        return auth == getDebuggingAuth();
    }

    public static final SfaAuthority getDebuggingAuth() {
        if (debuggingAuth == null) {
            Map< ServerType, URL> urls = new HashMap< ServerType, URL>();
            try {
                //these URL are not supposed to be get used
                //  the debug system should be in place automatically and handle
                //they also do not exist at all on the server
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 2), new URL("https://debug.jfed.iminds.be/example/AM/2"));
                urls.put(new ServerType(ServerType.GeniServerRole.AM, 3), new URL("https://debug.jfed.iminds.be/example/AM/3"));
                urls.put(new ServerType(ServerType.GeniServerRole.GENI_CH, 0), new URL("https://debug.jfed.iminds.be/example/GENI_CH/0"));
                urls.put(new ServerType(ServerType.GeniServerRole.GENI_CH_MA, 0), new URL("https://debug.jfed.iminds.be/example/GENI_CH_MA/0"));
                urls.put(new ServerType(ServerType.GeniServerRole.GENI_CH_SA, 0), new URL("https://debug.jfed.iminds.be/example/GENI_CH_SA/0"));
                urls.put(new ServerType(ServerType.GeniServerRole.PlanetLabSliceRegistry, 1), new URL("https://debug.jfed.iminds.be/example/PL/SR/1"));
                urls.put(new ServerType(ServerType.GeniServerRole.PROTOGENI_CH, 1), new URL("https://debug.jfed.iminds.be/example/PG/CH/1"));
                urls.put(new ServerType(ServerType.GeniServerRole.PROTOGENI_SA, 1), new URL("https://debug.jfed.iminds.be/example/PG/SA/1"));
                urls.put(new ServerType(ServerType.GeniServerRole.SCS, 1), new URL("https://debug.jfed.iminds.be/example/SCS/1"));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            try {
                debuggingAuth = new SfaAuthority(getDebuggingAuthUrn(), "jFed Debug Authority", urls, null, "debugging");
            } catch (GeniException e) {
                throw new RuntimeException(e);
            }
        }
        return debuggingAuth;
    }

    /* returns whether successful or not */
    public static boolean load(AuthorityListModel authorityListModel) {
        SfaAuthority sfaAuthority = getDebuggingAuth();
        authorityListModel.addAuthority(sfaAuthority);
        authorityListModel.fireChange();
        return true;
    }
}
