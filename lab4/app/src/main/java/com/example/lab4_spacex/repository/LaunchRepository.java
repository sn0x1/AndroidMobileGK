package com.example.lab4_spacex.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Додано логування
import com.example.lab4_spacex.database.AppDatabase;
import com.example.lab4_spacex.model.LaunchItem;
import com.example.lab4_spacex.model.NetworkLaunch;
import com.example.lab4_spacex.network.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaunchRepository {

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DataCallback {
        void onSuccess(List<LaunchItem> data);
        void onError(String error);
    }

    public LaunchRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    public void getLaunches(DataCallback callback) {
        RetrofitClient.getApi().getPastLaunches().enqueue(new Callback<List<NetworkLaunch>>() {
            @Override
            public void onResponse(Call<List<NetworkLaunch>> call, Response<List<NetworkLaunch>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<LaunchItem> items = new ArrayList<>();

                    // Обчислюємо час.
                    // ЗМІНА: Беремо 15 років замість 5, бо API v3 старе і в 2025 році покаже 0 результатів за 5 років.
                    long currentTimeSeconds = System.currentTimeMillis() / 1000;
                    long yearsInSeconds = 15L * 365 * 24 * 60 * 60;
                    long cutoffTime = currentTimeSeconds - yearsInSeconds;

                    Log.d("SpaceX_Debug", "Cutoff time (Unix): " + cutoffTime);

                    for (NetworkLaunch netItem : response.body()) {
                        // Лог для перевірки дат, які приходять
                        Log.d("SpaceX_Debug", "Item Date: " + netItem.launchDateUnix + " (" + netItem.missionName + ")");

                        // Якщо хочете побачити ВСІ запуски, просто закоментуйте умову if
                        if (netItem.launchDateUnix >= cutoffTime) {
                            String patch = (netItem.links != null) ? netItem.links.missionPatchSmall : null;
                            items.add(new LaunchItem(
                                    netItem.flightNumber,
                                    netItem.missionName,
                                    netItem.launchDateUnix,
                                    netItem.details,
                                    patch
                            ));
                        }
                    }

                    Log.d("SpaceX_Debug", "Items after filter: " + items.size());

                    executor.execute(() -> {
                        db.launchDao().deleteAll();
                        db.launchDao().insertAll(items);
                        mainHandler.post(() -> callback.onSuccess(items));
                    });
                } else {
                    loadFromDb(callback);
                }
            }

            @Override
            public void onFailure(Call<List<NetworkLaunch>> call, Throwable t) {
                Log.e("SpaceX_Error", "Network error", t); // Логування помилки
                loadFromDb(callback);
            }
        });
    }

    private void loadFromDb(DataCallback callback) {
        executor.execute(() -> {
            List<LaunchItem> localData = db.launchDao().getAllLaunches();
            mainHandler.post(() -> {
                if (localData.isEmpty()) {
                    callback.onError("База порожня. Перевірте інтернет або збільште діапазон років.");
                } else {
                    callback.onSuccess(localData);
                }
            });
        });
    }
}