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

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmailText,loginPassText;
    private Button loginBtn,loginRegBtn;
    private FirebaseAuth mAuth ;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        loginEmailText =  findViewById(R.id.email);
        loginPassText = findViewById(R.id.password);
        loginBtn =  findViewById(R.id.login);
        loginRegBtn =  findViewById(R.id.login_signin);
        loginProgress = findViewById(R.id.loginProgressBar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String loginEmail = loginEmailText.getText().toString();
                String loginPass = loginPassText.getText().toString();
                if(!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)){
                    loginProgress.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(loginEmail,loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToMain();
                            }   else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Error:"+ errorMessage, Toast.LENGTH_SHORT).show();
                            }
                            loginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                }

            }
        });


        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToRegister();

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent I = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(I);
        finish();
    }

    private void sendToRegister() {
        Intent I = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(I);
        finish();
        Toast.makeText(LoginActivity.this, "Hello there", Toast.LENGTH_SHORT).show();
    }



}