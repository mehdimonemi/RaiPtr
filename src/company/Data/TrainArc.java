package company.Data;

import static company.backend1.ReadTypicalData.manovrStations;

/**
 * Created by Monemi_M on 03/11/2018.
 */
public class TrainArc {
    private int id = 0;
    private String origin;
    private String destination;
    private int originId;
    private int destinationId;
    private int trainId;
    private int weight;
    private int maxWeight;
    private double time;

    public TrainArc(int id, String origin, String destination, int train, int weight,int maxWeight, double time) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        setOriginId();
        setDestinationId();
        this.trainId = train;
        this.weight = weight;
        this.maxWeight = maxWeight;
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

    public int getTrainId() {
        return trainId;
    }

    public void setTrainId(int trainId) {
        this.trainId = trainId;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(int maxWeight) {
        this.maxWeight = maxWeight;
    }

    public boolean IsNeighborTrainArcs(TrainArc trainArc) {
        if (this.getDestinationId() == trainArc.getOriginId() &&
                this.getTrainId() == trainArc.getTrainId() &&
                ((this.destinationId > this.originId && trainArc.destinationId > trainArc.originId) ||
                        (this.originId > this.destinationId && trainArc.originId > trainArc.destinationId))) {
            return true;
        }
        return false;
    }
}
