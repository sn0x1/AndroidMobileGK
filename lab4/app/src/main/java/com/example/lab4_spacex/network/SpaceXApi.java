package com.example.lab4_spacex.network;

import com.example.lab4_spacex.model.NetworkLaunch;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SpaceXApi {
    @GET("launches/past")
    Call<List<NetworkLaunch>> getPastLaunches();
}