package com.example.firebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;

import com.example.firebase.databinding.ActivityDashboardBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Dashboard extends AppCompatActivity {

    private ActivityDashboardBinding dashboardBinding;
    private DatabaseReference databaseReference;
    private DatabaseListDataAdapter databaseListDataAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dashboardBinding= ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(dashboardBinding.getRoot());

        databaseReference= FirebaseDatabase.getInstance().getReference().child("student");
        FirebaseRecyclerOptions<StudentDataHolder> options =
                new FirebaseRecyclerOptions.Builder<StudentDataHolder>()
                        .setQuery(databaseReference, StudentDataHolder.class)
                        .build();
        dashboardBinding.firebaseDataLst.setLayoutManager(new LinearLayoutManager(this));
        databaseListDataAdapter = new DatabaseListDataAdapter(options);
        dashboardBinding.firebaseDataLst.setAdapter( databaseListDataAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        databaseListDataAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        databaseListDataAdapter.stopListening();
    }
}