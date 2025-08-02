package manager;

import model.*;
import service.*;
import util.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Singleton manager class that controls all modules and user sessions
 */
public class ResourcePlannerManager {
    private static ResourcePlannerManager instance;
    private UserProfile currentUser;
    private InventoryService inventoryService;
    private ShoppingService shoppingService;
    private ChoreService choreService;
    private BillService billService;
    private UserService userService;
    private ExportImportService exportImportService;
    
    private ResourcePlannerManager() {
        initialize();
    }
    
    public static ResourcePlannerManager getInstance() {
        if (instance == null) {
            synchronized (ResourcePlannerManager.class) {
                if (instance == null) {
                    instance = new ResourcePlannerManager();
                }
            }
        }
        return instance;
    }
    
    private void initialize() {
        try {
            this.inventoryService = new InventoryService();
            this.shoppingService = new ShoppingService();
            this.choreService = new ChoreService();
            this.billService = new BillService();
            this.userService = new UserService();
            this.exportImportService = new ExportImportService();
            
            ExceptionLogger.getInstance().logInfo("ResourcePlannerManager initialized successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to initialize ResourcePlannerManager", e);
            throw new RuntimeException("Initialization failed", e);
        }
    }
    
    // User session management
    public boolean login(String username, String pin) {
        UserProfile user = userService.authenticateUser(username, pin);
        if (user != null) {
            this.currentUser = user;
            user.updateLastLogin();
            userService.saveUser(user);
            ExceptionLogger.getInstance().logInfo("User logged in: " + username);
            return true;
        }
        return false;
    }
    
    public void logout() {
        if (currentUser != null) {
            ExceptionLogger.getInstance().logInfo("User logged out: " + currentUser.getUsername());
            this.currentUser = null;
        }
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public UserProfile getCurrentUser() {
        return currentUser;
    }
    
    public boolean hasPermission(String permission) {
        return currentUser != null && currentUser.hasPermission(permission);
    }
    
    // Service getters
    public InventoryService getInventoryService() {
        return inventoryService;
    }
    
    public ShoppingService getShoppingService() {
        return shoppingService;
    }
    
    public ChoreService getChoreService() {
        return choreService;
    }
    
    public BillService getBillService() {
        return billService;
    }
    
    public UserService getUserService() {
        return userService;
    }
    
    public ExportImportService getExportImportService() {
        return exportImportService;
    }
    
    // Dashboard and summary methods
    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Inventory summary
            List<InventoryItem> allItems = inventoryService.getAllItems();
            long expiredItems = allItems.stream().filter(item -> item.getStatus() == ItemStatus.EXPIRED).count();
            long lowStockItems = allItems.stream().filter(item -> item.getStatus() == ItemStatus.LOW).count();
            
            summary.put("totalInventoryItems", allItems.size());
            summary.put("expiredItems", expiredItems);
            summary.put("lowStockItems", lowStockItems);
            
            // Shopping lists summary
            List<ShoppingList> shoppingLists = shoppingService.getAllShoppingLists();
            summary.put("totalShoppingLists", shoppingLists.size());
            summary.put("incompleteShoppingLists", 
                shoppingLists.stream().filter(list -> !list.isCompleted()).count());
            
            // Chores summary
            List<Chore> chores = choreService.getAllChores();
            long overdueChores = chores.stream().filter(Chore::isOverdue).count();
            long pendingChores = chores.stream().filter(chore -> chore.getStatus() == Chore.Status.PENDING).count();
            
            summary.put("totalChores", chores.size());
            summary.put("overdueChores", overdueChores);
            summary.put("pendingChores", pendingChores);
            
            // Bills summary
            List<BillUtility> bills = billService.getAllBills();
            long overdueBills = bills.stream().filter(BillUtility::isOverdue).count();
            double totalUnpaidAmount = bills.stream()
                .filter(bill -> !bill.isPaid())
                .mapToDouble(BillUtility::getAmount)
                .sum();
            
            summary.put("totalBills", bills.size());
            summary.put("overdueBills", overdueBills);
            summary.put("totalUnpaidAmount", totalUnpaidAmount);
            
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to generate dashboard summary", e);
        }
        
        return summary;
    }
    
    // Urgent items that need attention
    public List<String> getUrgentNotifications() {
        List<String> notifications = new ArrayList<>();
        
        try {
            // Expired inventory items
            List<InventoryItem> expiredItems = inventoryService.getExpiredItems();
            for (InventoryItem item : expiredItems) {
                notifications.add("EXPIRED: " + item.getName() + " expired on " + item.getExpiryDate());
            }
            
            // Low stock items
            List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
            for (InventoryItem item : lowStockItems) {
                notifications.add("LOW STOCK: " + item.getName() + " - " + item.getQuantity() + " " + item.getUnit());
            }
            
            // Overdue chores
            List<Chore> overdueChores = choreService.getOverdueChores();
            for (Chore chore : overdueChores) {
                notifications.add("OVERDUE CHORE: " + chore.getDescription() + " (assigned to " + chore.getAssignee() + ")");
            }
            
            // Overdue bills
            List<BillUtility> overdueBills = billService.getOverdueBills();
            for (BillUtility bill : overdueBills) {
                notifications.add("OVERDUE BILL: " + bill.getUtilityName() + " - $" + bill.getAmount() + " was due " + bill.getDueDate());
            }
            
            // Bills due soon (within 3 days)
            List<BillUtility> upcomingBills = billService.getBillsDueSoon(3);
            for (BillUtility bill : upcomingBills) {
                notifications.add("BILL DUE SOON: " + bill.getUtilityName() + " - $" + bill.getAmount() + " due " + bill.getDueDate());
            }
            
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to get urgent notifications", e);
        }
        
        return notifications;
    }
    
    // System maintenance
    public void performSystemMaintenance() {
        try {
            ExceptionLogger.getInstance().logInfo("Starting system maintenance");
            
            // Save all data
            inventoryService.saveAllData();
            shoppingService.saveAllData();
            choreService.saveAllData();
            billService.saveAllData();
            userService.saveAllData();
            
            // Create automatic backup
            String backupPath = FileOperations.createBackup();
            ExceptionLogger.getInstance().logInfo("Automatic backup created: " + backupPath);
            
            ExceptionLogger.getInstance().logInfo("System maintenance completed");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("System maintenance failed", e);
        }
    }
    
    // Shutdown cleanup
    public void shutdown() {
        try {
            ExceptionLogger.getInstance().logInfo("Shutting down ResourcePlannerManager");
            performSystemMaintenance();
            logout();
            ExceptionLogger.getInstance().logInfo("Shutdown completed successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Error during shutdown", e);
        }
    }
}