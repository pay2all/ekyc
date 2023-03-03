package com.ekyc.sdk.KYCDetails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ekyc.sdk.CallResAPIPOSTMethod;
import com.ekyc.sdk.Contants;
import com.ekyc.sdk.CustomeAlertProgressBar;
import com.ekyc.sdk.DBHelper;
import com.ekyc.sdk.DetectConnection;
import com.ekyc.sdk.R;
import com.ekyc.sdk.UTLsData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.List;
import java.util.Locale;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class OTPVerify extends AppCompatActivity implements LocationListener {

    EditText ed_mobile,ed_aadhaar_number,ed_pan_number,ed_matm_sn;
    Button bt_submit;

    DBHelper dbHelper;
    SecretKey secretKey = null;

    TextView tv_otp_message,tv_resend;
    CardView cardview_detail,cardview_otp;
    EditText ed_otp;
    Button bt_submit_otp;

    String encodeFPTxnId="",primaryKeyId="",device_id="";

    String pan="",aadhaar="",matm_sn="";


    LocationManager locationManager;
    String provider="";
    double latitude=0.0,longitude=0.0;

    String address="noida";

    String pan_number="",outlet_id="",mobile="";

    JSONObject jsonObject1;

    boolean error=false;

    String status="",message="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_verify);
        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        this.dbHelper = new DBHelper(this);
        try {
            secretKey = UTLsData.generateKey(dbHelper.mGet());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
        }

        device_id= Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        Intent intent=getIntent();
        if (intent.hasExtra("outlet_id"))
        {
            outlet_id=getIntent().getStringExtra("outlet_id");
        }

        if (intent.hasExtra("pan_number"))
        {
            pan_number=getIntent().getStringExtra("pan_number");
        }

        if (intent.hasExtra("mobile"))
        {
            mobile=getIntent().getStringExtra("mobile");
        }

        try {
            jsonObject1=new JSONObject();
            if (outlet_id.equals(""))
            {
                jsonObject1.put("outlet_id","Outlet id should not be null");
                error=true;
            }

            if (pan_number.equals(""))
            {
                jsonObject1.put("pan_number","PAN number should not be null");
                error=true;
            }
            else if (pan_number.length()<10)
            {
                jsonObject1.put("pan_number","PAN number should be valid");
                error=true;
            }

            assert mobile != null;
            if (mobile.equals(""))
            {
                jsonObject1.put("mobile","Mobile number should not be null");
                error=true;
            }
            else if (mobile.length()<10)
            {
                jsonObject1.put("mobile","Mobile number should be valid ");
                error=true;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (error)
        {
            Intent intent1=new Intent(OTPVerify.this,EKYC.class);
            intent1.putExtra("status","2");
            intent1.putExtra("data",jsonObject1.toString());
            Contants.status="2";
            Contants.message=jsonObject1.toString();
            setResult(1421,intent1);
            finish();
        }

        ed_mobile=findViewById(R.id.ed_mobile);
        ed_mobile.setText(mobile);
        ed_aadhaar_number=findViewById(R.id.ed_aadhaar_number);

        ed_pan_number=findViewById(R.id.ed_pan_number);
        ed_pan_number.setText(pan_number);

        ed_matm_sn=findViewById(R.id.ed_matm_sn);
        bt_submit=findViewById(R.id.bt_submit);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DetectConnection.checkInternetConnection(OTPVerify.this))
                {
                    if (ed_aadhaar_number.getText().toString().equals(""))
                    {
                        Toast.makeText(OTPVerify.this, "Please enter aadhaar number", Toast.LENGTH_SHORT).show();
                    }
                    else if (ed_aadhaar_number.getText().toString().length()<12)
                    {
                        Toast.makeText(OTPVerify.this, "Please enter a valid aadhaar number", Toast.LENGTH_SHORT).show();
                    }
                    else if (ed_pan_number.getText().toString().equals(""))
                    {
                        Toast.makeText(OTPVerify.this, "Please enter PAN number", Toast.LENGTH_SHORT).show();
                    }
                    else if (ed_pan_number.getText().toString().length()<10)
                    {
                        Toast.makeText(OTPVerify.this, "Please enter a valid PAN number", Toast.LENGTH_SHORT).show();
                    }
                    else {
                         pan=ed_pan_number.getText().toString();
                         aadhaar=ed_aadhaar_number.getText().toString();
                         matm_sn=ed_matm_sn.getText().toString();
                        mValidateData(pan,aadhaar,matm_sn,"10");

                    }
                }
                else
                {
                    Toast.makeText(OTPVerify.this, "No intenet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tv_otp_message=findViewById(R.id.tv_otp_message);
        tv_resend=findViewById(R.id.tv_resend);
        tv_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DetectConnection.checkInternetConnection(OTPVerify.this))
                {
                    mValidateData(pan,aadhaar,matm_sn,"10");
                }
                else
                {
                    Toast.makeText(OTPVerify.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cardview_detail=findViewById(R.id.cardview_detail);
        cardview_otp=findViewById(R.id.cardview_otp);
        ed_otp=findViewById(R.id.ed_otp);
        bt_submit_otp=findViewById(R.id.bt_submit_otp);
        bt_submit_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DetectConnection.checkInternetConnection(OTPVerify.this))
                {
                    if (ed_otp.getText().toString().equals(""))
                    {
                        tv_otp_message.setText("We have sent you OTP on your registred mobile number "+mobile+" please check your sms inbox or Whatsapp inbox");
                        tv_otp_message.setTextColor(getResources().getColor(R.color.green));
                    }
                    else
                    {
                        String otp=ed_otp.getText().toString();
                        mValidateOTPData(otp,primaryKeyId,encodeFPTxnId);
                    }
                }
                else
                {
                    Toast.makeText(OTPVerify.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mShowLocation();
    }


    public void mValidateData(String pan,String aadhaar,String matm_sn,String paymentid) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("outlet_id",  outlet_id);
            jSONObject.put("payment_id",  paymentid);
            jSONObject.put("mobile_number",  mobile);
            jSONObject.put("pan_number",  pan);
            jSONObject.put("aadhar_number",  aadhaar);
            jSONObject.put("lat",  latitude);
            jSONObject.put("long",  longitude);
            jSONObject.put("location",  address);
            jSONObject.put("ip_address",  device_id);
            jSONObject.put("matm_sn",  matm_sn);

            try {
                mSubmit(mEncodByteToStringBase64(UTLsData.encryptMsg(jSONObject.toString(), this.secretKey)));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e2) {
                e2.printStackTrace();
            } catch (InvalidKeyException e3) {
                e3.printStackTrace();
            } catch (InvalidParameterSpecException e4) {
                e4.printStackTrace();
            } catch (IllegalBlockSizeException e5) {
                e5.printStackTrace();
            } catch (BadPaddingException e6) {
                e6.printStackTrace();
            } catch (UnsupportedEncodingException e7) {
                e7.printStackTrace();
            }
        } catch (JSONException e8) {
            e8.printStackTrace();
        }
    }

    public String mEncodByteToStringBase64(byte[] bArr) {
        return Base64.encodeToString(bArr, 0);
    }

    @SuppressLint("StaticFieldLeak")
    protected void mSubmit(String jsondata)
    {
        String sending_url=dbHelper.mBaseURL()+"v1/outletapi";
        Uri.Builder builder=new Uri.Builder();
        builder.appendQueryParameter("json_data",jsondata);
        final CustomeAlertProgressBar alertProgressBar=new CustomeAlertProgressBar();
        new CallResAPIPOSTMethod(OTPVerify.this,builder,sending_url,true,"POST")
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                alertProgressBar.mShowProgressBar(OTPVerify.this,"Please wait...");
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.e("response","data "+s);

                if (alertProgressBar.AlertDialogShowing())
                {
                    alertProgressBar.alert.dismiss();
                }
                try {
                    JSONObject jsonObject=new JSONObject(s);
                    if (jsonObject.has("status_id"))
                    {
                        status=jsonObject.getString("status_id");
                    }
                    if (jsonObject.has("status"))
                    {
                        status=jsonObject.getString("status");
                    }
                    if (jsonObject.has("message"))
                    {
                        message=jsonObject.getString("message");
                    }
                    if (jsonObject.has("primaryKeyId"))
                    {
                        primaryKeyId=jsonObject.getString("primaryKeyId");
                    }
                    if (jsonObject.has("encodeFPTxnId"))
                    {
                        encodeFPTxnId=jsonObject.getString("encodeFPTxnId");
                    }

                    if (status.equals("1")||status.equalsIgnoreCase("true"))
                    {
                        cardview_detail.setVisibility(View.GONE);
                        cardview_otp.setVisibility(View.VISIBLE);
                    }

                    else if (status.equalsIgnoreCase("false")||status.equalsIgnoreCase("2"))
                    {
                        if (!message.equals(""))
                        {
                            Toast.makeText(OTPVerify.this, message, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            message="Something went wrong...";
                            Toast.makeText(OTPVerify.this, message, Toast.LENGTH_SHORT).show();
                        }

                        try {
                            jsonObject1=new JSONObject();
                            jsonObject1.put("status",status);
                            jsonObject1.put("message",message);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                        Intent intent=new Intent(OTPVerify.this,EKYC.class);
                        intent.putExtra("status",status);
                        intent.putExtra("data",jsonObject1.toString());

                        Contants.status="2";
                        Contants.message=jsonObject1.toString();

                        setResult(1421,intent);
                        finish();
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }.execute();
    }


    public void mValidateOTPData(String otp,String primaryKeyId,String encodeFPTxnId) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("outlet_id",  outlet_id);
            jSONObject.put("payment_id",  "11");
            jSONObject.put("mobile_number",  mobile);
            jSONObject.put("otp",  otp);
            jSONObject.put("primaryKeyId",  primaryKeyId);
            jSONObject.put("encodeFPTxnId",  encodeFPTxnId);

            Log.e("response","data "+jSONObject.toString());

            try {
                mSubmitOTP(mEncodByteToStringBase64(UTLsData.encryptMsg(jSONObject.toString(), this.secretKey)));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e2) {
                e2.printStackTrace();
            } catch (InvalidKeyException e3) {
                e3.printStackTrace();
            } catch (InvalidParameterSpecException e4) {
                e4.printStackTrace();
            } catch (IllegalBlockSizeException e5) {
                e5.printStackTrace();
            } catch (BadPaddingException e6) {
                e6.printStackTrace();
            } catch (UnsupportedEncodingException e7) {
                e7.printStackTrace();
            }
        } catch (JSONException e8) {
            e8.printStackTrace();
        }
    }


    @SuppressLint("StaticFieldLeak")
    protected void mSubmitOTP(String jsondata)
    {
        String sending_url=dbHelper.mBaseURL()+"v1/outletapi";
        Uri.Builder builder=new Uri.Builder();
        builder.appendQueryParameter("json_data",jsondata);
        final CustomeAlertProgressBar alertProgressBar=new CustomeAlertProgressBar();
        new CallResAPIPOSTMethod(OTPVerify.this,builder,sending_url,true,"POST")
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                alertProgressBar.mShowProgressBar(OTPVerify.this,"Please wait...");
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.e("response","data submit"+s);
                if (alertProgressBar.AlertDialogShowing())
                {
                    alertProgressBar.alert.dismiss();
                }

                try {
                    JSONObject jsonObject=new JSONObject(s);
                    if (jsonObject.has("status_id"))
                    {
                        status=jsonObject.getString("status_id");
                    }
                    if (jsonObject.has("status"))
                    {
                        status=jsonObject.getString("status");
                    }
                    if (jsonObject.has("message"))
                    {
                        message=jsonObject.getString("message");
                    }
                    if (jsonObject.has("primaryKeyId"))
                    {
                        primaryKeyId=jsonObject.getString("primaryKeyId");
                    }
                    if (jsonObject.has("encodeFPTxnId"))
                    {
                        encodeFPTxnId=jsonObject.getString("encodeFPTxnId");
                    }

                    if (status.equals("1")||status.equalsIgnoreCase("true"))
                    {
                        Intent intent=new Intent(OTPVerify.this,BiomatricVerification.class);
                        intent.putExtra("aadhaar",aadhaar);
                        intent.putExtra("pan",pan);
                        intent.putExtra("outlet_id",outlet_id);
                        intent.putExtra("mobile",mobile);
                        intent.putExtra("key",primaryKeyId);
                        intent.putExtra("id",encodeFPTxnId);
                        startActivityForResult(intent,1421);
                    }

                    else if (status.equalsIgnoreCase("false")||status.equalsIgnoreCase("2"))
                    {
                        if (!message.equals(""))
                        {
                            Toast.makeText(OTPVerify.this, message, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            message="something went wrong";
                            Toast.makeText(OTPVerify.this, message, Toast.LENGTH_SHORT).show();
                        }

                        try {

                            jsonObject1.put("status",status);
                            jsonObject1.put("message",message);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                        Intent intent=new Intent();
                        Bundle bundle=new Bundle();
                        bundle.putString("status",status);
                        bundle.putString("data",jsonObject1.toString());
                        intent.putExtras(bundle);

                        Contants.status="2";
                        Contants.message=jsonObject1.toString();

                        setResult(1421,intent);
                        finish();

                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }.execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {
            if (cardview_detail.getVisibility()==View.GONE)
            {
                cardview_detail.setVisibility(View.VISIBLE);
                cardview_otp.setVisibility(View.GONE);
            }
            else
            {

                if (status.equals(""))
                {
                    status="2";
                }
                if (message.equals(""))
                {
                    message="Cancelled by back pressed";
                }

                try {
                    jsonObject1.put("status",status);
                    jsonObject1.put("message",message);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                Intent intent=new Intent(OTPVerify.this,EKYC.class);
                intent.putExtra("status",status);
                intent.putExtra("data",jsonObject1.toString());

                Contants.status="2";
                Contants.message=jsonObject1.toString();

                setResult(1421,intent);
                finish();

            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (cardview_detail.getVisibility()==View.GONE)
        {
            cardview_detail.setVisibility(View.VISIBLE);
            cardview_otp.setVisibility(View.GONE);
        }
        else
        {

            if (status.equals(""))
            {
                status="2";
            }
            if (message.equals(""))
            {
                message="Cancelled by back pressed";
            }

            try {
                jsonObject1.put("status",status);
                jsonObject1.put("message",message);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }


            Intent intent=new Intent(OTPVerify.this,EKYC.class);
            intent.putExtra("status",status);
            intent.putExtra("data",jsonObject1.toString());

            Contants.status="2";
            Contants.message=jsonObject1.toString();

            setResult(1421,intent);
            finish();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude=location.getLatitude();
        longitude=location.getLongitude();
//        Log.e("lat",latitude+"");
//        Log.e("long",longitude+"");


        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            address=addresses.toString();
//            Log.e("data","location "+address);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    0);
        }
    }

    @Override
    public void onProviderEnabled(String s) {


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }


    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                "Your GPS seems like disabled, do you want to enable it?")
                .setCancelable(false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    protected void mShowLocation()
    {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    0);
        }

        locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        statusCheck();

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        if (provider != null && !provider.equals("")) {
            if (!provider.contains("gps")) { // if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
            // Get the location from the given provider
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 500, 0, this);

            if (location != null)
                onLocationChanged(location);
            else
                location = locationManager.getLastKnownLocation(provider);
            if (location != null)
                onLocationChanged(location);
            else

                Toast.makeText(getBaseContext(), "Location can't be retrieved",
                        Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getBaseContext(), "No Provider Found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==1421)
        {
            assert data != null;
            status=data.getStringExtra("status");
            Intent intent=new Intent(OTPVerify.this,EKYC.class);
            intent.putExtra("status",status);
            intent.putExtra("data",data.getStringExtra("data"));
            Contants.status="2";
            Contants.message=jsonObject1.toString();
            setResult(1421,intent);
            finish();
        }
    }
}