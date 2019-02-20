package com.aware.plugin.template.IntentServices;

import com.aware.plugin.template.Objects.Cluster;
import com.aware.plugin.template.Objects.ClusterCoordinates;
import com.aware.plugin.template.Algorithms.ChangedDBSCAN;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import org.json.JSONException;


import smile.clustering.BIRCH;
import smile.clustering.Clustering;
import smile.clustering.DBScan;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
//import java.util.Iterator;
import java.io.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;

import smile.data.AttributeDataset;
import smile.data.parser.DelimitedTextParser;
import smile.math.distance.EuclideanDistance;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ClusterLocation {

    // algorithm
    public static String ACTION;
    int minPts;
    double range;
    int k;
    int B;
    int T;
    //public static final String ACTION = "ALGORITHM";
    public static final String ACTION_DBSCAN = "DBSCAN";
    public static final String ACTION_BIRCH = "BIRCH";

    //  algorithm = DBSCAN / BIRCH;
    public ClusterLocation(String algorithm, int mP, double r, int k_t, int B_t, int T_t) {
        ACTION = algorithm;
        minPts = mP;
        range = r;
        k = k_t;
        B = B_t;
        T = T_t;
    }

    //@Override
    // protected
    public void onHandle() {

        //if (ACTION_DBSCAN.equals(action)) {

        Cluster dataset = getDataFromFile("D:/Studia/PP/dane_lokalizacja/dane_lokalizacja.txt");
        //Cluster dataset = getDataFromFile("D:/Studia/PP/dane_lokalizacja/dane.txt");

        //ArrayList<Cluster> clusters = clusterDBSCAN(dataset, minPts, range);
        ArrayList<ClusterCoordinates> clusters = cluster_changedDBSCAN(dataset, minPts, range);


        ArrayList<ClusterCoordinates> clusters_od = new ArrayList<>();
        //System.out.println("Show");
        //System.out.println(dataset);
        //sendDataToFileMAR(dataset.cluster_data, clusters);

        System.out.println("Start_test");
        sendDataToFileMAR(dataset, clusters);
        clusters_od = getDataFromFileMAR();
        System.out.println("End_test");

        //sendDataToFile(dataset, clusters);
        //}
        //else if(ACTION_BIRCH.equals(action)){

        //	Cluster dataset = getDataFromFile("D:/Studia/PP/dane_lokalizacja/dane.txt");
        //    ArrayList<Cluster> clusters = clusterBIRCH(dataset, minPts, k, B, T);

        //}

    }

    private ArrayList<Cluster> clusterDBSCAN(Cluster dataset, int minPts, double radius) {

        ArrayList<Cluster> clusters = new ArrayList<Cluster>();

        DBScan<double[]> dbscan = new DBScan<>(dataset.cluster_data, new EuclideanDistance(), minPts, radius);
        for (int k = 0; k < dbscan.getNumClusters(); k++) {
            Cluster cluster = new Cluster();
            cluster.cluster_data = new double[dbscan.getClusterSize()[k]][];
            for (int i = 0, j = 0; i < dataset.cluster_data.length; i++) {
                if (dbscan.getClusterLabel()[i] == k) {
                    cluster.cluster_data[j++] = dataset.cluster_data[i];
                }
            }
            clusters.add(cluster);
            System.out.println(cluster);
        }

        return clusters;

    }

    private ArrayList<ClusterCoordinates> cluster_changedDBSCAN(Cluster dataset, int minPts, double radius) {

        ArrayList<ClusterCoordinates> previous_clusters = getDataFromFileMAR();
        //ArrayList<ClusterCoordinates> previous_clusters = null;
        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();

        //ChangedDBSCAN<double[]> dbscan = new ChangedDBSCAN(previous_clusters, dataset.cluster_data, new EuclideanDistance(), minPts, radius);
        ChangedDBSCAN<double[]> dbscan = new ChangedDBSCAN<>(previous_clusters, dataset.cluster_data, new EuclideanDistance(), minPts, radius);

//        for (int k = 0; k < dbscan.getNumClusters(); k++) {
//            ClusterPolygon cluster = new ClusterPolygon(dbscan.getCluster_coordinates(k));
//            clusters.add(cluster);
//        }
        clusters = dbscan.getClusters();

        return clusters;

    }

    private Cluster getDataFromFile(String fileName) {

        //  get data from file in Assets folder
        double[][][] dataset = null;

        int datasetIndex = 0;
        String[] datasetName = {"geoLocal3"};

        if (dataset == null) {
            dataset = new double[datasetName.length][][];
            DelimitedTextParser parser = new DelimitedTextParser();
            parser.setDelimiter(",");
            try {

                //AssetManager am = this.getAssets();
                File initialFile = new File(fileName);
                InputStream is = new FileInputStream(initialFile);

                //InputStream is = am.open(fileName);

                AttributeDataset data = parser.parse(datasetName[datasetIndex], is);
                dataset[datasetIndex] = data.toArray(new double[data.size()][]);

            } catch (Exception e) {

                System.err.println(e);
            }
        }

        Cluster returnData = new Cluster();
        returnData.cluster_data = dataset[0];
        returnData.getSeries();

        return returnData;
    }


    private void sendDataToFile(Cluster locationPoints, ArrayList<Cluster> clusters) {

        // convert locationPoints to json
        Gson gson_locationPoints = new Gson();
        byte[] bytes_locationPoints = gson_locationPoints.toJson(locationPoints).getBytes();

        // convert clusters to json
        Gson gson_clusters = new Gson();
        //byte[] bytes_clusters = gson_clusters.toJson(clusters).getBytes();


        // convert clusters to json
        Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();
        //String json_clusters = gson_clusters.toJson(clusters, listType);
        byte[] bytes_clusters = gson_clusters.toJson(clusters, listType).getBytes();

        JSONObject obj = new JSONObject();
        obj.put("clusters", bytes_clusters);
        obj.put("locationPoints", bytes_locationPoints);

        // try-with-resources statement based on post comment below :)
        try (FileWriter file = new FileWriter("D:/Studia/PP/dane_lokalizacja/temp.txt")) {
            file.write(obj.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + obj);
        } catch (Exception e) {
        }

    }


    private void sendDataToFileMAR(Cluster locationPoints, ArrayList<ClusterCoordinates> clusters) {

        Gson gson_locationPoints = new Gson();

        System.out.println("!!!!!Save!!!!!!");
        System.out.println(clusters);

        byte[] bytes_locationPoints = gson_locationPoints.toJson(locationPoints).getBytes();

        // convert clusters to json
        Gson gson_clusters = new Gson();
        Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();
        String json_clusters = gson_clusters.toJson(clusters, listType);
        //byte[] bytes_clusters = gson_clusters.toJson(clusters, listType).getBytes();

        System.out.println("json_save");
        System.out.println(json_clusters);
        JSONObject obj = new JSONObject();
        obj.put("clusters", json_clusters);
        //obj.put("clusters", bytes_clusters);
        //obj.put("locationPoints", bytes_locationPoints);
        //System.out.println("Show");
        //System.out.println(obj);

        try (FileWriter file = new FileWriter("D:/Studia/PP/dane_lokalizacja/temp3.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject obj2 = new JSONObject();
        //obj.put("clusters", bytes_clusters);
        obj2.put("locationPoints", bytes_locationPoints);
        try (FileWriter file = new FileWriter("D:/Studia/PP/dane_lokalizacja/temp4.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<ClusterCoordinates> getDataFromFileMAR() {

        JSONParser parser = new JSONParser();
        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();


        try (FileReader file = new FileReader("D:/Studia/PP/dane_lokalizacja/temp3.json")) {

            Object objj = parser.parse(file);

            JSONObject obj = (JSONObject) objj;

            String json_clusters = (String) obj.get("clusters");
            System.out.println("json_read");
            System.out.println(json_clusters);
            Gson gson_clusters = new Gson();
            Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();
            clusters = gson_clusters.fromJson(json_clusters, listType);

            System.out.println("!!!!!!!Read!!!!!");
            System.out.println(clusters);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return clusters;
    }

    private void sendDataToFileMAR22(Cluster locationPoints, ArrayList<ClusterCoordinates> clusters) {

        // convert locationPoints to json
        Gson gson_locationPoints = new Gson();
        System.out.println("!!!!!Save!!!!!!");
        System.out.println(clusters);

        byte[] bytes_locationPoints = gson_locationPoints.toJson(locationPoints).getBytes();

        // convert clusters to json
        Gson gson_clusters = new Gson();
        Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();
        String json_clusters = gson_clusters.toJson(clusters, listType);
        byte[] bytes_clusters = gson_clusters.toJson(clusters, listType).getBytes();


        JSONObject obj = new JSONObject();
        //obj.put("clusters", clusters);
        obj.put("clusters", bytes_clusters);
        //obj.put("locationPoints", bytes_locationPoints);
        //System.out.println("Show");
        System.out.println(obj);

        try (FileWriter file = new FileWriter("D:/Studia/PP/dane_lokalizacja/temp3.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject obj2 = new JSONObject();
        //obj.put("clusters", bytes_clusters);
        obj2.put("locationPoints", bytes_locationPoints);
        try (FileWriter file = new FileWriter("D:/Studia/PP/dane_lokalizacja/temp4.json")) {

            file.write(obj.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<ClusterCoordinates> getDataFromFileMAR222() {

        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();

        try {

            BufferedInputStream bStream = new BufferedInputStream(new FileInputStream("D:/Studia/PP/dane_lokalizacja/temp3.json"));
            byte[] bytes = new byte[4096];
            int read;
            while ((read = bStream.read(bytes)) > 0) {
                System.out.println("Get_data_1");
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(read);
                System.out.println(bytes);
            }
            System.out.println("Get_data");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(bytes);


            BufferedInputStream bStream2 = new BufferedInputStream(new FileInputStream("D:/Studia/PP/dane_lokalizacja/temp4.json"));
            byte[] bytes2 = new byte[4096];
            int read2;
            while ((read2 = bStream2.read(bytes2)) > 0) {
            }
            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //System.out.println(bytes2);

            //ByteArrayInputStream cluster_temp = new ByteArrayInputStream(bytes);
            //JsonReader reader = Json.createReader(cluster_temp);
            //JsonObject clus =  reader.readObject();
            //reader.close();

            //ArrayList<ClusterCoordinates> clus = new ArrayList<ClusterCoordinates>();
            //System.out.println("Get_data");
            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //System.out.println(bytes);

            if (bytes != null) {
                //byte[] byte_location = serverData.getBlob(serverData.getColumnIndex(Provider.TableOne_Data.LOCATION_DATA));
                byte[] byte_clusters = bytes;

                //convert blob to clusters data
                String json_clusters = new String(byte_clusters);
                Gson gson_clusters = new Gson();
                clusters = gson_clusters.fromJson(json_clusters, new TypeToken<ArrayList<ClusterCoordinates>>(){}.getType());

                System.out.println("!!!!!Read!!!!!!!!!");
                System.out.println(clusters);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            //} catch (ParseException e) {
            //    e.printStackTrace();
        }

        return clusters;
    }





    private ArrayList<ClusterCoordinates> getDataFromFileMAR22() {

        JSONParser parser = new JSONParser();
        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();

        try {

            BufferedInputStream bStream = new BufferedInputStream(new FileInputStream("D:/Studia/PP/dane_lokalizacja/temp3.json"));
            byte[] bytes = new byte[4096];
            int read;
            while ((read = bStream.read(bytes)) > 0) {
                System.out.println("Get_data_1");
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(read);
                System.out.println(bytes);
            }
            System.out.println("Get_data");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(bytes);


            BufferedInputStream bStream2 = new BufferedInputStream(new FileInputStream("D:/Studia/PP/dane_lokalizacja/temp4.json"));
            byte[] bytes2 = new byte[4096];
            int read2;
            while ((read2 = bStream2.read(bytes2)) > 0) {
            }
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(bytes2);

            //ByteArrayInputStream cluster_temp = new ByteArrayInputStream(bytes);
            //JsonReader reader = Json.createReader(cluster_temp);
            //JsonObject clus =  reader.readObject();
            //reader.close();

            //ArrayList<ClusterCoordinates> clus = new ArrayList<ClusterCoordinates>();
            System.out.println("Get_data");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(bytes);

            Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();

            Gson gson_clusters = new Gson();
            String decodedData = new String(bytes);
            System.out.println(decodedData);
            ArrayList<ClusterCoordinates> result = gson_clusters.fromJson(decodedData, listType);

            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println(result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        //} catch (ParseException e) {
        //    e.printStackTrace();
        }
        /*
        try {

            //File file_test = new File("D:/Studia/PP/dane_lokalizacja/temp3.json");
            Object obj = parser.parse(new FileReader("D:/Studia/PP/dane_lokalizacja/temp3.json"));
            //if (file_test.length() != 0) {
                //Object obj = parser.parse(new FileReader("D:\Studia\PP\dane_lokalizacja\temp3.txt"));

                JSONObject jsonObject = (JSONObject) obj;

                String name = (String) jsonObject.get("locationPoints");
                System.out.println(jsonObject);
                System.out.println(name);
                String name2 = (String) jsonObject.get("clusters");
                System.out.println(name2);
                //byte[] byte_location = jsonObject.get("locationPoints");
                //byte[] byte_clusters = jsonObject.get("clusters");
                //byte[] byte_location = jsonObject.toString().getBytes("locationPoints");
                //byte[] byte_clusters = jsonObject.toString().getBytes("clusters");

                //convert blob to clusters data
                //String json_clusters = new String(byte_clusters);
                //Gson gson_clusters = new Gson();
                //clusters = gson_clusters.fromJson(json_clusters, new TypeToken<ArrayList<ClusterCoordinates>>() {
                //}.getType());

                //return clusters;
            //}

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
*/
        return clusters;
    }

    private ArrayList<Cluster> clusterBIRCH(Cluster dataset, int minPts, int k, int B, int T) {

        int d = dataset.cluster_data[0].length;

        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        BIRCH birch = new BIRCH(d, B, T);
        for (int i = 0; i < dataset.cluster_data.length; i++)
            birch.add(dataset.cluster_data[i]);
        birch.partition(k, minPts);
        int[] membership = new int[dataset.cluster_data.length];
        int[] clusterSize = new int[k];
        for (int i = 0; i < dataset.cluster_data.length; i++) {
            membership[i] = birch.predict(dataset.cluster_data[i]);
            if (membership[i] != Clustering.OUTLIER) {
                clusterSize[membership[i]]++;
            }
        }

        // TODO: centorids
        double[][] centroids = birch.centroids();

        for (int count = 0; count < k; count++) {
            if (clusterSize[count] > 0) {
                Cluster cluster = new Cluster();
                cluster.cluster_data = new double[clusterSize[count]][];
                for (int i = 0, j = 0; i < dataset.cluster_data.length; i++) {
                    if (membership[i] == count) {
                        cluster.cluster_data[j++] = dataset.cluster_data[i];
                    }
                }
                clusters.add(cluster);
            }
        }

        return clusters;
    }
}
