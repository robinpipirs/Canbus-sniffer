package dao;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;

import jdk.internal.util.xml.impl.ReaderUTF16;
import jdk.internal.util.xml.impl.ReaderUTF8;

/**
 * Created by robinpipirs on 10/03/16.
 */
public class CanbusLogFileParser {

    public CanbusLogFileParser() {
    }

    public ArrayList<CanbusMessage> ParseCanBusLog(String path) {

        ArrayList<CanbusMessage> canMessages = new ArrayList<>();

        BufferedReader br = null;

        try {

            String sCurrentLine;
          
            br = new BufferedReader(new ReaderUTF8(Sniffer.class.getResourceAsStream(path)));
           
            //br = new BufferedReader(new FileReader(path));

            while ((sCurrentLine = br.readLine()) != null) {


                //TODO: parse with string method

                CanbusMessage can = ParseCanBusString(sCurrentLine);

                    //TODO: Test the structure of the message after parsing

                    canMessages.add(can);

                //}/

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return canMessages;
    }

    public CanbusMessage ParseCanBusString(String sCurrentLine) {


        String delims = "[ ]";
        String[] tokens = sCurrentLine.split(delims);
        String id;
        //System.out.println("Before parse: "+sCurrentLine);
        //System.out.println(" length:"+tokens.length);
        if (tokens.length > 5) {
            String time = tokens[1];
            if (tokens[3].substring(tokens[3].length() - 1).equals(",")){
                id = tokens[3].substring(0, tokens[3].length()-1);
            }
            else{
                id = tokens[3].substring(0, tokens[3].length());
            }
            String data[] = new String[tokens.length-5]; //TODO dynamic length -6 works for input but not for files
            for (int i = 0; i < data.length; i++) {
                if (tokens[5 + i].length() == 1) {
                    data[i] = "0" + tokens[5 + i];
                } else{
                    data[i] = tokens[5 + i];
                }
            }
//            String s = "";
//            for (int j = 0; j < data.length;j++){
//                s = s +" " +data[j];
//            }
//            System.out.println(s);

           // System.out.println("time:" + time);
           // System.out.println("id:"+ id);
           // System.out.println("data:"  + dataString);


            //TODO: Test the structure of the message after parsing

            return new CanbusMessage(time, id, data);
        }
        return null;
    }

    public CanbusMessage ParseCanBusStringFromInputSerial(String sCurrentLine) {


        String delims = "[ ]";
        String[] tokens = sCurrentLine.split(delims);
        String id;
//        System.out.println("Before parse: "+sCurrentLine);
//        System.out.println(" length:"+tokens.length);
        if (tokens.length > 5) {
            String time = tokens[1];
            if (tokens[3].substring(tokens[3].length() - 1).equals(",")){
                id = tokens[3].substring(0, tokens[3].length()-1);
            }
            else{
                id = tokens[3].substring(0, tokens[3].length());
            }
            String data[] = new String[tokens.length-6]; //TODO dynamic length -6 works for input but not for files
            for (int i = 0; i < data.length; i++) {
                if (tokens[5 + i].length() == 1) {
                    data[i] = "0" + tokens[5 + i];
                } else{
                    data[i] = tokens[5 + i];
                }
            }
//            String s = "";
//            for (int j = 0; j < data.length;j++){
//                s = s +" " +data[j];
//            }
//            System.out.println(s);

            // System.out.println("time:" + time);
            // System.out.println("id:"+ id);
            // System.out.println("data:"  + dataString);


            //TODO: Test the structure of the message after parsing

            return new CanbusMessage(time, id, data);
        }
        return null;
    }
}