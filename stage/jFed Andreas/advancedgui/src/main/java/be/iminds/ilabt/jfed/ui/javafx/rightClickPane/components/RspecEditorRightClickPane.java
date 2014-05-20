package be.iminds.ilabt.jfed.ui.javafx.rightClickPane.components;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class RspecEditorRightClickPane extends BorderPane {
    @FXML private javafx.scene.control.ComboBox<String> selectNodeType; //atm nog string later nodes dus
    @FXML private NodePane drawCanvas;
    @FXML private AnchorPane drawCanvasAnchor;
    @FXML private ToolBar topToolBar;
    @FXML private ToolBar botToolBar;
    @FXML private Button btnClear;

    @FXML private Button zoomDefault;
    @FXML private Slider zoomSlider;

    private ObservableList<String> types;


    public RspecEditorRightClickPane() {
        //init stuffs
        //load gui
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("rspecEditorRightClickPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        //make canvas autoresize ( http://stackoverflow.com/questions/18647386/javafx-canvas-in-anchorpane-getwidth-issue )
        //compute own size + add manually
        drawCanvas = new NodePane(zoomSlider);
        drawCanvas.prefWidthProperty().bind(drawCanvasAnchor.widthProperty());
        drawCanvas.prefHeightProperty().bind(drawCanvasAnchor.heightProperty());

        //add
        drawCanvasAnchor.getChildren().add(drawCanvas);

        //fix combobox
        loadNodeTypes();//fill combobox + select first item
        selectNodeType.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                drawCanvas.setType(selectNodeType.getValue().toString());
            }
        });

        //clear
        btnClear.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                drawCanvas.clear();
            }
        });

        //zoom
        zoomDefault.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                drawCanvas.zoomDefault();
            }
        });
        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                double zoom = number2.doubleValue();

                if (zoom <= 10 )
                   zoom /= 10;
                else
                   zoom -= 10;

                drawCanvas.setZoom(zoom);
            }
        });
    }

    //initfunctions
    private void loadNodeTypes(){   //hardcoded
        //load items   & set first
        types = FXCollections.observableArrayList(
                "node1", "node2", "node3"
        );
        selectNodeType.setItems(types);
        selectNodeType.setValue(types.get(0));
        drawCanvas.setType(types.get(0)); //aanpassen dat vanzelf mee veranderd.
    }
}
