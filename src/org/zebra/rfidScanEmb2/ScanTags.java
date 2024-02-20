package org.zebra.rfidScanEmb2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import com.mot.rfid.api3.*;

public class ScanTags {
    static Timer timer;
    static HashMap<String, Long> scannedTags = new HashMap<String, Long>();
    static boolean scannedTagsWriting = false;
    static boolean buzzerActivated = false;
    static String urlEndPoint;
    public boolean isBuzzerActivated() {
        return buzzerActivated;
    }

    public void setBuzzerActivated(boolean buzzerActivated) {
        ScanTags.buzzerActivated = buzzerActivated;
    }

    public static void mainScan(RFIDReader reader)
            throws InterruptedException, InvalidUsageException, OperationFailureException {
        //Check if the reader is still connected after main method pass it 
        System.out.println("Is Reader Connected?\n" + reader.isConnected());
        int green = 3;
        int yellow = 2;
        int red = 1;
        int alarm = 4;
        //Load endpoint url and asset tags that need to be monitored
        ReaderFile readerFile = new ReaderFile();          
        List<String> alertOf = new ArrayList<String>();
        alertOf = readerFile.fillAsset();
        System.out.println("Assets loaded "+alertOf);
        urlEndPoint = readerFile.endPointUrl().get(0);
        System.out.println(urlEndPoint);
        //Set up the GPO port configuration
        GPO_PORT_STATE enable = GPO_PORT_STATE.TRUE;
        GPO_PORT_STATE disable = GPO_PORT_STATE.FALSE;
        reader.Config.GPO.setPortState(green, enable);
        // // Subscribe required status notification
        reader.Events.setInventoryStartEvent(true);
        reader.Events.setInventoryStopEvent(true);
        reader.Events.setAccessStartEvent(true);
        reader.Events.setAccessStopEvent(true);

        // enables tag read notification. if this is set to false, no tag read
        // notification will be send
        reader.Events.setTagReadEvent(true);
        reader.Events.setAntennaEvent(true);
        reader.Events.setBufferFullEvent(true);
        reader.Events.setBufferFullWarningEvent(true);
        reader.Events.setGPIEvent(true);
        reader.Events.setReaderDisconnectEvent(true);
        TagStorageSettings tagStorageSettings = reader.Config.getTagStorageSettings();
        tagStorageSettings.enableAccessReports(true);
        reader.Config.setTagStorageSettings(tagStorageSettings);
        
        //Create an array to store tags 
        TagData[] tags = null;
        
        //set the initial configuration for the GOP ports
        reader.Config.GPO.setPortState(green, enable);
        reader.Config.GPO.setPortState(red, disable);
        reader.Config.GPO.setPortState(alarm, disable);
        reader.Config.GPO.setPortState(yellow, disable);

        try {
            
            // for (int element: reader.ReaderCapabilities.getTransmitPowerLevelValues()) {
            //   System.out.println("Max power level value "+ element);
            // }
              
            //  for (int element: reader.ReaderCapabilities.getReceiveSensitivityValues()) {
            //     System.out.println("Receive Sensitivity value "+ element);
            // }
            //print RFID reader information by console
            System.out
                    .println("GET number of antennas supported  " + reader.ReaderCapabilities.getNumAntennaSupported());

            System.out.println("GET ID reader  " + reader.ReaderCapabilities.ReaderID.getID());

            System.out.println("getRFModeTableInfo " + reader.ReaderCapabilities.RFModes.getRFModeTableInfo(0)
                    .getRFModeTableEntryInfo(1).getMaxTariValue());
            //Set up configuration for antennas 
            for (int element : reader.Config.Antennas.getAvailableAntennas()) {
                Antennas.AntennaRfConfig antennaRfConfig = reader.Config.Antennas.getAntennaRfConfig(element);
                antennaRfConfig.setTransmitPowerIndex(200);
                antennaRfConfig.setrfModeTableIndex(0);
                antennaRfConfig.setTari(0);
                antennaRfConfig.setReceiveSensitivityIndex(0);
                reader.Config.Antennas.setAntennaRfConfig(element, antennaRfConfig);
            }
            //save the first eight chars of the tag, it will be used to determine if the tag belongs to the site
            String scannerTypeCode;
            //save all chars of the tag, this value will saved on the data base 
            String scannerCode;
            //loop to keep reading
            while (true) {
                //start caprturing tags for one sec
                reader.Actions.Inventory.perform();
                Thread.sleep(1000);
                //stop capturing tags
                reader.Actions.Inventory.stop();
                //save until 3000 tags in the array
                tags = reader.Actions.getReadTags(3000);
                //determine if the antenna detected any tag
                if (tags != null) {
                    for (int i = 0; i < tags.length; i++) {
                        //get the tag id
                        scannerTypeCode = tags[i].getTagID().substring(0, 8);
                        scannerCode = tags[i].getTagID();
                        System.out.println("Antenna ID "+tags[i].getAntennaID()+" Increment loop "+i+" Tag ID "+tags[i].getTagID());
                        //Determine if the tags are a chased asset
                        if (alertOf.contains(scannerTypeCode)) {
                            callUrl web;
                            Alarm sound;
                            LogWriter write;
                            //determine is the tag was seen in the last 30 sec, so that AA has 30 sec to remove the tag from antenna area coverage
                            if (!wasFiveMinutesBefore(scannerCode)) {
                                //put tag on a list to see when the tag was seen
                                scannedTags.put(scannerCode, System.currentTimeMillis());
                                //create object to manage the buzzer, lights and web server
                                sound = new Alarm(reader, enable, disable); 
                                web = new callUrl();
                                //if buzzer is not activate it starts thread to activate alarm
                                if (!buzzerActivated) {
                                    buzzerActivated = true;
                                    sound.start();
                                } 
                                //set the url endpoint and tag found                              
                                web.setUrlEndPoint(urlEndPoint);
                                web.setAssetCode(scannerCode);
                                //send http request to the endpoint and it starts thread
                                if (!web.isAlive()) {
                                     web.start();                                    
                                }
                                //save data in log text 
                                write = new LogWriter();
                                write.setTags(tags[i].getTagID());
                                write.setType("Scanner");
                                if (!write.isAlive()) {
                                    write.start();
                                }
                            }
                        }                        
                    }
                }                
            }

        } catch (OperationFailureException ex) {
            System.out.println((" Antenna configuration failed " + ex.getVendorMessage()));
        } catch (IllegalThreadStateException e) {
            // TODO Auto-generated catch block
            System.out.println("In the catch Ileagalthread on the class ScanTags");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occured forcing the temination of this program.");
            System.out.println(e);
            try {
                reader.Config.GPO.setPortState(green, enable);
                reader.Config.GPO.setPortState(red, disable);
                reader.Config.GPO.setPortState(alarm, disable);
                reader.Config.GPO.setPortState(yellow, disable);
                reader.Actions.Inventory.stop();
                reader.disconnect();
                System.exit(1);
                Runtime.getRuntime().halt(1);

            } catch (Exception e2) {
                System.out.println("Unable to stop reader and set LED status. Terminating program.");
                try {
                    reader.disconnect();
                } catch (Exception e3) {
                    System.out.println("Critial Error.");
                    System.out.println(e3);
                    System.exit(1);
                    Runtime.getRuntime().halt(1);
                }
                System.out.println(e2);
                System.exit(1);
                Runtime.getRuntime().halt(1);

            }
        }
    }
    public static Boolean wasFiveMinutesBefore(String scannerCode) {
        // reader.Events.set;
        if (!scannedTags.containsKey(scannerCode))
            return false;
        Long timeStamp = scannedTags.get(scannerCode);
        // 1 * 20 * 1000 = 20 seconds
        if ((System.currentTimeMillis() - timeStamp) < 2 * 21600 * 1000) {
            return true;
        }
        return false;
    }
}