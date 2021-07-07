package ir.rai.Backend;

import ir.rai.Data.Station;
import ir.rai.Data.Block;
import ir.rai.Data.TrainArc;
import ir.rai.Data.Wagon;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ir.rai.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Initializer {
    public void prepareData() {
        System.out.println("----------------------start trainArc-------------------");
        sql.dizelsKey = new ArrayList<>();
        sql.dizelsKey.addAll(sql.dizelListMap.keySet());
        try {
            IloCplex model = new IloCplex();
            IloNumVar[] X = new IloNumVar[sql.blockMap.size()];
            IloNumExpr goalFunction;
            IloNumExpr constraint;

            Iterator<Map.Entry<Long, Wagon>> wagonIterator = sql.wagonListMap.entrySet().iterator();
            while (wagonIterator.hasNext()) {
                Map.Entry<Long, Wagon> wagon = wagonIterator.next();
                long wagonId = wagon.getKey();
                Wagon commodity = sql.wagonListMap.get(wagonId);

                //reach to destination wagon are not consider in model
                if (commodity.getStatus() == 0) {
                    sql.addWagonToStation(wagonId, commodity.getFreight(),
                            sql.stationMap.get(commodity.getDestination()).getStationCapacity());
                    wagonIterator.remove();
                    continue;
                }
                //Moving wagon are not consider in model
                if (commodity.getStatus() == 3) {
                    sql.addWagonToStation(wagonId, commodity.getFreight(),
                            sql.stationMap.get(commodity.getLastStation()).getStationCapacity(),
                            sql.stationMap.get(commodity.getDestination()).getStationCapacity());
                    wagonIterator.remove();
                    continue;
                }

                try {
                    sql.addWagonToStation(wagonId, commodity.getFreight(),
                            sql.stationMap.get(commodity.getLastStation()).getStationCapacity(),
                            sql.stationMap.get(commodity.getDestination()).getStationCapacity());
                } catch (NullPointerException e) {
                    System.out.println("Error in adding the wagon to station");
                    wagonIterator.remove();
                    continue;
                }

                int stationA = commodity.getLastStation();
                int stationB = commodity.getDestination();
                //to decrease proses time, duplicate od will  pass but their train arc will add
                if (sql.ODTrainArcs.containsKey(stationA + "-" + stationB)) {
                    commodity.getTrainArcs().addAll(sql.ODTrainArcs.get(stationA + "-" + stationB));
                    commodity.setDistance(sql.ODDistances.get(stationA + "-" + stationB));
                    continue;
                }
                //start solving model for the commodity
                for (int i = 0; i < sql.blockMap.size(); i++) {
                    X[i] = model.numVar(0, 1, IloNumVarType.Int);
                }
                Integer[] blocksKey = sql.blockMap.keySet().toArray(new Integer[0]);
                goalFunction = model.constant(0);
                for (int i = 0; i < sql.blockMap.size(); i++) {
                    goalFunction = model.sum(goalFunction, model.prod(X[i], sql.blockMap.get(blocksKey[i]).getLengthGIS()));
                }
                model.addMinimize(goalFunction);

                // constraints
                for (int key : sql.stationMap.keySet()) {
                    constraint = model.constant(0);
                    if (key == stationA) {
                        for (int j = 0; j < sql.blockMap.size(); j++) {
                            if (stationA == sql.blockMap.get(blocksKey[j]).getStartStationID()) {
                                constraint = model.sum(constraint, X[j]);
                            }
                            if (stationA == sql.blockMap.get(blocksKey[j]).getEndStationID()) {
                                constraint = model.sum(constraint, model.negative(X[j]));
                            }
                        }
                        model.addEq(constraint, 1);
                    } else if (key == (stationB)) {
                        for (int j = 0; j < sql.blockMap.size(); j++) {
                            if (stationB == sql.blockMap.get(blocksKey[j]).getStartStationID()) {
                                constraint = model.sum(constraint, X[j]);
                            }
                            if (stationB == sql.blockMap.get(blocksKey[j]).getEndStationID()) {
                                constraint = model.sum(constraint, model.negative(X[j]));
                            }
                        }
                        model.addEq(constraint, -1);
                    } else {
                        for (int j = 0; j < sql.blockMap.size(); j++) {
                            if (key == (sql.blockMap.get(blocksKey[j]).getStartStationID())) {
                                constraint = model.sum(constraint, X[j]);
                            }
                            if (key == (sql.blockMap.get(blocksKey[j]).getEndStationID())) {
                                constraint = model.sum(constraint, model.negative(X[j]));
                            }
                        }
                        model.addEq(constraint, 0);
                    }
                }// end of constraints

                model.setOut(null);
                ArrayList<Block> tempBlocks1 = new ArrayList<>();
                if (model.solve()) {
                    if (model.getObjValue() < Formation.minimumAllowedArc) {
                        sql.removeWagonFromStation(wagonId,
                                commodity.getFreight(),
                                sql.stationMap.get(commodity.getLastStation()).getStationCapacity(),
                                sql.stationMap.get(commodity.getDestination()).getStationCapacity());
                        wagonIterator.remove();
                        model.clearModel();
                        for (int i = 0; i < sql.blockMap.size(); i++) {
                            if (X[i] != null) {
                                X[i] = null;
                            }
                        }
                        continue;
                    }

                    commodity.setDistance((long) model.getObjValue());

                    sql.wagonListMap.get(wagonId).setDistance((int) model.getObjValue());
                    for (int i = 0; i < sql.blockMap.size(); i++) {
                        if (model.getValue(X[i]) > 0.5) {
                            tempBlocks1.add(sql.blockMap.get(blocksKey[i]));
                        }
                    }

                    //sort blocks
                    int tempOrigin = stationA;
                    ArrayList<Block> tempBlocks2 = new ArrayList<>();

                    while (!tempBlocks1.isEmpty()) {
                        for (Block block : tempBlocks1) {
                            if (block.getStartStationID() == (tempOrigin)) {
                                tempBlocks2.add(block);
                                tempOrigin = block.getEndStationID();
                                tempBlocks1.remove(block);
                                break;
                            }
                        }
                    }

                    Iterator<Block> iterator = tempBlocks2.iterator();

                    Block block = iterator.next();

                    int arcStart = block.getStartStationID();
                    int arcEnd = block.getEndStationID();
                    int arcWight = block.getTrainWeight();
                    int arcLength = block.getTrainLength();
                    double arcDistance = block.getLengthGIS();
                    int arcEfficiency = block.getTrainWeight();

                    ArrayList<Integer> arcBlocks = new ArrayList<>();
                    arcBlocks.add(getBlockId(blocksKey, block));
                    //create basic trainArcs
                    while (iterator.hasNext()) {
                        block = iterator.next();
                        if (block.getTrainWeight() != arcWight) {
                            //agar block vazne na mosavi dasht traicarc ra ta sare in block ezafe mikonim
                            //bad train arc ra az abtedai in block edame midahom
                            TrainArc trainArc = new TrainArc(arcStart, arcEnd, arcWight, arcLength, arcDistance,
                                    arcEfficiency / (float) arcWight);
                            if (!sql.trainArcs.contains(trainArc)) {
                                trainArc.getBlocks().addAll(arcBlocks);
                                sql.trainArcs.add(trainArc);
                                addPossibleDizel2Arcs(trainArc, arcBlocks);
                                arcBlocks = new ArrayList<>();
                            }
                            commodity.getTrainArcs().add(sql.trainArcs.indexOf(trainArc));
                            arcBlocks.add(getBlockId(blocksKey, block));
                            arcStart = block.getStartStationID();
                            arcEnd = block.getEndStationID();
                            arcWight = block.getTrainWeight();
                            arcLength = block.getTrainLength();
                            arcDistance = block.getLengthGIS();
                            arcEfficiency = block.getTrainWeight();
                        } else if (!iterator.hasNext()) {
                            //agar mosavi bood va digar blocki nabood, train arc ra ta entehai block ezafe mikonim
                            arcBlocks.add(getBlockId(blocksKey, block));
                            arcEnd = block.getEndStationID();
                            arcDistance += block.getLengthGIS();
                            arcEfficiency = block.getTrainWeight();
                            TrainArc trainArc = new TrainArc(arcStart, arcEnd, arcWight, arcLength, arcDistance,
                                    arcEfficiency / (float) arcWight);
                            if (!sql.trainArcs.contains(trainArc)) {
                                trainArc.getBlocks().addAll(arcBlocks);
                                sql.trainArcs.add(trainArc);
                                addPossibleDizel2Arcs(trainArc, arcBlocks);
                                arcBlocks = new ArrayList<>();
                            }
                            commodity.getTrainArcs().add(sql.trainArcs.indexOf(trainArc));
                        } else {
                            //agar mosavi bood va baz block vojood dasht, block ra be train arc ezafe mikonim
                            arcBlocks.add(getBlockId(blocksKey, block));
                            arcEnd = block.getEndStationID();
                            arcDistance += block.getLengthGIS();
                            arcEfficiency = block.getTrainWeight();
                        }
                    }

                    TrainArc trainArc;
                    //create possible arcs by combining basic arcs
                    int size = commodity.getTrainArcs().size();
                    for (int i = 0; i < size; i++) {
                        arcBlocks = new ArrayList<>();
                        arcBlocks.addAll(sql.trainArcs.get(commodity.getTrainArcs().get(i)).getBlocks());
                        double tempDistance = sql.trainArcs.get(commodity.getTrainArcs().get(i)).getDistance();
                        int minWeight = sql.trainArcs.get(commodity.getTrainArcs().get(i)).getMaxWeight();
                        int minLength = sql.trainArcs.get(commodity.getTrainArcs().get(i)).getMaxLength();
                        arcEfficiency = sql.trainArcs.get(commodity.getTrainArcs().get(i)).getMaxWeight();
                        for (int j = i + 1; j < size; j++) {
                            arcBlocks.addAll(sql.trainArcs.get(commodity.getTrainArcs().get(j)).getBlocks());
                            tempDistance += sql.trainArcs.get(commodity.getTrainArcs().get(j)).getDistance();
                            minWeight = Math.min(minWeight,
                                    sql.trainArcs.get(commodity.getTrainArcs().get(j)).getMaxWeight());
                            arcEfficiency = Math.max(arcEfficiency, sql.trainArcs.get(commodity.getTrainArcs().get(j)).getMaxWeight());
                            minLength = Math.min(minLength, sql.trainArcs.get(commodity.getTrainArcs().get(j)).getMaxLength());
                            trainArc = new TrainArc(
                                    sql.trainArcs.get(commodity.getTrainArcs().get(i)).getOrigin(),
                                    sql.trainArcs.get(commodity.getTrainArcs().get(j)).getDestination(),
                                    minWeight,
                                    minLength,
                                    tempDistance,
                                    arcEfficiency / (float) minWeight
                            );
                            if (!sql.trainArcs.contains(trainArc)) {
                                trainArc.getBlocks().addAll(arcBlocks);
                                sql.trainArcs.add(trainArc);
                                addPossibleDizel2Arcs(trainArc, arcBlocks);
                            }
                            commodity.getTrainArcs().add(sql.trainArcs.indexOf(trainArc));
                        }
                    }
                    sql.ODTrainArcs.put(stationA + "-" + stationB, commodity.getTrainArcs());
                    sql.ODDistances.put(stationA + "-" + stationB, commodity.getDistance());
                } else {
                    wagonIterator.remove();
                    sql.removeWagonFromStation(wagonId,
                            commodity.getFreight(),
                            sql.stationMap.get(commodity.getLastStation()).getStationCapacity(),
                            sql.stationMap.get(commodity.getDestination()).getStationCapacity());
                    System.out.println("No trainArc for commodity: " + sql.stationMap.get(stationA).getName() +
                            "--" + stationB);
                }
                model.clearModel();
                for (int i = 0; i < sql.blockMap.size(); i++) {
                    if (X[i] != null) {
                        X[i] = null;
                    }
                }

                goalFunction = null;
                constraint = null;
            }
            System.out.println("---------------------End train Arcs-----------------------");
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public void setPriority() {
        for (Map.Entry<Integer, Station> station : sql.stationMap.entrySet()) {
            for (Map.Entry<Integer, Station.Capacity> freight : station.getValue().getStationCapacity().entrySet()) {
                if (10 * freight.getValue().cap <= (freight.getValue().comingWagons.size() +
                        freight.getValue().stationWagon.size())) {
                    for (Long wagonId : freight.getValue().comingWagons) {
                        try {
                            sql.wagonListMap.get(wagonId).setPriority(0);
                        } catch (NullPointerException ignored) {
                            System.out.println("could not set priority for wagon: " + wagonId);
                        }
                    }
                } else {
                    for (Long wagonId : freight.getValue().comingWagons) {
                        try {
                            sql.wagonListMap.get(wagonId).setPriority((float)
                                    (10*(Math.max(sql.wagonListMap.get(wagonId).getDistance() / 20 / 24, 1) * freight.getValue().cap) -
                                            (freight.getValue().comingWagons.size() + freight.getValue().stationWagon.size())));

                            if (sql.wagonListMap.get(wagonId).getPriority() > Wagon.maxPriority) {
                                Wagon.maxPriority = sql.wagonListMap.get(wagonId).getPriority();
                            }
                        } catch (NullPointerException ignored) {
                            System.out.println("could not set priority for wagon: " + wagonId);
                        }
                    }
                }
            }
        }

//        for (Map.Entry<Long, newWagon> wagon : wagonListMap.entrySet()) {
//            wagon.getValue().setPriority(wagon.getValue().getPriority() / newWagon.maxPriority * 100);
//        }
    }

    private void addPossibleDizel2Arcs(TrainArc trainArc, ArrayList<Integer> arcBlocks) {
        for (Integer key : sql.dizelsKey) {
            int powerOnArc = 100000;
            if (sql.dizelListMap.get(key).getAllowedBlock().keySet().containsAll(arcBlocks)) {
                for (Integer block : arcBlocks) {
                    int a = sql.dizelListMap.get(key).getAllowedBlock().get(block);
                    powerOnArc = Math.min(powerOnArc, a);
                }
                sql.dizelListMap.get(key).getTrainArcs().put(sql.trainArcs.indexOf(trainArc), powerOnArc);
            }
        }
    }

    private boolean dizelHaveStation(Integer dizelkey, int station) {
        for (Integer trainArc : sql.dizelListMap.get(dizelkey).getTrainArcs().keySet()) {
            if (sql.trainArcs.get(trainArc).getOrigin() == station)
                return true;
            if (sql.trainArcs.get(trainArc).getDestination() == station)
                return true;
        }
        for (Integer block : sql.dizelListMap.get(dizelkey).getAllowedBlock().keySet()) {
            if (sql.blockMap.get(block).getStartStationID() == station)
                return true;
            if (sql.blockMap.get(block).getEndStationID() == station)
                return true;
        }
        return false;
    }

    private Integer getBlockId(Integer[] blocksKey, Block block) {
        for (Integer key : blocksKey) {
            if (sql.blockMap.get(key).equals(block))
                return key;
        }
        return null;
    }

}
