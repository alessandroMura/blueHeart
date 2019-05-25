package com.example.blueheart;

//Import delle classi utilizzate
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

import sew.SewBluetoothDevice;

import static com.example.blueheart.utilities.*;


//Activity che gestisce i device bluetooth in cui è possibile selezionare il device per l'acquisizione
public class deviceListActivity extends AppCompatActivity {

    //    Dichiarazione variabili globali interne utilizzate nella classe Main
    static public String MAIN_BUTTON_MESSAGE="Device List Activity Button";

    //    inizializzazione Adapter Bluetooth
    static public SewBluetoothDevice sewDevice;
    static public String currentID;
    static public String currentAddress;

    //Override del metodo onCreate che setta la listview con i device accoppiati all'interno
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        if (MainActivity.mBlueAdapter.isEnabled()){

            Set<BluetoothDevice> devices = MainActivity.mBlueAdapter.getBondedDevices();
            final ArrayList<BluetoothDevice> devs=new ArrayList<>();
            for (BluetoothDevice device: devices){
                Log.v(MAIN_BUTTON_MESSAGE,MainActivity.toString("\nDevice: " + device.getName()+ ", " + device));
                devs.add(device);
            }
            final deviceAdapter itemsAdapter = new deviceAdapter(this, devs);
            final ListView listView = findViewById(R.id.list);
            listView.setAdapter(itemsAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.v("Item Click: ",devs.get(position).getName() );

                    currentID=devs.get(position).getName();
                    currentAddress=devs.get(position).getAddress();

                    if (currentID.substring(0,3).equals("sew")){
                        Intent choose_analysis = new Intent(deviceListActivity.this, chooseAnalysisTypeActivity.class);
                        showToast(getApplicationContext(),"Choose Analysis");
                        startActivity(choose_analysis);
                    }
                    else {
                        showToast(getApplicationContext(),"Not a sew device! ");
                    }
                }
            });
        }
        else {
            //bluetooth is off so can't get paired devices
            showToast(getApplicationContext(),"Turn on bluetooth to get paired devices");
        }

    }

}
