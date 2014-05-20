package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import be.iminds.ilabt.jfed.lowlevel.GeniUser;
import be.iminds.ilabt.jfed.lowlevel.resourceid.ResourceUrn;
import thinlet.Thinlet;

/**
 * StringChooser
 */
public class UserUrnChooser extends AbstractChooser {
     private CredentialAndUrnHistory history;

    /* example user urn:
          urn:publicid:IDN+wall3.test.ibbt.be+user+wvdemeer
    */
    public UserUrnChooser(Thinlet thinlet, CredentialAndUrnHistory history, GeniUser context, boolean required, String text, boolean ifOptionalAddDefau) {
        super(thinlet, required, text,
                "<combobox name=\"choice\" text=\""+context.getUserUrn()+"\"  weightx=\"1\"/>", ifOptionalAddDefau);
        this.history = history;

        for (String userUrn : history.userUrnlist) {
            Object c = thinlet.create("choice");
            thinlet.setString(c, "text", userUrn);
            thinlet.add(choice, c);
        }
    }

    @Override
    public Object getChoiceValue() {
        return new ResourceUrn(thinlet.getString(choice, "text"));
    }
}
