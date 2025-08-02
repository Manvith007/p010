package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user profile with permissions and login information
 */
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Role {
        ADMIN("Administrator"), MEMBER("Member"), GUEST("Guest");
        
        private final String description;
        Role(String description) { this.description = description; }
        public String getDescription() { return description; }
        @Override
        public String toString() { return description; }
    }
    
    private String username;
    private String hashedPin;
    private Role role;
    private LocalDateTime lastLogin;
    private Set<String> permissions;
    private boolean isActive;
    private String displayName;
    
    public UserProfile() {
        this.permissions = new HashSet<>();
        this.isActive = true;
        this.role = Role.MEMBER;
    }
    
    public UserProfile(String username, String displayName, Role role) {
        this();
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        initializeDefaultPermissions();
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getHashedPin() { return hashedPin; }
    public void setHashedPin(String hashedPin) { this.hashedPin = hashedPin; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { 
        this.role = role;
        initializeDefaultPermissions();
    }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public void addPermission(String permission) {
        this.permissions.add(permission);
    }
    
    public void removePermission(String permission) {
        this.permissions.remove(permission);
    }
    
    public boolean hasPermission(String permission) {
        return this.permissions.contains(permission) || this.role == Role.ADMIN;
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    private void initializeDefaultPermissions() {
        permissions.clear();
        switch (role) {
            case ADMIN:
                permissions.add("MANAGE_USERS");
                permissions.add("EXPORT_DATA");
                permissions.add("IMPORT_DATA");
                permissions.add("SYSTEM_SETTINGS");
                // Fall through to include member permissions
            case MEMBER:
                permissions.add("MANAGE_INVENTORY");
                permissions.add("MANAGE_SHOPPING_LISTS");
                permissions.add("MANAGE_CHORES");
                permissions.add("MANAGE_BILLS");
                permissions.add("VIEW_REPORTS");
                // Fall through to include guest permissions
            case GUEST:
                permissions.add("VIEW_INVENTORY");
                permissions.add("VIEW_SHOPPING_LISTS");
                break;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %s [%s]", 
            displayName, username, role, isActive ? "Active" : "Inactive");
    }
}