package com.example.blueheart;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;

import sew.RegularDataBlock;

import static com.example.blueheart.bluheartConstants.SEW_SAMPLING_RATE;
import static com.example.blueheart.deviceListActivity.sewDevice;
import static com.example.blueheart.graphUtilities.setData0;
import static com.example.blueheart.graphUtilities.setPoincareData;
import static com.example.blueheart.graphUtilities.setupChart;
import static com.example.blueheart.sensorUtilities.isConnected;
import static com.example.blueheart.sensorUtilities.isInStreaming;
import static com.example.blueheart.sensorUtilities.setupSensor;
import static com.example.blueheart.sensorUtilities.tryConnect;
import static com.example.blueheart.sensorUtilities.tryDisconnect;
import static com.example.blueheart.sensorUtilities.tryStream;

public class recordAndSendActivity extends AppCompatActivity {

    private Button startb,stop,send;
    private TextView currentrr,currentsd1,currentsd2,currents;
    private LineChart chart;
    private ToneGenerator toneG;

    private Thread setupThread,thread3;

    private float value = 0;
    private boolean canStream = true;
    private boolean streamtofeed;
    private long start;
    private double time;
    private long startPeaks;
    private double timepeakdetector;
    private int c=0;
    private static boolean buffered2 = true;
    private PanTompkins pan = new PanTompkins(SEW_SAMPLING_RATE);
    private float poincareValue = 0;


    private double rpeaktime;
    private double peaktimevector[]=new double[500];
    private double diffvector[]=new double[500];
    private float max=-100000;
    private float min=100000;
    private boolean lookfor=true;
    private int diff_indx=0;
    private int visibility_range = 1024;



    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_and_send);

        startb=findViewById(R.id.startrec);
        stop=findViewById(R.id.endrec);
        send=findViewById(R.id.send);
        currentrr=findViewById(R.id.rrdist);
        currentsd1=findViewById(R.id.sd1value);
        currentsd2=findViewById(R.id.sd2value);
        currents=findViewById(R.id.svalue);
        chart=findViewById(R.id.recchart);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 50);


        firebaseAuth=FirebaseAuth.getInstance();

        setup();


    }

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
                    if (c==0){
                        startPeaks=System.nanoTime();
                        timepeakdetector=0;
                    }
                    buffered2 = true;
                    poincareValue = pan.next(value, (long) time);
                    peakDetector(poincareValue,250,c);
                    c++;
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
        timepeakdetector=(System.nanoTime() - startPeaks) / 1_000_000_000.0000000;
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


    @Override
    protected void onDestroy() {
        Log.v("Actlif", "onDestroy Called");
        super.onDestroy();
//        removeDataSet();

        if (thread3 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "StreamThread interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }

    }

    @Override
    protected void onPause() {
        Log.v("Actlif", "onPause Called");
        super.onPause();
        c=0;
        timepeakdetector=0;
    }


    private void Logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(),recordAndSendLogin.class));
        if (thread3 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "StreamThread interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.record_and_send_menu,menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.logoutmenu:{
                Logout();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
