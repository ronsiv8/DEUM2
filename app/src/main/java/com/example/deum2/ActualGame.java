package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActualGame extends AppCompatActivity {
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_game);
        // get the intent isHost
        Boolean isHost = getIntent().getBooleanExtra("isHost", false);
        // we need to change the game structure as host
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        if (isHost) {
            // we need to make a turn order from the list of players in Players
            // get the game code from the intent
            String code = getIntent().getStringExtra("code");
            // change the game structure to host
            mDatabase.child("Games").child(code).child("Players").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        Log.d("firebase", String.valueOf(task.getResult().getValue()));

                    }
                }
            });
        }
    }
}