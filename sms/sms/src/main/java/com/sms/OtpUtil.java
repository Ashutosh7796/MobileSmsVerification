package com.sms;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class OtpUtil {

    private static final SecureRandom random = new SecureRandom();
    private static final String digits = "0123456789";

    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(digits.charAt(random.nextInt(digits.length())));
        }
        return otp.toString();
    }

    public static String hashOtp(String otp, String salt) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] hashedBytes = md.digest(otp.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public static String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
