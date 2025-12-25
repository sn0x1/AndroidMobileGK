package com.example.lab3_thread;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ConverterActivity extends AppCompatActivity {

    private ConversionThread conversionThread;
    private Handler uiHandler; // Handler Головного потоку (для оновлення UI)

    private String category;
    private EditText etValue;
    private Spinner spinnerFrom, spinnerTo;
    private TextView tvResult, tvTitle;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        category = getIntent().getStringExtra("CATEGORY");

        // 1. Створюємо Handler для UI (приймає відповіді від потоку)
        uiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                // Отримали результат з потоку
                Double result = (Double) msg.obj;
                tvResult.setText(String.format("%.4f", result));
            }
        };

        // 2. Створюємо і запускаємо робочий потік
        conversionThread = new ConversionThread(this);
        conversionThread.start();

        initViews();
        setupUI();

        // Кнопка Назад
        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Зупиняємо потік, коли Activity закривається
        if (conversionThread.workerHandler != null) {
            conversionThread.workerHandler.getLooper().quit();
        }
    }

    private void calculate() {
        // Якщо потік ще не готовий або поле пусте
        if (conversionThread.workerHandler == null || etValue.getText().toString().isEmpty()) {
            tvResult.setText("0");
            return;
        }

        try {
            double value = Double.parseDouble(etValue.getText().toString());
            String from = spinnerFrom.getSelectedItem().toString();
            String to = spinnerTo.getSelectedItem().toString();

            // Формуємо пакет даних
            ConversionThread.DataBundle data = new ConversionThread.DataBundle(
                    value, from, to, category, uiHandler
            );

            // Створюємо повідомлення і відправляємо в потік
            Message msg = Message.obtain();
            msg.what = ConversionThread.MSG_CONVERT;
            msg.obj = data;

            conversionThread.workerHandler.sendMessage(msg);

        } catch (NumberFormatException e) {
            tvResult.setText("Err");
        }
    }

    // --- Стандартний код ініціалізації UI (без змін) ---
    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        etValue = findViewById(R.id.etValue);
        spinnerFrom = findViewById(R.id.spinnerFrom);
        spinnerTo = findViewById(R.id.spinnerTo);
        tvResult = findViewById(R.id.tvResult);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupUI() {
        int arrayId;
        String titleText;
        switch (category) {
            case "weight": arrayId = R.array.units_weight; titleText = "Вага"; break;
            case "temperature": arrayId = R.array.units_temperature; titleText = "Температура"; break;
            default: arrayId = R.array.units_length; titleText = "Довжина"; break;
        }
        tvTitle.setText(titleText);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { calculate(); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etValue.addTextChangedListener(textWatcher);

        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { calculate(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerFrom.setOnItemSelectedListener(spinnerListener);
        spinnerTo.setOnItemSelectedListener(spinnerListener);
    }
}