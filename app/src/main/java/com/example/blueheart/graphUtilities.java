package com.example.blueheart;


import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.ArrayList;


public class graphUtilities {




    public static void removeDataSet(LineChart mLineChart) {

        LineData data = mLineChart.getData();

        if (data != null) {
            data.removeDataSet(data.getDataSetByIndex(data.getDataSetCount() - 1));
            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }
    }

    public static void removeDataSet1(LineChart mLineChart) {

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

    public static void setupScatterChart(ScatterChart mychart) {

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
        ScatterData data = new ScatterData();
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

    public static void setDataScat(ScatterChart chart, float currentrrdistance, float nextrrdistance, int visibility) {

        ScatterData data = chart.getData();
        if (data != null) {
            IScatterDataSet set = data.getDataSetByIndex(0);
//            Log.v("sett",set.getLabel());
            if (set== null) {
//                set.clear();
//                data.removeDataSet(0);
                set = createSetscat();
//                set.addEntry(new Entry(currentrrdistance,nextrrdistance ));
                data.addDataSet(set);
//                data.notifyDataChanged();
//                chart.notifyDataSetChanged();
//                YAxis leftAxis = chart.getAxisLeft();
//
//                leftAxis.setAxisMaximum(30f);
//                leftAxis.setAxisMinimum(-30f);
//
////            XAxis xAxis=chart.getXAxis();
//                // limit the number of visible entries
//                chart.setVisibleXRangeMaximum(visibility);
//
//                // move to the latest entry
//                chart.moveViewToX(data.getEntryCount());
//
//                Legend l = chart.getLegend();
//                l.setEnabled(false);


            }
            data.addEntry(new Entry(currentrrdistance,nextrrdistance  ), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            YAxis leftAxis = chart.getAxisLeft();

            leftAxis.setAxisMaximum(30f);
            leftAxis.setAxisMinimum(-30f);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(30);

            // move to the latest entry
//            chart.moveViewToX(data.getEntryCount());
            chart.invalidate();

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




}

