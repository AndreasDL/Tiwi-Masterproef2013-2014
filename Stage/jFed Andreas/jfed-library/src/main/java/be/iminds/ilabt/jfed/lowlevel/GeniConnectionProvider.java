package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.ClientSslAuthenticationXmlRpcTransportFactory;

import java.net.URL;

/**
 * GeniConnectionProvider
 */
public interface GeniConnectionProvider {
    /** debug mode will print out a info during the call */
    public boolean isDebugMode();
    /** debug mode will print out a info during the call */
    public void setDebugMode(boolean debugMode);

    //GeniUser user is only required for HTTPS connections

    public GeniConnection getConnectionByAuthority(GeniUser user, SfaAuthority authority, Class targetClass) throws GeniException;
    public GeniConnection getConnectionByAuthority(GeniUser user, SfaAuthority authority, ServerType serverType) throws GeniException;

    public GeniConnection getConnectionByUserAuthority(GeniUser user, ServerType serverType) throws GeniException;

    public GeniConnection getConnectionByUrl(GeniUser user, URL url, ClientSslAuthenticationXmlRpcTransportFactory.HandleUntrustedCallback handleUntrustedCallback) throws GeniException;
}
