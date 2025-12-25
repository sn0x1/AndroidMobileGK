package com.example.lab1_converter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Підключаємо наш макет меню
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Знаходимо кнопки
        Button btnLength = view.findViewById(R.id.btnLength);
        Button btnWeight = view.findViewById(R.id.btnWeight);
        Button btnTemperature = view.findViewById(R.id.btnTemperature);

        // Додаємо обробку натискань
        // Ми передаємо назву категорії (length, weight, temperature) у наступний фрагмент
        btnLength.setOnClickListener(v -> openConverter("length"));
        btnWeight.setOnClickListener(v -> openConverter("weight"));
        btnTemperature.setOnClickListener(v -> openConverter("temperature"));
    }

    // Метод для відкриття конвертера
    private void openConverter(String category) {
        ConverterFragment fragment = new ConverterFragment();

        // Передаємо дані (аргументи) у фрагмент
        Bundle args = new Bundle();
        args.putString("CATEGORY", category);
        fragment.setArguments(args);

        // Замінюємо поточний екран на екран конвертера
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null) // Щоб працювала кнопка "Назад" на телефоні
                .commit();
    }
}