package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuffColorFilter;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "D";
    public DatabaseReference mDatabase;
    public FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void signUp(View view) {
        EditText email = findViewById(R.id.email);
        EditText password = findViewById(R.id.PasswordTextField);
        EditText username = findViewById(R.id.username);
        createNewUser(email.getText().toString(), password.getText().toString(), username.getText().toString());
    }

    public void createNewUser(String email, String password, String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        TextView reactionText = findViewById(R.id.AlertText);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            reactionText.setText("200 OK");
                            mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                            assert user != null;
                            mDatabase.child("users").child(user.getUid()).child("W").setValue(0);
                            mDatabase.child("users").child(user.getUid()).child("L").setValue(0);
                            mDatabase.child("users").child(user.getUid()).child("Username").setValue(username);
                            goToProfile();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            reactionText.setText(Objects.requireNonNull(task.getException()).getMessage());
                        }
                    }
                });
    }

    public void goToProfile() {
        Intent moveToProfile = new Intent(this, profile.class);
        startActivity(moveToProfile);
        finish();
    }

    public void goToLogin(View view) {
        Intent moveToProfile = new Intent(this, loginActivity.class);
        startActivity(moveToProfile);
    }
}