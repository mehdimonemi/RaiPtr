package ir.rai;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.sql.SQLException;

public class testModel {

    public static void main(String[] args) throws SQLException, IloException {

        testModel test = new testModel();
        test.model();
        System.exit(0);

    }

    IloCplex model;
    IloNumVar[][][][] l;
    IloNumVar[] y;
    int trip = 5;
    int tripKind = 2;// 0 for active, 1 for dead


    public void model() throws IloException {

        int[] nodes = {1, 2, 3, 4, 5, 6};
        int[][] blocks = {
                {1, 2, 1, 3},
                {1, 3, 2, 4},
                {1, 4, 1, 5},
                {2, 6, 0, 2},
                {3, 6, 2, 7},
                {4, 6, 2, 5},
                {2, 5, 1, 4},
                {5, 6, 1, 2},
                {2, 3, 1, 7},
                {3, 4, 1, 6},
                {2, 1, 0, 3},
                {3, 1, 0, 4},
                {4, 1, 0, 5},
                {6, 2, 1, 2},
                {6, 3, 0, 7},
                {6, 4, 0, 5},
                {5, 2, 1, 4},
                {6, 5, 0, 2},
                {3, 2, 0, 7},
                {4, 3, 0, 6},
        };
        int[][] dizels = {
                {1, 3},
                {2, 1},
                {3, 6},
                {4, 5},
        };

        model = new IloCplex();

        l = new IloNumVar[dizels.length][blocks.length][trip][2];
        y = new IloNumVar[blocks.length];

        //decision variables
        for (int i = 0; i < dizels.length; i++) {
            for (int j = 0; j < blocks.length; j++) {
                for (int m = 0; m < trip; m++) {
                    if (m == 0 && dizels[i][1] != blocks[j][0]) {
                        l[i][j][m][0] = model.numVar(0, 0, IloNumVarType.Int);
                        l[i][j][m][1] = model.numVar(0, 0, IloNumVarType.Int);
                    } else {
                        l[i][j][m][0] = model.numVar(0, 1, IloNumVarType.Int);
                        l[i][j][m][1] = model.numVar(0, 1, IloNumVarType.Int);
                    }
                }
            }
        }

        for (int i = 0; i < blocks.length; i++) {
            y[i] = model.numVar(0, 1, IloNumVarType.Int);
        }

        //Goal Function
        //cost of maneuver
        IloNumExpr goalFunction = model.constant(0);
        for (int i = 0; i < dizels.length; i++) {
            for (int j = 0; j < blocks.length; j++) {
                for (int k = 0; k < trip; k++) {
                    goalFunction = model.sum(goalFunction, model.prod(l[i][j][k][0], blocks[j][3]));
                    goalFunction = model.sum(goalFunction, model.prod(model.negative(l[i][j][k][1])
                            , 10 * blocks[j][3]));
                }
            }
        }

        for (int i = 0; i < blocks.length; i++) {
            goalFunction = model.sum(goalFunction, model.prod(100, y[i]));
        }
        model.addMaximize(goalFunction);

        IloNumExpr constraint1;
        IloNumExpr constraint2;

        //trips
        for (int i = 0; i < dizels.length; i++) {
            for (int j = 0; j < trip; j++) {
                constraint1 = model.constant(0);
                for (int k = 0; k < blocks.length; k++) {
                    constraint1 = model.sum(constraint1, l[i][k][j][0]);
                    constraint1 = model.sum(constraint1, l[i][k][j][1]);
                }
                model.addLe(constraint1, 1);
            }
        }

        //continuous trips
        for (int i = 0; i < dizels.length; i++) {
            for (int k = 0; k < nodes.length; k++) {
                for (int j = 0; j < trip - 1; j++) {
                    constraint1 = model.constant(0);//outgoing blocks
                    constraint2 = model.constant(0);//incoming blocks
                    for (int m = 0; m < blocks.length; m++) {
                        if (k + 1 == blocks[m][1]) {
                            constraint1 = model.sum(constraint1, l[i][m][j][0]);
                            constraint1 = model.sum(constraint1, l[i][m][j][1]);
                        }
                    }

                    for (int m = 0; m < blocks.length; m++) {
                        if (k + 1 == blocks[m][0]) {
                            constraint2 = model.sum(constraint2, l[i][m][j + 1][0]);
                            constraint2 = model.sum(constraint2, l[i][m][j + 1][1]);
                        }
                    }
                    model.addGe(constraint1, constraint2);
                }
            }
        }

        for (int i = 0; i < blocks.length; i++) {
            constraint1 = model.constant(0);
            for (int j = 0; j < dizels.length; j++) {
                for (int k = 0; k < trip; k++) {
                    constraint1 = model.sum(constraint1, l[j][i][k][0]);
                }
            }
            model.addEq(constraint1, model.prod(y[i], blocks[i][2]));
        }

        if (model.solve()) {
            for (int i = 0; i < dizels.length; i++) {
                System.out.println("loco " + (i + 1) + ": ");
                for (int k = 0; k < trip; k++) {
                    for (int j = 0; j < blocks.length; j++) {
                        if (model.getValue(l[i][j][k][0]) > 0.5) {
                            System.out.print("\t" + blocks[j][0] + "-" + blocks[j][1] + " -- active");
                        }
                        if (model.getValue(l[i][j][k][1]) > 0.5) {
                            System.out.print("\t" + blocks[j][0] + "-" + blocks[j][1] + " -- dead");
                        }
                    }
                }
                System.out.println();
            }
        }

    }
}
