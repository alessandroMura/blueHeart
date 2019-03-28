package com.example.blueheart;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import java.util.ArrayList;
import java.util.List;

import sew.CommunicationException;
import sew.DeviceException;
import sew.DeviceFinder;
import sew.RegularDataBlock;
import sew.SewBluetoothDevice;

import static com.example.blueheart.deviceListActivity.sewDevice;


public class realTimeAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,parameterSend {



    private static boolean buffered=true;

    private LineChart chart;

    Fragment timeDomainFrag,freqMagFrag,freqPhasFrag,poincarFrag,lagPoincarFrag;
    Spinner spinner;

    private int whatFragment;
    private float value=0;
    int i=0;
    private int visibility_range=1024;

    private Filter filter=new Filter();
    private HeartyFilter.SavGolayFilter savgol =new HeartyFilter.SavGolayFilter(1);
    private HeartyFilter.StatFilter stats=new HeartyFilter.StatFilter();
    private float minr;
    private float maxr;
    private float ranger;

//    private HeartyFilter  lp=new HeartyFilter(bl,a);
//    private HeartyFilter  hp=new HeartyFilter(bh,a);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Actlif","onCreate Called");

        setContentView(R.layout.activity_real_time_analysis);
        spinner =  findViewById(R.id.spinner_mode);

        timeDomainFrag= new timeDomainFragment();
        freqMagFrag= new frequencyMagnitudeFragment();
        freqPhasFrag=new frequencyPhaseFragment();
        poincarFrag= new poincarePlotFragment();
        lagPoincarFrag=new laggedPoincareFragment();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,getResources().getStringArray(R.array.modes_array));
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        setupSensor();
        setupChart();
        runDataStreamThread();



        spinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {


                showToast((String) parent.getItemAtPosition(pos));
                switch (pos){
                    case 0:
                        whatFragment=0;
                        setFragment(timeDomainFrag);


                        break;
                    case 1:
                        whatFragment=1;
                        setFragment(freqMagFrag);


                        break;
                    case 2:
                        whatFragment=2;
                        setFragment(freqPhasFrag);


                        break;
                    case 3:
                        whatFragment=3;
                        setFragment(poincarFrag);
                        break;
                    case 4:
                        whatFragment=4;
                        setFragment(lagPoincarFrag);
                        break;

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void setFragment(Fragment fragment){
        FragmentTransaction fragtransaction=getSupportFragmentManager().beginTransaction();
        fragtransaction.replace(R.id.fragment_frame,fragment);
        fragtransaction.commit();

    }


    public void setupSensor(){
        List<SewBluetoothDevice> bluetoothDeviceList = DeviceFinder.findPairedDevices("sew");
        for (int i = 0; i < bluetoothDeviceList.size(); i++) {
            if (bluetoothDeviceList.get(i).getName().equals(deviceListActivity.currentID)) {
                sewDevice = bluetoothDeviceList.get(i);
                Log.v("sewdevice","created");
                Log.v("sewdevice", sewDevice.getAddress());
            }else{
                showToast("Selected!= found");
            }
        }

    }

//    public void setupSensor(){
//        Log.d(TAG,"setupSensor:Initializing Sensor Services");
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        sensorManager.registerListener(realTimeAnalysisActivity.this, sensor,SensorManager.SENSOR_DELAY_FASTEST);
//        Log.d(TAG,"setupSensor:Registered Accelerometer Listener");
//
//    }
    private void setupChart(){

        chart = findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        // no description text
        chart.getDescription().setEnabled(false);
        // enable touch gestures
        chart.setTouchEnabled(true);
        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setDrawGridLines(true);
        chart.getXAxis().setDrawAxisLine(false);
        LineData data = new LineData();
        chart.setData(data);
    }

    private void setData(float[] in) {

        ArrayList<Entry> values = new ArrayList<>();


        for (int j=0;j<size/2;j++) {
//
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
    private void setData0() {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), value), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            YAxis leftAxis = chart.getAxisLeft();

           leftAxis.setAxisMaximum(maxr/2);
           leftAxis.setAxisMinimum(minr/2);

//            XAxis xAxis=chart.getXAxis();


            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(visibility_range);



            // move to the latest entry
            chart.moveViewToX(data.getEntryCount()-100);


            Legend l = chart.getLegend();
            l.setEnabled(false);


        }
    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "");
        set.setColor(Color.RED);
        set.setLineWidth(0.5f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawFilled(false);
        return set;
    }

    private Thread thread0;
    private Thread thread1;
    private Thread thread2;
    private static int size=512;
    private Complex complexArray[]=new Complex[size];
    private Complex fftOut []=new Complex[size];
    private float out []=new float[size/2];
    private float in []=new float[size];

    private void feedMultiple0(){

        if (thread0 != null)
            thread0.interrupt();

        final Runnable runnable0 = new Runnable() {

            @Override
            public void run() {

                setData0();


            }
        };

        thread0 = new Thread(new Runnable() {

            @Override
            public void run() {



                    runOnUiThread(runnable0);
                    try {
                        Log.v("feedMultiple","feedmultiple 0");

                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    }


            }

        });

        thread0.start();



    }
    private void feedMultiple1() {

        if (thread1 != null)
            thread1.interrupt();

        final Runnable runnable1 = new Runnable() {

            @Override
            public void run() {



                out =new float[size];



                for (int s=0;s<size;s++){

//                    Log.v("return","yes");
                    complexArray[s]=new Complex((double)in[s],0);
                }

                fftOut=FFT.fft(complexArray);

                for (int z=0;z<size/2;z++) {
                    out[z] = (float) fftOut[z].abs();
                }


                setData(out);
//                Log.v("Running","run");

            }
        };

        thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                // Don't generate garbage runnables inside the loop.
                while (buffered) {
//                    runOnUiThread(runnable);
                    runnable1.run();
                    Log.v("feedMultiple","feedmultiple 1");
                    buffered=false;


                    try {
//                        runnable.run();
                        Thread.sleep(1);



                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        thread1.start();
    }
    private void feedMultiple2() {

        if (thread2 != null)
            thread2.interrupt();

        final Runnable runnable2 = new Runnable() {

            @Override
            public void run() {



                out =new float[size];

                for (int s=0;s<size;s++){

//                    Log.v("return","yes");
                    complexArray[s]=new Complex((double)in[s],0);
                }

                fftOut=FFT.fft(complexArray);

                for (int z=0;z<size/2;z++) {
                    out[z] = (float) fftOut[z].phase();
                }


                setData(out);
//                Log.v("Running","run");

            }
        };

        thread2 = new Thread(new Runnable() {

            @Override
            public void run() {
                // Don't generate garbage runnables inside the loop.
                while (buffered) {
//                    runOnUiThread(runnable);
                    runnable2.run();
                    Log.v("feedMultiple","feedmultiple 2");
                    buffered=false;


                    try {
//                        runnable.run();
                        Thread.sleep(1);



                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });

        thread2.start();
    }

    private Thread thread3;
    boolean canStream=true;
    boolean streamtofeed;

    private void tryConnect(SewBluetoothDevice s){
        try {
            Log.v("sewdevice","Connecting....");
            s.connect();
            Log.v("sewdevice","Connected");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    private void tryStream(SewBluetoothDevice s){
        try {
            Log.v("sewdevice","Starting Stream....");
            s.startStreaming();
            Log.v("sewdevice","Streaming Started!");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    private void tryStopStream(SewBluetoothDevice s){
        try {
            Log.v("sewdevice","Stopping Stream....");
            s.stopStreaming();
            Log.v("sewdevice","Streaming Stopped!");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    private void tryDisconnect(SewBluetoothDevice s){
        try {
            Log.v("sewdevice","Disconnecting...");
            s.disconnect();
            Log.v("sewdevice","Disconnected");
        } catch (CommunicationException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        }

    }

    private boolean isInStreaming(SewBluetoothDevice s) {
        try {
            boolean is;
            Log.v("sewdevice", "Checking Stream...");
            is = s.isStreaming();
            if (is) {
                Log.v("sewdevice", "Is streaming");
                return true;

            } else {
                Log.v("sewdevice", "Not streaming");
                return false;
            }

        } catch (CommunicationException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int isConnected(SewBluetoothDevice s){
            try {
                int stat;
                Log.v("sewdevice","Checking Status...");
                stat=s.getStatus();
                Log.v("sewdevice","Status=  "+stat);
                return stat;


            } catch (CommunicationException e) {
                e.printStackTrace();
            }
        return -1;
    }

    private long tryGetClock(SewBluetoothDevice s){

        try {

            long t =s.getClock();
            return t;

        } catch (CommunicationException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void runDataStreamThread() {

        final Runnable runnable3 = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnable 3","Starting");
                if (isConnected(sewDevice)>-1) {
                    if (isInStreaming(sewDevice)) {
                        boolean stream=isInStreaming(sewDevice);

                        while (stream) {
                            RegularDataBlock[] rdbs = (RegularDataBlock[]) sewDevice.getDataBlocks();

                            for (int c = 0; c < rdbs.length; c++) {

                                if (rdbs[c].getId() == 1) {


                                    float [] signal = rdbs[c].getValues();


                                    for (int z = 0; z < signal.length; z++) {

                                        float bp=filter.HighPassFilter(filter.LowPassFilter(signal[z]));
                                        value=savgol.next(bp);



                                        stats.next(value);
                                        minr=stats.min;
                                        maxr=stats.max;
                                        ranger=stats.range;
                                        Log.v("sewdevice", "Max:  " + maxr+"");
                                        Log.v("sewdevice", "Min:  " + minr+"");
                                        Log.v("sewdevice", "Range:  " + ranger+"");



                                        Log.v("sewdevice", "Value:  " + String.valueOf(value) + "----" + String.valueOf(z));

                                        streamtofeed = true;
                                        onSensorc();
                                    }
                                }
                            }
                            streamtofeed = false;
                        }
                    }else{
                        tryStream(sewDevice);
                    }
                }else {
                    tryConnect(sewDevice);
                }
            }
        };

        thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (canStream) {
                    runnable3.run();
                    Log.v("sewdevice", "running runnable 3");
                }
            }
        });

        thread3.start();

    }


    public void onSensorc(){
        if (streamtofeed) {

            switch (whatFragment) {
                case 0:
                    feedMultiple0();
                    break;
                case 1:
                    in[i] = value;
                    i++;
                    if (i == size) {
                        buffered = true;
                        feedMultiple1();
                        i = 0;
                    }
                    break;
                case 2:
                    in[i] = value;
                    i++;
                    if (i == size) {
                        buffered = true;
                        feedMultiple2();
                        i = 0;
                    }
                    break;
                default:
                    feedMultiple0();
                    break;
            }
        }


    }



    @Override
    protected void onResume() {
        Log.v("Actlif","onResume Called");
        super.onResume();

        canStream=true;
    }

    @Override
    protected void onStart() {
        Log.v("Actlif","onStart Called");
        super.onStart();

        canStream=true;
    }

    @Override
    protected void onStop() {
        Log.v("Actlif","onStop Called");
        super.onStop();

    }

    @Override
    protected void onPause() {
        Log.v("Actlif","onPause Called");
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        Log.v("Actlif","onDestroy Called");
        super.onDestroy();

        if (thread0!=null){
            thread0.interrupt();
        }
        if (thread1!=null){
            thread1.interrupt();
        }
        if (thread2!=null){
            thread2.interrupt();
        }
        if (thread3!=null){
            canStream=false;
            tryDisconnect(sewDevice);
            Log.v("sewdevice", String.valueOf(thread3.isInterrupted()));
//            thread3.interrupt();
//            Log.v("sewdevice","on pause disconnect");
//            tryDisconnect(sewDevice);
//            Log.v("sewdevice","on pause stopping stream");
//            tryStopStream(sewDevice);
        }

    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    private void removeDataSet() {
//
//        LineData data = chart.getData();
//
//        if (data != null) {
//
//            data.removeDataSet(data.getDataSetByIndex(data.getDataSetCount() - 1));
//
//            chart.notifyDataSetChanged();
//            chart.invalidate();
//        }
//    }

    @Override
    public void fftSize(int n) {

        i=0;
        size=n;
        in=new float[size];
        complexArray=new Complex[size];
        fftOut =new Complex[size];
        out =new float[size/2];
        showToast(String.valueOf(size));
    }

    @Override
    public void timeSize(int n) {

        visibility_range=n;

        chart.fitScreen();
        showToast(String.valueOf(visibility_range));
    }

    @Override
    public String sendString() {
        return null;
    }
}
