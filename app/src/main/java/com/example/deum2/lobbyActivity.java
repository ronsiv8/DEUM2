package com.example.deum2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class lobbyActivity extends AppCompatActivity {
    BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        bottomNav = findViewById(R.id.nav);
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
}