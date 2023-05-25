package org.zebra.rfidScanEmb2;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

    public boolean isBuzzerActivated() {
        return buzzerActivated;
    }

    public void setBuzzerActivated(boolean buzzerActivated) {
        this.buzzerActivated = buzzerActivated;
    }

    public static void mainScan(RFIDReader reader)
            throws InterruptedException, InvalidUsageException, OperationFailureException {

        System.out.println("Is Reader Connected?\n" + reader.isConnected());
        int green = 3;
        int yellow = 2;
        int red = 1;
        int alarm = 4;

        // Use IP address of reader when you are running this code here
        // String hostname = "169.254.169.229";
        // String hostname = "169.254.10.1";
        // String hostname = "10.244.3.26";        
        List<String> alertOf = new ArrayList<String>();

        alertOf.add("ABEF0000");
        alertOf.add("E2003412");
        alertOf.add("E2801191");

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

        TagData[] tags = null;
        ArrayList<String> deafen = new ArrayList<String>(100);
        reader.Config.GPO.setPortState(green, enable);
        reader.Config.GPO.setPortState(red, disable);
        reader.Config.GPO.setPortState(alarm, disable);
        reader.Config.GPO.setPortState(yellow, disable);

        try {
            /**
             * for (int element: reader.ReaderCapabilities.getTransmitPowerLevelValues()) {
             * System.out.println("Max power level value "+ element);
             * }
             * 
             * for (int element: reader.ReaderCapabilities.getReceiveSensitivityValues()) {
             * System.out.println("Receive Sensitivity value "+ element);
             * }
             **/
            System.out
                    .println("GET number of antennas supported  " + reader.ReaderCapabilities.getNumAntennaSupported());

            System.out.println("GET ID reader  " + reader.ReaderCapabilities.ReaderID.getID());

            System.out.println("getRFModeTableInfo " + reader.ReaderCapabilities.RFModes.getRFModeTableInfo(0)
                    .getRFModeTableEntryInfo(1).getMaxTariValue());

            for (int element : reader.Config.Antennas.getAvailableAntennas()) {
                Antennas.AntennaRfConfig antennaRfConfig = reader.Config.Antennas.getAntennaRfConfig(element);
                antennaRfConfig.setTransmitPowerIndex(200);
                antennaRfConfig.setrfModeTableIndex(0);
                antennaRfConfig.setTari(0);
                antennaRfConfig.setReceiveSensitivityIndex(0);
                reader.Config.Antennas.setAntennaRfConfig(element, antennaRfConfig);
                // System.out.println("Antenna properties
                // "+reader.Config.Antennas.AntennaProperties(element).getIndex());
            }

            while (true) {

                reader.Actions.Inventory.perform();
                Thread.sleep(1000);
                reader.Actions.Inventory.stop();
                tags = reader.Actions.getReadTags(3000);
                //System.out.println("Awaiting for more tags");

                if (tags != null) {

                    for (int i = 0; i < tags.length; i++) {
                        String scannerTypeCode = tags[i].getTagID().substring(0, 8);
                        String scannerCode = tags[i].getTagID();
                        // System.out.println("Antenna ID "+i+" "+tags[i].getAntennaID());
                        // System.out.println("Channel ID "+i+" "+tags[i].getChannelIndex());
                        if (alertOf.contains(scannerTypeCode)) {
                            Alarm sound = new Alarm(reader, enable, disable);                           
                            if (!buzzerActivated) {
                                buzzerActivated = true;
                                sound.start();
                            }                          

                            if (!wasFiveMinutesBefore(scannerCode)) {
                                scannedTags.put(scannerCode, System.currentTimeMillis());
                                callUrl web = new callUrl();
                                web.setAssetCode(scannerCode);
                                if (!web.isAlive()) {
                                    web.start();                                    
                                }
                                LogWriter write = new LogWriter();
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
        if ((System.currentTimeMillis() - timeStamp) < 1 * 30 * 1000) {
            return true;
        }
        return false;
    }

    public static boolean callUrl(String assetCode) throws MalformedURLException {
        try {
            URL url = new URL("http://dev-dsk-davsuar-2b-a59798a1.us-west-2.amazon.com:5000/api/newread?scannerCode="
                    + assetCode);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            System.out.println("value returned by the web server " + con.getResponseCode());
            if(!(con.getResponseCode()==200)){
                return false;                
            }
            else{
                return true;
            }            
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }
}