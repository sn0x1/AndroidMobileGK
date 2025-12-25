package com.example.lab4_spacex.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.lab4_spacex.model.LaunchItem;
import java.util.List;

@Dao
public interface LaunchDao {
    @Query("SELECT * FROM launches ORDER BY launchDateUnix DESC")
    List<LaunchItem> getAllLaunches();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LaunchItem> launches);

    @Query("DELETE FROM launches")
    void deleteAll();
}