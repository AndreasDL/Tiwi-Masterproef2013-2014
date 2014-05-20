package be.iminds.ilabt.jfed.lowlevel.resourceid;

/**
 * ResourceIdParser
 *
 */
public class ResourceIdParser {
    /**
     * parses a resource from String, trying to determine it's type.
     *
     * Note that it might not always be possible to do this correctly:
     *   - some hrn's might be confused with Uuid's. (only a simple test is done)
     * */
    public static ResourceId parse(String s) {
        if (s.startsWith("urn:"))
            return new ResourceUrn(s);
        //uuid is hexadecimal plus -
        if (s.matches("0123456789abcdef-"))
            return new ResourceUuid(s);
        return new ResourceHrn(s);

        //throw new RuntimeException("Unknown resource ID type for: '"+s+"'");
    }
}
