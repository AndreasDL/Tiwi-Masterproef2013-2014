package be.iminds.ilabt.jfed.lowlevel.resourceid;

/**
* HrnResource
*/
public class ResourceHrn implements ResourceId {
    public String value;
    public ResourceHrn(String value) { this.value = value; }
    public String getType() { return "hrn"; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return "HrnResource{" + value + "\"}";
    }
}
