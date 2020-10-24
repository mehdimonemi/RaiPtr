package company.Data;

import company.Data.oldOnes.Block;

import java.util.ArrayList;

import static company.backend1.ReadTypicalData.manovrStations;

/**
 * Created by Monemi_M on 10/04/2017.
 */
public class Train {

    private int id;
    private String origin;
    private String destination;
    private int originId;
    private int destinationId;
    private int maxTrainLength;
    private int maxTrainWeight;
    private ArrayList<Block> trinBlocks = new ArrayList<>();
    private double distance;
    private int totalWagonOnTrain;
    private int longTrainLength;
    private double time;
    private double priority;

    public Train(int id, String origin, String destination, int maxTrainLength,
                 int maxTrainForce, double distance, int longTrainLength, double time, ArrayList<Block> trinBlocks) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        setOriginId();
        setDestinationId();
        this.maxTrainLength = maxTrainLength;
        this.maxTrainWeight = maxTrainForce;
        this.trinBlocks = trinBlocks;
        this.distance = distance;
        this.longTrainLength = longTrainLength;
        this.time = time;
    }

    public Train(int id, String origin, String destination, int maxTrainLength,
                 int maxTrainForce, double distance, int longTrainLength, double time) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        setOriginId();
        setDestinationId();
        this.maxTrainLength = maxTrainLength;
        this.maxTrainWeight = maxTrainForce;
        this.distance = distance;
        this.longTrainLength = longTrainLength;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getMaxTrainLength() {
        return maxTrainLength;
    }

    public void setMaxTrainLength(int maxTrainLength) {
        this.maxTrainLength = maxTrainLength;
    }

    public int getMaxTrainWeight() {
        return maxTrainWeight;
    }

    public void setMaxTrainWeight(int maxTrainWeight) {
        this.maxTrainWeight = maxTrainWeight;
    }

    public ArrayList<Block> getTrainBlocks() {
        return trinBlocks;
    }

    public void setTrainBlocks(ArrayList<Block> trinBlocks) {
        this.trinBlocks = trinBlocks;
    }

    public int getOriginId() {
        return originId;
    }

    public void setOriginId() {
        for (ManovrStation station : manovrStations) {
            if (station.getName().equals(this.origin)) {
                this.originId = station.getId();
            }
        }
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId() {
        for (ManovrStation station : manovrStations) {
            if (station.getName().equals(this.destination)) {
                this.destinationId = station.getId();
            }
        }
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getTotalWagonOnTrain() {
        return totalWagonOnTrain;
    }

    public void setTotalWagonOnTrain(int totalWagonOnTrain) {
        this.totalWagonOnTrain = totalWagonOnTrain;
    }

    public int getLongTrainLength() {
        return longTrainLength;
    }

    public void setLongTrainLength(int longTrainLength) {
        this.longTrainLength = longTrainLength;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Train that = (Train) o;

        return priority == that.priority;
    }
}
