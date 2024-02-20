package org.zebra.rfidScanEmb2;

import com.mot.rfid.api3.*;

public class Alarm extends Thread{
    //Varibles
    int green = 3;
    int yellow = 2;
    int red = 1;
    int alarm = 4;
    String hostname;
    RFIDReader reader;
    GPO_PORT_STATE enable;
    GPO_PORT_STATE disable;
    Boolean buzzerActivated = false;
    ScanTags scan = new ScanTags();

    
    public Boolean getBuzzerActivated() {
        return buzzerActivated;
    }
    public void setBuzzerActivated(Boolean buzzerActivated) {
        this.buzzerActivated = buzzerActivated;
    }
    public Alarm( RFIDReader reader, GPO_PORT_STATE enable, GPO_PORT_STATE disable) throws InterruptedException{
       this.reader = reader; this.enable = enable; this.disable = disable;
    }
    @Override
    public void run() {
        alarmSound();
    }

    public synchronized void alarmSound(){
        try {           
            reader.Config.GPO.setPortState(red, enable);
            reader.Config.GPO.setPortState(alarm, enable);
            System.out.println("Buzzer activate");
            Thread.sleep(4000);            
            reader.Config.GPO.setPortState(red, disable);
            reader.Config.GPO.setPortState(alarm, disable);
            System.out.println("Buzzer desactivated");
            scan.setBuzzerActivated(false);
        } catch (InvalidUsageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationFailureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        catch (IllegalThreadStateException e) {
            // TODO Auto-generated catch block            
            e.printStackTrace();
        }
        System.out.println("Terminating thread alarmSound"); 
    }
}