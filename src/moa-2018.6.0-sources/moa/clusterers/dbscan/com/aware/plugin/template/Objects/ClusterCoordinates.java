package com.aware.plugin.template.Objects;

//import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.Serializable;

/**
 * Created by renia on 22.07.17.
 */

// object used for sending to and from server
public class ClusterCoordinates implements Serializable {


//    public String TAG = "AWARE::"+"ClusterCoordinates";

    public Coordinate[] cluster_polygon_coordinates;
    public int weight;

    public ClusterCoordinates(Coordinate[] data, int w){
        cluster_polygon_coordinates = data;
        weight = w;}

    // series for visualisation
    public Number[] x_latitude;
    public Number[] y_longitude;

    // Series for visualisation (only lat&long - 2D Scatter Plot) POINTS
    public void getSeriesForPoints(){

        int lenght = cluster_polygon_coordinates.length;

        x_latitude = new Number[lenght];
        y_longitude = new Number[lenght];

        //x_latitude[lenght-1] = cluster_polygon_coordinates[0].x;
        //y_longitude[lenght-1] = cluster_polygon_coordinates[0].y;
        for (int i=0;i<lenght;i++) {

            x_latitude[i] = cluster_polygon_coordinates[i].x;
            y_longitude[i] = cluster_polygon_coordinates[i].y;
//            Log.d(TAG, Double.toString(cluster_polygon_coordinates[i].x));
//            Log.d(TAG, Double.toString(cluster_polygon_coordinates[i].y));
//            Log.d(TAG, Double.toString(cluster_polygon_coordinates[i].z));
        }

    }

}
