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

import com.example.blueheart.HeartyFilter.PeakDetectionFilter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sew.CommunicationException;
import sew.DeviceException;
import sew.DeviceFinder;
import sew.RegularDataBlock;
import sew.SewBluetoothDevice;

import static com.example.blueheart.deviceListActivity.sewDevice;


public class realTimeAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,parameterSend {

//Graphic elements Initialization
    Fragment timeDomainFrag,freqMagFrag,freqPhasFrag,poincarFrag,lagPoincarFrag;
    Spinner spinner;
    private LineChart chart;

//Options
    private int visibility_range=1024; //Numero campioni visualizzati nel grafico temporale all'inizializzazione
    private static int size=512; //Numero campioni per la FFT all'inizializzazione
    private Complex complexArray[]=new Complex[size]; //Array di complessi per la fft in ingresso
    private Complex fftOut []=new Complex[size]; //Array di complessi per la fft in uscita
    private float out []=new float[size/2]; // Array float in uscita per la fft reale o imag
    private float in []=new float[size]; //Array di valori presi dal sensore per l'ingresso della fft
    private float value=0; //variabile temporanea per il valore I-esimo preso dal sensore
    private float poincareValue=0;
    private float value0=0;

// Boolean Variables
    private static boolean buffered=true;
    private boolean canStream=true;
    private boolean streamtofeed;

    private int whatFragment;
    private int i=0;

//Parametri, oggetti e variabili per il filtraggio
    private Filter filter=new Filter();
    private HeartyFilter.SavGolayFilter savgol =new HeartyFilter.SavGolayFilter(1);
    private HeartyFilter.StatFilter stats=new HeartyFilter.StatFilter();
    private float minr;
    private float maxr;
    private float ranger;
    private float minrp=-100f;
    private float maxrp=100f;
    private float rangerp=200f;
    private PeakDetectionFilter peak=new PeakDetectionFilter(10,10f);
    private float peakv=0f;
    private int peakindex=0;
    private PanTompkins pan= new PanTompkins(250);
    private double time;
    /** LOW-PASS filter */
    public static final float	lp_a[]			= { 1f, 2f, -1f };
    public static final float	lp_b[]			= { 0.03125f, 0, 0, 0, 0, 0, -0.0625f, 0, 0, 0, 0, 0, 0.03125f };
    public LmeFilter			lowpass			= new LmeFilter( lp_b, lp_a );

    /** HIGH-PASS filter */
    public static final float	hp_a[]			= { 1f, 1f };
    public static final float	hp_b[]			= { -0.03125f, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1f, -1f, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0.03125f };
    public LmeFilter			highpass		= new LmeFilter( hp_b, hp_a );
//    private HeartyFilter  lp=new HeartyFilter(bl,a);
//    private HeartyFilter  hp=new HeartyFilter(bh,a);

//    Inizializzazione oggetti thread
    private Thread thread0;
    private Thread thread1;
    private Thread thread2;
    private Thread thread3;
    private Thread poincareThread;




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
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }


//Activity Methods Implementation
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

    public void setFragment(Fragment fragment){
        FragmentTransaction fragtransaction=getSupportFragmentManager().beginTransaction();
        fragtransaction.replace(R.id.fragment_frame,fragment);
        fragtransaction.commit();

    }

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
//        chart.getXAxis().setDrawGridLines(false);
//        chart.getXAxis().setDrawAxisLine(false);
        chart.getXAxis().setTextColor(Color.WHITE);
        LineData data = new LineData();
        chart.setData(data);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    private void setData0() {

        LineData data = chart.getData();
        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            data.addEntry(new Entry(set.getEntryCount(), value0), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            YAxis leftAxis = chart.getAxisLeft();

            leftAxis.setAxisMaximum(200f);
            leftAxis.setAxisMinimum(-200f);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(visibility_range);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

            Legend l = chart.getLegend();
            l.setEnabled(false);


        }
    }

    private void setData(float[] in) {

        ArrayList<Entry> values = new ArrayList<>();
        for (int j=0;j<size/2;j++) {
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


    private void setPoincareData(float datapoint,float peakpoint){

        LineData data = chart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            if (set == null) {
                set = createSet();
                set2=createSet2();
                data.addDataSet(set);
                data.addDataSet(set2);
            }

            data.addEntry(new Entry(set.getEntryCount(), datapoint), 0);
            data.addEntry(new Entry(set2.getEntryCount(), peakpoint), 1);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            YAxis leftAxis = chart.getAxisLeft();

            leftAxis.setAxisMaximum(maxrp);
            leftAxis.setAxisMinimum(minrp);

//            XAxis xAxis=chart.getXAxis();
            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(visibility_range);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());

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

    private LineDataSet createSet2() {

        LineDataSet set2 = new LineDataSet(null, "");
        set2.setColor(Color.BLACK);
        set2.setLineWidth(0.5f);
        set2.setDrawValues(false);
        set2.setDrawCircles(true);
        set2.setMode(LineDataSet.Mode.STEPPED);
        set2.setDrawFilled(false);
        return set2;
    }

    private void feedMultiple0(){

        if (thread0 != null)
            thread0.interrupt();

        final Runnable runnable0 = new Runnable() {

            @Override
            public void run() {

                value0=highpass.next(lowpass.next(value));

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

    private void poincarePlotThread(){
        if (poincareThread != null)
            poincareThread.interrupt();
        final Runnable poincareRunnable = new Runnable() {

            @Override
            public void run() {

                poincareValue=pan.next(value,new Date().getTime());
                peak.next(poincareValue);

                Log.v("sewdevice", "Peak finder:  Value= " + peak.peakValue+"Index= "+peak.peakIdx);
                if (peak.peakValue!=Float.NaN){
                    peakv=peak.peakValue;
                }else{
                    peakv=0f;
                }

                stats.next(poincareValue);
                minrp=stats.min;
                maxrp=stats.max;
                rangerp=stats.range;
                Log.v("sewdevice", "Max:  " + maxrp+"");
                Log.v("sewdevice", "Min:  " + minrp+"");
                Log.v("sewdevice", "Range:  " + rangerp+"");
                setPoincareData(poincareValue,peakv);


            }
        };

        poincareThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (buffered) {
//                    runOnUiThread(runnable);
                    Log.v("PoincareThread","poincare thread run");
//                    poincareRunnable.run();
                    runOnUiThread(poincareRunnable);
                    buffered=false;
                    try {
                        Thread.sleep(1);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }

        });

        poincareThread.start();




    }

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

    private long tryGetClock(SewBluetoothDevice s){

        try {

            return s.getClock();

        } catch (CommunicationException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void runDataStreamThread() {

        final Runnable runnable3 = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnable Poincare","Starting");
                if (isConnected(sewDevice)>-1) {
                    if (isInStreaming(sewDevice)) {
                        boolean stream=isInStreaming(sewDevice);
//                        long start=System.nanoTime();

                        while (stream) {
                            RegularDataBlock[] rdbs = (RegularDataBlock[]) sewDevice.getDataBlocks();

                            for (int c = 0; c < rdbs.length; c++) {

                                if (rdbs[c].getId() == 1) {
                                    float [] signal = rdbs[c].getValues();

                                    for (int z = 0; z < signal.length; z++) {
                                        value=signal[z];
                                        streamtofeed = true;
                                        onSensorc();
//                                        time=(System.nanoTime()-start)/1_000_000_000.0;
                                        Log.v("sewdevice", "Value:  " + String.valueOf(value) + "--" + String.valueOf(z) );


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

    private void onSensorc(){
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
                case 3:
                    buffered=true;
                    poincarePlotThread();
                    break;

                default:
                    feedMultiple0();
                    break;
            }
        }


    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void removeDataSet() {

        LineData data = chart.getData();

        if (data != null) {

            data.removeDataSet(data.getDataSetByIndex(data.getDataSetCount() - 1));

            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }


//    Activity Lifecycle
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
        }

    }


//    Interfaces Overrides
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

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
