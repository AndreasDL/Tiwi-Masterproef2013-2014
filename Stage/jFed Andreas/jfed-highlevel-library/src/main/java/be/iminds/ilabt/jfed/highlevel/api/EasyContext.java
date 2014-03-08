package be.iminds.ilabt.jfed.highlevel.api;

import be.iminds.ilabt.jfed.highlevel.model.Slice;
import be.iminds.ilabt.jfed.highlevel.model.Sliver;
import be.iminds.ilabt.jfed.lowlevel.authority.AuthorityProvider;
import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * EasyContext contains:
 *   - list of GeniAuthority
 *   - selected GeniAuthority
 *   - GeniContext
 *   - selected Slice (including slivers for relevant authorities)
 *   - selected Sliver
 */
@Deprecated
public class EasyContext implements GeniUserProvider, AuthorityProvider {
    private List<SfaAuthority> authorityList;
    private SfaAuthority selectedAuthority;
    private GeniUserProvider contextProvider;
    private List<Slice> slices;
    private Slice selectedSlice;
//    private Sliver selectedSliver;

    public EasyContext(GeniUserProvider contextProvider) {
        if (contextProvider == null) throw new RuntimeException("contextProvider == null");
        this.contextProvider = contextProvider;
        this.authorityList = new ArrayList<SfaAuthority>();
        this.slices = new ArrayList<Slice>();
    }

    public List<SfaAuthority> getAuthorityList() {
        return authorityList;
    }

    //TODO this overwrites, maybe make one that adds non existing (and merges existing if needed)
    public void setAuthorityList(List<SfaAuthority> authorityList, SfaAuthority selectedAuthority) {
        this.authorityList = authorityList;
        this.selectedAuthority = selectedAuthority; //if null, will auto select one when requested
        fireChange();
    }

    public SfaAuthority getSelectedAuthority() {
        if (selectedAuthority == null) {
            //always select an authority, add one if needed
            if (authorityList.isEmpty()) {
                if (contextProvider.isUserLoggedIn() && contextProvider.getLoggedInGeniUser().getUserAuthority() != null)
                    authorityList.add(contextProvider.getLoggedInGeniUser().getUserAuthority());
            }
            selectedAuthority = authorityList.get(0);
            if (selectedAuthority == null) throw new RuntimeException("selectedAuthority == null");
        }
        return selectedAuthority;
    }
    @Override
    public SfaAuthority getAuthority() {
        return getSelectedAuthority();
    }

    public void setSelectedAuthority(SfaAuthority selectedAuthority) {
        if (selectedAuthority == this.selectedAuthority)
            return;

        if (selectedAuthority != null && authorityList.isEmpty())
            authorityList.add(selectedAuthority);

        if (selectedAuthority != null && (!authorityList.contains(selectedAuthority)))
            authorityList.add(selectedAuthority);

        this.selectedAuthority = selectedAuthority;
//        if (selectedSlice != null)
//            this.selectedSliver = selectedSlice.getSliver(selectedAuthority);
//        else
//            this.selectedSliver = null;
        fireChange();
    }

    @Override
    public GeniUser getLoggedInGeniUser() {
        return contextProvider.getLoggedInGeniUser();
    }
    @Override
    public boolean isUserLoggedIn() {
        return contextProvider.isUserLoggedIn();
    }

    public List<Slice> getSlices() {
        return slices;
    }
    public void setSlices(List<Slice> slices) {
        this.slices = slices;
        fireChange();
    }
    public void addSlice(Slice slice) {
        this.slices.add(slice);
        fireChange();
    }

    public Slice getSelectedSlice() {
        return selectedSlice;
    }

    public void setSelectedSlice(Slice selectedSlice) {
        if (selectedSlice == this.selectedSlice)
            return;
        this.selectedSlice = selectedSlice;
//        if (selectedSlice != null)
//            this.selectedSliver = selectedSlice.getSliver(selectedAuthority);
//        else
//            this.selectedSliver = null;
        fireChange();
    }

    public Sliver getSelectedSliver() {
        return selectedSlice.findSlivers(selectedAuthority).get(0);
    }

//    public void setSelectedSliver(Sliver selectedSliver) {
//        if (selectedSliver == this.selectedSliver)
//            return;
//        this.selectedSliver = selectedSliver;
//        fireChange();
//    }


    private List<EasyContextListener> changeListeners = new ArrayList<EasyContextListener>();
    public void fireChange() { //TODO make this private, and instead of calling this, make slice and sliver have a change listener mechanism triggering this
        for (EasyContextListener l : changeListeners)
            l.onEasyContextChange();
    }
    public void addEasyContextListener(EasyContextListener l) {
        changeListeners.add(l);
    }
    public void removeEasyContextListener(EasyContextListener l){
        changeListeners.remove(l);
    }
}
