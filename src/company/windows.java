package company;


import company.Backend2.formation;
import company.backend1.Massages;
import ilog.concert.IloException;
import javafx.application.Application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.*;
import java.sql.*;
import java.util.Optional;
import java.util.Scanner;

import static company.Backend2.cycleTime.cycleTime;
import static company.sql.runQueries;

public class windows extends Application {

    public static String dataLocation = "";


    @Override
    public void start(Stage primaryStage) {

        Parent root = null;
        Scanner fileToRead = null;
        try {
            fileToRead = new Scanner(new File("Data/dat"));
            for (String line; fileToRead.hasNextLine() && (line = fileToRead.nextLine()) != null; ) {
                dataLocation = line;
            }

            File file = new File(dataLocation);
            if (!file.exists() && !file.isDirectory())
                dataLocation = System.getProperty("user.home");


            root = FXMLLoader.load(getClass().getResource("/company/resource/sample.fxml"));
        } catch (IOException e) {
            System.out.println(Massages.FileNotFound);
        } finally {
            fileToRead.close();
        }

        primaryStage.setTitle("Rail Plan");
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setOnCloseRequest(this::confirmClose);
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/company/resource/images/Logo.png")));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void confirmClose(WindowEvent dialogEvent) {
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
        } else
            dialogEvent.consume();
    }

    public static void main(String[] args) throws SQLException, IloException {

        runQueries();
//        cycleTime();
        formation formation = new formation();
        formation.prepareData();
        formation.model();
        System.exit(0);

//        launch(args);
    }
}