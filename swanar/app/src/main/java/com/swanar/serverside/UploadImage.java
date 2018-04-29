package com.swanar.serverside;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;


import com.R;

import org.apache.commons.codec.binary.Base64;
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

public class UploadImage extends Activity{


    private String accessKey = "95070fd07e31dde5639e3c1153f7cffdfc917bef";
    private String secretKey = "a3bfb8e2849bcef4bbd4d4e904035822c66d6fe1";

    private String url = "https://vws.vuforia.com";
    private String targetName = "imagetesttest";
    private String imageLocation = "C:\\Users\\Tom\\Desktop\\test.jpeg";
ImageView imageView0;
    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

    String str = ""; //image uri path

Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uploadimage);
        imageView0 = (ImageView) findViewById(R.id.imageView0);


        callCamera();






    }


    private void setHeaders(HttpUriRequest request) {
        SignatureBuilder sb = new SignatureBuilder();
        request.setHeader(new BasicHeader("Date", DateUtils.formatDate(new Date()).replaceFirst("[+]00:00$", "")));
        request.setHeader(new BasicHeader("Content-Type", "application/json"));
        accessKey = accessKey;
        secretKey = secretKey;
        sb = sb;
        request = request;
        sb.tmsSignature(request,secretKey);
        String s = null;





                request.setHeader("Authorization", "VWS " + accessKey + ":" + sb.tmsSignature(request, secretKey));
    }


    void sendToServer () throws URISyntaxException, IOException, JSONException{

        HttpPost postRequest = new HttpPost();
        HttpClient client = new DefaultHttpClient();


        postRequest.setURI(new URI(url + "/targets"));
        JSONObject requestBody = new JSONObject();


        File imageFile = new File(imageUri.getPath());

        //int flags =  Base64.NO_WRAP | Base64.URL_SAFE;

        byte[] image = FileUtils.readFileToByteArray(imageFile);
        requestBody.put("name", targetName); // Mandatory
        requestBody.put("width", 0.25); // Mandatory
      //  requestBody.put("image", Base64.encode(image,flags)); // Mandatory
        Base64.encodeBase64String(image);

        String ss = null;



        postRequest.setEntity(new StringEntity(requestBody.toString()));

        setHeaders(postRequest); // Must be done after setting the body

        HttpResponse response = client.execute(postRequest);
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);

        JSONObject jobj = new JSONObject(responseBody);

        String uniqueTargetId = jobj.has("target_id") ? jobj.getString("target_id") : "";
        System.out.println("\nCreated target with id: " + uniqueTargetId);



    }






    void callCamera ()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"fname_" +
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



        try {
            sendToServer();

        }catch (URISyntaxException ex) {
        } catch (IOException ex) {
        }
        catch (JSONException ex) {
        }

    }

   /* private void setPic() {
        // Get the dimensions of the View
        int targetW = imageView0.getWidth();
        int targetH = imageView0.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        imageView0.setImageBitmap(bitmap);
    }*/


    /*void PostTarget() throws URISyntaxException {

        HttpPost postRequest = new HttpPost();
        HttpClient client = new DefaultHttpClient();=


        postRequest.setURI(new URI(url + "/targets"));
        JSONObject requestBody = new JSONObject();


        byte[] image = FileUtils.readFileToByteArray(imageFile);
        requestBody.put("name", targetName); // Mandatory
        requestBody.put("width", 0.25); // Mandatory
        requestBody.put("image", Base64.encodeBase64String(image)); // Mandatory
        requestBody.put("active_flag", 1); // Optional
        requestBody.put("application_metadata", Base64.encodeBase64String("Vuforia test metadata".getBytes())); // Optional

    }*/




}
