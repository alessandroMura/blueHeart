package com.example.blueheart;

//Import delle classi utilizzate
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import static com.example.blueheart.utilities.*;


//Activity principale che gestisce l'accensione del bluetooth
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    Dichiarazione variabili globali interne utilizzate nella classe Main
    static public String MAIN_BUTTON_MESSAGE="MainActivity Button";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    //    inizializzazione Adapter Bluetooth
    public static BluetoothAdapter mBlueAdapter;

//Override del metodo onCreate che setta i listeners sui button del layout
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btOn = findViewById(R.id.btOn);
        btOn.setOnClickListener(this);

        Button btOff = findViewById(R.id.btOff);
        btOff.setOnClickListener(this);

        Button make_Discoverable = findViewById(R.id.btMakeDiscoverable);
        make_Discoverable.setOnClickListener(this);

        Button device_list= findViewById(R.id.device_list);
        device_list.setOnClickListener(this);

        //get dell'adapter di default
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

//            Case del button TURN ON BLUETOOTH
            case R.id.btOn:

                Log.v(MAIN_BUTTON_MESSAGE, "BtOn clicked!");
                if (!mBlueAdapter.isEnabled()){
                    showToast(getApplicationContext(),"Turning On Bluetooth...");
                    //intent che attiva il bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    showToast(getApplicationContext(),"Bluetooth ON");

                }
                else {
                    showToast(getApplicationContext(),"Bluetooth is already on");
                }
                break;

//            Case del button TURN OFF BLUETOOTH
            case R.id.btOff:
                Log.v(MAIN_BUTTON_MESSAGE, "BtnOff clicked!");


                if (mBlueAdapter.isEnabled()){
                    mBlueAdapter.disable();
                    showToast(getApplicationContext(),"Turning Bluetooth Off");

                }
                else {
                    showToast(getApplicationContext(),"Bluetooth is already off");
                }

                break;

//                Case del button MAKE DEVICE DISCOVERABLE
            case R.id.btMakeDiscoverable:
                Log.v(MAIN_BUTTON_MESSAGE, "Make_discoverable clicked!");

                if (!mBlueAdapter.isDiscovering()){
                    showToast(getApplicationContext(),"Making Your Device Discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                    showToast(getApplicationContext(),"Device is now Discoverable");
                }

                break;

//            Case del button DEVICE LIST
            case R.id.device_list:
                Intent deviceListIntent = new Intent(this, deviceListActivity.class);
                Log.v(MAIN_BUTTON_MESSAGE, "Device_list clicked!");

                showToast(getApplicationContext(),"Listing Paired Devices");
                startActivity(deviceListIntent);
                break;
            default:
                break;
        }
    }

    static public String toString(String s) {
        return s;
    }

}
