package com.example.lab4_spacex.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.lab4_spacex.model.LaunchItem;

@Database(entities = {LaunchItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LaunchDao launchDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "spacex_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}