package com.example.blueheart;


import android.graphics.Color;
import android.util.Log;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import java.util.ArrayList;



public class graphUtilities {


    public static void setupChart(LineChart mychart) {

//        mychart = mychart.findViewById(id);
        mychart.setDrawGridBackground(false);
        // no description text
        mychart.getDescription().setEnabled(false);
        // enable touch gestures
        mychart.setTouchEnabled(true);
        // enable scaling and dragging
        mychart.setDragEnabled(true);
        mychart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mychart.setPinchZoom(true);
        mychart.getAxisLeft().setDrawGridLines(false);
        mychart.getAxisRight().setEnabled(false);
//        chart.getXAxis().setDrawGridLines(false);
//        chart.getXAxis().setDrawAxisLine(false);
        mychart.getXAxis().setTextColor(Color.WHITE);
        LineData data = new LineData();
        mychart.setData(data);

        XAxis xl = mychart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mychart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mychart.getAxisRight();
        rightAxis.setEnabled(false);

    }


    public static LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "1");
        set.setColor(Color.RED);
        set.setLineWidth(0.5f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawFilled(false);
        return set;
    }

    public static ScatterDataSet createSetscat() {

        ScatterDataSet set = new ScatterDataSet(null, "scat");
        set.setColor(Color.CYAN);

        set.setDrawValues(false);

        return set;
    }

    public static LineDataSet createSet2() {

        LineDataSet set2 = new LineDataSet(null, "2");
        set2.setColor(Color.BLACK);
        set2.setLineWidth(0.5f);
        set2.setDrawValues(false);
        set2.setDrawCircles(false);
        set2.setMode(LineDataSet.Mode.STEPPED);
        set2.setDrawFilled(false);
        return set2;
    }

    public static void setData0(LineChart chart,float Value,int visibility) {

        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            if (set.getLabel() != "1") {
                set.clear();
                data.removeDataSet(0);
                set = createSet();
                set.addEntry(new Entry(set.getEntryCount(),Value ));
                data.addDataSet(set);
                data.notifyDataChanged();
                chart.notifyDataSetChanged();
                YAxis leftAxis = chart.getAxisLeft();

                leftAxis.setAxisMaximum(300f);
                leftAxis.setAxisMinimum(-300f);

//            XAxis xAxis=chart.getXAxis();
                // limit the number of visible entries
                chart.setVisibleXRangeMaximum(visibility);

                // move to the latest entry
                chart.moveViewToX(data.getEntryCount());

                Legend l = chart.getLegend();
                l.setEnabled(false);


            }
            data.addEntry(new Entry(set.getEntryCount(),Value ), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            YAxis leftAxis = chart.getAxisLeft();

            leftAxis.setAxisMaximum(300f);
            leftAxis.setAxisMinimum(-300f);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(visibility);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            Legend l = chart.getLegend();
            l.setEnabled(false);


        }
    }


    public static void setData(LineChart chart,float[] in,int size) {

        ArrayList<Entry> values = new ArrayList<>();
        for (int j = 0; j < size / 2; j++) {
            values.add(new Entry(j, in[j]));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setColor(Color.BLUE);
        set1.setLineWidth(0.5f);
        set1.setDrawValues(false);
        set1.setDrawCircles(false);
        set1.setMode(LineDataSet.Mode.LINEAR);
        set1.setDrawFilled(false);

        // create a data object with the data sets
        LineData data = new LineData(set1);
        // set data
        chart.setData(data);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMaximum(5000f);
        leftAxis.setAxisMinimum(-1000f);

//        chart.setVisibleXRangeMaximum(size);
        chart.moveViewToX(0f);
        chart.fitScreen();

        // let the chart know it's data has changed
        chart.invalidate();
        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();
        l.setEnabled(false);
    }

    public static void setPoincareData(LineChart chart,float datapoint, float peakpoint,int visibility) {

        LineData data = chart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            if (set == null) {
                set = createSet();
                set2 = createSet2();
                data.addDataSet(set);
                data.addDataSet(set2);
            }
            if (set2 == null) {
                set2 = createSet2();
                data.addDataSet(set2);
            }
            if (set.getLabel() != "1") {
                set.clear();
                data.removeDataSet(0);
                set = createSet();
                set.addEntry(new Entry(set.getEntryCount(),datapoint ));
                data.addDataSet(set);
                data.notifyDataChanged();
                chart.notifyDataSetChanged();
                YAxis leftAxis = chart.getAxisLeft();

                leftAxis.setAxisMaximum(300f);
                leftAxis.setAxisMinimum(-300f);

//            XAxis xAxis=chart.getXAxis();
                // limit the number of visible entries
                chart.setVisibleXRangeMaximum(visibility);

                // move to the latest entry
                chart.moveViewToX(data.getEntryCount());

                Legend l = chart.getLegend();
                l.setEnabled(false);


            }
            if (set2.getLabel() != "2") {
                set2.clear();
                data.removeDataSet(1);
                set2 = createSet2();
                set2.addEntry(new Entry(set2.getEntryCount(),datapoint ));
                data.addDataSet(set2);
                data.notifyDataChanged();
                chart.notifyDataSetChanged();
                YAxis leftAxis = chart.getAxisLeft();

                leftAxis.setAxisMaximum(300f);
                leftAxis.setAxisMinimum(-300f);

//            XAxis xAxis=chart.getXAxis();
                // limit the number of visible entries
                chart.setVisibleXRangeMaximum(visibility);

                // move to the latest entry
                chart.moveViewToX(data.getEntryCount());

                Legend l = chart.getLegend();
                l.setEnabled(false);


            }
            data.addEntry(new Entry(set.getEntryCount(), datapoint), 0);
            data.addEntry(new Entry(set.getEntryCount(), peakpoint), 1);
            data.notifyDataChanged();


            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            YAxis leftAxis = chart.getAxisLeft();

            leftAxis.setAxisMaximum(800);
            leftAxis.setAxisMinimum(-50);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(visibility);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            Legend l = chart.getLegend();
            l.setEnabled(false);


        }
    }


//    ............................................................................................................




    public static void initscatt(GraphView scatterplot,ArrayList<XYValue> xyValueArray,PointsGraphSeries<DataPoint> xySeries,double xvalue,double yvalue){
        //declare the xySeries Object
        xySeries = new PointsGraphSeries<>();

        if (xvalue!=0 && yvalue!=0){
            xyValueArray.add(new XYValue(xvalue,yvalue));
            Log.d("initscatt", "Adding a new point. (x,y): (" + xvalue + "," + yvalue + ")" );

        }else{
            Log.d("initscatt", "You must fill out both fields!" );
        }


        //little bit of exception handling for if there is no data.
        if(xyValueArray.size() != 0){
            createScatterPlot(scatterplot,xyValueArray,xySeries);
        }else{
            Log.d("initscatt", "No data to plot.");
        }
    }

    public static void createScatterPlot(GraphView mScatterPlot,ArrayList<XYValue> xyValueArray, PointsGraphSeries<DataPoint> xySeries) {
        Log.d("initscatt", "createScatterPlot: Creating scatter plot.");

        //sort the array of xy values
        xyValueArray = sortArray(xyValueArray);


        //        //set manual x bounds


        mScatterPlot.getViewport().setYAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxY(mScatterPlot.getViewport().getMaxY(true));
        mScatterPlot.getViewport().setMinY(mScatterPlot.getViewport().getMinY(true));
//
//        //set manual y bounds

        mScatterPlot.getViewport().setXAxisBoundsManual(true);
        mScatterPlot.getViewport().setMaxX(mScatterPlot.getViewport().getMaxX(true));
        mScatterPlot.getViewport().setMinX(mScatterPlot.getViewport().getMinX(true));
        //add the data to the series
        for(int i = 0;i <xyValueArray.size(); i++){
            try{
                double x = xyValueArray.get(i).getX();
                double y = xyValueArray.get(i).getY();
                xySeries.appendData(new DataPoint(x,y),true, 1000);
            }catch (IllegalArgumentException e){
                Log.e("initscatt", "createScatterPlot: IllegalArgumentException: " + e.getMessage() );
            }
        }

        //set some properties
        xySeries.setShape(PointsGraphSeries.Shape.TRIANGLE);
        xySeries.setColor(Color.GREEN);
        xySeries.setSize(20f);

        //set Scrollable and Scaleable
        mScatterPlot.getViewport().setScalable(true);
        mScatterPlot.getViewport().setScalableY(true);
        mScatterPlot.getViewport().setScrollable(true);
        mScatterPlot.getViewport().setScrollableY(true);

//        mScatterPlot.getGridLabelRenderer().setNumVerticalLabels(40);



        mScatterPlot.addSeries(xySeries);

    }

    /**
     * Sorts an ArrayList<XYValue> with respect to the x values.
     * @param array
     * @return
     */
    public static ArrayList<XYValue> sortArray(ArrayList<XYValue> array){
        /*
        //Sorts the xyValues in Ascending order to prepare them for the PointsGraphSeries<DataSet>
         */
        int factor = Integer.parseInt(String.valueOf(Math.round(Math.pow(array.size(),2))));
        int m = array.size() - 1;
        int count = 0;
//        Log.d(TAG, "sortArray: Sorting the XYArray.");


        while (true) {
            m--;
            if (m <= 0) {
                m = array.size() - 1;
            }
//            Log.d(TAG, "sortArray: m = " + m);
            try {
                //print out the y entrys so we know what the order looks like
                //Log.d(TAG, "sortArray: Order:");
                //for(int n = 0;n < array.size();n++){
                //Log.d(TAG, "sortArray: " + array.get(n).getY());
                //}
                double tempY = array.get(m - 1).getY();
                double tempX = array.get(m - 1).getX();
                if (tempX > array.get(m).getX()) {
                    array.get(m - 1).setY(array.get(m).getY());
                    array.get(m).setY(tempY);
                    array.get(m - 1).setX(array.get(m).getX());
                    array.get(m).setX(tempX);
                } else if (tempX == array.get(m).getX()) {
                    count++;
//                    Log.d(TAG, "sortArray: count = " + count);
                } else if (array.get(m).getX() > array.get(m - 1).getX()) {
                    count++;
//                    Log.d(TAG, "sortArray: count = " + count);
                }
                //break when factorial is done
                if (count == factor) {
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
//                Log.e(TAG, "sortArray: ArrayIndexOutOfBoundsException. Need more than 1 data point to create Plot." +e.getMessage());
                break;
            }
        }
        return array;
    }






}

