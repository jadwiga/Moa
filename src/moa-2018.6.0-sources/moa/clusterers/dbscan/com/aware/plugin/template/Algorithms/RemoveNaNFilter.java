package com.aware.plugin.template.Algorithms;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;

/**
 * Created by renia on 21.07.17.
 */

public class RemoveNaNFilter implements CoordinateFilter {
//    int j;
//    RemoveNaNFilter(int j){this.j=j;}
    public void filter(Coordinate coord) {
        coord.z = 1;
    }
}
