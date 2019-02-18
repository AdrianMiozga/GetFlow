package com.wentura.pomodoroapp;

import android.os.Bundle;

import com.wentura.pomodoroapp.database.Database;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class StatisticsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Database database = Database.getInstance(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        List<StatisticsItem> statisticsItems = database.pomodoroDao().getAll(7);

        StatisticsAdapter statisticsAdapter = new StatisticsAdapter(statisticsItems);

        recyclerView.setAdapter(statisticsAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
