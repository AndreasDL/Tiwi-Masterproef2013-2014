package be.iminds.ilabt.jfed.lowlevel.stitching;

/**
* HopId
*/
class HopId {
    private final String linkName;
    private final String hopId;
    private final String linkId; //urn

    public HopId(String linkName, String hopId, String linkId) {
        assert linkName != null;
        assert hopId != null;
        assert linkId != null;
        this.linkName = linkName;
        this.hopId = hopId;
        this.linkId = linkId;
    }

    public String getLinkName() {
        return linkName;
    }

    public String getHopId() {
        return hopId;
    }

    public String getLinkId() {
        return linkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HopId hopId1 = (HopId) o;

        if (!hopId.equals(hopId1.hopId)) return false;
        if (!linkId.equals(hopId1.linkId)) return false;
        if (!linkName.equals(hopId1.linkName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = linkName.hashCode();
        result = 31 * result + hopId.hashCode();
        result = 31 * result + linkId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "HopId{" +
                "linkName='" + linkName + '\'' +
                ", hopId='" + hopId + '\'' +
                ", linkId='" + linkId + '\'' +
                '}';
    }
}
