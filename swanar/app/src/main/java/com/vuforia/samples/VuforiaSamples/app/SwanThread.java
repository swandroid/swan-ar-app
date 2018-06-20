package com.vuforia.samples.VuforiaSamples.app;

import android.app.ActivityManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import org.json.*;

import com.google.android.gms.location.LocationRequest;
import com.vuforia.samples.VuforiaSamples.app.CloudRecognition.CloudReco;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import interdroid.swancore.swanmain.ExpressionManager;
import interdroid.swancore.swanmain.SwanException;
import interdroid.swancore.swanmain.ValueExpressionListener;
import interdroid.swancore.swansong.ExpressionFactory;
import interdroid.swancore.swansong.ExpressionParseException;
import interdroid.swancore.swansong.TimestampedValue;
import interdroid.swancore.swansong.ValueExpression;

import static com.vuforia.samples.VuforiaSamples.app.CloudRecognition.CloudReco.go;

public class SwanThread extends Thread{


    private String id;
    private Context context;
    String lightValue;
    double latitudeValue;
    double longitudeValue;
    String ovValue;
    boolean passLight = false;
    boolean passLocation = false;
    String stringWithAllTrips = "";

    public   SwanThread(String id, Context context)
    {
        this.id = id;
        this.context = context;
        run();

    }

    public   SwanThread(String id, Context context, String ovValue)
    {
        this.id = id;
        this.context = context;
        this.ovValue = ovValue;
        run();
    }

    public void setOvValue(String ovValue) {
        this.ovValue = ovValue;
    }

    public void run() {

 registerSWANSensor();



    }

    public String getStringWithAllTrips() {
        return stringWithAllTrips;
    }

  /* public boolean gettripsAcquired ()
    {
        return tripsAcquired;
    }
    public void settripsAcquired (boolean bool)
    {
        tripsAcquired = bool;
    }*/

    public double getLatitudeValue() {
        return latitudeValue;
    }

    public double getLongitudeValue() {
        return longitudeValue;
    }

    public void registerSWANSensor(){

         String myExpression = "";

     if (id.equals("light"))
     {
       //   myExpression = "self@light:lux{ANY,0}";
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
                         passLight = true;
                        //Log.d(id, lightValue);
                     }

                 }
             });
         } catch (SwanException e) {
             e.printStackTrace();
         } catch (ExpressionParseException e) {
             e.printStackTrace();
         }

     }
     else if (id.equals("ov"))
     {
         myExpression = "self@ov:stopareacode?value='" + ovValue + "'{ANY,1000}";
        // myExpression = "self@ov:stopareacode?value='07404'{ANY,1000}";

         go = false;

         try {
             ExpressionManager.registerValueExpression(context, id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                 @Override
                 public void onNewValues(String id, TimestampedValue[] newValues) {
                     if (newValues.length > 0) {
                         String str;
                         str = String.valueOf(newValues[0]);
                        stringWithAllTrips = parseTheJson(String.valueOf(newValues[0]));


                         CloudReco.tripsAcquired = true;
                         Log.e(id, str);






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
         myExpression = "self@location:location?provider='network'{ANY,0}";



         try {
             ExpressionManager.registerValueExpression(context, id, (ValueExpression) ExpressionFactory.parse(myExpression), new ValueExpressionListener() {
                 @Override
                 public void onNewValues(String id, TimestampedValue[] newValues) {
                     if (newValues.length > 0) {


                         double[] locationValues = (double[]) newValues[0].getValue();
                         passLocation = true;

                         latitudeValue = locationValues[0];
                         longitudeValue = locationValues[1];
                         Log.d(id, String.valueOf(locationValues[0]));
                         Log.d(id, String.valueOf(locationValues[1]));

                     }
                 }
             });
         } catch (SwanException e) {
             e.printStackTrace();
         } catch (ExpressionParseException e) {
             e.printStackTrace();
         }

     }
        }


        String parseTheJson(String string)   /////1
        {
            String result = "";
            int compteur = 0;
            /*Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
            String currentPhoneTime = String.valueOf(calendar.get(Calendar.YEAR)) + " " + String.valueOf(calendar.get(Calendar.MONTH)+1) + " " + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
                    + " " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + " " + String.valueOf(calendar.get(Calendar.MINUTE)) + " " + String.valueOf(calendar.get(Calendar.SECOND));*/

            try
            {
                JSONObject obj0 = new JSONObject(string);
                JSONObject obj1 = obj0.getJSONObject(String.valueOf(ovValue));

                Iterator<?> keys = obj1.keys(); //LIST of 4 STOPS

                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if ( obj1.get(key) instanceof JSONObject ) {
                        JSONObject json = (JSONObject) obj1.get(key);
                        result+=parseStops(json);
                    }
                }
            }
catch (JSONException e){
                e.printStackTrace();
}

            Log.e("resullltt" , result);

            return result;
        }





        String parseStops (JSONObject obj)   ////2
        {
            String result = "";
            List <Trip> trips = new ArrayList<>();
            try{
                JSONObject obj0 = obj.getJSONObject("Passes");

                Iterator<?> keys = obj0.keys(); //LIST of 4 STOPS

                while( keys.hasNext() ) {
                    String key = (String)keys.next();
                    if ( obj0.get(key) instanceof JSONObject ) {
                        JSONObject json = (JSONObject) obj0.get(key);

                      Trip trip = getInfoFromEachTrip(json);
                      trips.add(trip);
                    }
                }

            } catch (JSONException e){
            e.printStackTrace();
        }



            result = getRelevantTrips(trips);




            return result;
        }


       String getRelevantTrips(List <Trip> trips)   /////3
        {



            if (trips.size() == 0)
            {
                return "";
            }

            int counter = 0;
            while (!ordered(trips))
            {
                Trip savedTrip;
                String str1 = trips.get(counter).getLine();
                str1 = str1.replaceAll("[^\\d.]", "");
String str2 = "-1";

                if (counter < trips.size()-1)
                {
                    str2 = trips.get(counter+1).getLine();
                    str2 = str2.replaceAll("[^\\d.]", "");
                }


                if (counter < trips.size()-1 && Integer.parseInt(str1) > Integer.parseInt(str2))
                {
                    savedTrip = trips.get(counter);
                    trips.set(counter,trips.get(counter+1));
                    trips.set(counter+1,savedTrip);
                }
                if (counter < trips.size()-1)
                {
                counter++;
                }
                else
                {
                    counter=0;
                }

            }


            List <String> schedules = new ArrayList<>();

            String firstDestination = trips.get(0).getDestination();
            String firstLine = trips.get(0).getLine();
            String firstTime = trips.get(0).getTime();

            for (int i = 1; i < trips.size();i++)
            {

                if (!trips.get(i).getLine().equals(firstLine))
                {
                    schedules.add(firstLine + " " + firstDestination + " " + firstTime);
                    firstDestination = trips.get(i).getDestination();
                    firstLine = trips.get(i).getLine();
                    firstTime = trips.get(i).getTime();
                }
                else
                {
                    Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
                    String currentPhoneTime = String.valueOf(calendar.get(Calendar.YEAR)) + " " + String.valueOf(calendar.get(Calendar.MONTH)+1) + " " + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
                            + " " + String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) + " " + String.valueOf(calendar.get(Calendar.MINUTE)) + " " + String.valueOf(calendar.get(Calendar.SECOND));

                    Calendar date0 = new GregorianCalendar();
                    Calendar date1 = new GregorianCalendar();
                    Calendar date2 = new GregorianCalendar();

                    Scanner scanner = new Scanner(currentPhoneTime);
                    date0.set(scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());


                    scanner = new Scanner(firstTime);
                    date1.set(scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());

                    scanner = new Scanner(trips.get(i).getTime());
                    date2.set(scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt(),scanner.nextInt());

                    Date d0 = date0.getTime();
                    Date d1 = date1.getTime();
                    Date d2 = date2.getTime();

                    int comparisonResult01 = d0.compareTo(d1);
                    int comparisonResult02 = d0.compareTo(d2);
                    int comparisonResult12 = d1.compareTo(d2);

                    if (comparisonResult01 <= 0 && comparisonResult02 <=0)
                    {
                        if (comparisonResult12 <= 0)
                        {
                            //do nothing
                        }
                        else
                        {
                            firstTime = trips.get(i).getTime();
                        }
                    }
                    else if (comparisonResult01 <= 0 && comparisonResult02 > 0)
                    {
                        //do nothing
                    }
                    else if (comparisonResult01 > 0 && comparisonResult02 <=0)
                    {
                        firstTime = trips.get(i).getTime();
                    }
                    else
                    {
                        //do nothing
                    }

            }

        }
            schedules.add(firstLine + " " + firstDestination + " " + firstTime);

            String result = "";

            for (int i = 0 ; i < schedules.size();i++)
            {
                result += schedules.get(i) + "\n";
            }

            return result;
        }


        public boolean ordered (List <Trip> trips)
        {

            for (int i = 0; i < trips.size()-1;i++)
            {

                String str1 = trips.get(i).getLine();
                String str2 = trips.get(i+1).getLine();
                str1 = str1.replaceAll("[^\\d.]", "");
                str2 = str2.replaceAll("[^\\d.]", "");

                if (Integer.parseInt(str1) > Integer.parseInt(str2)) //careful with lines named N30 etc. non numbers
                {
                    return false;
                }
            }

            return true;

        }


    Trip getInfoFromEachTrip(JSONObject json)
    {
        String line = "";
        String time = "";
        String destination = "";
        String type = "";
        String company = "";

      try
      {

          line = (String) json.get("LinePublicNumber");
          time = (String) json.get("ExpectedDepartureTime");

          for (int i = 0; i < time.length();i++) //reformat time
          {
              if (!Character.isDigit(time.charAt(i)))
              {
                  if (i == time.length()-1)
                  {
                      time = time.substring(0,i);
                  }
                  else
                  {
                      time = time.substring(0,i) + " " + time.substring(i+1);
                  }
              }
          }

          destination = (String) json.get("DestinationName50");
          type = (String) json.get("TransportType");
          company = (String) json.get("OperatorCode");



          //Log.e("line is " , (String) json.get("LinePublicNumber"));
      }
      catch (JSONException e){
          e.printStackTrace();

    }



        return new Trip(line, time, destination, type, company);
    }

    public void unregisterSWANSensor(){

        ExpressionManager.unregisterExpression(context, String.valueOf(id));


    }


    public boolean checkForGoodLight ()
    {
        if (passLight)
        {
            if (Double.parseDouble(lightValue) < 8)
        {
            return false;
        }
        }

return true;
    }



    public String [] returnNearStations ()
    {
        if (passLocation)
        {

        }

      //  AssetManager assetMgr = this.getAssets();










        return null;
    }


}
