package com.example.lab4_spacex.network;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.spacexdata.com/v3/";
    private static Retrofit retrofit = null;

    public static SpaceXApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }
        return retrofit.create(SpaceXApi.class);
    }
}