package com.example.lab2_service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button; // Не забудьте цей імпорт
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConverterActivity extends AppCompatActivity {

    private ConversionService mService;
    private boolean mBound = false;
    private String category;

    private EditText etValue;
    private Spinner spinnerFrom, spinnerTo;
    private TextView tvResult, tvTitle;
    private Button btnBack; // Змінна для кнопки

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        category = getIntent().getStringExtra("CATEGORY");

        // Ініціалізація View
        tvTitle = findViewById(R.id.tvTitle);
        etValue = findViewById(R.id.etValue);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        tvResult = findViewById(R.id.tvResult);
        btnBack = findViewById(R.id.btnBack); // Знаходимо кнопку

        setupUI();

        // Обробка натискання кнопки "Назад"
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ConversionService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ConversionService.LocalBinder binder = (ConversionService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            calculate();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void setupUI() {
        int arrayId;
        String titleText;

        switch (category) {
            case "weight":
                arrayId = R.array.units_weight;
                titleText = "Вага";
                break;
            case "temperature":
                arrayId = R.array.units_temperature;
                titleText = "Температура";
                break;
            default:
                arrayId = R.array.units_length;
                titleText = "Довжина";
                break;
        }
        tvTitle.setText(titleText);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { calculate(); }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etValue.addTextChangedListener(textWatcher);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { calculate(); }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerFrom.setOnItemSelectedListener(spinnerListener);
        spinnerTo.setOnItemSelectedListener(spinnerListener);
    }

    private void calculate() {
        if (!mBound || etValue.getText().toString().isEmpty()) {
            tvResult.setText("0");
            return;
        }
        try {
            double value = Double.parseDouble(etValue.getText().toString());
            String from = spinnerFrom.getSelectedItem().toString();
            String to = spinnerTo.getSelectedItem().toString();

            double result = mService.convert(value, from, to, category);
            tvResult.setText(String.format("%.4f", result));
        } catch (Exception e) {
            tvResult.setText("Err");
        }
    }
}