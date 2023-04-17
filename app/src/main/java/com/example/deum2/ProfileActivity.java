package com.example.deum2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

//import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
//import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ProfileActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;
    public DataSnapshot userMap;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    userMap = task.getResult();
                    showUsername();

                }
            }
        });
        bottomNav = findViewById(R.id.nav);
        Menu menuNav = bottomNav.getMenu();
        menuNav.getItem(2).setChecked(true);
        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getTitle().toString()) {
                case "Home":
                    goToHome(null);
                    break;
                case "Battle!":
                    goToLobby(null);
                    break;
                case "Profile":
                    gotToProfile(null);
            }
            return true;
        });
    }



    @SuppressLint("SetTextI18n")
    public void showUsername() {
        /*
        Assuming userMap is the usermap...
         */
        TextView userNameDisplay = findViewById(R.id.playerName);
        TextView winsLosses = findViewById(R.id.WinsLosses);
        userNameDisplay.setText((String)Objects.requireNonNull(userMap.child("Username").getValue()));
        winsLosses.setText("Wins: " + Objects.requireNonNull(userMap.child("W").getValue()) + "\n Losses: " +
                Objects.requireNonNull(userMap.child("L").getValue()));
    }

    public void goToHome(View view) {
        Intent intent = new Intent(this, homeActivity.class);
        startActivity(intent);
    }
    public void goToLobby(View view) {
        Intent intent = new Intent(this, lobbyActivity.class);
        startActivity(intent);
    }
    public void gotToProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    public void resolveMenu(@Nullable MenuItem item) {
        if (item == null) return;
        Toast.makeText(this,  item.getTitle(), Toast.LENGTH_SHORT).show();
    }


}