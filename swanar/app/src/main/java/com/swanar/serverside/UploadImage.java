package com.swanar.serverside;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.R;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.ActivityLauncher;

import android.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.Policy;
import java.util.Date;
import java.util.List;

public class UploadImage extends Activity{


    private String accessKey = "95070fd07e31dde5639e3c1153f7cffdfc917bef";
    private String secretKey = "a3bfb8e2849bcef4bbd4d4e904035822c66d6fe1";

    private String url = "https://vws.vuforia.com";
    private String targetName = "";
ImageView imageView0;
    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

    String str = ""; //image uri path

Uri imageUri;
Uri compressedUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadimage);
        imageView0 = (ImageView) findViewById(R.id.imageView0);

      //  popUpInput();



        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);


        callCamera();






    }


    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader(new BasicHeader("Content-Type", "application/json"));

        sb.tmsSignature(request,secretKey);

                request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }


    void sendToServer () throws URISyntaxException, IOException, JSONException{




        HttpPost postRequest = new HttpPost();
        HttpClient client = new DefaultHttpClient();


        postRequest.setURI(new URI(url + "/targets"));
        JSONObject requestBody = new JSONObject();


    //    File imageFile = new File(imageUri.getPath());


        BitmapFactory.Options bitoption = new BitmapFactory.Options();
        bitoption.inSampleSize = 4;

        Bitmap bmpp = BitmapFactory.decodeFile(imageUri.getPath(), bitoption);


//Bitmap bmpp = get_Reduce_bitmap_Picture (imageUri.getPath());




         compressedUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/swanARpics","fname_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg"));






        FileOutputStream out = null;
        try {
            out = new FileOutputStream(compressedUri.getPath());
            bmpp.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        File cFile = new File(compressedUri.getPath());






        byte[] image = FileUtils.readFileToByteArray(cFile);



        requestBody.put("name", targetName); // Mandatory
        requestBody.put("width", 0.25); // Mandatory
        requestBody.put("image", encodeToString(image)); // Mandatory







        postRequest.setEntity(new StringEntity(requestBody.toString()));

        setHeaders(postRequest); // Must be done after setting the body

        HttpResponse response = client.execute(postRequest);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);

        JSONObject jobj = new JSONObject(responseBody);

        String uniqueTargetId = jobj.has("target_id") ? jobj.getString("target_id") : "";
        System.out.println("\nCreated target with id: " + uniqueTargetId);



    }



    public Bitmap get_Reduce_bitmap_Picture(String imagePath) {

        int ample_size = 4;
        // change ample_size to 32 or any power of 2 to increase or decrease bitmap size of image


        Bitmap bitmap = null;
        BitmapFactory.Options bitoption = new BitmapFactory.Options();
        bitoption.inSampleSize = ample_size;

        Bitmap bitmapPhoto = BitmapFactory.decodeFile(imagePath, bitoption);

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }



        int orientation = exif
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
        Matrix matrix = new Matrix();

        if ((orientation == 3)) {
            matrix.postRotate(180);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        } else if (orientation == 6) {
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        } else if (orientation == 8) {
            matrix.postRotate(270);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        } else {
            matrix.postRotate(0);
            bitmap = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    bitmapPhoto.getWidth(), bitmapPhoto.getHeight(), matrix,
                    true);

        }

        return bitmap;

    }




public String encodeToString (byte [] byteArray) throws UnsupportedEncodingException
{
    String hash = Base64.encodeToString(byteArray, Base64.DEFAULT);
    return hash;
}


    void callCamera ()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/swanARpics","fname_" +
                String.valueOf(System.currentTimeMillis()) + ".jpg"));



        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

                //use imageUri here to access the image
               str = imageUri.getPath();
                Bitmap bmp = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(str),
                        1024, 1024);
                //Bundle extras = data.getExtras();

                Log.e("URI",imageUri.toString());

              //  Bitmap bmp = (Bitmap) extras.get("data");


                imageView0.setImageBitmap(bmp);
                // here you will get the image as bitmap
                Toast.makeText(this, str, Toast.LENGTH_LONG);

            }
            else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
            }
        }





        final EditText input = (EditText)  findViewById(R.id.editText);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub


                try {

                    targetName = input.getText().toString();

                    sendToServer();

                    Context ctx = v.getContext();
                    Intent intent = new Intent(ctx, ActivityLauncher.class);


                    ctx.startActivity(intent);


                }catch (URISyntaxException ex) {
                } catch (IOException ex) {
                }
                catch (JSONException ex) {
                }


            }
        });



    }








}
