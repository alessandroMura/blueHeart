package com.example.blueheart;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import static com.example.blueheart.utilities.*;

public class forgotPassword extends AppCompatActivity {

    private EditText email;
    private Button reset;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email=findViewById(R.id.emailforgot);
        reset=findViewById(R.id.resetpass);
        firebaseAuth=FirebaseAuth.getInstance();

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usermail=email.getText().toString().trim();

                if (usermail.equals("")){
                    showToast(getApplicationContext(),"Please enter oyur email ID");

                }else{
                    firebaseAuth.sendPasswordResetEmail(usermail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                showToast(getApplicationContext(),"Password Reset Email sent!");
                                finish();
                                startActivity(new Intent(getApplicationContext(),recordAndSendLogin.class));
                            }else{
                                showToast(getApplicationContext(),"Error in sending Password Reset Email");
                            }
                        }
                    });
                }
            }
        });
    }
}
