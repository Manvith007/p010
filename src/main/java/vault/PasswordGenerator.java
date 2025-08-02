package vault;

import java.security.SecureRandom;
import java.util.*;

/**
 * PasswordGenerator - Utility class for building strong passwords
 * Uses StringBuilder with various character sets and constraints
 */
public class PasswordGenerator {
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String AMBIGUOUS_CHARS = "0O1lI|`";
    
    private static final SecureRandom random = new SecureRandom();
    
    // Private constructor to prevent instantiation
    private PasswordGenerator() {}
    
    /**
     * Generate a password with default settings
     */
    public static String generate(int length) {
        return generate(new PasswordOptions(length));
    }
    
    /**
     * Generate a password with custom options
     */
    public static String generate(PasswordOptions options) {
        if (options.getLength() < 4) {
            throw new IllegalArgumentException("Password length must be at least 4 characters");
        }
        
        StringBuilder characterPool = new StringBuilder();
        List<String> requiredSets = new ArrayList<>();
        
        // Build character pool and required character sets
        if (options.isIncludeLowercase()) {
            String chars = options.isExcludeAmbiguous() ? 
                          removeAmbiguous(LOWERCASE) : LOWERCASE;
            characterPool.append(chars);
            requiredSets.add(chars);
        }
        
        if (options.isIncludeUppercase()) {
            String chars = options.isExcludeAmbiguous() ? 
                          removeAmbiguous(UPPERCASE) : UPPERCASE;
            characterPool.append(chars);
            requiredSets.add(chars);
        }
        
        if (options.isIncludeDigits()) {
            String chars = options.isExcludeAmbiguous() ? 
                          removeAmbiguous(DIGITS) : DIGITS;
            characterPool.append(chars);
            requiredSets.add(chars);
        }
        
        if (options.isIncludeSpecialChars()) {
            String chars = options.isExcludeAmbiguous() ? 
                          removeAmbiguous(SPECIAL_CHARS) : SPECIAL_CHARS;
            characterPool.append(chars);
            requiredSets.add(chars);
        }
        
        if (characterPool.length() == 0) {
            throw new IllegalArgumentException("At least one character type must be enabled");
        }
        
        // Generate password ensuring at least one character from each required set
        StringBuilder password = new StringBuilder(options.getLength());
        
        // First, add one character from each required set
        for (String charSet : requiredSets) {
            password.append(charSet.charAt(random.nextInt(charSet.length())));
        }
        
        // Fill remaining positions with random characters from the pool
        String pool = characterPool.toString();
        for (int i = password.length(); i < options.getLength(); i++) {
            password.append(pool.charAt(random.nextInt(pool.length())));
        }
        
        // Shuffle the password to avoid predictable patterns
        return shuffleString(password.toString());
    }
    
    /**
     * Generate multiple password options
     */
    public static List<String> generateMultiple(PasswordOptions options, int count) {
        List<String> passwords = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            passwords.add(generate(options));
        }
        return passwords;
    }
    
    /**
     * Generate a memorable password using word combinations
     */
    public static String generateMemorable(int wordCount, boolean includeNumbers, boolean includeSpecialChars) {
        String[] words = {
            "Apple", "Bridge", "Castle", "Dragon", "Eagle", "Forest", "Guitar", "Harbor",
            "Island", "Jungle", "Knight", "Lion", "Mountain", "Ocean", "Palace", "Queen",
            "River", "Storm", "Tiger", "Unicorn", "Valley", "Wizard", "Xray", "Yacht", "Zebra"
        };
        
        StringBuilder password = new StringBuilder();
        
        // Add random words
        for (int i = 0; i < wordCount; i++) {
            if (i > 0) {
                password.append("-");
            }
            password.append(words[random.nextInt(words.length)]);
        }
        
        // Add numbers if requested
        if (includeNumbers) {
            password.append(random.nextInt(100));
        }
        
        // Add special character if requested
        if (includeSpecialChars) {
            password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        }
        
        return password.toString();
    }
    
    /**
     * Generate a PIN (numeric only)
     */
    public static String generatePIN(int length) {
        StringBuilder pin = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            pin.append(random.nextInt(10));
        }
        return pin.toString();
    }
    
    /**
     * Check password strength
     */
    public static PasswordStrength checkStrength(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0, "Empty password", StrengthLevel.VERY_WEAK);
        }
        
        int score = 0;
        List<String> feedback = new ArrayList<>();
        
        // Length scoring
        if (password.length() >= 12) {
            score += 25;
        } else if (password.length() >= 8) {
            score += 15;
        } else if (password.length() >= 6) {
            score += 5;
        } else {
            feedback.add("Password is too short (minimum 6 characters)");
        }
        
        // Character type scoring
        if (password.matches(".*[a-z].*")) {
            score += 15;
        } else {
            feedback.add("Add lowercase letters");
        }
        
        if (password.matches(".*[A-Z].*")) {
            score += 15;
        } else {
            feedback.add("Add uppercase letters");
        }
        
        if (password.matches(".*\\d.*")) {
            score += 15;
        } else {
            feedback.add("Add numbers");
        }
        
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            score += 15;
        } else {
            feedback.add("Add special characters");
        }
        
        // Pattern penalties
        if (password.matches(".*(..).*\\1.*")) {
            score -= 10;
            feedback.add("Avoid repeated patterns");
        }
        
        if (password.matches(".*(?:012|123|234|345|456|567|678|789|890|abc|bcd|cde).*")) {
            score -= 15;
            feedback.add("Avoid sequential characters");
        }
        
        // Common password penalties
        if (isCommonPassword(password.toLowerCase())) {
            score -= 25;
            feedback.add("Avoid common passwords");
        }
        
        // Determine strength level
        StrengthLevel level;
        if (score >= 80) {
            level = StrengthLevel.VERY_STRONG;
        } else if (score >= 60) {
            level = StrengthLevel.STRONG;
        } else if (score >= 40) {
            level = StrengthLevel.MODERATE;
        } else if (score >= 20) {
            level = StrengthLevel.WEAK;
        } else {
            level = StrengthLevel.VERY_WEAK;
        }
        
        return new PasswordStrength(Math.max(0, Math.min(100, score)), 
                                   String.join(", ", feedback), level);
    }
    
    // Helper methods
    private static String removeAmbiguous(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (AMBIGUOUS_CHARS.indexOf(c) == -1) {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    private static String shuffleString(String input) {
        List<Character> characters = new ArrayList<>();
        for (char c : input.toCharArray()) {
            characters.add(c);
        }
        Collections.shuffle(characters, random);
        
        StringBuilder shuffled = new StringBuilder();
        for (char c : characters) {
            shuffled.append(c);
        }
        return shuffled.toString();
    }
    
    private static boolean isCommonPassword(String password) {
        String[] commonPasswords = {
            "password", "123456", "password123", "admin", "qwerty", "letmein",
            "welcome", "monkey", "1234567890", "abc123", "password1", "123456789"
        };
        
        for (String common : commonPasswords) {
            if (password.contains(common)) {
                return true;
            }
        }
        return false;
    }
    
    // Inner classes
    public static class PasswordOptions {
        private int length;
        private boolean includeLowercase;
        private boolean includeUppercase;
        private boolean includeDigits;
        private boolean includeSpecialChars;
        private boolean excludeAmbiguous;
        
        public PasswordOptions(int length) {
            this.length = length;
            this.includeLowercase = true;
            this.includeUppercase = true;
            this.includeDigits = true;
            this.includeSpecialChars = true;
            this.excludeAmbiguous = true;
        }
        
        // Getters and setters
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        
        public boolean isIncludeLowercase() { return includeLowercase; }
        public void setIncludeLowercase(boolean includeLowercase) { this.includeLowercase = includeLowercase; }
        
        public boolean isIncludeUppercase() { return includeUppercase; }
        public void setIncludeUppercase(boolean includeUppercase) { this.includeUppercase = includeUppercase; }
        
        public boolean isIncludeDigits() { return includeDigits; }
        public void setIncludeDigits(boolean includeDigits) { this.includeDigits = includeDigits; }
        
        public boolean isIncludeSpecialChars() { return includeSpecialChars; }
        public void setIncludeSpecialChars(boolean includeSpecialChars) { this.includeSpecialChars = includeSpecialChars; }
        
        public boolean isExcludeAmbiguous() { return excludeAmbiguous; }
        public void setExcludeAmbiguous(boolean excludeAmbiguous) { this.excludeAmbiguous = excludeAmbiguous; }
    }
    
    public static class PasswordStrength {
        private final int score;
        private final String feedback;
        private final StrengthLevel level;
        
        public PasswordStrength(int score, String feedback, StrengthLevel level) {
            this.score = score;
            this.feedback = feedback;
            this.level = level;
        }
        
        public int getScore() { return score; }
        public String getFeedback() { return feedback; }
        public StrengthLevel getLevel() { return level; }
        
        @Override
        public String toString() {
            return String.format("Strength: %s (%d/100)%s", 
                               level.getDisplayName(), 
                               score, 
                               feedback.isEmpty() ? "" : " - " + feedback);
        }
    }
    
    public enum StrengthLevel {
        VERY_WEAK("Very Weak"),
        WEAK("Weak"),
        MODERATE("Moderate"),
        STRONG("Strong"),
        VERY_STRONG("Very Strong");
        
        private final String displayName;
        
        StrengthLevel(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}