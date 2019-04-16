package com.example.blueheart;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class recordAndSendLogin extends AppCompatActivity {
    private EditText name;
    private EditText pass;
    private TextView info;
    private Button login;
    private int counter=5;
    private TextView registrationActivitylaunch;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_and_send_login);

        name = findViewById(R.id.name);
        pass=findViewById(R.id.pass);
        login=findViewById(R.id.login);
        info=findViewById(R.id.info);
        registrationActivitylaunch=findViewById(R.id.register);

        info.setText("No of attempts remaining: "+counter);

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);

        FirebaseUser user= firebaseAuth.getCurrentUser();

        if(user!=null){
            finish();
            startActivity(new Intent(getApplicationContext(),recordAndSendActivity.class));
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(name.getText().toString(),pass.getText().toString());
            }
        });

        registrationActivitylaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),registrationActivity.class));
            }
        });

    }

    private void validate(String userName,String userPassword) {

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(userName,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
//                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    checkEmailVerification();
                }else{
                    Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_SHORT).show();
                    counter--;
                    info.setText("No of attempts remaining: "+counter);
                    progressDialog.dismiss();
                    if(counter==0){
                        login.setEnabled(false);
                    }
                }
            }
        });

    }

    private void checkEmailVerification(){
        FirebaseUser firebaseUser=firebaseAuth.getInstance().getCurrentUser();
        Boolean emailflag=firebaseUser.isEmailVerified();

        if(emailflag){
            finish();
            startActivity(new Intent(getApplicationContext(),recordAndSendActivity.class));
        }else{
            Toast.makeText(this,"Verify your email!",Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();
        }
    }



}
