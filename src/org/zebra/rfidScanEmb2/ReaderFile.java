package org.zebra.rfidScanEmb2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReaderFile {
     
    public List<String> fillAsset(){
        String filePath = "/mnt/data/app/asset.txt";  // Replace with the actual path to your file
        List<String> alertOf = new ArrayList<String>();
        alertOf = readFile(filePath);
        return alertOf;
    }
    
    private static List<String> readFile(String filePath) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
    public List<String> endPointUrl(){
        String filePath = "/mnt/data/app/endpoint.txt";  // Replace with the actual path to your file
        List<String> urlEndPoint = new ArrayList<String>();
        urlEndPoint = readFile(filePath);
        return urlEndPoint;
    }
}
