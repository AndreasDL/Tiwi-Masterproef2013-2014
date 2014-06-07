package be.iminds.ilabt.jfed.lowlevel.resourceid;

/**
* UrnResource
 *
 * example urn's:
 *   user urn:  urn:publicid:IDN+<AUTH>+user+<userName>
 *   slice urn: urn:publicid:IDN+<AUTH>+slice+<SLICENAME>
*/
public class ResourceUrn implements ResourceId {
    public String value;
    public ResourceUrn(String value) { this.value = value; }
    public String getType() { return "urn"; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return "UrnResource{" + value + "\"}";
    }
}
