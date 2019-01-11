package com.soerdev.trackerroute;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.soerdev.trackerroute.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private ImageView imageUser;
    private Button absenIn, absenOut;
    private TextView counter, namaUser;

    private String TAG_ID = "id";
    private String TAG_USERNAME = "username";
    private String TAG_TABLE_NAME = "namaTable";
    private String TAG_KOORDINAT_AWAL = "awal";
    private String TAG_KOORDINAT_AKHIR = "akhir";
    private String TAG_KOORDINAT = "koordinat";
    private String TAG_WAKTU = "date";
    private String TAG_SUCESS = "success";
    private String TAG_IMG_COLUMN = "link_foto";
    private String TAG_IMG_ABSEN = "image";
    private String TAG_MESSAGE = "message";
    private String TAG_DEVICE_ID = "id_device";

    private String TAG = HomeActivity.class.getSimpleName();
    private String counterTime;

    private String URL_INSERT_KOORDINAT = "https://sembarangsims.000webhostapp.com/backSims/create_table_koordinat.php";
    private String URL_SELECT_SIMS = "https://sembarangsims.000webhostapp.com/backSims/select_file.php";
    private String URL_UPLOAD_ABSEN = "https://sembarangsims.000webhostapp.com/backSims/upload_pict.php";

    Bitmap bitmap, decoded;

    int btnabsenIn = 0;

    SharedPreferences sharedPreferences;

    LocationManager locationManager;

    Uri Fileurl, gmmIntentUri;

    Thread t;

    int count;
    int success;

    int REQUEST_CAMERA = 0;
    int bitmap_size = 60;

    double latti, longi;

    private String varUserNameNow;
    private int varUserUidNow;

    String status, deviceID;
    String json_obj_req = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);

        sharedPreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

        varUserUidNow = (sharedPreferences.getInt(TAG_ID, 0));
        varUserNameNow = (sharedPreferences.getString(TAG_USERNAME, ""));

        imageUser = findViewById(R.id.imageView);

        counter = findViewById(R.id.counter);
        namaUser = findViewById(R.id.textView2);

        Toast.makeText(getApplicationContext(), "Selamat Datang " + varUserNameNow, Toast.LENGTH_SHORT).show();

        absenIn = findViewById(R.id.absenIn);
        absenOut = findViewById(R.id.absenOut);

        deviceID  = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);

        absenIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Fileurl = getOutPutMediaFileURI();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Fileurl);
                startActivityForResult(intent, REQUEST_CAMERA);
                locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                getloc();
                btnabsenIn = 1;
            }
        });

        absenOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();*/
                count = 6;

            }
        });
        //selectIMAGEfromUser();

    }

    public void startHitungan(){

        disabledBtn();

        t = new Thread(){

            @Override
            public void run(){

                while(!isInterrupted()){

                    try {
                        Thread.sleep(1000);  //1000ms = 1 sec

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                count++;
                                counter.setText(String.valueOf(count));
                                counterTime = counter.getText().toString();
                                if (counterTime.equals("60")){
                                    locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                                    getLocation();
                                    count = 0;
                                }
                            }
                        });

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }

    private void uploadGambarAbsen() {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Mengupload Absen", false, false);
       StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPLOAD_ABSEN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response" + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    success = jsonObject.getInt(TAG_SUCESS);

                    if (success == 1) {
                        Log.e("v Add", jsonObject.toString());

                        Toast.makeText(HomeActivity.this, jsonObject.getString(TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                        startHitungan();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(HomeActivity.this, jsonObject.getString(TAG_MESSAGE), Toast.LENGTH_SHORT).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dialog.dismiss();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.dismiss();

                Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage().toString());
            }
        }){
            @Override
            protected Map<String, String>getParams(){
                String koordinat = latti+","+longi;
                Date currentTime = Calendar.getInstance().getTime();
                String tanggal = currentTime.toString();
                String nol = Integer.toString(0);
                Map<String, String> params = new HashMap<String, String>();

                params.put(TAG_USERNAME, varUserNameNow);
                params.put(TAG_IMG_ABSEN, getStringImage(decoded));
                params.put(TAG_KOORDINAT_AWAL, koordinat);
                params.put(TAG_KOORDINAT_AKHIR, nol);
                params.put(TAG_DEVICE_ID, deviceID);
                params.put(TAG_WAKTU, tanggal);
                Log.e(TAG, ""+params);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, json_obj_req);
    }

    private void disabledBtn() {
        if(btnabsenIn == 1){
            absenIn.setEnabled(false);
            absenIn.setClickable(false);
        }else{
            absenIn.setEnabled(true);
            absenIn.setClickable(true);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            ProgressDialog dialog = ProgressDialog.show(this, null, "Lihat Lokasi . . .", false, false);

            if (location !=null){
                dialog.dismiss();
                latti = location.getLatitude();
                longi = location.getLongitude();
                //namaUser.setText(""+latti+","+longi);
                final String koordinat = latti+","+longi;

                StringRequest stringRequest = null;
                stringRequest = new StringRequest(Request.Method.POST, URL_INSERT_KOORDINAT, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "Response: " + response);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            status = jsonObject.getString(TAG_SUCESS);

                            if (status.equals("success")) {
                                Log.e("v Add", jsonObject.toString());
                            } else {
                                Toast.makeText(HomeActivity.this, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(HomeActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, error.getMessage().toString());
                    }
                }){
                    @Override
                    protected Map<String, String>getParams(){
                        Map<String, String>params = new HashMap<String, String>();

                        params.put(TAG_TABLE_NAME, varUserNameNow);
                        params.put(TAG_KOORDINAT, koordinat);
                        Log.e(TAG, ""+params);

                        return params;
                    }
                };
                AppController.getInstance().addToRequestQueue(stringRequest, json_obj_req);

            }
            else {
                //namaUser.setText("Nyalakan GPS Anda !");

                dialog.dismiss();

                //loc();

            }
        }
    }

    private void getloc() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            ProgressDialog dialog = ProgressDialog.show(this, null, "Lihat Lokasi . . .", false, false);

            if (location !=null){
                dialog.dismiss();
                latti = location.getLatitude();
                longi = location.getLongitude();
                //namaUser.setText(""+latti+","+longi);
                final String koordinat = latti+","+longi;

            }
            else {
                //namaUser.setText("Nyalakan GPS Anda !");

                dialog.dismiss();

                //loc();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case  1:
                getLocation();
                break;
        }
    }

    private void selectIMAGEfromUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SELECT_SIMS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    //success = jsonObject.getInt(TAG_SUCESS);

                    /*if (success == 1) {
                        Log.e("Registration Success !", jsonObject.toString());

                    }*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Failed To Load Image ! " +error.getMessage());
            }
        }){
            @Override
            protected Map<String, String>getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put(TAG_USERNAME, varUserNameNow);
                Log.e(TAG, ""+params);

                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest, json_obj_req);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            AlertDialog.Builder popDialog = new AlertDialog.Builder(this);
            popDialog.setTitle("Keluar")
                    .setMessage("Anda Ingin Keluar ?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(LoginActivity.session_status, false);
                            editor.putString(TAG_ID, null);
                            editor.putString(TAG_USERNAME, null);
                            editor.apply();
                            finish();
                        }
                    })
                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            popDialog.show();
        }

        return true;
    }

    private Uri getOutPutMediaFileURI() {
        return Uri.fromFile(getOutPutMediaFile());
    }

    private static File getOutPutMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "DeKa");
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Log.e("Monitoring", "Oops, Failed Create Monitoring Directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_DeKa_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImages = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImages;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "requestCode" +requestCode+ ", resultCode" + requestCode);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_CAMERA){
                try{
                    Log.e("CAMERA", Fileurl.getPath());

                    bitmap = BitmapFactory.decodeFile(Fileurl.getPath());
                    setToImageView(getResizedBitmap(bitmap, 512));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void setToImageView(Bitmap bmp){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        //checkGambar();
        uploadGambarAbsen();
    }

    public Bitmap getResizedBitmap(Bitmap image, int max_size){
        int width = image.getWidth();
        int height = image.getHeight();
        Bitmap gambar = image;

        float bitmapRatio = (float)width / (float)height;
        if(bitmapRatio > 1){
            width = max_size;
            height = (int)(width / bitmapRatio);
        }else{
            height = max_size;
            width = (int)(height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(gambar, width, height, true);
    }
}
