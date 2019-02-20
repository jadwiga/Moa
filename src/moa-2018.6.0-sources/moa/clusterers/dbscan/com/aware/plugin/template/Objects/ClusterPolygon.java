package com.aware.plugin.template.Objects;

//import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.io.Serializable;

/**
 * Created by renia on 09.07.17.
 */

public class ClusterPolygon implements Serializable {

    public ClusterCoordinates cluster_coordinates;
    public Polygon cluster_polygon;

    public ClusterPolygon(Polygon polygon, Coordinate[] data, int w) {
        this.cluster_polygon = polygon;
        this.cluster_coordinates = new ClusterCoordinates(data, w);
    }

    public ClusterPolygon(Polygon polygon, ClusterCoordinates cluster_coordinates) {
        this.cluster_polygon = polygon;
        this.cluster_coordinates = cluster_coordinates;
    }


}
