package service;

import model.UserProfile;
import util.*;

import java.io.IOException;
import java.util.*;

/**
 * Service class for managing user accounts and authentication
 */
public class UserService {
    private static final String USERS_FILE = "users.dat";
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PIN = "1234";
    
    private Map<String, UserProfile> users;
    
    public UserService() {
        this.users = new HashMap<>();
        loadData();
        ensureDefaultAdminExists();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            Map<String, UserProfile> loadedUsers = FileOperations.readObjectFromFile(USERS_FILE, Map.class);
            if (loadedUsers != null) {
                this.users = loadedUsers;
            }
            ExceptionLogger.getInstance().logInfo("Users data loaded successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load users data", e);
        }
    }
    
    public void saveAllData() {
        try {
            FileOperations.writeObjectToFile(users, USERS_FILE);
            ExceptionLogger.getInstance().logInfo("Users data saved successfully");
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to save users data", e);
        }
    }
    
    private void ensureDefaultAdminExists() {
        if (users.isEmpty() || !users.containsKey(DEFAULT_ADMIN_USERNAME)) {
            UserProfile defaultAdmin = new UserProfile(DEFAULT_ADMIN_USERNAME, "Administrator", UserProfile.Role.ADMIN);
            defaultAdmin.setHashedPin(EncryptionUtil.hashPin(DEFAULT_ADMIN_PIN));
            users.put(DEFAULT_ADMIN_USERNAME, defaultAdmin);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Created default admin user");
        }
    }
    
    // User management
    public boolean createUser(String username, String displayName, String pin, UserProfile.Role role) {
        ValidationChain validation = new ValidationChain()
            .notEmpty("Username")
            .minLength(3, "Username")
            .maxLength(20, "Username");
        
        if (!validation.validate(username) || users.containsKey(username)) {
            return false;
        }
        
        ValidationChain pinValidation = new ValidationChain().isPinFormat();
        if (!pinValidation.validate(pin)) {
            return false;
        }
        
        UserProfile newUser = new UserProfile(username, displayName, role);
        newUser.setHashedPin(EncryptionUtil.hashPin(pin));
        users.put(username, newUser);
        saveAllData();
        
        ExceptionLogger.getInstance().logInfo("Created user: " + username + " with role: " + role);
        return true;
    }
    
    public boolean updateUser(String username, UserProfile updatedUser) {
        if (users.containsKey(username) && updatedUser != null) {
            users.put(username, updatedUser);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Updated user: " + username);
            return true;
        }
        return false;
    }
    
    public boolean deleteUser(String username) {
        // Don't allow deleting the default admin
        if (DEFAULT_ADMIN_USERNAME.equals(username)) {
            return false;
        }
        
        UserProfile removed = users.remove(username);
        if (removed != null) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Deleted user: " + username);
            return true;
        }
        return false;
    }
    
    public UserProfile getUser(String username) {
        return users.get(username);
    }
    
    public boolean saveUser(UserProfile user) {
        if (user != null && user.getUsername() != null) {
            users.put(user.getUsername(), user);
            saveAllData();
            return true;
        }
        return false;
    }
    
    public List<UserProfile> getAllUsers() {
        return new ArrayList<>(users.values());
    }
    
    public List<UserProfile> getActiveUsers() {
        return users.values().stream()
            .filter(UserProfile::isActive)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<UserProfile> getUsersByRole(UserProfile.Role role) {
        return users.values().stream()
            .filter(user -> user.getRole() == role)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // Authentication
    public UserProfile authenticateUser(String username, String pin) {
        UserProfile user = users.get(username);
        if (user == null || !user.isActive()) {
            ExceptionLogger.getInstance().logWarning("Authentication failed for user: " + username + " (user not found or inactive)");
            return null;
        }
        
        if (EncryptionUtil.verifyPin(pin, user.getHashedPin())) {
            ExceptionLogger.getInstance().logInfo("User authenticated successfully: " + username);
            return user;
        } else {
            ExceptionLogger.getInstance().logWarning("Authentication failed for user: " + username + " (invalid PIN)");
            return null;
        }
    }
    
    public boolean changeUserPin(String username, String oldPin, String newPin) {
        UserProfile user = users.get(username);
        if (user == null) {
            return false;
        }
        
        // Verify old PIN
        if (!EncryptionUtil.verifyPin(oldPin, user.getHashedPin())) {
            ExceptionLogger.getInstance().logWarning("PIN change failed for user: " + username + " (invalid old PIN)");
            return false;
        }
        
        // Validate new PIN
        ValidationChain pinValidation = new ValidationChain().isPinFormat();
        if (!pinValidation.validate(newPin)) {
            return false;
        }
        
        // Update PIN
        user.setHashedPin(EncryptionUtil.hashPin(newPin));
        saveAllData();
        ExceptionLogger.getInstance().logInfo("PIN changed for user: " + username);
        return true;
    }
    
    public boolean resetUserPin(String username, String newPin) {
        UserProfile user = users.get(username);
        if (user == null) {
            return false;
        }
        
        ValidationChain pinValidation = new ValidationChain().isPinFormat();
        if (!pinValidation.validate(newPin)) {
            return false;
        }
        
        user.setHashedPin(EncryptionUtil.hashPin(newPin));
        saveAllData();
        ExceptionLogger.getInstance().logInfo("PIN reset for user: " + username);
        return true;
    }
    
    // User activation/deactivation
    public boolean activateUser(String username) {
        UserProfile user = users.get(username);
        if (user != null) {
            user.setActive(true);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Activated user: " + username);
            return true;
        }
        return false;
    }
    
    public boolean deactivateUser(String username) {
        // Don't allow deactivating the default admin
        if (DEFAULT_ADMIN_USERNAME.equals(username)) {
            return false;
        }
        
        UserProfile user = users.get(username);
        if (user != null) {
            user.setActive(false);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Deactivated user: " + username);
            return true;
        }
        return false;
    }
    
    // Permission management
    public boolean addPermissionToUser(String username, String permission) {
        UserProfile user = users.get(username);
        if (user != null) {
            user.addPermission(permission);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Added permission '" + permission + "' to user: " + username);
            return true;
        }
        return false;
    }
    
    public boolean removePermissionFromUser(String username, String permission) {
        UserProfile user = users.get(username);
        if (user != null) {
            user.removePermission(permission);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Removed permission '" + permission + "' from user: " + username);
            return true;
        }
        return false;
    }
    
    public boolean updateUserRole(String username, UserProfile.Role newRole) {
        UserProfile user = users.get(username);
        if (user != null) {
            user.setRole(newRole);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Updated role for user " + username + " to: " + newRole);
            return true;
        }
        return false;
    }
    
    // Search and filtering
    public List<UserProfile> searchUsers(String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return users.values().stream()
            .filter(user -> user.getUsername().toLowerCase().contains(lowerTerm) ||
                          user.getDisplayName().toLowerCase().contains(lowerTerm))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    // Statistics
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", users.size());
        stats.put("activeUsers", getActiveUsers().size());
        stats.put("inactiveUsers", users.size() - getActiveUsers().size());
        
        // Role distribution
        Map<UserProfile.Role, Long> roleDistribution = users.values().stream()
            .collect(HashMap::new,
                (map, user) -> map.merge(user.getRole(), 1L, Long::sum),
                (map1, map2) -> { map2.forEach((k, v) -> map1.merge(k, v, Long::sum)); return map1; });
        stats.put("roleDistribution", roleDistribution);
        
        return stats;
    }
    
    // Bulk operations
    public int deactivateInactiveUsers(int daysInactive) {
        int deactivated = 0;
        java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(daysInactive);
        
        for (UserProfile user : users.values()) {
            if (!DEFAULT_ADMIN_USERNAME.equals(user.getUsername()) && 
                user.isActive() && 
                user.getLastLogin() != null && 
                user.getLastLogin().isBefore(cutoffDate)) {
                user.setActive(false);
                deactivated++;
            }
        }
        
        if (deactivated > 0) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Deactivated " + deactivated + " inactive users");
        }
        
        return deactivated;
    }
    
    // Security audit
    public List<String> performSecurityAudit() {
        List<String> auditResults = new ArrayList<>();
        
        // Check for users without recent login
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        long inactiveUsers = users.values().stream()
            .filter(user -> user.isActive() && 
                          (user.getLastLogin() == null || user.getLastLogin().isBefore(thirtyDaysAgo)))
            .count();
        
        if (inactiveUsers > 0) {
            auditResults.add("Warning: " + inactiveUsers + " active users haven't logged in for 30+ days");
        }
        
        // Check for users with excessive permissions
        long adminUsers = getUsersByRole(UserProfile.Role.ADMIN).size();
        if (adminUsers > 3) {
            auditResults.add("Warning: " + adminUsers + " admin users (consider reducing admin accounts)");
        }
        
        // Check for default credentials
        UserProfile admin = users.get(DEFAULT_ADMIN_USERNAME);
        if (admin != null && EncryptionUtil.verifyPin(DEFAULT_ADMIN_PIN, admin.getHashedPin())) {
            auditResults.add("SECURITY RISK: Default admin PIN has not been changed!");
        }
        
        return auditResults;
    }
    
    // Utility methods
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    public List<String> getUsernames() {
        return new ArrayList<>(users.keySet());
    }
    
    public List<String> getActiveUsernames() {
        return getActiveUsers().stream()
            .map(UserProfile::getUsername)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}