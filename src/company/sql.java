package company;

import company.Backend2.TrainArc;
import company.Data.Dizel;
import company.Data.Station;
import company.Data.newBlock;
import company.Data.newWagon;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static company.Data.Station.maxStationID;
import static company.Data.newBlock.maxBlockId;
import static sun.swing.MenuItemLayoutHelper.max;

public class sql {
    public static HashMap<Integer, Station> stationMap = new HashMap<>();
    public static HashMap<Integer, newBlock> blockMap = new HashMap<>();
    public static HashMap<Long, newWagon> wagonListMap = new HashMap<>();
    public static HashMap<Integer, Dizel> dizelListMap = new HashMap<>();
    public static HashMap<Integer, String> freightMap = new HashMap<>();
    public static HashMap<Integer, String> nahiehtMap = new HashMap<>();
    public static HashMap<Integer, String> wagonType = new HashMap<>();
    public static ArrayList<TrainArc> trainArcs = new ArrayList<>();
    public static HashMap<String, ArrayList<Integer>> od = new HashMap<>();

    public static ArrayList<Integer> stationsKey;
    public static ArrayList<Long> wagonsKey;
    public static ArrayList<Integer> dizelsKey;
    public static String url = "jdbc:sqlserver://localhost;integratedSecurity=true";

    public static void runQueries() {
        System.out.println("start sql");
//        calculateStationStops();
        runStationQuery(getStationQuery());
        runCapacityQuery("Select * from Traffic.dbo.capacity");
        runBlockTimeQuery(getBlockTimeQuery());
        runFreightQuery("select * from graph.dbo.Kala");
        runNahiehQuery("select * from graph.dbo.nahi");
        runWagonTypeQuery("select * from graph.dbo.Wagon_Type");
        runWagonQuery(getWagonQuery());
        runDizelListQuery(getDizelListQuery());
        runDizelQuery(getDizelQuery());
        System.out.println("end sql");
    }

    private static void runWagonTypeQuery(String query) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            ResultSet wagonTypeResultSet = statement.executeQuery(query);
            while (wagonTypeResultSet.next()) {
                wagonType.put(wagonTypeResultSet.getInt("code"), wagonTypeResultSet.getString("Name"));
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    private static void runCapacityQuery(String query) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement = connection.createStatement();
            ResultSet capacitiesResultSet = statement.executeQuery(query);
            while (capacitiesResultSet.next()) {

                int station = capacitiesResultSet.getInt("station");
                int freight = capacitiesResultSet.getInt("freight");
                int loadCap = capacitiesResultSet.getInt("loadCapacity");
                int unloadCap = capacitiesResultSet.getInt("unloadCapacity");
                if (stationMap.containsKey(station)) {
                    if (loadCap != 0) {
                        if (!stationMap.get(station).getStationCapacity().containsKey(freight))
                            stationMap.get(station).getStationCapacity().put(freight, new Station.Capacity(
                                    loadCap, 0)
                            );
                        else
                            stationMap.get(station).getStationCapacity().get(freight).loadingCap = loadCap;
                    }
                    if (unloadCap != 0) {
                        if (!stationMap.get(station).getStationCapacity().containsKey(freight))
                            stationMap.get(station).getStationCapacity().put(freight, new Station.Capacity(
                                    0, unloadCap)
                            );
                        else
                            stationMap.get(station).getStationCapacity().get(freight).unloadingCap = unloadCap;
                    }
                }

            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    private static void runNahiehQuery(String query) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet nahiehResultSet = statement1.executeQuery(query);
            while (nahiehResultSet.next()) {
                nahiehtMap.put(nahiehResultSet.getInt("code"), nahiehResultSet.getString("descrip"));
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    public static void runFreightQuery(String stationQuery) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet stationResultSet = statement1.executeQuery(stationQuery);
            while (stationResultSet.next()) {
                freightMap.put(stationResultSet.getInt("code"), stationResultSet.getString("Descript"));
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    public static void calculateStationStops() {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            String wagonNumberQuery = "SELECT F4602 AS wagon\n" +
                    "FROM  graph.dbo.f46\n" +
                    "where f4601 = 10 and F4602 is not null\n" +
                    "GROUP BY F4602";
            ResultSet wagonsNumber = statement1.executeQuery(wagonNumberQuery);

            Statement statement2 = connection.createStatement();
            int i = 0;
            while (wagonsNumber.next()) {
                i++;
                System.out.printf("\ri: %7d%%", i);
                int stationID;
                int barId;
                int wagonType;
                float stopTime;
                String query = getWagonStationQuery(wagonsNumber.getInt(1));
                ResultSet wagonStationTimes = statement2.executeQuery(query);
                while (wagonStationTimes.next()) {
                    stationID = wagonStationTimes.getInt("stationID");
                    barId = wagonStationTimes.getInt("fright");
                    wagonType = wagonStationTimes.getInt("Wagon_Type");
                    String StationName = wagonStationTimes.getString("stationName");
                    //agar feild zaman khoroj khali bashad
                    //yani wagon be maghasad ghatar reside wa baiad zaman khoroj ghatar
                    //badi ba zaman vorod moghaise shav
                    if (wagonStationTimes.getInt("ExitTime") != 0) {
                        stopTime = wagonStationTimes.getInt("ExitTime") - wagonStationTimes.getInt("EnterTime");
                    } else {
                        int enterTime = wagonStationTimes.getInt("EnterTime");
                        if (wagonStationTimes.next())
                            stopTime = wagonStationTimes.getInt("ExitTime") - enterTime;
                        else
                            break;
                    }

                    if (!stationMap.containsKey(stationID)) {
                        stationMap.put(stationID, new Station(StationName,
                                wagonType,
                                barId,
                                1,
                                stopTime));
                    } else {
                        if (!stationMap.get(stationID).getTimeHashMap().
                                containsKey(new Station.TimeHashMapKey(wagonType, barId))) {
                            stationMap.get(stationID).getTimeHashMap().
                                    put(new Station.TimeHashMapKey(wagonType, barId), new Station.WagonTypeTime(1, stopTime));
                        } else {
                            Station.WagonTypeTime wagonTypeTime = stationMap.get(stationID).getTimeHashMap()
                                    .get(new Station.TimeHashMapKey(wagonType, barId));
                            wagonTypeTime.setWagonTransferNumber(wagonTypeTime.getWagonTransferNumber() + 1);
                            wagonTypeTime.setTotalWagonStopTime(wagonTypeTime.getTotalWagonStopTime() + stopTime);
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
        System.out.println();
        Iterator it1 = stationMap.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<Integer, Station> station = (Map.Entry<Integer, Station>) it1.next();
            Iterator it2 = station.getValue().getTimeHashMap().entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<Station.TimeHashMapKey, Station.WagonTypeTime> wagonTimeElement =
                        (Map.Entry<Station.TimeHashMapKey, Station.WagonTypeTime>) it2.next();
                System.out.println(station.getKey() + " - " + wagonTimeElement.getKey().getWagonType() + " - " +
                        wagonTimeElement.getKey().getBarKind() + " - " + wagonTimeElement.getValue().getWagonTransferNumber() + " - " +
                        wagonTimeElement.getValue().getTotalWagonStopTime() / wagonTimeElement.getValue().getWagonTransferNumber());
            }
        }
    }

    public static String getWagonStationQuery(int wagon) {
        String sql = "SELECT * FROM Traffic.dbo.wagon_station_stop where wagonID=" + wagon + " ORDER BY formationTime, EnterTime";
        return sql;
    }

    public static String getBlockTimeQuery() {
        String sql = "SELECT * FROM Traffic.dbo.seir_blockTime";
        return sql;
    }

    public static String getWagonQuery() {
        String sql = "SELECT * FROM Traffic.dbo.wagon_last_stat";
        return sql;
    }

    public static String getDizelListQuery() {
        String sql = "SELECT * FROM Traffic.dbo.Dizel_powers";
        return sql;
    }

    public static String getDizelQuery() {
        String sql = "SELECT * FROM Traffic.dbo.dizel_last_stat";
        return sql;
    }

    public static String getStationQuery() {
        String sql = "SELECT * FROM Traffic.dbo.stations ORDER BY code";
        return sql;
    }

    public static void runBlockTimeQuery(String blockTimeQuery) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet blocksResultSet = statement1.executeQuery(blockTimeQuery);
            while (blocksResultSet.next()) {
                blockMap.put((blocksResultSet.getInt("BlockId") * 10) + 2,
                        new newBlock(
                                blocksResultSet.getInt("startStationID"),
                                blocksResultSet.getInt("endStationID"),
                                blocksResultSet.getInt("length"),
                                blocksResultSet.getDouble("lengthGIS"),
                                blocksResultSet.getInt("train"),
                                blocksResultSet.getFloat("timeTravel"),
                                blocksResultSet.getInt("trainLength"),
                                blocksResultSet.getInt("trainWeight")
                        ));
                blockMap.put((blocksResultSet.getInt("BlockId") * 10) + 1,
                        new newBlock(
                                blocksResultSet.getInt("endStationID"),
                                blocksResultSet.getInt("startStationID"),
                                blocksResultSet.getInt("length"),
                                blocksResultSet.getDouble("lengthGIS"),
                                blocksResultSet.getInt("train"),
                                blocksResultSet.getFloat("timeTravel"),
                                blocksResultSet.getInt("trainLength"),
                                blocksResultSet.getInt("trainWeight")
                        ));
                maxBlockId = max(maxBlockId, (blocksResultSet.getInt("BlockId") * 10) + 2);
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    public static void runWagonQuery(String wagonQuery) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet wagonsResultSet = statement1.executeQuery(wagonQuery);
            while (wagonsResultSet.next()) {
                //agar yek wagon 2 time max dasht recordi ghabool ast ke balatarin zaman tashkil ghatar dashte bashad
                if (!wagonListMap.containsKey(wagonsResultSet.getInt("fleetId"))
                        || (
                        Long.valueOf(wagonsResultSet.getString("trainFormationYear") +
                                wagonsResultSet.getString("trainFormationTime")) >
                                Long.valueOf(wagonListMap.get(wagonsResultSet.getInt("fleetId")).getTrainFormationYear() +
                                        wagonListMap.get(wagonsResultSet.getInt("fleetId")).getTrainFormationTime()))) {
                    wagonListMap.put(wagonsResultSet.getLong("fleetId"),
                            new newWagon(
                                    wagonsResultSet.getLong("fleetKind"),
                                    wagonsResultSet.getInt("frieght"),
                                    wagonsResultSet.getInt("Destination"),
                                    wagonsResultSet.getInt("lastStation"),
                                    wagonsResultSet.getInt("detachStation"),
                                    wagonsResultSet.getInt("trainDestination"),
                                    wagonsResultSet.getInt("STATUS"),
                                    wagonsResultSet.getInt("trainRecId"),
                                    wagonsResultSet.getString("lastStationEnterYear"),
                                    wagonsResultSet.getString("lastStationEnterTime"),
                                    wagonsResultSet.getString("lastStationExitYear"),
                                    wagonsResultSet.getString("lastStationExitTime"),
                                    wagonsResultSet.getInt("lastTimeCalculate"),
                                    wagonsResultSet.getString("trainFormationYear"),
                                    wagonsResultSet.getString("trainFormationTime"),
                                    wagonsResultSet.getInt("wagonType"),
                                    wagonsResultSet.getInt("WagonLength"),
                                    wagonsResultSet.getInt("emptyWeight"),
                                    wagonsResultSet.getInt("FullWeight")
                            ));
                    if (stationMap.get(wagonsResultSet.getInt("lastStation")).getStationCapacity()
                            .containsKey(wagonsResultSet.getInt("frieght"))) {
                        stationMap.get(wagonsResultSet.getInt("lastStation")).getStationCapacity()
                                .get(wagonsResultSet.getInt("frieght")).stationWagon.add(wagonsResultSet.getLong("fleetId"));
                    } else {
                        stationMap.get(wagonsResultSet.getInt("lastStation")).getStationCapacity().put(
                                wagonsResultSet.getInt("frieght"),
                                new Station.Capacity(100,100)
                        );
                        stationMap.get(wagonsResultSet.getInt("lastStation")).getStationCapacity()
                                .get(wagonsResultSet.getInt("frieght")).stationWagon.add(wagonsResultSet.getLong("fleetId"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    public static void runDizelListQuery(String DizelListQuery) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet dizelResultSet = statement1.executeQuery(DizelListQuery);
            while (dizelResultSet.next()) {
                if (dizelResultSet.getInt("fleetId") != -1) {
                    if (!dizelListMap.containsKey(dizelResultSet.getInt("fleetId"))) {
                        //first time adding a dizel
                        HashMap<Integer, Integer> hashSet = new HashMap<>();

                        if (blockMap.get(dizelResultSet.getInt("BlockId") * 10 + 2).getStartStationID() ==
                                dizelResultSet.getInt("Ent_St")) {
                            hashSet.put(dizelResultSet.getInt("BlockId") * 10 + 2, dizelResultSet.getInt("dizelPower"));
                        } else if (blockMap.get(dizelResultSet.getInt("BlockId") * 10 + 1).getStartStationID() ==
                                dizelResultSet.getInt("Ent_St")) {
                            hashSet.put(dizelResultSet.getInt("BlockId") * 10 + 1, dizelResultSet.getInt("dizelPower"));
                        } else {
                            continue;
                        }
                        dizelListMap.put(dizelResultSet.getInt("fleetId"), new Dizel(hashSet));
                    } else {
                        //add blocks to a dizel
                        if (blockMap.get(dizelResultSet.getInt("BlockId") * 10 + 2).getStartStationID() ==
                                dizelResultSet.getInt("Ent_St")) {
                            dizelListMap.get(dizelResultSet.getInt("fleetId")).getAllowedBlock()
                                    .put(dizelResultSet.getInt("BlockId") * 10 + 2, dizelResultSet.getInt("dizelPower"));
                        } else if (blockMap.get(dizelResultSet.getInt("BlockId") * 10 + 1).getStartStationID() ==
                                dizelResultSet.getInt("Ent_St")) {
                            dizelListMap.get(dizelResultSet.getInt("fleetId")).getAllowedBlock()
                                    .put(dizelResultSet.getInt("BlockId") * 10 + 1, dizelResultSet.getInt("dizelPower"));
                        }
                    }
                }
            }
            System.out.println();
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    public static void runDizelQuery(String DizelQuery) {
        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet dizelResultSet = statement1.executeQuery(DizelQuery);
            while (dizelResultSet.next()) {
                //agar yek wagon 2 time max dasht recordi ghabool ast ke balatarin zaman tashkil ghatar dashte bashad
                if (dizelListMap.containsKey(dizelResultSet.getInt("fleetId"))
                        && (dizelListMap.get(dizelResultSet.getInt("fleetId")).getTrainFormationYear().equals("") ||
                        Long.valueOf(dizelResultSet.getString("trainFormationYear") +
                                dizelResultSet.getString("trainFormationTime")) >
                                Long.valueOf(dizelListMap.get(dizelResultSet.getInt("fleetId")).getTrainFormationYear() +
                                        dizelListMap.get(dizelResultSet.getInt("fleetId")).getTrainFormationTime()))) {
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setFleetKind(dizelResultSet.getLong("fleetKind"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setDestination(dizelResultSet.getInt("Destination"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setLastStation(dizelResultSet.getInt("lastStation"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setDetachStation(dizelResultSet.getInt("detachStation"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setTrainDestination(dizelResultSet.getInt("trainDestination"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setStatus(dizelResultSet.getInt("STATUS"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setTrainRecId(dizelResultSet.getInt("trainRecId"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setLastStationEnterYear(dizelResultSet.getString("lastStationEnterYear"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setLastStationEnterTime(dizelResultSet.getString("lastStationEnterTime"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setLastStationExitYear(dizelResultSet.getString("lastStationExitYear"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setLastStationExitTime(dizelResultSet.getString("lastStationExitTime"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setLastTimeCalculate(dizelResultSet.getInt("lastTimeCalculate"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setTrainFormationYear(dizelResultSet.getString("trainFormationYear"));
                    dizelListMap.get(dizelResultSet.getInt("fleetId")).setTrainFormationTime(dizelResultSet.getString("trainFormationTime"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }

    public static void runStationQuery(String stationQuery) {

        try {
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet stationResultSet = statement1.executeQuery(stationQuery);
            while (stationResultSet.next()) {
                stationMap.put(stationResultSet.getInt("code"), new Station(stationResultSet.getString("NAME"),
                        stationResultSet.getInt("nahi")));
                maxStationID = stationResultSet.getInt("code");
            }
        } catch (SQLException e) {
            System.out.println("Connection had not made for: " + e.getMessage());
        }
    }
}
