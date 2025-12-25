package com.example.lab2_service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.IBinder;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConversionService extends Service {

    private final IBinder binder = new LocalBinder();
    private JSONObject coefficientsData;

    // Клас для зв'язку (Має бути public)
    public class LocalBinder extends Binder {
        public ConversionService getService() {
            return ConversionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadCoefficients();
    }

    private void loadCoefficients() {
        try {
            AssetManager assetManager = getAssets();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(assetManager.open("coeffs.json")));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            coefficientsData = new JSONObject(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
            coefficientsData = new JSONObject();
        }
    }

    // Публічний метод для конвертації
    public double convert(double value, String fromUnit, String toUnit, String category) {
        if (category.equals("temperature")) {
            return convertTemperature(value, fromUnit, toUnit);
        }

        try {
            JSONObject categoryObj = coefficientsData.getJSONObject(category);
            JSONObject factors = categoryObj.getJSONObject("factors");

            double fromFactor = factors.getDouble(fromUnit);
            double toFactor = factors.getDouble(toUnit);

            return (value * fromFactor) / toFactor;

        } catch (Exception e) {
            return 0;
        }
    }

    private double convertTemperature(double val, String from, String to) {
        double inCelsius = val;

        if (from.equals("Кельвін")) inCelsius = val - 273.15;
        else if (from.equals("Фаренгейт")) inCelsius = (val - 32) * 5.0/9.0;

        if (to.equals("Кельвін")) return inCelsius + 273.15;
        if (to.equals("Фаренгейт")) return (inCelsius * 9.0/5.0) + 32;

        return inCelsius;
    }
}