package ir.rai.Data;

import java.util.HashMap;

public class Dizel extends Fleet {
    private HashMap</*block*/Integer,/*power*/Integer> allowedBlock;
    private HashMap</*trainArc*/Integer,/*power*/Integer> trainArcs= new HashMap<>();

    public HashMap<Integer, Integer> getTrainArcs() {
        return trainArcs;
    }

    public void setTrainArcs(HashMap<Integer, Integer> trainArcs) {
        this.trainArcs = trainArcs;
    }

    public Dizel(HashMap<Integer,Integer> allowedBlock) {
        super();
        this.allowedBlock = allowedBlock;
    }

    public HashMap<Integer,Integer> getAllowedBlock() {
        return allowedBlock;
    }

    public void setAllowedBlock(HashMap<Integer,Integer> allowedBlock) {
        this.allowedBlock = allowedBlock;
    }
}
