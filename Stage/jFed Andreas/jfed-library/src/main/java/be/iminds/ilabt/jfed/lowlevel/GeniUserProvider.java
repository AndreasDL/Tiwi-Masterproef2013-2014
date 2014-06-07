package be.iminds.ilabt.jfed.lowlevel;

/**
 * GeniUserProvider
 */
public interface GeniUserProvider {
    public GeniUser getLoggedInGeniUser();
    public boolean isUserLoggedIn();
}
