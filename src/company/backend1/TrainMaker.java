package company.backend1;

import company.Data.*;
import company.Data.oldOnes.Block;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static company.backend1.Massages.*;
import static company.backend1.ReadTypicalData.*;

/**
 * Created by Monemi_M on 10/07/2017.
 */
public class TrainMaker extends ExcelSetValue {
    public static int maxOneOfTheKindTrain = 10;
    public static int longTrainLength = 600;
    int trainCounter = 0;
    int stationA;
    int stationB;

    public String main(String longTrain) {
        trainCounter = 0;

        if (!longTrain.equals("")) {
            longTrainLength = Integer.parseInt(longTrain);
        }

        String result = buildModel();

        //each commodity can transfer with some particular trains
        for (Commodity commodity : commodities) {
            ArrayList<Train> commodityTrains = new ArrayList<>();
            if (commodity.getOriginId() < commodity.getDestinationId()) {
                for (Train train : trains) {
                    if ((train.getOriginId() >= commodity.getOriginId() &&
                            train.getOriginId() < commodity.getDestinationId()) &&
                            (train.getDestinationId() > commodity.getOriginId()
                                    && train.getDestinationId() <= commodity.getDestinationId()) &&
                            (train.getOriginId() < train.getDestinationId())) {
                        commodityTrains.add(train);
                    }
                }
                commodity.setTrains(commodityTrains);
            }
            if (commodity.getOriginId() > commodity.getDestinationId()) {
                for (Train train : trains) {
                    if ((train.getOriginId() <= commodity.getOriginId() &&
                            train.getOriginId() > commodity.getDestinationId())
                            && (train.getDestinationId() < commodity.getOriginId() &&
                            train.getDestinationId() >= commodity.getDestinationId())
                            && (train.getOriginId() > train.getDestinationId())) {
                        commodityTrains.add(train);
                    }
                }
                commodity.setTrains(commodityTrains);
            }
        }

        //normalization of trains distances
        double maxDistance = 0;
        for (Train train : trains) {
            if (train.getDistance() > maxDistance) {
                maxDistance = train.getDistance();
            }
        }

        for (Train train : trains) {
            train.setDistance(train.getDistance() / maxDistance);
        }
        return result;
    }

    public String buildModel() {
        try {
            IloCplex model = new IloCplex();
            IloNumVar[] X = new IloNumVar[blocks.size()];
            IloNumExpr goalFunction;
            IloNumExpr constraint;


            for (int z = 0; z < manovrStations.size(); z++) {
                for (int x = 0; x < manovrStations.size(); x++) {
                    if (z == x) {
                        continue;
                    } else if (isThereThisOD(manovrStations.get(z).getName(), manovrStations.get(x).getName())) {
                        stationA = manovrStations.get(z).getId();
                        stationB = manovrStations.get(x).getId();

                        for (int i = 0; i < blocks.size(); i++) {
                            X[i] = model.numVar(0, 1, IloNumVarType.Int);
                        }

                        goalFunction = model.constant(0);
                        for (int i = 0; i < blocks.size(); i++) {
                            goalFunction = model.sum(goalFunction, model.prod(X[i],
                                    blocks.get(i).getLength()));
                        }
                        model.addMinimize(goalFunction);

                        // constraints
                        for (int i = 0; i < manovrStations.size(); i++) {
                            constraint = model.constant(0);
                            if (manovrStations.get(i).getId() == stationA) {
                                for (int j = 0; j < blocks.size(); j++) {
                                    if (stationA == blocks.get(j).getOriginId()) {
                                        constraint = model.sum(constraint, X[j]);
                                    }
                                    if (stationA == blocks.get(j).getDestinationId()) {
                                        constraint = model.sum(constraint, model.negative(X[j]));
                                    }
                                }
                                model.addEq(constraint, 1);
                            } else if (manovrStations.get(i).getId() == (stationB)) {
                                for (int j = 0; j < blocks.size(); j++) {
                                    if (stationB == blocks.get(j).getOriginId()) {
                                        constraint = model.sum(constraint, X[j]);
                                    }
                                    if (stationB == blocks.get(j).getDestinationId()) {
                                        constraint = model.sum(constraint, model.negative(X[j]));
                                    }
                                }
                                model.addEq(constraint, -1);
                            } else {
                                for (int j = 0; j < blocks.size(); j++) {
                                    if (manovrStations.get(i).getId() ==
                                            (blocks.get(j).getOriginId())) {
                                        constraint = model.sum(constraint, X[j]);
                                    }
                                    if (manovrStations.get(i).getId() ==
                                            (blocks.get(j).getDestinationId())) {
                                        constraint = model.sum(constraint, model.negative(X[j]));
                                    }
                                }
                                model.addEq(constraint, 0);
                            }
                        } // end of constraints

                        model.setOut(null);
                        if (model.solve()) {
                            ArrayList<Block> trinBlocks = new ArrayList<>();
                            for (int i = 0; i < blocks.size(); i++) {
                                if (model.getValue(X[i]) == 1) {
                                    trinBlocks.add(blocks.get(i));
                                }
                            }
                            int maxAscent = 900000;

                            for (Block trinBlock : trinBlocks) {
                                if (maxAscent >= trinBlock.getAscent()) {
                                    maxAscent = trinBlock.getAscent();
                                }
                            }

                            int maxTrainLength = 900000;

                            for (Block trinBlock : trinBlocks) {
                                if (maxTrainLength >= trinBlock.getMaxTrainLength()) {
                                    maxTrainLength = trinBlock.getMaxTrainLength();
                                }
                            }

                            double time = 0;
                            for (int i = 0; i < blocks.size(); i++) {
                                if (model.getValue(X[i]) == 1) {
                                    time += blocks.get(i).getTime();
                                }
                            }

                            //add possible train force to the train
                            Set<Integer> forces = new HashSet<>();
                            //tak loco
                            for (Loco loco : locos) {
                                forces.add(loco.getPowers()[maxAscent - 1]);
                            }
                            //double loco
                            for (Loco loco1 : locos) {
                                for (Loco loco2 : locos) {
                                    forces.add(loco1.getPowers()[maxAscent - 1] + loco2.getPowers()[maxAscent - 1]);
                                }
                            }
                            //souble loco
                            for (Loco loco1 : locos) {
                                for (Loco loco2 : locos) {
                                    for (Loco loco3 : locos) {
                                        forces.add(loco1.getPowers()[maxAscent - 1]
                                                + loco2.getPowers()[maxAscent - 1]
                                                + loco3.getPowers()[maxAscent - 1]);

                                    }
                                }
                            }

                            for (int force : forces) {
                                if (force <= 5000) {
                                    for (int i = 0; i < maxOneOfTheKindTrain; i++) {
                                        trains.add(new Train(trainCounter,
                                                manovrStations.get(stationA).getName(),
                                                manovrStations.get(stationB).getName(),
                                                maxTrainLength - (force / 1000) * 20,
                                                force, model.getObjValue(),
                                                longTrainLength - (force / 1000) * 20, time, trinBlocks));
                                        trainCounter++;
                                    }
                                }
                            }
                            model.clearModel();
                        } else {
                            model.clearModel();
                            System.out.println("station " + stationA + " to " + stationB + " : No");
                        }
                    }
                }
            }
            return MakeTriansSuccess.toString();
        } catch (IloCplex.UnknownObjectException e) {
            return UnknownCplexException.toString();
        } catch (IloException e) {
            return CplexException.toString();
        }
    }

    public String getOutput(String file) {

        FileInputStream inputFile;
        FileOutputStream outputFile;
        XSSFWorkbook workbook;

        try {
            inputFile = new FileInputStream(new File(file));
            workbook = new XSSFWorkbook(inputFile);
            XSSFSheet sheet = workbook.getSheetAt(3);
            XSSFRow row;

            XSSFCellStyle style = setStyle(workbook, "B Zar");

            //delete existing cells
            if (sheet.getLastRowNum() > 1) {
                for (int i = 1; i < sheet.getLastRowNum(); i++) {
                    row = sheet.getRow(i);
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        row.removeCell(row.getCell(j));
                    }
                }
            }

            for (int i = 0; i < trains.size(); i++) {
                row = sheet.createRow(i + 1);
                setCell(sheet, row, 0, (double) trains.get(i).getId(), style, null);
                setCell(sheet, row, 1, trains.get(i).getOrigin(), style, null);
                setCell(sheet, row, 2, trains.get(i).getDestination(), style, null);
                setCell(sheet, row, 3, (double) trains.get(i).getMaxTrainLength(), style, null);
                setCell(sheet, row, 4, (double) trains.get(i).getMaxTrainWeight(), style, null);
                setCell(sheet, row, 5, trains.get(i).getDistance(), style, null);
                setCell(sheet, row, 6, (double) trains.get(i).getLongTrainLength(), style, null);
                setCell(sheet, row, 7, trains.get(i).getTime(), style, null);

                int j = 8;
                for (Block block : trains.get(i).getTrainBlocks()) {
                    setCell(sheet, row, j, (double) block.getId(), style, null);
                    j++;
                }
            }

            inputFile.close();
            outputFile = new FileOutputStream(new File(file));
            workbook.write(outputFile);
            outputFile.flush();
            outputFile.close();
            return ExcelSave.toString();
        } catch (FileNotFoundException e) {
            return NoFileSelected.toString();
        } catch (IOException e) {
            return FileNotFound.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        }
    }

    public boolean isThereThisOD(String origin, String destination) {
        for (Commodity commodity : commodities) {
            if (origin.equals(commodity.getOrigin()) && destination.equals(commodity.getDestination())) {
                return true;
            }
        }
        return false;
    }
}

