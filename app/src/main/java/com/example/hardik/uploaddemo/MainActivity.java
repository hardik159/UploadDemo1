package com.example.hardik.uploaddemo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button UploadBn, ChooseBn;
    private EditText NAME;
    private ImageView imgView;
    private Bitmap bitmap;
    private String imageLocation="";
    private String encoded_string, ImageFileName;
    private static final int REQUEST_EXTERNAL_STORAGE_RESULT=1;

    private File photoFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UploadBn=(Button) findViewById(R.id.uploadBn);
        ChooseBn=(Button) findViewById(R.id.chooseBn);
        NAME= (EditText) findViewById(R.id.name);
        imgView= (ImageView) findViewById(R.id.imageView);

        ChooseBn.setOnClickListener(this);
        UploadBn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.chooseBn:

                /*if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED){
                    selectImage();
                }
                else{
                    if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        Toast.makeText(this,"External Storage required to save images",Toast.LENGTH_LONG).show();
                    }
                    requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_EXTERNAL_STORAGE_RESULT);
                }*/

                selectImage();
                break;
            
            case R.id.uploadBn:

                uploadImage();
                break;

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){

        if(requestCode==REQUEST_EXTERNAL_STORAGE_RESULT){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                uploadImage();
            }
            else{
                Toast.makeText(this,"External write permission is not being granted",Toast.LENGTH_LONG).show();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    private void uploadImage() {

        StringRequest request=  new StringRequest(Request.Method.POST, "http://54.209.199.81:81/tutorial3/upload.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            String Response = jsonObject.getString("response");
                            Toast.makeText(MainActivity.this,Response,Toast.LENGTH_LONG).show();

                            imgView.setImageResource(0);
                            imgView.setVisibility(View.GONE);
                            NAME.setText("");
                            NAME.setVisibility(View.GONE);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> map= new HashMap<>();
                map.put("name",NAME.getText().toString().trim());
                map.put("image",imageToString(bitmap));
                Log.e("Hello",imageToString(((BitmapDrawable)imgView.getDrawable()).getBitmap()));

                return map;
            }
        };

        MySingleton.getInstance(MainActivity.this).addToRequestQue(request);
    }

    private String imageToString(Bitmap bitmap1){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes,Base64.DEFAULT);
    }

    private void selectImage(){
        Intent intent= new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        try{
            photoFile=createImageFile();

        }
        catch (IOException e){
            e.printStackTrace();
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        startActivityForResult(intent, 1231);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==1231 && resultCode==RESULT_OK){

                bitmap= BitmapFactory.decodeFile(imageLocation);
                imgView.setImageBitmap(bitmap);

                imgView.setVisibility(View.VISIBLE);
                NAME.setVisibility(View.VISIBLE);
        }
    }

    File createImageFile() throws IOException{
        //String timestamp= new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        ImageFileName= "IMAGE.jpg";
        File fileDirectory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image= File.createTempFile(ImageFileName,"",fileDirectory);
        imageLocation= image.getAbsolutePath();

        return image;
    }
}
