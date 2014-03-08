package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import thinlet.Thinlet;

/**
 * StringChooser
 */
public class ChCredentialChooser extends AbstractCredentialChooser {
    public ChCredentialChooser(Thinlet thinlet, CredentialAndUrnHistory model, boolean required, String text) {
        super(thinlet, model, required, text, model.chCredentialList);
    }

    @Override
    public Object getChoiceValue(GeniCredential chosenCred) {
        return chosenCred;
    }

    @Override
    public void addCredential(GeniCredential cred) {
        history.chCredentialList.add(cred);
    }
}
