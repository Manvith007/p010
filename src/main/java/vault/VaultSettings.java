package vault;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * VaultSettings - Manages PIN validation, retry lockout, theme settings,
 * export/import options, and vault wipe/restore functionality
 */
public class VaultSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_PIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 300000; // 5 minutes
    
    private String hashedPin;
    private Theme theme;
    private int failedAttempts;
    private long lockoutUntil;
    private boolean autoLockEnabled;
    private int autoLockMinutes;
    private boolean exportEncrypted;
    private String lastBackupPath;
    
    public VaultSettings() {
        this.hashedPin = hashPin("1234"); // Default PIN
        this.theme = Theme.DARK;
        this.failedAttempts = 0;
        this.lockoutUntil = 0;
        this.autoLockEnabled = true;
        this.autoLockMinutes = 15;
        this.exportEncrypted = true;
        this.lastBackupPath = "";
    }
    
    // PIN management with hashing
    public boolean validatePin(String pin) {
        if (isLockedOut()) {
            return false;
        }
        
        if (pin != null && hashPin(pin).equals(hashedPin)) {
            // Successful authentication
            failedAttempts = 0;
            lockoutUntil = 0;
            return true;
        } else {
            // Failed authentication
            failedAttempts++;
            if (failedAttempts >= MAX_PIN_ATTEMPTS) {
                lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            }
            return false;
        }
    }
    
    public void setPin(String newPin) {
        if (newPin != null && newPin.length() >= 4) {
            this.hashedPin = hashPin(newPin);
            // Reset lockout on PIN change
            this.failedAttempts = 0;
            this.lockoutUntil = 0;
        } else {
            throw new IllegalArgumentException("PIN must be at least 4 characters long");
        }
    }
    
    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to simple encoding if SHA-256 is not available
            return Base64.getEncoder().encodeToString(pin.getBytes());
        }
    }
    
    // Lockout management (recursion for PIN retry lockout)
    public boolean isLockedOut() {
        if (lockoutUntil > System.currentTimeMillis()) {
            return true;
        } else if (lockoutUntil > 0) {
            // Lockout period has expired, reset
            lockoutUntil = 0;
            failedAttempts = 0;
        }
        return false;
    }
    
    public long getRemainingLockoutTime() {
        if (isLockedOut()) {
            return lockoutUntil - System.currentTimeMillis();
        }
        return 0;
    }
    
    public int getFailedAttempts() {
        return failedAttempts;
    }
    
    public int getRemainingAttempts() {
        return Math.max(0, MAX_PIN_ATTEMPTS - failedAttempts);
    }
    
    // Theme management (enum for console coloring/symbols simulate)
    public Theme getTheme() {
        return theme;
    }
    
    public void setTheme(Theme theme) {
        this.theme = theme != null ? theme : Theme.DARK;
    }
    
    // Auto-lock settings
    public boolean isAutoLockEnabled() {
        return autoLockEnabled;
    }
    
    public void setAutoLockEnabled(boolean enabled) {
        this.autoLockEnabled = enabled;
    }
    
    public int getAutoLockMinutes() {
        return autoLockMinutes;
    }
    
    public void setAutoLockMinutes(int minutes) {
        this.autoLockMinutes = Math.max(1, Math.min(120, minutes)); // 1-120 minutes
    }
    
    // Export settings
    public boolean isExportEncrypted() {
        return exportEncrypted;
    }
    
    public void setExportEncrypted(boolean encrypted) {
        this.exportEncrypted = encrypted;
    }
    
    public String getLastBackupPath() {
        return lastBackupPath;
    }
    
    public void setLastBackupPath(String path) {
        this.lastBackupPath = path != null ? path : "";
    }
    
    // Vault wipe functionality (secure delete/restore)
    public void wipeVault() {
        // This would be called during secure deletion
        // Reset to defaults but keep theme preference
        Theme currentTheme = this.theme;
        
        this.hashedPin = hashPin("1234");
        this.failedAttempts = 0;
        this.lockoutUntil = 0;
        this.theme = currentTheme;
        this.autoLockEnabled = true;
        this.autoLockMinutes = 15;
        this.exportEncrypted = true;
        this.lastBackupPath = "";
    }
    
    // Validation methods
    public boolean isValidPin(String pin) {
        return pin != null && pin.length() >= 4 && pin.length() <= 20 &&
               pin.matches("\\d+"); // Only digits for simplicity
    }
    
    // Display helpers
    public String getThemeDisplayName() {
        switch (theme) {
            case LIGHT: return "Light Mode";
            case DARK: return "Dark Mode";
            case COLORFUL: return "Colorful Mode";
            default: return "Unknown";
        }
    }
    
    public String getLockoutStatus() {
        if (isLockedOut()) {
            long remainingMs = getRemainingLockoutTime();
            long remainingSeconds = remainingMs / 1000;
            return String.format("Locked out for %d seconds", remainingSeconds);
        } else if (failedAttempts > 0) {
            return String.format("%d failed attempts, %d remaining", 
                               failedAttempts, getRemainingAttempts());
        } else {
            return "No failed attempts";
        }
    }
    
    // Serialization methods
    @Override
    public String toString() {
        return String.join("|",
            hashedPin,
            theme.name(),
            String.valueOf(failedAttempts),
            String.valueOf(lockoutUntil),
            String.valueOf(autoLockEnabled),
            String.valueOf(autoLockMinutes),
            String.valueOf(exportEncrypted),
            lastBackupPath.replace("|", "\\|")
        );
    }
    
    public static VaultSettings fromString(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 8) {
            return new VaultSettings(); // Return default if invalid format
        }
        
        VaultSettings settings = new VaultSettings();
        
        try {
            settings.hashedPin = parts[0];
            settings.theme = Theme.valueOf(parts[1]);
            settings.failedAttempts = Integer.parseInt(parts[2]);
            settings.lockoutUntil = Long.parseLong(parts[3]);
            settings.autoLockEnabled = Boolean.parseBoolean(parts[4]);
            settings.autoLockMinutes = Integer.parseInt(parts[5]);
            settings.exportEncrypted = Boolean.parseBoolean(parts[6]);
            settings.lastBackupPath = parts[7].replace("\\|", "|");
        } catch (Exception e) {
            // Return default settings if parsing fails
            ExceptionLogger.log("Failed to parse vault settings, using defaults", e);
            return new VaultSettings();
        }
        
        return settings;
    }
    
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vault Settings:\n");
        sb.append("Theme: ").append(getThemeDisplayName()).append("\n");
        sb.append("Auto-lock: ").append(autoLockEnabled ? 
                  autoLockMinutes + " minutes" : "Disabled").append("\n");
        sb.append("Export encryption: ").append(exportEncrypted ? "Enabled" : "Disabled").append("\n");
        sb.append("Security status: ").append(getLockoutStatus()).append("\n");
        
        if (!lastBackupPath.isEmpty()) {
            sb.append("Last backup: ").append(lastBackupPath);
        }
        
        return sb.toString();
    }
}