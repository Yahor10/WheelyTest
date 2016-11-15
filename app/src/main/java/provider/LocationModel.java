package provider;

import android.database.Cursor;

/**
 * Created by CoolerBy on 29.09.2016.
 */
public class LocationModel extends BaseModel {

    public static final String LAT = "number";
    public static final String LON = "type";


    private final Double lat,lon;

    public LocationModel(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(ID);
        id = cursor.getInt(columnIndex);
        columnIndex = cursor.getColumnIndex(LAT);
        lat = cursor.getDouble(columnIndex);
        columnIndex = cursor.getColumnIndex(LON);
        lon = cursor.getDouble(columnIndex);
    }


    @Override
    public String toString() {
        return "LocationModel{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }

    public Double getLon() {
        return lon;
    }

    public Double getLat() {
        return lat;
    }
}
