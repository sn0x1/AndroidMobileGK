package com.example.lab4_spacex.model;

import com.squareup.moshi.Json;

public class NetworkLaunch {
    @Json(name = "flight_number")
    public int flightNumber;

    @Json(name = "mission_name")
    public String missionName;

    @Json(name = "launch_date_unix")
    public long launchDateUnix;

    @Json(name = "details")
    public String details;

    @Json(name = "links")
    public Links links;

    public static class Links {
        @Json(name = "mission_patch_small")
        public String missionPatchSmall;
    }
}