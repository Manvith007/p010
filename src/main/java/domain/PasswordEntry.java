package domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a password entry in the vault
 * Implements Serializable for file storage
 */
public class PasswordEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String site;
    private String username;
    private transient String encryptionKey; // Used only in memory, not serialized
    private String encryptedPassword;
    private String note;
    private LocalDateTime lastModified;
    
    public PasswordEntry(String site, String username, String password, String note) {
        this.site = site;
        this.username = username;
        this.encryptedPassword = encryptPassword(password);
        this.note = note;
        this.lastModified = LocalDateTime.now();
    }
    
    // Simple encryption using transient fields (basic XOR for demonstration)
    private String encryptPassword(String password) {
        if (password == null) return null;
        StringBuilder encrypted = new StringBuilder();
        int key = 42; // Simple key for demonstration
        for (char c : password.toCharArray()) {
            encrypted.append((char)(c ^ key));
        }
        return encrypted.toString();
    }
    
    public String decryptPassword() {
        if (encryptedPassword == null) return null;
        StringBuilder decrypted = new StringBuilder();
        int key = 42; // Same key for decryption
        for (char c : encryptedPassword.toCharArray()) {
            decrypted.append((char)(c ^ key));
        }
        return decrypted.toString();
    }
    
    // Getters and setters
    public String getSite() { return site; }
    public void setSite(String site) { 
        this.site = site; 
        updateLastModified();
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username; 
        updateLastModified();
    }
    
    public String getNote() { return note; }
    public void setNote(String note) { 
        this.note = note; 
        updateLastModified();
    }
    
    public LocalDateTime getLastModified() { return lastModified; }
    
    public void updatePassword(String newPassword) {
        this.encryptedPassword = encryptPassword(newPassword);
        updateLastModified();
    }
    
    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PasswordEntry that = (PasswordEntry) obj;
        return Objects.equals(site, that.site) && Objects.equals(username, that.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(site, username);
    }
    
    @Override
    public String toString() {
        return String.format("Site: %s | Username: %s | Modified: %s", 
                           site, username, lastModified.toString());
    }
}