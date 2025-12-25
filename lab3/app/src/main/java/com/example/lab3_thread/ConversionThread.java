package com.example.lab3_thread;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConversionThread extends Thread {

    public Handler workerHandler; // Обробник повідомлень ЦЬОГО потоку
    private Context context;
    private JSONObject coefficientsData;

    // Константи для повідомлень
    public static final int MSG_CONVERT = 1;

    public ConversionThread(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        // 1. Ініціалізуємо Looper для цього потоку
        Looper.prepare();

        // 2. Завантажуємо дані з файлу (один раз при старті потоку)
        loadCoefficients();

        // 3. Створюємо Handler, який буде ловити повідомлення від Activity
        workerHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_CONVERT) {
                    // Отримуємо дані з повідомлення
                    DataBundle data = (DataBundle) msg.obj;

                    // Рахуємо
                    double result = calculate(data.value, data.from, data.to, data.category);

                    // Відправляємо результат назад в Activity (через її Handler)
                    if (data.replyTo != null) {
                        Message replyMsg = Message.obtain();
                        replyMsg.obj = result; // Кладемо результат
                        data.replyTo.sendMessage(replyMsg);
                    }
                }
            }
        };

        // 4. Запускаємо нескінченний цикл обробки повідомлень
        Looper.loop();
    }

    // Допоміжний клас для передачі даних
    public static class DataBundle {
        double value;
        String from, to, category;
        Handler replyTo; // Кому відповісти

        public DataBundle(double value, String from, String to, String category, Handler replyTo) {
            this.value = value;
            this.from = from;
            this.to = to;
            this.category = category;
            this.replyTo = replyTo;
        }
    }

    // --- Логіка розрахунків (така сама, як в Лаб 2) ---
    private void loadCoefficients() {
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open("coeffs.json")));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            coefficientsData = new JSONObject(sb.toString());
        } catch (Exception e) {
            coefficientsData = new JSONObject();
        }
    }

    private double calculate(double value, String fromUnit, String toUnit, String category) {
        if (category.equals("temperature")) {
            return convertTemperature(value, fromUnit, toUnit);
        }
        try {
            JSONObject catObj = coefficientsData.getJSONObject(category);
            JSONObject factors = catObj.getJSONObject("factors");
            double fromFactor = factors.getDouble(fromUnit);
            double toFactor = factors.getDouble(toUnit);
            return (value * fromFactor) / toFactor;
        } catch (Exception e) { return 0; }
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