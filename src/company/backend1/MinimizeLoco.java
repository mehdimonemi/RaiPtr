package company.backend1;

import company.Data.ManovrStation;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.io.*;
import java.util.Random;

import static company.backend1.Formation.locoInHand;
import static company.backend1.Massages.*;
import static company.backend1.ReadTypicalData.*;


/**
 * Created by Monemi_M on 11/12/2017.
 */
public class MinimizeLoco extends ExcelSetValue {
    public static int locoNumber = 50;
    public static double locoDepoTime = 120;
    public static double locoOfficeWorkTime = 60;
    double locoPreMoveTime = locoDepoTime + locoOfficeWorkTime;
    IloCplex model;
    IloNumVar[][] x;
    IloNumVar[][][] y;
    IloNumVar[][] soArcs;
    IloNumVar[][] siArcs;


    public String main(String locoDepoTime, String locoOfficeWorkTime) {

        if (!locoDepoTime.equals("")) {
            this.locoDepoTime = Double.parseDouble(locoDepoTime);
        }

        if (!locoOfficeWorkTime.equals("")) {
            this.locoOfficeWorkTime = Double.parseDouble(locoOfficeWorkTime);
        }

            this.locoNumber = locoInHand;


        locoPreMoveTime = this.locoDepoTime + this.locoOfficeWorkTime;

        try {
            if (model != null) {
                model.clearModel();
            }

            buildModel();

//        model.setParam(IloCplex.Param.TimeLimit, 10);
//        model.setParam(IloCplex.Param.MIP.Strategy.Probe, 3);
//        model.setParam(IloCplex.Param.MIP.Strategy.VariableSelect, 3);
//        model.setParam(IloCplex.Param.MIP.Strategy.PresolveNode, 1);
//        model.setParam(IloCplex.Param.MIP.Strategy.RINSHeur, 3);
//        model.setParam(IloCplex.Param.Emphasis.MIP, 2);
//        model.setParam(IloCplex.Param.MIP.Cuts.Gomory, 2);
//        model.setParam(IloCplex.Param.MIP.Tolerances.MIPGap, 0.0306);

            if (model.solve()) {
                return LocoMinimizeSuccess.toString();
            } else {
                return LocoMinimizeUnSuccess.toString();
            }
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (IloException e) {
            return CplexException.toString();
        }
    }

    public void buildModel() {
        try {
            model = new IloCplex();

            //define decision variables
            x = new IloNumVar[locoNumber][trainArcs.size()];
            y = new IloNumVar[locoNumber][trainArcs.size()][trainArcs.size()];
            soArcs = new IloNumVar[locoNumber][sourceArcs.size()];
            siArcs = new IloNumVar[locoNumber][sinkArcs.size()];

            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < trainArcs.size(); j++) {
                    x[i][j] = model.numVar(0, 1, IloNumVarType.Int);
                    for (int k = 0; k < trainArcs.size(); k++) {
                        if (trainArcs.get(j).IsNeighborTrainArcs(trainArcs.get(k))) {
                            y[i][j][k] = model.numVar(0, 1, IloNumVarType.Int);
                        }
                    }
                }
            }

            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < sourceArcs.size(); j++) {
                    soArcs[i][j] = model.numVar(0, 1, IloNumVarType.Int);
                }
            }

            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < sourceArcs.size(); j++) {
                    siArcs[i][j] = model.numVar(0, 1, IloNumVarType.Int);
                }
            }
            //end define decision variables


            //objective function
            IloNumExpr goalFunction = model.constant(0);
            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < trainArcs.size(); j++) {
                    goalFunction = model.sum(goalFunction, model.prod(x[i][j], trainArcs.get(j).getTime()));
                }
            }

            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < trainArcs.size(); j++) {
                    for (int k = 0; k < trainArcs.size(); k++) {
                        if (trainArcs.get(j).IsNeighborTrainArcs(trainArcs.get(k))) {
                            goalFunction = model.sum(goalFunction, model.prod(model.negative(y[i][j][k]), 100));
                        }
                    }
                }
            }

            model.addMinimize(goalFunction);

            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < trainArcs.size(); j++) {
                    for (int k = 0; k < trainArcs.size(); k++) {
                        if (trainArcs.get(j).IsNeighborTrainArcs(trainArcs.get(k))) {
                            model.addLe(y[i][j][k], model.prod(model.sum(x[i][j], x[i][k]), 0.5));
                        }
                    }
                }
            }

            //constraint 1
            IloNumExpr constraint;
            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < trainArcs.size(); j++) {
                    constraint = model.constant(0);
                    for (int k = 0; k < trainArcs.size(); k++) {
                        if (j != k) {
                            if (trainArcs.get(j).getOriginId() == trainArcs.get(k).getDestinationId()) {
                                constraint = model.sum(constraint, x[i][k]);
                            }
                            if (trainArcs.get(j).getOriginId() == trainArcs.get(k).getOriginId()) {
                                constraint = model.sum(constraint, model.negative(x[i][k]));
                            }
                        }
                    }
                    constraint = model.sum(constraint, soArcs[i][trainArcs.get(j).getOriginId()]);
                    constraint = model.sum(constraint, model.negative(siArcs[i][trainArcs.get(j).getOriginId()]));
                    model.addEq(constraint, x[i][j]);
                }

                for (int j = 0; j < sinkArcs.size(); j++) {
                    constraint = model.constant(0);
                    for (int k = 0; k < trainArcs.size(); k++) {
                        if (sinkArcs.get(j).getOriginId() == trainArcs.get(k).getDestinationId()) {
                            constraint = model.sum(constraint, x[i][k]);
                        }
                        if (sinkArcs.get(j).getOriginId() == trainArcs.get(k).getOriginId()) {
                            constraint = model.sum(constraint, model.negative(x[i][k]));
                        }
                    }
                    constraint = model.sum(constraint, soArcs[i][sinkArcs.get(j).getOriginId()]);
                    model.addEq(constraint, siArcs[i][j]);
                }
            }//end constraint 1

            //constraint 2
            for (int i = 0; i < trainArcs.size(); i++) {
                constraint = model.constant(0);
                for (int j = 0; j < locoNumber; j++) {
                    constraint = model.sum(constraint, model.prod(x[j][i], locos.get(0).getPowers()[14]));
                }
                model.addEq(constraint, trainArcs.get(i).getMaxWeight());
            }

            //constraint 3
            for (int i = 0; i < locoNumber; i++) {
                constraint = model.constant(0);
                for (int j = 0; j < trainArcs.size(); j++) {
                    constraint = model.sum(constraint, model.prod(x[i][j], trainArcs.get(j).getTime()));
                }
                model.addLe(constraint, ((25 * 60)));
            }

            //constraint 5
            for (int i = 0; i < locoNumber; i++) {
                constraint = model.constant(0);
                for (int j = 0; j < sourceArcs.size(); j++) {
                    constraint = model.sum(constraint, soArcs[i][j]);
                }
                model.addEq(constraint, 1);
            }

            for (int i = 0; i < locoNumber; i++) {
                constraint = model.constant(0);
                for (int j = 0; j < sinkArcs.size(); j++) {
                    constraint = model.sum(constraint, siArcs[i][j]);
                }
                model.addEq(constraint, 1);
            }


            for (int i = 0; i < locoNumber; i++) {
                for (int j = 0; j < trainArcs.size(); j++) {
                    constraint = model.constant(0);
                    constraint = model.sum(constraint, x[i][j]);
                    for (int k = 0; k < trainArcs.size(); k++) {
                        if (trainArcs.get(k).getOriginId() == trainArcs.get(j).getDestinationId() &&
                                trainArcs.get(k).getDestinationId() == trainArcs.get(j).getOriginId()) {
                            constraint = model.sum(constraint, x[i][k]);
                        }
                    }
                    model.addLe(constraint, 1);
                }
            }
        } catch (IloException e) {

        }
    }

    public String getOutput(String locoFilePath) {


        FileInputStream inputFile;
        FileOutputStream outputFile;
        XSSFWorkbook workbook;

        try {

            inputFile = new FileInputStream(new File(locoFilePath));

            workbook = new XSSFWorkbook(inputFile);

            XSSFSheet sheet1 = workbook.getSheet("دیزل ها");
            if (sheet1 != null) {
                int index = workbook.getSheetIndex(sheet1);
                workbook.removeSheetAt(index);
                sheet1 = workbook.createSheet("دیزل ها");
            }
            if (sheet1 == null) {
                sheet1 = workbook.createSheet("دیزل ها");
            }

            sheet1.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);
            XSSFRow row = null;
            XSSFCell cell;

            XSSFCellStyle style = setStyle(workbook, "B Zar");
            Color c = new Color(200, 200, 200);
            XSSFColor headingColor = new XSSFColor(c);

            //labeling
            XSSFRow row1 = sheet1.createRow(0);
            XSSFRow row2 = sheet1.createRow(1);

            setCell(sheet1, row1, 0, "شماره دیزل", style, headingColor);
            setCell(sheet1, row2, 0, "", style, headingColor);
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

            setCell(sheet1, row1, 1, "ایستگاه شروع کار", style, headingColor);
            setCell(sheet1, row2, 1, "", style, headingColor);
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));

            setCell(sheet1, row1, 2, "ایستگاه پایان کار", style, headingColor);
            setCell(sheet1, row2, 2, "", style, headingColor);
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));

            setCell(sheet1, row1, 3, "ساعت کار", style, headingColor);
            setCell(sheet1, row2, 3, "", style, headingColor);
            sheet1.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));

            for (int i = 0; i < 5; i++) {
                setCell(sheet1, row1, (3 * i) + 4, "قطار", style, headingColor);
                sheet1.addMergedRegion(new CellRangeAddress(0, 0, (3 * i) + 4, (3 * i) + 4 + 2));

                setCell(sheet1, row2, (3 * i) + 4 + 0, "مبدا", style, headingColor);
                setCell(sheet1, row2, (3 * i) + 4 + 1, "مقصد", style, headingColor);
                setCell(sheet1, row2, (3 * i) + 4 + 2, "شماره قطار", style, headingColor);
            }//end of labeling


            XSSFColor bodyColor;
            Random random = new Random();
            int rowCounter = 2;
            for (ManovrStation station : manovrStations) {
                XSSFCellStyle style1 = setStyle(workbook, "B Zar");
                //choose color
                Color color = new Color(
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);
                bodyColor = new XSSFColor(color);

                for (int q = 0; q < trains.size(); q++) {
                    for (int i = 0; i < locoNumber; i++) {
                        boolean isInStation = false;
                        boolean allOkay = false;

                        for (int j = 0; j < sourceArcs.size(); j++) {
                            if (model.getValue(soArcs[i][j]) > 0.5 &&
                                    station.getName().equals(sourceArcs.get(j).getDestination())) {
                                isInStation = true;
                            }
                        }

                        int k = 4;
                        for (int j = 0; j < trainArcs.size(); j++) {
                            if (model.getValue(x[i][j]) > 0.5
                                    && isInStation
                                    && trainArcs.get(j).getTrainId() == trains.get(q).getId()) {
                                row = sheet1.createRow(rowCounter);
                                setCell(sheet1, row, k, (trainArcs.get(j).getOrigin()), style1, bodyColor);
                                k++;
                                setCell(sheet1, row, k, trainArcs.get(j).getDestination(), style1, bodyColor);
                                k++;
                                setCell(sheet1, row, k, (double) trainArcs.get(j).getTrainId(), style1, bodyColor);
                                k++;
                                allOkay = true;
                            }
                        }

                        double time = 0;
                        if (allOkay) {
                            for (int j = 0; j < trainArcs.size(); j++) {
                                if (model.getValue(x[i][j]) > 0.5){
                                    time += (model.getValue(x[i][j]) * (trainArcs.get(j).getTime()));
                                }
                            }
                        }
                        if (allOkay) {
                            setCell(sheet1, row, 3, time / 60, style1, bodyColor);
                            for (int j = 0; j < sourceArcs.size(); j++) {
                                if (model.getValue(soArcs[i][j]) > 0.5) {
                                    setCell(sheet1, row, 0, (double) (i + 1), style1, bodyColor);
                                    setCell(sheet1, row, 1, sourceArcs.get(j).getDestination(), style1, bodyColor);
                                }
                            }
                        }
                        if (allOkay) {
                            for (int j = 0; j < sinkArcs.size(); j++) {
                                if (model.getValue(siArcs[i][j]) > 0.5) {
                                    setCell(sheet1, row, 2, sinkArcs.get(j).getOrigin(), style1, bodyColor);
                                }
                            }
                            rowCounter++;
                        }
                    }
                }
            }


            //excel: how many locos each stations need?
            XSSFSheet sheet2 = workbook.getSheet("آمار دیزل های هر ایستگاه");

            if (sheet2 != null) {
                int index = workbook.getSheetIndex(sheet2);
                workbook.removeSheetAt(index);
                sheet2 = workbook.createSheet("آمار دیزل های هر ایستگاه");
            }

            if (sheet2 == null) {
                sheet2 = workbook.createSheet("آمار دیزل های هر ایستگاه");
            }

            sheet2.getCTWorksheet().getSheetViews().getSheetViewArray(0).setRightToLeft(true);

            row = sheet2.createRow(0);

            setCell(sheet2, row, 0, "ایستگاه", style, headingColor);
            setCell(sheet2, row, 1, "تعداد دیزل", style, headingColor);

            rowCounter = 1;
            for (ManovrStation station : manovrStations) {
                int temp = 0;
                for (int j = 0; j < sourceArcs.size(); j++) {
                    if (station.getName().equals(sourceArcs.get(j).getDestination())) {
                        for (int i = 0; i < locoNumber; i++) {
                            if (model.getValue(soArcs[i][j]) > 0.5) {
                                //Is loco Moving?
                                for (int k = 0; k < trainArcs.size(); k++) {
                                    if (model.getValue(x[i][k]) > 0) {
                                        temp++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }


                if (temp > 0) {
                    row = sheet2.createRow(rowCounter);
                    setCell(sheet2, row, 0, station.getName(), style, null);
                    setCell(sheet2, row, 1, (double) temp, style, null);
                    rowCounter++;
                }
            }

            inputFile.close();
            outputFile = new FileOutputStream(new File(locoFilePath));

            workbook.write(outputFile);

            outputFile.flush();
            outputFile.close();

            return ExcelSave.toString();
        } catch (IloCplex.UnknownObjectException e) {
            return UnknownCplexException.toString();
        } catch (FileNotFoundException e) {
            return NoFileSelected.toString();
        } catch (IOException e) {
            return FileNotFound.toString();
        } catch (IloException e) {
            return CplexException.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        }
    }
}
