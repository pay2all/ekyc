package com.ekyc.sdk.KYCDetails;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;

import com.android.volley.RequestQueue;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ekyc.sdk.CallResAPIPOSTMethod;
import com.ekyc.sdk.Contants;
import com.ekyc.sdk.CustomeAlertProgressBar;
import com.ekyc.sdk.DBHelper;
import com.ekyc.sdk.DetectConnection;
import com.ekyc.sdk.R;
import com.ekyc.sdk.RetrofitDetails.DocumentSubmitResponse;
import com.ekyc.sdk.RetrofitDetails.RetrofitFactory;
import com.ekyc.sdk.RetrofitDetails.RetrofitService;
import com.ekyc.sdk.UTLsData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
import retrofit2.Retrofit;

public class DocumentKYC extends AppCompatActivity implements LocationListener {

    ImageView iv_pan_photo,iv_cancel_check,iv_kyc_doc,iv_kyc_doc_back;


    int CAMERA_INTENT=1,GALLERY_INTENT=2;
    String image="",image_type="";

    String pan_image="",cancel_cheque_image="",document_image="",document_image_back="";

    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    AlertDialog alertDialog;
    String imagepath="";

    Button bt_submit;


    DBHelper dbHelper;
    SecretKey secretKey = null;

    String pan_number="",outlet_id="",mobile="";

    JSONObject jsonObject1;

    boolean error=false;

    String status="",message="";

    LocationManager locationManager;

    String provider="";


    EditText ed_sending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_kyc);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (getSupportActionBar()!=null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ed_sending=findViewById(R.id.ed_sending);

        this.dbHelper = new DBHelper(this);
        try {
            secretKey = UTLsData.generateKey(dbHelper.mGet());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e2) {
            e2.printStackTrace();
        }


        Bundle intent=getIntent().getExtras();
        if (intent.containsKey("outlet_id"))
        {
            outlet_id=getIntent().getStringExtra("outlet_id");
        }

        if (intent.containsKey("pan_number"))
        {
            pan_number=getIntent().getStringExtra("pan_number");
        }

        if (intent.containsKey("mobile"))
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
            Intent intent1=new Intent(DocumentKYC.this,EKYC.class);
            intent1.putExtra("status","2");
            intent1.putExtra("data",jsonObject1.toString());

            Contants.status="2";
            Contants.message=jsonObject1.toString();

            setResult(RESULT_CANCELED,intent1);
            finish();
        }


        iv_pan_photo=findViewById(R.id.iv_pan_photo);
        iv_pan_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_type="pan";
                mShowDialog();
            }
        });


        iv_cancel_check=findViewById(R.id.iv_cancel_check);
        iv_cancel_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_type="cancel";
                mShowDialog();
            }
        });

        iv_kyc_doc=findViewById(R.id.iv_kyc_doc);
        iv_kyc_doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_type="doc";
                mShowDialog();
            }
        });

        iv_kyc_doc_back=findViewById(R.id.iv_kyc_doc_back);
        iv_kyc_doc_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_type="doc_back";
                mShowDialog();
            }
        });

        bt_submit=findViewById(R.id.bt_submit);
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DetectConnection.checkInternetConnection(DocumentKYC.this))
                {
                    if (pan_image.equals(""))
                    {
                        Toast.makeText(DocumentKYC.this, "Please select PAN card photo", Toast.LENGTH_SHORT).show();
                    }
                    else if (cancel_cheque_image.equals(""))
                    {
                        Toast.makeText(DocumentKYC.this, "Please select cancelled cheque photo", Toast.LENGTH_SHORT).show();
                    }
                    else if (document_image.equals(""))
                    {
                        Toast.makeText(DocumentKYC.this, "Please select KYC document font photo", Toast.LENGTH_SHORT).show();
                    }
                    else if (document_image_back.equals(""))
                    {
                        Toast.makeText(DocumentKYC.this, "Please select KYC document back photo", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        mValidateData();
                    }
                }
                else
                {
                    Toast.makeText(DocumentKYC.this, "No internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mShowLocation();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK) {

            InputStream inputStream;
            if (requestCode==CAMERA_INTENT) {

                Uri uri= Uri.fromFile(new File(imagepath));
                try {
                    inputStream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    inputStream = null;
                }

                Bitmap zoom_decode_bitmap = BitmapFactory.decodeStream(inputStream);

//                File imgFile = new File(imagepath);
//                if (imgFile.exists()) {
//                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mSetimageInImageView(image_type,zoom_decode_bitmap);
//                }
            }
            else if (requestCode==GALLERY_INTENT)
            {
                Uri selectedImage = data.getData();
                InputStream imageStream = null;
                try {
                    imageStream = getApplicationContext().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
                mSetimageInImageView(image_type,yourSelectedImage);
            }
            else if (requestCode==1421)
            {
                status=data.getStringExtra("status");
                Intent intent=new Intent(DocumentKYC.this,EKYC.class);
                intent.putExtra("status",status);
                intent.putExtra("data",data.getStringExtra("data"));

                Contants.status="2";
                Contants.message=jsonObject1.toString();

                setResult(RESULT_CANCELED,intent);
                finish();
            }

        }
    }

    public static String encodeToBase64(Bitmap image)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void mShowDialog()
    {
        LayoutInflater inflater2 = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v2 = inflater2.inflate(R.layout.custome_alertdialog_choose_image, null);

        final AlertDialog.Builder builder2 = new AlertDialog.Builder(DocumentKYC.this);
        builder2.setCancelable(false);

        builder2.setView(v2);

        RelativeLayout rl_capture=v2.findViewById(R.id.rl_capture);
        rl_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCheckCamera()) {
                    alertDialog.dismiss();
                    String imageFileName = getResources().getString(R.string.app_name) + ".jpg";
                    File storageDir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES);
                    imagepath = storageDir.getAbsolutePath() + "/" + imageFileName;
                    File file = new File(imagepath);
                    Uri outputFileUri = Uri.fromFile(file);
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

                    startActivityForResult(cameraIntent, CAMERA_INTENT);
                }
                else
                {
                    alertDialog.dismiss();
                    mRequestCamera();
                }
            }
        });
        RelativeLayout rl_from_gallery=v2.findViewById(R.id.rl_from_gallery);
        rl_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheckWriteStorage()) {
                    alertDialog.dismiss();
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_INTENT);
                }
                else
                {
                    mRequestWriteStorage();
                }
            }
        });

        alertDialog = builder2.create();
        alertDialog.setCancelable(true);

        alertDialog.show();
    }


    protected void mSetimageInImageView(String img_type,Bitmap img)
    {
         if (img_type.equalsIgnoreCase("pan"))
        {
            iv_pan_photo.setImageBitmap(img);
            pan_image=encodeToBase64(img);

        }
        else if (img_type.equalsIgnoreCase("cancel"))
        {
            iv_cancel_check.setImageBitmap(img);
            cancel_cheque_image=encodeToBase64(img);

        }
        else if (img_type.equalsIgnoreCase("doc"))
        {
            iv_kyc_doc.setImageBitmap(img);
            document_image=encodeToBase64(img);

        }
        else if (img_type.equalsIgnoreCase("doc_back"))
        {
            iv_kyc_doc_back.setImageBitmap(img);
            document_image_back=encodeToBase64(img);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==android.R.id.home)
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

            Intent intent=new Intent(DocumentKYC.this,EKYC.class);
            intent.putExtra("status",status);
            intent.putExtra("data",jsonObject1.toString());

            Contants.status="2";
            Contants.message=jsonObject1.toString();

            setResult(RESULT_CANCELED,intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void mValidateData() {
        try {

            JSONObject jSONObject = new JSONObject();
            jSONObject.put("outlet_id",  outlet_id);
            jSONObject.put("payment_id",  "15");
            jSONObject.put("mobile_number",  mobile);
            jSONObject.put("pan_number",  pan_image);
            jSONObject.put("outlet_cancel_cheque_photo",  cancel_cheque_image);
            jSONObject.put("kyc_document",  document_image);
            jSONObject.put("aadhar_back_image",  document_image_back);

            try {
//                mSubmit(mEncodByteToStringBase64(UTLsData.encryptMsg(jSONObject.toString(), this.secretKey)));

                String sending_url=dbHelper.mBaseURL()+"v1/outletapi";
                String send_data=mEncodByteToStringBase64(UTLsData.encryptMsg(jSONObject.toString(), this.secretKey));
                mOkkHttps(sending_url,send_data);
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
    protected void mSubmit(final String jsondata)
    {

        String sending_url=dbHelper.mBaseURL()+"v1/outletapi";
        Uri.Builder builder=new Uri.Builder();
        builder.appendQueryParameter("json_data",jsondata);

//        ed_sending.setText(jsondata);

        mCreateFile(jsondata);

        final CustomeAlertProgressBar alertProgressBar=new CustomeAlertProgressBar();
        new CallResAPIPOSTMethod(DocumentKYC.this,builder,sending_url,true,"POST")
        {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.e("data",jsondata);

                alertProgressBar.mShowProgressBar(DocumentKYC.this,"Please wait...");
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

                    if (status.equals("1")||status.equalsIgnoreCase("true"))
                    {

                        Intent intent=new Intent(DocumentKYC.this,OTPVerify.class);
                        intent.putExtra("pan_number",pan_number);
                        intent.putExtra("outlet_id",outlet_id);
                        intent.putExtra("mobile",mobile);
                        startActivityForResult(intent,1421);
                        finish();
                    }

                    else if (status.equalsIgnoreCase("false")||status.equalsIgnoreCase("2"))
                    {
                        if (!message.equals(""))
                        {
                            Toast.makeText(DocumentKYC.this, message, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            message="Something went wrong...";
                            Toast.makeText(DocumentKYC.this, message, Toast.LENGTH_SHORT).show();
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

                        Intent intent=new Intent(DocumentKYC.this,EKYC.class);
                        intent.putExtra("status",status);
                        intent.putExtra("data",jsonObject1.toString());

                        Contants.status="2";
                        Contants.message=jsonObject1.toString();

                        setResult(RESULT_CANCELED,intent);
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


    public boolean mCheckCamera() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        else {
            return false;
        }
    }

    public void mRequestCamera() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale( DocumentKYC.this, "android.permission.CAMERA")) {
            ActivityCompat.requestPermissions(DocumentKYC.this, new String[]{"android.permission.CAMERA"}, 1);
        }
    }
    public boolean mCheckWriteStorage() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0;
    }

    public void mRequestWriteStorage() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale( DocumentKYC.this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            ActivityCompat.requestPermissions(DocumentKYC.this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }
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
            jsonObject1.put("status",status);
            jsonObject1.put("message",message);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        Intent intent=new Intent(DocumentKYC.this,EKYC.class);
        intent.putExtra("status",status);
        intent.putExtra("data",jsonObject1.toString());

        Contants.status="2";
        Contants.message=jsonObject1.toString();

        setResult(RESULT_CANCELED,intent);
        finish();

        super.onBackPressed();
    }

    @Override
    public void onLocationChanged(Location location) {
//        Log.e("lat",latitude+"");
//        Log.e("long",longitude+"");


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


    protected void mCreateFile(String sBody)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
        Date now = new Date();
        String fileName = formatter.format(now) + ".txt";//like 2016_01_12.txt


        try
        {
            File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+File.separator, "Report Files");
            //File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists())
            {
                root.mkdirs();
            }
            File gpxfile = new File(root, fileName);


            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Data has been written to Report File", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void mOkkHttps(String sending_url,String json_data)
    {
        final CustomeAlertProgressBar alertProgressBar=new CustomeAlertProgressBar();
        class DatNewSubmit extends AsyncTask<String, String,String>
        {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.e("data",json_data);

                alertProgressBar.mShowProgressBar(DocumentKYC.this,"Please wait...");
            }

            @Override
            protected String doInBackground(String... strings) {

                String response_data=null;
                try {
                    OkHttpClient client = new OkHttpClient().newBuilder()

                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)

                            .build();


                    RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                            .setType(Objects.requireNonNull(mediaType))

                            .addFormDataPart("json_data",json_data)
                                    .build();
                    Request request = new Request.Builder()
                            .url(sending_url)
                            .method("POST", body)
                            .addHeader("Content-Type","application/json; charset=utf-8")
                            .addHeader("Accept","application/json")
                            .build();
                    Response response = client.newCall(request).execute();

                    response_data= response.body().string();

                    Log.e("respon","respos "+response.message());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    response_data=e.getMessage();
                }

                return response_data;
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

                    if (status.equals("1")||status.equalsIgnoreCase("true"))
                    {

                        Intent intent=new Intent(DocumentKYC.this,OTPVerify.class);
                        intent.putExtra("pan_number",pan_number);
                        intent.putExtra("outlet_id",outlet_id);
                        intent.putExtra("mobile",mobile);
                        startActivityForResult(intent,1421);
                        finish();
                    }

                    else if (status.equalsIgnoreCase("false")||status.equalsIgnoreCase("2"))
                    {
                        if (!message.equals(""))
                        {
                            Toast.makeText(DocumentKYC.this, message, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            message="Something went wrong...";
                            Toast.makeText(DocumentKYC.this, message, Toast.LENGTH_SHORT).show();
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

                        Intent intent=new Intent(DocumentKYC.this,EKYC.class);
                        intent.putExtra("status",status);
                        intent.putExtra("data",jsonObject1.toString());

                        Contants.status="2";
                        Contants.message=jsonObject1.toString();

                        setResult(RESULT_CANCELED,intent);
                        finish();
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }

        new DatNewSubmit().execute();
    }

}