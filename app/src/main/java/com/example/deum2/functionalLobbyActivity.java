package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class functionalLobbyActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;
    public DataSnapshot userMap;
    private String[] MUSIC = new String[]{"all_or_nothing", "cant_stop", "night_after_night", "slow_roller",
            "we_hustlin", "where_the_demons_hide"};
    private double HoldTime;
    private int NextHoldTime;
    public String currentMusic;
    public Button startGame;
    public MediaPlayer sentMediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functional_lobby);
        Intent intent = getIntent();
        TextView codeText= findViewById(R.id.code);
        // unload the code from the intent
        String code = intent.getStringExtra("code");
        codeText.setText("Code: " + code);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        startGame = findViewById(R.id.startGameButton);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // listen to changes in Music and update currentMusic
        mDatabase.child("Games").child(code).child("Music").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentMusic = snapshot.getValue().toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        // listen to the state of the game from the database
        mDatabase.child("Games").child(code).child("State").addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if the game is in the lobby state, then the start game button should be visible
                if (snapshot.getValue().toString().equals("pick")) {
                    findHoldTimes();
                    // play the specified music (name should be in the variable currentMusic)
                    // play the music
                    startService(new Intent(getApplicationContext(), SoundService.class).putExtra("music", currentMusic));
                    // start a timer for HoldTime seconds and move on to ChampSelectActivity

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        // onclick of the start game button
        startGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // generate a random song name from the MUSIC list and put it as the value in Music
                int random = (int) (Math.random() * MUSIC.length);
                mDatabase.child("Games").child(code).child("Music").setValue(MUSIC[random]);
                // set the state of the game to starting
                mDatabase.child("Games").child(code).child("State").setValue("pick");
                initTurnOrder();
                mDatabase.child("Games").child(code).child("CurrentTurn").setValue(0);
            }
        });
        // get all the players from firebase using the game code (database location: Games, game code, players)
        // display the players in the list
        // create a listener to mDatabase.child("Games").child(code).child("players") that updates the list when a player joins
        ListView playerList = findViewById(R.id.playerList);
        mDatabase.child("Games").child(code).child("Players").addValueEventListener(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userMap = snapshot;
                        HashMap users = (HashMap) snapshot.getValue();
                        assert users != null;
                        String[] players = new String[users.size()];
                        int i = 0;
                        for (Object user : users.values()) {
                            players[i] = (String) user;
                            i++;
                        }
                        playerList.setAdapter(new ArrayAdapter<>(functionalLobbyActivity.this, android.R.layout.simple_list_item_1, players));
                        Log.e("hello", "onDataChange: " + users.values());
                        HostButtonReveal();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("L", "Failed to read value.", error.toException());
                    }
                }
        );
    }

    private void findHoldTimes() {
        // find the hold times for the song
        // set the hold times to the variables HoldTime and NextHoldTime
        // find the musicName from the database
                switch (currentMusic) {
                    case "all_or_nothing":
                        HoldTime = 2.95;
                        NextHoldTime = 3;
                        break;
                    case "cant_stop":
                        HoldTime = 15;
                        NextHoldTime = 6;
                        break;
                    case "night_after_night":
                        HoldTime = 6.7;
                        NextHoldTime = 7;
                        break;
                    case "slow_roller":
                        HoldTime = 9.7;
                        NextHoldTime = 4;
                        break;
                    case "we_hustlin":
                        HoldTime = 6.9;//nice
                        NextHoldTime = 4;
                        break;
                    case "where_the_demons_hide":
                        HoldTime = 6.8;
                        NextHoldTime = 6;
                        break;
                }
        new android.os.Handler().postDelayed(
                (Runnable) new Runnable() {
                    public void run() {
                        Log.e("hello", "run: " + HoldTime);
                        Intent intent = new Intent(getApplicationContext(), ChampSelectActivity.class);
                        intent.putExtra("code", getIntent().getStringExtra("code"));
                        intent.putExtra("music", currentMusic);
                        intent.putExtra("holdTime", NextHoldTime);
                        intent.putExtra("startTime", HoldTime);
                        startActivity(intent);
                        finish();
                    }
                },
                (long)HoldTime * 1000);
    }
    private void initTurnOrder() {
        // get the list of players from the database
        // sort the list of players
        // put the sorted list of players in the database
        String code = getIntent().getStringExtra("code");
        mDatabase.child("Games").child(code).child("Players").get().addOnCompleteListener(
                new OnCompleteListener<DataSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot snapshot = task.getResult();
                            assert snapshot != null;
                            HashMap users = (HashMap) snapshot.getValue();
                            assert users != null;
                            String[] players = new String[users.size()];
                            int i = 0;
                            for (Object user : users.values()) {
                                players[i] = (String) user;
                                i++;
                            }
                            //randomize the order of the players
                            Collections.shuffle(Arrays.asList(players));
                            // put the players in the database
                            mDatabase.child("Games").child(code).child("TurnOrder").setValue(Arrays.asList(players));
                            // put the players in the database in Score, which is Username: 0 (score 0 for each player)
                            for (String player : players) {
                                mDatabase.child("Games").child(code).child("Score").child(player).setValue(0);
                            }
                        }
                    }
                }

        );
    }
    public void HostButtonReveal() {
        if (userMap.getChildrenCount() > 1 && getIntent().getBooleanExtra("isHost", false)) {
            startGame.setVisibility(Button.VISIBLE);
        }
        else {
            startGame.setVisibility(Button.INVISIBLE);
        }
    }
}