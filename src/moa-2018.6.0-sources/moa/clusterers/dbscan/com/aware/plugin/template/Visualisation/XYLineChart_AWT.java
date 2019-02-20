package com.aware.plugin.template.Visualisation;

import java.awt.Color;
import java.awt.BasicStroke;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import com.aware.plugin.template.Objects.Cluster;
import com.aware.plugin.template.Objects.ClusterCoordinates;
//import com.aware.plugin.template.Objects.ClusterPolygon;

import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
//import java.util.Iterator;
import java.io.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

public class XYLineChart_AWT extends ApplicationFrame {

    public XYLineChart_AWT( String applicationTitle, String chartTitle ) {
        super(applicationTitle);

        Color [] colors = {Color.CYAN, Color.darkGray, Color.GREEN, Color.RED, Color.BLUE, Color.lightGray,
                Color.GRAY, Color.YELLOW, Color.MAGENTA, Color.WHITE};

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                chartTitle ,
                "Longitude" ,
                "Latitude" ,
                createDataset() ,
                PlotOrientation.VERTICAL ,
                true , true , false);

        ChartPanel chartPanel = new ChartPanel( xylineChart );
        //chartPanel.setPreferredSize( new java.awt.Dimension( 1360 , 1367 ) );
        final XYPlot plot = xylineChart.getXYPlot( );

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        //domain.setRange(39.9721*1000000, 39.9871*1000000);
        domain.setRange(39.825*1000000, 39.95*1000000);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        //range.setRange(116.2989*1000000, 116.3392*1000000);
        range.setRange(116.3*1000000, 116.45*1000000);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );

        ArrayList<ClusterCoordinates> dataFromServerMAR = getDataFromFileMAR();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(dataFromServerMAR);

        int i = 0;

        // visualise each set of points
        for (ClusterCoordinates cluster: dataFromServerMAR){

//            //get points Series
            cluster.getSeriesForPoints();
            renderer.setSeriesPaint( i , colors[i]);
            renderer.setSeriesStroke( i , new BasicStroke( 10.0f-i ) );

            //renderer.setSeriesShape( 0, new Ellipse2D.Double( -3, -3, 6, 6 ) );
            //renderer.setSeriesShape( 4, new Rectangle( 39875000, 116375000, 25000, 25000 ) );
            //renderer.setSeriesLinesVisible( 0, true );
            //renderer.setSeriesShapesVisible( 0, true );
            i++;
        }

        //plot.addSeries(new SimpleXYSeries(Arrays.asList(cluster.x_latitude), Arrays.asList(cluster.y_longitude),"Series_" + i),
        //        new LineAndPointFormatter(colors[i%10], colors[i%10], null, null));
        //renderer.setSeriesPaint( 0 , Color.RED );
        //renderer.setSeriesPaint( 1 , Color.GREEN );
        //renderer.setSeriesPaint( 2 , Color.YELLOW );
        //renderer.setSeriesStroke( 0 , new BasicStroke( 4.0f ) );
        //renderer.setSeriesStroke( 1 , new BasicStroke( 3.0f ) );
        //renderer.setSeriesStroke( 2 , new BasicStroke( 2.0f ) );
        plot.setRenderer( renderer );
        setContentPane( chartPanel );
    }

    private XYDataset createDataset( ) {

        final XYSeriesCollection dataset = new XYSeriesCollection( );
        //System.out.println("Done2");
        ArrayList<ClusterCoordinates> dataFromServerMAR = getDataFromFileMAR();

        //int i = 0;

        for (ClusterCoordinates cluster: dataFromServerMAR){

            cluster.getSeriesForPoints();
            String ccc = cluster.toString();
            //String name = "polygon" + ccc;
            final XYSeries name = new XYSeries( ccc );
            name.add( cluster.x_latitude[0] , cluster.y_longitude[0] );
            name.add( cluster.x_latitude[1] , cluster.y_longitude[1] );
            name.add( cluster.x_latitude[2] , cluster.y_longitude[2] );
            name.add( cluster.x_latitude[3] , cluster.y_longitude[3] );
            //name.add( Arrays.asList(cluster.x_latitude[0]) , Arrays.asList(cluster.y_longitude[0]) );
            //name.add( Arrays.asList(cluster.x_latitude[1]) , Arrays.asList(cluster.y_longitude[1]) );
            //name.add( Arrays.asList(cluster.x_latitude[2]) , Arrays.asList(cluster.y_longitude[2]) );
            //name.add( Arrays.asList(cluster.x_latitude[3]) , Arrays.asList(cluster.y_longitude[3]) );

            //plot.addSeries(new SimpleXYSeries(Arrays.asList(cluster.x_latitude), Arrays.asList(cluster.y_longitude),"Series_" + i),
                    //        new LineAndPointFormatter(colors[i%10], colors[i%10], null, null));
//            //get points Series
            //cluster.getSeriesForPoints();
            dataset.addSeries( name );

            //i++;
        }

        return dataset;
    }

    private void sendDataToFileMAR(Cluster locationPoints, ArrayList<ClusterCoordinates> clusters) {

        Gson gson_locationPoints = new Gson();

        System.out.println("!!!!!Save!!!!!!");
        System.out.println(clusters);

        byte[] bytes_locationPoints = gson_locationPoints.toJson(locationPoints).getBytes();

        // convert clusters to json
        Gson gson_clusters = new Gson();
        java.lang.reflect.Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();
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
            java.lang.reflect.Type listType = new TypeToken<ArrayList<ClusterCoordinates>>() {}.getType();
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

    private ArrayList<ClusterCoordinates> getDataFromServerMAR_old(){

        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();

        try {

            BufferedInputStream bStream = new BufferedInputStream(new FileInputStream("D:/Studia/PP/dane_lokalizacja/temp3.json"));
            byte[] byte_clusters = new byte[4096];
            int read;
            while ((read = bStream.read(byte_clusters)) > 0) {
            }

            BufferedInputStream bStream2 = new BufferedInputStream(new FileInputStream("D:/Studia/PP/dane_lokalizacja/temp4.json"));
            byte[] byte_location = new byte[4096];
            int read2;
            while ((read2 = bStream2.read(byte_location)) > 0) {
            }

            //String json_clusters = new String(byte_clusters);
            ///Gson gson_clusters = new Gson();
            //clusters = gson_clusters.fromJson(json_clusters, new TypeToken<ArrayList<ClusterCoordinates>>(){}.getType());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clusters;
    }





/*
    private ArrayList<ClusterCoordinates> getDataFromServerMAR(){

        //serverData.moveToFirst();
        System.out.println("Show1");
        JSONParser parser = new JSONParser();
        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();

        //JSONParser parser = new JSONParser();

        try {

            File file_test = new File("D:\\Studia\\PP\\dane_lokalizacja\\temp3.txt");

            if (file_test.length() != 0) {
                System.out.println("Show2");
                Object obj = parser.parse(new FileReader("D:\\Studia\\PP\\dane_lokalizacja\\temp3.txt"));

                JSONObject jsonObject = (JSONObject) obj;
                System.out.println(jsonObject);

                byte[] byte_location = (byte []) jsonObject.get("locationPoints");
                System.out.println(byte_location);

                byte[] byte_clusters = (byte []) jsonObject.get("clusters");
                System.out.println(byte_clusters);


                //FileReader file = new FileReader("D:\Studia\PP\dane_lokalizacja\temp3.txt");

                //BufferedReader br = new BufferedReader(file);
                //String sCurrentLine;

                //while ((sCurrentLine = br.readLine()) != null) {
                 //   System.out.println(sCurrentLine);
                //}
                //JSONParser parser;
                //JSONObject json = (JSONObject) parser.parse(sCurrentLine);
                //JSONObject jsonObject = parser.parse();
                //JSONObject jsonObject = (JSONObject) sCurrentLine;

                ///byte[] byte_location = jsonObject.toString().getBytes("locationPoints");
                //byte[] byte_clusters = jsonObject.toString().getBytes("clusters");

        // convert blob to location points
//        String json_location = new String(byte_location);
//        Gson gson_location = new Gson();
//        Cluster locationPoints = gson_location.fromJson(json_location, new TypeToken<Cluster>(){}.getType());

        //convert blob to clusters data
                String json_clusters = new String(byte_clusters);
                Gson gson_clusters = new Gson();
                clusters = gson_clusters.fromJson(json_clusters, new TypeToken<ArrayList<ClusterCoordinates>>() {
                }.getType());

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return clusters;
    } */

/*
    private ArrayList<ClusterCoordinates> getDataFromServerMAR() {

        JSONParser parser = new JSONParser();
        ArrayList<ClusterCoordinates> clusters = new ArrayList<>();

        try {

            File file_test = new File("D:/Studia/PP/dane_lokalizacja/temp2.txt");
            Object obj = parser.parse(new FileReader("D:/Studia/PP/dane_lokalizacja/temp2.txt"));
            if (file_test.length() != 0) {
                //Object obj = parser.parse(new FileReader("D:\Studia\PP\dane_lokalizacja\temp3.txt"));

                JSONObject jsonObject = (JSONObject) obj;

                String name = (String) jsonObject.get("locationPoints");
                System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!");
                System.out.println(jsonObject);
                System.out.println(name);
                String name2 = (String) jsonObject.get("clusters");
                System.out.println(name2);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return clusters;
    }
    */
}