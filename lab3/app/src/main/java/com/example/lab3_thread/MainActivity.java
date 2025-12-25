package com.example.lab3_thread;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLength = findViewById(R.id.btnLength);
        Button btnWeight = findViewById(R.id.btnWeight);
        Button btnTemperature = findViewById(R.id.btnTemperature);

        btnLength.setOnClickListener(v -> openConverter("length"));
        btnWeight.setOnClickListener(v -> openConverter("weight"));
        btnTemperature.setOnClickListener(v -> openConverter("temperature"));
    }

    private void openConverter(String category) {
        Intent intent = new Intent(this, ConverterActivity.class);
        intent.putExtra("CATEGORY", category);
        startActivity(intent);
    }
}