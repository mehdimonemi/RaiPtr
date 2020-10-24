package company;

import com.jfoenix.controls.*;
import com.jfoenix.controls.cells.editors.IntegerTextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.TextFieldEditorBuilder;
import com.jfoenix.controls.cells.editors.base.GenericEditableTreeTableCell;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeSortMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.FlowPane;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Table {
    static JFXTreeTableView<commodity> treeView;
    static JFXTreeTableColumn<commodity, String> wagonColumn;
    static JFXTreeTableColumn<commodity, String> freightColumn;
    static JFXTreeTableColumn<commodity, String> numberColumn;
    static JFXTreeTableColumn<commodity, Integer> priorityColumn;

    public static JFXTreeTableView<commodity> Table(ArrayList<String[]> acceptedRecords) {

        wagonColumn = new JFXTreeTableColumn<>("واگن");
        wagonColumn.setPrefWidth(100);
        wagonColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<commodity, String> param) -> {
            if (wagonColumn.validateValue(param)) {
                return param.getValue().getValue().wagon;
            } else {
                return wagonColumn.getComputedValue(param);
            }
        });

        freightColumn = new JFXTreeTableColumn<>("بار");
        freightColumn.setPrefWidth(100);
        freightColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<commodity, String> param) -> {
            if (freightColumn.validateValue(param)) {
                return param.getValue().getValue().freight;
            } else {
                return freightColumn.getComputedValue(param);
            }
        });

        numberColumn = new JFXTreeTableColumn<>("تعداد");
        numberColumn.setPrefWidth(100);
        numberColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<commodity, String> param) -> {
            if (numberColumn.validateValue(param)) {
                return param.getValue().getValue().value;
            } else {
                return numberColumn.getComputedValue(param);
            }
        });

        priorityColumn = new JFXTreeTableColumn<>("اولویت");
        priorityColumn.setPrefWidth(100);
        priorityColumn.setCellValueFactory((TreeTableColumn.CellDataFeatures<commodity, Integer> param) -> {
            if (priorityColumn.validateValue(param)) {
                return param.getValue().getValue().priority.asObject();
            } else {
                return priorityColumn.getComputedValue(param);
            }
        });

        priorityColumn.setCellFactory((TreeTableColumn<commodity, Integer> param) -> new GenericEditableTreeTableCell<>(
                new IntegerTextFieldEditorBuilder()));

        priorityColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<commodity, Integer>>() {
                    @Override
                    public void handle(CellEditEvent<commodity, Integer> t) {
                        t.getTreeTableView()
                                .getTreeItem(t.getTreeTablePosition().getRow())
                                .getValue().priority.set(t.getNewValue());
                        treeView.edit(t.getTreeTablePosition().getRow() + 1, priorityColumn);
                    }
                });

        // data
        ArrayList<commodity> temp = new ArrayList<>();
        int i = 0;
        for (String[] string : acceptedRecords) {
            Boolean flag = true;
            for (commodity commodity : temp) {
                if (commodity.wagon.getValue().equals(string[3]) && commodity.freight.getValue().equals(string[4])) {
                    flag = false;
                    commodity.value.setValue(String.valueOf(Integer.parseInt(commodity.value.getValue()) + Integer.parseInt(string[2])));
                }
            }
            if (flag) {
                temp.add(new commodity(string[3], string[4], string[2], 0));
            }

        }

        ObservableList<commodity> commodities = FXCollections.observableArrayList(temp);

        // build tree
        final TreeItem<commodity> root = new RecursiveTreeItem<>(commodities, RecursiveTreeObject::getChildren);
        treeView = new JFXTreeTableView<>(root);

        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.getColumns().setAll(wagonColumn, freightColumn, numberColumn, priorityColumn);

        treeView.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        return treeView;
    }


    public static String getTreeCell(int row, String string) {
        switch (string) {
            case "wagon": {
                return treeView.getTreeItem(row).getValue().wagon.getValue();
            }
            case "freight": {
                return treeView.getTreeItem(row).getValue().freight.getValue();
            }
            case "priority": {
                return String.valueOf(treeView.getTreeItem(row).getValue().priority.getValue());
            }
        }
        return null;
    }

    public static final class commodity extends RecursiveTreeObject<commodity> {
        final StringProperty wagon;
        final StringProperty freight;
        final StringProperty value;
        final IntegerProperty priority;

        commodity(String wagon, String freight, String value, Integer priority) {
            this.wagon = new SimpleStringProperty(wagon);
            this.freight = new SimpleStringProperty(freight);
            this.value = new SimpleStringProperty(value);
            this.priority = new SimpleIntegerProperty(priority);
        }
    }
}