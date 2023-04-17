package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.os.Build;
import android.os.Bundle;
import android.service.autofill.Transformation;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;

public class gameActivity extends AppCompatActivity {

    public DatabaseReference mDatabase;
    public FirebaseAuth mAuth;
    List<String> TurnOrderArray;
    Map PlayerArray;
    int currentTurn;
    String username;
    Map userMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //first lets take the turn order from the game data and put it in the listView called turnOrderList
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        mAuth = FirebaseAuth.getInstance();
        //now we need to get the turn order from the game data
        String code = getIntent().getStringExtra("code");
        PlayerObserver playerObserver = new PlayerObserver();
        mDatabase.child("Games").child(code).child("Players").addValueEventListener(playerObserver);
        TurnOrderObserver turnOrderObserver = new TurnOrderObserver();
        mDatabase.child("Games").child(code).child("TurnOrder").addValueEventListener(turnOrderObserver);
        CurrentTurnObserver currentTurnObserver = new CurrentTurnObserver();
        mDatabase.child("Games").child(code).child("CurrentTurn").addValueEventListener(currentTurnObserver);
        Log.e("onCreate: ", String.valueOf(TurnOrderArray));
        Button PassTurnButton = findViewById(R.id.passButton);
        PassTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTurn == TurnOrderArray.size() - 1) {
                    currentTurn = 0;
                } else {
                    currentTurn++;
                }
                mDatabase.child("Games").child(code).child("CurrentTurn").setValue(currentTurn);
            }
        });

    }

    // we need an observer for the turn order
    class TurnOrderObserver implements com.google.firebase.database.ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            TurnOrderArray = (List<String>) snapshot.getValue();
        }


        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    // we need an observer for the current turn
    class CurrentTurnObserver implements com.google.firebase.database.ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            currentTurn = snapshot.getValue(Integer.class);
            Update();
        }

        public void Update() {
            // we need to check if this is the current player's turn
            String currentUserName = getIntent().getStringExtra("username");
            if (TurnOrderArray.get(currentTurn).equals(currentUserName)) {
                // we need to make an animation -
                TextView YourTurnView = findViewById(R.id.NotificationText);
                YourTurnView.setText("YOU'RE UP");
                YourTurnView.setVisibility(TextView.VISIBLE);
                // make the textview visible, make it grow for a second, than make it shrink for a seconed.
                YourTurnView.animate().scaleX(1.5f).scaleY(1.5f).setDuration(500).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        YourTurnView.animate().scaleX(1.2f).scaleY(1.2f).setDuration(400).setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                YourTurnView.setVisibility(TextView.INVISIBLE);
                                Button passButton = findViewById(R.id.passButton);
                                passButton.setVisibility(Button.VISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

            } else {
                Button passButton = findViewById(R.id.passButton);
                passButton.setVisibility(Button.INVISIBLE);
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }

    //we need an observer for the player list
    class PlayerObserver implements com.google.firebase.database.ValueEventListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            PlayerArray = (Map) snapshot.getValue();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    }
}