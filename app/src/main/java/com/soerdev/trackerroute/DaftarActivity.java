package com.soerdev.trackerroute;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.soerdev.trackerroute.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DaftarActivity extends AppCompatActivity {

    private String varemailUser, varpassUser, varkonfPass;

    private TextInputEditText emailDaftar, sandiDaftar, namaUser;
    private ProgressDialog progressDialog;
    private ImageView fotoGambar;

    private String TAG = DaftarActivity.class.getSimpleName();

    private String TAG_SUCCESS = "success";
    private String TAG_MESSAGE = "message";

    private String KEY_USERNAME = "username";
    private String KEY_PASSWORD = "password";
    private String KEY_CONFIRMPASS = "con_pass";
    private String KEY_IMAGE = "link_foto";

    int success;
    int REQUEST_CAMERA = 1888;
    int bitmap_size = 60;

    Bitmap bitmap, decoded;

    Uri Fileurl, gmmIntentUri;

    ConnectivityManager connectivityManager;

    String json_obj_req = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        emailDaftar = findViewById(R.id.emailUserDaftar);
        sandiDaftar = findViewById(R.id.sandiUserDaftar);
        namaUser = findViewById(R.id.namaUser);

        Button kirimKode = findViewById(R.id.daftarAkun);

        fotoGambar = findViewById(R.id.gambarFoto);

        fotoGambar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Fileurl = getOutPutMediaFileURI();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Fileurl);
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        });

        kirimKode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);{
                    if(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected()){
                        cekNIntent();
                    }else{
                        Toast.makeText(getApplicationContext(), "Tidak Ada Koneksi Internet !", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Tunggu Sebentar . . .");
        progressDialog.setCancelable(false);
    }



    private void cekNIntent() {
        varemailUser = emailDaftar.getText().toString().trim();
        varpassUser = sandiDaftar.getText().toString().trim();
        varkonfPass = namaUser.getText().toString().trim();

        if(TextUtils.isEmpty(varemailUser)){
            Toast.makeText(DaftarActivity.this, "Masukkan E - mail Anda !", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(varpassUser)){
            Toast.makeText(DaftarActivity.this, "Masukkan Kata Sandi Anda !", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(varkonfPass)){
            Toast.makeText(DaftarActivity.this, "Ketik Ulang Kata Sandi Anda !", Toast.LENGTH_SHORT).show();
        }
        else{
            if(varpassUser.length() < 8 ){
                Toast.makeText(DaftarActivity.this, "Kata Sandi Minimal 8 Karakter !", Toast.LENGTH_SHORT).show();
            }
            else{
                buatAkun();
            }
        }
    }

    private void buatAkun() {
        String URL_DAFTAR = "https://sembarangsims.000webhostapp.com/backSims/register.php";
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_DAFTAR, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Response:" + response);

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    success = jsonObject.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        Log.e("Registration Success !", jsonObject.toString());

                        Toast.makeText(getApplicationContext(), jsonObject.getString(TAG_MESSAGE) + ", silahkan login !", Toast.LENGTH_SHORT).show();

                        emailDaftar.setText("");
                        sandiDaftar.setText("");
                        namaUser.setText("");

                        Intent intent = new Intent(DaftarActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), jsonObject.getString(TAG_MESSAGE) + ", coba lagi !", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                Toast.makeText(DaftarActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, error.getMessage().toString());
            }
        }){
            @Override
            protected Map<String, String> getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put(KEY_USERNAME, varemailUser);
                params.put(KEY_PASSWORD, varpassUser);
                params.put(KEY_CONFIRMPASS, varkonfPass);
                params.put(KEY_IMAGE, getStringImage(decoded));
                Log.e(TAG, "" + params);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, json_obj_req);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImages = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImages;
    }

    private Uri getOutPutMediaFileURI() {
        return Uri.fromFile(getOutPutMediaFile());
    }

    private File getOutPutMediaFile() {
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

    private void setToImageView(Bitmap bmp){
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        fotoGambar.setImageBitmap(decoded);
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
}
