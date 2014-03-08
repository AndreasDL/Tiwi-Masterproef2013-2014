package be.iminds.ilabt.jfed.lowlevel.authority;

import be.iminds.ilabt.jfed.lowlevel.GeniException;
import be.iminds.ilabt.jfed.lowlevel.Gid;
import be.iminds.ilabt.jfed.lowlevel.ServerType;
import be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities;
import be.iminds.ilabt.jfed.util.GeniUrn;
import be.iminds.ilabt.jfed.util.KeyUtil;

import javax.xml.bind.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * SfaAuthority is a mutable class.
 * The URN never changes, and is the only thing used for testing equality: we always know at least the URN, but all
 * other info may be unknown initially and can be updated later on.
 *
 * A lot of getter's will always return something non null, even if there is nothing known yet. They derive info
 * from other sources, like the URN to do so.
 *
 *
 * This class represents an "authority", but that is a vague term here.
 * It contains info about one "site", which typically has it's own aggregate manager,
 *    and often also it's own identity/slice authority/registry,
 *
 * This class keeps basic info such as URL's, urn, etc...
 */
public class SfaAuthority {
    public static boolean debug = true; //ugly hack

    private String urn;
    private String urnPart; //name if part of urn    ex: wall3.test.ibbt.be

    private String hrn;

    private Map<ServerType, URL> urls;

    private Gid gid;

    /** Example types: emulab, planetlab */
    private String type;

    /** copy constructor */
    public SfaAuthority(SfaAuthority otherAuthority) {
        this.urn = otherAuthority.urn;
        this.urnPart = otherAuthority.urnPart;
        this.hrn = otherAuthority.hrn;
        if (otherAuthority.gid != null)
            this.gid = new Gid(otherAuthority.gid);
        else
            this.gid = null;
        this.urls = new HashMap<ServerType, URL>(otherAuthority.urls);
        this.source = otherAuthority.source;
        this.reconnectEachTime = otherAuthority.reconnectEachTime;
        this.pemSslTrustCert = otherAuthority.pemSslTrustCert;
        this.allowedCertificateHostnameAliases = new ArrayList<String>(otherAuthority.allowedCertificateHostnameAliases);
    }

    public enum InfoSource { UTAH_CLEARINGHOUSE, BUILTIN, USER_PROVIDED };
    private InfoSource source; //the source this authority info comes from
    private boolean wasStored = false; //local storage is not a source, as it needs to store source info...

    /**
     *  hack: require reconnecting after each XmlRpc request
     *  This is used to fix a bug with PlanetLab Europe connections:
     *     HttpClient disconnects after the first request (unknown reason, possibly just no KeepAlive, possibly bug in
     *     HttpClient), and HttpClient tries to reconnect on the second MmlRpc request, BUT is does not use the key and
     *     trust stores anymore at that point! That makes that reconnection fail. The solution is to construct a new
     *     HttpClient, so a new GeniConnection each time.
     *  This is used in GeniConnectionPool. IF enabled, the GeniConnection is not stored there, so not reused.
     */
    private boolean reconnectEachTime;

    private String pemSslTrustCert;

    /**
     * Sometimes, the SSL cerificate for a host does not have the correct name for that host. For example, if the url is:
     * https://sfa.planet-lab.eu:12347, while the certificate is for "ple". To fix these issues, add allowed aliases for
     * the certificate here. In the example, you would add "ple" to this list.
     * */
    private List<String> allowedCertificateHostnameAliases;

    /**
     * @param urn the URN (may never be null)
     * @param hrn may be null
     * @param urls may be null
     * @param gid may be null
     * @param type may be null
     */
    public SfaAuthority(String urn, String hrn, Map<ServerType, URL> urls, Gid gid, String type) throws GeniException {
        assert urn != null : "Cannot create GeniAuthority if URN is not known";

        this.urn = urn;
        this.urnPart = urnPartFromUrn(urn);
//        System.out.println("GeniAuthority urn=" + urn + " urnPart=" + urnPart);

        this.hrn = hrn;

        this.urls = new HashMap<ServerType, URL>();
        if (urls != null)
            this.urls.putAll(urls);

        this.gid = gid;

        this.type = type;

        this.reconnectEachTime = false;
        this.allowedCertificateHostnameAliases = new ArrayList<String>();

        //these defaults are overwritten by the classes that implement another source
        source = InfoSource.USER_PROVIDED;
        this.wasStored = false;
    }

    public SfaAuthority(String urn) throws GeniException {
        this.urn = urn;
        this.urnPart = urnPartFromUrn(urn);

        this.hrn = null;
        this.urls = new HashMap<ServerType, URL>();
        this.gid = null;

        this.reconnectEachTime = false;
        this.allowedCertificateHostnameAliases = new ArrayList<String>();
    }

    /**
     * Get urnPart from urn, and check if URN is correct
     */
    public static String urnPartFromUrn(String urn) throws GeniException {
        assert urn != null;

        GeniUrn geniUrn = GeniUrn.parse(urn);
        if (geniUrn == null)
            throw new GeniException("Illegal Geni Authority URN: \""+urn+"\"");

        String urnPart = geniUrn.getTopLevelAuthority();
        if (urnPart == null)
            throw new GeniException("Illegal Geni Authority URN: urnPart=null in \""+urn+"\"");
        if (urnPart.length() < 2)
            throw new GeniException("Illegal Geni Authority URN: urnPart=\""+urnPart+"\" in \""+urn+"\"");
        if (urnPart.equals(urn))
            throw new GeniException("Error extracting urnPart from urn=\""+urn+"\"");

        return new String(urnPart);
    }

    /**
     * Synonym for {@link SfaAuthority#getHrn()}
     */
    public String getName() {
        if (hrn == null) return urnPart;
        return hrn;
    }
    public String getHrn() {
        return getName();
    }

    public URL getUrl(ServerType serverType) {
        return urls.get(serverType);
    }
    public URL getUrl(ServerType.GeniServerRole role, int version) {
        return urls.get(new ServerType(role, version));
    }
    public Map<ServerType, URL> getUrls() {
        return Collections.unmodifiableMap(urls);
    }

    public boolean isReconnectEachTime() {
        return reconnectEachTime;
    }

    public String getType() {
        return type;
    }

    /**
     * Sometimes, the SSL cerificate for a host does not have the correct name for that host. For example, if the url is:
     * https://sfa.planet-lab.eu:12347, while the certificate is for "ple". To fix these issues, add allowed aliases for
     * the certificate here. In the example, you would add "ple" to this list.
     *
     * This list is used in {@link be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory}
     * where it is used to create a X509HostnameVerifier that uses these extra aliases.
     * */
    public List<String> getAllowedCertificateHostnameAliases() {
        if (allowedCertificateHostnameAliases == null)
            return new LinkedList<String>();
        return Collections.unmodifiableList(allowedCertificateHostnameAliases);
    }

    public void setUrl(ServerType serverType, URL newUrl) {
        urls.put(serverType, newUrl);
    }
    public void replaceUrls(Map<ServerType, URL> newMap) {
        urls.clear();
        urls.putAll(newMap);
    }
    public void setHrn(String hrn) {
        this.hrn = hrn;
    }
    public void setName(String name) {
        this.hrn = name;
    }
    public void setGid(Gid gid) {
        this.gid = gid;
    }
    public void setReconnectEachTime(boolean reconnectEachTime) {
        this.reconnectEachTime = reconnectEachTime;
    }
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Sometimes, the SSL cerificate for a host does not have the correct name for that host. For example, if the url is:
     * https://sfa.planet-lab.eu:12347, while the certificate is for "ple". To fix these issues, add allowed aliases for
     * the certificate here. In the example, you would add "ple" to this list.
     * */
    public void addAllowedCertificateHostnameAlias(String alias) {
        allowedCertificateHostnameAliases.add(alias);
    }
    public void setAllowedCertificateHostnameAlias(List<String> aliases) {
        allowedCertificateHostnameAliases.clear();
        allowedCertificateHostnameAliases.addAll(aliases);
    }

    public void updateAll(String hrn, Map<ServerType, URL> urls, Gid gid, String type) {
        updateHrn(hrn);
        for (Map.Entry<ServerType, URL> e : urls.entrySet())
            updateUrl(e.getKey(), e.getValue());
        updateGid(gid);
        updateType(type);
    }

    public void updateUrl(ServerType serverType, URL newUrl) {
        URL prevUrl = getUrl(serverType);
        if (prevUrl == null) return; //no update if not set
        if (prevUrl != null && !prevUrl.equals(newUrl) && debug)
            System.err.println("Warning updateUrl("+serverType+", \"" + newUrl + "\") while prevUrl=\"" + prevUrl + "\"");
        urls.put(serverType, newUrl);
    }
    public void updateHrn(String hrn) {
        if (hrn == null) return; //no update if not set
        if (this.hrn != null && !hrn.equals(this.hrn) && debug)
            System.err.println("Warning updateHrn(\""+hrn+"\") while hrn=\""+this.hrn+"\"");
        this.hrn = hrn;
    }

    public void updateUrn(String urn) throws GeniException {
        if (urn == null) return; //no update if not set
        if (this.urn != null && !urn.equals(this.urn) && debug)
            System.err.println("Warning updateUrn(\""+urn+"\") while urn=\""+this.urn+"\"");

        if (this.urn == null || !urn.equals(this.urn)) {
            this.urn = urn;
            this.urnPart = urnPartFromUrn(urn);
        }
    }
    /**
     * Synonym for {@link SfaAuthority#updateHrn(String)}
     */
    public void updateName(String name) {
        updateHrn(name);
    }
    public void updateGid(Gid gid) {
        if (gid == null) return; //no update if not set
        if (this.gid != null && !gid.equals(this.gid) && debug)
            System.err.println("Warning updateGid(\""+gid+"\") while gid=\""+this.gid+"\"");
        this.gid = gid;
    }
    public void updateType(String type) {
        if (type == null) return; //no update if not set
        if (this.type != null && !type.equals(this.type) && debug)
            System.err.println("Warning updateType(\""+type+"\") while type=\""+this.type+"\"");
        this.type = type;
    }



    /** the authority as named in an URN. This is useful to construct slice and user URNs.*/
    public String getNameForUrn() {
        return urnPart;
    }

    public String getUrn() {
        return urn;
    }


    public Gid getGid() {
        return gid;
    }

    public void setPemSslTrustCert(String pemSslTrustCert) {
        this.pemSslTrustCert = pemSslTrustCert;
    }

    public void setPemSslTrustCert(X509Certificate sslTrustCert) {
        this.pemSslTrustCert = KeyUtil.x509certificateToPem(sslTrustCert);
    }

    public String getPemSslTrustCert() {
        return pemSslTrustCert;
    }





    public void setSource(InfoSource source) {
        this.source = source;
    }

    public void setWasStored(boolean wasStored) {
        this.wasStored = wasStored;
    }

    public InfoSource getSource() {
        return source;
    }

    public boolean isWasStored() {
        return wasStored;
    }



    /*
    * Note about hte XML binding:
    *
    * XML binding generated with JAXB from authority.xsd
    * command line was:
    *   cd <main project dir>
    *   cd jfed-library/src/main/java/
    *   xjc -p be.iminds.ilabt.jfed.lowlevel.authority.binding <main project dir>/jfed-library/src/main/resources/be/iminds/ilabt/jfed/lowlevel/authority/authority.xsd
    * */


    public String toXmlString() {
        try {
            StringWriter writer = new StringWriter();
            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority xmlAuth = toXml();
            JAXBContext context = JAXBContext.newInstance(Authorities.Authority.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(xmlAuth, writer);

            writer.close();

            String xml = writer.toString();
            return xml;
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority toXml() {
        be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority res =
                new be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority();

        res.setUrn(urn);
        if (gid != null) res.setGid(gid.toString());
        if (hrn != null) res.setHrn(hrn);
        if (pemSslTrustCert != null) res.setPemSslTrustCert(pemSslTrustCert);
        res.setReconnectEachTime(reconnectEachTime);
        if (type != null) res.setType(type);

        if (source != null) {
            String sourceString = null;
            switch (source) {
                case UTAH_CLEARINGHOUSE: { sourceString = "Clearinghouse"; break; }
                case BUILTIN: { sourceString = "Builtin"; break; }
                case USER_PROVIDED: { sourceString = "User"; break; }
            }
            if (sourceString != null)
                res.setSource(sourceString);
        }

        if (allowedCertificateHostnameAliases != null && !allowedCertificateHostnameAliases.isEmpty()) {
            Authorities.Authority.AllowedCertificateHostnameAliases
                    xmlAllowedCertificateHostnameAliases =
                    new Authorities.Authority.AllowedCertificateHostnameAliases();
            for (String alias : allowedCertificateHostnameAliases)
                xmlAllowedCertificateHostnameAliases.getAlias().add(alias);
            res.setAllowedCertificateHostnameAliases(xmlAllowedCertificateHostnameAliases);
        }

        Authorities.Authority.Urls xmlUrls =
                new Authorities.Authority.Urls();
        for (Map.Entry<ServerType, URL> e : urls.entrySet()) {
            Authorities.Authority.Urls.Serverurl xmlServerUrl = new Authorities.Authority.Urls.Serverurl();
            Authorities.Authority.Urls.Serverurl.Servertype xmlServerType = new Authorities.Authority.Urls.Serverurl.Servertype();
            String roleString = e.getKey().getRole().toString();
            if (e.getKey().getRole().equals(ServerType.GeniServerRole.PROTOGENI_SA)) roleString = "Slice Authority";
            if (e.getKey().getRole().equals(ServerType.GeniServerRole.AM)) roleString = "Aggregate Manager";
            if (e.getKey().getRole().equals(ServerType.GeniServerRole.PlanetLabSliceRegistry)) roleString = "PlanetLab registry";
            xmlServerType.setRole(roleString);
            xmlServerType.setVersion(e.getKey().getVersion());
            xmlServerUrl.setServertype(xmlServerType);
            xmlServerUrl.setUrl(e.getValue().toExternalForm());
            xmlUrls.getServerurl().add(xmlServerUrl);
        }
        res.setUrls(xmlUrls);

        return res;
        //to string:
//        StringWriter sw = new StringWriter();
//        try {
//            JAXBContext context = JAXBContext.newInstance(be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority.class);
//            Marshaller m = context.createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            m.marshal(res, sw);
//            return sw.getBuffer().toString();
//        } catch (JAXBException e) {
//            //TODO: this should be handled better
//            //just dump the error to the output
//            e.printStackTrace();
//            return "Error converting to SfaAuthority to XML: "+ e;
//        }
    }
    public static SfaAuthority fromXml(String xml) throws GeniException {
        try {
            Class docClass = be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority.class;
            String packageName = docClass.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            JAXBElement<be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority> doc =
                    (JAXBElement<be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority>) u.unmarshal( new StringReader(xml));
            be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority c = doc.getValue();

            return fromXml(c);
        } catch (GeniException ex) {
            throw ex;
        } catch (Exception e) {
            System.err.println("WARNING: Error reading Authority XML: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public static SfaAuthority fromXml(be.iminds.ilabt.jfed.lowlevel.authority.binding.Authorities.Authority xmlAuthority) throws GeniException {
        try {
            SfaAuthority res = new SfaAuthority(xmlAuthority.getUrn());
            res.wasStored = true;

            if (xmlAuthority.getGid() != null)
                res.setGid(new Gid(xmlAuthority.getGid()));
            res.setHrn(xmlAuthority.getHrn());
            res.setPemSslTrustCert(xmlAuthority.getPemSslTrustCert());
            res.setReconnectEachTime(xmlAuthority.isReconnectEachTime());
            res.setType(xmlAuthority.getType());

            if (xmlAuthority.getSource() != null) {
                InfoSource source = null;
                if (xmlAuthority.getSource().equals("Clearinghouse")) source = InfoSource.UTAH_CLEARINGHOUSE;
                if (xmlAuthority.getSource().equals("Builtin")) source = InfoSource.BUILTIN;
                if (xmlAuthority.getSource().equals("User")) source = InfoSource.USER_PROVIDED;
                res.setSource(source);
            }

            if (xmlAuthority.getAllowedCertificateHostnameAliases() != null)
                res.setAllowedCertificateHostnameAlias(xmlAuthority.getAllowedCertificateHostnameAliases().getAlias());

            for (Authorities.Authority.Urls.Serverurl xmlUrl: xmlAuthority.getUrls().getServerurl()) {
                ServerType.GeniServerRole role = null;
                if (xmlUrl.getServertype().getRole().equals("Slice Authority")) role = ServerType.GeniServerRole.PROTOGENI_SA;
                if (xmlUrl.getServertype().getRole().equals("Aggregate Manager")) role = ServerType.GeniServerRole.AM;
                if (xmlUrl.getServertype().getRole().equals("PlanetLab registry")) role = ServerType.GeniServerRole.PlanetLabSliceRegistry;
                assert role != null : "invalid role in XML: "+xmlUrl.getServertype().getRole();
                ServerType serverType = new ServerType(role, xmlUrl.getServertype().getVersion());
                res.setUrl(serverType, new URL(xmlUrl.getUrl()));
            }

            return res;
        } catch (GeniException ex) {
            throw ex;
        } catch (Exception e) {
            System.err.println("WARNING: Error reading authority xml: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SfaAuthority that = (SfaAuthority) o;

        if (!urn.equals(that.urn)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return urn.hashCode();
    }

    @Override
    public String toString() {
        return "SfaAuthority{" +
                "urn='" + urn + '\'' +
                '}';
    }
}
