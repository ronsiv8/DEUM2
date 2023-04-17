package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Map;

public class ChampSelectActivity extends AppCompatActivity {
    public DatabaseReference mDatabase;
    public FirebaseAuth mAuth;
    private MediaPlayer player;
    private Map userMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_champ_select);
        // find the relative top of the screen
        // get the ChampSelectTextView, and animate it to grow bigger for (get the time from the intent)
        // then make it disappear
        TextView ChampSelectTextView = findViewById(R.id.ChampSelectTextView);
        ValueAnimator animator = ValueAnimator.ofFloat(1f, 50f);
        animator.setDuration(getIntent().getIntExtra("holdTime", 1) * 1000L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedValue = (float) valueAnimator.getAnimatedValue();
                ChampSelectTextView.setTextSize(animatedValue);
            }

        });
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // listen for the state of the game
        mDatabase.child("Games").child(getIntent().getStringExtra("code")).child("State").addValueEventListener(
                new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.e("ChampSelectActivity", "onDataChange: " + snapshot.getValue());
                        if (snapshot.getValue().equals("playing")) {
                            stopService(new Intent(ChampSelectActivity.this, SoundService.class));
                            Intent intent = new Intent(ChampSelectActivity.this, gameActivity.class);
                            intent.putExtra("code", getIntent().getStringExtra("code"));
                            mDatabase.child("users").child(mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase", "Error getting data", task.getException());
                                    } else {
                                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                        intent.putExtra("username", task.getResult().child("Username").getValue().toString());
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
        animator.addListener(new ValueAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ChampSelectTextView.setVisibility(View.INVISIBLE);
                // todo make the whole thing
                mDatabase.child("Games").child(getIntent().getStringExtra("code")).child("State").setValue("playing");
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        animator.start();
    }
}