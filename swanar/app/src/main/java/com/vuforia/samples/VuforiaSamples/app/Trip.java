package com.vuforia.samples.VuforiaSamples.app;

public class Trip {

    String line ;
    String time;
    String destination;
    String type;
    String company;
    String concatenation = "";


    Trip (String line, String time, String destination, String type, String company)
    {
        this.line = line;
        this.time = time;
        this.destination = destination;
        this.type = type;
        this.company = company;
    }


    public String getCompany() {
        return company;
    }

    public String getLine() {
        return line;
    }

    public String getTime() {
        return time;
        }

    public String getDestination() {
        return destination;
    }

    public String getType() {
        return type;
    }

    public String getConcatenation() {

        return concatenation;
    }
}
