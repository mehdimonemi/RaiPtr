package company.Data;

import java.util.ArrayList;

public class TrainArc {
    private int origin;
    private int destination;
    private int maxWeight;
    private int maxLength;
    private double distance;

    private float realWeight;
    private float realLength;
    private float realWagon;
    private float arcEfficiency;

    private ArrayList<Integer> blocks = new ArrayList<>();

    public TrainArc(int origin, int destination, int maxWeight, int maxLength, double distance, float arcEfficiency) {
        this.origin = origin;
        this.destination = destination;
        this.maxWeight = maxWeight;
        this.maxLength = maxLength;
        this.distance = distance;
        this.arcEfficiency = arcEfficiency;
    }

    public ArrayList getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList blocks) {
        this.blocks = blocks;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public float getRealWeight() {
        return realWeight;
    }

    public void setRealWeight(float realWeight) {
        this.realWeight = realWeight;
    }

    public float getRealLength() {
        return realLength;
    }

    public void setRealLength(float realLength) {
        this.realLength = realLength;
    }

    public float getRealWagon() {
        return realWagon;
    }

    public void setRealWagon(float realWagon) {
        this.realWagon = realWagon;
    }

    public float getArcEfficiency() {
        return arcEfficiency;
    }

    public void setArcEfficiency(float arcEfficiency) {
        this.arcEfficiency = arcEfficiency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrainArc trainArc = (TrainArc) o;

        if (origin != trainArc.origin) return false;
        if (destination != trainArc.destination) return false;
        else return (maxWeight == trainArc.maxWeight);
    }

    @Override
    public int hashCode() {
        int result = origin;
        result = 31 * result + destination;
        result = 31 * result + maxWeight;
        return result;
    }
}
