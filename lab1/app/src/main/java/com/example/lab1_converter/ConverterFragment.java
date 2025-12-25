package com.example.lab1_converter;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ConverterFragment extends Fragment {

    private String category; // "length", "weight" або "temperature"

    // Елементи інтерфейсу
    private EditText etValue;
    private Spinner spinnerFrom, spinnerTo;
    private TextView tvResult, tvCategoryTitle;
    private Button btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_converter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ініціалізація View (знаходимо елементи за ID)
        etValue = view.findViewById(R.id.etValue);
        spinnerFrom = view.findViewById(R.id.spinnerFrom);
        spinnerTo = view.findViewById(R.id.spinnerTo);
        tvResult = view.findViewById(R.id.tvResult);
        tvCategoryTitle = view.findViewById(R.id.tvCategoryTitle);
        btnBack = view.findViewById(R.id.btnBack);

        // 2. Отримуємо категорію, яку передали з меню
        if (getArguments() != null) {
            category = getArguments().getString("CATEGORY");
        }

        // 3. Налаштовуємо списки (Spinner) залежно від категорії
        setupCategory();

        // 4. Слухач зміни тексту (для миттєвого перерахунку)
        etValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculate();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 5. Слухачі зміни вибору у випадаючих списках
        AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                calculate();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };
        spinnerFrom.setOnItemSelectedListener(spinnerListener);
        spinnerTo.setOnItemSelectedListener(spinnerListener);

        // 6. Кнопка "Назад" - повертає до меню
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    // Налаштування заголовка та вмісту списків
    private void setupCategory() {
        int arrayId; // ID масиву рядків (із strings.xml)
        String title;

        switch (category) {
            case "weight":
                arrayId = R.array.units_weight;
                title = "Вага";
                break;
            case "temperature":
                arrayId = R.array.units_temperature;
                title = "Температура";
                break;
            case "length":
            default:
                arrayId = R.array.units_length;
                title = "Довжина";
                break;
        }

        tvCategoryTitle.setText(title);

        // Створюємо адаптер для спінерів
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                arrayId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFrom.setAdapter(adapter);
        spinnerTo.setAdapter(adapter);
    }

    // Основна логіка розрахунків
    private void calculate() {
        String inputStr = etValue.getText().toString();
        // Якщо поле порожнє, показуємо 0
        if (inputStr.isEmpty()) {
            tvResult.setText("0");
            return;
        }

        // Перетворюємо текст у число
        double input = Double.parseDouble(inputStr);
        String unitFrom = spinnerFrom.getSelectedItem().toString();
        String unitTo = spinnerTo.getSelectedItem().toString();

        double result = 0;

        if (category.equals("temperature")) {
            result = convertTemperature(input, unitFrom, unitTo);
        } else {
            // Для довжини та ваги використовуємо метод базової одиниці
            double baseValue = toBase(input, unitFrom, category); // Перевід у базову (метр/кг)
            result = fromBase(baseValue, unitTo, category);       // Перевід з базової у цільову
        }

        // Вивід результату (до 4 знаків після коми)
        tvResult.setText(String.format("%.4f", result));
    }

    // --- Логіка конвертації (Варіант 9) ---

    // Конвертація температури (особлива формула)
    private double convertTemperature(double val, String from, String to) {
        // Спочатку переводимо все в Цельсій
        double inCelsius = val;

        if (from.equals("Кельвін")) inCelsius = val - 273.15;
        else if (from.equals("Фаренгейт")) inCelsius = (val - 32) * 5/9;

        // З Цельсія у цільову одиницю
        if (to.equals("Кельвін")) return inCelsius + 273.15;
        if (to.equals("Фаренгейт")) return (inCelsius * 9/5) + 32;

        return inCelsius; // Якщо вибрано Цельсій
    }

    // Перевід У базову одиницю (Метр або Кілограм)
    private double toBase(double val, String unit, String cat) {
        if (cat.equals("length")) {
            // База: МЕТР
            switch (unit) {
                case "Сантиметр": return val * 0.01;
                case "Кілометр": return val * 1000.0;
                case "Дюйм": return val * 0.0254;
                case "Миля": return val * 1609.34;
                case "Ярд": return val * 0.9144;
                case "Фут": return val * 0.3048;
                default: return val; // Метр
            }
        } else { // weight
            // База: КІЛОГРАМ
            switch (unit) {
                case "Грам": return val * 0.001;
                case "Тонна": return val * 1000.0;
                case "Карат": return val * 0.0002;
                case "Фунт": return val * 0.453592;
                case "Пуд": return val * 16.3807;
                default: return val; // Кілограм
            }
        }
    }

    // Перевід З базової одиниці у цільову
    private double fromBase(double val, String unit, String cat) {
        if (cat.equals("length")) {
            // База: МЕТР
            switch (unit) {
                case "Сантиметр": return val / 0.01;
                case "Кілометр": return val / 1000.0;
                case "Дюйм": return val / 0.0254;
                case "Миля": return val / 1609.34;
                case "Ярд": return val / 0.9144;
                case "Фут": return val / 0.3048;
                default: return val; // Метр
            }
        } else { // weight
            // База: КІЛОГРАМ
            switch (unit) {
                case "Грам": return val / 0.001;
                case "Тонна": return val / 1000.0;
                case "Карат": return val / 0.0002;
                case "Фунт": return val / 0.453592;
                case "Пуд": return val / 16.3807;
                default: return val; // Кілограм
            }
        }
    }
}