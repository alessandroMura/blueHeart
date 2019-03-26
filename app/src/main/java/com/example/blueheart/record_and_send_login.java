package com.example.blueheart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class record_and_send_login extends AppCompatActivity implements View.OnClickListener{
    static public String MAIN_BUTTON_MESSAGE="Record and send login Activity Button";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_and_send_login);

        Button login = findViewById(R.id.login_button);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                Log.v(MAIN_BUTTON_MESSAGE, "Logging In!");
                Intent record_and_send = new Intent(this, recordAndSendActivity.class);
                startActivity(record_and_send);
                break;
            default:
                break;
        }
    }



}
