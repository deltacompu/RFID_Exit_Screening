package org.zebra.rfidScanEmb2;

import com.mot.rfid.api3.*;


public class Main {
    static RFIDReader reader = null;
	static boolean running = true; // Used to sync spawned threads.
    static int status = 3; // Reader status
    static boolean webServerConnectInProgress = false; // Used for other threads to figure out if they can send traffic to the webserver.

    public static void main(String[] args) throws InterruptedException, InvalidUsageException, OperationFailureException{
        

		//Use IP address of reader when you are running this code here
        //String hostname = "169.254.169.229";
		//String hostname = "10.244.3.26";
		String hostname = "localhost";

		reader = new RFIDReader(hostname, 0, 0);
		
		try {
			System.out.println("Connecting to reader at: " + hostname);
			reader.connect();
		} catch (Exception e) {
			System.out.println("Failed to connect to reader: " + e);
			return;
		}
		
		System.out.println("Successfully connected to RFID reader " + reader.ReaderCapabilities.getModelName());
		
		ScanTags.mainScan(reader);
		System.exit(0);
	}
	
	public static int getStatus() {
		return status;
	}
	
	public static void setStatus(int val) {
		status = val;
	}
}
