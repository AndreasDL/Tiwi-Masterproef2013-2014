package be.iminds.ilabt.jfed.highlevel.model;

import be.iminds.ilabt.jfed.lowlevel.GeniCredential;

/**
 * CredentialInfo wraps GeniCredential
 *
 * fields of credential: name, xml, type and version
 *
 * TODO: do I need this wrapper?
 */
public class CredentialInfo {
    private GeniCredential credential;

    public CredentialInfo(GeniCredential credential) {
        this.credential = credential;
    }

    public GeniCredential getCredential() {
        return credential;
    }


    public String toString() {
        return credential.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CredentialInfo that = (CredentialInfo) o;

        if (credential != null ? !credential.equals(that.credential) : that.credential != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return credential != null ? credential.hashCode() : 0;
    }
}
