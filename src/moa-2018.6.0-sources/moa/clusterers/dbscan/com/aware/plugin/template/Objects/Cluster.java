package com.aware.plugin.template.Objects;

//import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by renia on 19.05.17.
 */

public class Cluster implements Serializable {


//    public String TAG = "AWARE::"+"Cluster";

    public double [][] cluster_data;

    // for visualisation
    public Number[] x_latitude;
    public Number[] y_longitude;

    // Series for visualisation (only lat&long - 2D Scatter Plot)
    public void getSeries(){

        int lenght = cluster_data.length;
//        Log.d(TAG, "Data length: " + lenght);

        x_latitude = new Number[lenght];
        y_longitude = new Number[lenght];

        for (int i=0;i<lenght;i++) {


            x_latitude[i] = cluster_data[i][0];
            y_longitude[i] = cluster_data[i][1];

            //System.out.print(x_latitude[i]);
            //System.out.printf("%n");
            //System.out.print(y_longitude[i]);
            //System.out.printf("%n");
        }

//        Log.d(TAG, "Series Done");
    }

}
