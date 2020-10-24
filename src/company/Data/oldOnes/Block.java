package company.Data.oldOnes;

import company.Data.ManovrStation;
import company.Data.Station;

import static company.backend1.ReadTypicalData.Stations;

/**
 * Created by Monemi_M on 10/07/2017.
 */
public class Block {
    private int id;
    private String origin;
    private String destination;
    private int originId;
    private int destinationId;
    private int capacity;
    private int maxTrainLength;
    private int length;
    private double time;
    private int loco;
    private int train;
    private int ascent;


    public Block(int id, String origin, String destination,
                 int capacity, int maxTrainLength, int ascent, int length, double time) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        setOriginId();
        setDestinationId();
        this.capacity = capacity;
        this.maxTrainLength = maxTrainLength;
        this.ascent = ascent;
        this.length = length;
        this.time = time;
    }


    public Block(int id, String origin, String destination, int originId, int destinationId) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.originId = originId;
        this.destinationId = destinationId;
    }

    public Block(int id, ManovrStation origin, ManovrStation destination, int capacity,
                 int maxTrainLength, int ascent, int length, double time) {
        this.id = id;
        this.origin = origin.getName();
        this.originId = origin.getId();
        this.destination = destination.getName();
        this.destinationId = destination.getId();
        this.capacity = capacity;
        this.maxTrainLength = maxTrainLength;
        this.ascent = ascent;
        this.length = length;
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
        for (Station station : Stations) {
            if (station.getName().equals(this.origin)) {
                this.originId = station.getId();
            }
        }
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId() {
        for (Station station : Stations) {
            if (station.getName().equals(this.destination)) {
                this.destinationId = station.getId();
            }
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMaxTrainLength() {
        return maxTrainLength;
    }

    public void setMaxTrainLength(int maxTrainLength) {
        this.maxTrainLength = maxTrainLength;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getLoco() {
        return loco;
    }

    public void setLoco(int loco) {
        this.loco = loco;
    }

    public int getTrain() {
        return train;
    }

    public void setTrain(int train) {
        this.train = train;
    }

    public int getAscent() {
        return ascent;
    }

    public void setAscent(int ascent) {
        this.ascent = ascent;
    }
}