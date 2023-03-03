package com.ekyc.sdk;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static com.ekyc.sdk.UTLsData.generateKey;


public class DBHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME="d_sss";
    public static String TABLE_NAME="tbss_dat";
    public String id="id";
    public String mobile_number="mobile_number";
    public String pan_number="pan_number";
    public String access_token="access_token";
    public String balance="balance";

    Context mContext;

    private String tendency ="";

    public DBHelper(Context context)
    {

        super(context,DATABASE_NAME,null,1);
        mContext=context;
        tendency=mDecoded(mContext.getResources().getString(R.string.sjgcjsgfjhdgfh));

        Log.e("data","tendancy "+tendency);

//        tendency=mDecoded(context.getResources().getString(R.string.unknownfile)+context.getResources().getString(R.string.unknownfile1)).substring(31,47);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME+"("+id+" INTEGER PRIMARY KEY AUTOINCREMENT,"+access_token+" TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
//        onCreate(sqLiteDatabase);
    }







    //    select item from table by item_id
    public String mGet()
    {

        return mDecoded(mContext.getResources().getString(R.string.sjgcjsgfjhdgfh));
    }

    public String mGetOutLetId()
    {
        String selectdata = "SELECT  * FROM " + TABLE_NAME ;
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor=db.rawQuery(selectdata,null);

        String first="";

        if (cursor.moveToFirst())
        {
            do
            {
                first=cursor.getString(1);

            }
            while (cursor.moveToNext());
        }


        first=mGetDetail(tendency,first,id);

        


        return first;
    }
    public String mGetPanNumber()
    {
        String selectdata = "SELECT  * FROM " + TABLE_NAME ;
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor=db.rawQuery(selectdata,null);

        String first="";

        if (cursor.moveToFirst())
        {
            do
            {
                first=cursor.getString(1);

            }
            while (cursor.moveToNext());
        }


        first=mGetDetail(tendency,first,pan_number);

        


        return first;
    }
    public String mGetMobile()
    {
        String selectdata = "SELECT  * FROM " + TABLE_NAME ;
        SQLiteDatabase db = this.getReadableDatabase();


        Cursor cursor=db.rawQuery(selectdata,null);

        String data="";

        if (cursor.moveToFirst())
        {
            do
            {
                data=cursor.getString(1);
            }
            while (cursor.moveToNext());
        }

        

        data=mGetDetail(tendency,data,mobile_number);

        return data;
    }


    protected byte[] mEncodStringToByteBase64(String value)
    {
        byte[] data1 = Base64.decode(value, Base64.DEFAULT);

        return data1;
    }


    protected String mDecoded(String data)
    {
        byte[] data1 = Base64.decode(data, Base64.DEFAULT);
        String text1 = null;
        try {
            text1 = new String(data1, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return text1;
    }

    public String mBaseURL()
    {
       String baseurl="https://api.pay2all.in/outlet/";

        return baseurl;
    }


    public String mGetDetail(String secret_key,String data,String getvalue)
    {

        SecretKey secretKey=null;

        String value="";
        try
        {
            secretKey=generateKey(secret_key);
        }
        catch (NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
        catch (InvalidKeySpecException e)
        {
            e.printStackTrace();
        }

        try
        {
            if (!data.equals(""))
            {
                JSONObject jsonObject=new JSONObject(UTLsData.decryptMsg(mEncodStringToByteBase64(data),secretKey));
                value=jsonObject.getString(getvalue);
            }
            else
            {
                value="";
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (InvalidParameterSpecException e)
        {
            e.printStackTrace();
        }
        catch (InvalidAlgorithmParameterException e)
        {
            e.printStackTrace();
        }
        catch (InvalidKeyException e)
        {
            e.printStackTrace();
        }
        catch (BadPaddingException e)
        {
            e.printStackTrace();
        }
        catch (IllegalBlockSizeException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return value;
    }
}