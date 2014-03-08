package be.iminds.ilabt.jfed.ui.lowlevel_gui.chooser;

import be.iminds.ilabt.jfed.lowlevel.GeniCredential;
import be.iminds.ilabt.jfed.history.CredentialAndUrnHistory;
import thinlet.Thinlet;

import java.util.ArrayList;
import java.util.List;

/**
 * CredentialListChooser: choose one or multiple credentials
 */
public class CredentialListChooser  extends AbstractCredentialChooser {
    public CredentialListChooser(Thinlet thinlet, CredentialAndUrnHistory history, boolean required, String text) {
        super(thinlet, history, required, text, history.allCredentialList(), true);
    }

    @Override
    public Object getChoiceValue(GeniCredential chosenCred) {
        List<GeniCredential> res = new ArrayList<GeniCredential>();
        if (chosenCred != null)
            res.add(chosenCred);
        return res;
    }

    @Override
    public Object getChoiceValue(List<GeniCredential> chosenCred) {
        return chosenCred;
    }

    @Override
    public void addCredential(GeniCredential cred) {
        //add to both
        history.userCredentialList.add(cred);
        history.sliceCredentialList.add(cred);
    }
}
