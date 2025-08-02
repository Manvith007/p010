package util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Chain of Responsibility pattern for validation logic
 * Validates all input (PIN, file path, note length, etc.)
 */
public class ValidatorChain {
    private static volatile ValidatorChain instance;
    private static final Object lock = new Object();
    
    // Validation patterns
    private static final Pattern PIN_PATTERN = Pattern.compile("^\\d{4,8}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int MAX_NOTE_LENGTH = 10000;
    private static final int MAX_TITLE_LENGTH = 200;
    
    private ValidatorChain() {}
    
    public static ValidatorChain getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ValidatorChain();
                }
            }
        }
        return instance;
    }
    
    // PIN validation
    public boolean validatePin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return false;
        }
        
        // Check pattern (4-8 digits)
        if (!PIN_PATTERN.matcher(pin).matches()) {
            return false;
        }
        
        // Check for weak patterns
        if (isWeakPin(pin)) {
            return false;
        }
        
        return true;
    }
    
    private boolean isWeakPin(String pin) {
        // Check for repeating digits (1111, 2222, etc.)
        if (pin.matches("(.)\\1+")) {
            return true;
        }
        
        // Check for sequential digits (1234, 4321, etc.)
        boolean ascending = true;
        boolean descending = true;
        
        for (int i = 1; i < pin.length(); i++) {
            int current = Character.getNumericValue(pin.charAt(i));
            int previous = Character.getNumericValue(pin.charAt(i - 1));
            
            if (current != previous + 1) {
                ascending = false;
            }
            if (current != previous - 1) {
                descending = false;
            }
        }
        
        return ascending || descending;
    }
    
    // File path validation
    public boolean validateFilePath(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            
            // Check if parent directory exists or can be created
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                return Files.isWritable(parent.getParent());
            }
            
            // Check if file is writable (if exists) or parent directory is writable
            if (Files.exists(path)) {
                return Files.isWritable(path);
            } else {
                return parent == null || Files.isWritable(parent);
            }
            
        } catch (Exception e) {
            return false;
        }
    }
    
    // Password strength validation
    public PasswordStrength validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.VERY_WEAK;
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;
        
        // Character variety
        if (password.matches(".*[a-z].*")) score++; // lowercase
        if (password.matches(".*[A-Z].*")) score++; // uppercase
        if (password.matches(".*\\d.*")) score++;   // digits
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++; // special chars
        
        // Avoid common patterns
        if (!password.matches(".*(123|abc|qwe|password|admin).*")) score++;
        
        switch (score) {
            case 0: case 1: case 2: return PasswordStrength.VERY_WEAK;
            case 3: case 4: return PasswordStrength.WEAK;
            case 5: case 6: return PasswordStrength.MEDIUM;
            case 7: return PasswordStrength.STRONG;
            default: return PasswordStrength.VERY_STRONG;
        }
    }
    
    public enum PasswordStrength {
        VERY_WEAK, WEAK, MEDIUM, STRONG, VERY_STRONG
    }
    
    // Text content validation
    public boolean validateNoteContent(String content) {
        return content != null && content.length() <= MAX_NOTE_LENGTH;
    }
    
    public boolean validateTitle(String title) {
        return title != null && 
               !title.trim().isEmpty() && 
               title.length() <= MAX_TITLE_LENGTH &&
               !title.contains("/") && 
               !title.contains("\\") &&
               !title.contains(":") &&
               !title.contains("*") &&
               !title.contains("?") &&
               !title.contains("\"") &&
               !title.contains("<") &&
               !title.contains(">") &&
               !title.contains("|");
    }
    
    // Email validation
    public boolean validateEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    // URL validation (basic)
    public boolean validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Basic URL format check
            return url.matches("^https?://[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?$");
        } catch (Exception e) {
            return false;
        }
    }
    
    // Date validation helpers
    public boolean validateDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        
        // Accept formats: YYYY-MM-DD, MM/DD/YYYY, DD-MM-YYYY
        return dateStr.matches("^\\d{4}-\\d{2}-\\d{2}$") ||
               dateStr.matches("^\\d{2}/\\d{2}/\\d{4}$") ||
               dateStr.matches("^\\d{2}-\\d{2}-\\d{4}$");
    }
    
    // Time validation
    public boolean validateTimeString(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return false;
        }
        
        // Accept formats: HH:MM, HH:MM:SS, HH:MM AM/PM
        return timeStr.matches("^\\d{2}:\\d{2}$") ||
               timeStr.matches("^\\d{2}:\\d{2}:\\d{2}$") ||
               timeStr.matches("^\\d{1,2}:\\d{2}\\s?(AM|PM|am|pm)$");
    }
    
    // Integer range validation
    public boolean validateIntegerRange(String value, int min, int max) {
        try {
            int intValue = Integer.parseInt(value);
            return intValue >= min && intValue <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    // General string validation
    public boolean validateNonEmptyString(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    public boolean validateStringLength(String value, int maxLength) {
        return value != null && value.length() <= maxLength;
    }
}