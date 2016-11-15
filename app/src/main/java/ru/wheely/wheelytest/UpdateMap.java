package ru.wheely.wheelytest;

import com.google.android.gms.maps.model.LatLng;

public  interface UpdateMap{
        void updateMap(LatLng latLng);
        void updateLocation(LatLng latLng);
    }