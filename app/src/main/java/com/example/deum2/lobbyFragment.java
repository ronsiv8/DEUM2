package com.example.deum2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link lobbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class lobbyFragment extends Fragment {
    public FirebaseAuth mAuth;
    public DatabaseReference mDatabase;
    public DataSnapshot userMap;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public lobbyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment lobbyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static lobbyFragment newInstance(String param1, String param2) {
        lobbyFragment fragment = new lobbyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Button createLobby = getView().findViewById(R.id.createButton);

        createLobby.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String generated = getAlphaNumericString(4);
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                assert currentUser != null;
                mDatabase.child("Games").child(generated).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot resultMap = task.getResult();
                            if (resultMap.getValue() == null) {
                                // generate game data
                                mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        }
                                        else {
                                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                            userMap = task.getResult();
                                            generateGame(generated);
                                        }
                                    }
                                });
                            }
                            else {
                                Log.e("Bad", resultMap.child("value").toString());
                            }
                        }
                    }
                });
            }
        });

        Button joinLobby = getView().findViewById(R.id.JoinButton);
        EditText lobbyCode = getView().findViewById(R.id.lobbyCode);
        joinLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance("https://deum-4666f-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                assert currentUser != null;
                mDatabase.child("Games").child(lobbyCode.getText().toString()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            DataSnapshot resultMap = task.getResult();
                            if (resultMap.getValue() == null) {
                                Log.e("Bad", resultMap.child("value").toString());
                            }
                            else {
                                mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        }
                                        else {
                                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                            userMap = task.getResult();
                                            JoinGame(lobbyCode.getText().toString());
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    public void generateGame(String generated) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mDatabase.child("Games").child(generated).child("Players").child(currentUser.getUid()).setValue(userMap.child("Username").getValue());
        mDatabase.child("Games").child(generated).child("State").setValue("Waiting");
        mDatabase.child("Games").child(generated).child("Timer").setValue(10);
        mDatabase.child("Games").child(generated).child("Host").setValue(currentUser.getUid());
        mDatabase.child("Games").child(generated).child("Music").setValue("Not Set");
        Intent intent = new Intent(getActivity(), functionalLobbyActivity.class);
        intent.putExtra("code", generated);
        intent.putExtra("isHost", true);
        startActivity(intent);
        getActivity().finish();
    }

    public void JoinGame(String lobbyName) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        mDatabase.child("Games").child(lobbyName).child("Players").child(currentUser.getUid()).setValue(userMap.child("Username").getValue());
        Intent intent = new Intent(getActivity(), functionalLobbyActivity.class);
        intent.putExtra("code", lobbyName);
        intent.putExtra("isHost", false);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lobby, container, false);
    }
    static String getAlphaNumericString(int n)
    {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}