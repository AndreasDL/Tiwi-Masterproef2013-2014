package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.value.ObservableValue;

/**
 * ExecuteServicesPanel
 */
public class ExecuteServicesPanel extends AbstractServicesPanel<RspecNode.ExecuteService> {

    @Override
    protected int getColumnCount() {
        return 2;
    }

    @Override
    protected RspecNode.ExecuteService emptyToAdd() {
//        RspecNode.ExecuteService newExecuteService = new RspecNode.ExecuteService(shellField.getText(), commandField.getText());
        return new RspecNode.ExecuteService("sh", "/");
    }


    protected String columnName(int colIndex) {
        switch (colIndex) {
            case 0: return "Shell";
            case 1: return "Command";
            default : return "Unknown collumn";
        }
    }

    @Override
    protected ObservableValue<String> getColumnValue(RspecNode.ExecuteService item, int colIndex) {
        switch (colIndex) {
            case 0: return item.shellProperty();
            case 1: return item.commandProperty();
            default : throw new RuntimeException("no such column "+colIndex);
        }
    }

    @Override
    protected void setColumnValue(RspecNode.ExecuteService item, int colIndex, String value) {
        switch (colIndex) {
            case 0: { item.setShell(value); break; }
            case 1: { item.setCommand(value); break; }
            default : throw new RuntimeException("no such column "+colIndex);
        }
    }
}
