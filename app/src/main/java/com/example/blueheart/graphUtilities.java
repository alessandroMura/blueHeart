package com.example.blueheart;


import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;


public class graphUtilities {


    private static LineChart mLineChart;

    public static void removeDataSet() {

        LineData data = mLineChart.getData();

        if (data != null) {
            data.removeDataSet(data.getDataSetByIndex(data.getDataSetCount() - 1));
            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }
    }

    public static void removeDataSet1() {

        LineData data = mLineChart.getData();

        if (data != null) {

            data.removeDataSet(data.getDataSetByIndex(1));

            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }
    }

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

    public static void setupCombinedChart(CombinedChart mychart,CombinedData cData) {

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

        LineData data = new LineData();
        cData.setData(data);


        mychart.setData(cData);
//        mychart.invalidate();

    }

    public static ScatterDataSet createScatterDataSet(){
        ScatterDataSet set = new ScatterDataSet(null,"scatter");
        set.setScatterShapeSize(10f);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        set.setColor(Color.CYAN);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    public static void AddScatterEntry(CombinedChart mychart,float Value,int delay,int visibility) {

        CombinedData data = mychart.getData();
        if (data != null) {
            ScatterData scatterDataData = data.getScatterData();
            if (scatterDataData != null) {

                ScatterDataSet set = (ScatterDataSet) data.getDataSetByIndex(2);
                if (set == null) {

                    set = createScatterDataSet();
                    data.addDataSet(set);
                }

                LineData lineData = data.getLineData();
                if (lineData != null) {

                    LineDataSet pakk = (LineDataSet) data.getDataSetByIndex(1);
                    scatterDataData.addEntry(new Entry(pakk.getEntryCount() - delay, Value), 0);

                }
                mychart.notifyDataSetChanged();
                mychart.invalidate();
            }

            data.notifyDataChanged();
            // let the chart know it's data has changed
            mychart.notifyDataSetChanged();
            mychart.invalidate();
            mychart.setVisibleXRangeMaximum(visibility);

//            count=count+1;
//            Log.v("countnum",String.valueOf(count));
            //int valueCount = data.getXValCount();
            mychart.moveViewToX(scatterDataData.getEntryCount());
        }
    }

    public static void AddLineEntry(CombinedChart mychart,float Value,int visibility){
        CombinedData data = mychart.getData();
        if (data != null) {
            LineData lineData = data.getLineData();
            if (lineData != null) {
                ILineDataSet set = (ILineDataSet) data.getDataSetByIndex(0);

                if (set == null) {
                    set = createSet();
                    lineData.addDataSet(set);
                }
                lineData.addEntry(new Entry(set.getEntryCount(), Value), 0);
                mychart.notifyDataSetChanged();
                mychart.invalidate();
            }
            data.notifyDataChanged();
            mychart.notifyDataSetChanged();

            YAxis leftAxis = mychart.getAxisLeft();

            leftAxis.setAxisMaximum(300);
            leftAxis.setAxisMinimum(-300);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            mychart.setVisibleXRangeMaximum(visibility);

            // move to the latest entry
            mychart.moveViewToX(lineData.getDataSetByIndex(0).getEntryCount());

            Legend l = mychart.getLegend();
            l.setEnabled(false);
        }
    }

    public static void AddLineEntryScatter(CombinedChart mychart,float Value){

        // create a dataset and give it a type
        LineDataSet set1 = createSetscat();
        CombinedData data=mychart.getData();
        if (data != null) {
            LineData lineData = data.getLineData();
            if (lineData != null) {
                ILineDataSet set = lineData.getDataSetByIndex(0);
                ILineDataSet set2=lineData.getDataSetByIndex(1);
                if (set2!=null){
                    set2.clear();

                }
                if (set.getLabel()!="scat") {
                    set.clear();
                    if (set == null) {
                        lineData.addDataSet(set1);
                    }
                    lineData.removeDataSet(0);
                    lineData.addDataSet(set1);
                    mychart.notifyDataSetChanged();
                    mychart.invalidate();
                }
            }
            ILineDataSet set = lineData.getDataSetByIndex(0);
            if (set.getLabel()=="scat") {
                lineData.addEntry(new Entry(data.getEntryCount(), Value), 0);


                data.notifyDataChanged();
                mychart.notifyDataSetChanged();

                // create a data object with the data sets

//        LineData data= cdata.getLineData();
//        data.removeDataSet(0);
//        data.removeDataSet(1);
//
//        data.addDataSet(set1);
//        data.notifyDataChanged();


                YAxis leftAxis = mychart.getAxisLeft();
                leftAxis.setAxisMaximum(500f);
                leftAxis.setAxisMinimum(-100f);

                mychart.setVisibleXRangeMaximum(100);
                mychart.moveViewToX(data.getEntryCount());
                mychart.fitScreen();
//        chart.notifyDataSetChanged();

                // let the chart know it's data has changed
//        chart.invalidate();
                // get the legend (only possible after setting data)
                Legend l = mychart.getLegend();
                l.setEnabled(false);
            }
        }

    }


    public static void Add2LineEntry(CombinedChart mychart,float Value,float Value2,int visibility){
        CombinedData data = mychart.getData();
        if (data != null) {
            LineData lineData = data.getLineData();
            if (lineData != null) {
                ILineDataSet set = (ILineDataSet) data.getDataSetByIndex(0);
                ILineDataSet set2 = (ILineDataSet) data.getDataSetByIndex(1);
                if (set == null) {
                    set = createSet();
                    set2=createSet2();
                    lineData.addDataSet(set);
                    lineData.addDataSet(set2);
                }
                if (set2 == null) {
                    set2 = createSet2();
                    lineData.addDataSet(set2);


                }
                lineData.addEntry(new Entry(set.getEntryCount(), Value), 0);
                lineData.addEntry(new Entry(set.getEntryCount(), Value2), 1);

                Log.v("datast",String.valueOf(data.getLineData()));


                mychart.notifyDataSetChanged();
                mychart.invalidate();
            }
            data.notifyDataChanged();
            mychart.notifyDataSetChanged();

            YAxis leftAxis = mychart.getAxisLeft();

            leftAxis.setAxisMaximum(800);
            leftAxis.setAxisMinimum(-50);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            mychart.setVisibleXRangeMaximum(visibility);

            // move to the latest entry
            mychart.moveViewToX(lineData.getDataSetByIndex(0).getEntryCount());

            Legend l = mychart.getLegend();
            l.setEnabled(false);





        }
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

    public static LineDataSet createSetscat() {

        LineDataSet set = new LineDataSet(null, "scat");
        set.setColor(Color.TRANSPARENT);
        set.setLineWidth(0.5f);
        set.setDrawValues(false);
        set.setDrawCircles(true);
        set.setCircleColor(Color.CYAN);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawFilled(false);
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



    public static void AddMultipleLineEntries(CombinedChart chart,float[] in,int size){

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

        CombinedData data=chart.getData();
        if (data != null) {
            LineData lineData = data.getLineData();
            if (lineData != null) {
                ILineDataSet set = lineData.getDataSetByIndex(0);
                set.clear();
                if (set == null) {
                    lineData.addDataSet(set1);
                }
                lineData.removeDataSet(0);
                lineData.addDataSet(set1);
                chart.notifyDataSetChanged();
                chart.invalidate();
            }

            data.notifyDataChanged();
            chart.notifyDataSetChanged();

            // create a data object with the data sets

//        LineData data= cdata.getLineData();
//        data.removeDataSet(0);
//        data.removeDataSet(1);
//
//        data.addDataSet(set1);
//        data.notifyDataChanged();


            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setAxisMaximum(5000f);
            leftAxis.setAxisMinimum(-100f);

//        chart.setVisibleXRangeMaximum(size/2);
            chart.moveViewToX(0f);
            chart.fitScreen();
//        chart.notifyDataSetChanged();

            // let the chart know it's data has changed
//        chart.invalidate();
            // get the legend (only possible after setting data)
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
        set1.setColor(Color.RED);
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




}

