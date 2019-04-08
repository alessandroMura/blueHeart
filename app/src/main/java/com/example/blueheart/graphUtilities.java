package com.example.blueheart;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;


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

}

