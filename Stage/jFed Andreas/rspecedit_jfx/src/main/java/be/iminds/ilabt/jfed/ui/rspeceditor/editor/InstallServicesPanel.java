package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.value.ObservableValue;

/**
 * ExecuteServicesPanel
 */
public class InstallServicesPanel extends AbstractServicesPanel<RspecNode.InstallService> {

    @Override
    protected int getColumnCount() {
        return 2;
    }

    @Override
    protected RspecNode.InstallService emptyToAdd() {
//        RspecNode.ExecuteService newExecuteService = new RspecNode.ExecuteService(shellField.getText(), commandField.getText());
        return new RspecNode.InstallService("/", "http://example.com/software.tar.gz");
    }


    protected String columnName(int colIndex) {
        switch (colIndex) {
            case 0: return "Install Path";
            case 1: return "Archive URL";
            default : return "Unknown collumn";
        }
    }

    @Override
    protected ObservableValue<String> getColumnValue(RspecNode.InstallService item, int colIndex) {
        switch (colIndex) {
            case 0: return item.installPathProperty();
            case 1: return item.urlProperty();
            default : throw new RuntimeException("no such column "+colIndex);
        }
    }

    @Override
    protected void setColumnValue(RspecNode.InstallService item, int colIndex, String value) {
        switch (colIndex) {
            case 0: { item.setInstallPath(value); break; }
            case 1: { item.setUrl(value); break; }
            default : throw new RuntimeException("no such column "+colIndex);
        }
    }
}
