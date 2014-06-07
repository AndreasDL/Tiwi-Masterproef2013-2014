package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import thinlet.Thinlet;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * UrnListChooser lets a user choose one or multiple from a list of urns
 */
public class UrnListChooser implements Chooser {
    private Object pan;
    private Object choice;
    private Thinlet thinlet;
    private CredentialAndUrnHistory model;

    private List<Object> allItems;

    public UrnListChooser(Thinlet thinlet, CredentialAndUrnHistory model, boolean required, String text) {
        this.thinlet = thinlet;
        this.model = model;

        String urnModel = "urn:publicid:IDN+<AUTH>+slice+<NAME>";
        allItems = new ArrayList<Object>();

        String guixml =
                "<panel name=\"chooserpan\" columns=\"2\" gap=\"4\" top=\"4\" left=\"4\" weightx=\"1\" weighty=\"1\">\n" +
//                "        <label text=\""+text+" (select multiple):\" colspan=\"2\" />\n" +
                "        <label text=\"select one or multiple URNs:\" colspan=\"2\" />\n" +
                "        <list selection=\"multiple\" name=\"choice\" weightx=\"1\" weighty=\"1\" colspan=\"2\"/>\n" +
                "        <textfield name=\"addField\" text=\""+urnModel+"\" columns=\"10\" weightx=\"1\" visible=\"false\"/>\n" +
                "        <button name=\"addBut\" text=\"add\" action=\"add(addField.text, showBut, addBut, addField)\" visible=\"false\"/>\n" +
                "        <button name=\"showBut\" text=\"Manually add URN to list...\" action=\"showAdd(showBut, addBut, addField)\" colspan=\"2\" weightx=\"0\" halign=\"right\"/>\n" +
                "</panel>";
        try {
            pan = thinlet.parse(new ByteArrayInputStream(guixml.getBytes()), this);
            choice = thinlet.find(pan, "choice");
            String selectedUrn = model.getAllUrnlist().isEmpty() ? "" : model.getAllUrnlist().get(model.getAllUrnlist().size()-1);
            for (String urn : model.getAllUrnlist()) {
                Object item = thinlet.create("item");
                thinlet.setString(item, "text", urn);
                if (selectedUrn.equals(urn))
                    thinlet.setBoolean(item, "selected", true);
                thinlet.add(choice, item);
                allItems.add(item);
            }
            assert(choice != null);
        } catch (IOException e) {
            throw new RuntimeException("Should never fail, but did for: "+guixml, e);
        }
    }

    public void add(String urn, Object showBut, Object addBut, Object addField) {
        Object item = thinlet.create("item");
        thinlet.setString(item, "text", urn);
        thinlet.add(choice, item);
        allItems.add(item);
        thinlet.setBoolean(showBut, "visible", true);
        thinlet.setBoolean(addBut, "visible", false);
        thinlet.setBoolean(addField, "visible", false);
    }

    public void showAdd(Object showBut, Object addBut, Object addField) {
        thinlet.setBoolean(showBut, "visible", false);
        thinlet.setBoolean(addBut, "visible", true);
        thinlet.setBoolean(addField, "visible", true);
    }

    @Override
    public Object getThinletPanel() {
        return pan;
    }

    @Override
    public Object getChoice() {
        List<String> selectedUrns = new ArrayList<String>();
        for (Object item : allItems) {
            String urn = thinlet.getString(item, "text");
            if (thinlet.getBoolean(item, "selected")) {
                selectedUrns.add(urn);
            }
            model.addAllUrn(urn);
        }

        return new ArrayList<String>(selectedUrns);
    }
}
