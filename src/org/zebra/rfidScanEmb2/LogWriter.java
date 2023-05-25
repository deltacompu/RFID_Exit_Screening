package org.zebra.rfidScanEmb2;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LogWriter extends Thread  {

    
    Date date;
	String currentTime, currentDate, dateFormatString = "EEE, MMM d, yy", timeFormatString = "hh:mm:ss a";
	DateFormat timeFormat = new SimpleDateFormat(timeFormatString), dateFormat = new SimpleDateFormat(dateFormatString);
    String tags;
    String type;
   
    
    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTags() {
        return tags;
    }

    public String getType() {
        return type;
    }
    @Override
    public synchronized void run() {
        date = new Date();
		currentTime = timeFormat.format(date);
		currentDate = dateFormat.format(date);
        System.out.println("Writer "+Thread.currentThread());
        try {
            FileWriter fWriter =  new FileWriter("/mnt/data/app/TagsFound.txt", true);
            fWriter.write(currentDate + " (" +  currentTime + ")"+ type +"Tag found! --" + tags + "--\n");
            System.out.println("\nADDED TAG");
            fWriter.close();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println( "Unable to write to log. Error: " + e + "\n\n");
            try {
                FileWriter fWriter =  new FileWriter("/mnt/data/app/TagsFound.txt");
                fWriter.write(currentDate + " (" +  currentTime + ") Scanner Tag found! --" + tags + "--\n");
                fWriter.close();
            } catch (Exception z) {
                // TODO: handle exception
                System.out.println("Im deleting this whole program " + z);
            }
        }
        System.out.println("Terminating thread");
    }

    public void addNewTagRadio(String tags) throws InterruptedException{
        date = new Date();
		currentTime = timeFormat.format(date);
		currentDate = dateFormat.format(date);
        try {
            FileWriter fWriter =  new FileWriter("/mnt/data/app/TagsFound.txt", true);
            fWriter.write(currentDate + " (" +  currentTime + ") Radio Tag found!--" + tags + "--\n");
            System.out.println("\nADDED TAG");
            fWriter.close();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(" Unable to write to log. Error: " + e + "\n\n");
            try {
                FileWriter fWriter =  new FileWriter("/mnt/data/app/TagsFound.txt");
                fWriter.write(currentDate + " (" +  currentTime + ") Radio Tag found!--" + tags + "--\n");
                fWriter.close();
            } catch (Exception z) {
                // TODO: handle exception
                System.out.println("Im deleting this whole program " + z);
            }
        }
    }
}
