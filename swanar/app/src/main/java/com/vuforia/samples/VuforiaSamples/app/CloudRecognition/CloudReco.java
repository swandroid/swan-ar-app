/*===============================================================================
Copyright (c) 2016-2017 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.CloudRecognition;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.IntegerRes;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vuforia.CameraDevice;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.TargetFinder;
import com.vuforia.TargetSearchResult;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.R;
import com.vuforia.samples.VuforiaSamples.app.DisplayResultsOnScreen;
import com.vuforia.samples.VuforiaSamples.app.SwanThread;
import com.vuforia.samples.VuforiaSamples.ui.ActivityList.ActivityLauncher;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenu;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuGroup;
import com.vuforia.samples.VuforiaSamples.ui.SampleAppMenu.SampleAppMenuInterface;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


// The main activity for the CloudReco sample.
public class CloudReco extends Activity implements SampleApplicationControl,
    SampleAppMenuInterface
{
    private static final String LOGTAG = "CloudReco";
    private final String idLight = "light";
    private final String idLocation = "location";
    private final String idOV = "ov";
    boolean swanIsRunning = false;
    boolean isOvThreadActive = false;
    int counter = 0;
    boolean continueScanning = true;
    boolean continueLooking= true;
    boolean useLocation = false;
    String closestStation;
    public static boolean tripsAcquired = false;
    public static boolean go = true;


    private SampleApplicationSession vuforiaAppSession;
    
    // These codes match the ones defined in TargetFinder in Vuforia.jar
    static final int INIT_SUCCESS = 2;
    static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
    static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
    static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
    static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
    static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
    static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
    static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
    static final int UPDATE_ERROR_UPDATE_SDK = -6;
    static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;
    
    static final int HIDE_LOADING_DIALOG = 0;
    static final int SHOW_LOADING_DIALOG = 1;
    
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private CloudRecoRenderer mRenderer;
    
    private SampleAppMenu mSampleAppMenu;
    
    private boolean mExtendedTracking = false;
    private boolean mFinderStarted = false;
    private boolean mStopFinderIfStarted = false;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;

    private static final String kAccessKey = "ec2f3154f2ab78d31b1f65690754943daecb08d0";
    private static final String kSecretKey = "ba4d9ccecba48bac79cc0ff0b49d20849c45a5b1";
    
    // View overlays to be displayed in the Augmented View
    private RelativeLayout mUILayout;
    
    // Error message handling:
    private int mlastErrorCode = 0;
    private int mInitErrorCode = 0;
    private boolean mFinishActivityOnError;
    
    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;
    
    private GestureDetector mGestureDetector;
    
    private LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(
        this);

    // declare scan line and its animation
    private View scanLine;
    private TranslateAnimation scanAnimation;

    private double mLastErrorTime;

    private boolean mIsDroidDevice = false;
    //String identite;

    SwanThread lightThread;
    SwanThread locationThread;
    SwanThread ovThread;



    // Called when the activity first starts or needs to be recreated after
    // resuming the application or a configuration change.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        vuforiaAppSession = new SampleApplicationSession(this);
        
        startLoadingAnimation();
        
        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Creates the GestureDetector listener for processing double tap
        mGestureDetector = new GestureDetector(this, new GestureListener());
        
        mTextures = new Vector<Texture>();
        loadTextures();
        
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        AssetManager assetManager = this.getAssets();




        startSwan();



    }
    
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
            if (!result)
                Log.e("SingleTapUp", "Unable to trigger focus");

            // Generates a Handler to trigger continuous auto-focus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    final boolean autofocusResult = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

                    if (!autofocusResult)
                        Log.e("SingleTapUp", "Unable to re-enable continuous auto-focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    
    
    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk("TextureTeapotRed.png",
            getAssets()));
    }








    
    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
lightThread.registerSWANSensor();
locationThread.registerSWANSensor();
        //registerSWANSensor();

        showProgressIndicator(true);
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        vuforiaAppSession.onResume();
    }
    
    
    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
    }


    public String getOVValue (String stationName)
    {
        String ovValue = "";
        try {
            DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format("stops.txt")));
            Scanner scanner = new Scanner(textFileStream);
            scanner.nextLine(); //first line
            while (scanner.hasNextLine() && continueScanning)
            {
                /*String testt = scanner.nextLine();
                testt = testt;*/
                ovValue = searchLine(scanner.nextLine(), stationName);
            }

            // ovThread = new SwanThread(idOV, this, sc,);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ovValue;



    }


    String searchLine(String line, String stationName)
    {
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(",");
        String str = scanner.next();
     //   Log.e("strrrr", str);

        String code = str.substring(3);
        //int code = Integer.parseInt(str.substring(3));

        if (code.equals("00000000"))
        {
            continueScanning = false;
            return "";
        }

        String name = scanner.next();
        name = name.replace(" ", "");


        if (stationName.equals(name))
        {
            continueScanning = false;
            return code;
        }

            return "";



    }

    public void startOvThread (String identite)
    {


String stationName = identite.substring(10); //format Station_StationRAI

       String ovValue = getOVValue(stationName);

//Log.e("ovvalue", String.valueOf(ovValue));



         ovThread = new SwanThread(idOV, this, ovValue);





        isOvThreadActive = true;


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
            lightThread = new SwanThread(idLight,this);
           // startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            locationThread = new SwanThread(idLocation,this);




        }
    }




    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();

        lightThread.unregisterSWANSensor();
        locationThread.unregisterSWANSensor();
        if (isOvThreadActive)
        {
            ovThread.unregisterSWANSensor();
        }


        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Pauses the OpenGLView
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
    }
    
    
    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        lightThread.unregisterSWANSensor();
        locationThread.unregisterSWANSensor();
        if (isOvThreadActive)
        {
            ovThread.unregisterSWANSensor();
        }



        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        System.gc();
    }


    
    
    public void deinitCloudReco()
    {
        // Get the object tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
            .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
        {
            Log.e(LOGTAG,
                "Failed to destroy the tracking data set because the ObjectTracker has not"
                    + " been initialized.");
            return;
        }
        
        // Deinitialize target finder:
        TargetFinder finder = objectTracker.getTargetFinder();
        finder.deinit();
    }
    
    
    private void startLoadingAnimation()
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay_with_scanline,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // By default
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        loadingDialogHandler.mLoadingDialogContainer
            .setVisibility(View.VISIBLE);

        scanLine = mUILayout.findViewById(R.id.scan_line);
        scanLine.setVisibility(View.GONE);
        scanAnimation = new TranslateAnimation(
            TranslateAnimation.ABSOLUTE, 0f,
            TranslateAnimation.ABSOLUTE, 0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0f,
            TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);
        scanAnimation.setDuration(4000);
        scanAnimation.setRepeatCount(-1);
        scanAnimation.setRepeatMode(Animation.REVERSE);
        scanAnimation.setInterpolator(new LinearInterpolator());

        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        
    }
    
    
    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        // Initialize the GLView with proper flags
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        // Setups the Renderer of the GLView
        mRenderer = new CloudRecoRenderer(vuforiaAppSession, this);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
    }
    
    
    // Returns the error message for each error code
    private String getStatusDescString(int code)
    {
        if (code == UPDATE_ERROR_AUTHORIZATION_FAILED)
            return getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_DESC);
        if (code == UPDATE_ERROR_PROJECT_SUSPENDED)
            return getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_DESC);
        if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION)
            return getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_DESC);
        if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE)
            return getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_DESC);
        if (code == UPDATE_ERROR_UPDATE_SDK)
            return getString(R.string.UPDATE_ERROR_UPDATE_SDK_DESC);
        if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE)
            return getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_DESC);
        if (code == UPDATE_ERROR_REQUEST_TIMEOUT)
            return getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_DESC);
        if (code == UPDATE_ERROR_BAD_FRAME_QUALITY)
            return getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_DESC);
        else
        {
            return getString(R.string.UPDATE_ERROR_UNKNOWN_DESC);
        }
    }
    
    
    // Returns the error message for each error code
    private String getStatusTitleString(int code)
    {
        if (code == UPDATE_ERROR_AUTHORIZATION_FAILED)
            return getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_TITLE);
        if (code == UPDATE_ERROR_PROJECT_SUSPENDED)
            return getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_TITLE);
        if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION)
            return getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_TITLE);
        if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE)
            return getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_TITLE);
        if (code == UPDATE_ERROR_UPDATE_SDK)
            return getString(R.string.UPDATE_ERROR_UPDATE_SDK_TITLE);
        if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE)
            return getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_TITLE);
        if (code == UPDATE_ERROR_REQUEST_TIMEOUT)
            return getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_TITLE);
        if (code == UPDATE_ERROR_BAD_FRAME_QUALITY)
            return getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_TITLE);
        else
        {
            return getString(R.string.UPDATE_ERROR_UNKNOWN_TITLE);
        }
    }
    
    
    // Shows error messages as System dialogs
    public void showErrorMessage(int errorCode, double errorTime, boolean finishActivityOnError)
    {
        if (errorTime < (mLastErrorTime + 5.0) || errorCode == mlastErrorCode)
            return;
        
        mlastErrorCode = errorCode;
        mFinishActivityOnError = finishActivityOnError;
        
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }
                
                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                    CloudReco.this);
                builder
                    .setMessage(
                        getStatusDescString(CloudReco.this.mlastErrorCode))
                    .setTitle(
                        getStatusTitleString(CloudReco.this.mlastErrorCode))
                    .setCancelable(false)
                    .setIcon(0)
                    .setPositiveButton(getString(R.string.button_OK),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                if(mFinishActivityOnError)
                                {
                                    finish();
                                }
                                else
                                {
                                    dialog.dismiss();
                                }
                            }
                        });
                
                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }
    
    
    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }
                
                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                    CloudReco.this);
                builder
                    .setMessage(errorMessage)
                    .setTitle(getString(R.string.INIT_ERROR))
                    .setCancelable(false)
                    .setIcon(0)
                    .setPositiveButton(getString(R.string.button_OK),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                finish();
                            }
                        });
                
                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }
    
    
    public void startFinderIfStopped()
    {
        if(!mFinderStarted)
        {
            mFinderStarted = true;
            
            // Get the object tracker:
            TrackerManager trackerManager = TrackerManager.getInstance();
            ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
            
            // Initialize target finder:
            TargetFinder targetFinder = objectTracker.getTargetFinder();
            
            targetFinder.clearTrackables();
            targetFinder.startRecognition();
            scanlineStart();
        }
    }
    
    
    public void stopFinderIfStarted()
    {
        if(mFinderStarted)
        {
            mFinderStarted = false;
            
            // Get the object tracker:
            TrackerManager trackerManager = TrackerManager.getInstance();
            ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());
            
            // Initialize target finder:
            TargetFinder targetFinder = objectTracker.getTargetFinder();
            
            targetFinder.stop();
            scanlineStop();
        }
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Process the Gestures
        if (mSampleAppMenu != null && mSampleAppMenu.processEvent(event))
            return true;


        return mGestureDetector.onTouchEvent(event);
    }
    
    
    @Override
    public boolean doLoadTrackersData()
    {
        Log.d(LOGTAG, "initCloudReco");
        
        // Get the object tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
            .getTracker(ObjectTracker.getClassType());
        
        // Initialize target finder:
        TargetFinder targetFinder = objectTracker.getTargetFinder();
        
        // Start initialization:
        if (targetFinder.startInit(kAccessKey, kSecretKey))
        {
            targetFinder.waitUntilInitFinished();
        }
        
        int resultCode = targetFinder.getInitState();
        if (resultCode != TargetFinder.INIT_SUCCESS)
        {
            if(resultCode == TargetFinder.INIT_ERROR_NO_NETWORK_CONNECTION)
            {
                mInitErrorCode = UPDATE_ERROR_NO_NETWORK_CONNECTION;
            }
            else
            {
                mInitErrorCode = UPDATE_ERROR_SERVICE_NOT_AVAILABLE;
            }
                
            Log.e(LOGTAG, "Failed to initialize target finder.");
            return false;
        }
        
        return true;
    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        return true;
    }

    @Override
    public void onVuforiaResumed()
    {
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }
    
    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();

            mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

            vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            
            mUILayout.bringToFront();
            
            // Hides the Loading Dialog
            loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);
            
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            mSampleAppMenu = new SampleAppMenu(this, this, "Cloud Reco",
                mGlView, mUILayout, null);


            setSampleAppMenuSettings();
            
        } else
        {
            Log.e(LOGTAG, exception.getString());
            if(mInitErrorCode != 0)
            {
                showErrorMessage(mInitErrorCode,10, true);
            }
            else
            {
                showInitializationErrorMessage(exception.getString());
            }
        }
    }


    @Override
    public void onVuforiaStarted()
    {
        mRenderer.updateRenderingPrimitives();

        // Set camera focus mode
        if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO))
        {
            // If continuous autofocus mode fails, attempt to set to a different mode
            if(!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
            {
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
            }
        }

        showProgressIndicator(false);
    }

    public void showProgressIndicator(boolean show)
    {
        if (loadingDialogHandler != null)
        {
            if (show)
            {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
            }
            else
            {
                loadingDialogHandler
                        .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
            }
        }
    }
    
    
    @Override
    public void onVuforiaUpdate(State state)
    {
        // Get the tracker manager:
        TrackerManager trackerManager = TrackerManager.getInstance();
        
        // Get the object tracker:
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
            .getTracker(ObjectTracker.getClassType());
        
        // Get the target finder:
        TargetFinder finder = objectTracker.getTargetFinder();
        
        // Check if there are new results available:
        final int statusCode = finder.updateSearchResults();

if (!lightThread.checkForGoodLight() && (counter == 10 || counter % 100 == 0))
        {

            showToast("Warning, lighting may be insufficient");

        }

counter++;
       // Log.d("Light sensor", String.valueOf(counter) );

        
        // Show a message if we encountered an error:
        if (statusCode < 0)
        {
            
            boolean closeAppAfterError = (
                statusCode == UPDATE_ERROR_NO_NETWORK_CONNECTION ||
                statusCode == UPDATE_ERROR_SERVICE_NOT_AVAILABLE);
            
            showErrorMessage(statusCode, state.getFrame().getTimeStamp(), closeAppAfterError);
            
        } else if (statusCode == TargetFinder.UPDATE_RESULTS_AVAILABLE)
        {
            // Process new search results
            if (finder.getResultCount() > 0)
            {
                TargetSearchResult result = finder.getResult(0);
               String identite = result.getTargetName();
               String stationName = "";

                if (identite.contains("Station"))
                {
                    startOvThread(identite);
                    stationName = identite.substring(10);
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    }
                    catch (InterruptedException e){
                        e.printStackTrace();
                    }

                    if (tripsAcquired)
                    {
                        Intent i = new Intent(this, DisplayResultsOnScreen.class);
                        i.putExtra("result", ovThread.getStringWithAllTrips());
                        i.putExtra("stationName", stationName);
                        startActivity(i);
                        go = true;
                    }


                }

               /* if (tripsAcquired)
                {

                    tripsAcquired = false;
                }*/

                //showToast(identite);
              /*  try {
                    String escapedQuery = URLEncoder.encode(identite, "UTF-8"); ////open chrome
                    Uri uri = Uri.parse("http://www.google.com/#q=" + escapedQuery);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                catch (UnsupportedEncodingException u)
                {

                }*/



                // Check if this target is suitable for tracking:
                if (result.getTrackingRating() > 0)
                {
                    Trackable trackable = finder.enableTracking(result);
                    
                    if (mExtendedTracking)
                        trackable.startExtendedTracking();
                }
            }
        }

        if (useLocation && tripsAcquired)
        {


            /*try {
                TimeUnit.SECONDS.sleep(5);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }*/

            Intent i = new Intent(this, DisplayResultsOnScreen.class);
            i.putExtra("result", ovThread.getStringWithAllTrips());
            closestStation = closestStation.substring(10);
            i.putExtra("stationName", closestStation);
            startActivity(i);
            tripsAcquired=false;
            useLocation  =false;
        }

    }
    
    
    @Override
    public boolean doInitTrackers()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        
        return result;
    }
    
    
    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        // Start the tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
            .getTracker(ObjectTracker.getClassType());
        objectTracker.start();
        
        // Start cloud based recognition if we are in scanning mode:
        TargetFinder targetFinder = objectTracker.getTargetFinder();
        targetFinder.startRecognition();
        scanlineStart();
        mFinderStarted = true;
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
            .getTracker(ObjectTracker.getClassType());
        
        if(objectTracker != null)
        {
            objectTracker.stop();
            
            // Stop cloud based recognition:
            TargetFinder targetFinder = objectTracker.getTargetFinder();
            targetFinder.stop();
            scanlineStop();
            mFinderStarted = false;
            
            // Clears the trackables
            targetFinder.clearTrackables();
        }
        else
        {
            result = false;
        }
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());
        
        return result;
    }
    
    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    
    // This method sets the menu's settings
    private void setSampleAppMenuSettings()
    {
        SampleAppMenuGroup group;
        
        group = mSampleAppMenu.addGroup("", false);
        group.addTextItem("SWAN AR", -1);
        
        group = mSampleAppMenu.addGroup("", true);
        group.addSelectionItem("Activate location tracking",
            2, false);
        
        mSampleAppMenu.attachMenu();
    }
    
    
    @Override
    public boolean menuProcess(int command)
    {
        boolean result = true;
        
        switch (command)
        {
            case CMD_BACK:
                finish();
                break;
            
            case CMD_EXTENDED_TRACKING:
                TrackerManager trackerManager = TrackerManager.getInstance();
                ObjectTracker objectTracker = (ObjectTracker) trackerManager
                    .getTracker(ObjectTracker.getClassType());
                
                TargetFinder targetFinder = objectTracker.getTargetFinder();
                
                if (targetFinder.getNumImageTargets() == 0)
                {
                    result = true;
                }
                
                for (int tIdx = 0; tIdx < targetFinder.getNumImageTargets(); tIdx++)
                {
                    Trackable trackable = targetFinder.getImageTarget(tIdx);
                    
                    if (!mExtendedTracking)
                    {
                        if (!trackable.startExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                "Failed to start extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                "Successfully started extended tracking target");
                        }
                    } else
                    {
                        if (!trackable.stopExtendedTracking())
                        {
                            Log.e(LOGTAG,
                                "Failed to stop extended tracking target");
                            result = false;
                        } else
                        {
                            Log.d(LOGTAG,
                                "Successfully started extended tracking target");
                        }
                    }
                }
                
                if (result)
                    mExtendedTracking = !mExtendedTracking;
                
                break;

            case 2:

                mSampleAppMenu.hideMenu();
                mSampleAppMenu.hide();


                 useLocation = true;

                 closestStation = lookForClosestLocation();

                 if (closestStation != null)
                 {
                     closestStation = "Station_00" + closestStation;
                     startOvThread(closestStation);
                 }

              /*  String closestStation = lookForClosestLocation();
                closestStation = "Station_0" + closestStation;
                //String ovValue = getOVValue(closestStation);




                startOvThread(closestStation);
                closestStation = closestStation.substring(9);
                try {
                    TimeUnit.SECONDS.sleep(5);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }




                Intent i = new Intent(this, DisplayResultsOnScreen.class);
                i.putExtra("result", ovThread.getStringWithAllTrips());
                i.putExtra("stationName", closestStation);
                startActivity(i);

                if (ovThread.gettripsAcquired())
                {

                    ovThread.settripsAcquired(false);
                }

                break;*/
        }
        
        return result;
    }

    String lookForClosestLocation ()
    {

      // double currentLatitude = 52.340894;
      // double currentLongitude = 4.873153;
        double currentLatitude =locationThread.getLatitudeValue();
        double currentLongitude =locationThread.getLongitudeValue();


        String sName = "";
        double sLatitude, sLongitude;
        //Log.e("locationnn", String.valueOf(currentLatitude) + " " + String.valueOf(currentLongitude));

        try {
            DataInputStream textFileStream = new DataInputStream(getAssets().open(String.format("stopsForLocationVVVX.xml")));
            Scanner scanner = new Scanner(textFileStream);
           // scanner.nextLine(); //first line
            while (scanner.hasNextLine() && continueLooking)
            {
                String line = scanner.nextLine();
                Scanner scan = new Scanner(line);
                scan.useDelimiter(",");
                scan.next();
                scan.next();
                scan.next();

                sName = scan.next();
                sName = sName.substring(0,sName.length()-1);
                sName = sName.replace(" ", "");

                String test1 = scan.next();
                String test2 = scan.next();

                if (isNumeric(test1) && isNumeric(test2))
                {
                    sLatitude = Double.parseDouble(test1);
                    sLongitude = Double.parseDouble(test2);


              /*  sLatitude = scan.nextDouble();
                sLongitude = scan.nextDouble();*/



                   /* float[] results = new float[1];
                    Location.distanceBetween(currentLatitude, currentLongitude, sLatitude, sLongitude, results);
                    float distanceInMeters = results[0];
                    Log.e("disstaa" , String.valueOf(distanceInMeters));
                    boolean isWithin100m = distanceInMeters < 100;*/




                /*if (Math.abs(currentLatitude-sLatitude) <= 0.001 && Math.abs(currentLongitude-sLongitude)<=0.001)
                {
                    continueLooking = false;
                    Log.e("sname" , sName);

                   // station


                    return sName;


                }*/
                double distance = measure(currentLatitude, currentLongitude,sLatitude,sLongitude);
                if ( distance <= 550)//in meters
                {
                    continueLooking = false;
                    Log.e("sname" , sName);

                    return sName;
                }


                }

            }

            // ovThread = new SwanThread(idOV, this, sc,);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.e("near NOWHERE", "NEAR NOWHERE");
        Toast.makeText(this, "No Station where you are", Toast.LENGTH_LONG).show();

        return null;
    }


    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    double  measure(double lat1, double lon1, double lat2, double lon2){
        double R = 6378.137; // Radius of earth in KMs
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }



    private void scanlineStart() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanLine.setVisibility(View.VISIBLE);
                scanLine.setAnimation(scanAnimation);
            }
        });
    }

    private void scanlineStop() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scanLine.setVisibility(View.GONE);
                scanLine.clearAnimation();
            }
        });
    }

    private void showToast(String text)
    {
        Toast.makeText(CloudReco.this, text, Toast.LENGTH_LONG).show();
    }

}
