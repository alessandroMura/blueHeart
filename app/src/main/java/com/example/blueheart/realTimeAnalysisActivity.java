package com.example.blueheart;


import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.karlotoy.perfectune.instance.PerfectTune;

import java.util.ArrayList;

import sew.RegularDataBlock;

import static com.example.blueheart.deviceListActivity.sewDevice;
import static com.example.blueheart.utilities.*;
import static com.example.blueheart.sensorUtilities.*;
import static com.example.blueheart.bluheartConstants.*;
import static com.example.blueheart.graphUtilities.*;


public class realTimeAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, parameterSend {

    //    Graphic elements Initialization ..............................................................
    Fragment timeDomainFrag, freqMagFrag, freqPhasFrag, poincarFrag, lagPoincarFrag,RPeaksFrag;
    Spinner spinner;
    //    private LineChart chart;
    private LineChart chart;
    private ScatterChart scatterchart;

    //    Options ......................................................................................
    private int visibility_range = 1024; //Numero campioni visualizzati nel grafico temporale all'inizializzazione
    private static int size = 512; //Numero campioni per la FFT all'inizializzazione
    private Complex complexArray[] = new Complex[size]; //Array di complessi per la fft in ingresso
    private Complex fftOut[] = new Complex[size]; //Array di complessi per la fft in uscita
    private float out[] = new float[size / 2]; // Array float in uscita per la fft reale o imag
    private float in[] = new float[size]; //Array di valori presi dal sensore per l'ingresso della fft
    private float value = 0; //variabile temporanea per il valore I-esimo preso dal sensore
    private float poincareValue = 0;
    private float value0 = 0;

    //    Boolean Variables ........................................................................
    private static boolean buffered = true;
    private boolean canStream = true;
    private boolean streamtofeed;
    private int whatFragment;
    private int i = 0;


    //    Parametri, oggetti e variabili per il filtraggio ..........................................
    private float minrp = -100f;
    private float maxrp = 100f;
    private float rangerp = 200f;
    private float peakp;
    private long start;
    private long startPeaks;
    private double timepeakdetector;
    private double rpeaktime;
    private double peaktimevector[]=new double[500];
    private float diffvector[]=new float[500];
    private double time;
    private float max=-100000;
    private float min=100000;
    private boolean lookfor=true;
    private int c=0;
    private int diff_indx=0;

    //Filtri non più utilizzati .....................................................................
    private HeartyFilter.SavGolayFilter savgol = new HeartyFilter.SavGolayFilter(1);
    private HeartyFilter.StatFilter stats = new HeartyFilter.StatFilter();
    private LmeFilter lp = new LmeFilter(LP2_B, LP2_A);
    private LmeFilter hp = new LmeFilter(HP2_B, HP2_A);
    private LmeFilter notch = new LmeFilter(NOTCH_B,NOTCH_A);
    private LmeFilter.WndIntFilter meanw = new LmeFilter.WndIntFilter(5);
//    ...........................................................................................

    //    Inizializzazione oggetti per il Pan Tompkins ..............................................
    private PanTompkins pan = new PanTompkins(SEW_SAMPLING_RATE);
    private PerfectTune perfectTune = new PerfectTune();
    private ToneGenerator toneG;
//    ...........................................................................................

    //    Inizializzazione oggetti thread ............................................................
    private Thread setupThread;
    private Thread thread0;
    private Thread thread1;
    private Thread thread2;
    private Thread thread3;
//    ............................................................................................



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Actlif", "onCreate Called");

        setContentView(R.layout.activity_real_time_analysis);
        spinner = findViewById(R.id.spinner_mode);
        scatterchart=findViewById(R.id.chart2);
        chart=findViewById(R.id.chart);
        scatterchart.setVisibility(View.GONE);

        timeDomainFrag = new timeDomainFragment();
        freqMagFrag = new frequencyMagnitudeFragment();
        freqPhasFrag = new frequencyPhaseFragment();
        poincarFrag = new poincarePlotFragment();
        lagPoincarFrag = new laggedPoincareFragment();
        RPeaksFrag=new RPeaksFragment();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.modes_array));
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);

        setup();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                showToast(getApplicationContext(),(String) parent.getItemAtPosition(pos));
                switch (pos) {
                    case 0:
                        whatFragment = 0;
                        setFragment(timeDomainFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 1:
                        whatFragment = 1;
                        c=0;
                        setFragment(RPeaksFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 2:
                        whatFragment = 2;
                        setFragment(freqMagFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 3:
                        whatFragment = 3;
                        setFragment(freqPhasFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 4:
                        whatFragment = 4;
                        chart.setVisibility(View.GONE);
                        scatterchart.setVisibility(View.VISIBLE);
                        setupScatterChart(scatterchart);
                        c=0;
                        setFragment(poincarFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 5:
                        whatFragment = 5;
                        setFragment(lagPoincarFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }


    //Activity Methods Implementation
    private void setup() {

        if (setupThread != null)
            setupThread.interrupt();

        final Runnable setupRunnable = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnables", "setupRunnable Started");

//                setupChart(chart);
                setupChart(chart);
                setupSensor(getApplicationContext());
                runDataStreamThread();


            }
        };
        setupThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    runOnUiThread(setupRunnable);
                    Log.v("Runnables", "setupRunnable Done");
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }

        });

        setupThread.start();


    }

    private void feedMultiple0() {

        if (thread0 != null)
            thread0.interrupt();

        final Runnable runnable0 = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnables", "FeedMultiple0 Started");

//                value0=highpass.next(lowpass.next(value));
                value0=value;

                Log.v("xx", String.valueOf(value0));
//                value0=hp.next(lp.next(savgol.next(value)));
//                value0=savgol.next(value);
//                float tempVar;
//                tempVar = Filter.lowPassNext(value) ;
//                value0 = Filter.highPassNext(tempVar) ;

//                setData0(chart,value0,visibility_range);
            }
        };

        thread0 = new Thread(new Runnable() {

            @Override
            public void run() {


                while (buffered) {
                    runOnUiThread(runnable0);
                    buffered = false;
                    Log.v("Runnables", "FeedMultiple0 Done");
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
                Log.v("Runnables", "FeedMultiple1 Started");

                out = new float[size];
                for (int s = 0; s < size; s++) {
                    complexArray[s] = new Complex((double) in[s], 0);
                }
                fftOut = FFT.fft(complexArray);
                for (int z = 0; z < size / 2; z++) {
                    out[z] = (float) fftOut[z].abs();
                }
                setData(chart,out,size);


            }
        };
        thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                while (buffered) {
                    runOnUiThread(runnable1);
                    buffered = false;
                    Log.v("Runnables", "FeedMultiple1 Done");
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

                Log.v("Runnables", "FeedMultiple2 Starting");
                out = new float[size];
                for (int s = 0; s < size; s++) {
                    complexArray[s] = new Complex((double) in[s], 0);
                }

                fftOut = FFT.fft(complexArray);

                for (int z = 0; z < size / 2; z++) {
                    out[z] = (float) fftOut[z].phase();
                }
                setData(chart,out,size);


            }
        };

        thread2 = new Thread(new Runnable() {

            @Override
            public void run() {

                while (buffered) {
                    runOnUiThread(runnable2);
                    buffered = false;
                    Log.v("Runnables", "FeedMultiple2 Done");
                }
            }

        });

        thread2.start();
    }

    private void runDataStreamThread() {

        final Runnable runnable3 = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnables", "Streaming Starting");
                if (isConnected(sewDevice) == 0 || isConnected(sewDevice) == 1) {
                    if (isInStreaming(sewDevice)) {
                        boolean stream = isInStreaming(sewDevice);
                        start = System.nanoTime();
                        RegularDataBlock[] rdbs;

                        while (stream && canStream) {
                            rdbs = (RegularDataBlock[]) sewDevice.getDataBlocks();

                            for (int c = 0; c < rdbs.length; c++) {

                                if (rdbs[c].getId() == 1) {
                                    float[] signal = rdbs[c].getValues();

                                    for (int z = 0; z < signal.length; z++) {

                                        time = (System.nanoTime() - start) / 1_000_000_000.0;
//                                        Log.v("Timing",String.valueOf(time));
                                        value = signal[z];
                                        streamtofeed = true;
                                        onSensorc();
//                                        Log.v("sewdevice", "Value:  " + String.valueOf(value) + "--" + String.valueOf(z));
//                                        Log.v("sewdevice",  String.valueOf(value));
                                    }
                                }
                            }
                            streamtofeed = false;
                        }
                    } else {
                        tryStream(sewDevice);
                    }
                } else {
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

    private void onSensorc() {
        if (streamtofeed) {
            switch (whatFragment) {
                case 0:
                    buffered = true;
                    value0=pan.highpass.next(pan.lowpass.next(value));
//                  value0=pan.next(value,(long) time);
                    setData0(chart,value0,visibility_range);


                    break;
                case 1:
                    if (c==0){
                        startPeaks=System.nanoTime();
                        timepeakdetector=0;
                    }
                    buffered = true;
                    poincareValue = pan.next(value, (long) time);
                    peakDetector(poincareValue,250,c);
                    c++;
                    break;
                case 2:
                    in[i] = pan.highpass.next(pan.lowpass.next(value));
                    i++;
                    if (i == size) {
                        buffered = true;
                        feedMultiple1();
                        i = 0;
                    }
                    break;
                case 3:
                    in[i] = pan.highpass.next(pan.lowpass.next(value));
                    i++;
                    if (i == size) {
                        buffered = true;
                        feedMultiple2();
                        i = 0;
                    }
                    break;
                case 4:
                    if (c==0){
                        startPeaks=System.nanoTime();
                        timepeakdetector=0;
                    }
                    buffered = true;
                    value0=pan.next(value, (long) time);
                    peakDetector2(value0,200,c);
//                    setDataScat(chart,value0,visibility_range);
//                  value0=pan.next(value,(long) time);

                    c++;
                    break;
                default:
                    buffered = true;
//                    feedMultiple0();
                    value0=pan.highpass.next(pan.lowpass.next(value));
//                  value0=pan.next(value,(long) time);
                    setData0(chart,value0,visibility_range);
                    break;
            }
        }


    }

    int countp=0;
    public void peakDetector(float in, float delta,int count){
        if (count==0){
            max=-100000;
            min=100000;
            lookfor=true;
            c=0;
            timepeakdetector=0;
            countp=0;
            peaktimevector=new double[500];


//            for (int i=0;i<peaktimevector.length;i++) {
//                Log.v("Timing", "Peak Time Vector" + "------------" + String.valueOf(peaktimevector[i]));
//            }

        }
        timepeakdetector=(System.nanoTime() - startPeaks) / 1_000_000_000.0;
        Log.v("Timing","Current peak detector time"+String.valueOf(timepeakdetector));
        if (in>max){max=in;}
        if (in<min){min=in;}
        if(lookfor){
            if (in<max-delta){
                setPoincareData(chart,poincareValue,0,visibility_range);

                min=in;
                lookfor=false;
            }else{
                setPoincareData(chart,poincareValue,0,visibility_range);

            }


        }else{
            if (in>min+delta && in<600){
                rpeaktime=timepeakdetector;
                Log.v("Timing","Current peak time"+"------------"+String.valueOf(timepeakdetector));
                peaktimevector[countp]=rpeaktime;

                if(countp>2){
                    double diff=peaktimevector[countp]-peaktimevector[countp-1];
                    Log.v("Timing","Diff between current r and previous  "+String.valueOf(diff));
                }
                countp++;
                max=in;
                lookfor=true;
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                setPoincareData(chart,poincareValue,in,visibility_range);



            }else{
                setPoincareData(chart,poincareValue,0,visibility_range);

            }
        }
    }

    public void peakDetector2(float in, float delta,int count){
        if (count==0){
            max=-100000;
            min=100000;
            lookfor=true;
            c=0;
            timepeakdetector=0;
            countp=0;
            diff_indx=0;
            peaktimevector=new double[500];
            diffvector=new float[500];


//            for (int i=0;i<peaktimevector.length;i++) {
//                Log.v("Timing", "Peak Time Vector" + "------------" + String.valueOf(peaktimevector[i]));
//            }

        }
        timepeakdetector=(System.nanoTime() - startPeaks) / 1_000_000_000.0;
//        Log.v("Timing","Current peak detector time"+String.valueOf(timepeakdetector));
        if (in>max){max=in;}
        if (in<min){min=in;}
        if(lookfor){
            if (in<max-delta){
//                setPoincareData(chart,value0,0,visibility_range);
                min=in;
                lookfor=false;
            }else{
//                setPoincareData(chart,value0,0,visibility_range);
            }


        }else{
            if (in>min+delta && in<600){
                rpeaktime=timepeakdetector;
                Log.v("Timing","Current peak time"+"------------"+String.valueOf(timepeakdetector));
                peaktimevector[countp]=rpeaktime;

                if(countp>2){
                    double diff=peaktimevector[countp]-peaktimevector[countp-1];
                    diffvector[diff_indx]=(float)diff;
                    Log.v("Timing","Diff between current r and previous  "+String.valueOf(diff));
                    if (diff_indx>2) {
                        setDataScat(scatterchart, diffvector[diff_indx - 1], diffvector[diff_indx], visibility_range);
                    }
                    diff_indx++;
                }
                countp++;
                max=in;
                lookfor=true;
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);




            }else{
//                setPoincareData(chart,value0,0,visibility_range);
            }
        }
    }

    //    Activity Lifecycle
    @Override
    protected void onDestroy() {
        Log.v("Actlif", "onDestroy Called");
        super.onDestroy();
//        removeDataSet();

        if (thread0 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Thread0 interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }
        if (thread1 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "Thread1 interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }
        if (thread2 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "Thread2 interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }
        if (thread3 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "StreamThread interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }

    }

    @Override
    protected void onResume() {
        Log.v("Actlif", "onResume Called");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.v("Actlif", "onStart Called");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.v("Actlif", "onStop Called");
        super.onStop();

    }

    @Override
    protected void onPause() {
        Log.v("Actlif", "onPause Called");
        super.onPause();
        c=0;
        timepeakdetector=0;
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
        i = 0;
        size = n;
        in = new float[size];
        complexArray = new Complex[size];
        fftOut = new Complex[size];
        out = new float[size / 2];
        showToast(getApplicationContext(),String.valueOf(size));
    }

    @Override
    public void timeSize(int n) {
        visibility_range = n;
        chart.fitScreen();
        showToast(getApplicationContext(),String.valueOf(visibility_range));
    }

    @Override
    public void timeSize2(int n) {
        visibility_range = n;
        chart.fitScreen();
        showToast(getApplicationContext(),String.valueOf(visibility_range));
    }

    @Override
    public String sendString() {
        return null;
    }
}
