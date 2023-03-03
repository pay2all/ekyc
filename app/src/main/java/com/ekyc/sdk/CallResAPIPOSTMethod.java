package com.ekyc.sdk;

import android.app.Activity;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public abstract class CallResAPIPOSTMethod extends AsyncTask<String, String, String> {
    Builder builder;
    boolean isbodyavaialbel;
    Activity mWeakActivity;
    String method;
    HttpURLConnection urlConnection;
    String weburl = "";

    public CallResAPIPOSTMethod(Activity activity, Builder builder2, String str, boolean z, String str2) {
        this.mWeakActivity = activity;
        this.builder = builder2;
        this.weburl = str;
        this.isbodyavaialbel = z;
        this.method = str2;
    }

    /* access modifiers changed from: protected */
    public String doInBackground(String... strArr) {
        InputStream inputStream;
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.weburl).openConnection();
            httpURLConnection.setReadTimeout(40000);
            httpURLConnection.setConnectTimeout(40000);
            httpURLConnection.setRequestMethod(this.method);
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            Log.e("sending data", this.builder.toString());
            if (this.isbodyavaialbel) {
                String encodedQuery = this.builder.build().getEncodedQuery();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(httpURLConnection.getOutputStream(), "UTF-8"));
                bufferedWriter.write(encodedQuery);
                bufferedWriter.flush();
            }
            if (httpURLConnection.getResponseCode() == 200) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    return stringBuffer.toString();
                }
                stringBuffer.append(readLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
