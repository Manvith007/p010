package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encryption and hashing operations
 */
public class EncryptionUtil {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generates a random salt for password hashing
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Hashes a PIN with a salt using SHA-256
     */
    public static String hashPin(String pin, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            digest.update(salt.getBytes());
            byte[] hashedBytes = digest.digest(pin.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            ExceptionLogger.getInstance().logError("Hashing algorithm not available", e);
            throw new RuntimeException("Failed to hash PIN", e);
        }
    }
    
    /**
     * Hashes a PIN with an auto-generated salt
     */
    public static String hashPin(String pin) {
        String salt = generateSalt();
        String hash = hashPin(pin, salt);
        return salt + ":" + hash; // Store salt with hash
    }
    
    /**
     * Verifies a PIN against a stored hash
     */
    public static boolean verifyPin(String pin, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        
        String[] parts = storedHash.split(":", 2);
        if (parts.length != 2) {
            return false;
        }
        
        String salt = parts[0];
        String hash = parts[1];
        String inputHash = hashPin(pin, salt);
        
        return hash.equals(inputHash);
    }
    
    /**
     * Generates a secure random password
     */
    public static String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Simple string obfuscation for temporary data storage
     */
    public static String obfuscate(String input) {
        if (input == null) return null;
        
        StringBuilder obfuscated = new StringBuilder();
        for (char c : input.toCharArray()) {
            obfuscated.append((char) (c ^ 42)); // Simple XOR with key
        }
        return Base64.getEncoder().encodeToString(obfuscated.toString().getBytes());
    }
    
    /**
     * Deobfuscate a string
     */
    public static String deobfuscate(String obfuscated) {
        if (obfuscated == null) return null;
        
        try {
            String decoded = new String(Base64.getDecoder().decode(obfuscated));
            StringBuilder original = new StringBuilder();
            for (char c : decoded.toCharArray()) {
                original.append((char) (c ^ 42)); // Same XOR key
            }
            return original.toString();
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to deobfuscate string", e);
            return null;
        }
    }
}