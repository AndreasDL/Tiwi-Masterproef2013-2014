package be.iminds.ilabt.jfed.lowlevel;

/**
 * ServerType represents the type of server we are connecting to.
 */
public class ServerType {
    public enum GeniServerRole { PlanetLabSliceRegistry, PROTOGENI_SA, AM, PROTOGENI_CH, GENI_CH, GENI_CH_SA, GENI_CH_MA, SCS; }

    private GeniServerRole role;
    private String version;

    public ServerType(GeniServerRole role, int version) {
        this.role = role;
        this.version = version+"";
    }
    public ServerType(GeniServerRole role, String version) {
        this.role = role;
        this.version = version;
    }

    public GeniServerRole getRole() {
        return role;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerType that = (ServerType) o;

        if (role != that.role) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServerType{" +
                "\"" + role +
                "\" \"" + version + '\"' +
                '}';
    }

////    /* helpers */
//    public static void addSAToUrlMap(Map<ServerType, URL> map, int version, URL url) {
//        map.put(new ServerType(GeniServerRole.SA, version), url);
//    }
//    public static void addSAToUrlMap(Map<ServerType, URL> map, int version, String url) throws MalformedURLException {
//        map.put(new ServerType(GeniServerRole.SA, version), new URL(url));
//    }
//    public static void addAMToUrlMap(Map<ServerType, URL> map, int version, URL url) {
//        map.put(new ServerType(GeniServerRole.AM, version), url);
//    }
//    public static void addAMToUrlMap(Map<ServerType, URL> map, int version, String url) throws MalformedURLException {
//        map.put(new ServerType(GeniServerRole.AM, version), new URL(url));
//    }
}
