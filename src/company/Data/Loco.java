package company.Data;

/**
 * Created by Monemi_M on 03/07/2018.
 */
public class Loco {
    private int kind;
    private int number;
    private int[] powers;

    public Loco(int kind, int[] powers) {
        this.kind = kind;
        this.powers = powers;
    }

    public Loco(int kind, int number, int[] powers) {
        this.kind = kind;
        this.number = number;
        this.powers = powers;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public int[] getPowers() {
        return powers;
    }

    public void setPowers(int[] powers) {
        this.powers = powers;
    }
}
