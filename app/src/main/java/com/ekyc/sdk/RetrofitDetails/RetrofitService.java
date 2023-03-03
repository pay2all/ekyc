package com.ekyc.sdk.RetrofitDetails;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RetrofitService {

    @Headers("Content-Type: application/json")
    @FormUrlEncoded
    @POST("v1/outletapi")
//    void login(@Field("json_data") String json_data, Callback<DocumentSubmitResponse> callback);

    Call<DocumentSubmitResponse> isValid(@Field("json_data") String json_data);
}
