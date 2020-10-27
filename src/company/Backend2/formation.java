package company.Backend2;

import company.Data.*;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import org.apache.poi.EmptyFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.awt.*;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static company.Data.newBlock.maxBlockId;
import static company.sql.*;

public class formation {
    public static int minimumAllowedArc = 20;
    public static XSSFCell cell;
    public static XSSFRow row;

    IloCplex model;
    IloNumVar[][] x;
    IloNumVar[] s;
    IloNumVar[][][] l;
    IloNumVar[][][] lAlone;
    IloNumVar[] y;

    ArrayList<Integer> stationsKey;
    ArrayList<Long> wagonsKey;
    ArrayList<Integer> dizelsKey;

    int locoTrip = 3;

    public static ArrayList<TrainArc> trainArcs = new ArrayList<>();
    public static HashMap<String, ArrayList<Integer>> od = new HashMap<>();

    public void prepareData() {
        System.out.println("start trainArc");
        dizelsKey = new ArrayList<>();
        dizelsKey.addAll(dizelListMap.keySet());
        try {
            IloCplex model = new IloCplex();
            IloNumVar[] X = new IloNumVar[blockMap.size()];
            IloNumExpr goalFunction;
            IloNumExpr constraint;

            Iterator<Map.Entry<Long, newWagon>> wagonIterator = wagonListMap.entrySet().iterator();
            while (wagonIterator.hasNext()) {
                Map.Entry<Long, newWagon> wagon = wagonIterator.next();
                long x = wagon.getKey();
                newWagon commodity = wagonListMap.get(x);
                int stationA = commodity.getLastStation();
                int stationB = commodity.getDestination();
                //stop and reach to destination wagon are not consider in model
                if (commodity.getStatus() == 0 || commodity.getStatus() == 3) {
                    continue;
                }
                //to decrease proses time, duplicate od will  pass but their train arc will add
                if (od.containsKey(stationA + "-" + stationB)) {
                    commodity.getTrainArcs().addAll(od.get(stationA + "-" + stationB));
                    continue;
                }
                //start solving model for the commodity
                for (int i = 0; i < blockMap.size(); i++) {
                    X[i] = model.numVar(0, 1, IloNumVarType.Int);
                }
                Integer[] blocksKey = blockMap.keySet().toArray(new Integer[0]);
                goalFunction = model.constant(0);
                for (int i = 0; i < blockMap.size(); i++) {
                    goalFunction = model.sum(goalFunction, model.prod(X[i], blockMap.get(blocksKey[i]).getLengthGIS()));
                }
                model.addMinimize(goalFunction);

                // constraints
                for (int key : stationMap.keySet()) {
                    constraint = model.constant(0);
                    if (key == stationA) {
                        for (int j = 0; j < blockMap.size(); j++) {
                            if (stationA == blockMap.get(blocksKey[j]).getStartStationID()) {
                                constraint = model.sum(constraint, X[j]);
                            }
                            if (stationA == blockMap.get(blocksKey[j]).getEndStationID()) {
                                constraint = model.sum(constraint, model.negative(X[j]));
                            }
                        }
                        model.addEq(constraint, 1);
                    } else if (key == (stationB)) {
                        for (int j = 0; j < blockMap.size(); j++) {
                            if (stationB == blockMap.get(blocksKey[j]).getStartStationID()) {
                                constraint = model.sum(constraint, X[j]);
                            }
                            if (stationB == blockMap.get(blocksKey[j]).getEndStationID()) {
                                constraint = model.sum(constraint, model.negative(X[j]));
                            }
                        }
                        model.addEq(constraint, -1);
                    } else {
                        for (int j = 0; j < blockMap.size(); j++) {
                            if (key == (blockMap.get(blocksKey[j]).getStartStationID())) {
                                constraint = model.sum(constraint, X[j]);
                            }
                            if (key == (blockMap.get(blocksKey[j]).getEndStationID())) {
                                constraint = model.sum(constraint, model.negative(X[j]));
                            }
                        }
                        model.addEq(constraint, 0);
                    }
                } // end of constraints

                model.setOut(null);
                ArrayList<newBlock> tempBlocks1 = new ArrayList<>();
                if (model.solve()) {
                    if (model.getObjValue() < minimumAllowedArc) {
                        wagonIterator.remove();
                        model.clearModel();
                        for (int i = 0; i < blockMap.size(); i++) {
                            if (X[i] != null) {
                                X[i] = null;
                            }
                        }

                        continue;
                    }

                    wagonListMap.get(x).setDistance((int) model.getObjValue());
                    for (int i = 0; i < blockMap.size(); i++) {
                        if (model.getValue(X[i]) > 0.5) {
                            tempBlocks1.add(blockMap.get(blocksKey[i]));
                        }
                    }

                    //sort blocks
                    int tempOrigin = stationA;
                    ArrayList<newBlock> tempBlocks2 = new ArrayList<>();

                    while (!tempBlocks1.isEmpty()) {
                        for (newBlock block : tempBlocks1) {
                            if (block.getStartStationID() == (tempOrigin)) {
                                tempBlocks2.add(block);
                                tempOrigin = block.getEndStationID();
                                tempBlocks1.remove(block);
                                break;
                            }
                        }
                    }

                    Iterator<newBlock> iterator = tempBlocks2.iterator();

                    newBlock block = iterator.next();

                    int arcStart = block.getStartStationID();
                    int arcEnd = block.getEndStationID();
                    int arcWight = block.getTrainWeight();
                    int arcLength = block.getTrainLength();
                    int arcDistance = block.getLength();
                    int arcEfficiency = block.getTrainWeight();

                    ArrayList<Integer> arcBlocks = new ArrayList<>();
                    arcBlocks.add(getBlockId(blocksKey, block));
                    //create basic trainArcs
                    while (iterator.hasNext()) {
                        block = (newBlock) iterator.next();
                        if (block.getTrainWeight() != arcWight) {
                            //agar block vazne na mosavi dasht traicarc ra ta sare in block ezafe mikonim
                            //bad train arc ra az abtedai in block edame midahom
                            TrainArc trainArc = new TrainArc(arcStart, arcEnd, arcWight, arcLength, arcDistance,
                                    arcEfficiency / (float) arcWight);
                            if (!trainArcs.contains(trainArc)) {
                                trainArc.getBlocks().addAll(arcBlocks);
                                trainArcs.add(trainArc);
                                addPossibleDizel2Arcs(trainArc, arcBlocks);
                                arcBlocks = new ArrayList<>();
                            }
                            commodity.getTrainArcs().add(trainArcs.indexOf(trainArc));
                            arcBlocks.add(getBlockId(blocksKey, block));
                            arcStart = block.getStartStationID();
                            arcEnd = block.getEndStationID();
                            arcWight = block.getTrainWeight();
                            arcLength = block.getTrainLength();
                            arcDistance = block.getLength();
                            arcEfficiency = block.getTrainWeight();
                        } else if (!iterator.hasNext()) {
                            //agar mosavi bood va digar blocki nabood, train arc ra ta entehai block ezafe mikonim
                            arcBlocks.add(getBlockId(blocksKey, block));
                            arcEnd = block.getEndStationID();
                            arcDistance += block.getLength();
                            arcEfficiency = block.getTrainWeight();
                            TrainArc trainArc = new TrainArc(arcStart, arcEnd, arcWight, arcLength, arcDistance,
                                    arcEfficiency / (float) arcWight);
                            if (!trainArcs.contains(trainArc)) {
                                trainArc.getBlocks().addAll(arcBlocks);
                                trainArcs.add(trainArc);
                                addPossibleDizel2Arcs(trainArc, arcBlocks);
                                arcBlocks = new ArrayList<>();
                            }
                            commodity.getTrainArcs().add(trainArcs.indexOf(trainArc));
                        } else {
                            //agar mosavi bood va baz block vojood dasht, block ra be train arc ezafe mikonim
                            arcBlocks.add(getBlockId(blocksKey, block));
                            arcEnd = block.getEndStationID();
                            arcDistance += block.getLength();
                            arcEfficiency = block.getTrainWeight();
                        }
                    }

                    TrainArc trainArc;
                    //create possible arcs by combining basic arcs
                    int size = commodity.getTrainArcs().size();
                    for (int i = 0; i < size; i++) {
                        arcBlocks = new ArrayList<>();
                        arcBlocks.addAll(trainArcs.get(commodity.getTrainArcs().get(i)).getBlocks());
                        int tempDistance = trainArcs.get(commodity.getTrainArcs().get(i)).getDistance();
                        int minWeight = trainArcs.get(commodity.getTrainArcs().get(i)).getMaxWeight();
                        int minLength = trainArcs.get(commodity.getTrainArcs().get(i)).getMaxLength();
                        arcEfficiency = trainArcs.get(commodity.getTrainArcs().get(i)).getMaxWeight();
                        for (int j = i + 1; j < size; j++) {
                            arcBlocks.addAll(trainArcs.get(commodity.getTrainArcs().get(j)).getBlocks());
                            tempDistance += trainArcs.get(commodity.getTrainArcs().get(j)).getDistance();
                            minWeight = Math.min(minWeight,
                                    trainArcs.get(commodity.getTrainArcs().get(j)).getMaxWeight());
                            arcEfficiency = Math.max(arcEfficiency, trainArcs.get(commodity.getTrainArcs().get(j)).getMaxWeight());
                            minLength = Math.min(minLength, trainArcs.get(commodity.getTrainArcs().get(j)).getMaxLength());
                            trainArc = new TrainArc(
                                    trainArcs.get(commodity.getTrainArcs().get(i)).getOrigin(),
                                    trainArcs.get(commodity.getTrainArcs().get(j)).getDestination(),
                                    minWeight,
                                    minLength,
                                    tempDistance,
                                    arcEfficiency / (float) minWeight
                            );
                            if (!trainArcs.contains(trainArc)) {
                                trainArc.getBlocks().addAll(arcBlocks);
                                trainArcs.add(trainArc);
                                addPossibleDizel2Arcs(trainArc, arcBlocks);
                            }
                            commodity.getTrainArcs().add(trainArcs.indexOf(trainArc));
                        }
                    }
                    od.put(stationA + "-" + stationB, commodity.getTrainArcs());
                } else {
                    System.out.println("No trainArc for commodity: " + stationMap.get(stationA).getName() +
                            "--" + stationB);
                }
                model.clearModel();
                for (int i = 0; i < blockMap.size(); i++) {
                    if (X[i] != null) {
                        X[i] = null;
                    }
                }

                goalFunction = null;
                constraint = null;
            }
            System.out.println("end train Arcs");
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void addPossibleDizel2Arcs(TrainArc trainArc, ArrayList<Integer> arcBlocks) {
        for (Integer key : dizelsKey) {
            int powerOnArc = 100000;
            if (dizelListMap.get(key).getAllowedBlock().keySet().containsAll(arcBlocks)) {
                for (Integer block : arcBlocks) {
                    int a = dizelListMap.get(key).getAllowedBlock().get(block);
                    powerOnArc = Math.min(powerOnArc, a);
                }
                dizelListMap.get(key).getTrainArcs().put(trainArcs.indexOf(trainArc), powerOnArc);
            }
        }
    }

    private Integer getBlockId(Integer[] blocksKey, newBlock block) {
        for (Integer key : blocksKey) {
            if (blockMap.get(key).equals(block))
                return key;

        }
        return null;
    }

    public void model() throws IloException {
        model = new IloCplex();

        model.setParam(IloCplex.Param.TimeLimit, 180);

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
            s[wagonsKey.indexOf(wagonKey)] = model.numVar(0, 1, IloNumVarType.Int);
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
                goalFunction = model.sum(goalFunction, model.prod(x[wagonsKey.indexOf(wagonKey)][trainArc], 2));
            }
        }

        for (Integer dizelKey : dizelsKey) {
            for (Integer trainArc : dizelListMap.get(dizelKey).getTrainArcs().keySet()) {
                for (int i = 0; i < locoTrip; i++) {
                    goalFunction = model.sum(goalFunction, model.prod
                            (l[dizelsKey.indexOf(dizelKey)][trainArc][i],
                                    -trainArcs.get(trainArc).getDistance() / 100.0));
                }
            }
        }
        for (Integer dizelKey : dizelsKey) {
            for (Integer blockId : dizelListMap.get(dizelKey).getAllowedBlock().keySet()) {
                for (int i = 0; i < locoTrip; i++) {goalFunction = model.sum(goalFunction, model.prod
                        (lAlone[dizelsKey.indexOf(dizelKey)][blockId][i],
                                100));
                }
            }
        }

        //cost of train formation
        for (int i = 0; i < trainArcs.size(); i++) {
            goalFunction = model.sum(goalFunction, model.prod(y[i], 1000));
        }

        //transport maximum of wagons
        for (Long wagonKey : wagonsKey) {
            goalFunction = model.sum(goalFunction, model.negative(model.prod(s[wagonsKey.indexOf(wagonKey)],
                    1000)));
        }
        model.addMinimize(goalFunction);

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

        System.out.println("build");
        if (model.solve()) {
            System.out.println("well");

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
            getOutputTrains("out.xlsx");
            getDizelMoves("out.xlsx");
            getOutputDizels("out.xlsx");
            getOutputWagons("out.xlsx");
            try {
                Desktop.getDesktop().open(new File("out.xlsx"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.print("finished output");
        }
    }

    private boolean DizelHasArc(Integer dizel, int trainarc) {
        for (Integer dizelArc : dizelListMap.get(dizel).getTrainArcs().keySet()) {
            if (trainarc == dizelArc)
                return true;
        }
        return false;
    }

    private boolean dizelHaveStation(Integer dizelkey, int station) {
        for (Integer trainArc : dizelListMap.get(dizelkey).getTrainArcs().keySet()) {
            if (trainArcs.get(trainArc).getOrigin() == station)
                return true;
            if (trainArcs.get(trainArc).getDestination() == station)
                return true;
        }
        for (Integer block : dizelListMap.get(dizelkey).getAllowedBlock().keySet()) {
            if (blockMap.get(block).getStartStationID() == station)
                return true;
            if (blockMap.get(block).getEndStationID() == station)
                return true;
        }
        return false;
    }

    public void getOutputTrains(String formationFilePath) {
        System.out.println("start output");
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
                                        if (!firstRowIsWrite) {
                                            setCell(row, 0, (float) j, style1, bodyColor);
                                            setCell(row, 1, stationMap.get(trainArcs.get(j).getOrigin()).getNahieh(),
                                                    style1, bodyColor);
                                            setCell(row, 2, stationMap.get(trainArcs.get(j).getOrigin()).getName(),
                                                    style1, bodyColor);
                                            setCell(row, 3, stationMap.get(
                                                    trainArcs.get(j).getDestination()).getName(), style1, bodyColor);
                                            setCell(row, 4, trainArcs.get(j).getRealWagon(), style1, bodyColor);
                                            setCell(row, 5, trainArcs.get(j).getRealLength(), style1, bodyColor);
                                            setCell(row, 6, trainArcs.get(j).getRealWeight(), style1, bodyColor);
                                            setCell(row, 7, (float) model.getValue(y[j]), style1, bodyColor);
                                            setCell(row, 8, trainArcs.get(j).getMaxWeight(), style1, bodyColor);
                                            setCell(row, 9, trainArcs.get(j).getMaxLength(), style1, bodyColor);
                                            setCell(row, 10, trainArcs.get(j).getArcEfficiency(), style1, bodyColor);
                                            firstRowIsWrite = true;
                                        } else {
                                            setCell(row, 0, (float) j, style1, bodyColor);
                                            setCell(row, 1, stationMap.get(
                                                    trainArcs.get(j).getOrigin()).getNahieh(), style1, bodyColor);
                                            setCell(row, 2, stationMap.get(
                                                    trainArcs.get(j).getOrigin()).getName(), style1, bodyColor);
                                            setCell(row, 3, stationMap.get(
                                                    trainArcs.get(j).getDestination()).getName(), style1, bodyColor);
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

            row = sheet1.createRow(1);
            XSSFCellStyle style1 = setStyle(workBook, "B Zar");
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
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);
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


            outFile = new FileOutputStream(new File(formationFilePath));
            workBook.write(outFile);

            outFile.flush();
            outFile.close();
            workBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getOutputWagons(String formationFilePath) {
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
            setCell(row, 3, "تعداد", style1, headingColor);

            XSSFColor bodyColor;
            int rowCounter = 2;
            Random random = new Random();
            for (Map.Entry<Integer, Station> station : stationMap.entrySet()) {
                boolean isFirstRow = true;
                //choose color
                Color color = new Color(
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200,
                        random.nextInt(255 - 200) + 200);
                bodyColor = new XSSFColor(color);
                int temp = rowCounter;
                ArrayList<Integer> checkedFreight = new ArrayList<>();
                for (Long wagon1 : station.getValue().getStationWagon()) {
                    int wagonCount = 0;
                    if (wagonListMap.containsKey(wagon1)) {//some wagons would be deleted cause of no trainArc
                        if (!checkedFreight.contains(wagonListMap.get(wagon1).getFreight())) {
                            wagonCount++;
                            for (Long wagon2 : station.getValue().getStationWagon()) {
                                if (wagonListMap.containsKey(wagon2)) {//some wagons would be deleted cause of no trainArc
                                    if (wagon1 != wagon2 &&
                                            wagonListMap.get(wagon1).getFreight() ==
                                                    wagonListMap.get(wagon2).getFreight()) {
                                        wagonCount++;
                                    }
                                }
                            }
                            if (wagonCount > 0) {
                                row = sheet1.createRow(rowCounter);
                                setCell(row, 0,nahiehtMap.get(station.getValue().getNahieh()), style1, bodyColor);
                                setCell(row, 1, station.getValue().getName(), style1, bodyColor);
                                setCell(row, 2, freightMap.get(wagonListMap.get(wagon1).getFreight()), style1, bodyColor);
                                setCell(row, 3, wagonCount, style1, bodyColor);
                                rowCounter++;
                            }
                            checkedFreight.add(wagonListMap.get(wagon1).getFreight());
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setCell(XSSFRow row, int columnId, String value, XSSFCellStyle style, XSSFColor color) {
        if (color != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }
        cell = row.createCell(columnId);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static void setCell(XSSFRow row, int columnId, float value, XSSFCellStyle style, XSSFColor color) {
        if (color != null) {
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(color);
        }
        cell = row.createCell(columnId);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    public static XSSFCellStyle setStyle(XSSFWorkbook workbook, String fontName) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setCharSet(FontCharset.ARABIC);
        font.setFontName(fontName);
        style.setFont(font);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

}
