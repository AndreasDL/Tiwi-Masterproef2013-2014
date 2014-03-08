package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;

import java.util.List;

/**
 * ManifestList
 *
 * A manifest has a type:
 *   - request (sent by user)
 *   - request echo (request as reported back by allocate)
 *   - manifest (provision reply or createsliver reply)
 *
 * Each manifest belongs to
 *   - a slice (may be unknown)
 *   - one or more slivers (may be unknown, may be some but no all slivers of the slice)
 */
public class RSpecList {
    private List<RSpecInfo> all;


    public List<RSpecInfo> getBySliceUrn(String sliceUrn) {
        //TODO
        return null;
    }

    public List<RSpecInfo> getBySliverUrn(String sliverUrn) {
        //TODO
        return null;
    }



    public void seeRequestRspec(SfaAuthority auth, String sliceUrn, List<String> sliverUrns, String rspec) {
        //TODO (also backlink)
    }

    public void seeEchoRequestRspec(SfaAuthority auth, String sliceUrn, List<String> sliverUrns, String rspec) {
        //TODO (also backlink)
    }

    public void seeManifestRspec(SfaAuthority auth, String sliceUrn, List<String> sliverUrns, String rspec) {
        //TODO (also backlink)
    }
}
