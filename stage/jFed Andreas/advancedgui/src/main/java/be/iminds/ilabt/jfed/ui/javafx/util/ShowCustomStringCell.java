package be.iminds.ilabt.jfed.ui.javafx.util;

import be.iminds.ilabt.jfed.highlevel.model.AuthorityInfo;
import javafx.scene.control.*;
import javafx.util.Callback;

/**
 * ShowCustomStringCell
 */
public class ShowCustomStringCell<T> extends ListCell<T> {
    public ShowCustomStringCell() {
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(toString(item));
            setGraphic(null);
        }
    }
    public String toString(T t) {
        return t == null ? "" : t.toString();
    }

    //as this doesn't override toString, this does the same as not overriding the default CellFactory at all
    //it is still useful to have this, just as reference code
    public static <T> Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> abstractApiListView) {
                return new ShowCustomStringCell<T>();
            }
        };
    }
}
