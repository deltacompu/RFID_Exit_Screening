package org.zebra.rfidScanEmb2;

import java.net.HttpURLConnection;
import java.net.URL;

public class callUrl extends Thread {

    String assetCode;
    boolean respondWeb = false;

    public boolean respondWeb() {
        return respondWeb;
    }
    public void setrespondWeb(Boolean respondWeb) {
        this.respondWeb = respondWeb;
    }
    public String getAssetCode() {
        return assetCode;
    }
    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }
    @Override
    public void run() {
        callWebSite();
    }

    public synchronized void callWebSite(){
        try {
            URL url = new URL("http://dev-dsk-davsuar-2b-a59798a1.us-west-2.amazon.com:5000/api/newread?scannerCode="
                    + assetCode);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            System.out.println("value returned by the web server " + con.getResponseCode());            
            if(con.getResponseCode()!=200){
                setrespondWeb(false);            
            }
            else{
                setrespondWeb(true);
            }  
            con.disconnect();
        } catch (Exception e) {
            System.out.println(e);
            setrespondWeb(false);
        }
    }
}
