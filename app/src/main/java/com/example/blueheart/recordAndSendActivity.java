package com.example.blueheart;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sew.RegularDataBlock;

import static com.example.blueheart.bluheartConstants.SEW_SAMPLING_RATE;
import static com.example.blueheart.deviceListActivity.sewDevice;
import static com.example.blueheart.graphUtilities.setPoincareData;
import static com.example.blueheart.graphUtilities.setupChart;
import static com.example.blueheart.sensorUtilities.isConnected;
import static com.example.blueheart.sensorUtilities.isInStreaming;
import static com.example.blueheart.sensorUtilities.setupSensor;
import static com.example.blueheart.sensorUtilities.tryConnect;
import static com.example.blueheart.sensorUtilities.tryDisconnect;
import static com.example.blueheart.sensorUtilities.tryStream;
import org.apache.commons.math3.stat.descriptive.*;

public class recordAndSendActivity extends AppCompatActivity {

    private static final String TAG = "recordAndSendActivity";

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mConversationArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

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
    DecimalFormat numberFormat = new DecimalFormat("#00.00000");
    private double sd1,sd2,S;

    private int STORAGE_PERMISSION_CODE = 1;
    private boolean onSave;
    private double [] val=new double[1000];
    private int i;
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluheart";
    List<Double> vallist = new ArrayList<Double>();

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.activity_record_and_send);

        startb=findViewById(R.id.startrec);
        stop=findViewById(R.id.endrec);
        send=findViewById(R.id.send);
        currentrr=findViewById(R.id.rrdist);
        currentsd1=findViewById(R.id.sd1value);
        currentsd2=findViewById(R.id.sd2value);
        currents=findViewById(R.id.svalue);
        chart=findViewById(R.id.recchart);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 30);

        File dir = new File(path);
        dir.mkdirs();

        if (ContextCompat.checkSelfPermission(recordAndSendActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(recordAndSendActivity.this, "You have already granted this permission!",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestStoragePermission();
        }


        startb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave=true;
                vallist.clear();
                i=0;
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave=false;
                i=0;
                writeFile2();
            }
        });

        firebaseAuth=FirebaseAuth.getInstance();

        setup();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mChatService.getState()!=BluetoothChatService.STATE_CONNECTED) {
                    Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity2.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                    Toast.makeText(getApplicationContext(),"Waiting...",Toast.LENGTH_SHORT).show();
                }else{
                    sendFile();
                }

            }
        });

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

    private void setupChat() {
        mChatService = new BluetoothChatService(getParent(), mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getParent(), "You are not connected to a device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
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
            diff_indx=0;
        }

        Log.v("Time","Current peak detector time"+String.valueOf(timepeakdetector));
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
                timepeakdetector=(System.nanoTime() - startPeaks) / 1_000_000_000.0000000;
                rpeaktime=timepeakdetector;
                peaktimevector[countp]=rpeaktime;

                if(countp>=1){
                    double diff=peaktimevector[countp]-peaktimevector[countp-1];

                    if (diff>=0.3 && diff<=1.4000){

                        diffvector[diff_indx] = diff;
                        Log.v("t0","Diff between current r and previous  "+diff);
                        sd1=SD1(diffvector);
                        sd2=SD2(diffvector);
                        S=SArea(sd1,sd2);
                        Log.v("t1","Diff between current r and previous  "+sd1);
                        Log.v("t2","Diff between current r and previous  "+ sd2);
                        Log.v("t3","Diff between current r and previous  "+ S);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                currentrr.setText(String.valueOf(diffvector[diff_indx-1]));
                                currentsd1.setText(String.valueOf(sd1));
                                currentsd2.setText(String.valueOf(sd2));
                                currents.setText(String.valueOf(S));
                            }
                        });

                        if (onSave){
                            vallist.add(diffvector[diff_indx]);
                            vallist.add(sd1);
                            vallist.add(sd2);
                            vallist.add(S);
                            i++;
                        }
                        diff_indx++;
                    }
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


    public void writeFile2(){
        if(isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            File textFile = new File(path, "File_" + System.nanoTime() + ".txt");
            if(textFile.exists()){
                textFile.delete();
                try {
                    textFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try{
                FileOutputStream fos = new FileOutputStream(textFile,true);

                for(int j=0; j<vallist.size()-4; j=j+4) {
                    String s ="N: "+j+ " --RR: " + vallist.get(j) +" SD1: "+vallist.get(j+1)+" SD2: "+vallist.get(j+2)+" S: "+vallist.get(j+3)+ "\n";
                    fos.write(s.getBytes());
                }

                fos.close();

                Toast.makeText(this, "File Saved.", Toast.LENGTH_SHORT).show();
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Cannot Write to External Storage.", Toast.LENGTH_SHORT).show();
        }
    }


    public void sendFile(){
        for(int j=0; j<vallist.size()-4; j=j+4) {
            String s ="N: "+j+ " --RR: " + vallist.get(j) +" SD1: "+vallist.get(j+1)+" SD2: "+vallist.get(j+2)+" S: "+vallist.get(j+3)+ "\n";
            sendMessage(s);
        }
    }


    private void setStatus(int resId) {
        Activity activity = getParent();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        Activity activity = getParent();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity=getParent();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
//                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity2 returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity2 returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getParent(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getParent().finish();
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity2#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity2.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed for writing of txt files on external memory.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(recordAndSendActivity.this,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.i("State","Yes, it is writable!");
            return true;
        }else{
            return false;
        }
    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_chat,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity2 to see devices and do scan
                Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity2.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.logoutmenu:{
                Logout();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.v("Actlif", "onDestroy Called");
        super.onDestroy();
//        removeDataSet();

        canStream=false;
        if (thread3 != null) {
            thread3.interrupt();
            canStream = false;

            tryDisconnect(sewDevice);
            Log.v("sewdevice", "Sew State:  " + String.valueOf(isConnected(sewDevice)));
            Log.v("sewdevice", "StreamThread interrupted:  " + String.valueOf(thread3.isInterrupted()));
        }

        if (mChatService != null) {
            mChatService.stop();
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
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }


}
