package com.v2v.eduguard;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("predict")
    Call<RiskResponse> getRisk(@Body StudentData data);
}
