package com.ekyc.sdk.RetrofitDetails;

import com.google.gson.annotations.SerializedName;

public class DocumentSubmitResponse {

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @SerializedName("status_id")
    String status_id;

    @SerializedName("status")
    String status;

    @SerializedName("message")
    String message;

}