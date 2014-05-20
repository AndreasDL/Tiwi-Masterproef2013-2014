package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.lowlevel.GeniUserProvider;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import thinlet.Thinlet;

/**
 * StringChooser
 */
public class SliceUrnChooser extends AbstractChooser {
    private CredentialAndUrnHistory model;

    private static String defaultSliceName(GeniUserProvider geniUserProvider) {
        String authUrnPart = "<AUTHORITY NAME>";
        if (geniUserProvider != null &&
            geniUserProvider.isUserLoggedIn() &&
            geniUserProvider.getLoggedInGeniUser() != null &&
            geniUserProvider.getLoggedInGeniUser().getUserAuthority() != null)
                authUrnPart = geniUserProvider.getLoggedInGeniUser().getUserAuthority().getNameForUrn();

        return "urn:publicid:IDN+"+authUrnPart+"+slice+sliceName";
    }

    /* example slice urn:
          urn:publicid:IDN+wall3.test.ibbt.be+slice+TestSliceA
    */
    public SliceUrnChooser(Thinlet thinlet, CredentialAndUrnHistory model, boolean required, String text, boolean ifOptionalAddDefault, GeniUserProvider geniUserProvider) {
        super(thinlet, required, text,
                "<combobox name=\"choice\" text=\""+defaultSliceName(geniUserProvider)+"\"  weightx=\"1\"/>", ifOptionalAddDefault);
        this.model = model;

        for (String userUrn : model.sliceUrnlist) {
            Object c = thinlet.create("choice");
            thinlet.setString(c, "text", userUrn);
            thinlet.add(choice, c);
        }

        //auto select last slice URN in list
        if (model.sliceUrnlist.size() > 0) {
            int index = model.sliceUrnlist.size()-1;
            String urn = model.sliceUrnlist.get(index);
            thinlet.setInteger(choice, "selected", index);
            thinlet.setString(choice, "text", urn);
        }
    }

    @Override
    public Object getChoiceValue() {
        String urn = thinlet.getString(choice, "text");
        model.addSliceUrn(urn);
        return new ResourceUrn(urn);
    }
}
