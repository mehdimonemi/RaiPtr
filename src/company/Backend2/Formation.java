package company.Backend2;

import company.Data.*;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.awt.Color;
import java.io.*;
import java.util.*;

import static company.Backend2.Exell.*;
import static company.Data.newBlock.maxBlockId;
import static company.sql.*;

public class Formation {
    public static int minimumAllowedArc = 20;
    public static XSSFRow row;

    IloCplex model;
    IloNumVar[][] x;
    IloNumVar[] s;
    IloNumVar[][][] l;
    IloNumVar[][][] lAlone;
    IloNumVar[] y;

    int locoTrip = 3;

    //Model parameters:
    int maneuverCost = 2;
    int useLoco;//or based on assignment train distance
    int penaltyLAlone = 100;
    int costTrain = 1000;
    int benefitTransportWagon = 1000;//or based priority

    public void model() throws IloException {
        System.out.println("----------------------Building model-------------------");

        model = new IloCplex();

        model.setParam(IloCplex.Param.TimeLimit, 250);

        s = new IloNumVar[wagonListMap.size()];//whether wagon i transport
        x = new IloNumVar[wagonListMap.size()][trainArcs.size()];//whether wagon i transport by train j
        l = new IloNumVar[dizelListMap.size()][trainArcs.size()][locoTrip];//whether dizel l be on train j

        // we have about 400 block but max id is around 1000
        lAlone = new IloNumVar[dizelListMap.size()][maxBlockId + 1][locoTrip];
        y = new IloNumVar[trainArcs.size()];//whether train j be in final solution

        //auxiliary index sets
        wagonsKey = new ArrayList<>();
        wagonsKey.addAll(wagonListMap.keySet());

        stationsKey = new ArrayList<>();
        stationsKey.addAll(stationMap.keySet());

        dizelsKey = new ArrayList<>();
        dizelsKey.addAll(dizelListMap.keySet());

        //decision variables
        for (Long wagonKey : wagonsKey) {
            if (wagonListMap.get(wagonKey).getPriority() == 0) {
                s[wagonsKey.indexOf(wagonKey)] = model.numVar(0, 0, IloNumVarType.Int);
            } else {
                s[wagonsKey.indexOf(wagonKey)] = model.numVar(0, 1, IloNumVarType.Int);
            }
            for (Integer integer : wagonListMap.get(wagonKey).getTrainArcs()) {
                x[wagonsKey.indexOf(wagonKey)][integer] = model.numVar(0, 1, IloNumVarType.Int);
            }
        }

        for (Integer dizelKey : dizelsKey) {
            for (Integer integer : dizelListMap.get(dizelKey).getTrainArcs().keySet()) {
                for (int i = 0; i < locoTrip; i++) {
                    if (i == 0 && dizelListMap.get(dizelKey).getLastStation() != trainArcs.get(integer).getOrigin()) {
                        l[dizelsKey.indexOf(dizelKey)][integer][i] = model.numVar(0, 0, IloNumVarType.Int);
                    } else
                        l[dizelsKey.indexOf(dizelKey)][integer][i] = model.numVar(0, 1, IloNumVarType.Int);
                }
            }
        }

        for (Integer dizelKey : dizelsKey) {
            for (Integer blockId : dizelListMap.get(dizelKey).getAllowedBlock().keySet()) {
                for (int i = 0; i < locoTrip; i++) {
                    if (i == 0 && dizelListMap.get(dizelKey).getLastStation() != blockMap.get(blockId).getStartStationID())
                        lAlone[dizelsKey.indexOf(dizelKey)][blockId][i] = model.numVar(0, 0, IloNumVarType.Int);
                    else
                        lAlone[dizelsKey.indexOf(dizelKey)][blockId][i] = model.numVar(0, 1, IloNumVarType.Int);
                }
            }
        }

        for (int i = 0; i < trainArcs.size(); i++) {
            y[i] = model.numVar(0, 10, IloNumVarType.Int);
        }

        //Goal Function
        //cost of maneuver
        IloNumExpr goalFunction = model.constant(0);
        for (Long wagonKey : wagonsKey) {
            for (Integer trainArc : wagonListMap.get(wagonKey).getTrainArcs()) {
                goalFunction = model.sum(goalFunction, model.prod(x[wagonsKey.indexOf(wagonKey)][trainArc], -maneuverCost));
            }
        }

//        for (Integer dizelKey : dizelsKey) {
//            for (Integer trainArc : dizelListMap.get(dizelKey).getTrainArcs().keySet()) {
//                for (int i = 0; i < locoTrip; i++) {
//                    goalFunction = model.sum(goalFunction,
//                            model.prod(l[dizelsKey.indexOf(dizelKey)][trainArc][i],
//                                    -trainArcs.get(trainArc).getMaxWeight())
//                    );
//                }
//            }
//        }
        for (Integer dizelKey : dizelsKey) {
            for (Integer blockId : dizelListMap.get(dizelKey).getAllowedBlock().keySet()) {
                for (int i = 0; i < locoTrip; i++) {
                    goalFunction = model.sum(goalFunction, model.prod
                            (lAlone[dizelsKey.indexOf(dizelKey)][blockId][i], -penaltyLAlone));
                }
            }
        }

        //cost of train formation
        for (int i = 0; i < trainArcs.size(); i++) {
            goalFunction = model.sum(goalFunction, model.prod(y[i], -trainArcs.get(i).getMaxWeight()));
        }

        //transport maximum of wagons
        for (Long wagonKey : wagonsKey) {
            goalFunction = model.sum(goalFunction, model.prod(s[wagonsKey.indexOf(wagonKey)],
                    benefitTransportWagon * wagonListMap.get(wagonKey).getPriority()));
        }
        model.addMaximize(goalFunction);

        //constraints1: flow wagon
        IloNumExpr constraint1;
        IloNumExpr constraint2;
        for (Long wagonKey : wagonsKey) {
            int stationA = wagonListMap.get(wagonKey).getLastStation();
            int stationB = wagonListMap.get(wagonKey).getDestination();
            for (Integer stationKey : stationsKey) {
                constraint1 = model.constant(0);
                if (stationKey == stationA) {
                    for (Integer trainArc : wagonListMap.get(wagonKey).getTrainArcs()) {
                        if (stationA == trainArcs.get(trainArc).getOrigin()) {
                            constraint1 = model.sum(constraint1, x[wagonsKey.indexOf(wagonKey)][trainArc]);
                        }
                        if (stationA == trainArcs.get(trainArc).getDestination()) {
                            constraint1 = model.sum(constraint1, model.negative(x[wagonsKey.indexOf(wagonKey)][trainArc]));
                        }
                    }
                    model.addEq(constraint1, s[wagonsKey.indexOf(wagonKey)]);
                } else if (stationKey == stationB) {

                    for (Integer trainArc : wagonListMap.get(wagonKey).getTrainArcs()) {
                        if (stationB == trainArcs.get(trainArc).getOrigin()) {
                            constraint1 = model.sum(constraint1, x[wagonsKey.indexOf(wagonKey)][trainArc]);
                        }
                        if (stationB == trainArcs.get(trainArc).getDestination()) {
                            constraint1 = model.sum(constraint1, model.negative(x[wagonsKey.indexOf(wagonKey)][trainArc]));
                        }
                    }
                    model.addEq(constraint1, model.negative(s[wagonsKey.indexOf(wagonKey)]));

                } else {
                    for (Integer trainArc : wagonListMap.get(wagonKey).getTrainArcs()) {
                        if (stationKey == trainArcs.get(trainArc).getOrigin()) {
                            constraint1 = model.sum(constraint1, x[wagonsKey.indexOf(wagonKey)][trainArc]);
                        }
                        if (stationKey == trainArcs.get(trainArc).getDestination()) {
                            constraint1 = model.sum(constraint1, model.negative(x[wagonsKey.indexOf(wagonKey)][trainArc]));
                        }
                    }
                    model.addEq(constraint1, 0);
                }
            }
        }//end constraint 1

        //constraint 2: loco flow 1
        for (Integer dizelKey : dizelsKey) {
            for (int i = 0; i < locoTrip; i++) {
                constraint1 = model.constant(0);

                for (Integer trainArc : dizelListMap.get(dizelKey).getTrainArcs().keySet()) {
                    constraint1 = model.sum(constraint1, l[dizelsKey.indexOf(dizelKey)][trainArc][i]);
                }
                for (Integer blockId : dizelListMap.get(dizelKey).getAllowedBlock().keySet()) {
                    constraint1 = model.sum(constraint1, lAlone[dizelsKey.indexOf(dizelKey)][blockId][i]);
                }
                model.addLe(constraint1, 1);
            }
        }

        //constraint 3: loco flow 2
        for (Integer dizelKey : dizelsKey) {
            for (Integer station : stationsKey) {
                for (int i = 0; i < locoTrip - 1; i++) {
                    constraint1 = model.constant(0);
                    constraint2 = model.constant(0);
                    for (Integer trainArc : dizelListMap.get(dizelKey).getTrainArcs().keySet()) {
                        if (station == trainArcs.get(trainArc).getDestination())
                            constraint1 = model.sum(constraint1, l[dizelsKey.indexOf(dizelKey)][trainArc][i]);
                    }
                    for (Integer blockId : dizelListMap.get(dizelKey).getAllowedBlock().keySet()) {
                        if (station == blockMap.get(blockId).getEndStationID())
                            constraint1 = model.sum(constraint1, lAlone[dizelsKey.indexOf(dizelKey)][blockId][i]);
                    }

                    for (Integer trainArc : dizelListMap.get(dizelKey).getTrainArcs().keySet()) {
                        if (station == trainArcs.get(trainArc).getOrigin())
                            constraint2 = model.sum(constraint2, l[dizelsKey.indexOf(dizelKey)][trainArc][i + 1]);
                    }
                    for (Integer blockId : dizelListMap.get(dizelKey).getAllowedBlock().keySet()) {
                        if (station == blockMap.get(blockId).getStartStationID())
                            constraint2 = model.sum(constraint2, lAlone[dizelsKey.indexOf(dizelKey)][blockId][i + 1]);
                    }

                    model.addGe(constraint1, constraint2);
                }
            }
        }

        //constraint 4: train arcs weights
        for (int i = 0; i < trainArcs.size(); i++) {
            constraint1 = model.constant(0);
            constraint2 = model.constant(0);
            for (Long wagonKey : wagonsKey) {
                if (wagonListMap.get(wagonKey).getTrainArcs().contains(i)) {
                    if (wagonListMap.get(wagonKey).getFreight() != 1883)
                        constraint1 = model.sum(constraint1, model.prod(
                                wagonListMap.get(wagonKey).getFullWeight(), x[wagonsKey.indexOf(wagonKey)][i]));
                    else
                        constraint1 = model.sum(constraint1, model.prod(
                                wagonListMap.get(wagonKey).getEmptyWeight(), x[wagonsKey.indexOf(wagonKey)][i]));
                }
            }
            for (Integer dizelKey : dizelsKey) {
                if (DizelHasArc(dizelKey, i)) {
                    for (int j = 0; j < locoTrip; j++) {
                        constraint2 = model.sum(constraint2, model.prod(l[dizelsKey.indexOf(dizelKey)][i][j],
                                dizelListMap.get(dizelKey).getTrainArcs().get(i)));
                    }
                }
            }
            model.addGe(model.prod(trainArcs.get(i).getMaxWeight(), y[i]), constraint2);
            model.addGe(constraint2, constraint1);
            model.addGe(constraint1, model.prod(constraint2, 0.4));
        }

        //constraint 5: train arcs length
        for (int i = 0; i < trainArcs.size(); i++) {
            constraint1 = model.constant(0);
            for (Long wagonKey : wagonsKey) {
                if (wagonListMap.get(wagonKey).getTrainArcs().contains(i)) {
                    constraint1 = model.sum(constraint1, model.prod(
                            wagonListMap.get(wagonKey).getWagonLength(), x[wagonsKey.indexOf(wagonKey)][i]));
                }
            }
            model.addLe(constraint1, model.prod(trainArcs.get(i).getMaxLength(), y[i]));
        }

        System.out.println("--------------------built----------------------");
        if (model.solve()) {
            System.out.println("--------------------Model solved-------------------");

            for (int i = 0; i < trainArcs.size(); i++) {
                for (Long wagonKey : wagonsKey) {
                    for (Integer integer : wagonListMap.get(wagonKey).getTrainArcs()) {
                        if (integer == i) {
                            if (model.getValue(x[wagonsKey.indexOf(wagonKey)][integer]) > 0) {
                                trainArcs.get(i).setRealLength(trainArcs.get(i).getRealLength()
                                        + wagonListMap.get(wagonKey).getWagonLength());

                                if (wagonListMap.get(wagonKey).getFreight() != 1883)
                                    trainArcs.get(i).setRealWeight(trainArcs.get(i).getRealWeight()
                                            + wagonListMap.get(wagonKey).getFullWeight());
                                else
                                    trainArcs.get(i).setRealWeight(trainArcs.get(i).getRealWeight()
                                            + wagonListMap.get(wagonKey).getEmptyWeight());

                                trainArcs.get(i).setRealWagon(trainArcs.get(i).getRealWagon() + 1);
                            }
                        }
                    }
                }
            }

            System.out.println("--------------------start output-------------------");
            getOutputTrains("out.xlsx");
            getDizelMoves("out.xlsx");
            getOutputDizels("out.xlsx");
            getWagons("out.xlsx");
            getWagonsInfo("out.xlsx");

            try {
                Desktop.getDesktop().open(new File("out.xlsx"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print("---------------------finished output------------------------");
        }
    }

    private boolean DizelHasArc(Integer dizel, int trainarc) {
        for (Integer dizelArc : dizelListMap.get(dizel).getTrainArcs().keySet()) {
            if (trainarc == dizelArc)
                return true;
        }
        return false;
    }

    public void getOutputTrains(String formationFilePath) {
        FileOutputStream outputFile = null;
        XSSFWorkbook workbook = null;
        try {
            outputFile = new FileOutputStream(new File(formationFilePath));
            workbook = new XSSFWorkbook();

            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            //trains sheet
            XSSFSheet sheet1 = workbook.createSheet("قطارها");
            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);


            //label first row
            XSSFCellStyle style = setStyle(workbook, "B Zar");
            XSSFRow row = sheet1.createRow(0);
            setCell(row, 0, "کد قطار", style, headingColor);
            setCell(row, 1, "ناحیه", style, headingColor);
            setCell(row, 2, "مبدا قطار", style, headingColor);
            setCell(row, 3, "مقصد قطار", style, headingColor);
            setCell(row, 4, "کل واگن های روی قطار", style, headingColor);
            setCell(row, 5, "مجموع طول", style, headingColor);
            setCell(row, 6, "مجموع وزن", style, headingColor);
            setCell(row, 7, "تعداد قطار", style, headingColor);
            setCell(row, 8, "حداکثر وزن هر قطار", style, headingColor);
            setCell(row, 9, "حداکثر طول هر قطار", style, headingColor);
            setCell(row, 10, "بهره وری", style, headingColor);
            setCell(row, 11, "جزییات بارها", style, headingColor);
            setCell(row, 12, "", style, headingColor);
            setCell(row, 13, "", style, headingColor);
            setCell(row, 14, "", style, headingColor);

            row = sheet1.createRow(1);
            setCell(row, 0, "", style, headingColor);
            setCell(row, 1, "", style, headingColor);
            setCell(row, 2, "", style, headingColor);
            setCell(row, 3, "", style, headingColor);
            setCell(row, 4, "", style, headingColor);
            setCell(row, 5, "", style, headingColor);
            setCell(row, 6, "", style, headingColor);
            setCell(row, 7, "", style, headingColor);
            setCell(row, 8, "", style, headingColor);
            setCell(row, 9, "", style, headingColor);
            setCell(row, 10, "", style, headingColor);
            setCell(row, 11, "شماره واگن", style, headingColor);
            setCell(row, 12, "مبدا واگن", style, headingColor);
            setCell(row, 13, "مقصد واگن", style, headingColor);
            setCell(row, 14, "نوع بار", style, headingColor);

            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 6, 6));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 7, 7));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 8, 8));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 9, 9));
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 10, 10));
            sheet1.addMergedRegion(new CellRangeAddress(0, 0, 11, 14));
            XSSFColor bodyColor;
            int rowCounter = 2;
            Random random = new Random();
            for (Integer station : stationsKey) {
                XSSFCellStyle style1 = setStyle(workbook, "B Zar");
                //choose color
                Color color = new Color(
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);
                bodyColor = new XSSFColor(color);
                for (int j = 0; j < trainArcs.size(); j++) {
                    if (model.getValue(y[j]) > 0.5) {
                        if (trainArcs.get(j).getOrigin() == station) {
                            boolean firstRowIsWrite = false;
                            int temp = rowCounter;
                            for (Long wagonKey : wagonsKey) {
                                if (wagonListMap.get(wagonKey).getTrainArcs().contains(j)) {
                                    if (model.getValue(x[wagonsKey.indexOf(wagonKey)][j]) >= 0.5) {

                                        row = sheet1.createRow(rowCounter);
                                        setCell(row, 0, (float) j, style1, bodyColor);
                                        setCell(row, 1, stationMap.get(trainArcs.get(j).getOrigin()).getNahieh(),
                                                style1, bodyColor);
                                        setCell(row, 2, stationMap.get(trainArcs.get(j).getOrigin()).getName(),
                                                style1, bodyColor);
                                        setCell(row, 3, stationMap.get(
                                                trainArcs.get(j).getDestination()).getName(), style1, bodyColor);

                                        if (!firstRowIsWrite) {
                                            setCell(row, 4, trainArcs.get(j).getRealWagon(), style1, bodyColor);
                                            setCell(row, 5, trainArcs.get(j).getRealLength(), style1, bodyColor);
                                            setCell(row, 6, trainArcs.get(j).getRealWeight(), style1, bodyColor);
                                            setCell(row, 7, (float) model.getValue(y[j]), style1, bodyColor);
                                            setCell(row, 8, trainArcs.get(j).getMaxWeight(), style1, bodyColor);
                                            setCell(row, 9, trainArcs.get(j).getMaxLength(), style1, bodyColor);
                                            setCell(row, 10, trainArcs.get(j).getArcEfficiency(), style1, bodyColor);
                                            firstRowIsWrite = true;
                                        } else {
                                            setCell(row, 3, "", style1, bodyColor);
                                            setCell(row, 4, "", style1, bodyColor);
                                            setCell(row, 5, "", style1, bodyColor);
                                            setCell(row, 6, "", style1, bodyColor);
                                            setCell(row, 7, "", style1, bodyColor);
                                            setCell(row, 8, "", style1, bodyColor);
                                            setCell(row, 9, "", style1, bodyColor);
                                            setCell(row, 10, "", style1, bodyColor);
                                        }

                                        setCell(row, 11, wagonKey, style1, bodyColor);
                                        setCell(row, 12, stationMap.get(
                                                wagonListMap.get(wagonKey).getLastStation()).getName(), style1, bodyColor);
                                        setCell(row, 13, stationMap.get(
                                                wagonListMap.get(wagonKey).getDestination()).getName(), style1, bodyColor);
                                        setCell(row, 14, freightMap.get(wagonListMap.get(wagonKey).getFreight()), style1, bodyColor);
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
                                sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 8, 8));
                                sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 9, 9));
                                sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 10, 10));
                            }
                        }
                    }
                }
            }

            workbook.write(outputFile);
            outputFile.flush();
            outputFile.close();
        } catch (IOException | IloException e) {
            e.printStackTrace();
        }
    }

    public void getDizelMoves(String formationFilePath) {
        FileInputStream inFile;
        FileOutputStream outFile;
        XSSFWorkbook workBook;
        try {
            inFile = new FileInputStream(new File(formationFilePath));
            try {
                workBook = new XSSFWorkbook(inFile);
            } catch (EmptyFileException e) {
                workBook = new XSSFWorkbook();
            }


            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            //trains sheet
            XSSFSheet sheet1 = workBook.createSheet("دیزل ها");

            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);

            row = sheet1.createRow(1);
            XSSFCellStyle style1 = setStyle(workBook, "B Zar");
            setCell(row, 0, "شماره دیزل", style1, headingColor);
            setCell(row, 1, "ایستگاه مبدا", style1, headingColor);
            setCell(row, 2, "سفر", style1, headingColor);
            setCell(row, 3, "اصلی/منفرد/سرد", style1, headingColor);
            setCell(row, 4, "کد قطار", style1, headingColor);
            setCell(row, 5, "حداکثر کشش دیزل", style1, headingColor);
            setCell(row, 6, "مبدا", style1, headingColor);
            setCell(row, 7, "مقصد", style1, headingColor);

            XSSFColor bodyColor;
            int rowCounter = 2;
            Random random = new Random();
            for (Integer dizelkey : dizelsKey) {
                //choose color
                Color color = new Color(
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);
                bodyColor = new XSSFColor(color);
                int temp = rowCounter;
                for (int i = 0; i < locoTrip; i++) {
                    for (Integer trainArc : dizelListMap.get(dizelkey).getTrainArcs().keySet()) {
                        if (model.getValue(l[dizelsKey.indexOf(dizelkey)][trainArc][i]) > 0.5) {
                            boolean firstRowIsWrite = true;

                            row = sheet1.createRow(rowCounter);

                            setCell(row, 0, (float) dizelkey, style1, bodyColor);
                            setCell(row, 1,
                                    stationMap.get(dizelListMap.get(dizelkey).getLastStation()).getName()
                                    , style1, bodyColor);

                            setCell(row, 2, i + 1, style1, bodyColor);
                            setCell(row, 3, "اصلی", style1, bodyColor);
                            setCell(row, 4, trainArc, style1, bodyColor);
                            setCell(row, 5, dizelListMap.get(dizelkey).getTrainArcs().get(trainArc),
                                    style1, bodyColor);
                            setCell(row, 6, stationMap.get(trainArcs.get(trainArc).getOrigin()).getName(),
                                    style1, bodyColor);
                            setCell(row, 7, stationMap.get(trainArcs.get(trainArc).getDestination()).getName(),
                                    style1, bodyColor);
                            rowCounter++;
                        }
                    }
                    for (Integer blockId : dizelListMap.get(dizelkey).getAllowedBlock().keySet()) {
                        if (model.getValue(lAlone[dizelsKey.indexOf(dizelkey)][blockId][i]) > 0.5) {
                            row = sheet1.createRow(rowCounter);

                            setCell(row, 0, (float) dizelkey, style1, bodyColor);
                            setCell(row, 1,
                                    stationMap.get(dizelListMap.get(dizelkey).getLastStation()).getName()
                                    , style1, bodyColor);

                            setCell(row, 2, i + 1, style1, bodyColor);
                            setCell(row, 3, "منفرد", style1, bodyColor);
                            setCell(row, 4, "-", style1, bodyColor);
                            setCell(row, 5, dizelListMap.get(dizelkey).getAllowedBlock().get(blockId),
                                    style1, bodyColor);
                            setCell(row, 6, stationMap.get(blockMap.get(blockId).getStartStationID()).getName(),
                                    style1, bodyColor);
                            setCell(row, 7, stationMap.get(blockMap.get(blockId).getEndStationID()).getName(),
                                    style1, bodyColor);
                            rowCounter++;
                        }
                    }
                }

                if (rowCounter > temp && (rowCounter - 1) != temp) {
                    sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 0, 0));
                    sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 1, 1));
                }

            }
            outFile = new FileOutputStream(new File(formationFilePath));
            workBook.write(outFile);

            outFile.flush();
            outFile.close();
            workBook.close();
        } catch (IOException | IloException e) {
            e.printStackTrace();
        }
    }

    public void getOutputDizels(String formationFilePath) {
        FileInputStream inFile;
        FileOutputStream outFile;
        XSSFWorkbook workBook;
        try {
            inFile = new FileInputStream(new File(formationFilePath));
            try {
                workBook = new XSSFWorkbook(inFile);
            } catch (EmptyFileException e) {
                workBook = new XSSFWorkbook();
            }

            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            //trains sheet
            XSSFSheet sheet1 = workBook.createSheet("تعداد دیزل ها");

            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);

            XSSFCellStyle style1 = setStyle(workBook, "B Zar");
            row = sheet1.createRow(0);
            setCell(row, 1, "مجموع", style1, headingColor);

            row = sheet1.createRow(1);
            setCell(row, 0, "ناحیه", style1, headingColor);
            setCell(row, 1, "ایستگاه", style1, headingColor);
            setCell(row, 2, "تعداد دیزل", style1, headingColor);

            XSSFColor bodyColor;
            int rowCounter = 2;
            Random random = new Random();
            for (Map.Entry<Integer, Station> station : stationMap.entrySet()) {
                int dizelCount = 0;
                //choose color
                Color color = new Color(
                        random.nextInt(55) + 200,
                        random.nextInt(55) + 200,
                        random.nextInt(55) + 200);
                bodyColor = new XSSFColor(color);
                for (Map.Entry<Integer, Dizel> dizel : dizelListMap.entrySet()) {
                    if (dizel.getValue().getLastStation() == station.getKey()) {
                        dizelCount++;
                    }
                }
                if (dizelCount > 0) {
                    row = sheet1.createRow(rowCounter);
                    setCell(row, 0, nahiehtMap.get(station.getValue().getNahieh()), style1, bodyColor);
                    setCell(row, 1, station.getValue().getName(), style1, bodyColor);
                    setCell(row, 2, dizelCount, style1, bodyColor);
                    rowCounter++;
                }
            }
            setCellFormula(sheet1.getRow(0), 2, "SUBTOTAL(9,C2:C"+rowCounter+")", style1, headingColor);

            outFile = new FileOutputStream(new File(formationFilePath));
            workBook.write(outFile);

            outFile.flush();
            outFile.close();
            workBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getWagons(String formationFilePath) {
        FileInputStream inFile;
        FileOutputStream outFile;
        XSSFWorkbook workBook;
        try {
            inFile = new FileInputStream(new File(formationFilePath));
            try {
                workBook = new XSSFWorkbook(inFile);
            } catch (EmptyFileException e) {
                workBook = new XSSFWorkbook();
            }


            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            //trains sheet
            XSSFSheet sheet1 = workBook.createSheet("تعداد واگن ها");

            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);

            row = sheet1.createRow(1);
            XSSFCellStyle style1 = setStyle(workBook, "B Zar");
            setCell(row, 0, "ناحیه", style1, headingColor);
            setCell(row, 1, "ایستگاه", style1, headingColor);
            setCell(row, 2, "نوع بار", style1, headingColor);
            setCell(row, 3, "ظرفیت بارگیری", style1, headingColor);
            setCell(row, 4, "ظرفیت تخلیه", style1, headingColor);
            setCell(row, 5, "موجود ایستگاه", style1, headingColor);
            setCell(row, 6, "پر در راه", style1, headingColor);
            setCell(row, 7, "خالی در راه", style1, headingColor);

            XSSFColor bodyColor;
            int rowCounter = 2;
            Random random = new Random();
            for (Map.Entry<Integer, Station> station : stationMap.entrySet()) {
                //choose color
                Color color = new Color(
                        random.nextInt(55) + 200,
                        random.nextInt(55) + 200,
                        random.nextInt(55) + 200);
                bodyColor = new XSSFColor(color);
                int temp = rowCounter;
                for (Map.Entry<Integer, Station.Capacity> freight : station.getValue().getStationCapacity().entrySet()) {
                    row = sheet1.createRow(rowCounter);
                    setCell(row, 0, nahiehtMap.get(station.getValue().getNahieh()), style1, bodyColor);
                    setCell(row, 1, station.getValue().getName(), style1, bodyColor);
                    setCell(row, 2, freightMap.get(freight.getKey()), style1, bodyColor);
                    setCell(row, 3, freight.getValue().loadingCap, style1, bodyColor);
                    setCell(row, 4, freight.getValue().unloadingCap, style1, bodyColor);
                    setCell(row, 5, freight.getValue().stationWagon.size(), style1, bodyColor);
                    setCell(row, 6, freight.getValue().comingLoadWagons.size(), style1, bodyColor);
                    setCell(row, 7, freight.getValue().comingEmptyWagons.size(), style1, bodyColor);
                    rowCounter++;
                }

                if (rowCounter > temp && (rowCounter - 1) != temp) {
                    sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 0, 0));
                    sheet1.addMergedRegion(new CellRangeAddress(temp, rowCounter - 1, 1, 1));
                }
            }

            outFile = new FileOutputStream(new File(formationFilePath));
            workBook.write(outFile);

            outFile.flush();
            outFile.close();
            workBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getWagonsInfo(String formationFilePath) {
        FileInputStream inFile;
        FileOutputStream outFile;
        XSSFWorkbook workBook = null;
        try {
            inFile = new FileInputStream(new File(formationFilePath));
            workBook = new XSSFWorkbook(inFile);

            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            //trains sheet
            XSSFSheet sheet1 = workBook.createSheet("اطلاعات واگن ها");

            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);

            XSSFCellStyle style1 = setStyle(workBook, "B Zar");
            row = sheet1.createRow(0);
            setCell(row, 5, "مجموع", style1, headingColor);
            row = sheet1.createRow(1);
            setCell(row, 0, "شماره واگن", style1, headingColor);
            setCell(row, 1, "ناحیه حال حاضر", style1, headingColor);
            setCell(row, 2, "ایستگاه حال حاضر", style1, headingColor);
            setCell(row, 3, "مقصد", style1, headingColor);
            setCell(row, 4, "نوع بار", style1, headingColor);
            setCell(row, 5, "نوع واگن", style1, headingColor);
            setCell(row, 6, "اولویت", style1, headingColor);
            setCell(row, 7, "حمل شده یا نشده", style1, headingColor);

            XSSFColor bodyColor;
            int rowCounter = 2;
            Random random = new Random();


            for (Map.Entry<Long, newWagon> wagon : wagonListMap.entrySet()) {
                Color color = new Color(
                        random.nextInt(55) + 200,
                        random.nextInt(55) + 200,
                        random.nextInt(55) + 200);
                bodyColor = new XSSFColor(color);
                row = sheet1.createRow(rowCounter);
                setCell(row, 0, wagon.getKey(), style1, bodyColor);
                setCell(row, 1, nahiehtMap.get(stationMap.get(wagon.getValue().getLastStation()).getNahieh())
                        , style1, bodyColor);
                setCell(row, 2, stationMap.get(wagon.getValue().getLastStation()).getName(), style1, bodyColor);
                try {
                    setCell(row, 3, stationMap.get(wagon.getValue().getDestination()).getName(), style1, bodyColor);
                } catch (NullPointerException e) {
                    setCell(row, 3, wagon.getValue().getDestination(), style1, bodyColor);
                }
                setCell(row, 4, freightMap.get(wagon.getValue().getFreight()), style1, bodyColor);
                setCell(row, 5, wagonType.get(wagon.getValue().getWagonType()), style1, bodyColor);
                setCell(row, 6, wagon.getValue().getPriority(), style1, bodyColor);


                if (model.getValue(s[(wagonsKey.indexOf(wagon.getKey()))]) > 0.5)
                    setCell(row, 7, 1, style1, bodyColor);
                else
                    setCell(row, 7, 0, style1, bodyColor);

                rowCounter++;
            }
            setCellFormula(sheet1.getRow(0), 6, "SUBTOTAL(2,G2:G"+rowCounter+")", style1, headingColor);
            setCellFormula(sheet1.getRow(0), 7, "SUBTOTAL(9,H2:H"+rowCounter+")", style1, headingColor);

            outFile = new FileOutputStream(new File(formationFilePath));
            workBook.write(outFile);

            outFile.flush();
            outFile.close();
            workBook.close();

        } catch (IOException | IloException e) {
            System.out.println(e.getMessage());;
        }
    }
}
