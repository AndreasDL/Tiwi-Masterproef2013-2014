package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.Rspec;

import java.util.List;

/**
 * RSpec
 */
public class RSpecInfo {
    public enum RspecType { REQUEST, REQUEST_ECHO, MANIFEST, ADVERTISEMENT };

    private String stringContent;
    private Rspec content;
    private RspecType type;
    private Slice slice;
    private List<Sliver> slivers;

    private AuthorityInfo authority;

    public RSpecInfo(String stringContent, RspecType type, Slice slice, List<Sliver> slivers, AuthorityInfo authority) {
        this.stringContent = stringContent;
        this.type = type;
        this.slice = slice;
        this.slivers = slivers;
        this.authority = authority;
        this.content = null;
    }
    public RSpecInfo(String stringContent, RspecType type, Slice slice, List<Sliver> slivers, AuthorityInfo authority, Rspec content) {
        this.stringContent = stringContent;
        this.type = type;
        this.slice = slice;
        this.slivers = slivers;
        this.authority = authority;
        this.content = content;
    }

    public String getStringContent() {
        return stringContent;
    }
    public Rspec getRSpec() {
        //buffer RSpec parsing?

        if (content == null) {
            switch (type) {
                case REQUEST_ECHO:
                case REQUEST: { content = Rspec.fromGeni3RequestRspecXML(stringContent); break; }
                case MANIFEST: { content = Rspec.fromGeni3ManifestRspecXML(stringContent); break; }
                case ADVERTISEMENT: { content = Rspec.fromGeni3AdvertisementRspecXML(stringContent); break; }
                default:
                    throw new RuntimeException("Unsupported RSpec type: "+type);
            }
        }

        return content;
    }

    public RspecType getType() {
        return type;
    }

    public Slice getSlice() {
        return slice;
    }

    public List<Sliver> getSlivers() {
        return slivers;
    }

    public AuthorityInfo getAuthority() {
        return authority;
    }
}
