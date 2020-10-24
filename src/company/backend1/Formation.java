package company.backend1;

import company.Data.*;
import company.Data.oldOnes.Block;
import ilog.concert.*;
import ilog.cplex.IloCplex;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static company.backend1.Massages.*;
import static company.backend1.ReadTypicalData.*;

/**
 * Created by Monemi_M on 10/08/2017.
 */
public class Formation extends ExcelSetValue {
    public static double trainOfficeWorkTime = 50;
    public static double trainOthersTime = 50;
    double locoManovrTime = 20;
    double oneTrainFormationTime = 0;
    public double solveTime = 180;

    public static int oneWagonManovrTime = 5;
    public static int maxLongNorthTrainNumber = 3;
    public static int maxLongSouthTrainNumber = 3;
    public static int minTrainLength = 300;
    public static int locoInHand = 30;
    int totalCommodity = 0;

    IloCplex model;
    IloNumVar[][] x;
    IloNumVar[][] z;
    //    IloNumVar[][][] h;
    IloNumVar[] y;
    IloNumVar[] ct;
    IloNumVar[] yStar;
    IloNumVar[] s;

    double[] weight;
    double[] length;

    public String main(String trainOfficeWorkTime, String trainOthersTime, String oneWagonManovrTime,
                       String maxLongSouthTrainNumber, String maxLongNorthTrainNumber,
                       String minTrainLength, String loco) {

        if (!trainOfficeWorkTime.equals("")) {
            this.trainOfficeWorkTime = Double.parseDouble(trainOfficeWorkTime);
        }

        if (!trainOthersTime.equals("")) {
            this.trainOthersTime = Double.parseDouble(trainOthersTime);
        }

        if (!oneWagonManovrTime.equals("")) {
            this.oneWagonManovrTime = Integer.parseInt(oneWagonManovrTime);
            this.locoManovrTime = Integer.parseInt(oneWagonManovrTime) * 3;
        }

        if (!maxLongSouthTrainNumber.equals("")) {
            this.maxLongSouthTrainNumber = Integer.parseInt(maxLongSouthTrainNumber);
        }

        if (!maxLongNorthTrainNumber.equals("")) {
            this.maxLongNorthTrainNumber = Integer.parseInt(maxLongNorthTrainNumber);
        }

        if (!minTrainLength.equals("")) {
            this.minTrainLength = Integer.parseInt(minTrainLength);
        }

        if (!loco.equals("")) {
            this.locoInHand = Integer.parseInt(loco);
        }

        this.oneTrainFormationTime = this.trainOfficeWorkTime + this.trainOthersTime;

        totalCommodity = 0;
        for (Commodity commodity : commodities) {
            totalCommodity += commodity.getVolume();
        }

        String result;
        try {
            if (model != null) {
                model.clearModel();
            }
            result = buildModel();
//            model.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.001);
            model.setParam(IloCplex.Param.TimeLimit, solveTime);
            model.setParam(IloCplex.Param.MIP.Strategy.File, 2);
            model.setParam(IloCplex.Param.WorkMem, 128);
//            model.setParam(IloCplex.Param.MIP.Limits.TreeMemory, 128);
            model.setParam(IloCplex.Param.WorkDir, "Data/cplexdata");
//            model.setParam(IloCplex.Param.MIP.Strategy.RINSHeur, 3);
//            model.setParam(IloCplex.Param.Emphasis.MIP, 2);
//            model.setParam(IloCplex.Param.MIP.Cuts.Gomory, 2);
//               model.setOut(null);

            result = solveModel();
            return result;
        } catch (IloException e) {
            return CplexException.toString();
        } catch (OutOfMemoryError e) {
            return OutOfMemory.toString();
        }
    }

    public String buildModel() {
        try {
            model = new IloCplex();

            //define decision variables
            x = new IloNumVar[commodities.size()][trains.size()];
            z = new IloNumVar[commodities.size()][trains.size()];
            for (int i = 0; i < commodities.size(); i++) {
                for (int j = 0; j < commodities.get(i).getTrains().size(); j++) {
                    x[i][commodities.get(i).getTrains().get(j).getId()] =
                            model.numVar(0, commodities.get(i).getVolume(), IloNumVarType.Int);
                    z[i][commodities.get(i).getTrains().get(j).getId()] =
                            model.numVar(0, 1, IloNumVarType.Int);
                }
            }

            y = new IloNumVar[trains.size()];
            yStar = new IloNumVar[trains.size()];
            ct = new IloNumVar[trains.size()];
            for (int i = 0; i < trains.size(); i++) {
                y[i] = model.numVar(0, 1, IloNumVarType.Int);
                ct[i] = model.numVar(0, 10, IloNumVarType.Int);
                yStar[i] = model.numVar(0, 1, IloNumVarType.Int);
            }

            s = new IloNumVar[commodities.size()];
            for (int i = 0; i < commodities.size(); i++) {
                s[i] = model.numVar(0, commodities.get(i).getVolume(), IloNumVarType.Int);
            }

//            h = new IloNumVar[commodities.size()][commodities.size()][trains.size()];
//            for (int i = 0; i < commodities.size(); i++) {
//                for (int j = 0; j < commodities.size(); j++) {
//                    if (i == j) {
//                        continue;
//                    } else if (areInHarmoni(commodities.get(i), commodities.get(j))) {
//                        for (int k = 0; k < trains.size(); k++) {
//                            if (commodities.get(i).hasTrain(trains.get(k).getId()) &&
//                                    commodities.get(j).hasTrain(trains.get(k).getId()))
//                                h[i][j][k] = model.numVar(0, 1, IloNumVarType.Int);
//                        }
//                    }
//                }
//            }

            //zaribe har part tabe hadaf
            //bar
            int p1 = 2;
            //manouvr
            int p2 = 1;
            //ghatar
            double p3 = 2;
            //yek dasti
            int p4 = 5;
            //vazne ghatar
            int p5 = 1;
            //harmoni wagon ha
            int p6 = 500;
            //end define decision variables


            //objective function
            IloNumExpr goalFunction = model.constant(0);

            //part bar
            double coefficient1 = 0;
            for (int i = 0; i < commodities.size(); i++) {
                coefficient1 += (commodities.get(i).getVolume() * commodities.get(i).getPriority());
            }

            for (int i = 0; i < commodities.size(); i++) {
                goalFunction = model.sum(goalFunction, model.prod(s[i],
                        (p1 * commodities.get(i).getPriority()) / coefficient1));
            }

            //part manouvr
            double coefficient5 = 0;
            for (int i = 0; i < commodities.size(); i++) {
                for (int j = 0; j < trains.size(); j++) {
                    if (commodities.get(i).hasTrain(trains.get(j).getId())) {
                        coefficient5 += oneWagonManovrTime;
                    }
                }
            }

            for (int i = 0; i < commodities.size(); i++) {
                for (int j = 0; j < trains.size(); j++) {
                    if (commodities.get(i).hasTrain(trains.get(j).getId())) {
                        goalFunction = model.sum(goalFunction, model.prod(x[i][j], p2 * (-oneWagonManovrTime / coefficient5)));
                    }
                }
            }

            //part ghatar
            double coefficient2 = 0;
            for (int i = 0; i < trains.size(); i++) {
                switch (trains.get(i).getMaxTrainWeight()) {
                    case 1000:
                        coefficient2 += oneTrainFormationTime + oneWagonManovrTime;

                        break;
                    case 2000:
                        coefficient2 += oneTrainFormationTime + (2 * oneWagonManovrTime);

                        break;
                    case 3000:
                        coefficient2 += oneTrainFormationTime + (3 * oneWagonManovrTime);
                        break;
                }
            }

            for (int i = 0; i < trains.size(); i++) {
                switch (trains.get(i).getMaxTrainWeight()) {
                    case 1000:
                        goalFunction = model.sum(goalFunction, (model.prod(model.negative(y[i]),
                                p3 * ((oneTrainFormationTime + oneWagonManovrTime) / coefficient2))));
                        break;
                    case 2000:
                        goalFunction = model.sum(goalFunction, ((model.prod(model.negative(y[i]),
                                p3 * (oneTrainFormationTime + (2 * oneWagonManovrTime)) / coefficient2))));
                        break;
                    case 3000:
                        goalFunction = model.sum(goalFunction, ((model.prod(model.negative(y[i]),
                                p3 * (oneTrainFormationTime + (3 * oneWagonManovrTime)) / coefficient2))));
                        break;
                }
            }


            //part yek dasti
            double coefficient6 = 0;
            for (int i = 0; i < trains.size(); i++) {
                coefficient6 += 10;
            }

            for (int i = 0; i < trains.size(); i++) {
                goalFunction = model.sum(goalFunction, model.prod(model.negative(ct[i]), p4 / coefficient6));

            }

            //part harmoni wagon ha
            double coefficient3 = 0;

            for (int i = 0; i < commodities.size(); i++) {
                for (int j = 0; j < commodities.size(); j++) {
                    if (i == j) {
                        continue;
                    } else {
                        for (int k = 0; k < trains.size(); k++) {
                            if (commodities.get(i).hasTrain(trains.get(k).getId()) &&
                                    commodities.get(j).hasTrain(trains.get(k).getId()))
                                coefficient3 += 1;
                        }
                    }
                }
            }

//            for (int i = 0; i < commodities.size(); i++) {
//                for (int j = 0; j < commodities.size(); j++) {
//                    if (i == j) {
//                        continue;
//                    } else if (areInHarmoni(commodities.get(i), commodities.get(j))) {
//                        for (int k = 0; k < trains.size(); k++) {
//                            if (commodities.get(i).hasTrain(trains.get(k).getId()) &&
//                                    commodities.get(j).hasTrain(trains.get(k).getId()))
//                                goalFunction = model.sum(goalFunction, model.prod(h[i][j][k], p6 / coefficient3));
//                        }
//                    }
//                }
//            }
            //end objective function

            //constraint 1
            IloNumExpr constraint;
            for (int i = 0; i < commodities.size(); i++) {

                int stationA = commodities.get(i).getOriginId();
                int stationB = commodities.get(i).getDestinationId();

                for (int j = 0; j < manovrStations.size(); j++) {
                    constraint = model.constant(0);

                    if (manovrStations.get(j).getId() == stationA) {

                        for (Train train : commodities.get(i).getTrains()) {
                            if (stationA == train.getOriginId()) {
                                constraint = model.sum(constraint, x[i][train.getId()]);
                            }
                            if (stationA == train.getDestinationId()) {
                                constraint = model.sum(constraint, model.negative(x[i][train.getId()]));
                            }
                        }
                        model.addEq(constraint, s[i]);

                    } else if (manovrStations.get(j).getId() == stationB) {

                        for (Train train : commodities.get(i).getTrains()) {
                            if (stationB == train.getOriginId()) {
                                constraint = model.sum(constraint, x[i][train.getId()]);

                            }
                            if (stationB == train.getDestinationId()) {
                                constraint = model.sum(constraint, model.negative(x[i][train.getId()]));

                            }
                        }
                        model.addEq(constraint, model.negative(s[i]));

                    } else {
                        for (Train train : commodities.get(i).getTrains()) {
                            if (manovrStations.get(j).getId() == (train.getOriginId())) {
                                constraint = model.sum(constraint, x[i][train.getId()]);

                            }
                            if (manovrStations.get(j).getId() == (train.getDestinationId())) {
                                constraint = model.sum(constraint, model.negative(x[i][train.getId()]));

                            }
                        }
                        model.addEq(constraint, 0);
                    }
                }
            }//end constraint 1

            double coefficient4 = 0;
            for (int i = 0; i < trains.size(); i++) {
                coefficient4 += trains.get(i).getMaxTrainWeight();
            }
            //constraint 2
            for (int i = 0; i < trains.size(); i++) {
                constraint = model.constant(0);
                for (int j = 0; j < commodities.size(); j++) {
                    if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                        constraint = model.sum(constraint, model.prod(x[j][i],
                                (commodities.get(j).getWagon().getWeight() * (1 - commodities.get(j).getKind())
                                        + commodities.get(j).getWagon().getLoadWeight() *
                                        (commodities.get(j).getKind()))));
                    }
                }
                model.addLe(constraint, model.prod(trains.get(i).getMaxTrainWeight(), y[i]));
                model.addGe(constraint, model.prod(trains.get(i).getMaxTrainWeight() - 400, y[i]));

                //part vazne ghatar
                goalFunction = model.sum(goalFunction, model.prod(model.negative(model.sum(model.prod(trains.get(i).getMaxTrainWeight(), y[i]),
                        model.negative(constraint))), p5 / coefficient4));
            }

            //constraint 3
            IloNumExpr constraint1;
            for (int i = 0; i < trains.size(); i++) {
                constraint = model.constant(0);
                constraint1 = model.constant(0);
                for (int j = 0; j < commodities.size(); j++) {
                    if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                        constraint = model.sum(constraint, model.prod(x[j][i],
                                commodities.get(j).getWagon().getLength()));
                    }
                }

                model.addGe(constraint, model.sum(model.prod(minTrainLength, y[i]),
                        model.prod(yStar[i], (-minTrainLength + 415))));

                constraint1 = model.sum(model.prod(trains.get(i).getMaxTrainLength(), y[i]),
                        model.prod(yStar[i], (-trains.get(i).getMaxTrainLength() + trains.get(i).getLongTrainLength())));
                model.addLe(constraint, constraint1);
                model.addGe(y[i], yStar[i]);
            }

            //constraint 4
            constraint = model.constant(0);
            constraint1 = model.constant(0);
            for (int i = 0; i < trains.size(); i++) {
                if (trains.get(i).getOriginId() < trains.get(i).getDestinationId())
                    constraint = model.sum(constraint, yStar[i]);
                if (trains.get(i).getOriginId() > trains.get(i).getDestinationId())
                    constraint1 = model.sum(constraint1, yStar[i]);
            }
            model.addLe(constraint, maxLongSouthTrainNumber);
            model.addLe(constraint1, maxLongNorthTrainNumber);

            //constraint 5
            for (int j = 0; j < blocks.size() / 2; j++) {
                constraint = model.constant(0);
                for (int i = 0; i < trains.size(); i++) {
                    for (Block trainBlock : trains.get(i).getTrainBlocks()) {
                        if ((blocks.get(j).getOriginId() == trainBlock.getOriginId() &&
                                blocks.get(j).getDestinationId() == trainBlock.getDestinationId()) ||
                                (blocks.get(j).getOriginId() == trainBlock.getDestinationId() &&
                                        blocks.get(j).getDestinationId() == trainBlock.getOriginId())) {
                            constraint = model.sum(constraint, y[i]);
                        }
                    }
                }
                model.addLe(constraint, blocks.get(j).getCapacity());
            }

            //constraint 6
            for (int i = 0; i < trains.size(); i++) {
                for (int j = 0; j < commodities.size(); j++) {
                    if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                        model.addGe(z[j][i], model.prod(x[j][i], 0.0001));
                    }
                }
            }

            //constraint 7
            for (int i = 0; i < trains.size(); i++) {
                constraint = model.constant(0);
                for (int j = 0; j < commodities.size(); j++) {
                    if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                        constraint = model.sum(constraint, z[j][i]);
                    }
                }
                model.addEq(constraint, ct[i]);
            }

            //constraint 9
            for (int i = 0; i < commodities.size(); i++) {
                for (int j = 0; j < commodities.size(); j++) {
                    if (i == j) {
                        continue;
                    } else if (!areInHarmoni(commodities.get(i), commodities.get(j))) {
                        for (int k = 0; k < trains.size(); k++) {
                            if (commodities.get(i).hasTrain(trains.get(k).getId()) &&
                                    commodities.get(j).hasTrain(trains.get(k).getId()))
                                model.addLe(model.sum(z[i][k], z[j][k]), 1);
                        }
                    }
                }
            }

            //constraint 10
            constraint = model.constant(0);
            for (int i = 0; i < trains.size(); i++) {
                constraint = model.sum(constraint, model.prod(y[i], trains.get(i).getMaxTrainWeight() / 1000));
            }
            model.addLe(constraint, locoInHand);


            model.addMaximize(goalFunction);
            return null;
        } catch (IloException e) {
            return CplexException.toString();
        }
    }

    public String solveModel() {
        try {
            if (model.solve()) {
                for (int i = 0; i < blocks.size(); i++) {
                    double temp = 0;
                    for (int j = 0; j < trains.size(); j++) {
                        for (Block block : trains.get(j).getTrainBlocks()) {
                            if (((blocks.get(i).getOriginId() == block.getOriginId() &&
                                    blocks.get(i).getDestinationId() == block.getDestinationId()))) {
                                if (model.getValue(y[j]) > 0.5) {
                                    temp += 1;
                                }
                            }
                        }
                    }
                    System.out.println(blocks.get(i).getOrigin() + " -- " + blocks.get(i).getDestination() + " -- " + temp + " -- " + blocks.get(i).getCapacity());
                }

                weight = new double[trains.size()];
                length = new double[trains.size()];

                //weight, length and priority of each train
                double priority = 0;
                for (int i = 0; i < (trains.size()); i++) {
                    priority = 0;
                    for (int j = 0; j < commodities.size(); j++) {
                        if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                            if (model.getValue(x[j][i]) >= 0.5) {
                                priority += (model.getValue(x[j][i]) * commodities.get(j).getPriority());
                            }
                        }
                    }
                    trains.get(i).setPriority(priority);
                }

                //sort trains list by priority
                Collections.sort(trains, new Comparator<Train>() {
                    @Override
                    public int compare(Train lhs, Train rhs) {
                        // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                        return lhs.getPriority() < rhs.getPriority() ? 1 : (lhs.getPriority() > rhs.getPriority()) ? -1 : 0;
                    }
                });

                int p = 1;
                for (int i = 0; i < trains.size(); i++) {
//                    trains.get(i).setId(i);
                    if (trains.get(i).getPriority() > 0) {
                        trains.get(i).setPriority(p);
                        p++;
                    }
                }

                for (int i = 0; i < (trains.size()); i++) {
                    weight[i] = 0;
                    length[i] = 0;
                    for (int j = 0; j < commodities.size(); j++) {
                        if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                            if (model.getValue(x[j][trains.get(i).getId()]) >= 0.5) {
                                weight[i] += (model.getValue(x[j][trains.get(i).getId()]) *
                                        (commodities.get(j).getWagon().getWeight() * (1 - commodities.get(j).getKind())
                                                + commodities.get(j).getWagon().getLoadWeight() * (commodities.get(j).getKind())));
                                length[i] += (model.getValue(x[j][trains.get(i).getId()]) * commodities.get(j).getWagon().getLength());
                            }
                        }
                    }
                    if (length[i] > 0 && i != 750) {
                        length[i] += ((trains.get(i).getMaxTrainWeight() / 1000) * 20);
                    }
                }

                trainArcs.clear();
                int trainArcsCounter = 0;
                for (int i = 0; i < trains.size(); i++) {
                    if (model.getValue(y[trains.get(i).getId()]) >= 0.5) {
                        int a;
                        if ((int) (weight[i] % 1000) != 0)
                            a = (1000 - (int) (weight[i] % 1000)) + (int) weight[i];
                        else
                            a = (int) weight[i];
                        trainArcs.add(new TrainArc(trainArcsCounter,
                                trains.get(i).getOrigin(),
                                trains.get(i).getDestination(),
                                trains.get(i).getId(),
                                (int) weight[i],
                                a,
                                trains.get(i).getTime()
                        ));
                        trainArcsCounter++;
                    }
                }


                return FormationSuccess.toString();
            } else {
                return FormationUnSuccess.toString();
            }
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (IloException e) {
            return CplexException.toString();
        }
    }

    public String getOutput(String formationFilePath) {


        FileOutputStream outputFile = null;
        XSSFWorkbook workbook = null;
        try {
            outputFile = new FileOutputStream(new File(formationFilePath));
            workbook = new XSSFWorkbook();


            //overview sheet
            XSSFSheet sheet = workbook.createSheet("خلاصه");
            sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);

            XSSFCellStyle style = setStyle(workbook, "B Zar");

            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            XSSFRow row = sheet.createRow(0);
            setCell(sheet, row, 0, "تعداد قطارها", style, headingColor);

            int t = 0;
            for (int i = 0; i < trains.size(); i++) {
                t += model.getValue(y[i]);

            }
            setCell(sheet, row, 1, (double) t, style, headingColor);


            row = sheet.createRow(1);
            setCell(sheet, row, 0, "کل تقاضای برآورده شده", style, headingColor);

            double z = 0;
            for (int i = 0; i < commodities.size(); i++) {
                for (Train train : commodities.get(i).getTrains()) {
                    if (commodities.get(i).getOriginId() == train.getOriginId()) {
                        if (model.getValue(x[i][train.getId()]) > 0.5) {
                            z += model.getValue(x[i][train.getId()]);
                        }

                    }

                }
            }
            setCell(sheet, row, 1, z, style, headingColor);

            row = sheet.createRow(2);
            setCell(sheet, row, 0, "کل بارهای روز", style, headingColor);
            setCell(sheet, row, 1, (double) totalCommodity, style, headingColor);

            //south and north freight
            double south = 0;
            double north = 0;
            for (Commodity commodity : commodities) {
                if (commodity.getOriginId() < commodity.getDestinationId())
                    south += commodity.getVolume();
                else
                    north += commodity.getVolume();
            }

            row = sheet.createRow(3);
            setCell(sheet, row, 0, "کل بارهای شمالی", style, headingColor);
            setCell(sheet, row, 1, north, style, headingColor);

            row = sheet.createRow(4);
            setCell(sheet, row, 0, "کل بارهای جنوبی", style, headingColor);
            setCell(sheet, row, 1, south, style, headingColor);

            //commodities sheets
            sheet = workbook.createSheet("بارها");
            sheet.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);


            int[] totalWagonOnTrains = new int[trains.size()];
            //calculate total wagons of each commodities on each train
            for (int i = 0; i < trains.size(); i++) {
                int totalWagonOnTrain = 0;
                for (int j = 0; j < commodities.size(); j++) {
                    if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                        if (model.getValue(x[j][trains.get(i).getId()]) >= 1) {
                            totalWagonOnTrain += model.getValue(x[j][trains.get(i).getId()]);
                        }
                    }
                    totalWagonOnTrains[i] = totalWagonOnTrain;
                }
            }

            //label first row
            row = sheet.createRow(0);
            setCell(sheet, row, 0, "مبدا", style, headingColor);
            setCell(sheet, row, 1, "مقصد", style, headingColor);
            setCell(sheet, row, 2, "بار", style, headingColor);
            setCell(sheet, row, 3, "تعداد واگن", style, headingColor);
            setCell(sheet, row, 4, "قطارهایی که هر بار را می برند", style, headingColor);
            setCell(sheet, row, 5, "", style, headingColor);
            setCell(sheet, row, 6, "", style, headingColor);
            setCell(sheet, row, 7, "", style, headingColor);
            setCell(sheet, row, 8, "", style, headingColor);

            row = sheet.createRow(1);
            setCell(sheet, row, 0, "", style, headingColor);
            setCell(sheet, row, 1, "", style, headingColor);
            setCell(sheet, row, 2, "", style, headingColor);
            setCell(sheet, row, 3, "", style, headingColor);
            setCell(sheet, row, 4, "کد قطار\n(موجود در صفحه قطارها)", style, headingColor);
            setCell(sheet, row, 5, "مبدا قطار", style, headingColor);
            setCell(sheet, row, 6, "مقصد قطار", style, headingColor);
            setCell(sheet, row, 7, "تعداد واگن این بار\nدر این قطار", style, headingColor);
            setCell(sheet, row, 8, "نوع قطار", style, headingColor);

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 8));


            int rowCounter = 2;

            XSSFColor bodyColor;
            Random random = new Random();
            for (int w = 0; w < Stations.size(); w++) {
                XSSFCellStyle style1 = setStyle(workbook, "B Zar");
                //choose color
                Color color = new Color(
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);

                bodyColor = new XSSFColor(color);
                for (int i = 0; i < commodities.size(); i++) {
                    if (commodities.get(i).getOrigin().equals(Stations.get(w).getName())) {
                        boolean flag = true;
                        int temp = rowCounter;
                        for (Train train : commodities.get(i).getTrains()) {

                            if (model.getValue(x[i][train.getId()]) >= 0.5) {
                                row = sheet.createRow(rowCounter);
                                if (flag) {
                                    setCell(sheet, row, 0, commodities.get(i).getOrigin(), style1, bodyColor);
                                    setCell(sheet, row, 1, commodities.get(i).getMainDestination(), style1, bodyColor);
                                    setCell(sheet, row, 2, commodities.get(i).getFreight(), style1, bodyColor);
                                    setCell(sheet, row, 3, (double) commodities.get(i).getVolume(), style1, bodyColor);
                                    flag = false;
                                } else {
                                    setCell(sheet, row, 0, "", style1, bodyColor);
                                    setCell(sheet, row, 1, "", style1, bodyColor);
                                    setCell(sheet, row, 2, "", style1, bodyColor);
                                    setCell(sheet, row, 3, "", style1, bodyColor);
                                }
                                setCell(sheet, row, 4, (double) train.getId(), style1, bodyColor);
                                setCell(sheet, row, 5, train.getOrigin(), style1, bodyColor);
                                setCell(sheet, row, 6, train.getDestination(), style1, bodyColor);
                                setCell(sheet, row, 7, model.getValue(x[i][train.getId()]), style1, bodyColor);
                                setCell(sheet, row, 8,
                                        (model.getValue(yStar[train.getId()]) == 0 ? "عادی" : "متراژی"), style1, bodyColor);
                                rowCounter++;
                            }
                        }
                        if (rowCounter > temp && (rowCounter - 1) != temp) {
                            sheet.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 0, 0));
                            sheet.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 1, 1));
                            sheet.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 2, 2));
                            sheet.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 3, 3));
                        }
                    }
                }
            }


            //trains sheet
            XSSFSheet sheet1 = workbook.createSheet("قطارها");

            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);


            //label first row
            row = sheet1.createRow(0);
            setCell(sheet1, row, 0, "کد قطار", style, headingColor);
            setCell(sheet1, row, 1, "اولویت سیر", style, headingColor);
            setCell(sheet1, row, 2, "مبدا قطار", style, headingColor);
            setCell(sheet1, row, 3, "مقصد قطار", style, headingColor);
            setCell(sheet1, row, 4, "کل واگن های روی قطار", style, headingColor);
            setCell(sheet1, row, 5, "نوع قطار", style, headingColor);
            setCell(sheet1, row, 6, "طول", style, headingColor);
            setCell(sheet1, row, 7, "وزن ناخالص", style, headingColor);
            setCell(sheet1, row, 8, "بارهایی که هر قطار می برد", style, headingColor);
            setCell(sheet1, row, 9, "", style, headingColor);
            setCell(sheet1, row, 10, "", style, headingColor);
            setCell(sheet1, row, 11, "", style, headingColor);
            setCell(sheet1, row, 12, "", style, headingColor);

            row = sheet1.createRow(1);
            setCell(sheet1, row, 0, "", style, headingColor);
            setCell(sheet1, row, 1, "", style, headingColor);
            setCell(sheet1, row, 2, "", style, headingColor);
            setCell(sheet1, row, 3, "", style, headingColor);
            setCell(sheet1, row, 4, "", style, headingColor);
            setCell(sheet1, row, 5, "", style, headingColor);
            setCell(sheet1, row, 6, "", style, headingColor);
            setCell(sheet1, row, 7, "", style, headingColor);
            setCell(sheet1, row, 8, "مبدا", style, headingColor);
            setCell(sheet1, row, 9, "مقصد", style, headingColor);
            setCell(sheet1, row, 10, "بار", style, headingColor);
            setCell(sheet1, row, 11, "واگن", style, headingColor);
            setCell(sheet1, row, 12, "تعداد واگن", style, headingColor);
            setCell(sheet1, row, 13, "تعداد واگن\nدر این قطار", style, headingColor);

            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 6, 6));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 7, 7));
            sheet1.addMergedRegion(new CellRangeAddress(0, 0, 8, 13));

            rowCounter = 2;
            for (Station station : Stations) {
                int priority = 1;
                XSSFCellStyle style1 = setStyle(workbook, "B Zar");
                //choose color
                Color color = new Color(
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);
                for (int i = 0; i < trains.size(); i++) {
                    if (trains.get(i).getOrigin().equals(station.getName())) {
                        bodyColor = new XSSFColor(color);
                        boolean firstRowIsWrite = false;
                        int temp = rowCounter;
                        for (int j = 0; j < commodities.size(); j++) {
                            if (commodities.get(j).hasTrain(trains.get(i).getId())) {
                                if (model.getValue(x[j][trains.get(i).getId()]) >= 0.5) {
                                    row = sheet1.createRow(rowCounter);

                                    if (!firstRowIsWrite) {
                                        setCell(sheet1, row, 0, (double) trains.get(i).getId(), style1, bodyColor);
                                        setCell(sheet1, row, 1, (double) priority, style1, bodyColor);
                                        setCell(sheet1, row, 2, trains.get(i).getOrigin(), style1, bodyColor);
                                        setCell(sheet1, row, 3, trains.get(i).getDestination(), style1, bodyColor);
                                        setCell(sheet1, row, 4, (double) totalWagonOnTrains[i], style1, bodyColor);
                                        setCell(sheet1, row, 5,
                                                (model.getValue(yStar[trains.get(i).getId()]) == 0 ? "عادی" : "متراژی"),
                                                style1, bodyColor);
                                        setCell(sheet1, row, 6, length[i], style1, bodyColor);
                                        setCell(sheet1, row, 7, weight[i], style1, bodyColor);
                                        firstRowIsWrite = true;
                                        priority++;
                                    } else {
                                        setCell(sheet1, row, 0, "", style1, bodyColor);
                                        setCell(sheet1, row, 1, "", style1, bodyColor);
                                        setCell(sheet1, row, 2, "", style1, bodyColor);
                                        setCell(sheet1, row, 3, "", style1, bodyColor);
                                        setCell(sheet1, row, 4, "", style1, bodyColor);
                                        setCell(sheet1, row, 5, "", style1, bodyColor);
                                        setCell(sheet1, row, 6, "", style1, bodyColor);
                                        setCell(sheet1, row, 7, "", style1, bodyColor);
                                    }

                                    setCell(sheet1, row, 8, commodities.get(j).getOrigin(), style1, bodyColor);
                                    setCell(sheet1, row, 9, commodities.get(j).getMainDestination(), style1, bodyColor);
                                    setCell(sheet1, row, 10, commodities.get(j).getFreight(), style1, bodyColor);
                                    setCell(sheet1, row, 11, commodities.get(j).getWagonName(), style1, bodyColor);
                                    setCell(sheet1, row, 12, (double) commodities.get(j).getVolume(), style1, bodyColor);
                                    setCell(sheet1, row, 13, model.getValue(x[j][trains.get(i).getId()]), style1, bodyColor);
                                    rowCounter++;
                                }
                            }

                        }
                        if (rowCounter > temp && (rowCounter - 1) != temp) {
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 0, 0));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 1, 1));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 2, 2));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 3, 3));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 4, 4));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 5, 5));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 6, 6));
                            sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 7, 7));
                        }
                    }
                }
            }


            return ExcelSave.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (IloCplex.UnknownObjectException e) {
            return UnknownCplexException.toString();
        } catch (FileNotFoundException e) {
            return NoFileSelected.toString();
        } catch (IloException e) {
            return CplexException.toString();
        } finally {
            try {
                workbook.write(outputFile);
                outputFile.flush();
                outputFile.close();
            } catch (IOException e) {
                return FileNotFound.toString();
            }
        }
    }

    public boolean areInHarmoni(Commodity commodity1, Commodity commodity2) {
        boolean result = true;
//        if (
//                commodity1.getOrigin().equals(commodity2.getOrigin()) &&
//                        commodity1.getDestination().equals(commodity2.getDestination()) &&
//                        commodity1.getWagon().equals(commodity2.getWagon())
//        ) {
//            result = true;
//        }
//
//        if (
//                commodity1.getOrigin().equals(commodity2.getOrigin()) &&
//                        commodity1.getDestination().equals(commodity2.getDestination()) &&
//                        (commodity1.getWagon().getName().equals("مسقف") || commodity1.getWagon().getName().equals("فله بر")) &&
//                        (commodity2.getWagon().getName().equals("مسقف") || commodity2.getWagon().getName().equals("فله بر"))
//
//        ) {
//            result = true;
//        }
//
//        if (
//                commodity1.getOrigin().equals(commodity2.getOrigin()) &&
//                        commodity1.getDestination().equals(commodity2.getDestination()) &&
//                        commodity1.getWagon().equals(commodity2.getWagon()) &&
//                        (commodity1.getFreight().equals(commodity2.getFreight()) ||
//                                (commodity1.getFreight().equals("سنگ آهن به صورت پودر") || commodity1.getFreight().equals("سنگ آهن")) &&
//                                        (commodity2.getFreight().equals("سنگ آهن به صورت پودر") || commodity2.getFreight().equals("سنگ آهن")))
//
//        ) {
//            result = true;
//        }
//
//        if (
//                commodity1.getOrigin().equals(commodity2.getOrigin()) &&
//                        commodity1.getDestination().equals(commodity2.getDestination()) &&
//                        commodity1.getVolume() <= 15 &&
//                        commodity2.getVolume() <= 15 &&
//                        !commodity1.getWagonName().equals("لبه بلند") &&
//                        !commodity2.getWagonName().equals("لبه بلند")
//
//        ) {
//            result = true;
//        }
        if (
                (((commodity1.getMainDestination().equals("ري") || commodity1.getDestination().equals("سمنگان (پالايشگاه)"))
                        && commodity1.getWagon().getName().equals("مخزندار")) &&
                        commodity2.getFreight().equals("گازهاي فشرده شده مايع طبق فصل ششم تعرفه"))
                        ||
                        (((commodity2.getMainDestination().equals("ري") || commodity2.getDestination().equals("سمنگان (پالايشگاه)"))
                                && commodity2.getWagon().getName().equals("مخزندار")) &&
                                commodity1.getFreight().equals("گازهاي فشرده شده مايع طبق فصل ششم تعرفه"))
                        ||
                        (commodity1.getFreight().equals("گازهاي فشرده شده مايع طبق فصل ششم تعرفه")
                                && commodity2.getFreight().equals("نفت كوره (مازوت)"))
                        ||
                        (commodity2.getFreight().equals("گازهاي فشرده شده مايع طبق فصل ششم تعرفه")
                                && commodity1.getFreight().equals("نفت كوره (مازوت)"))
        ) {
            result = false;
        }

//        if (
//                ((commodity1.getWagonName().equals("لبه بلند") && !commodity2.getWagonName().equals("لبه بلند"))
//                        ||
//                        (commodity2.getWagonName().equals("لبه بلند") && !commodity1.getWagonName().equals("لبه بلند")))
//                        ||
//                        ((commodity1.getWagonName().equals("فولاد اهواز") && !commodity2.getWagonName().equals("فولاد اهواز"))
//                                ||
//                                (commodity2.getWagonName().equals("فولاد اهواز") && !commodity1.getWagonName().equals("فولاد اهواز")))
//                        ||
//                        ((commodity1.getWagonName().equals("لبه بلند اکرايني") && !commodity2.getWagonName().equals("لبه بلند اکرايني"))
//                                ||
//                                (commodity2.getWagonName().equals("لبه بلند اکرايني") && !commodity1.getWagonName().equals("لبه بلند اکرايني")))
//        ) {
//            result = false;
//        }
//        if (
//                (commodity1.getWagonName().equals("مخزن کوره") &&
//                        commodity1.getFreight().equals("نفت كوره (مازوت)") && !commodity2.getWagonName().equals("مخزن کوره"))
//                        ||
//                        (commodity2.getWagonName().equals("مخزن کوره")
//                                && commodity2.getFreight().equals("نفت كوره (مازوت)") && !commodity1.getWagonName().equals("مخزن کوره"))
//        ) {
//            result = false;
//        }

        if (
                ((commodity1.getWagonName().equals("لبه بلند") ||
                        commodity1.getWagonName().equals("لبه بلند اکرايني") ||
                        commodity1.getWagonName().equals("فولاد اهواز"))
                        && (commodity2.getWagonName().equals("لبه بلند") ||
                        commodity1.getWagonName().equals("لبه بلند اکرايني") ||
                        commodity1.getWagonName().equals("فولاد اهواز")))
                        &&
                        !commodity1.getMainDestination().equals(commodity2.getMainDestination())

        ) {
            result = false;
        }

        return result;
    }
}

