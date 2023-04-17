package com.example.deum2;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class fragActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag);

        bottomNavigationView = findViewById(R.id.nav);
        NavigationBarView nav = bottomNavigationView;
        profile prof = new profile();
        homeFragment home = new homeFragment();
        lobbyFragment lobby = new lobbyFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container, prof).commit();
        nav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.person:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, prof).commit();
                    return true;

                case R.id.home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, home).commit();
                    return true;

                case R.id.battle:
                    getSupportFragmentManager().beginTransaction().replace(R.id.container, lobby).commit();
                    return true;
            }
            return false;
        });
    }
}