package com.vuforia.samples.VuforiaSamples.app;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.vuforia.samples.VuforiaSamples.app.CloudRecognition.CloudReco;

import java.util.List;
import java.util.Scanner;

import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swanmain.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;

public class SwanThread extends Thread{


    private String id;
    private Context context;
    String lightValue;
    String locationValue;
    boolean pass = false;

 public   SwanThread(String id, Context context)
    {
        this.id = id;
        this.context = context;
        run();
    }

    public void run() {

 registerSWANSensor();



    }


    public void registerSWANSensor(){

         String myExpression = "";

     if (id.equals("light"))
     {
          myExpression = "self@light:lux{ANY,0}";


         try {
             ExpressionManager.registerValueExpression(context, id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                 @Override
                 public void onNewValues(String id, TimestampedValue[] newValues) {
                     if (newValues.length > 0) {
                         String str;
                         str = String.valueOf(newValues[0]);
                         Scanner scanner = new Scanner(str);
                         scanner.useDelimiter(" ");
                         lightValue = scanner.next();
                         pass = true;
                         Log.d(id, lightValue);
                     }

                 }
             });
         } catch (SwanException e) {
             e.printStackTrace();
         } catch (ExpressionParseException e) {
             e.printStackTrace();
         }

     }
     else if (id.equals("location"))
     {
          myExpression = "self@location:location{ANY,0}";

         try {
             ExpressionManager.registerValueExpression(context, id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                 @Override
                 public void onNewValues(String id, TimestampedValue[] newValues) {
                     if (newValues.length > 0) {
                         String str;
                         str = String.valueOf(newValues[0]);
                         Log.d(id, str);



                     }
                 }
             });
         } catch (SwanException e) {
             e.printStackTrace();
         } catch (ExpressionParseException e) {
             e.printStackTrace();
         }


         }
        //  String myExpression = "self@light:lux{ANY,0}";

      /*try {
            ExpressionManager.registerValueExpression(context, id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                @Override
                public void onNewValues(String id, TimestampedValue[] newValues) {
                    if (newValues.length > 0) {
                        String str;
                        str = String.valueOf(newValues[0]);
                        Scanner scanner = new Scanner(str);
                        scanner.useDelimiter(" ");
                        lightValue = scanner.next();
                        pass = true;
                        Log.d(id, lightValue);
                    }

                }
            });
        } catch (SwanException e) {
            e.printStackTrace();
        } catch (ExpressionParseException e) {
            e.printStackTrace();
        }*/





        }
    public void unregisterSWANSensor(){

        ExpressionManager.unregisterExpression(context, String.valueOf(id));


    }


    public boolean checkForGoodLight ()
    {
        if (pass)
        {
            if (Double.parseDouble(lightValue) < 15)
        {
            return false;
        }
        }

return true;
    }



}
