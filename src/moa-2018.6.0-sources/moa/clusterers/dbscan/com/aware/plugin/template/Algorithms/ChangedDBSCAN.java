package com.aware.plugin.template.Algorithms;

//import android.util.Log;

import com.aware.plugin.template.Objects.ClusterCoordinates;
import com.aware.plugin.template.Objects.ClusterPolygon;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import smile.clustering.PartitionClustering;
import smile.math.Math;
import smile.math.distance.Distance;
import smile.math.distance.Metric;
import smile.neighbor.CoverTree;
import smile.neighbor.LinearSearch;
import smile.neighbor.Neighbor;
import smile.neighbor.RNNSearch;

/**
 * Created by renia on 06.06.17.
 */

public class ChangedDBSCAN<T> extends PartitionClustering<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int UNCLASSIFIED = -1;
    private double minPts;
    private double radius;
    private RNNSearch<T, T> nns;   // T - key, T - object
    private ArrayList <ClusterCoordinates> previous_clusters = new ArrayList<>();
    private ArrayList <ClusterPolygon> clusters_polygon = new ArrayList<>();

    public ChangedDBSCAN(ArrayList <ClusterCoordinates> previous_clusters, T[] data,
                         Distance<T> distance, int minPts, double radius)
    {this(previous_clusters, data, (RNNSearch)(new LinearSearch(data, distance)), minPts, radius);}

    public ChangedDBSCAN(ArrayList <ClusterCoordinates> previous_clusters, T[] data,
                         Metric<T> distance, int minPts, double radius)
    {this(previous_clusters, data, (RNNSearch)(new CoverTree(data, distance)), minPts, radius);}

    public ChangedDBSCAN(ArrayList <ClusterCoordinates> previous_clusters, T[] data,
                         RNNSearch<T, T> nns, int minPts, double radius) {

        if(minPts < 1) {
            throw new IllegalArgumentException("Invalid minPts: " + minPts);
        } else if(radius <= 0.0D) {
            throw new IllegalArgumentException("Invalid radius: " + radius);
        } else {
            this.nns = nns;
            this.minPts = (double)minPts;
            this.radius = radius;
            this.k = 10;
            int n = data.length;
            this.y = new int[n];
            Arrays.fill(this.y, -1);
            this.previous_clusters = previous_clusters;

            // factory used for creating polygons
            GeometryFactory geometryFactory = new GeometryFactory();

            int i;
            for(i = 0; i < data.length; ++i) {
                if(this.y[i] == -1) {
                    List<Neighbor<T, T>> neighbors = new ArrayList();
                    nns.range(data[i], radius, neighbors);
                    if(neighbors.size() < minPts) {
                        this.y[i] = 2147483647;
                    } else {
                        this.y[i] = this.k;

                        for(int j = 0; j < neighbors.size(); ++j) {
                            if(this.y[((Neighbor)neighbors.get(j)).index] == -1) {
                                this.y[((Neighbor)neighbors.get(j)).index] = this.k;
                                Neighbor<T, T> neighbor = (Neighbor)neighbors.get(j);
                                List<Neighbor<T, T>> secondaryNeighbors = new ArrayList();
                                nns.range(neighbor.key, radius, secondaryNeighbors);
                                if(secondaryNeighbors.size() >= minPts) {
                                    neighbors.addAll(secondaryNeighbors);
                                }
                            }

                            if(this.y[((Neighbor)neighbors.get(j)).index] == 2147483647) {
                                this.y[((Neighbor)neighbors.get(j)).index] = this.k;
                            }
                        }

                        ++this.k;
                    }
                }
            }

            this.size = new int[this.k + 1];

            for(i = 0; i < n; ++i) {
                if(this.y[i] == 2147483647) {
                    ++this.size[this.k];
                } else {
                    ++this.size[this.y[i]];
                }
            }

            // zmiana zapisy klastra: środek + promień
            int j;

            for (int l = 0; l < this.k; l++) {

                // create MAR

                Point[] points = new Point[this.size[l]];
//                Log.d("N", "n: " + n);
//                Log.d("K", "k: " + k);
//                Log.d("SIZE", "size: " + this.size[l]);

                double [][] cluster_data = new double [this.size[l]][];
                for (i = 0, j=0; i < n; ++i) {
                    //System.out.println(y[i]);
                    if (this.y[i] == l) {
//                        Log.d("I", "i: " + i);
//                        Log.d("J", "j: " + j);
//                        Log.d("y[i]", "y[i]" + y[i]);
                        cluster_data[j] = (double[]) data[i];
                        Coordinate coord = new Coordinate(cluster_data[j][0], cluster_data[j][1], 1);
                        Point point = geometryFactory.createPoint(coord);
//                        System.out.println(point);
                        points[j] = point;
                        j++;
                    }

                }

                if (points.length != 0) {

                    MultiPoint multiPoint = new MultiPoint(points, geometryFactory);
                    Polygon myRectangle = SmallestSurroundingRectangle.get(multiPoint);

                    // removing NaN from data
                    myRectangle.apply(new RemoveNaNFilter());
                    // returning only points of polygon
                    Coordinate[] coord = myRectangle.getCoordinates();

                    ClusterPolygon cluster_polygon = new ClusterPolygon(myRectangle, coord, j);
                    clusters_polygon.add(cluster_polygon);
                    //System.out.println(myRectangle);
                }

            }

            // aktualizacja klastrów
            if (previous_clusters != null) {

                ArrayList<ClusterPolygon> add_clusters = new ArrayList<>();

                // zmniejsz wagę klastrów
                for (ClusterCoordinates previous_cluster: this.previous_clusters){
                    previous_cluster.weight = (int) Math.round(0.9 * previous_cluster.weight);
                    Polygon previous_cluster_polygon = geometryFactory.createPolygon(previous_cluster.cluster_polygon_coordinates);


                    for (ClusterPolygon cluster: this.clusters_polygon){


                        if (cluster.cluster_polygon.intersects(previous_cluster_polygon)){

                            if (cluster.cluster_polygon.intersection(previous_cluster_polygon).getArea() > 0.5*cluster.cluster_polygon.getArea()){

//                                Log.d("UPS", "lol");
                                previous_cluster_polygon = (Polygon) cluster.cluster_polygon.union(previous_cluster_polygon);
                                previous_cluster_polygon.apply(new RemoveNaNFilter());
                                previous_cluster.cluster_polygon_coordinates = previous_cluster_polygon.getCoordinates();

                                //System.out.println(previous_cluster_polygon);
                                previous_cluster.weight = previous_cluster.weight + cluster.cluster_coordinates.weight;
                                cluster.cluster_coordinates.weight = 0;

                            }

                            else if (cluster.cluster_polygon.intersection(previous_cluster_polygon).getArea() > 0.5*previous_cluster_polygon.getArea()){
//                                Log.d("UPS", "lol2");
                                cluster.cluster_polygon = (Polygon) cluster.cluster_polygon.union(previous_cluster_polygon);
                                cluster.cluster_coordinates.weight = cluster.cluster_coordinates.weight + previous_cluster.weight;
                                previous_cluster.weight = 0;
                            }

                        }
                    }

//                    Log.d("Cluster weight: ", Integer.toString(previous_cluster.weight));
//                    Log.d("Cluster area: ", Double.toString(previous_cluster_polygon.getArea()));
                    if (Math.pow(previous_cluster.weight, 3) > previous_cluster_polygon.getArea()){
                        add_clusters.add(new ClusterPolygon(previous_cluster_polygon, previous_cluster));
                    }

                }
                
                for (ClusterPolygon add_cluster: add_clusters){
                    this.clusters_polygon.add(add_cluster);
                }
            }

        }
    }

    public double getMinPts() {
        return this.minPts;
    }

    public double getRadius() {
        return this.radius;
    }

    public ArrayList<ClusterCoordinates> getClusters() {

        ArrayList<ClusterCoordinates> returnClusters = new ArrayList<>();

        for (ClusterPolygon cluster: this.clusters_polygon){
            if (cluster.cluster_coordinates.weight != 0) {
                returnClusters.add(cluster.cluster_coordinates);
            }
        }

        return returnClusters;}

//    public Coordinate[] getCluster_coordinates(int t) { return this.clusters_coordinates.get(t); }
//    public ArrayList<Coordinate[]> getClusters_coordinates() { return this.clusters_coordinates; }

    public int predict(T x) {
        List<Neighbor<T, T>> neighbors = new ArrayList();
        this.nns.range(x, this.radius, neighbors);
        if((double)neighbors.size() < this.minPts) {
            return 2147483647;
        } else {
            int[] label = new int[this.k + 1];

            int yi;
            for(Iterator var4 = neighbors.iterator(); var4.hasNext(); ++label[yi]) {
                Neighbor<T, T> neighbor = (Neighbor)var4.next();
                yi = this.y[neighbor.index];
                if(yi == 2147483647) {
                    yi = this.k;
                }
            }

            int c = Math.whichMax(label);
            if(c == this.k) {
                c = 2147483647;
            }

            return c;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("DBScan clusters_coordinates of %d data points:%n", new Object[]{Integer.valueOf(this.y.length)}));

        int r;
        for(r = 0; r < this.k; ++r) {
            r = (int)Math.round(1000.0D * (double)this.size[r] / (double)this.y.length);
            sb.append(String.format("%3d\t%5d (%2d.%1d%%)%n", new Object[]{Integer.valueOf(r), Integer.valueOf(this.size[r]), Integer.valueOf(r / 10), Integer.valueOf(r % 10)}));
        }

        r = (int)Math.round(1000.0D * (double)this.size[this.k] / (double)this.y.length);
        sb.append(String.format("Noise\t%5d (%2d.%1d%%)%n", new Object[]{Integer.valueOf(this.size[this.k]), Integer.valueOf(r / 10), Integer.valueOf(r % 10)}));
        return sb.toString();
    }


}
