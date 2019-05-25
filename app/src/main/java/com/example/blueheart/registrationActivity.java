package com.example.blueheart;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class registrationActivity extends AppCompatActivity {

    private EditText name;
    private EditText email;
    private EditText pass;
    private Button register;
    private TextView alreadysigned;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupUiViews();

        firebaseAuth=FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()){
                    //Upload database with user data
                    String user_email=email.getText().toString().trim();
                    String user_password=pass.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                sendEmailVerification();
                            }else{
                                Toast.makeText(getApplicationContext(), "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });


        alreadysigned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),recordAndSendLogin.class));
            }
        });

    }

    private void sendEmailVerification(){
        FirebaseUser firebaseUser=firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(),"Successfully Registered, Verification Mail sent!",Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(getApplicationContext(),recordAndSendLogin.class));
                    }else{
                        Toast.makeText(getApplicationContext(),"Verification Mail hasn't been sent!",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void setupUiViews(){
        name=findViewById(R.id.name2);
        email=findViewById(R.id.email);
        pass=findViewById(R.id.pass2);
        register=findViewById(R.id.register2);
        alreadysigned=findViewById(R.id.alreadysigned);
    }

    private Boolean validate(){
        Boolean result=false;

        String writtename=name.getText().toString();
        String writtenpass=pass.getText().toString();
        String givenmail=email.getText().toString();

        if (writtename.isEmpty() || writtenpass.isEmpty() || givenmail.isEmpty()){
            Toast.makeText(getApplicationContext(),"Please complete all fields!",Toast.LENGTH_SHORT).show();
        }else{
            result=true;

        }
        return result;
    }
}
