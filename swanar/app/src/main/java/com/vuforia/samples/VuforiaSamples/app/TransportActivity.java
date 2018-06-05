package com.vuforia.samples.VuforiaSamples.app;


import android.os.Environment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class TransportSensor  {

    ArrayList<Station> stations = new ArrayList<Station>();

    private XmlPullParserFactory xmlFactoryObject;
String xmlpath = Environment.getExternalStorageDirectory() + "/swanARpics/stations.xml";

    TransportSensor ()
    {

    }






    public void parseXMLAndStoreIt(XmlPullParser myParser) {

       /* List<String> a = new ArrayList<String>(); //frame debut
        List<String> b = new ArrayList<String>(); //temps debut
        List<String> c = new ArrayList<String>(); //frame fin
        List<String> d = new ArrayList<String>();// temps fin

        List<Integer> aa = new ArrayList<Integer>(); //frame debut
        List<Integer> ba = new ArrayList<Integer>(); //temps debut
        List<Integer> ca = new ArrayList<Integer>(); //frame fin
        List<Integer> da = new ArrayList<Integer>();// temps fin


        List<Integer> f = new ArrayList<Integer>(); //nombre frames
        List<Integer> g = new ArrayList<Integer>(); //temps pfx*/
        boolean sampe = false;
        boolean timee = false;

        int event;
        String text="";
        String tagga = "";
        String taggb = "";


        try {
            event = myParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT) {


                switch (event){
                    case XmlPullParser.START_TAG:

                        


                        break;

                    case XmlPullParser.TEXT:
                        text = myParser.getText();


                        break;


                    case XmlPullParser.END_TAG:







                        break;
                }




                event = myParser.next();
            }




            parsingComplete = false;

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void fetchXML(){
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    File file = new File(xmlpath);
                    InputStream stream = new FileInputStream(file);



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
    }


}
