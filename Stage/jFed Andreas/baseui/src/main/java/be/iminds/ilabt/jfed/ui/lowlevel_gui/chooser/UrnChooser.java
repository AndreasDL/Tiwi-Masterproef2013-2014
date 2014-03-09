package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import thinlet.Thinlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * StringChooser
 */
public class UrnChooser implements Chooser {
    private Object pan;
    private Object choice;
    private Thinlet thinlet;
    private CredentialAndUrnHistory model;

    public UrnChooser(Thinlet thinlet, CredentialAndUrnHistory model, boolean required, String text, String defaultText) {
        this.thinlet = thinlet;
        this.model = model;

        String guixml =
                "<panel name=\"chooserpan\" columns=\"3\" gap=\"4\" top=\"4\" left=\"4\" weightx=\"1\">\n" +
//                "        <label text=\""+text+":\" />\n" +
                "        <textfield name=\"choice\" text=\""+defaultText+"\" columns=\"10\" weightx=\"1\"/>\n" +
                "</panel>";
        try {
            pan = thinlet.parse(new ByteArrayInputStream(guixml.getBytes()), this);
            choice = thinlet.find(pan, "choice");
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
        String urn = thinlet.getString(choice, "text");
        model.addAllUrn(urn);
        return new ResourceUrn(urn);
    }
}