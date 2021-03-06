/*===============================================================================
Copyright (c) 2016-2018 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2015 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/


package com.vuforia.samples.VuforiaSamples.ui.ActivityList;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.R;
import com.swanar.serverside.UploadImage;
import com.vuforia.samples.VuforiaSamples.app.CloudRecognition.CloudReco;
import com.vuforia.samples.VuforiaSamples.app.ImageTargets.ImageTargets;
import com.vuforia.samples.VuforiaSamples.app.SwanThread;
import com.vuforia.samples.VuforiaSamples.app.TransportActivity;

import java.util.List;

import com.vuforia.samples.VuforiaSamples.app.TransportActivity;

import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swanmain.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;

import static android.content.ContentValues.TAG;


// This activity starts activities which demonstrate the Vuforia features
public class ActivityLauncher extends ListActivity
{
    
    private String mActivities[] = { "Start SWANAR"};

    Button button0;
    Button button2;

    String id = "thomas";
    boolean swanIsRunning = false;
    SwanThread thread1;

    /* private String mActivities[] = { "Image Targets", "VuMark", "Cylinder Targets",
            "Multi Targets", "User Defined Targets", "Object Reco", "Cloud Reco",
            "Virtual Buttons"};*/

    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            R.layout.activities_list_text_view, mActivities);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activities_list);





      //  setListAdapter(adapter);




//startSwan();


       button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, CloudReco.class);

                ctx.startActivity(intent);
            }
        });



        button0 = (Button) findViewById(R.id.button0);
        button0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                Intent intent = new Intent(ctx, UploadImage.class);


               ctx.startActivity(intent);
            }
        });

     /*  Intent intent = new Intent(this,TransportActivity.class) ;
        startActivity(intent);*/


    }


protected void startSwan()
{

    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> runningProcInfo = activityManager .getRunningAppProcesses();
    for(int i = 0; i < runningProcInfo.size(); i++){
        if(runningProcInfo.get(i).processName.equals("interdroid.swan")) {
            swanIsRunning = true;
        }
    }
    if (!swanIsRunning)
    {
         thread1 = new SwanThread(id,this);

    }
}


    @Override
    protected void onPause()
    {
        super.onPause();
       // unregisterSWANSensor();
        //thread1.unregisterSWANSensor();


        }

    @Override
    protected void onResume()
    {
        super.onResume();
       //thread1.registerSWANSensor();


    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //thread1.unregisterSWANSensor();


    }






    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        
       /* Intent intent = new Intent(this, AboutScreen.class);
        intent.putExtra("ABOUT_TEXT_TITLE", mActivities[position]);*/
        Context ctx = v.getContext();
        
        switch (position)
        {
            /*case 0:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.ImageTargets.ImageTargets");
                intent.putExtra("ABOUT_TEXT", "");
                Intent intent = new Intent(ctx, ImageTargets.class);


                ctx.startActivity(intent);
                break;*/
           /* case 6:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                        "app.VuMark.VuMark");
                intent.putExtra("ABOUT_TEXT", "VuMark/VM_about.html");
                break;
            case 2:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.CylinderTargets.CylinderTargets");
                intent.putExtra("ABOUT_TEXT", "CylinderTargets/CY_about.html");
                break;
            case 3:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.MultiTargets.MultiTargets");
                intent.putExtra("ABOUT_TEXT", "MultiTargets/MT_about.html");
                break;
            case 4:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.UserDefinedTargets.UserDefinedTargets");
                intent.putExtra("ABOUT_TEXT",
                    "UserDefinedTargets/UD_about.html");
                break;
            case 5:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.ObjectRecognition.ObjectTargets");
                intent.putExtra("ABOUT_TEXT", "ObjectRecognition/OR_about.html");
                break;*/
            case 0:
                /*intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.CloudRecognition.CloudReco");
                intent.putExtra("ABOUT_TEXT", "CloudReco/CR_about.html");*/
                Intent intent = new Intent(ctx, CloudReco.class);


                ctx.startActivity(intent);
                break;
            /*case 7:
                intent.putExtra("ACTIVITY_TO_LAUNCH",
                    "app.VirtualButtons.VirtualButtons");
                intent.putExtra("ABOUT_TEXT", "VirtualButtons/VB_about.html");
                break;*/
        }
        
       // startActivity(intent);
        
    }
}
