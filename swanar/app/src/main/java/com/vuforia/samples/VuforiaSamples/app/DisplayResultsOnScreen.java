package com.vuforia.samples.VuforiaSamples.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.R;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.TimeZone;

public class DisplayResultsOnScreen extends Activity {

    String result = "";
    String stationName= "";
    TextView textView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayresults);

        Bundle b = getIntent().getExtras();
        result = b.getString("result");
        stationName = b.getString("stationName");

        textView = (TextView) findViewById(R.id.textView);


        result = parseResult(result);

        String str = "You are at station : " + stationName + "\n\n" + "These are the next trips from this station : " + "\n\n" + result;
        textView.setText(str);





    }

   String parseResult (String string)
    {

        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
        String currentYear = String.valueOf(calendar.get(Calendar.YEAR));


        String result = "";
        Scanner scanner = new Scanner(string);

        String name = "";
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            Scanner scan = new Scanner(line);

                String local = "Line ";
                local+=scan.next() + " "; //line number
                local+= "to";
                while(!name.equals(currentYear))
                {
                    local+= name+" ";
                    name = scan.next();
                }
                scan.next();//month;
                scan.next();//day

                local += "leaving at ";
                local += scan.next() + ":" + scan.next() + ":" + scan.next(); //hour min sec

            result += local + "\n";
            name="";
        }

        return result;
    }

}
