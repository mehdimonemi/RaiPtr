package company.Backend;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.*;
import java.util.HashMap;

import static company.sql.*;

public class cycleTime {

    static HashMap<String, double[]> od = new HashMap<>();

    public static void cycleTime() {
        try {
            PrintStream out = new PrintStream("log.txt");
            System.setOut(out);
            Connection connection = DriverManager.getConnection(url);
            Statement statement1 = connection.createStatement();
            ResultSet wagonsNumber = statement1
                    .executeQuery("select wagonID from Traffic.dbo.wagon_station_stop " +
                            "group by wagonID");


            Statement statement2 = connection.createStatement();

            int i = 0;
            System.out.println("واگن" + ";" + "مبدا" + ";" + "مقصد" + ";" + "بار" + ";" + "زمان سیر" + ";" + "مسافت");

            while (wagonsNumber.next()) {
                i++;
                System.out.println(i);
                String query = getQuery(wagonsNumber.getInt(1));
                ResultSet wagonStationTimes = statement2.executeQuery(query);
                while (wagonStationTimes.next()) {
                    int origin = wagonStationTimes.getInt("stationID");
                    int destination = wagonStationTimes.getInt("stationID");
                    int a = wagonStationTimes.getInt("origin");
                    int b = wagonStationTimes.getInt("destination");
                    int fright = wagonStationTimes.getInt("fright");
                    int wagon = wagonStationTimes.getInt("wagonID");

                    int time = 0;
                    int aTime = 0;
                    int countBlocks = 0;
                    if (wagonStationTimes.getInt("ExitTime") != 0) {
                        time = wagonStationTimes.getInt("ExitTime") - wagonStationTimes.getInt("EnterTime");
                        aTime = wagonStationTimes.getInt("ExitTime");
                    } else {
                        aTime = wagonStationTimes.getInt("EnterTime");
                    }
                    double dist = 0;

                    while (wagonStationTimes.next() && a == wagonStationTimes.getInt("origin")
                            && b == wagonStationTimes.getInt("destination")
                            && fright == wagonStationTimes.getInt("fright")) {
                        destination = wagonStationTimes.getInt("stationID");
                        //seir block time+time stop
                        if (wagonStationTimes.getInt("ExitTime") != 0) {
                            time += (wagonStationTimes.getInt("EnterTime") - aTime);
                            time += wagonStationTimes.getInt("ExitTime") - wagonStationTimes.getInt("EnterTime");
                            dist = addBlockLength(dist, wagonStationTimes.getInt("enterBlock"));
                            countBlocks++;
                            aTime = wagonStationTimes.getInt("ExitTime");
                        } else {
                            time += (wagonStationTimes.getInt("EnterTime") - aTime);
                            aTime = wagonStationTimes.getInt("EnterTime");
                        }
                    }

                    String commodity = origin + ";" + destination + ";" + fright;
                    if (od.containsKey(commodity)) {
                        od.get(commodity)[0] += 1;
                        od.get(commodity)[1] += time;
                        od.get(commodity)[2] += dist;
                    } else {
                        double[] temp = new double[3];
                        temp[0] = 1;
                        temp[1] = time;
                        temp[2] = dist;
                        od.put(commodity, temp);
                    }
                    if (stationMap.containsKey(origin) && stationMap.containsKey(destination))
                        System.out.println(wagon + ";" + stationMap.get(origin).getName()
                                + ";" + stationMap.get(destination).getName()
                                + ";" + fright + ";" + time / 60.0 + ";" + dist+ ";" + countBlocks);
                    else
                        System.out.println(wagon + ";" + origin
                                + ";" + destination + ";" + fright + ";" + time+ ";" + dist+ ";" + countBlocks);
                }
            }
//            for (String commodity : od.keySet()) {
//                System.out.println(commodity + ";" + od.get(commodity)[0] + ";" + od.get(commodity)[1] + ";" + od.get(commodity)[2]);
//            }
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static double addBlockLength(double dist, int enterBlock) {
        for (Integer blockId : blockMap.keySet()) {
            //divide by 10 cause we added each block 2 times as zoj va fard
            if ((blockId / 10) == enterBlock) {
                dist += blockMap.get(blockId).getLengthGIS();
                return dist;
            }
        }
        return dist;
    }

    private static String getQuery(int anInt) {
        String sql = "select * from Traffic.dbo.wagon_station_stop where wagonID=" + anInt
                + "ORDER BY \n"
                + "formationTime \n"
                + ",EnterTime";
        return sql;
    }


}
