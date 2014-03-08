package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class PropertiesWindow extends BorderPane {
    @FXML private Button btnAdd;
    @FXML private ListView<String> list;
    final ObservableList<String> items = FXCollections.observableArrayList();

    public PropertiesWindow() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("propertiesWindow.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        btnAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                items.add("hoi");
            }
        });
        list.setItems(items);
    }
}