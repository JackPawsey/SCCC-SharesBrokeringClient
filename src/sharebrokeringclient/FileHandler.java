/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sharebrokeringclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

/**
 *
 * @author Jack
 */
public class FileHandler {
    
    String moneyFilePath = "C:\\Users\\Jack\\Documents\\NetBeansProjects\\ShareBrokeringClient\\money.txt";
    String sharesFilePath = "C:\\Users\\Jack\\Documents\\NetBeansProjects\\ShareBrokeringClient\\shares.txt";
    
    public FileHandler(String moneyPath, String sharesPath) {
        moneyFilePath = moneyPath;
        sharesFilePath = sharesPath;
    }
    
    public String readMoney() {
        double value = 0.0;
        
        if (!new File(moneyFilePath).canRead()) {
            System.out.println("File not found!");
        } else {
            try (DataInputStream dis = new DataInputStream(new FileInputStream(moneyFilePath))) {
                value = dis.readDouble();
            } catch (IOException ignored) {
                System.out.println("IOException : " + ignored);
            }
        }
        
        return String.valueOf(value);
    }
    
    public void writeMoney(double money) {
        try {
            FileOutputStream fos = new FileOutputStream(moneyFilePath);

            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeDouble(money);

            dos.close();
        }
        catch (IOException e) {
          System.out.println("IOException : " + e);
        }
    }
    
    public ArrayList readShares() throws FileNotFoundException {
        ArrayList<String> shares = new ArrayList<>();
        Scanner scanner = new Scanner(new File(sharesFilePath));
      
        while (scanner.hasNext()){
            shares.add(scanner.nextLine());
        }
        scanner.close();
        
        return shares;
    }
    
    public void writeShares(ArrayList<String> shares) {
        try {
            FileWriter fileWriter = new FileWriter(sharesFilePath);

            Writer writer = new BufferedWriter(fileWriter);

            int size = shares.size();
            
            for (int x = 0; x < size; x++) {
                writer.write(shares.get(x) + "\n");
            }
         
            writer.close();
        }
        catch (IOException e) {
          System.out.println("IOException : " + e);
        }
    }
}
