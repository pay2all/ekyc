package com.ekyc.sdk.KYCDetails;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ekyc.sdk.Contants;
import com.ekyc.sdk.R;

import org.json.JSONException;
import org.json.JSONObject;

public class EKYC extends AppCompatActivity {

    EditText ed_outlet_id,ed_mobile,ed_pan;
    Button bt_submit;
    String pan_number="",outlet_id="",mobile="";
    JSONObject jsonObject1;
    boolean error=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_e_k_y_c);

        ed_outlet_id=findViewById(R.id.ed_outlet_id);
        ed_mobile=findViewById(R.id.ed_mobile);
        ed_pan=findViewById(R.id.ed_pan);
        bt_submit=findViewById(R.id.bt_submit);

        bt_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ed_outlet_id.getText().toString().equals(""))
                {
                    Toast.makeText(EKYC.this, "Please enter outlet id", Toast.LENGTH_SHORT).show();
                }
                else if (ed_pan.getText().toString().equals(""))
                {
                    Toast.makeText(EKYC.this, "Please enter PAN number", Toast.LENGTH_SHORT).show();
                }
                else if (ed_mobile.getText().toString().equals(""))
                {
                    Toast.makeText(EKYC.this, "Please enter mobile number", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent intent=new Intent(EKYC.this,DocumentKYC.class);
                    intent.putExtra("outlet_id",ed_outlet_id.getText().toString());
                    intent.putExtra("mobile",ed_mobile.getText().toString());
                    intent.putExtra("pan_number",ed_pan.getText().toString());
                    startActivityForResult(intent,1421);
                }
            }
        });

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
            Intent intent1=new Intent();
            Contants.status="2";
            Contants.message=jsonObject1.toString();
            intent1.putExtra("status",Contants.status);
            intent1.putExtra("data",Contants.message);

            setResult(1421,intent1);
            finish();
        }
        else
        {
            Intent intent2=new Intent(EKYC.this,DocumentKYC.class);
            intent2.putExtra("outlet_id",outlet_id);
            intent2.putExtra("mobile",mobile);
            intent2.putExtra("pan_number",pan_number);
            startActivityForResult(intent2,1421);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Intent intent=new Intent();
        intent.putExtra("status",Contants.status);
        intent.putExtra("data",Contants.message);
        setResult(1421,intent);
        finish();

    }
}