package data;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public class LatLonEntity extends BaseEntity {
    String lat;
    String lon;

    public LatLonEntity(String lat, String lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
