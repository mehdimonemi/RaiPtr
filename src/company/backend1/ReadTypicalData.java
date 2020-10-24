package company.backend1;

import com.jfoenix.controls.JFXTreeTableView;
import company.Data.*;
import company.Data.oldOnes.Block;
import company.Data.oldOnes.Wagon;
import company.Table;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.*;

import static company.backend1.ExcelSetValue.setCell;
import static company.backend1.ExcelSetValue.setStyle;
import static company.backend1.Massages.*;
import static org.apache.poi.ss.usermodel.CellType.NUMERIC;
import static org.apache.poi.ss.usermodel.CellType.STRING;

public class ReadTypicalData {

    public static ArrayList<Station> Stations = new ArrayList<>();
    public static ArrayList<Block> blocks = new ArrayList<>();
    public static ArrayList<Train> trains = new ArrayList<>();
    public static ArrayList<TrainArc> trainArcs = new ArrayList<>();
    public static ArrayList<Commodity> commodities = new ArrayList<>();
    public static ArrayList<Wagon> wagons = new ArrayList<>();
    public static ArrayList<Loco> locos = new ArrayList<>();
    public static ArrayList<Block> sourceArcs = new ArrayList<>();
    public static ArrayList<Block> sinkArcs = new ArrayList<>();
    public static ArrayList<ManovrStation> manovrStations = new ArrayList<>();
    public static ArrayList<String> jonoobStations = new ArrayList<>();
    public static ArrayList<String> northStations = new ArrayList<>();
    public static ArrayList<String[]> rawInputs = new ArrayList<>();


    public String main(String fileName) {

        manovrStations.clear();
        Stations.clear();
        blocks.clear();
        trains.clear();
        trainArcs.clear();
        commodities.clear();
        wagons.clear();
        locos.clear();
        sourceArcs.clear();
        sinkArcs.clear();
        northStations.clear();

        XSSFWorkbook inputs = null;
        FileInputStream file = null;
        try {
            file = new FileInputStream(new File(fileName));
            inputs = new XSSFWorkbook(file);

            // read stations data
            XSSFSheet sheet = inputs.getSheetAt(0);
            for (int i = 0; i < sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i + 1);
                Stations.add(new Station((int) row.getCell(0).getNumericCellValue(),
                        row.getCell(1).getStringCellValue()));
            }

            //read jonoob stations
            sheet = inputs.getSheetAt(6);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);

                jonoobStations.add(row.getCell(0).getStringCellValue());
            }

            // read commodities data
            int bigCommodity = 0;
            HashSet<ManovrStation> tempManovrStations = new HashSet();
            XSSFSheet sheet2 = inputs.getSheetAt(2);

            //read priority
            double[] temp = new double[sheet2.getLastRowNum()];
            double bound = 100;
            double maxPr = 1;
            double minPr = 100;
            for (int i = 1; i <= sheet2.getLastRowNum(); i++) {
                XSSFRow row = sheet2.getRow(i);
                if (row.getCell(0).getCellTypeEnum().equals(STRING)) {
                    temp[i - 1] = 0;
                } else {
                    temp[i - 1] = row.getCell(0).getNumericCellValue();
                    if (temp[i - 1] == 0)
                        continue;

                    switch (row.getCell(1).getStringCellValue()) {
                        case "گار":
                            break;
                        case "انديمشك":
                            break;
                        default:
                            temp[i - 1] += 1;
                    }
                    if (!row.getCell(1).getStringCellValue().equals("")) {
                        temp[i - 1] += 1;
                    }

                    if (maxPr < temp[i - 1])
                        maxPr = temp[i - 1];
                    if (minPr > temp[i - 1])
                        minPr = temp[i - 1];
                }
            }

            int id = 0;
            for (int i = 1; i <= sheet2.getLastRowNum(); i++) {
                XSSFRow row = sheet2.getRow(i);
                if (temp[i - 1] == 0) {
                    continue;
                } else if ((int) row.getCell(3).getNumericCellValue() < 80) {
                    commodities.add(new Commodity(
                            id,
                            bound - ((temp[i - 1] - 1)) * (bound / (maxPr - minPr + 1)),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            (int) row.getCell(3).getNumericCellValue(),
                            row.getCell(7).getStringCellValue()
                    ));
                    id++;
                } else {
                    bigCommodity++;
                    commodities.add(new Commodity(
                            id,
                            bound - (temp[i - 1] - 1) * (bound / (maxPr - minPr + 1)),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            80,
                            row.getCell(7).getStringCellValue()));
                    id++;

                    commodities.add(new Commodity(
                            id,
                            bound - ((temp[i - 1] + 1 < maxPr) ? temp[i - 1] + 1 - 1 : (maxPr - 1)) *
                                    (bound / (maxPr - minPr + 1)),
                            row.getCell(1).getStringCellValue(),
                            row.getCell(2).getStringCellValue(),
                            (int) row.getCell(3).getNumericCellValue() - 80,
                            row.getCell(7).getStringCellValue()));
                    id++;

                }

                tempManovrStations.add(new ManovrStation(tempManovrStations.size(), commodities.get(commodities.size() - 1).getOrigin()));
                tempManovrStations.add(new ManovrStation(tempManovrStations.size(), commodities.get(commodities.size() - 1).getDestination()));
            }
            manovrStations.addAll(tempManovrStations);

            //sort manovr stations
            Collections.sort(manovrStations, new Comparator<ManovrStation>() {
                @Override
                public int compare(ManovrStation lhs, ManovrStation rhs) {
                    // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                    return lhs.getMainId() < rhs.getMainId() ? -1 : (lhs.getMainId() > rhs.getMainId()) ? 1 : 0;
                }
            });

            for (int i = 0; i < manovrStations.size(); i++) {
                manovrStations.get(i).setId(i);
            }

            for (Commodity commodity : commodities) {
                commodity.setOriginId();
                commodity.setDestinationId();
                if (commodity.getOriginId() < commodity.getDestinationId()) {
                    commodity.setDirection("جنوبی");
                } else
                    commodity.setDirection("شمالی");
            }

            // read blocks data
            ArrayList<Block> physicalBlocks = new ArrayList<>();
            sheet = inputs.getSheetAt(1);
            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i + 1);
                physicalBlocks.add(new Block(
                        (int) row.getCell(0).getNumericCellValue(),
                        row.getCell(1).getStringCellValue(),
                        row.getCell(2).getStringCellValue(),
                        (int) row.getCell(3).getNumericCellValue(),
                        (int) row.getCell(4).getNumericCellValue(),
                        (int) row.getCell(5).getNumericCellValue(),
                        (int) row.getCell(6).getNumericCellValue(),
                        row.getCell(7).getNumericCellValue()

                ));
            }
            buildBlocks(physicalBlocks);

            //read wagons data
            sheet = inputs.getSheetAt(4);
            for (int i = 0; i < sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i + 1);
                wagons.add(new Wagon(row.getCell(0).getStringCellValue(),
                        row.getCell(1).getStringCellValue(),
                        row.getCell(2).getNumericCellValue(),
                        row.getCell(3).getNumericCellValue() > 80
                                ? 80 : row.getCell(3).getNumericCellValue(),
                        row.getCell(4).getNumericCellValue()
                ));
            }

            //set wagon type
            int c = 0;
            for (int i = 1; i <= sheet2.getLastRowNum(); i++) {
                if (temp[i - 1] == 0)
                    continue;
                boolean flag = true;
                XSSFRow row = sheet2.getRow(i);
                String name = "null";
                for (Wagon wagon : wagons) {
                    if (row.getCell(6).getStringCellValue().contains("مخزن")) {
                        name = "مخزندار";
                    } else if (row.getCell(6).getStringCellValue().equals("ويژه")) {
                        name = "حمل پودر آلومینیم";
                    } else if (row.getCell(6).getStringCellValue().equals("استراکچر")) {
                        name = "مسطح";
                    } else if (row.getCell(6).getStringCellValue().contains("روس")) {
                        name = "روسی";
                    } else if (row.getCell(6).getStringCellValue().contains("اروپا")) {
                        name = "اروپايي";
                    } else if (row.getCell(6).getStringCellValue().contains("فله بر")) {
                        name = "حمل غلات( فله بر )";
                    } else if (row.getCell(6).getStringCellValue().equals("حمل بالاست")
                            || row.getCell(6).getStringCellValue().equals("شن کش")
                            || row.getCell(6).getStringCellValue().equals("سازمان جرثقيل")) {
                        name = "حمل بالاست";
                    } else if (row.getCell(6).getStringCellValue().contains("لبه بلند")
                            || row.getCell(6).getStringCellValue().equals("فولاد اهواز")
                            || row.getCell(6).getStringCellValue().equals("لبه بلند اکرايني")
                    ) {
                        name = "لبه بلند";
                    } else {
                        name = row.getCell(6).getStringCellValue();
                    }
                    if (row.getCell(5).getStringCellValue().equals(wagon.getOwner()) &&
                            name.equals(wagon.getName())) {
                        commodities.get(c).setWagon(wagon);
                        commodities.get(c).setWagonName(row.getCell(6).getStringCellValue());
                        if (commodities.get(c).getVolume() == 80 && commodities.size() - 1 != c) {
                            if (
                                    commodities.get(c).getOriginId() == commodities.get(c + 1).getOriginId() &&
                                            commodities.get(c).getDestinationId() == commodities.get(c + 1).getDestinationId() &&
                                            commodities.get(c).getMainDestination().equals(commodities.get(c + 1).getMainDestination()) &&
                                            commodities.get(c).getFreight().equals(commodities.get(c + 1).getFreight()) &&
                                            commodities.get(c + 1).getVolume() == ((int) row.getCell(3).getNumericCellValue() - 80)
                            ) {
                                c++;
                                commodities.get(c).setWagon(wagon);
                                commodities.get(c).setWagonName(row.getCell(6).getStringCellValue());

                            }
                        }
                        flag = false;
                        c++;
                    }
                }
                if (flag) {
                    if (commodities.get(c).getWagon() == null) {
                        commodities.get(c).setWagon(wagons.get(wagons.size() - 1));
                        commodities.get(c).setWagonName(row.getCell(6).getStringCellValue());
                        if (commodities.get(c).getVolume() == 80 && commodities.size() - 1 != c) {
                            if (
                                    commodities.get(c).getOriginId() == commodities.get(c + 1).getOriginId() &&
                                            commodities.get(c).getDestinationId() == commodities.get(c + 1).getDestinationId() &&
                                            commodities.get(c).getMainDestination().equals(commodities.get(c + 1).getMainDestination()) &&
                                            commodities.get(c).getFreight().equals(commodities.get(c + 1).getFreight())
                            ) {
                                c++;
                                commodities.get(c).setWagon(wagons.get(wagons.size() - 1));
                                commodities.get(c).setWagonName(row.getCell(6).getStringCellValue());
                            }
                        }
                        c++;
                    }
                }
            }

            // read loco data
            sheet = inputs.getSheetAt(5);
            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                int[] ascentsPower = new int[15];
                for (int j = 0; j < 15; j++) {
                    ascentsPower[j] = (int) row.getCell(j + 8).getNumericCellValue();
                }
                locos.add(new Loco(i, (int) row.getCell(3).getNumericCellValue(), ascentsPower));
            }

            //add source and sink Arcs
            for (int i = 0; i < manovrStations.size(); i++) {
                sinkArcs.add(new Block(i, manovrStations.get(i).getName(), "sink", manovrStations.get(i).getId(), -1));
                sourceArcs.add(new Block(i, "source", manovrStations.get(i).getName(), -1, manovrStations.get(i).getId()));
            }
            return ReadDataSuccess.toString();
        } catch (IllegalStateException e) {
            return IllegalStateInExcel.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (FileNotFoundException e) {
            return SpecificationFileNotFound.toString();
        } catch (IOException e) {
            return FileNotFound.toString();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                }
            }
            if (inputs != null) {
                try {
                    inputs.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String buildBlocks(ArrayList<Block> physicalBlocks) {
        try {
            IloCplex model = new IloCplex();
            IloNumVar[] X = new IloNumVar[physicalBlocks.size()];
            IloNumExpr goalFunction;
            IloNumExpr constraint;


            int stationA;
            int stationB;
            int counter = 0;
            // first loop made loops to first build went blocks and then back blocks
            for (int a = 0; a < 2; a++) {
                for (int z = 0; z < manovrStations.size(); z++) {
                    for (int x = 0; x < manovrStations.size(); x++) {
                        if ((a == 0 && z == x - 1) || (z == x + 1 && a == 1)) {
                            stationA = manovrStations.get(z).getMainId();
                            stationB = manovrStations.get(x).getMainId();

                            for (int i = 0; i < physicalBlocks.size(); i++) {
                                X[i] = model.numVar(0, 1, IloNumVarType.Int);
                            }

                            goalFunction = model.constant(0);
                            for (int i = 0; i < physicalBlocks.size(); i++) {
                                goalFunction = model.sum(goalFunction, model.prod(X[i], physicalBlocks.get(i).getLength()));
                            }
                            model.addMinimize(goalFunction);

                            // constraints
                            for (int i = 0; i < Stations.size(); i++) {
                                constraint = model.constant(0);
                                if (Stations.get(i).getId() == stationA) {
                                    for (int j = 0; j < physicalBlocks.size(); j++) {
                                        if (stationA == physicalBlocks.get(j).getOriginId()) {
                                            constraint = model.sum(constraint, X[j]);
                                        }
                                        if (stationA == physicalBlocks.get(j).getDestinationId()) {
                                            constraint = model.sum(constraint, model.negative(X[j]));
                                        }
                                    }
                                    model.addEq(constraint, 1);
                                } else if (physicalBlocks.get(i).getId() == (stationB)) {
                                    for (int j = 0; j < physicalBlocks.size(); j++) {
                                        if (stationB == physicalBlocks.get(j).getOriginId()) {
                                            constraint = model.sum(constraint, X[j]);
                                        }
                                        if (stationB == physicalBlocks.get(j).getDestinationId()) {
                                            constraint = model.sum(constraint, model.negative(X[j]));
                                        }
                                    }
                                    model.addEq(constraint, -1);
                                } else {
                                    for (int j = 0; j < physicalBlocks.size(); j++) {
                                        if (Stations.get(i).getId() == (physicalBlocks.get(j).getOriginId())) {
                                            constraint = model.sum(constraint, X[j]);
                                        }
                                        if (Stations.get(i).getId() == (physicalBlocks.get(j).getDestinationId())) {
                                            constraint = model.sum(constraint, model.negative(X[j]));
                                        }
                                    }
                                    model.addEq(constraint, 0);
                                }
                            } // end of constraints

                            model.setOut(null);
                            if (model.solve()) {
                                ArrayList<Block> tempBlocks = new ArrayList<>();
                                for (int i = 0; i < physicalBlocks.size(); i++) {
                                    if (model.getValue(X[i]) == 1) {
                                        tempBlocks.add(physicalBlocks.get(i));
                                    }
                                }
                                int maxAscent = 0;
                                int capacity = 900000;
                                int maxTrainLength = 900000;

                                for (Block trainBlock : tempBlocks) {
                                    if (maxAscent <= trainBlock.getAscent()) {
                                        maxAscent = trainBlock.getAscent();
                                    }
                                }

                                for (Block trinBlock : tempBlocks) {
                                    if (capacity >= trinBlock.getCapacity()) {
                                        capacity = trinBlock.getCapacity();
                                    }
                                }


                                for (Block trinBlock : tempBlocks) {
                                    if (maxTrainLength >= trinBlock.getMaxTrainLength()) {
                                        maxTrainLength = trinBlock.getMaxTrainLength();
                                    }
                                }

                                double time = 0;
                                for (int i = 0; i < physicalBlocks.size(); i++) {
                                    if (model.getValue(X[i]) == 1) {
                                        time += physicalBlocks.get(i).getTime();
                                    }
                                }

                                blocks.add(new Block(counter, manovrStations.get(z), manovrStations.get(x),
                                        capacity, maxTrainLength, maxAscent, (int) model.getObjValue(), time
                                ));
                                counter++;
                                model.clearModel();
                            } else {
                                model.clearModel();
                                System.out.println("station " + stationA + " to " + stationB + " : No");
                            }
                        }
                    }
                }
            }
            return MakeTriansSuccess.toString();
        } catch (IloCplex.UnknownObjectException e) {
            return UnknownCplexException.toString();
        } catch (IloException e) {
            return CplexException.toString();
        }
    }

    public String manageGraphData(String fileName) {
        FileInputStream inputFile = null;
        FileOutputStream outputFile = null;
        XSSFWorkbook workbook = null;

        manovrStations.clear();
        Stations.clear();
        blocks.clear();
        trains.clear();
        trainArcs.clear();
        commodities.clear();
        wagons.clear();
        locos.clear();
        sourceArcs.clear();
        sinkArcs.clear();
        northStations.clear();
        rawInputs.clear();
        try {
            inputFile = new FileInputStream(new File(fileName));
            workbook = new XSSFWorkbook(inputFile);
            XSSFCellStyle style = setStyle(workbook, "B Zar");

            // read stations data
            XSSFSheet sheet1 = workbook.getSheetAt(0);
            for (int i = 0; i < sheet1.getLastRowNum(); i++) {
                XSSFRow row = sheet1.getRow(i + 1);
                Stations.add(new Station((int) row.getCell(0).getNumericCellValue(),
                        row.getCell(1).getStringCellValue()));
            }

            //read jonoob stations
            XSSFSheet sheet2 = workbook.getSheetAt(6);
            for (int i = 0; i <= sheet2.getLastRowNum(); i++) {
                XSSFRow row = sheet2.getRow(i);
                jonoobStations.add(row.getCell(0).getStringCellValue());
            }
            for (int i = 0; i <= sheet2.getLastRowNum(); i++) {
                XSSFRow row = sheet2.getRow(i);
                northStations.add(row.getCell(1).getStringCellValue());
            }


            //find accepted commodities
            ArrayList<String[]> acceptedRecprds = new ArrayList<>();
            XSSFSheet sheet3 = workbook.getSheetAt(7);
            for (int i = 0; i <= sheet3.getLastRowNum(); i++) {
                String[] strings = new String[10];
                XSSFRow row = sheet3.getRow(i);
                boolean isARecord = true;
                for (int j = 9; j >= 0; j--) {
                    switch (row.getCell(j).getCellTypeEnum()) {
                        case STRING: {
                            strings[j] = row.getCell(j).getStringCellValue();
                            break;
                        }
                        case NUMERIC: {
                            strings[j] = String.valueOf((int) row.getCell(j).getNumericCellValue());
                            break;
                        }
                        default: {
                            if (j == 8)
                                isARecord = false;
                            else if (j == 6)
                                strings[j] = "";
                            else if (j == 0)
                                strings[j] = "null";
                            break;
                        }
                    }
                    if (!isARecord) break;
                }

                if (isARecord) {
                    if (strings[8].contains("سمنگان") || strings[8].contains("پالايشگاه")) {
                        strings[8] = "سمنگان (پالايشگاه)";
                    }
                    if (strings[1].contains("سمنگان") || strings[1].contains("پالايشگاه")) {
                        strings[1] = "سمنگان (پالايشگاه)";
                    }

                    if (strings[1].equals(strings[8])) {
                        isARecord = false;
                    }
                }

                if (!isARecord) continue;
                if (isCommodityInPath(strings))
                    acceptedRecprds.add(strings);
            }

            XSSFSheet sheet4 = workbook.getSheet("بارها");
            //delete existing cells
            for (int i = 1; i <= sheet4.getLastRowNum(); i++) {
                XSSFRow row = sheet4.getRow(i);
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    if (row.getCell(j).getCellTypeEnum().equals(STRING) || row.getCell(j).getCellTypeEnum().equals(NUMERIC))
                        row.removeCell(row.getCell(j));
                }
            }

            rawInputs = new ArrayList<>(acceptedRecprds);

            int i = 1;
            for (Station station : Stations) {
                int size = acceptedRecprds.size();
                for (int j = 0; j < size; j++) {

                    if (station.getName().equals(acceptedRecprds.get(j)[8])
                            && isCommodityWithMaxValueInStation(
                            station.getName(), Double.parseDouble(acceptedRecprds.get(j)[2]), acceptedRecprds)) {

                        XSSFRow row = sheet4.createRow(i);
                        setCell(sheet4, row, 0, "", style, null);
                        setCell(sheet4, row, 1, acceptedRecprds.get(j)[8], style, null);
                        setCell(sheet4, row, 2, acceptedRecprds.get(j)[1], style, null);
                        setCell(sheet4, row, 3, Double.parseDouble(acceptedRecprds.get(j)[2]), style, null);
                        setCell(sheet4, row, 4, acceptedRecprds.get(j)[9], style, null);
                        setCell(sheet4, row, 5, acceptedRecprds.get(j)[0], style, null);
                        setCell(sheet4, row, 6, acceptedRecprds.get(j)[3], style, null);
                        setCell(sheet4, row, 7, acceptedRecprds.get(j)[4], style, null);
                        acceptedRecprds.remove(acceptedRecprds.get(j));
                        i++;
                        j = -1;
                        size--;
                    }
                }
            }

            outputFile = new FileOutputStream(new File(fileName));
            workbook.write(outputFile);
            outputFile.flush();

            return GraphDataSuccess.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (FileNotFoundException e) {
            return NoFileSelected.toString();
        } catch (IOException e) {
            return FileNotFound.toString();
        } finally {
            try {
                if (inputFile != null)
                    inputFile.close();
                if (outputFile != null)
                    outputFile.close();
                if (workbook != null)
                    workbook.close();
            } catch (IOException e) {
                return FileNotFound.toString();
            }
        }
    }

    public String afterPriorityTree(String fileName, JFXTreeTableView tree) {
        FileInputStream inputFile = null;
        FileOutputStream outputFile = null;
        XSSFWorkbook workbook = null;
        try {
            inputFile = new FileInputStream(new File(fileName));
            workbook = new XSSFWorkbook(inputFile);
            XSSFCellStyle style = setStyle(workbook, "B Zar");

            XSSFSheet sheet4 = workbook.getSheet("بارها");


            for (int j = 1; j < sheet4.getLastRowNum(); j++) {
                XSSFRow row = sheet4.getRow(j);
                for (int i = 0; i < tree.getCurrentItemsCount(); i++) {
                    if (Table.getTreeCell(i, "wagon").equals(row.getCell(6).getStringCellValue()) &&
                            Table.getTreeCell(i, "freight").equals(row.getCell(7).getStringCellValue()))
                            setCell(sheet4, row, 0, Double.valueOf(Table.getTreeCell(i, "priority")), style, null);
                }
            }

            outputFile = new FileOutputStream(new File(fileName));
            workbook.write(outputFile);
            outputFile.flush();

            return GraphDataSuccess.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (FileNotFoundException e) {
            return NoFileSelected.toString();
        } catch (IOException e) {
            return FileNotFound.toString();
        } finally {
            try {
                if (inputFile != null)
                    inputFile.close();
                if (outputFile != null)
                    outputFile.close();
                if (workbook != null)
                    workbook.close();
            } catch (IOException e) {
                return FileNotFound.toString();
            }
        }
    }

    public String eraseExistingGraphData(String fileName) {
        FileInputStream inputFile = null;
        FileOutputStream outputFile;
        XSSFWorkbook workbook = null;
        try {
            inputFile = new FileInputStream(new File(fileName));
            workbook = new XSSFWorkbook(inputFile);
            workbook.removeSheetAt(7);

            XSSFSheet sheet = workbook.createSheet("اطلاعات خام");

            inputFile.close();
            outputFile = new FileOutputStream(new File(fileName));
            workbook.write(outputFile);
            outputFile.flush();
            outputFile.close();

            return GraphDataSuccess.toString();
        } catch (NullPointerException e) {
            return NullPointerError.toString();
        } catch (FileNotFoundException e) {
            return NoFileSelected.toString();
        } catch (IOException e) {
            return FileNotFound.toString();
        }
    }

    public boolean isCommodityInPath(String[] record) {
        boolean okay = false;
        boolean end = false;
        for (Station station : Stations) {
            if (station.getName().equals(record[8])) {
                record[9] = "";
                okay = true;
                if (record[8].equals("گار") && !record[7].equals("سواريان")) {
                    okay = false;
                }

                if (record[8].equals("انديمشك") && !record[7].equals("تنگ هفت")) {
                    okay = false;
                }
                if (record[6].equals("در حرکت")) {
                    okay = false;
                }
                end = true;
                break;
            }
        }

        if (!end) {
            for (String station : jonoobStations) {
                if (station.equals(record[8]) && (record[7].equals("هفت تپه") || record[7].equals("انديمشك"))) {
                    okay = true;
                    record[9] = record[8];
                    record[8] = "انديمشك";
                    for (String station1 : jonoobStations) {
                        if (station1.equals(record[1])) {
                            okay = false;
                            break;
                        }
                    }
                    end = true;
                    break;
                }
            }
        }
        if (!end) {
            for (String station : northStations) {
                if (station.equals(record[8]) &&
//                        (record[7].equals("گار") || record[7].equals("قم") || record[7].equals("كاشان") || record[7].equals("شورآب")) &&
                        record[6].equals("در حرکت")) {
                    okay = false;
                    for (String station1 : jonoobStations) {
                        if (station1.equals(record[1])) {
                            record[9] = record[8];
                            record[8] = "گار";
                            okay = true;
                        }
                    }
                    for (Station station1 : Stations) {
                        if (station1.getName().equals(record[1])) {
                            record[9] = record[8];
                            record[8] = "گار";
                            okay = true;
                        }
                    }
                    break;
                }
            }
        }
        return okay;
    }

    public boolean isCommodityWithMaxValueInStation(String station, double value, ArrayList<String[]> records) {

        boolean result = true;
        for (String[] record : records) {
            if (station.equals(record[8]) && value >= Double.parseDouble(record[2])) {
                result = true;
            } else if (station.equals(record[8])) {
                result = false;
                break;
            }
        }
        return result;
    }

}
