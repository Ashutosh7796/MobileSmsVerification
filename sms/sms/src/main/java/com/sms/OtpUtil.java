package com.sms;

import java.security.MessageDigest; // Import for message digest algorithms like SHA-256
import java.security.SecureRandom; // Import for generating cryptographically strong random numbers
import java.util.Base64; // Import for encoding binary data to Base64

public class OtpUtil {

    // SecureRandom instance for generating random numbers securely
    private static final SecureRandom random = new SecureRandom();

    // String containing digits 0-9 for OTP generation
    private static final String digits = "0123456789";

    public static String generateOtp(int length) {
        // StringBuilder for constructing the OTP
        StringBuilder otp = new StringBuilder(length);

        // Loop to generate each digit of the OTP
        for (int i = 0; i < length; i++) {
            // Append a random digit to the OTP
            otp.append(digits.charAt(random.nextInt(digits.length())));
        }
        // Convert StringBuilder to String and return the OTP
        return otp.toString();
    }

    public static String hashOtp(String otp, String salt) throws Exception {
        // Get an instance of the SHA-256 message digest
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Update the digest with the salt
        md.update(salt.getBytes());

        // Perform the hashing on the OTP
        byte[] hashedBytes = md.digest(otp.getBytes());

        // Encode the hashed bytes to a Base64 string and return it
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    // Method to generate a random salt
    public static String generateSalt() {
        // Array to hold the salt bytes
        byte[] salt = new byte[16];

        // Generate random bytes and fill the salt array
        random.nextBytes(salt);

        // Encode the salt bytes to a Base64 string and return it
        return Base64.getEncoder().encodeToString(salt);
    }
}
