package company;

import company.Backend.Formation;
import company.Backend.Initializer;
import ilog.concert.IloException;

import java.sql.SQLException;

import static company.sql.runQueries;

public class windows {

    public static String dataLocation = "";
    public static void main(String[] args) throws SQLException, IloException {

//        launch(args);//Run UI: need to be updated
        runQueries();
//        cycleTime();
        Initializer Initializer =new Initializer();
        Initializer.prepareData();
        Initializer.setPriority();
        Formation formation = new Formation();
        formation.model();
        System.exit(0);
    }
}