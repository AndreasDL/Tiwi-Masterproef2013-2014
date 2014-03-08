package be.iminds.ilabt.jfed.ui.rspeceditor.util;

import be.iminds.ilabt.jfed.ui.rspeceditor.editor.*;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.GuiEditable;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecLink;
import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * uses GuiEditable annotation to create GUI
 */
public class AutoEditPanel extends BorderPane implements Initializable {
    private BooleanProperty editable = new SimpleBooleanProperty(true);
    public boolean isEditable() {
        return editable.get();
    }
    public void setEditable(boolean editable) {
        this.editable.set(editable);
    }
    public BooleanProperty editableProperty() {
        return editable;
    }

    private Class targetClass;
    private SelectedObjectPropertyBinder selectedObjectPropertyBinder = new SelectedObjectPropertyBinder(true);

    public AutoEditPanel(Class targetClass) {
        this.targetClass = targetClass;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AutoEditPanel.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private List<TextField> textFields = new ArrayList<TextField>();

    //    @FXML private VBox propertiesVBox;
    @FXML private Label titleLabel;
    @FXML private ToolBar toolbar;
    @FXML private Button removeSelectedItemButton;
    @FXML private GridPane propertiesGridPane;
    private int propertiesGridPaneRowIndex = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert targetClass != null;

        //add extra nodes that have been added before init
        toolbar.getItems().addAll(extraNodes);

        //System.out.println("Checking fields for class \""+targetClass.getName()+"\": counted "+targetClass.getDeclaredFields().length);
        for (final Field f : targetClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(GuiEditable.class)) {
                //System.out.println("   Found field with GuiEditable annotation: "+f.getName());
                GuiEditable guiEditable = f.getAnnotation(GuiEditable.class);
                Class<?> fieldType = f.getType();

                //make private field accessible
                f.setAccessible(true);

                Class propertyClass = guiEditable.clazz();

                titleLabel.setText("Edit "+targetClass.getSimpleName());

                String guiName = guiEditable.guiName();
                if (guiName.equals("")) guiName = f.getName();
                String guiHelp = guiEditable.guiHelp();
                if (guiHelp.equals("")) guiHelp = guiName;

                //System.out.println("   field guiName=\""+guiName+"\" propertyClass=\""+propertyClass.getName()+"\"");

//                HBox hbox = new HBox();
                RowConstraints prevRC = propertiesGridPane.getRowConstraints().get(0);
                RowConstraints rc = new RowConstraints(
                        prevRC.minHeightProperty().get(),
                        prevRC.prefHeightProperty().get(),
                        prevRC.maxHeightProperty().get(),
                        prevRC.vgrowProperty().get(),
                        prevRC.valignmentProperty().get(),
                        prevRC.fillHeightProperty().get());
                propertiesGridPane.getRowConstraints().add(rc);


                CheckBox checkBox = null;
                if (guiEditable.nullable()) {
                    checkBox = new CheckBox();
                    checkBox.setSelected(false);
                    checkBox.setTooltip(new Tooltip("Disable this to exclude the field from the Rspec"));
//                    hbox.getChildren().add(checkBox);
                    propertiesGridPane.add(checkBox, 0, propertiesGridPaneRowIndex);

                    checkBox.disableProperty().bind(editable.not());
                }

                Label label = new Label();
                label.setText(guiName+":");
                label.setTooltip(new Tooltip(guiHelp));
//                hbox.getChildren().add(label);
                propertiesGridPane.add(label, 1, propertiesGridPaneRowIndex);

                boolean supportedProperty = false;

                if (propertyClass.equals(String.class)) {
                    supportedProperty = true;
                    //System.out.println("     creating TextField and adding String binding");

                    TextField textField = new TextField();
                    textField.setTooltip(new Tooltip(guiHelp));
                    textFields.add(textField);
//                    hbox.getChildren().add(textField);
                    propertiesGridPane.add(textField, 2, propertiesGridPaneRowIndex);

                    BooleanProperty isNotNullProperty = checkBox == null ? null : checkBox.selectedProperty();

                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(textField.textProperty(), isNotNullProperty) {
                        @Override public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    if (!guiEditable.editable()) {
                        textField.editableProperty().set(false);
                        if (isNotNullProperty != null)
                            label.disableProperty().bind(isNotNullProperty.not());
                    } else {
                        textField.editableProperty().bind(editable);
                        if (isNotNullProperty != null) {
                            label.disableProperty().bind(isNotNullProperty.not());
                            textField.disableProperty().bind(isNotNullProperty.not());
                        }
                    }

                }
                if (propertyClass.equals(Boolean.class)) {
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "due to restrictions due to JavaFX BooleanExpression, boolean fields can NOT be nullable";
                    //System.out.println("     creating TextField and adding String binding");

                    CheckBox truthBox = new CheckBox();
                    truthBox.setTooltip(new Tooltip(guiHelp));
//                    hbox.getChildren().add(truthBox);
                    propertiesGridPane.add(truthBox, 2, propertiesGridPaneRowIndex);

                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(truthBox.selectedProperty()) {
                        @Override public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });


                    if (!guiEditable.editable())
                        truthBox.disableProperty().set(true);
                    else
                        truthBox.disableProperty().bind(editable.not());
                }
                if (propertyClass.equals(ListProperty.class) && guiEditable.listClass().equals(RspecNode.ExecuteService.class)) {
                    assert(!supportedProperty);
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "it makes no sense for Collection types to be nullable";

                    ExecuteServicesPanel executeServicePanel = new ExecuteServicesPanel();
                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(executeServicePanel.servicesProperty()) {
                        @Override
                        public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

//                    executeServicePanel.setTooltip(new Tooltip(guiHelp));
                    propertiesGridPane.add(executeServicePanel, 2, propertiesGridPaneRowIndex);

                    if (!guiEditable.editable())
                        executeServicePanel.editableProperty().set(false);
                    else
                        executeServicePanel.editableProperty().bind(editable);
                }
                if (propertyClass.equals(ListProperty.class) && guiEditable.listClass().equals(RspecNode.InstallService.class)) {
                    assert(!supportedProperty);
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "it makes no sense for Collection types to be nullable";

                    InstallServicesPanel installServicePanel = new InstallServicesPanel();
                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(installServicePanel.servicesProperty()) {
                        @Override
                        public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

//                    installServicePanel.setTooltip(new Tooltip(guiHelp));
                    propertiesGridPane.add(installServicePanel, 2, propertiesGridPaneRowIndex);

                    if (!guiEditable.editable())
                        installServicePanel.editableProperty().set(false);
                    else
                        installServicePanel.editableProperty().bind(editable);
                }
                if (propertyClass.equals(ListProperty.class) && guiEditable.listClass().equals(RspecNode.LoginService.class)) {
                    assert(!supportedProperty);
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "it makes no sense for Collection types to be nullable";

                    LoginServicesPanel loginServicePanel = new LoginServicesPanel();
                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(loginServicePanel.servicesProperty()) {
                        @Override
                        public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

//                    loginServicePanel.setTooltip(new Tooltip(guiHelp));
                    propertiesGridPane.add(loginServicePanel, 2, propertiesGridPaneRowIndex);

                    if (!guiEditable.editable())
                        loginServicePanel.editableProperty().set(false);
                    else
                        loginServicePanel.editableProperty().bind(editable);
                }
                if (propertyClass.equals(ListProperty.class) && guiEditable.listClass().equals(RspecLink.LinkSetting.class)) {

                    assert(!supportedProperty);
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "it makes no sense for Collection types to be nullable";

                    LinkSettingsPanel linkSettingsPanel = new LinkSettingsPanel();
                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(linkSettingsPanel.linkSettingsProperty()) {
                        @Override
                        public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    linkSettingsPanel.setTooltip(new Tooltip(guiHelp));
                    propertiesGridPane.add(linkSettingsPanel, 2, propertiesGridPaneRowIndex);

                    if (!guiEditable.editable())
                        linkSettingsPanel.editableProperty().set(false);
                    else
                        linkSettingsPanel.editableProperty().bind(editable);
                }
                if (propertyClass.equals(ListProperty.class) && guiEditable.listClass().equals(String.class)) {
                    assert(!supportedProperty);
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "it makes no sense for Collection types to be nullable";

                    EditableStringListPanel editableStringListPanel = new EditableStringListPanel();
                    selectedObjectPropertyBinder.getBinders().add(new ObjectPropertyBindHelper(editableStringListPanel.listProperty()) {
                        @Override
                        public Property objectProperty(Object o) {
                            try {
                                return (Property) f.get(o);
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                    propertiesGridPane.add(editableStringListPanel, 2, propertiesGridPaneRowIndex);

                    if (!guiEditable.editable())
                        editableStringListPanel.editableProperty().set(false);
                    else
                        editableStringListPanel.editableProperty().bind(editable);
                }
                if (!supportedProperty && (propertyClass.equals(ObservableList.class) || propertyClass.equals(ListProperty.class) || propertyClass.equals(List.class))) {
                    supportedProperty = true;
                    assert guiEditable.nullable() == false : "it makes no sense for Collection types to be nullable";

                    Label labelTodo = new Label();
                    labelTodo.setText("TODO: add editable list \""+guiEditable.guiName()+"\"of "+guiEditable.listClass().getName());
                    labelTodo.setWrapText(true);
                    labelTodo.setTooltip(new Tooltip(guiHelp));
                    propertiesGridPane.add(labelTodo, 2, propertiesGridPaneRowIndex);
                }

                if (!supportedProperty) {
                    Label labelTodo = new Label();
                    labelTodo.setText("TODO: add support for editing of \""+guiEditable.guiName()+"\" of type "+guiEditable.clazz().getName());
                    labelTodo.setWrapText(true);
                    labelTodo.setTooltip(new Tooltip(guiHelp));
                    propertiesGridPane.add(labelTodo, 2, propertiesGridPaneRowIndex);
                }

//                propertiesVBox.getChildren().add(hbox);
                propertiesGridPaneRowIndex++;
            }
        }

        boolean objectSelected = false;
        if (selectedObjectProperty != null && selectedObjectProperty.get() != null)
            objectSelected = true;
        updateObjectSelected(objectSelected);

    }

    public void updateObjectSelected(boolean objectSelected) {
        this.setVisible(objectSelected);
        propertiesGridPane.setVisible(objectSelected);
        removeSelectedItemButton.setVisible(objectSelected && editable.get());

        if (objectSelected)
            this.toFront();
        else
            this.toBack();
    }

    /** set an ObjectProperty for the object the annotated fields belong too. */
    private ObjectProperty selectedObjectProperty;
    public void setSelection(ObjectProperty selectedObjectProperty) {
        this.selectedObjectProperty = selectedObjectProperty;
        selectedObjectPropertyBinder.setSelectedObjectProperty(selectedObjectProperty);
        selectedObjectProperty.addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observableValue, Object oldValue, Object newValue) {
                updateObjectSelected(newValue != null);
            }
        });
    }


    public interface SelectedObjectRemover<T> {
        public void removeSelected(T selectedObject);
    }
    private SelectedObjectRemover selectedObjectRemover;
    public void setSelectedObjectRemover(SelectedObjectRemover selectedObjectRemover) {
        this.selectedObjectRemover = selectedObjectRemover;
    }

    public void removeSelectedItem() {
        Object selectedItem = selectedObjectProperty.get();
        if (selectedItem != null) {
            if (selectedObjectRemover != null)
                selectedObjectRemover.removeSelected(selectedItem);
        }
    }



    private final List<Node> extraNodes = new ArrayList<Node>();
    public void addButton(Node extraNode) {
        extraNodes.add(extraNode);
        if (toolbar != null)
            toolbar.getItems().add(extraNode);
    }
}
