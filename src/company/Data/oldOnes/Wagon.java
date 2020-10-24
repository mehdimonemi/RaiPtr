package company.Data.oldOnes;

/**
 * Created by Monemi_M on 10/25/2017.
 */
public class Wagon {
    private String name;
    private String owner;
    private double weight;
    private double length;
    private double loadWeight;

    public Wagon(String name, String owner, double weight, double loadWeight, double length) {
        this.name = name;
        this.owner = owner;
        this.weight = weight;
        this.length = length;
        this.loadWeight = loadWeight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getLoadWeight() {
        return loadWeight;
    }

    public void setLoadWeight(double loadWeight) {
        this.loadWeight = loadWeight;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Wagon{" +
                "name='" + name + '\'' +
                '}';
    }
}
