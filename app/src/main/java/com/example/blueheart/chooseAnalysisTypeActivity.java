package com.example.blueheart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class chooseAnalysisTypeActivity extends AppCompatActivity implements View.OnClickListener {
    static public String MAIN_BUTTON_MESSAGE="Choose Analysis type Activity Button";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose__analysis_type);

        Button real_time_button=findViewById(R.id.real_time_button);
        real_time_button.setOnClickListener(this);
        Button record_and_send_log=findViewById(R.id.record_and_send);
        record_and_send_log.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.real_time_button:
                Log.v(MAIN_BUTTON_MESSAGE,"Real Time Analysis chosen!");
                Intent start_real_time=new Intent(this, realTimeAnalysisActivity.class);
                startActivity(start_real_time);
                break;
            case R.id.record_and_send:
                Log.v(MAIN_BUTTON_MESSAGE,"Record and send chosen!");
                Intent start_record_and_send=new Intent(this, recordAndSendLogin.class);
                startActivity(start_record_and_send);
                break;
            default:
                break;
        }

    }


}
