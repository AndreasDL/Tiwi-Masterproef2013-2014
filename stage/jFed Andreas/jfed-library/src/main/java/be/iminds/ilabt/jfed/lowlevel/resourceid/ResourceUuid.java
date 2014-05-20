package be.iminds.ilabt.jfed.lowlevel.resourceid;

/**
* UuidResource
*/
public class ResourceUuid implements ResourceId {
    public String value;
    public ResourceUuid(String value) { this.value = value; }
    public String getType() { return "uuid"; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return "UuidResource{" + value + "\"}";
    }
}
