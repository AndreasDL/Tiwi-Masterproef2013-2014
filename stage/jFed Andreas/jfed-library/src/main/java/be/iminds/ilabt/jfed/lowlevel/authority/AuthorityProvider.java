package be.iminds.ilabt.jfed.lowlevel.authority;

/**
 * AuthorityProvider: interface for classes that provides an authority on demand
 */
public interface AuthorityProvider {
    public SfaAuthority getAuthority();
}
