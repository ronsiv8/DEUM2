package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
    Button clickMe;
    long delay;

    @SuppressLint("MissingInflatedId")
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
        // new boolean to check if the game is over, set to false and call it gameover in firebase
        mDatabase.child("Games").child(code).child("GameOver").setValue(false);
        // add a listener to the gameover boolean, if it is true, start the findWinner method
        mDatabase.child("Games").child(code).child("GameOver").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue().equals(true)) {
                    mDatabase.child("Games").child(code).child("Score").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                findWinner(task);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    public void startClickReflex() {
        clickMe = findViewById(R.id.ReflexButton);
        // random amount of time for the button to be hidden until it appears
        float randomTime = (float) (Math.random() * 5000) + 1000;
        // handler to make the button be VISIBLE after that many ms
        clickMe.postDelayed(new Runnable() {
            @Override
            public void run() {
                clickMe.setVisibility(View.VISIBLE);
                delay = System.currentTimeMillis();
                String code = getIntent().getStringExtra("code");
                // count delay with ms
                clickMe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickMe.setVisibility(View.INVISIBLE);
                        delay = System.currentTimeMillis() - delay;
                        Log.e("path", "pathing to " +
                                "Games/" + code + "/Score/" + username + " with value " + delay);
                        mDatabase.child("Games").child(code).child("Score").child(getIntent().getStringExtra("username")).setValue(delay);
                        Pass();
                    }
                });
            }
        }, (long) randomTime);
    }

    public void Pass()
    {
        String code = getIntent().getStringExtra("code");
        // check if every score in Score is not 0
        mDatabase.child("Games").child(code).child("Score").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    // we need to check if every score is not 0
                    boolean allScores = true;
                    for (DataSnapshot score : task.getResult().getChildren()) {
                        if (score.getValue(Integer.class) == 0) {
                            allScores = false;
                        }
                    }
                    if (allScores) {
                        // we need to set the gameover boolean to true
                        mDatabase.child("Games").child(code).child("GameOver").setValue(true);
                    }
                }
            }
        });
        if (currentTurn == TurnOrderArray.size() - 1) {
            currentTurn = 0;
        } else {
            currentTurn++;
        }
        mDatabase.child("Games").child(code).child("CurrentTurn").setValue(currentTurn);
    }

    public void findWinner(Task<DataSnapshot> task) {
        // we need to decide a winner
        // we need to get the lowest score
        int lowestScore = 1000000000;
        String lowestScorePlayer = "";
        for (DataSnapshot score : task.getResult().getChildren()) {
            if (score.getValue(Integer.class) < lowestScore) {
                lowestScore = score.getValue(Integer.class);
                lowestScorePlayer = score.getKey();
            }
        }
        Log.e("Winner", lowestScorePlayer);
        Log.e("Username", getIntent().getStringExtra("username"));
        // if im the winner
        if (!getIntent().getStringExtra("username").equals(lowestScorePlayer)) {
            // add an L to my profile
            mDatabase.child("users").child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    Log.e("task", task.getResult().toString());
                    int losses = task.getResult().child("L").getValue(Integer.class);
                    mDatabase.child("users").child(mAuth.getUid()).child("L").setValue(losses + 1);
                }
            });
            // go to profileActivity
            Intent intent = new Intent(gameActivity.this, fragActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            finish();
            return;
        }
        Log.e("Winner", "You won!");
        // write the winner to the database user and add a W: users/userId/W += 1
        mDatabase.child("users").child(mAuth.getUid()).child("W").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                int wins = task.getResult().getValue(Integer.class);
                mDatabase.child("users").child(mAuth.getUid()).child("W").setValue(wins + 1);
            }
        });
        // go to profileActivity
        Intent intent = new Intent(gameActivity.this, fragActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
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
                                startClickReflex();
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