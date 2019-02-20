package moa.clusterers.dbscan;

import java.util.ArrayList;

import java.util.LinkedList;
import com.*;
import moa.cluster.Cluster;
import moa.cluster.Clustering;
import moa.clusterers.AbstractClusterer;
import moa.core.Measurement;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.FlagOption;
import com.yahoo.labs.samoa.instances.Instance;

public class DBscan extends AbstractClusterer{

    private static final long serialVersionUID = 1L;

    public IntOption minPtsOption = new IntOption("minPts",
            'm', "Min number of points.", 3);

    public IntOption rangeOption = new IntOption("range",
            'r', "Radius of clusters.", 500);

    public static String ACTION;
    private int minPts;
    private double range;
    private int k;
    private int B;
    private int T;
    private ArrayList<ClusterCoordinates> clusters;
    private long timestamp = -1;

    @Override
    public void resetLearningImpl() {
        ACTION = "DBSCAN";
        minPts = minPtsOption.getValue();
        range = rangeOption.getValue();
        k = 1;
        B = 1;
        T = 1;
        this.initialized = false;
        this.clusters = new ArrayList<>();
    }

    @Override
    public void trainOnInstanceImpl(Instance instance) {

        timestamp++;
        // Initialize
        if (!initialized) {
            Cluster dataset = getDataFromFile("D:/Studia/PP/dane_lokalizacja/dane_lokalizacja.txt");
            clusters = cluster_changedDBSCAN(dataset, minPts, range);
            sendDataToFileMAR(dataset, clusters);
            initialized = true;
            return;
        }
    }

    @Override
    public Clustering getClusteringResult() {
        if (!initialized) {
            return new Clustering(new Cluster[0]);
        }

        ArrayList<ClusterCoordinates> clust = new ArrayList<>();
        clust = getDataFromFileMAR();
        return clust;
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

    /*
    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return null;
    }

    @Override
    public void getModelDescription(StringBuilder out, int indent) {
    }

    @Override
    public boolean implementsMicroClusterer() {
        return true;
    }

    @Override
    public Clustering getMicroClusteringResult() {
        return getClustering(timestamp, -1);
    }

    @Override
    public Clustering getClusteringResult() {
        return null;
    } */
}