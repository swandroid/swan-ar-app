package com.vuforia.samples.VuforiaSamples.app;


import android.app.Activity;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class TransportActivity extends Activity {

    ArrayList<Station> stations = new ArrayList<Station>();

   // private XmlPullParserFactory xmlFactoryObject;
//String xmlpath = "android.resource://com.your.package/raw/xml/stations.xml";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
fetchXml();
    }








    public void fetchXml() {


        XmlResourceParser xpp=getResources().getXml(R.xml.stations);


        int event;

        String newstation = "station";
        String stationname = "stationname";
        String stationlatitude = "stationlatitude";
        String stationlongitude = "stationlongitude";
        String stationurl = "stationurl";

        boolean sn = false;
        boolean sla = false;
        boolean slo = false;
        boolean u = false;


        String text="";
        String name = "";
        String latitude = "";
        String longitude = "";
        String URL = "";


        try {
            event = xpp.getEventType();
            while (event != XmlResourceParser.END_DOCUMENT) {
                switch (event){
                    case XmlPullParser.START_TAG:

                        if (xpp.getName().equals(stationname))
                        {
                            sn = true;
                        }
                        else if (xpp.getName().equals(stationlatitude))
                        {
                            sla = true;
                        }
                        else if (xpp.getName().equals(stationlongitude))
                        {
                            slo = true;
                        }
                        else if (xpp.getName().equals(stationurl))
                        {
                            u = true;
                        }
                        break;

                    case XmlResourceParser.TEXT:
                        text = xpp.getText();

                        if (sn)
                        {
                            name = text;
                        }
                        else if (sla)
                        {
                            latitude = text;
                        }
                        else if (slo)
                        {
                            longitude = text;
                        }
                        else if (u)
                        {
                            URL = text;
                        }
                        break;
                    case XmlResourceParser.END_TAG:
                        if (xpp.getName().equals(stationname))
                        {
                            sn = false;
                        }
                        else if (xpp.getName().equals(stationlatitude))
                        {
                            sla = false;
                        }
                        else if (xpp.getName().equals(stationlongitude))
                        {
                            slo = false;
                        }
                        else if (xpp.getName().equals(stationurl))
                        {
                            u = false;
                        }
                        else if (xpp.getName().equals(newstation))
                        {
                            stations.add(new Station(name,latitude,longitude,URL));
                        }
                        break;
                }
                event = xpp.next();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

stations = stations;





    }




 /*   public void fetchXML(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    File file = getResources().getXml(R.xml.stations);
                    InputStream stream = new FileInputStream(file);

                    InputStream is = getResources().openRawResource(R.xml.stations);

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser myparser = xmlFactoryObject.newPullParser();

                    myparser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    myparser.setInput(stream, null);

                    parseXMLAndStoreIt(myparser);
                    stream.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }*/


}
