package company.Data;

import java.util.ArrayList;

public class Fleet {

    private long fleetKind;

    //last data for a fleet
    private int Destination;
    private int lastStation;
    private int detachStation;
    private int trainDestination;
    private int status;//0 for reach destination, 1 for reach train destination, 2 for stoped
    private int trainRecId;
    private String lastStationEnterYear="";
    private String lastStationEnterTime="";
    private String lastStationExitYear="";
    private String lastStationExitTime="";
    private int lastTimeCalculate;
    private String trainFormationYear="";
    private String trainFormationTime="";



    public Fleet(long fleetKind, int destination,
                 int lastStation, int detachStation,
                 int trainDestination, int status,
                 int trainRecId, String lastStationEnterYear,
                 String lastStationEnterTime, String lastStationExitYear,
                 String lastStationExitTime, int lastTimeCalculate,
                 String trainFormationYear, String trainFormationTime) {
        this.fleetKind = fleetKind;
        Destination = destination;
        this.lastStation = lastStation;
        this.detachStation = detachStation;
        this.trainDestination = trainDestination;
        this.status = status;
        this.trainRecId = trainRecId;
        this.lastStationEnterYear = lastStationEnterYear;
        this.lastStationEnterTime = lastStationEnterTime;
        this.lastStationExitYear = lastStationExitYear;
        this.lastStationExitTime = lastStationExitTime;
        this.lastTimeCalculate = lastTimeCalculate;
        this.trainFormationYear = trainFormationYear;
        this.trainFormationTime = trainFormationTime;
    }

    public Fleet(long fleetKind) {
        this.fleetKind = fleetKind;
    }

    public Fleet() {

    }

    public long getFleetKind() {
        return fleetKind;
    }

    public void setFleetKind(long fleetKind) {
        this.fleetKind = fleetKind;
    }

    public int getDestination() {
        return Destination;
    }

    public void setDestination(int destination) {
        Destination = destination;
    }

    public int getLastStation() {
        return lastStation;
    }

    public void setLastStation(int lastStation) {
        this.lastStation = lastStation;
    }

    public int getDetachStation() {
        return detachStation;
    }

    public void setDetachStation(int detachStation) {
        this.detachStation = detachStation;
    }

    public int getTrainDestination() {
        return trainDestination;
    }

    public void setTrainDestination(int trainDestination) {
        this.trainDestination = trainDestination;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTrainRecId() {
        return trainRecId;
    }

    public void setTrainRecId(int trainRecId) {
        this.trainRecId = trainRecId;
    }

    public String getLastStationEnterYear() {
        return lastStationEnterYear;
    }

    public void setLastStationEnterYear(String lastStationEnterYear) {
        this.lastStationEnterYear = lastStationEnterYear;
    }

    public String getLastStationEnterTime() {
        return lastStationEnterTime;
    }

    public void setLastStationEnterTime(String lastStationEnterTime) {
        this.lastStationEnterTime = lastStationEnterTime;
    }

    public String getLastStationExitYear() {
        return lastStationExitYear;
    }

    public void setLastStationExitYear(String lastStationExitYear) {
        this.lastStationExitYear = lastStationExitYear;
    }

    public String getLastStationExitTime() {
        return lastStationExitTime;
    }

    public void setLastStationExitTime(String lastStationExitTime) {
        this.lastStationExitTime = lastStationExitTime;
    }

    public int getLastTimeCalculate() {
        return lastTimeCalculate;
    }

    public void setLastTimeCalculate(int lastTimeCalculate) {
        this.lastTimeCalculate = lastTimeCalculate;
    }

    public String getTrainFormationYear() {
        return trainFormationYear;
    }

    public void setTrainFormationYear(String trainFormationYear) {
        this.trainFormationYear = trainFormationYear;
    }

    public String getTrainFormationTime() {
        return trainFormationTime;
    }

    public void setTrainFormationTime(String trainFormationTime) {
        this.trainFormationTime = trainFormationTime;
    }
}
