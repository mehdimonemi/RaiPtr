package company.Data;

import java.util.ArrayList;

public class newWagon extends Fleet {

    public static float maxPriority;

    private int wagonType;
    private float wagonLength;
    private float emptyWeight;
    private float FullWeight;
    private int freight;
    private float distance;
    private float priority=0;
    private ArrayList<Integer> trainArcs= new ArrayList<>();

    public newWagon(long fleetKind, int freight, int destination, int lastStation, int detachStation, int trainDestination, int status, int trainRecId, String lastStationEnterYear, String lastStationEnterTime, String lastStationExitYear, String lastStationExitTime, int lastTimeCalculate, String trainFormationYear, String trainFormationTime, int wagonType, float wagonLength, float emptyWeight, float fullWeight) {
        super(fleetKind, destination, lastStation, detachStation, trainDestination, status, trainRecId, lastStationEnterYear, lastStationEnterTime, lastStationExitYear, lastStationExitTime, lastTimeCalculate, trainFormationYear, trainFormationTime);
        this.wagonType = wagonType;
        this.wagonLength = wagonLength;
        this.emptyWeight = emptyWeight;
        this.FullWeight = fullWeight;
        this.freight = freight;
    }

    public ArrayList<Integer> getTrainArcs() {
        return trainArcs;
    }

    public void setTrainArcs(ArrayList<Integer> trainArcs) {
        this.trainArcs = trainArcs;
    }

    public newWagon(int fleetKind) {
        super(fleetKind);
    }

    public int getWagonType() {
        return wagonType;
    }

    public void setWagonType(int wagonType) {
        this.wagonType = wagonType;
    }

    public float getWagonLength() {
        return wagonLength;
    }

    public void setWagonLength(float wagonLength) {
        this.wagonLength = wagonLength;
    }

    public float getEmptyWeight() {
        return emptyWeight;
    }

    public void setEmptyWeight(float emptyWeight) {
        this.emptyWeight = emptyWeight;
    }

    public float getFullWeight() {
        return FullWeight;
    }

    public void setFullWeight(float fullWeight) {
        FullWeight = fullWeight;
    }

    public int getFreight() {
        return freight;
    }

    public void setFreight(int freight) {
        this.freight = freight;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getPriority() {
        return priority;
    }

    public void setPriority(float priority) {
        this.priority = priority;
    }
}
