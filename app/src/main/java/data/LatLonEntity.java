package data;

/**
 * Created by CoolerBy on 14.11.2016.
 */
public class LatLonEntity extends BaseEntity {
    double lat;
    double lon;

    public LatLonEntity(String lat, String lon) {
        try {
            this.lat = Double.parseDouble(lat);
            this.lon = Double.parseDouble(lon);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
    }

    public LatLonEntity(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
