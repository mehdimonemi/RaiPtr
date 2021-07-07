package ir.rai;

import ilog.concert.IloException;
import ir.rai.Backend.Formation;
import ir.rai.Backend.Initializer;

import java.sql.SQLException;

import static ir.rai.sql.runQueries;

public class windows {

    public static String dataLocation = "";

    public static void main(String[] args) throws SQLException, IloException {

//        launch(args);//Run UI: need to be updated
        runQueries();
//        cycleTime();
        Initializer Initializer = new Initializer();
        Initializer.prepareData();
        Initializer.setPriority();
        Formation formation = new Formation();
        formation.model();
        System.exit(0);
    }
}