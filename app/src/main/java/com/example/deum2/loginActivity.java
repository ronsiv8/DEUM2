package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class loginActivity extends AppCompatActivity {
    private static final String TAG = "L";
    public DatabaseReference mDatabase;
    public FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        EditText email = findViewById(R.id.emailText);
        EditText password = findViewById(R.id.passwordText);
        mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        TextView reactionText = findViewById(R.id.AlertTextLogin);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "loggedIn:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            reactionText.setText("200 OK");
                            profileGo();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            reactionText.setText(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    public void Register(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void profileGo() {
        Intent intent = new Intent(this, fragActivity.class);
        startActivity(intent);
        finish();
    }

    public void ForgotPassword(View view) {
        EditText email = findViewById(R.id.emailText);
        mAuth.sendPasswordResetEmail(email.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                TextView reactionText = findViewById(R.id.AlertTextLogin);
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "reset:success");
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "reset:failure", task.getException());
                }
            }
        });
        TextView reactionText = findViewById(R.id.AlertTextLogin);
        reactionText.setText("If that email exists, we sent an email to it to reset your password!");
    }
}