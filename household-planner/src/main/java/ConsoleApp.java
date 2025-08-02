import manager.ResourcePlannerManager;
import model.*;
import service.*;
import util.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Console-based Household Resource Planner & Inventory Management application
 */
public class ConsoleApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static ResourcePlannerManager manager;
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("HOUSEHOLD RESOURCE PLANNER & INVENTORY MANAGEMENT CONSOLE");
        System.out.println("=".repeat(60));
        
        try {
            manager = ResourcePlannerManager.getInstance();
            showLoginMenu();
            
            if (manager.isLoggedIn()) {
                showMainMenu();
            }
        } catch (Exception e) {
            System.err.println("Critical error: " + e.getMessage());
            ExceptionLogger.getInstance().logError("Critical application error", e);
        } finally {
            if (manager != null) {
                manager.shutdown();
            }
            scanner.close();
        }
    }
    
    private static void showLoginMenu() {
        while (!manager.isLoggedIn()) {
            System.out.println("\n=== LOGIN ===");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.print("Enter choice: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    performLogin();
                    break;
                case "2":
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void performLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("PIN: ");
        String pin = scanner.nextLine().trim();
        
        if (manager.login(username, pin)) {
            System.out.println("Login successful! Welcome, " + manager.getCurrentUser().getDisplayName());
            showUrgentNotifications();
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }
    
    private static void showUrgentNotifications() {
        List<String> notifications = manager.getUrgentNotifications();
        if (!notifications.isEmpty()) {
            System.out.println("\n⚠️  URGENT NOTIFICATIONS:");
            System.out.println("-".repeat(50));
            for (String notification : notifications) {
                System.out.println("• " + notification);
            }
            System.out.println("-".repeat(50));
            pauseForUser();
        }
    }
    
    private static void showMainMenu() {
        while (manager.isLoggedIn()) {
            System.out.println("\n=== HOUSEHOLD RESOURCE PLANNER & INVENTORY ===");
            System.out.println("Current User: " + manager.getCurrentUser().getDisplayName() + 
                             " (" + manager.getCurrentUser().getRole() + ")");
            System.out.println("1. Inventory Management");
            System.out.println("2. Shopping Lists & Auto Suggestions");
            System.out.println("3. Maintenance & Appliance Schedules");
            System.out.println("4. Bill Payments & Recurring Reminders");
            System.out.println("5. Household Chores & Task Assignment");
            System.out.println("6. Multi-User Access & Permissions");
            System.out.println("7. Export/Backup All Data");
            System.out.println("8. Import/Restore Data");
            System.out.println("9. Vault Settings");
            System.out.println("0. Exit");
            System.out.println("=".repeat(50));
            
            showDashboardSummary();
            
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    showInventoryMenu();
                    break;
                case "2":
                    showShoppingMenu();
                    break;
                case "3":
                    showMaintenanceMenu();
                    break;
                case "4":
                    showBillsMenu();
                    break;
                case "5":
                    showChoresMenu();
                    break;
                case "6":
                    showUserManagementMenu();
                    break;
                case "7":
                    showExportMenu();
                    break;
                case "8":
                    showImportMenu();
                    break;
                case "9":
                    showVaultSettings();
                    break;
                case "0":
                    manager.logout();
                    System.out.println("Logged out successfully. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void showDashboardSummary() {
        Map<String, Object> summary = manager.getDashboardSummary();
        System.out.println("📊 Dashboard Summary:");
        System.out.printf("   Inventory: %d items (%d expired, %d low stock)%n",
            summary.get("totalInventoryItems"), summary.get("expiredItems"), summary.get("lowStockItems"));
        System.out.printf("   Shopping: %d lists (%d incomplete)%n",
            summary.get("totalShoppingLists"), summary.get("incompleteShoppingLists"));
        System.out.printf("   Chores: %d total (%d overdue, %d pending)%n",
            summary.get("totalChores"), summary.get("overdueChores"), summary.get("pendingChores"));
        System.out.printf("   Bills: %d total (%d overdue, $%.2f unpaid)%n",
            summary.get("totalBills"), summary.get("overdueBills"), summary.get("totalUnpaidAmount"));
    }
    
    private static void showInventoryMenu() {
        if (!manager.hasPermission("VIEW_INVENTORY")) {
            System.out.println("Access denied: Insufficient permissions");
            return;
        }
        
        while (true) {
            System.out.println("\n=== INVENTORY MANAGEMENT ===");
            System.out.println("1. Add Inventory Item");
            System.out.println("2. View/Update/Delete Item");
            System.out.println("3. View Items by Category");
            System.out.println("4. Search Items");
            System.out.println("5. View Expired Items");
            System.out.println("6. View Low Stock Items");
            System.out.println("7. Restock Item");
            System.out.println("8. Consume Item");
            System.out.println("9. Manage Categories");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter choice: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addInventoryItem();
                    break;
                case "2":
                    manageInventoryItem();
                    break;
                case "3":
                    viewItemsByCategory();
                    break;
                case "4":
                    searchInventoryItems();
                    break;
                case "5":
                    viewExpiredItems();
                    break;
                case "6":
                    viewLowStockItems();
                    break;
                case "7":
                    restockItem();
                    break;
                case "8":
                    consumeItem();
                    break;
                case "9":
                    manageCategories();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void addInventoryItem() {
        if (!manager.hasPermission("MANAGE_INVENTORY")) {
            System.out.println("Access denied: Cannot add inventory items");
            return;
        }
        
        System.out.println("\n--- Add Inventory Item ---");
        
        System.out.print("Item name: ");
        String name = scanner.nextLine().trim();
        
        // Show available categories
        Set<String> categories = manager.getInventoryService().getCategoryNames();
        System.out.println("Available categories: " + categories);
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        
        System.out.print("Quantity: ");
        double quantity = getDoubleInput();
        
        System.out.print("Unit (e.g., kg, pieces, liters): ");
        String unit = scanner.nextLine().trim();
        
        System.out.print("Expiry date (YYYY-MM-DD, optional): ");
        String expiryStr = scanner.nextLine().trim();
        LocalDate expiryDate = null;
        if (!expiryStr.isEmpty()) {
            expiryDate = parseDateInput(expiryStr);
        }
        
        System.out.print("Restock threshold: ");
        double restockThreshold = getDoubleInput();
        
        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();
        
        InventoryItem item = new InventoryItem(name, category, quantity, unit);
        item.setExpiryDate(expiryDate);
        item.setRestockThreshold(restockThreshold);
        if (!notes.isEmpty()) {
            item.setNotes(notes);
        }
        
        if (manager.getInventoryService().addItem(item)) {
            System.out.println("✅ Item added successfully!");
        } else {
            System.out.println("❌ Failed to add item.");
        }
    }
    
    private static void showShoppingMenu() {
        if (!manager.hasPermission("VIEW_SHOPPING_LISTS")) {
            System.out.println("Access denied: Insufficient permissions");
            return;
        }
        
        while (true) {
            System.out.println("\n=== SHOPPING LISTS & AUTO SUGGESTIONS ===");
            System.out.println("1. Create Shopping List");
            System.out.println("2. View Shopping Lists");
            System.out.println("3. Add Item to List");
            System.out.println("4. Mark Items as Purchased");
            System.out.println("5. Auto-Generate from Inventory");
            System.out.println("6. View Suggestions");
            System.out.println("7. Export Shopping List");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter choice: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    createShoppingList();
                    break;
                case "2":
                    viewShoppingLists();
                    break;
                case "3":
                    addItemToShoppingList();
                    break;
                case "4":
                    markItemsAsPurchased();
                    break;
                case "5":
                    autoGenerateShoppingList();
                    break;
                case "6":
                    viewShoppingSuggestions();
                    break;
                case "7":
                    exportShoppingList();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void showChoresMenu() {
        while (true) {
            System.out.println("\n=== HOUSEHOLD CHORES & TASK ASSIGNMENT ===");
            System.out.println("1. Add Chore");
            System.out.println("2. View All Chores");
            System.out.println("3. View My Chores");
            System.out.println("4. Mark Chore Complete");
            System.out.println("5. View Overdue Chores");
            System.out.println("6. Assign Chore");
            System.out.println("7. Chore Statistics");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter choice: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addChore();
                    break;
                case "2":
                    viewAllChores();
                    break;
                case "3":
                    viewMyChores();
                    break;
                case "4":
                    markChoreComplete();
                    break;
                case "5":
                    viewOverdueChores();
                    break;
                case "6":
                    assignChore();
                    break;
                case "7":
                    showChoreStatistics();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void showBillsMenu() {
        while (true) {
            System.out.println("\n=== BILL PAYMENTS & RECURRING REMINDERS ===");
            System.out.println("1. Add Bill");
            System.out.println("2. View All Bills");
            System.out.println("3. View Overdue Bills");
            System.out.println("4. View Bills Due Soon");
            System.out.println("5. Mark Bill as Paid");
            System.out.println("6. Bill Statistics");
            System.out.println("7. Payment Reminders");
            System.out.println("0. Return to Main Menu");
            System.out.print("Enter choice: ");
            
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    addBill();
                    break;
                case "2":
                    viewAllBills();
                    break;
                case "3":
                    viewOverdueBills();
                    break;
                case "4":
                    viewBillsDueSoon();
                    break;
                case "5":
                    markBillAsPaid();
                    break;
                case "6":
                    showBillStatistics();
                    break;
                case "7":
                    showPaymentReminders();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    // Implementation of menu methods continues...
    private static void createShoppingList() {
        if (!manager.hasPermission("MANAGE_SHOPPING_LISTS")) {
            System.out.println("Access denied: Cannot create shopping lists");
            return;
        }
        
        System.out.print("Shopping list name: ");
        String name = scanner.nextLine().trim();
        
        if (manager.getShoppingService().createShoppingList(name)) {
            System.out.println("✅ Shopping list created successfully!");
        } else {
            System.out.println("❌ Failed to create shopping list (name may already exist).");
        }
    }
    
    private static void addChore() {
        if (!manager.hasPermission("MANAGE_CHORES")) {
            System.out.println("Access denied: Cannot add chores");
            return;
        }
        
        System.out.println("\n--- Add Chore ---");
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        List<String> users = manager.getUserService().getActiveUsernames();
        System.out.println("Available users: " + users);
        System.out.print("Assignee: ");
        String assignee = scanner.nextLine().trim();
        
        System.out.print("Due date (YYYY-MM-DD): ");
        String dueDateStr = scanner.nextLine().trim();
        LocalDate dueDate = parseDateInput(dueDateStr);
        
        System.out.println("Priority: LOW, MEDIUM, HIGH, URGENT");
        System.out.print("Priority: ");
        String priorityStr = scanner.nextLine().trim().toUpperCase();
        Chore.Priority priority = Chore.Priority.MEDIUM;
        try {
            priority = Chore.Priority.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid priority, using MEDIUM");
        }
        
        System.out.print("Recurring (true/false): ");
        boolean recurring = getBooleanInput();
        
        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();
        
        Chore chore = new Chore(description, assignee, dueDate);
        chore.setPriority(priority);
        chore.setRecurring(recurring);
        if (!notes.isEmpty()) {
            chore.setNotes(notes);
        }
        
        String choreId = manager.getChoreService().addChore(chore);
        if (choreId != null) {
            System.out.println("✅ Chore added successfully! ID: " + choreId);
        } else {
            System.out.println("❌ Failed to add chore.");
        }
    }
    
    private static void addBill() {
        System.out.println("\n--- Add Bill ---");
        
        System.out.print("Utility name: ");
        String utilityName = scanner.nextLine().trim();
        
        System.out.print("Type (electricity, water, gas, internet, etc.): ");
        String type = scanner.nextLine().trim();
        
        System.out.print("Amount: $");
        double amount = getDoubleInput();
        
        System.out.print("Due date (YYYY-MM-DD): ");
        String dueDateStr = scanner.nextLine().trim();
        LocalDate dueDate = parseDateInput(dueDateStr);
        
        BillUtility bill = new BillUtility(utilityName, type, amount, dueDate);
        
        System.out.print("Recurring (true/false): ");
        boolean recurring = getBooleanInput();
        bill.setRecurring(recurring);
        
        System.out.print("Notes (optional): ");
        String notes = scanner.nextLine().trim();
        if (!notes.isEmpty()) {
            bill.setNotes(notes);
        }
        
        String billId = manager.getBillService().addBill(bill);
        if (billId != null) {
            System.out.println("✅ Bill added successfully! ID: " + billId);
        } else {
            System.out.println("❌ Failed to add bill.");
        }
    }
    
    // Stub implementations for other methods
    private static void manageInventoryItem() { System.out.println("Feature coming soon..."); }
    private static void viewItemsByCategory() { 
        Set<String> categories = manager.getInventoryService().getCategoryNames();
        System.out.println("Categories: " + categories);
        System.out.print("Enter category name: ");
        String category = scanner.nextLine().trim();
        
        List<InventoryItem> items = manager.getInventoryService().getItemsByCategory(category);
        if (items.isEmpty()) {
            System.out.println("No items found in category: " + category);
        } else {
            System.out.println("\nItems in " + category + ":");
            items.forEach(item -> System.out.println("• " + item));
        }
    }
    
    private static void searchInventoryItems() { System.out.println("Feature coming soon..."); }
    private static void viewExpiredItems() {
        List<InventoryItem> expired = manager.getInventoryService().getExpiredItems();
        if (expired.isEmpty()) {
            System.out.println("No expired items found.");
        } else {
            System.out.println("\n🔴 Expired Items:");
            expired.forEach(item -> System.out.println("• " + item));
        }
    }
    
    private static void viewLowStockItems() {
        List<InventoryItem> lowStock = manager.getInventoryService().getLowStockItems();
        if (lowStock.isEmpty()) {
            System.out.println("No low stock items found.");
        } else {
            System.out.println("\n🟡 Low Stock Items:");
            lowStock.forEach(item -> System.out.println("• " + item));
        }
    }
    
    private static void restockItem() { System.out.println("Feature coming soon..."); }
    private static void consumeItem() { System.out.println("Feature coming soon..."); }
    private static void manageCategories() { System.out.println("Feature coming soon..."); }
    private static void showMaintenanceMenu() { System.out.println("Feature coming soon..."); }
    private static void showUserManagementMenu() { System.out.println("Feature coming soon..."); }
    private static void showExportMenu() { System.out.println("Feature coming soon..."); }
    private static void showImportMenu() { System.out.println("Feature coming soon..."); }
    private static void showVaultSettings() { System.out.println("Feature coming soon..."); }
    private static void viewShoppingLists() { System.out.println("Feature coming soon..."); }
    private static void addItemToShoppingList() { System.out.println("Feature coming soon..."); }
    private static void markItemsAsPurchased() { System.out.println("Feature coming soon..."); }
    private static void autoGenerateShoppingList() { System.out.println("Feature coming soon..."); }
    private static void viewShoppingSuggestions() { System.out.println("Feature coming soon..."); }
    private static void exportShoppingList() { System.out.println("Feature coming soon..."); }
    private static void viewAllChores() { 
        List<Chore> chores = manager.getChoreService().getAllChores();
        if (chores.isEmpty()) {
            System.out.println("No chores found.");
        } else {
            System.out.println("\nAll Chores:");
            chores.forEach(chore -> System.out.println("• " + chore));
        }
    }
    
    private static void viewMyChores() {
        String currentUser = manager.getCurrentUser().getUsername();
        List<Chore> myChores = manager.getChoreService().getChoresByAssignee(currentUser);
        if (myChores.isEmpty()) {
            System.out.println("No chores assigned to you.");
        } else {
            System.out.println("\nYour Chores:");
            myChores.forEach(chore -> System.out.println("• " + chore));
        }
    }
    
    private static void markChoreComplete() { System.out.println("Feature coming soon..."); }
    private static void viewOverdueChores() {
        List<Chore> overdue = manager.getChoreService().getOverdueChores();
        if (overdue.isEmpty()) {
            System.out.println("No overdue chores found.");
        } else {
            System.out.println("\n🔴 Overdue Chores:");
            overdue.forEach(chore -> System.out.println("• " + chore));
        }
    }
    
    private static void assignChore() { System.out.println("Feature coming soon..."); }
    private static void showChoreStatistics() { System.out.println("Feature coming soon..."); }
    private static void viewAllBills() {
        List<BillUtility> bills = manager.getBillService().getAllBills();
        if (bills.isEmpty()) {
            System.out.println("No bills found.");
        } else {
            System.out.println("\nAll Bills:");
            bills.forEach(bill -> System.out.println("• " + bill));
        }
    }
    
    private static void viewOverdueBills() {
        List<BillUtility> overdue = manager.getBillService().getOverdueBills();
        if (overdue.isEmpty()) {
            System.out.println("No overdue bills found.");
        } else {
            System.out.println("\n🔴 Overdue Bills:");
            overdue.forEach(bill -> System.out.println("• " + bill));
        }
    }
    
    private static void viewBillsDueSoon() { System.out.println("Feature coming soon..."); }
    private static void markBillAsPaid() { System.out.println("Feature coming soon..."); }
    private static void showBillStatistics() { System.out.println("Feature coming soon..."); }
    private static void showPaymentReminders() { System.out.println("Feature coming soon..."); }
    
    // Utility methods
    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid number. Please try again: ");
            }
        }
    }
    
    private static boolean getBooleanInput() {
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("true") || input.equals("t") || input.equals("yes") || input.equals("y")) {
                return true;
            } else if (input.equals("false") || input.equals("f") || input.equals("no") || input.equals("n")) {
                return false;
            } else {
                System.out.print("Invalid input. Please enter true/false: ");
            }
        }
    }
    
    private static LocalDate parseDateInput(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Using today's date.");
            return LocalDate.now();
        }
    }
    
    private static void pauseForUser() {
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }
}