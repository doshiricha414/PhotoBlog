package com.example.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class RegisterActivity extends AppCompatActivity {

    private EditText emailField,RegPassField,RegConfirmPassField;
    private Button reg_btn,reg_login_btn;
    private ProgressBar reg_progress;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emailField =  findViewById(R.id.reg_email);
        RegPassField =  findViewById(R.id.reg_password);
        RegConfirmPassField =  findViewById(R.id.confirm);
        reg_btn =  findViewById(R.id.reg_create);
        reg_login_btn = findViewById(R.id.reg_login);
        reg_progress =  findViewById(R.id.regProgressBar);
        mAuth = FirebaseAuth.getInstance();

        reg_login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                String pass = RegPassField.getText().toString();
                String confirmPass = RegConfirmPassField.getText().toString();
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){
                    if(pass.equals(confirmPass)){
                        reg_progress.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                        sendToMain();
                                }   else{
                                        String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this, "Error:"+errorMessage, Toast.LENGTH_SHORT).show();
                                }
                                reg_progress.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Password fields doesn't match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
           // sendToMain();
        }
    }
    private void sendToMain() {
        Intent I = new Intent(RegisterActivity.this,MainActivity.class);
        startActivity(I);
        finish();
    }
}
