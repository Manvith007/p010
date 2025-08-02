package service;

import model.*;
import util.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing shopping lists
 */
public class ShoppingService {
    private static final String SHOPPING_LISTS_FILE = "shopping_lists.dat";
    
    private Map<String, ShoppingList> shoppingLists;
    
    public ShoppingService() {
        this.shoppingLists = new HashMap<>();
        loadData();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            Map<String, ShoppingList> loadedLists = FileOperations.readObjectFromFile(SHOPPING_LISTS_FILE, Map.class);
            if (loadedLists != null) {
                this.shoppingLists = loadedLists;
            }
            ExceptionLogger.getInstance().logInfo("Shopping lists data loaded successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load shopping lists data", e);
        }
    }
    
    public void saveAllData() {
        try {
            FileOperations.writeObjectToFile(shoppingLists, SHOPPING_LISTS_FILE);
            ExceptionLogger.getInstance().logInfo("Shopping lists data saved successfully");
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to save shopping lists data", e);
        }
    }
    
    // Shopping list management
    public boolean createShoppingList(String name) {
        if (name == null || name.trim().isEmpty() || shoppingLists.containsKey(name)) {
            return false;
        }
        
        ShoppingList newList = new ShoppingList(name);
        shoppingLists.put(name, newList);
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Created shopping list: " + name);
        return true;
    }
    
    public boolean deleteShoppingList(String name) {
        ShoppingList removed = shoppingLists.remove(name);
        if (removed != null) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Deleted shopping list: " + name);
            return true;
        }
        return false;
    }
    
    public ShoppingList getShoppingList(String name) {
        return shoppingLists.get(name);
    }
    
    public List<ShoppingList> getAllShoppingLists() {
        return new ArrayList<>(shoppingLists.values());
    }
    
    public List<ShoppingList> getIncompleteShoppingLists() {
        return shoppingLists.values().stream()
            .filter(list -> !list.isCompleted())
            .collect(Collectors.toList());
    }
    
    // Shopping item management
    public boolean addItemToList(String listName, ShoppingItem item) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null || item == null) {
            return false;
        }
        
        list.addItem(item);
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Added item to shopping list " + listName + ": " + item.getName());
        return true;
    }
    
    public boolean removeItemFromList(String listName, ShoppingItem item) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null || item == null) {
            return false;
        }
        
        boolean removed = list.removeItem(item);
        if (removed) {
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Removed item from shopping list " + listName + ": " + item.getName());
        }
        return removed;
    }
    
    public boolean markItemAsPurchased(String listName, String itemName) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null) {
            return false;
        }
        
        for (ShoppingItem item : list.getItems()) {
            if (item.getName().equals(itemName)) {
                item.setPurchased(true);
                saveAllData();
                ExceptionLogger.getInstance().logInfo("Marked item as purchased: " + itemName);
                return true;
            }
        }
        return false;
    }
    
    public boolean markItemAsNotPurchased(String listName, String itemName) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null) {
            return false;
        }
        
        for (ShoppingItem item : list.getItems()) {
            if (item.getName().equals(itemName)) {
                item.setPurchased(false);
                saveAllData();
                ExceptionLogger.getInstance().logInfo("Marked item as not purchased: " + itemName);
                return true;
            }
        }
        return false;
    }
    
    // Auto-suggestion based on inventory
    public List<String> suggestItemsBasedOnInventory(InventoryService inventoryService) {
        List<String> suggestions = new ArrayList<>();
        
        // Add expired items
        List<InventoryItem> expiredItems = inventoryService.getExpiredItems();
        for (InventoryItem item : expiredItems) {
            suggestions.add(item.getName() + " (expired - replace)");
        }
        
        // Add low stock items
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        for (InventoryItem item : lowStockItems) {
            suggestions.add(item.getName() + " (low stock - restock)");
        }
        
        // Add items expiring soon (within 7 days)
        List<InventoryItem> expiringSoon = inventoryService.getItemsExpiringWithinDays(7);
        for (InventoryItem item : expiringSoon) {
            suggestions.add(item.getName() + " (expires " + item.getExpiryDate() + ")");
        }
        
        return suggestions;
    }
    
    // Auto-generate shopping list from low stock items
    public boolean generateShoppingListFromInventory(String listName, InventoryService inventoryService) {
        if (shoppingLists.containsKey(listName)) {
            return false; // List already exists
        }
        
        ShoppingList autoList = new ShoppingList(listName);
        autoList.setNotes("Auto-generated from inventory analysis");
        
        // Add low stock items
        List<InventoryItem> lowStockItems = inventoryService.getLowStockItems();
        for (InventoryItem item : lowStockItems) {
            ShoppingItem shoppingItem = new ShoppingItem(item.getName(), 
                item.getRestockThreshold() - item.getQuantity() + 1, item.getUnit());
            shoppingItem.setCategory(item.getCategory());
            shoppingItem.setNotes("Restock - current: " + item.getQuantity() + " " + item.getUnit());
            autoList.addItem(shoppingItem);
        }
        
        // Add expired items
        List<InventoryItem> expiredItems = inventoryService.getExpiredItems();
        for (InventoryItem item : expiredItems) {
            ShoppingItem shoppingItem = new ShoppingItem(item.getName(), 1.0, item.getUnit());
            shoppingItem.setCategory(item.getCategory());
            shoppingItem.setNotes("Replace expired item");
            autoList.addItem(shoppingItem);
        }
        
        if (autoList.getTotalItems() > 0) {
            shoppingLists.put(listName, autoList);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Auto-generated shopping list: " + listName + " with " + autoList.getTotalItems() + " items");
            return true;
        }
        
        return false;
    }
    
    // List completion
    public boolean markListAsCompleted(String listName) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null) {
            return false;
        }
        
        list.setCompleted(true);
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Marked shopping list as completed: " + listName);
        return true;
    }
    
    public boolean markListAsIncomplete(String listName) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null) {
            return false;
        }
        
        list.setCompleted(false);
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Marked shopping list as incomplete: " + listName);
        return true;
    }
    
    // Search and filter
    public List<ShoppingList> searchShoppingLists(String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return shoppingLists.values().stream()
            .filter(list -> list.getName().toLowerCase().contains(lowerTerm) ||
                          (list.getNotes() != null && list.getNotes().toLowerCase().contains(lowerTerm)))
            .collect(Collectors.toList());
    }
    
    public List<ShoppingItem> searchItemsInList(String listName, String searchTerm) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null) {
            return new ArrayList<>();
        }
        
        String lowerTerm = searchTerm.toLowerCase();
        return list.getItems().stream()
            .filter(item -> item.getName().toLowerCase().contains(lowerTerm) ||
                          (item.getNotes() != null && item.getNotes().toLowerCase().contains(lowerTerm)))
            .collect(Collectors.toList());
    }
    
    // Statistics
    public Map<String, Object> getShoppingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalLists", shoppingLists.size());
        stats.put("completedLists", shoppingLists.values().stream().filter(ShoppingList::isCompleted).count());
        stats.put("incompleteLists", shoppingLists.values().stream().filter(list -> !list.isCompleted()).count());
        
        int totalItems = shoppingLists.values().stream().mapToInt(ShoppingList::getTotalItems).sum();
        int completedItems = shoppingLists.values().stream().mapToInt(ShoppingList::getCompletedItems).sum();
        
        stats.put("totalItems", totalItems);
        stats.put("completedItems", completedItems);
        stats.put("pendingItems", totalItems - completedItems);
        
        return stats;
    }
    
    // Export to text
    public String exportListToText(String listName) {
        ShoppingList list = shoppingLists.get(listName);
        if (list == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Shopping List: ").append(list.getName()).append("\n");
        sb.append("Created: ").append(list.getCreatedDate()).append("\n");
        sb.append("Status: ").append(list.isCompleted() ? "Completed" : "Incomplete").append("\n");
        sb.append("Progress: ").append(list.getCompletedItems()).append("/").append(list.getTotalItems())
          .append(" items (").append(String.format("%.1f", list.getCompletionPercentage())).append("%)\n\n");
        
        if (list.getNotes() != null && !list.getNotes().trim().isEmpty()) {
            sb.append("Notes: ").append(list.getNotes()).append("\n\n");
        }
        
        sb.append("Items:\n");
        for (ShoppingItem item : list.getItems()) {
            sb.append(item.toString()).append("\n");
        }
        
        return sb.toString();
    }
}