package company.Data;

public class newBlock {

    public static int maxBlockId;
    private int startStationID;
    private int endStationID;
    private int length;
    private double lengthGIS;
    private int train;
    private float timeTravel;
    private int trainLength;
    private int trainWeight;

    public newBlock(int startStationID, int endStationID, int length, double lengthGIS, int train, float timeTravel, int trainLength, int trainWeight) {
        this.startStationID = startStationID;
        this.endStationID = endStationID;
        this.length = length;
        this.lengthGIS = lengthGIS;
        this.train = train;
        this.timeTravel = timeTravel;
        this.trainLength = trainLength;
        this.trainWeight = trainWeight;
    }

    public int getStartStationID() {
        return startStationID;
    }

    public void setStartStationID(int startStationID) {
        this.startStationID = startStationID;
    }

    public int getEndStationID() {
        return endStationID;
    }

    public void setEndStationID(int endStationID) {
        this.endStationID = endStationID;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getTrain() {
        return train;
    }

    public void setTrain(int train) {
        this.train = train;
    }

    public float getTimeTravel() {
        return timeTravel;
    }

    public void setTimeTravel(float timeTravel) {
        this.timeTravel = timeTravel;
    }

    public int getTrainLength() {
        return trainLength;
    }

    public void setTrainLength(int trainLength) {
        this.trainLength = trainLength;
    }

    public int getTrainWeight() {
        return trainWeight;
    }

    public void setTrainWeight(int trainWeight) {
        this.trainWeight = trainWeight;
    }

    public double getLengthGIS() {
        return lengthGIS;
    }

    public void setLengthGIS(double lengthGIS) {
        this.lengthGIS = lengthGIS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        newBlock newBlock = (company.Data.newBlock) o;

        if (startStationID != newBlock.startStationID) return false;
        if (endStationID != newBlock.endStationID) return false;
        return length == newBlock.length;
    }

    @Override
    public int hashCode() {
        int result = startStationID;
        result = 31 * result + endStationID;
        result = 31 * result + length;
        return result;
    }
}
