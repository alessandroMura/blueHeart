package com.example.blueheart;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.karlotoy.perfectune.instance.PerfectTune;

import java.util.ArrayList;

import sew.RegularDataBlock;

import static com.example.blueheart.deviceListActivity.sewDevice;
import static com.example.blueheart.utilities.*;
import static com.example.blueheart.sensorUtilities.*;
import static com.example.blueheart.bluheartConstants.*;
import static com.example.blueheart.graphUtilities.*;


public class realTimeAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, parameterSend {

    //Graphic elements Initialization
    Fragment timeDomainFrag, freqMagFrag, freqPhasFrag, poincarFrag, lagPoincarFrag;
    Spinner spinner;
    private LineChart chart;

    //Options
    private int visibility_range = 1024; //Numero campioni visualizzati nel grafico temporale all'inizializzazione
    private static int size = 512; //Numero campioni per la FFT all'inizializzazione
    private Complex complexArray[] = new Complex[size]; //Array di complessi per la fft in ingresso
    private Complex fftOut[] = new Complex[size]; //Array di complessi per la fft in uscita
    private float out[] = new float[size / 2]; // Array float in uscita per la fft reale o imag
    private float in[] = new float[size]; //Array di valori presi dal sensore per l'ingresso della fft
    private float value = 0; //variabile temporanea per il valore I-esimo preso dal sensore
    private float poincareValue = 0;
    private float value0 = 0;

    // Boolean Variables
    private static boolean buffered = true;
    private boolean canStream = true;
    private boolean streamtofeed;
    private int whatFragment;
    private int i = 0;

    //Parametri, oggetti e variabili per il filtraggio
    private float minrp = -100f;
    private float maxrp = 100f;
    private float rangerp = 200f;
    private float peakp;
    private long start;
    private double time;

    private float max=MINUS_INFINITE;
    private float min=PLUS_INFINITE;
    private boolean lookfor=true;
    private int c=0;


    private HeartyFilter.SavGolayFilter savgol = new HeartyFilter.SavGolayFilter(1);
    private HeartyFilter.StatFilter stats = new HeartyFilter.StatFilter();

    private LmeFilter lp = new LmeFilter(LP2_B, LP2_A);
    private LmeFilter hp = new LmeFilter(HP2_B, HP2_A);

    private LmeFilter notch = new LmeFilter(NOTCH_B,NOTCH_A);

    private LmeFilter.WndIntFilter meanw = new LmeFilter.WndIntFilter(5);

    private PanTompkins pan = new PanTompkins(SEW_SAMPLING_RATE);

    private PerfectTune perfectTune = new PerfectTune();

    //    Inizializzazione oggetti thread
    private Thread setupThread;
    private Thread thread0;
    private Thread thread1;
    private Thread thread2;
    private Thread thread3;
    private Thread poincareThread;
    private ToneGenerator toneG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("Actlif", "onCreate Called");

        setContentView(R.layout.activity_real_time_analysis);
        spinner = findViewById(R.id.spinner_mode);
        chart=findViewById(R.id.chart);

        timeDomainFrag = new timeDomainFragment();
        freqMagFrag = new frequencyMagnitudeFragment();
        freqPhasFrag = new frequencyPhaseFragment();
        poincarFrag = new poincarePlotFragment();
        lagPoincarFrag = new laggedPoincareFragment();

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
                        setFragment(freqMagFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 2:
                        whatFragment = 2;
                        setFragment(freqPhasFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 3:
                        whatFragment = 3;

                        setFragment(poincarFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 4:
                        whatFragment = 4;
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

                setupChart(chart,R.id.chart);
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

                setData0(chart,value0,visibility_range);
            }
        };

        thread0 = new Thread(new Runnable() {

            @Override
            public void run() {


                while (buffered) {
                    runOnUiThread(runnable0);
                    buffered = false;
                    Log.v("Runnables", "FeedMultiple0 Done");
//                    try {
//                        Thread.sleep(0);
//                    }
//                    catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
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
//                Log.v("Running","run");

            }
        };

        thread1 = new Thread(new Runnable() {

            @Override
            public void run() {

                while (buffered) {
                    runOnUiThread(runnable1);
                    buffered = false;
                    Log.v("Runnables", "FeedMultiple1 Done");
//                    try {
//
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
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
//                Log.v("Running","run");

            }
        };

        thread2 = new Thread(new Runnable() {

            @Override
            public void run() {

                while (buffered) {
                    runOnUiThread(runnable2);
                    buffered = false;
                    Log.v("Runnables", "FeedMultiple2 Done");

//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }

        });

        thread2.start();
    }

    private void poincarePlotThread() {
        if (poincareThread != null)
            poincareThread.interrupt();
        final Runnable poincareRunnable = new Runnable() {

            @Override
            public void run() {
                Log.v("Runnables", "Poincare Starting");

                //                poincareValue=highpass.next(lowpass.next(value));

                poincareValue = pan.next(value, (long) time);
                Log.v("pantom", "Time:  " + String.valueOf(time) + "  Value:  " + String.valueOf(poincareValue));

                stats.next(poincareValue);
                minrp = stats.min;
                maxrp = stats.max;
                rangerp = stats.range;
                if (poincareValue > 70f) {
                    Log.v("sewdevice", "Peak finder:  Value= " + poincareValue);
                    peakp = maxrp;

                } else {
                    peakp = 0f;
                }
                Log.v("sewdevice", "Max:  " + maxrp + "");
                Log.v("sewdevice", "Min:  " + minrp + "");
                Log.v("sewdevice", "Range:  " + rangerp + "");
                setPoincareData(chart,poincareValue, peakp,visibility_range,c);


            }
        };

        poincareThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (buffered) {
                    runOnUiThread(poincareRunnable);
                    buffered = false;
                    Log.v("Runnables", "Poincare Done");
                }
            }

        });

        poincareThread.start();


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
                                        value = signal[z];
                                        streamtofeed = true;
                                        onSensorc();
//                                        Log.v("sewdevice", "Value:  " + String.valueOf(value) + "--" + String.valueOf(z));
                                        Log.v("sewdevice",  String.valueOf(value));


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
                    buffered = true;
//                    poincarePlotThread();
                    poincareValue = pan.next(value, (long) time);
                    Log.v("pantom", "Time:  " + String.valueOf(time) + "  Value:  " + String.valueOf(poincareValue));

                    peakDetector(poincareValue,250,c);

                    break;
                default:
                    buffered = true;
                    feedMultiple0();
                    break;
            }
        }


    }


    public void peakDetector(float in, float delta, int count){
        if (count==0){
            max=MINUS_INFINITE;
            min=PLUS_INFINITE;
            lookfor=true;
            c=0;
        }
        if (in>max){max=in;}
        if (in<min){min=in;}
        if(lookfor){
            if (in<max-delta){
                setPoincareData(chart,poincareValue,0,visibility_range,c);
                min=in;
                lookfor=false;
            }else{
                setPoincareData(chart,poincareValue,0,visibility_range,c);
            }


        }else{
            if (in>min+delta && in<600){
                max=in;
                lookfor=true;
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                setPoincareData(chart,poincareValue,in,visibility_range,c);


            }else{
                setPoincareData(chart,poincareValue,0,visibility_range,c);
            }
        }
    }


    //    Activity Lifecycle
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
    }

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
        if (poincareThread != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "PoincareThread interrupted:  " + String.valueOf(thread3.isInterrupted()));
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
    public String sendString() {
        return null;
    }
}
