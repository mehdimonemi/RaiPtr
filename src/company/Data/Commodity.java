package company.Data;

import company.Data.oldOnes.Wagon;

import java.util.ArrayList;

import static company.backend1.ReadTypicalData.jonoobStations;
import static company.backend1.ReadTypicalData.manovrStations;
import static company.backend1.ReadTypicalData.Stations;


/**
 * Created by Monemi_M on 10/07/2017.
 */
public class Commodity {

    private int id;
    private String origin;
    private String destination;
    private String mainDestination;
    private int originId;
    private int destinationId;
    private int kind; //0 for empty and 1 for loaded
    private int volume;
    private String direction;
    private double priority;
    private Wagon wagon;
    private String wagonName;
    private String freight;
    ArrayList<Train> trains = new ArrayList<>();

    public Commodity(int id, double priority, String origin, String mainDestination,
                     int volume, String freight) {
        this.id = id;
        this.priority = priority;
        this.origin = origin;
        this.mainDestination = mainDestination;
        this.volume = volume;
        this.freight = freight;
        this.wagonName = wagonName;
        if (freight.equals("واگن بدون بار") || freight.equals("واگن خالي جهت تعميرات اساسي")
                || freight.equals("واگن خالي جهت تعميرات نيمه اساسي")
                || freight.equals("واگن يك طبقه و دو طبقه ولاشه واگن")) {
            this.kind = 0;
        } else {
            this.kind = 1;
        }

        boolean flag = true;
        for (String name : jonoobStations) {
            if (mainDestination.equals(name)) {
                this.destination = Stations.get(Stations.size() - 1).getName();
                flag = false;
            }
        }

        for (Station station : Stations) {
            if (mainDestination.equals(station.getName())) {
                this.destination = this.mainDestination;
                flag = false;
            }
        }

        if (mainDestination.equals("باري كرمانشاه")) {
            this.destination = "سمنگان (پالايشگاه)";
            flag = false;
        }

        if (flag) {
            this.destination = Stations.get(0).getName();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
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

    public String getMainDestination() {
        return mainDestination;
    }

    public void setMainDestination(String mainDestination) {
        this.mainDestination = mainDestination;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getOriginId() {
        return originId;
    }

    public void setOriginId() {
        for (ManovrStation manovrStation : manovrStations) {
            if (manovrStation.getName().equals(this.origin)) {
                this.originId = manovrStation.getId();
            }
        }
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId() {
        for (ManovrStation manovrStation : manovrStations) {
            if (manovrStation.getName().equals(this.destination)) {
                this.destinationId = manovrStation.getId();
            }
        }
    }

    public Wagon getWagon() {
        return wagon;
    }

    public void setWagon(Wagon wagon) {
        this.wagon = wagon;
    }

    public ArrayList<Train> getTrains() {
        return trains;
    }

    public void setTrains(ArrayList<Train> trains) {
        this.trains = trains;
    }

    public String getFreight() {
        return freight;
    }

    public void setFreight(String freight) {
        this.freight = freight;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getWagonName() {
        return wagonName;
    }

    public void setWagonName(String wagonName) {
        this.wagonName = wagonName;
    }

    public boolean hasTrain(int id) {
        for (Train train : this.trains) {
            if (id == train.getId()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Commodity{" +
                "origin=" + origin +
                "destination=" + destination +
                "wagon=" + wagon +
                '}';
    }
}
