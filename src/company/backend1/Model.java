package company.backend1;

import ilog.concert.IloException;

import java.io.IOException;

/**
 * Created by Monemi_M on 10/08/2017.
 */
public interface Model {
    void  main() throws IloException, IOException;

    void buildModel() throws IloException, IOException;

    boolean solveModel() throws IloException;

    void readData() throws IOException;

    String getOutput() throws IOException, IloException;
}