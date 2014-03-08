package be.iminds.ilabt.jfed.lowlevel;

import be.iminds.ilabt.jfed.lowlevel.authority.SfaAuthority;
import be.iminds.ilabt.jfed.util.CommonsHttpClientXmlRpcTransportFactory;
import org.apache.xmlrpc.XmlRpcClient;

/**
 * GeniConnection
 */
public interface GeniConnection {
    boolean isDebugMode();

    boolean isFakeForDebugging();

    CommonsHttpClientXmlRpcTransportFactory getXmlRpcTransportFactory();

    XmlRpcClient getXmlRpcClient();

    String getServerUrl();

    void markError();

    boolean isError();

    SfaAuthority getGeniAuthority();
}
