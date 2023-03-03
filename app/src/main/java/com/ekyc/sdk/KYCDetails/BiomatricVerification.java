package com.ekyc.sdk.KYCDetails;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ekyc.sdk.CallResAPIPOSTMethod;
import com.ekyc.sdk.Contants;
import com.ekyc.sdk.CustomeAlertProgressBar;
import com.ekyc.sdk.DBHelper;
import com.ekyc.sdk.DetectConnection;

import com.ekyc.sdk.DevicesList.DeviceCardAdapter;
import com.ekyc.sdk.DevicesList.DevicesItems;
import com.ekyc.sdk.R;
import com.ekyc.sdk.UTLsData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;

public class BiomatricVerification extends AppCompatActivity {

    EditText edittext_customer_mobile,edittext_customer_aadhaar_number,edittext_pan_number;
    LinearLayout ll_select_device;
    TextView textview_select_device;

    public AlertDialog alertDialog;

    String device_package="";

    TextView textview_capture_quality;

    LinearLayout ll_fingerprint;
    ImageView imageview_finger_print;

    Button button_re_capture,button_submit;

    String biometricdata;

    String errCode;
    String errInfo;

    String pidtype;
    String qScore;
    String action = "scan";

    String sessionKey;
    String f137ci;
    String Piddata;


    DBHelper dbHelper;
    SecretKey secretKey = null;

    String aadhaar="",pan="",key="",id="";

    String full_bio_data="";

    Button textview_ok;

    TextView textview_message;
    ImageView imageview_messase_image;


    String pan_number="",outlet_id="",mobile="";

    JSONObject jsonObject;

    boolean error=false;

    String status="",message="";

    int result_code=1421;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biomatric_verification);
        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent=getIntent();

        if (intent.hasExtra("aadhaar"))
        {
            aadhaar=intent.getStringExtra("aadhaar");
        }
        if (intent.hasExtra("pan"))
        {
            pan=intent.getStringExtra("pan");
        }
        if (intent.hasExtra("key"))
        {
            key=intent.getStringExtra("key");
        }
        if (intent.hasExtra("id"))
        {
            id=intent.getStringExtra("id");
        }

        if (intent.hasExtra("outlet_id"))
        {
            outlet_id=getIntent().getStringExtra("outlet_id");
        }

        if (intent.hasExtra("pan"))
        {
            pan_number=getIntent().getStringExtra("pan");
        }

        if (intent.hasExtra("mobile"))
        {
            mobile=getIntent().getStringExtra("mobile");
        }

        try {
            jsonObject=new JSONObject();
            if (outlet_id.equals(""))
            {
                jsonObject.put("outlet_id","Outlet id should not be null");
                error=true;
            }

            if (pan_number.equals(""))
            {
                jsonObject.put("pan_number","PAN number should not be null");
                error=true;
            }
            else if (pan_number.length()<10)
            {
                jsonObject.put("pan_number","PAN number should be valid");
                error=true;
            }

            assert mobile != null;
            if (mobile.equals(""))
            {
                jsonObject.put("mobile","Mobile number should not be null");
                error=true;
            }
            else if (mobile.length()<10)
            {
                jsonObject.put("mobile","Mobile number should be valid ");
                error=true;
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        if (error)
        {
            Intent intent1=new Intent(BiomatricVerification.this,EKYC.class);
            intent1.putExtra("status","2");
            intent1.putExtra("message",jsonObject.toString());

            Contants.status="2";
            Contants.message=jsonObject.toString();

            setResult(result_code,intent1);
            finish();
        }

        this.dbHelper = new DBHelper(this);
        try {
            secretKey = UTLsData.generateKey(dbHelper.mGet());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
        }

        edittext_customer_mobile=findViewById(R.id.edittext_customer_mobile);
        edittext_customer_mobile.setText(mobile);

        edittext_customer_aadhaar_number=findViewById(R.id.edittext_customer_aadhaar_number);
        edittext_customer_aadhaar_number.setText(aadhaar);

        edittext_pan_number=findViewById(R.id.edittext_pan_number);
        edittext_pan_number.setText(pan);

        textview_capture_quality=findViewById(R.id.textview_capture_quality);
        ll_fingerprint=findViewById(R.id.ll_fingerprint);
        imageview_finger_print=findViewById(R.id.imageview_finger_print);

        ll_select_device=findViewById(R.id.ll_select_device);
        ll_select_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShowDialog();
            }
        });

        textview_select_device=findViewById(R.id.textview_select_device);

        button_re_capture=findViewById(R.id.button_re_capture);
        button_re_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DetectConnection.checkInternetConnection(BiomatricVerification.this))
                {
                    if (device_package.equals(""))
                    {
                        Toast.makeText(BiomatricVerification.this, "Please select Biometric device", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        mCheckAppInstall();
                    }
                }
                else
                {
                    Toast.makeText(BiomatricVerification.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_submit=findViewById(R.id.button_submit);
        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DetectConnection.checkInternetConnection(BiomatricVerification.this))
                {
                     if (action.equals("scan")) {
                         if (device_package.equals(""))
                         {
                             Toast.makeText(BiomatricVerification.this, "Please select Biometric device", Toast.LENGTH_SHORT).show();
                         }
                         else
                         {
                             mCheckAppInstall();
                         }

                        } else {
                            mValidateData(key,id);
                        }
                }
                else
                {
                    Toast.makeText(BiomatricVerification.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
        {

            if (status.equals(""))
            {
                status="failure";
            }
            if (message.equals(""))
            {
                message="Cancelled by back pressed";
            }


            try {
                jsonObject.put("status",status);
                jsonObject.put("message",message);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }


            Intent intent=new Intent(BiomatricVerification.this,EKYC.class);
            intent.putExtra("status",status);
            intent.putExtra("data",jsonObject.toString());

            Contants.status="2";
            Contants.message=jsonObject.toString();

            setResult(result_code,intent);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }


    public void mShowDialog() {
        View inflate = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custome_alert_dialog_show_devices_list, null);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.imageview_close);
        RecyclerView recyclerView = (RecyclerView) inflate.findViewById(R.id.recyclerview);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(inflate);
        this.alertDialog = builder.create();
        String[] strArr = {"Mantra", "Morpho", "Startek","SecuGen","Tatvik","Precision"};
        String[] strArr2 = {"MANTRA_PROTOBUF", "MORPHO_PROTOBUF", "STARTEK_PROTOBUF", "SECUGEN_PROTOBUF", "TATVIK_PROTOBUF", "PRECISION_PROTOBUF"};
        String[] strArr3 = {"com.mantra.rdservice", "com.scl.rdservice", "com.acpl.registersdk","com.secugen.rdservice","com.tatvik.bio.tmf20","com.precision.pb510.rdservice"};
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList arrayList = new ArrayList();
        DeviceCardAdapter deviceCardAdapter = new DeviceCardAdapter(this, arrayList);
        recyclerView.setAdapter(deviceCardAdapter);
        for (int i = 0; i < strArr.length; i++) {
            DevicesItems devicesItems = new DevicesItems();
            StringBuilder sb = new StringBuilder();
            sb.append(i);
            sb.append("");
            devicesItems.setId(sb.toString());
            devicesItems.setName(strArr[i]);
            devicesItems.setPackage_name(strArr3[i]);
            devicesItems.setType(strArr2[i]);
            devicesItems.setFragment_type("bio");
            arrayList.add(devicesItems);
            deviceCardAdapter.notifyDataSetChanged();
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        this.alertDialog.show();
    }

    public void mGetData(DevicesItems devicesItems) {
        alertDialog.dismiss();
        textview_select_device.setText(devicesItems.getName());
        device_package = devicesItems.getPackage_name();
    }

    public void mCheckAppInstall() {
        String str = "android.intent.action.VIEW";
        if (isPackageInstalled(device_package,getPackageManager())) {
            mGetBioData(this.device_package);
            return;
        }
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("market://details?id=");
            sb.append(this.device_package);
            startActivity(new Intent(str, Uri.parse(sb.toString())));
        } catch (ActivityNotFoundException unused) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("https://play.google.com/store/apps/details?id=");
            sb2.append(this.device_package);
            startActivity(new Intent(str, Uri.parse(sb2.toString())));
        }
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private final void mGetBioData(String str) {
        try {
            String createPidOptXML = DeviceScanFormateBioVer.createPidOptXML("0");

            if (!str.equals("")&&str.equalsIgnoreCase("com.precision.pb510.rdservice"))
            {
                createPidOptXML = DeviceScanFormateBioVer.createPrecisionPidOptXML("0");
            }

            if (createPidOptXML != null) {
                Log.e("PidOptions", createPidOptXML);
                Intent intent = new Intent();
                intent.setPackage(str);
                intent.setAction("in.gov.uidai.rdservice.fp.CAPTURE");
                intent.putExtra("PID_OPTIONS", createPidOptXML);
                startActivityForResult(intent, 1421);
            }

        } catch (Exception e) {
            Log.e("Error", e.toString());
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        String str = "qScore";
        String str2 = "ci";
        String str3 = "errInfo";
        String str4 = "type";
        String str5 = "Skey";
        String str6 = "errCode";
        String str7 = "Data";
        String str8 = "Resp";
        String str9 = "PidData";
        super.onActivityResult(i, i2, intent);
        if (i == 1421 && i2 == -1) {
            this.biometricdata = intent.getStringExtra("PID_DATA");
            full_bio_data=biometricdata;

            String str10 = "";
            this.biometricdata = this.biometricdata.replaceAll("\n", str10);
            this.biometricdata = this.biometricdata.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", str10);
            StringBuilder sb = new StringBuilder();
            sb.append("result ");
            sb.append(this.biometricdata);
            Log.e("bio data", sb.toString());
            try {
                JSONObject json = new XmlToJson.Builder(this.biometricdata).build().toJson();
                if (json.has(str9)) {
                    JSONObject jSONObject = json.getJSONObject(str9);
                    if (jSONObject.has(str8)) {
                        JSONObject jSONObject2 = jSONObject.getJSONObject(str8);
                        if (jSONObject2.has(str6)) {
                            this.errCode = jSONObject2.getString(str6);
                        }
                        if (jSONObject2.has(str3)) {
                            this.errInfo = jSONObject2.getString(str3);
                        }
                        if (jSONObject2.has(str)) {
                            this.qScore = jSONObject2.getString(str);
                        }
                    }
                    String str11 = "content";
                    if (jSONObject.has(str7)) {
                        JSONObject jSONObject3 = jSONObject.getJSONObject(str7);
                        if (jSONObject3.has(str4)) {
                            this.pidtype = jSONObject3.getString(str4);
                        }
                        if (jSONObject3.has(str11)) {
                            this.Piddata = jSONObject3.getString(str11);
                        }
                    }
                    if (jSONObject.has(str5)) {
                        JSONObject jSONObject4 = jSONObject.getJSONObject(str5);
                        if (jSONObject4.has(str2)) {
                            this.f137ci = jSONObject4.getString(str2);
                        }
                        if (jSONObject4.has(str11)) {
                            this.sessionKey = jSONObject4.getString(str11);
                        }
                    }
                    if (this.errCode.equals("0")) {
                        this.ll_fingerprint.setVisibility(View.VISIBLE);
                        this.imageview_finger_print.setColorFilter(getResources().getColor(R.color.green));
                        this.action = "submit";
                        this.button_submit.setText(getResources().getString(R.string.proceed_now));
                        this.button_re_capture.setVisibility(View.VISIBLE);
                        TextView textView = this.textview_capture_quality;
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append("Capture Score ");
                        sb2.append(this.qScore);
                        sb2.append(" %");
                        textView.setText(sb2.toString());
                        return;
                    }
                    this.action = "scan";
                    this.ll_fingerprint.setVisibility(View.GONE);
                    this.button_re_capture.setVisibility(View.GONE);
                    this.button_submit.setText(getResources().getString(R.string.capture_fingerprint));
                    Toast.makeText(this, this.errInfo, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void mValidateData(String key,String id) {
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("outlet_id",  outlet_id);
            jSONObject.put("payment_id",  "13");
            jSONObject.put("aadhar_number",  aadhaar);
            jSONObject.put("mobile_number",  mobile);
            jSONObject.put("primaryKeyId",  key);
            jSONObject.put("encodeFPTxnId",  id);
            StringBuilder sb = new StringBuilder();
            sb.append(this.biometricdata);
            sb.append("");
            jSONObject.put("biometric_data",  sb.toString());
            jSONObject.put("PidDatatype",  this.pidtype);
            jSONObject.put("ci",  this.f137ci);
            jSONObject.put("client_id",  this.dbHelper.mGetMobile());

            Log.e("sending","data "+jSONObject.toString());
            try {
                mSubmitData(mEncodByteToStringBase64(UTLsData.encryptMsg(jSONObject.toString(), this.secretKey)));
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
    protected void mSubmitData(String jsondata)
    {
        String sending_url=dbHelper.mBaseURL()+"v1/outletapi";
        Uri.Builder builder=new Uri.Builder();
        builder.appendQueryParameter("json_data",jsondata);
        builder.appendQueryParameter("biometric_data",biometricdata);
        Log.e("bio","data "+biometricdata);
        Log.e("bio","fulldata "+full_bio_data);
        final CustomeAlertProgressBar alertProgressBar=new CustomeAlertProgressBar();
        new CallResAPIPOSTMethod(BiomatricVerification.this,builder,sending_url,true,"POST")
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                alertProgressBar.mShowProgressBar(BiomatricVerification.this,"Please wait...");
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                Log.e("response","bio submit"+s);
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

                    mShowStatus();
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

            }
        }.execute();
    }

    public String mEncodByteToStringBase64(byte[] bArr) {
        return Base64.encodeToString(bArr, 0);
    }

    protected void mShowStatus()
    {
        LayoutInflater inflater =(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v2=inflater.inflate(R.layout.custom_alertdalog_for_message,null);

        textview_ok=v2.findViewById(R.id.button_ok);
        textview_message=(TextView)v2.findViewById(R.id.textview_message);
        if (!message.equals("")) {
            textview_message.setText(message);
        }
        else
        {
            textview_message.setText("Something went wrong...");
        }
        imageview_messase_image=(ImageView)v2.findViewById(R.id.imageview_messase_image);

        final AlertDialog.Builder builder2=new AlertDialog.Builder(BiomatricVerification.this);
        builder2.setCancelable(false);

        if (status.equals("1")||status.equalsIgnoreCase("true"))
        {
            imageview_messase_image.setImageResource(R.drawable.success);
        }
        else if (status.equalsIgnoreCase("false")||status.equals("2"))
        {
            textview_message.setTextColor(getResources().getColor(R.color.red));
            imageview_messase_image.setImageResource(R.drawable.failure_icon);

        }
        else
        {
            textview_message.setTextColor(getResources().getColor(R.color.red));
            imageview_messase_image.setImageResource(R.drawable.failure_icon);

        }

        builder2.setView(v2);
        final AlertDialog alert=builder2.create();

        textview_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert.dismiss();

                if (status.equals(""))
                {
                    status="failure";
                }

                if (message.equals(""))
                {
                    message="something went wrong";
                }

                try {
                    jsonObject.put("status",status);
                    jsonObject.put("message",message);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }

                Intent intent=new Intent(BiomatricVerification.this,OTPVerify.class);
                intent.putExtra("status",status);
                intent.putExtra("data",jsonObject.toString());

                Contants.status="2";
                Contants.message=jsonObject.toString();

                setResult(1421,intent);
                finish();
            }
        });
        alert.show();
    }

    @Override
    public void onBackPressed() {

        if (status.equals(""))
        {
            status="2";
        }
        if (message.equals(""))
        {
            message="Cancelled by back pressed";
        }

        try {
            jsonObject.put("status",status);
            jsonObject.put("message",message);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        Intent intent=new Intent(BiomatricVerification.this,EKYC.class);
        intent.putExtra("status",status);
        intent.putExtra("data",jsonObject.toString());

        Contants.status="2";
        Contants.message=jsonObject.toString();

        setResult(result_code,intent);
        finish();

    }
}