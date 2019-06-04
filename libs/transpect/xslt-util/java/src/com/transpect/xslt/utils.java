package com.transpect.xslt;

import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

public class utils {

    // method main: for testing without saxon
    /*public static void main(String[] args) {
        try{
            String rval = getCommandlineInputFromUser("Bitte Zeichen für MD5-Berechnung eingeben:");
            System.out.println(stringToMD5(rval));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    // method getCommandlineInputFromUser
    public static String getCommandlineInputFromUser(String prompt) throws IOException  {
        System.out.println(prompt);
        BufferedReader userInput = new BufferedReader (new InputStreamReader(System.in));
        return userInput.readLine().toString();
    }

    // make method getFileInputFromUser public when implementation is finished
    private static String getFileInputFromUser(String request, String dirpath, Integer maxWaitSecs) throws IOException  {
        
        //FileInputStream fis = new FileInputStream(dirpath + "[timestamp]_wait" + maxWaitSecs + "sec.answer");
        //Thread.sleep(10000);
        return request;
    }

    // method stringToMD5: get 32 chars long md5hash from utf8 string
    public static String stringToMD5(String chars) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(chars.getBytes("UTF-8"), 0, chars.length());
        BigInteger md5int = new BigInteger(1, md5.digest());
        return String.format("%1$032x", md5int);
    }

}