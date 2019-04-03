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

    private long start;

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
    private PeakDetectionFilter peak=new PeakDetectionFilter(19,4);
    private float peakv=0f;
    private int peakindex=0;
    private float peakp=0f;
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

    public static final float	diff_a[]		= { 8f };
    public static final float	diff_b[]		= { 2f, 1f, 0f, -1f, -2f };
    public LmeFilter			diff			= new LmeFilter( diff_b, diff_a );

    private float hp2_b[]={

            -0.011829420542201355207034829675194487209f,
                    -0.013800578668977568630449326292364276014f,
                    -0.015982922958368072502421952663098636549f,
                    -0.01842671715807690097088666902891418431f,
                    -0.0211997401587666160771572521070993389f,
                    -0.024395626679248692131318776432635786477f,
                    -0.028147475741042881497433469917268666904f,
                    -0.032651065111736737867076385555265005678f,
                    -0.038206637287977171513198015873058466241f,
                    -0.045299257137646015136667188016872387379f,
                    -0.054766832403761858683033381112181814387f,
                    -0.068192161521626326603495726885739713907f,
                    -0.088967063375629304444736078494315734133f,
                    -0.125910418426685544179832731970236636698f,
                    -0.211358374273133114984446478956670034677f,
                    -0.636337017713277441899322184326592832804f,
            0.636337017713277441899322184326592832804f,
            0.211358374273133114984446478956670034677f,
            0.125910418426685544179832731970236636698f,
            0.088967063375629304444736078494315734133f,
            0.068192161521626326603495726885739713907f,
            0.054766832403761858683033381112181814387f,
            0.045299257137646015136667188016872387379f,
            0.038206637287977171513198015873058466241f,
            0.032651065111736737867076385555265005678f,
            0.028147475741042881497433469917268666904f,
            0.024395626679248692131318776432635786477f,
            0.0211997401587666160771572521070993389f,
            0.01842671715807690097088666902891418431f,
            0.015982922958368072502421952663098636549f,
            0.013800578668977568630449326292364276014f,
            0.011829420542201355207034829675194487209f

    };
    private float hp2_a[]={1f};
    private float lp2_b[]={
            0.011693616047358012036139207623364200117f,
                    -0.000000000000000031185374660155001489261f,
                    -0.013364132625551961713883386551060539205f,
            0.0232869762658663576049278276514087338f,
                    -0.025227557621355242711835842328582657501f,
            0.017008896068884372942964233743623481132f,
                    -0.000000000000000031185374660155007652237f,
                    -0.020788650750858620602778970010149350855f,
            0.037841336432032843251072051771188853309f,
                    -0.043247241636608979575839839526452124119f,
            0.031182976126287995088937066157086519524f,
                    -0.000000000000000031185374660155007652237f,
                    -0.046774464189431930183360464070574380457f,
            0.100910230485420929213979945870960364118f,
                    -0.151365345728131400759863822713668923825f,
            0.187097856757727859511319934426865074784f,
            0.800000000000000044408920985006261616945f,
            0.187097856757727859511319934426865074784f,
                    -0.151365345728131400759863822713668923825f,
            0.100910230485420929213979945870960364118f,
                    -0.046774464189431930183360464070574380457f,
                    -0.000000000000000031185374660155007652237f,
            0.031182976126287995088937066157086519524f,
                    -0.043247241636608979575839839526452124119f,
            0.037841336432032843251072051771188853309f,
                    -0.020788650750858620602778970010149350855f,
                    -0.000000000000000031185374660155007652237f,
            0.017008896068884372942964233743623481132f,
                    -0.025227557621355242711835842328582657501f,
            0.0232869762658663576049278276514087338f,
                    -0.013364132625551961713883386551060539205f,
                    -0.000000000000000031185374660155001489261f,
            0.011693616047358012036139207623364200117f
    };
    private float lp2_a[]={1f};


    private LmeFilter  lp=new LmeFilter(lp2_b,lp2_a);
    private LmeFilter  hp=new LmeFilter(hp2_b,hp2_a);

    private float notch_a[]={1f};
    private float notch_b[]={
                -0.009291172581737500504872606654771516332f,
                        -0.032051810844675183986840494299030979164f,
                        -0.010441303862913935487921612832451501163f,
                0.025624254915773990448624175542136072181f,
                0.026217018998279079111668465884577017277f,
                        -0.009484228188649752866457021127644111402f,
                        -0.032056357342216713901539293374298722483f,
                        -0.010251040666833461170726060629476705799f,
                0.02574573744270795133681772881573124323f,
                0.026101417527400395945935684949290589429f,
                        -0.009676760355272177524521559632830758346f,
                        -0.032059085462631434215730052983417408541f,
                        -0.010060184082715209474834239244955824688f,
                0.025865766021603790042471260335332772229f,
                0.025984329655874480180521857164421817288f,
                        -0.00986875149407467379403247065283721895f,
                0.967940005126915914424046150088543072343f,
                        -0.00986875149407467379403247065283721895f,
                0.025984329655874480180521857164421817288f,
                0.025865766021603790042471260335332772229f,
                        -0.010060184082715209474834239244955824688f,
                        -0.032059085462631434215730052983417408541f,
                        -0.009676760355272177524521559632830758346f,
                0.026101417527400395945935684949290589429f,
                0.02574573744270795133681772881573124323f,
                        -0.010251040666833461170726060629476705799f,
                        -0.032056357342216713901539293374298722483f,
                        -0.009484228188649752866457021127644111402f,
                0.026217018998279079111668465884577017277f,
                0.025624254915773990448624175542136072181f,
                        -0.010441303862913935487921612832451501163f,
                        -0.032051810844675183986840494299030979164f,
                        -0.009291172581737500504872606654771516332f
    };

    private LmeFilter  notch=new LmeFilter(notch_b,notch_a);

//    Inizializzazione oggetti thread
    private Thread setupThread;
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


        setup();


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

        LineDataSet set = new LineDataSet(null, "1");
        set.setColor(Color.RED);
        set.setLineWidth(0.5f);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawFilled(false);
        return set;
    }

    private LineDataSet createSet2() {

        LineDataSet set2 = new LineDataSet(null, "2");
        set2.setColor(Color.BLACK);
        set2.setLineWidth(0.5f);
        set2.setDrawValues(false);
        set2.setDrawCircles(false);
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
                Log.v("Runnables","FeedMultiple0 Started");


                value0=highpass.next(lowpass.next(value));

//                value0=hp.next(lp.next(savgol.next(value)));
//                value0=savgol.next(value);
//                float tempVar;
//                tempVar = Filter.lowPassNext(value) ;
//                value0 = Filter.highPassNext(tempVar) ;

                setData0();
            }
        };

        thread0 = new Thread(new Runnable() {

            @Override
            public void run() {


                    try {
                        runOnUiThread(runnable0);
                        Log.v("Runnables","FeedMultiple0 Done");
                        Thread.sleep(0);
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
                Log.v("Runnables","FeedMultiple1 Started");

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
                    runOnUiThread(runnable1);
//                    runnable1.run();
                    buffered=false;
                    Log.v("Runnables","FeedMultiple1 Done");


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
                Log.v("Runnables","FeedMultiple2 Starting");



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
                    runOnUiThread(runnable2);
//                    runnable2.run();
                    buffered=false;
                    Log.v("Runnables","FeedMultiple2 Done");

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
                Log.v("Runnables","Poincare Starting");
//                poincareValue=pan.next(value, 250);

                poincareValue=highpass.next(lowpass.next(value));
//                Log.v("sewdevice", "Peak finder:  Value= " + pan.rrStats.value+"Index= "+pan.rPeak.peakIdx);
//                if (peak.peakValue!=Float.NaN){
//                    peakv=peak.peakValue;
//                }else{
//                    peakv=0f;
//                }

                stats.next(poincareValue);
                minrp=stats.min;
                maxrp=stats.max;
                rangerp=stats.range;
                if (poincareValue>60f){
                    Log.v("sewdevice", "Peak finder:  Value= " +poincareValue);
                    peakp=maxrp;

                }else{
                    peakp=0f;
                }
                Log.v("sewdevice", "Max:  " + maxrp+"");
                Log.v("sewdevice", "Min:  " + minrp+"");
                Log.v("sewdevice", "Range:  " + rangerp+"");
                setPoincareData(poincareValue,peakp);


            }
        };

        poincareThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (buffered) {
//                    runOnUiThread(runnable);
//                    poincareRunnable.run();
                    runOnUiThread(poincareRunnable);
                    buffered=false;
                    Log.v("Runnables","Poincare Done");
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
                Log.v("Runnables","Streaming Starting");
                if (isConnected(sewDevice)==0 ||isConnected(sewDevice)==1) {
                    if (isInStreaming(sewDevice)) {
                        boolean stream=isInStreaming(sewDevice);
                        start=System.nanoTime();
                        RegularDataBlock [] rdbs;

                        while (stream && canStream) {
                            rdbs = (RegularDataBlock[]) sewDevice.getDataBlocks();

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
                    Log.v("Runnables", "Streaming Done");
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

    private void removeDataSet1() {

        LineData data = chart.getData();

        if (data != null) {

            data.removeDataSet(data.getDataSetByIndex(1));

            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }


//    Activity Lifecycle
    @Override
    protected void onResume() {
        Log.v("Actlif","onResume Called");
        super.onResume();

    }

    @Override
    protected void onStart() {
        Log.v("Actlif","onStart Called");
        super.onStart();
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
        removeDataSet();

        if (thread0!=null){
            thread3.interrupt();
            canStream=false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Thread0 interrupted:  "+String.valueOf(thread3.isInterrupted()));
        }
        if (thread1!=null){
            thread3.interrupt();
            canStream=false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  "+String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "Thread1 interrupted:  "+String.valueOf(thread3.isInterrupted()));
        }
        if (thread2!=null){
            thread3.interrupt();
            canStream=false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  "+String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "Thread2 interrupted:  "+String.valueOf(thread3.isInterrupted()));
        }
        if (thread3!=null){
            thread3.interrupt();
            canStream=false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  "+String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "StreamThread interrupted:  "+String.valueOf(thread3.isInterrupted()));
        }
        if (poincareThread!=null){
            thread3.interrupt();
            canStream=false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  "+String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "PoincareThread interrupted:  "+String.valueOf(thread3.isInterrupted()));
        }

    }


    private void setup(){

        if (setupThread != null)
            setupThread.interrupt();

        final Runnable setupRunnable = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnables","setupRunnable Started");


                setupChart();
                setupSensor();
                runDataStreamThread();



            }
        };

        setupThread = new Thread(new Runnable() {

            @Override
            public void run() {


                try {
                    runOnUiThread(setupRunnable);
                    Log.v("Runnables","setupRunnable Done");



                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }

        });

        setupThread.start();



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
