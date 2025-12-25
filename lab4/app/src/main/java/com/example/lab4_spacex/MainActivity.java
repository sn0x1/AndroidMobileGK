package com.example.lab4_spacex;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab4_spacex.model.LaunchItem;
import com.example.lab4_spacex.repository.LaunchRepository;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LaunchAdapter adapter;
    private LaunchRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView); // Переконайся, що в XML є цей ID
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LaunchAdapter(item -> {
            // Клік на елемент - перехід до деталей
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("mission_name", item.missionName);
            intent.putExtra("details", item.details);
            intent.putExtra("patch_url", item.patchUrl);
            intent.putExtra("flight_number", item.flightNumber);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        repository = new LaunchRepository(this);
        loadData();
    }

    private void loadData() {
        repository.getLaunches(new LaunchRepository.DataCallback() {
            @Override
            public void onSuccess(List<LaunchItem> data) {
                adapter.setLaunches(data);
                Toast.makeText(MainActivity.this, "Дані оновлено: " + data.size(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}