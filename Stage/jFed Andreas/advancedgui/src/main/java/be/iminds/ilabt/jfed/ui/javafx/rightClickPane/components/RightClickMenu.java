package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components;


import be.iminds.ilabt.jfed.ui.javafx.rightClickPane.handlers.DeleteHandler;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class RightClickMenu extends ContextMenu {
    private MenuItem delete;
    private MenuItem properties;
    private PropertiesWindow prop;

    public RightClickMenu(NodePane pane,gNode n) {
        delete = new MenuItem("delete");
        delete.setOnAction(new DeleteHandler(pane,n));
        getItems().add(delete);

        prop = new PropertiesWindow();

        properties = new MenuItem("properties");
        properties.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage s = new Stage();
                s.setTitle("prop window");
                s.setScene(new Scene(prop));
                s.show();
            }
        });
        getItems().add(properties);
    }
}