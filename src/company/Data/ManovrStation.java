package company.Data;

import static company.backend1.ReadTypicalData.Stations;

/**
 * Created by Monemi_M on 10/07/2017.
 */
public class ManovrStation extends Station {
    int mainId;
    public static int counter = 0;

    public ManovrStation(int id, String name) {
        super(id, name);
        setMainId(name);
        counter++;
    }

    public int getMainId() {
        return mainId;
    }

    public void setMainId(String name) {
        for (Station station : Stations) {
            if (name.equals(station.getName())) {
                this.mainId = station.getId();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ManovrStation that = (ManovrStation) o;

        return mainId == that.mainId;
    }

    @Override
    public int hashCode() {
        return mainId;
    }
}
