package ir.rai.Data;

import ir.rai.Data.oldOnes.Block;

import java.util.ArrayList;

/**
 * Created by monemi_m on 11/18/2017.
 */
public class Path {
    private int id;
    private ArrayList<Block> blocks = new ArrayList<>();

    public Path(int id, ArrayList<Block> blocks) {
        this.id = id;
        this.blocks = blocks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Block> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<Block> blocks) {
        this.blocks = blocks;
    }
}
