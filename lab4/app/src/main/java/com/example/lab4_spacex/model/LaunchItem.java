package com.example.lab4_spacex.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.squareup.moshi.Json;

@Entity(tableName = "launches")
public class LaunchItem {

    @PrimaryKey
    @Json(name = "flight_number")
    public int flightNumber;

    @Json(name = "mission_name")
    public String missionName;

    @Json(name = "launch_date_unix")
    public long launchDateUnix;

    @Json(name = "details")
    public String details;

    // В API це вкладений об'єкт links.mission_patch_small
    // Для простоти в цій лабі ми будемо вручну діставати це поле
    // або зберігати його окремо, якщо робити правильний маппінг.
    // Але щоб не ускладнювати Room TypeConverters, зробимо поле String:
    public String patchUrl;

    // Конструктор
    public LaunchItem(int flightNumber, String missionName, long launchDateUnix, String details, String patchUrl) {
        this.flightNumber = flightNumber;
        this.missionName = missionName;
        this.launchDateUnix = launchDateUnix;
        this.details = details;
        this.patchUrl = patchUrl;
    }
}