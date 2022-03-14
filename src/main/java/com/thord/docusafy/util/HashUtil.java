package com.thord.docusafy.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    
    public static String sha256(String originalString){
        try{
            final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        final byte[] hashbytes = digest.digest(
        originalString.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashbytes);
        }catch(NoSuchAlgorithmException ex){
            throw new RuntimeException("Unable to create hash from given string", ex);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
