package com.example.blueheart;

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
import com.github.mikephil.charting.charts.LineChart;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.PointsGraphSeries;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.ArrayList;
import java.util.Arrays;
import sew.RegularDataBlock;

import static com.example.blueheart.deviceListActivity.sewDevice;
import static com.example.blueheart.utilities.*;
import static com.example.blueheart.sensorUtilities.*;
import static com.example.blueheart.bluheartConstants.*;
import static com.example.blueheart.graphUtilities.*;


public class realTimeAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, parameterSend {

    //    Graphic elements Initialization ..............................................................
    Fragment timeDomainFrag, freqMagFrag, freqPhasFrag, poincarFrag,RPeaksFrag;
    Spinner spinner;
    //    private LineChart chart;
    private LineChart chart;
    private GraphView scatterchart;
    private ArrayList<XYValue> xyValueArray;
    private PointsGraphSeries<DataPoint> xySeries;

    //    Options ......................................................................................
    private int visibility_range = 1024; //Numero campioni visualizzati nel grafico temporale all'inizializzazione
    private int visibility_range2 = 1024; //Numero campioni visualizzati nel grafico temporale all'inizializzazione
    private static int size = 256; //Numero campioni per la FFT all'inizializzazione
    private static int size2 = 256; //Numero campioni per la FFT all'inizializzazione
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
    private long start;
    private long startPeaks;
    private double timepeakdetector;
    private double rpeaktime;
    private double peaktimevector[]=new double[500];
    private double diffvector[]=new double[500];
    private double diff;
    private double sd1;
    private double sd2;
    private double S;
    private double time;
    private float max=-100000;
    private float min=100000;
    private boolean lookfor=true;
    private int c=0;
    private int diff_indx=0;
    private int lag=1;

    //    Inizializzazione oggetti per il Pan Tompkins ..............................................
    private PanTompkins pan = new PanTompkins(SEW_SAMPLING_RATE);
    private ToneGenerator toneG;
//    ...........................................................................................

    //    Inizializzazione oggetti thread ............................................................
    private Thread setupThread;
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
        scatterchart=findViewById(R.id.scatterchart);
        chart=findViewById(R.id.chart);
        scatterchart.setVisibility(View.GONE);

        timeDomainFrag = new timeDomainFragment();
        freqMagFrag = new frequencyMagnitudeFragment();
        freqPhasFrag = new frequencyPhaseFragment();
        poincarFrag = new poincarePlotFragment();
        RPeaksFrag=new RPeaksFragment();

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.modes_array));
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
        xyValueArray = new ArrayList<>();

        setup();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                showToast(getApplicationContext(),(String) parent.getItemAtPosition(pos));
                switch (pos) {
                    case 0:
                        whatFragment = 0;
                        scatterchart.setVisibility(View.GONE);
                        chart.setVisibility(View.VISIBLE);
                        c=0;
                        setFragment(timeDomainFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 1:
                        whatFragment = 1;
                        scatterchart.setVisibility(View.GONE);
                        chart.setVisibility(View.VISIBLE);

                        c=0;
                        setFragment(RPeaksFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 2:
                        whatFragment = 2;
                        scatterchart.setVisibility(View.GONE);
                        chart.setVisibility(View.VISIBLE);
                        c=0;
                        setFragment(freqMagFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 3:
                        whatFragment = 3;
                        scatterchart.setVisibility(View.GONE);
                        chart.setVisibility(View.VISIBLE);
                        c=0;
                        setFragment(freqPhasFrag,getSupportFragmentManager(),R.id.fragment_frame);
                        break;
                    case 4:
                        whatFragment = 4;
                        chart.setVisibility(View.GONE);
                        scatterchart.setVisibility(View.VISIBLE);
                        c=0;
                        lag=1;
                        setFragment(poincarFrag,getSupportFragmentManager(),R.id.fragment_frame);
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
                out = new float[size2];
                for (int s = 0; s < size2; s++) {
                    complexArray[s] = new Complex((double) in[s], 0);
                }

                fftOut = FFT.fft(complexArray);

                for (int z = 0; z < size2 / 2; z++) {
                    out[z] = (float) fftOut[z].phase();
                }
                setData(chart,out,size2);
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

                                        time = (System.nanoTime() - start) / 1_000_000_000.0000000;
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
//                    Log.v("valuexxx: ",  String.valueOf(value0));
                    runOnUiThread(new Runnable() {
                        public void run() {
                            changeTimeSizeTextView(String.valueOf(visibility_range));
                        }
                    });

                    setData0(chart,value0,visibility_range);
                    chart.setVisibleXRangeMaximum(visibility_range);

                    break;
                case 1:
                    if (c==0){
                        startPeaks=System.nanoTime();
                        timepeakdetector=0;
                    }
                    buffered = true;
                    poincareValue = pan.next(value, (long) time);
                    peakDetector(poincareValue,250,c);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            changeTimeSize2TextView(String.valueOf(visibility_range2));
                        }
                    });
                    c++;
                    break;
                case 2:
                    in[i] = pan.highpass.next(pan.lowpass.next(value));
                    runOnUiThread(new Runnable() {
                        public void run() {
                            changeMagSizeTextView(String.valueOf(size));
                        }
                    });
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
                    if (i == size2) {
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
                    c++;
                    break;
                default:
                    buffered = true;
                    value0=pan.highpass.next(pan.lowpass.next(value));
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
            diff=0;
            scatterchart.removeAllSeries();
        }


        timepeakdetector=(System.nanoTime() - startPeaks) / 1_000_000_000.0000000;
        Log.v("Timing","Current peak detector time"+String.valueOf(timepeakdetector));
        if (in>max){max=in;}
        if (in<min){min=in;}
        if(lookfor){
            if (in<max-delta){
                setPoincareData(chart,poincareValue,0,visibility_range2);
                min=in;
                lookfor=false;
            }else{
                setPoincareData(chart,poincareValue,0,visibility_range2);

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
                setPoincareData(chart,poincareValue,in,visibility_range2);
            }else{
                setPoincareData(chart,poincareValue,0,visibility_range2);
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
            diffvector=new double[500];
            xyValueArray=new ArrayList<>();
            xySeries = new PointsGraphSeries<>();
            diff=0;
            sd1=0;
            scatterchart.removeAllSeries();
        }

//        Log.v("Timing","Current peak detector time"+String.valueOf(timepeakdetector));
        if (in>max){max=in;}
        if (in<min){min=in;}
        if(lookfor){
            if (in<max-delta){
                min=in;
                lookfor=false;
            }else{ }
        }else{
            if (in>min+delta && in<600){
                timepeakdetector=(System.nanoTime() - startPeaks) / 1_000_000_000.0000000;
                rpeaktime=timepeakdetector;
                Log.v("Timing","Current peak time"+"------------"+String.valueOf(timepeakdetector));
                peaktimevector[countp]=rpeaktime;

                if(countp>=lag) {
                     diff = peaktimevector[countp] - peaktimevector[countp - 1];
                    if (diff >= 0.3 && diff <= 1.4000) {
                        diffvector[diff_indx] = diff;
                        Log.v("Timing", "Diff between current r and previous:  " + String.valueOf(diff)+"Num: "+String.valueOf(diff_indx));
                        if ((diff_indx>=lag) && (diff_indx % lag==0)) {
                            initscatt(scatterchart,xyValueArray,xySeries,diffvector[diff_indx - lag],diffvector[diff_indx]);

                            sd1=SD1(diffvector);
                            sd2=SD2(diffvector);
                            S=SArea(sd1,sd2);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    changeFragmentTextView(String.valueOf(sd1),String.valueOf(sd2),String.valueOf(S));
                                    changeLagTextView(String.valueOf(lag));
                                }
                            });
                        }
                        diff_indx++;
                    }
                }
                countp++;
                max=in;
                lookfor=true;
                toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);




            }else{ }
        }
    }


    public void changeFragmentTextView(String s1,String s2,String s) {
       poincarePlotFragment frag=(poincarePlotFragment)poincarFrag.getFragmentManager().findFragmentById(R.id.fragment_frame);
       frag.changeFragmentTextView(s1,s2,s);
    }

    public void changeLagTextView(String nn) {
        poincarePlotFragment frag=(poincarePlotFragment)poincarFrag.getFragmentManager().findFragmentById(R.id.fragment_frame);
        frag.changeLagTextView(nn);
    }

    public void changeTimeSizeTextView(String nn) {
        timeDomainFragment frag=(timeDomainFragment)timeDomainFrag.getFragmentManager().findFragmentById(R.id.fragment_frame);
        frag.changeTimeTextView(nn);
    }

    public void changeTimeSize2TextView(String nn) {
        RPeaksFragment frag=(RPeaksFragment)RPeaksFrag.getFragmentManager().findFragmentById(R.id.fragment_frame);
        frag.changeTime2TextView(nn);
    }

    public void changeMagSizeTextView(String nn) {
        frequencyMagnitudeFragment frag=(frequencyMagnitudeFragment) freqMagFrag.getFragmentManager().findFragmentById(R.id.fragment_frame);
        frag.changeMagTextView(nn);
    }

    public void changePhaSizeTextView(String nn) {
        frequencyPhaseFragment frag=(frequencyPhaseFragment) freqPhasFrag.getFragmentManager().findFragmentById(R.id.fragment_frame);
        frag.changePhaTextView(nn);
    }

    public int getVisibility_range2(){
        return visibility_range2;
    }

    public int getVisibility_range() {
        return visibility_range;
    }

    public int getSize() {
        return size;
    }

    public int getSize2() {
        return size2;
    }

    public double SD1(double [] in){
        double [] xp;
        double [] xm;

        xp= Arrays.copyOfRange(in, 0, diff_indx);
        xm= Arrays.copyOfRange(in, 1, diff_indx+1);
        double [] df=new double[xm.length];

        for (int ix=0;ix<xm.length;ix++){
            df[ix]=xp[ix]-xm[ix];
        }
        double sd1=new DescriptiveStatistics(df).getStandardDeviation();
        return sd1/Math.sqrt(2);
    }

    public double SD2(double [] in){

        double [] xp;
        double [] xm;

        xp= Arrays.copyOfRange(in, 0, diff_indx);
        xm= Arrays.copyOfRange(in, 1, diff_indx+1);
        double [] df=new double[xm.length];
        for (int ix=0;ix<xm.length-1;ix++){
            df[ix]=xp[ix]+xm[ix];
        }
        double sd2=new DescriptiveStatistics(df).getVariance();
        return sd2/Math.sqrt(2);
    }

    public double SArea(double sd1,double sd2){
        return Math.PI*sd1*sd2;
    }


    //    Activity Lifecycle
    @Override
    protected void onDestroy() {
        Log.v("Actlif", "onDestroy Called");
        super.onDestroy();
//        removeDataSet();
        c=0;

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
        c=0;

    }

    @Override
    protected void onPause() {
        Log.v("Actlif", "onPause Called");
        super.onPause();
        c=0;
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
    public void fftSizePhase(int n) {
        i = 0;
        size2 = n;
        in = new float[size2];
        complexArray = new Complex[size2];
        fftOut = new Complex[size2];
        out = new float[size2 / 2];
        showToast(getApplicationContext(),String.valueOf(size2));
    }

    @Override
    public void selectLag(int number) {

        if (number<1){
            number=1;
            max=-100000;
            min=100000;
            lookfor=true;
            c=0;
            timepeakdetector=0;
            countp=0;
            diff_indx=0;
            peaktimevector=new double[500];
            diffvector=new double[500];
            xyValueArray=new ArrayList<>();
            xySeries = new PointsGraphSeries<>();
            diff=0;
            sd1=0;
            scatterchart.removeAllSeries();
            lag=1;
            showToast(getApplicationContext(),"Lag: "+number+" must be >1");
            runOnUiThread(new Runnable() {
                public void run() {
                    changeLagTextView(String.valueOf(lag));
                }
            });
        }

        if (number>10){
            number=10;
            max=-100000;
            min=100000;
            lookfor=true;
            c=0;
            timepeakdetector=0;
            countp=0;
            diff_indx=0;
            peaktimevector=new double[500];
            diffvector=new double[500];
            xyValueArray=new ArrayList<>();
            xySeries = new PointsGraphSeries<>();
            diff=0;
            sd1=0;
            scatterchart.removeAllSeries();
            lag=10;
            showToast(getApplicationContext(),"Lag: "+number+" must be <10");
            runOnUiThread(new Runnable() {
                public void run() {
                    changeLagTextView(String.valueOf(lag));
                }
            });
        }


        if (number>=1 || number <=10){
            max=-100000;
            min=100000;
            lookfor=true;
            c=0;
            timepeakdetector=0;
            countp=0;
            diff_indx=0;
            peaktimevector=new double[500];
            diffvector=new double[500];
            xyValueArray=new ArrayList<>();
            xySeries = new PointsGraphSeries<>();
            diff=0;
            sd1=0;
            scatterchart.removeAllSeries();
            lag=number;
            showToast(getApplicationContext(),"Lag: "+lag);
            runOnUiThread(new Runnable() {
                public void run() {
                    changeLagTextView(String.valueOf(lag));
                }
            });
        }
    }

    @Override
    public void timeSize(int n1) {

            visibility_range = n1;

            chart.fitScreen();
            showToast(getApplicationContext(), String.valueOf(visibility_range));

    }

    @Override
    public void timeSize2(int n) {
        visibility_range2 = n;
        chart.fitScreen();
        showToast(getApplicationContext(), String.valueOf(visibility_range2));

    }


}
