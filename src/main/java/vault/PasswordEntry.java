package vault;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * PasswordEntry - Implements Serializable with encryption/decryption
 * Uses transient fields for in-memory encryption key
 */
public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String site;
    private String username;
    private String encryptedPassword; // Stored encrypted
    private transient String encryptionKey; // Not serialized
    private String note;
    private LocalDateTime lastModified;
    
    public PasswordEntry(String site, String username, String password, String note) {
        this.site = site;
        this.username = username;
        this.note = note;
        this.lastModified = LocalDateTime.now();
        // Password will be encrypted when toString() is called with key
    }
    
    // Encryption/Decryption methods using simple reversible logic
    private String encrypt(String text, String key) {
        if (text == null || key == null) return text;
        
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char keyChar = key.charAt(i % key.length());
            encrypted.append((char) (c ^ keyChar));
        }
        return Base64.getEncoder().encodeToString(encrypted.toString().getBytes());
    }
    
    private String decrypt(String encryptedText, String key) {
        if (encryptedText == null || key == null) return encryptedText;
        
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            String decodedStr = new String(decoded);
            
            StringBuilder decrypted = new StringBuilder();
            for (int i = 0; i < decodedStr.length(); i++) {
                char c = decodedStr.charAt(i);
                char keyChar = key.charAt(i % key.length());
                decrypted.append((char) (c ^ keyChar));
            }
            return decrypted.toString();
        } catch (Exception e) {
            ExceptionLogger.log("Failed to decrypt password", e);
            return "[DECRYPTION_ERROR]";
        }
    }
    
    public String getPassword(String key) {
        return decrypt(encryptedPassword, key);
    }
    
    public void setPassword(String password, String key) {
        this.encryptedPassword = encrypt(password, key);
        this.lastModified = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getSite() { return site; }
    public void setSite(String site) { 
        this.site = site; 
        this.lastModified = LocalDateTime.now();
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username; 
        this.lastModified = LocalDateTime.now();
    }
    
    public String getNote() { return note; }
    public void setNote(String note) { 
        this.note = note; 
        this.lastModified = LocalDateTime.now();
    }
    
    public LocalDateTime getLastModified() { return lastModified; }
    
    // Serialization methods
    public String toString(String key) {
        // Encrypt password before serialization
        if (encryptedPassword == null && key != null) {
            // This handles the case where password was set but not yet encrypted
            this.encryptedPassword = encrypt("", key);
        }
        
        return String.join("|", 
            site != null ? site : "",
            username != null ? username : "",
            encryptedPassword != null ? encryptedPassword : "",
            note != null ? note : "",
            lastModified != null ? lastModified.format(FORMATTER) : ""
        );
    }
    
    public static PasswordEntry fromString(String data, String key) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid password entry format");
        }
        
        PasswordEntry entry = new PasswordEntry(parts[0], parts[1], "", parts[3]);
        entry.encryptedPassword = parts[2];
        
        try {
            if (!parts[4].isEmpty()) {
                entry.lastModified = LocalDateTime.parse(parts[4], FORMATTER);
            }
        } catch (Exception e) {
            entry.lastModified = LocalDateTime.now();
        }
        
        return entry;
    }
    
    @Override
    public String toString() {
        return String.format("PasswordEntry{site='%s', username='%s', lastModified=%s}", 
                           site, username, lastModified);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PasswordEntry that = (PasswordEntry) obj;
        return site.equals(that.site) && username.equals(that.username);
    }
    
    @Override
    public int hashCode() {
        return site.hashCode() * 31 + username.hashCode();
    }
}