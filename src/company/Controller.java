package company;

import com.jfoenix.controls.*;
import company.backend1.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.awt.*;
import java.io.*;
import java.util.Optional;
import java.util.Set;

import static company.backend1.Massages.*;
import static company.backend1.MinimizeLoco.locoDepoTime;
import static company.backend1.MinimizeLoco.locoOfficeWorkTime;
import static company.backend1.ReadTypicalData.rawInputs;
import static company.backend1.TrainMaker.longTrainLength;
import static company.backend1.Formation.*;
import static company.windows.*;
import static company.Table.*;

public class Controller {

    TrainMaker trainMaker = new TrainMaker();
    ReadTypicalData readTypicalData = new ReadTypicalData();
    Formation formation = new Formation();
    MinimizeLoco minimizeLoco = new MinimizeLoco();

    int whatwanado = 0;

    @FXML
    private StackPane test;

    @FXML
    private JFXTabPane tabContainer;

    @FXML
    private Tab readData, formationTab, locoTab, exitTab;

    @FXML
    private AnchorPane readDataContainer, formationTabContainer, locoTabContainer;


    @FXML
    private JFXTextArea textArea;

    @FXML
    private GridPane gridPane1, gridPane2;

    @FXML
    private TextField dataPath, textField1,
            textField2, textField3, textField4, textField5, textField6, textField7, textField8, textField9, textField10;

    private double tabWidth = 90.0;
    public static int lastSelectedTabIndex = 0;

    @FXML
    private JFXButton chooseDataButton, readDataButton, formationButton,
            formationOutputButton, showFormationExcelButton, minimizeLocoButton, minimizeLocoOutputButton,
            showLocoExcelButton, readGraphDataButton, priorityButton, lastCheckButton;

    @FXML
    private JFXSpinner readDataSpinner, formationSpinner, locoSpinner;

    @FXML
    private JFXRadioButton radioButton1, radioButton2;

    @FXML
    private JFXProgressBar progressBar;

    String dataFilePath;
    String formationFilePath;
    String locoFilePath;

    @FXML
    public void initialize() {
        configureView();
        readDataTabOnAction();
        formationTabOnAction();
        minimizeLocoTabOnAction();
    }

    private void configureView() {
        tabContainer.setTabMinWidth(tabWidth);
        tabContainer.setTabMaxWidth(tabWidth);
        tabContainer.setTabMinHeight(tabWidth);
        tabContainer.setTabMaxHeight(tabWidth);
        tabContainer.setRotateGraphic(true);

        EventHandler<Event> replaceBackgroundColorHandler = event -> {
            lastSelectedTabIndex = tabContainer.getSelectionModel().getSelectedIndex();

            Tab currentTab = (Tab) event.getTarget();
            if (currentTab.isSelected()) {
                currentTab.setStyle("-fx-background-color: -jfx-light-primary-color;");
            } else {
                currentTab.setStyle("-fx-background-color: -jfx-primary-color;");
            }
        };

        EventHandler<Event> logoutHandler = event -> {

            Tab currentTab = (Tab) event.getTarget();
            if (currentTab.isSelected()) {
                tabContainer.getSelectionModel().select(lastSelectedTabIndex);

                ButtonType foo = new ButtonType("بله", ButtonBar.ButtonData.OK_DONE);
                ButtonType bar = new ButtonType("خیر", ButtonBar.ButtonData.CANCEL_CLOSE);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "آیا مایل به خروج از برنامه هستید؟", foo, bar);
                alert.setTitle("Rail Plan");
                alert.setHeaderText("");


                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(getClass().getResource("dialog.css").toExternalForm());
                dialogPane.getStyleClass().add("myDialog");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.orElse(bar) == foo) {
                    try {
                        BufferedWriter bufwriter = new BufferedWriter(new FileWriter("Data/dat"));
                        bufwriter.write(dataLocation);
                        bufwriter.close();
                    } catch (IOException e) {
                        System.out.println(Massages.FileNotFound);
                    } finally {
                        System.exit(0);
                    }
                }
            }
        };

        configureTab(readData, "اطلاعات پایه", "/company/resource/images/data.png"
                , readDataContainer, replaceBackgroundColorHandler);
        configureTab(formationTab, "تشکیل قطار", "/company/resource/images/formation.png",
                formationTabContainer, replaceBackgroundColorHandler);
        configureTab(locoTab, "تخصیص دیزل", "/company/resource/images/loco.png",
                locoTabContainer, replaceBackgroundColorHandler);
        configureTab(exitTab, "خروج", "/company/resource/images/logout.png",
                null, logoutHandler);

        readData.setStyle("-fx-background-color: -fx-focus-color;");

        setHintTextInTextField(textField1, String.valueOf(trainOfficeWorkTime));
        setHintTextInTextField(textField2, String.valueOf(trainOthersTime));
        setHintTextInTextField(textField3, String.valueOf(oneWagonManovrTime));
        setHintTextInTextField(textField4, String.valueOf(maxLongSouthTrainNumber));
        setHintTextInTextField(textField5, String.valueOf(maxLongNorthTrainNumber));
        setHintTextInTextField(textField6, String.valueOf(minTrainLength));
        setHintTextInTextField(textField7, String.valueOf(longTrainLength));
        setHintTextInTextField(textField8, String.valueOf(locoInHand));
        setHintTextInTextField(textField9, String.valueOf(locoDepoTime));
        setHintTextInTextField(textField10, String.valueOf(locoOfficeWorkTime));

    }

    public void setHintTextInTextField(TextField textField, String value) {
        textField.setPromptText(value); //to set the hint text
        textField.getParent().requestFocus();
    }

    private void configureTab(Tab tab, String title, String iconPath, AnchorPane containerPane,
                              EventHandler<Event> onSelectionChangedEvent) {
        double imageWidth = 40.0;

        ImageView imageView = new ImageView(new Image(iconPath));
        imageView.setFitHeight(imageWidth);
        imageView.setFitWidth(imageWidth);

        Label label = new Label(title);
        label.setTextFill(Color.web("#FFFFFF"));
        label.setMaxWidth(tabWidth - 20);
        label.setPadding(new Insets(5, 0, 0, 0));
        label.setTextAlignment(TextAlignment.CENTER);
        BorderPane tabPane = new BorderPane();
        tabPane.setRotate(90.0);
        tabPane.setMaxWidth(tabWidth);
        tabPane.setMaxHeight(tabWidth + 10);
        tabPane.setCenter(imageView);
        tabPane.setBottom(label);

        tab.setText("");
        tab.setGraphic(tabPane);

        tab.setOnSelectionChanged(onSelectionChangedEvent);
    }

    public void updateTextArea(String s) {
        textArea.appendText(s + "\n");
    }

    public void readDataTabOnAction() {
        final ToggleGroup group = new ToggleGroup();
        radioButton1.setToggleGroup(group);
        radioButton2.setToggleGroup(group);
        radioButton1.setDisable(true);
        radioButton2.setDisable(true);

        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (radioButton1.isSelected()) {
                readDataButton.setDisable(false);
            } else {
                readDataButton.setDisable(true);
            }

            if (radioButton2.isSelected()) {
                readGraphDataButton.setDisable(false);
                priorityButton.setDisable(false);
                lastCheckButton.setDisable(false);
            } else {
                readGraphDataButton.setDisable(true);
                priorityButton.setDisable(true);
                lastCheckButton.setDisable(true);
            }
        });

        chooseDataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialDirectory(new File(dataLocation));
                FileChooser.ExtensionFilter extFilter =
                        new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
                fileChooser.getExtensionFilters().add(extFilter);
                try {
                    File file = fileChooser.showOpenDialog(chooseDataButton.getScene().getWindow());
                    dataFilePath = file.getAbsolutePath();
                    dataLocation = file.getParent();
                } catch (NullPointerException e) {
                    updateTextArea(NoFileSelected.toString());
                }

                if (dataFilePath != null) {
                    dataPath.setText(dataFilePath);
                    try {
                        readTypicalData.eraseExistingGraphData(dataFilePath);
                        Desktop.getDesktop().open(new File(dataFilePath));
                    } catch (IOException e) {
                        updateTextArea(FileNotFound.toString());
                    }
                    radioButton1.setDisable(false);
                    radioButton2.setDisable(false);
                }
            }
        });

        Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Void call() throws InterruptedException {
                        switch (whatwanado) {
                            case 1: {
                                updateTextArea(readTypicalData.main(dataFilePath));
                                break;
                            }
                            case 2: {
                                if (isFileClose(dataFilePath)) {
                                    updateTextArea(readTypicalData.manageGraphData(dataFilePath));
                                } else {
                                    updateTextArea(FileIsOpen.toString());
                                }
                                break;
                            }
                        }
                        System.gc();
                        return null;
                    }
                };
            }
        };

        process.setOnFailed(e -> {
            readDataSpinner.setVisible(false);
            if (whatwanado == 1)
                formationButton.setDisable(true);
        });

        process.setOnSucceeded(e -> {
            readDataSpinner.setVisible(false);
            if (whatwanado == 1)
                formationButton.setDisable(false);
        });

        process.setOnRunning(eve -> {
            readDataSpinner.setVisible(true);
        });

        readDataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                whatwanado = 1;
                if (!process.isRunning()) {
                    process.reset();
                    process.start();
                }
            }
        });

        readGraphDataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                whatwanado = 2;
                if (!process.isRunning()) {
                    process.reset();
                    process.start();
                }
            }
        });

        lastCheckButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                whatwanado = 1;
                if (!process.isRunning()) {
                    process.reset();
                    process.start();
                }
            }
        });

    }

    public void formationTabOnAction() {
        //text fields only get number
        Set<Node> nodes = gridPane1.lookupAll(".text-field");
        for (Node node : nodes) {
            ((TextField) node).textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue) {
                    if (!newValue.matches("\\d*")) {
                        ((TextField) node).setText(newValue.replaceAll("[^\\d]", ""));
                    }
                }
            });
        }

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(
                        Duration.seconds(formation.solveTime), new KeyValue(progressBar.progressProperty(), 1)));

        timeline.setCycleCount(1);


        formationOnAction:
        {
            Service formationProcess = new Service() {
                @Override
                protected Task createTask() {
                    return new Task() {
                        @Override
                        protected Void call() throws InterruptedException {
                            switch (whatwanado) {
                                case 0: {
                                    trainMaker.main(textField7.getText());
//                                    trainMaker.getOutput(dataFilePath);
                                    updateTextArea(formation.main(
                                            textField1.getText(),
                                            textField2.getText(),
                                            textField3.getText(),
                                            textField4.getText(),
                                            textField5.getText(),
                                            textField6.getText(),
                                            textField8.getText()
                                    ));
                                    System.gc();
                                    break;
                                }
                                case 1: {
                                    if (isFileClose(dataFilePath)) {
                                        trainMaker.getOutput(dataFilePath);
                                        System.gc();
                                    } else {
                                        updateTextArea(FileIsOpen.toString());
                                    }

                                    if (isFileClose(formationFilePath))
                                        updateTextArea(formation.getOutput(formationFilePath));
                                    else
                                        updateTextArea(FileIsOpen.toString());
                                    break;
                                }
                                case 2: {
                                    try {
                                        Desktop.getDesktop().open(new File(formationFilePath));
                                    } catch (IOException e) {
                                        updateTextArea(FileNotFound.toString());
                                    }
                                    break;
                                }
                            }
                            return null;
                        }
                    };
                }
            };

            formationProcess.setOnFailed(e -> {
                formationSpinner.setVisible(false);
                if (whatwanado == 0) {
                    timeline.stop();
                    progressBar.setVisible(false);
                    minimizeLocoButton.setDisable(true);
                }
            });

            formationProcess.setOnSucceeded(e -> {
                formationSpinner.setVisible(false);
                if (whatwanado == 0) {
                    timeline.stop();
                    progressBar.setVisible(false);
                    minimizeLocoButton.setDisable(false);
                }
            });

            formationProcess.setOnRunning(eve -> {
                formationSpinner.setVisible(true);
                if (whatwanado == 0) {
                    progressBar.setVisible(true);
                    timeline.play();
                }
            });

            formationButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    whatwanado = 0;
                    if (!formationProcess.isRunning()) {
                        formationProcess.reset();
                        formationProcess.start();
                    }
                }
            });

            formationOutputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(dataLocation));
                    FileChooser.ExtensionFilter extFilter =
                            new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
                    fileChooser.getExtensionFilters().add(extFilter);
                    try {
                        File file = fileChooser.showSaveDialog(formationOutputButton.getScene().getWindow());
                        formationFilePath = file.getAbsolutePath();
                        dataLocation = file.getParent();
                        whatwanado = 1;
                        if (!formationProcess.isRunning()) {
                            formationProcess.reset();
                            formationProcess.start();
                        }
                    } catch (NullPointerException e) {
                        updateTextArea(NoFileSelected.toString());
                    }
                }
            });

            showFormationExcelButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    whatwanado = 2;
                    if (!formationProcess.isRunning()) {
                        formationProcess.reset();
                        formationProcess.start();
                    }
                }
            });
        }
    }

    public void minimizeLocoTabOnAction() {
        //text fields only get number
        Set<Node> nodes = gridPane2.lookupAll(".text-field");
        for (Node node : nodes) {
            ((TextField) node).textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue) {
                    if (!newValue.matches("\\d*")) {
                        ((TextField) node).setText(newValue.replaceAll("[^\\d]", ""));
                    }
                }
            });
        }


        locoOnAction:
        {
            Service locoProcess = new Service() {
                @Override
                protected Task createTask() {
                    return new Task() {
                        @Override
                        protected Void call() throws InterruptedException {
                            switch (whatwanado) {
                                case 0: {
                                    updateTextArea(minimizeLoco.main(
                                            textField9.getText(),
                                            textField10.getText()
                                    ));
                                    break;
                                }
                                case 1: {
                                    if (isFileClose(formationFilePath))
                                        updateTextArea(minimizeLoco.getOutput(locoFilePath));
                                    else
                                        updateTextArea(FileIsOpen.toString());
                                    break;
                                }
                                case 2: {
                                    try {
                                        Desktop.getDesktop().open(new File(locoFilePath));
                                    } catch (IOException e) {
                                        updateTextArea(FileNotFound.toString());
                                    }
                                    break;
                                }

                            }
                            return null;
                        }
                    };
                }
            };

            locoProcess.setOnFailed(e -> {
                locoSpinner.setVisible(false);
            });

            locoProcess.setOnSucceeded(e -> {
                locoSpinner.setVisible(false);
                System.gc();
            });

            locoProcess.setOnRunning(eve -> {
                locoSpinner.setVisible(true);
            });

            minimizeLocoButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    whatwanado = 0;
                    if (!locoProcess.isRunning()) {
                        locoProcess.reset();
                        locoProcess.start();
                    }
                }
            });

            minimizeLocoOutputButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(dataLocation));
                    FileChooser.ExtensionFilter extFilter =
                            new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
                    fileChooser.getExtensionFilters().add(extFilter);
                    try {
                        File file = fileChooser.showSaveDialog(minimizeLocoOutputButton.getScene().getWindow());
                        locoFilePath = file.getAbsolutePath();
                        dataLocation = file.getParent();

                        whatwanado = 1;
                        if (!locoProcess.isRunning()) {
                            locoProcess.reset();
                            locoProcess.start();
                        }
                    } catch (NullPointerException e) {
                        updateTextArea(NoFileSelected.toString());
                    }
                }
            });

            showLocoExcelButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    whatwanado = 2;
                    if (!locoProcess.isRunning()) {
                        locoProcess.reset();
                        locoProcess.start();
                    }
                }
            });
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {


        String title = "برای هر کدام از بارهای زیر اولویت سیر تعیین کنید:";

        JFXDialogLayout dialogContent = new JFXDialogLayout();

        dialogContent.setHeading(new Text(title));
        JFXTreeTableView tree = Table(rawInputs);
        dialogContent.setBody(tree);
        dialogContent.setMinWidth(750);
        dialogContent.setMaxHeight(450);

        dialogContent.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        JFXButton close = new JFXButton("بستن");
        JFXButton okay = new JFXButton("ثبت");

        HBox hBox = new HBox(close, okay);
        hBox.setSpacing(20);
        close.setButtonType(JFXButton.ButtonType.RAISED);
        okay.setButtonType(JFXButton.ButtonType.RAISED);

        dialogContent.setActions(hBox);

        JFXDialog dialog = new JFXDialog(test, dialogContent, JFXDialog.DialogTransition.CENTER);

        Service process = new Service() {
            @Override
            protected Task createTask() {
                return new Task() {
                    @Override
                    protected Void call() throws InterruptedException {
                        try {
                            if (isFileClose(dataFilePath)) {
                                readTypicalData.afterPriorityTree(dataFilePath, tree);
                                Desktop.getDesktop().open(new File(dataFilePath));
                            } else {
                                updateTextArea(FileIsOpen.toString());
                            }
                        } catch (IOException e) {
                            updateTextArea(FileNotFound.toString());
                        }
                        System.gc();
                        return null;
                    }
                };
            }
        };

        close.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent __) {
                dialog.close();
            }
        });

        okay.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent __) {
                if (!process.isRunning()) {
                    process.reset();
                    process.start();
                }
            }
        });

        dialog.show();
    }

    public boolean isFileClose(String fileName) {
        //  TO CHECK WHETHER A FILE IS OPENED
        //  OR NOT (not for .txt files)

        File file = new File(fileName);

        // try to rename the file with the same name
        File sameFileName = new File(fileName);

        if (file.renameTo(sameFileName)) {
            // if the file is renamed
            return true;
        } else {
            // if the file didnt accept the renaming operation
            return false;
        }

    }
}