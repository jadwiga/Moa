package com.aware.plugin.template.Algorithms;
import java.util.logging.Logger;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;


/**
 *
 * @author julien Gaffuri
 *
 */
public class SmallestSurroundingRectangle {
    public static Logger logger = Logger.getLogger(SmallestSurroundingRectangle.class.getName());  // logger - do logowania getLogger -> znajdz lub stworz logger

    public static Polygon get(Geometry geom){
        return get(geom, geom.getFactory());
    }

    public static Polygon get(Geometry geom, GeometryFactory gf){
        Geometry hull_ = (new ConvexHull(geom)).getConvexHull();  // oblicza zewnatrzny obwod (wypuklosc)
        if (!(hull_ instanceof Polygon)) return null;
        Polygon convHull = (Polygon)hull_;                        // obwod jako wielokat

        Coordinate c = geom.getCentroid().getCoordinate();        // zwraca pierwszy wierzcholek srodka (wspolrzedna?)
        Coordinate[] coords = convHull.getExteriorRing().getCoordinates();  // LineString !!! zwraca wspolrzedne wierzcholkow

        double minArea = Double.MAX_VALUE, minAngle = 0.0;
        Polygon ssr = null;
        Coordinate ci = coords[0], cii;
        for(int i=0; i<coords.length-1; i++){
            cii = coords[i+1];
            double angle = Math.atan2(cii.y-ci.y, cii.x-ci.x);
            Polygon rect = (Polygon) Rotation.get(convHull, c, -1.0*angle, gf).getEnvelope();  // tworzy wielobok o podanych parametrach (obraca) i najmn pow
            double area = rect.getArea();
            if (area < minArea) {
                minArea = area;
                ssr = rect;
                minAngle = angle;
            }
            ci = cii;
        }
        return Rotation.get(ssr, c, minAngle, gf);  // zwraca optymalny wielobok (o ustalonych parametrach)
    }

    public static Polygon get(Geometry geom, boolean preserveSize){
        return get(geom, geom.getFactory(), preserveSize);
    }

    public static Polygon get(Geometry geom, GeometryFactory gf, boolean preserveSize){
        if( !preserveSize ) return get(geom, gf);

        Polygon out = get(geom, gf);
        double ini = geom.getArea();
        double fin = out.getArea();

        if(fin == 0) {
            logger.warning("Failed to preserve size of smallest surrounding rectangle: Null final area.");
            return out;
        }

        return Scaling.get(out, out.getCentroid().getCoordinate(), Math.sqrt(ini/fin), gf);  // zwraca zmieniony (przeskalowany) wielobok
    }

}
