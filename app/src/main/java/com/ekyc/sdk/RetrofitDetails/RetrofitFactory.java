package com.ekyc.sdk.RetrofitDetails;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitFactory {

    private static Retrofit retrofit=null;

    private RetrofitFactory()
    {

    }

    public static Retrofit getRetrofit()
    {

        Gson gson = new GsonBuilder().setLenient().create();

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        if (retrofit==null)
        {
            retrofit = new Retrofit
                    .Builder()
                    .client(okHttpClient)
                    .baseUrl("https://api.pay2all.in/outlet/")
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;

    }
}