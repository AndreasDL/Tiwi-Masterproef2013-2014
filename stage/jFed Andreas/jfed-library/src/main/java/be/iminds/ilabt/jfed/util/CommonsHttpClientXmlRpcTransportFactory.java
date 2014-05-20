package be.iminds.ilabt.jfed.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.xmlrpc.XmlRpcClientException;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcTransport;
import org.apache.xmlrpc.XmlRpcTransportFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * CommonsHttpClientXmlRpcTransportFactory is an implementation of the apache commons XML RPC library {@link XmlRpcTransportFactory} interface.
 * which makes use of the apache commons {@link DefaultHttpClient}.
 *
 * It also captures the data sent and received, for debugging purposes.
*/
public class CommonsHttpClientXmlRpcTransportFactory implements XmlRpcTransportFactory {
    private String serverUrlStr;
    private DefaultHttpClient httpClient;


    /* debug will print out a info during the call */
    private boolean debugMode;
    /** debug mode will print out a info during the call */
    public boolean isDebugMode() {
        return debugMode;
    }
    /** debug mode will print out a info during the call */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    /**
     * Note: the "Http History" automatically resets on new XmlRpc call
     * */
    public CommonsHttpClientXmlRpcTransportFactory(String serverUrlStr, DefaultHttpClient httpClient, boolean debugMode) {
        this.serverUrlStr = serverUrlStr;
        this.httpClient = httpClient;
        this.debugMode = debugMode;
    }


    StringBuilder httpResult = new StringBuilder(1000);
    public String getHttpReceivedHistory() {
        return httpResult.toString();
    }

    private String httpSent = "";
    public String getHttpSentHistory() {
        return httpSent;
    }

    private void resetHttpHistory() {
        httpResult = new StringBuilder(1000);
        httpSent = "";
    }

    private class SpyingInputStream extends InputStream {
        private InputStream is;
        public SpyingInputStream(InputStream is) {
            this.is = is;
        }

        @Override
        public int read() throws IOException {
            int r = is.read();
            if (r >= 0)
                httpResult.append((char) r);
//                httpResult += (char)r;

            if (debugMode)
                System.out.print((char)r);
            return r;
        }
    }

    public static class HttpServerErrorException extends Exception {
        private int statusNr;
        private String reason;
        public HttpServerErrorException(int statusNr, String reason) {
            super("HTTP Server is busy");
            this.statusNr = statusNr;
            this.reason = reason;
        }

        public int getStatusNr() {
            return statusNr;
        }

        public String getReason() {
            return reason;
        }
    }
    class MyXmlRpcTransport implements XmlRpcTransport {
        HttpEntity entity = null;
        public InputStream sendXmlRpc(byte[] bytes) throws IOException, org.apache.xmlrpc.XmlRpcClientException {

            //note: we cannot assume the xmlrpc data of one command is sent in one call to this method
            if (entity == null)
                resetHttpHistory();

//            HttpPut httpput = new HttpPut(serverUrlStr);
            HttpPost httpput = new HttpPost(serverUrlStr);
            httpSent+=httpput;
            StringEntity myEntity = new StringEntity(new String(bytes), ContentType.create("text/xml", "UTF-8"));
            httpput.setEntity(myEntity);
            HttpResponse response = null;

            try {
                httpSent += new String(bytes);

                if (debugMode)
                    System.out.print("DEBUG CommonsHttpClientXmlRpcTransportFactory.MyXmlRpcTransport.sendXmlRpc is sending "+bytes.length+" bytes");
//                System.out.println("Using httpClient=" + httpClient);
                response = httpClient.execute(httpput);

                //not a good way to do this
                if (Thread.currentThread().isInterrupted())
                    throw new XmlRpcClientException("cancelled", new InterruptedException());

                if (debugMode)
                    System.out.print("DEBUG CommonsHttpClientXmlRpcTransportFactory.MyXmlRpcTransport.sendXmlRpc has sent. response.getStatusLine()="+response.getStatusLine());

                if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 500/*Busy*/)
                    throw new HttpServerErrorException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase());
            } catch (javax.net.ssl.SSLPeerUnverifiedException ex) {
                throw new org.apache.xmlrpc.XmlRpcClientException("The server certificate for '"+httpput.getURI()+"' could not be verified. " +
                        "Error message \""+ex.getMessage()+"\". " +
                        "Possible causes:\n" +
                        "  - The server's self-signed certificate is not in our trust store. \n" +
                        "  - The server's certificate \"CN\" field is not the server hostname or a known alias. \n" +
                        "  - The server is not accepting our client login certificate+key pair (perhaps is is not federated with our \"login provider\"). \n" +
                        "  - If the server's certificate is not self signed, we might not have the root certificate of the trust chain in our trust store.", ex);
            } catch (SSLException ex) {
                throw new org.apache.xmlrpc.XmlRpcClientException("SSLException: "+ex.getMessage(), ex);
            } catch (HttpServerErrorException ex) {
                /*first read received entity for logs*/
                entity = response.getEntity();
                if (entity != null)
                {
                    InputStream is = new SpyingInputStream(entity.getContent());
                    new Scanner(is,"UTF-8").useDelimiter("\\A").next(); //this reads entire stream. We discard the result.
                }
                throw new org.apache.xmlrpc.XmlRpcClientException("HTTP Server error", ex);
            }
            entity = response.getEntity();

            return new SpyingInputStream(entity.getContent());
            //return entity.getContent();
        }

        public void endClientRequest() throws org.apache.xmlrpc.XmlRpcClientException {
            if (entity != null) try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                e.printStackTrace();
            }
            entity = null;
        }
    }

    private MyXmlRpcTransport transport = null;

    public XmlRpcTransport createTransport() throws org.apache.xmlrpc.XmlRpcClientException {
        if (transport == null)
            transport = new MyXmlRpcTransport();
        return transport;
    }

    public void setProperty(String s, Object o) { }
}
