package com.example.blueheart;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class recordAndSendActivity extends AppCompatActivity {


    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_and_send);


        firebaseAuth=FirebaseAuth.getInstance();


    }

    private void Logout(){
        firebaseAuth.signOut();
        finish();
        startActivity(new Intent(getApplicationContext(),recordAndSendLogin.class));
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
