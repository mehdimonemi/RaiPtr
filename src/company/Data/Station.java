package company.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Monemi_M on 10/07/2017.
 */
public class Station {

    public static int maxStationID;

    private int id;
    private String name;
    private int nahieh;
    private double capacity;
    private ArrayList<Long> stationWagon;
    private HashMap<TimeHashMapKey, WagonTypeTime> timeHashMap;

    public Station(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Station(String name, int wagonType, int barKind, int wagonTransferNumber, float totalWagonStopTime) {
        this.name = name;
        this.timeHashMap = new HashMap<>();
        this.timeHashMap.put(new TimeHashMapKey(wagonType, barKind), new WagonTypeTime(wagonTransferNumber, totalWagonStopTime));
    }

    public Station(String name, int nahieh) {
        this.name = name;
        this.nahieh = nahieh;
        this.stationWagon= new ArrayList<>();
    }

    public int getNahieh() {
        return nahieh;
    }

    public void setNahieh(int nahieh) {
        this.nahieh = nahieh;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Long> getStationWagon() {
        return stationWagon;
    }

    public void setStationWagon(ArrayList<Long> stationWagon) {
        this.stationWagon = stationWagon;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public HashMap<TimeHashMapKey, WagonTypeTime> getTimeHashMap() {
        return timeHashMap;
    }

    public void setTimeHashMap(HashMap<TimeHashMapKey, WagonTypeTime> timeHashMap) {
        this.timeHashMap = timeHashMap;
    }

    public static class WagonTypeTime {
        private int wagonTransferNumber = 0;
        private float totalWagonStopTime = 0;

        public WagonTypeTime(int wagonTransferNumber, float totalWagonStopTime) {
            this.wagonTransferNumber = wagonTransferNumber;
            this.totalWagonStopTime = totalWagonStopTime;
        }

        public int getWagonTransferNumber() {
            return wagonTransferNumber;
        }

        public void setWagonTransferNumber(int wagonTransferNumber) {
            this.wagonTransferNumber = wagonTransferNumber;
        }

        public float getTotalWagonStopTime() {
            return totalWagonStopTime;
        }

        public void setTotalWagonStopTime(float totalWagonStopTime) {
            this.totalWagonStopTime = totalWagonStopTime;
        }

    }

    public static class TimeHashMapKey {
        private int wagonType;
        private int barKind;

        public TimeHashMapKey(int wagonType, int barKind) {
            this.wagonType = wagonType;
            this.barKind = barKind;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TimeHashMapKey that = (TimeHashMapKey) o;

            return (wagonType == that.wagonType) && (barKind == that.barKind);
        }

        @Override
        public int hashCode() {
            int result = wagonType;
            result = 31 * result + barKind;
            return result;
        }

        public int getWagonType() {
            return wagonType;
        }

        public void setWagonType(int wagonType) {
            this.wagonType = wagonType;
        }

        public int getBarKind() {
            return barKind;
        }

        public void setBarKind(int barKind) {
            this.barKind = barKind;
        }
    }
}
