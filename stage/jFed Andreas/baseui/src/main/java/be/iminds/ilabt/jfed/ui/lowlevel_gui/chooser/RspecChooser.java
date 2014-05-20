package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;
import thinlet.Thinlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * StringChooser
 */
public class RspecChooser implements Chooser {
    private Object pan;
    private Object choice;
    private Thinlet thinlet;

    public RspecChooser(Thinlet thinlet, CredentialAndUrnHistory model, boolean required, String text, GeniUserProvider geniUserProvider) {
        this.thinlet = thinlet;

        String authUrnPart = "<AUTHORITY NAME>";
        if (geniUserProvider != null && geniUserProvider.isUserLoggedIn() &&
                geniUserProvider.getLoggedInGeniUser() != null && geniUserProvider.getLoggedInGeniUser().getUserAuthority() != null)
            authUrnPart = geniUserProvider.getLoggedInGeniUser().getUserAuthority().getNameForUrn();

        String defaultRspec = "<rspec type=\"request\" generated=\"2013-01-16T14:20:39Z\" xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 http://www.geni.net/resources/rspec/3/request.xsd \" xmlns:client=\"http://www.protogeni.net/resources/rspec/ext/client/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.geni.net/resources/rspec/3\">\n" +
                "  <node client_id=\"PC\" component_manager_id=\"urn:publicid:IDN+"+authUrnPart+"+authority+cm\" exclusive=\"true\">\n" +
                "    <sliver_type name=\"raw-pc\"/>\n" +
                "  </node>\n" +
                "</rspec>\n";

        String guixml =
                "<panel name=\"chooserpan\" columns=\"1\" gap=\"4\" top=\"4\" left=\"4\" weightx=\"1\">\n" +
                "        <label text=\""+text+":\" />\n" +
                "        <textarea name=\"choice\" text=\""+"\" columns=\"50\" rows=\"10\"  weightx=\"1\"  weighty=\"1\"/>\n" +
                "</panel>";
        try {
            pan = thinlet.parse(new ByteArrayInputStream(guixml.getBytes()), this);
            choice = thinlet.find(pan, "choice");
            thinlet.setString(choice, "text", defaultRspec);
            assert(choice != null);
        } catch (IOException e) {
            throw new RuntimeException("Should never fail, but did for: "+guixml, e);
        }
    }

    @Override
    public Object getThinletPanel() {
        return pan;
    }

    @Override
    public Object getChoice() {
        return thinlet.getString(choice, "text");
    }
}
