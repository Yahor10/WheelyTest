package ru.wheely.wheelytest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class MapsActivity extends BaseFragmentMapActivity  {

    public static String ACTION_START_MAP = "ru.wheely.wheelytest.START_MAP";

    public static Intent buildIntent(Context context){
        return new Intent(context,MapsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
