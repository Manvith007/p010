package service;

import model.*;
import util.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing inventory items and categories
 */
public class InventoryService {
    private static final String INVENTORY_FILE = "inventory.dat";
    private static final String CATEGORIES_FILE = "categories.dat";
    
    private Map<String, InventoryItem> inventory;
    private Map<String, InventoryCategory> categories;
    
    public InventoryService() {
        this.inventory = new HashMap<>();
        this.categories = new HashMap<>();
        loadData();
    }
    
    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            // Load inventory items
            Map<String, InventoryItem> loadedInventory = FileOperations.readObjectFromFile(INVENTORY_FILE, Map.class);
            if (loadedInventory != null) {
                this.inventory = loadedInventory;
            }
            
            // Load categories
            Map<String, InventoryCategory> loadedCategories = FileOperations.readObjectFromFile(CATEGORIES_FILE, Map.class);
            if (loadedCategories != null) {
                this.categories = loadedCategories;
            }
            
            // Create default categories if none exist
            if (categories.isEmpty()) {
                createDefaultCategories();
            }
            
            ExceptionLogger.getInstance().logInfo("Inventory data loaded successfully");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load inventory data", e);
            createDefaultCategories();
        }
    }
    
    public void saveAllData() {
        try {
            FileOperations.writeObjectToFile(inventory, INVENTORY_FILE);
            FileOperations.writeObjectToFile(categories, CATEGORIES_FILE);
            ExceptionLogger.getInstance().logInfo("Inventory data saved successfully");
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to save inventory data", e);
        }
    }
    
    private void createDefaultCategories() {
        categories.put("Food", new InventoryCategory("Food"));
        categories.put("Beverages", new InventoryCategory("Beverages"));
        categories.put("Cleaning", new InventoryCategory("Cleaning"));
        categories.put("Personal Care", new InventoryCategory("Personal Care"));
        categories.put("Medicine", new InventoryCategory("Medicine"));
        categories.put("Other", new InventoryCategory("Other"));
    }
    
    // Inventory item management
    public boolean addItem(InventoryItem item) {
        if (item == null || item.getName() == null || item.getName().trim().isEmpty()) {
            return false;
        }
        
        String key = generateItemKey(item.getName(), item.getCategory());
        inventory.put(key, item);
        
        // Add to category if it exists
        InventoryCategory category = categories.get(item.getCategory());
        if (category != null) {
            category.addItem(item);
        }
        
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Added inventory item: " + item.getName());
        return true;
    }
    
    public boolean updateItem(String name, String category, InventoryItem updatedItem) {
        String key = generateItemKey(name, category);
        if (inventory.containsKey(key)) {
            inventory.put(key, updatedItem);
            
            // Update in category
            InventoryCategory cat = categories.get(category);
            if (cat != null) {
                cat.removeItem(inventory.get(key));
                cat.addItem(updatedItem);
            }
            
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Updated inventory item: " + name);
            return true;
        }
        return false;
    }
    
    public boolean removeItem(String name, String category) {
        String key = generateItemKey(name, category);
        InventoryItem removed = inventory.remove(key);
        
        if (removed != null) {
            // Remove from category
            InventoryCategory cat = categories.get(category);
            if (cat != null) {
                cat.removeItem(removed);
            }
            
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Removed inventory item: " + name);
            return true;
        }
        return false;
    }
    
    public InventoryItem getItem(String name, String category) {
        String key = generateItemKey(name, category);
        return inventory.get(key);
    }
    
    public List<InventoryItem> getAllItems() {
        return new ArrayList<>(inventory.values());
    }
    
    public List<InventoryItem> getItemsByCategory(String categoryName) {
        return inventory.values().stream()
            .filter(item -> categoryName.equals(item.getCategory()))
            .collect(Collectors.toList());
    }
    
    public List<InventoryItem> searchItems(String searchTerm) {
        String lowerTerm = searchTerm.toLowerCase();
        return inventory.values().stream()
            .filter(item -> item.getName().toLowerCase().contains(lowerTerm) ||
                          (item.getNotes() != null && item.getNotes().toLowerCase().contains(lowerTerm)))
            .collect(Collectors.toList());
    }
    
    public List<InventoryItem> getExpiredItems() {
        LocalDate today = LocalDate.now();
        return inventory.values().stream()
            .filter(item -> item.getExpiryDate() != null && item.getExpiryDate().isBefore(today))
            .collect(Collectors.toList());
    }
    
    public List<InventoryItem> getLowStockItems() {
        return inventory.values().stream()
            .filter(item -> item.getQuantity() <= item.getRestockThreshold())
            .collect(Collectors.toList());
    }
    
    public List<InventoryItem> getItemsExpiringWithinDays(int days) {
        LocalDate cutoffDate = LocalDate.now().plusDays(days);
        return inventory.values().stream()
            .filter(item -> item.getExpiryDate() != null && 
                          item.getExpiryDate().isAfter(LocalDate.now()) &&
                          item.getExpiryDate().isBefore(cutoffDate))
            .collect(Collectors.toList());
    }
    
    // Category management
    public boolean addCategory(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty() || categories.containsKey(categoryName)) {
            return false;
        }
        
        categories.put(categoryName, new InventoryCategory(categoryName));
        saveAllData();
        ExceptionLogger.getInstance().logInfo("Added category: " + categoryName);
        return true;
    }
    
    public boolean removeCategory(String categoryName) {
        if ("Other".equals(categoryName)) {
            return false; // Don't allow removing the default "Other" category
        }
        
        InventoryCategory removed = categories.remove(categoryName);
        if (removed != null) {
            // Move items to "Other" category
            InventoryCategory otherCategory = categories.get("Other");
            for (InventoryItem item : removed.getItems()) {
                item.setCategory("Other");
                if (otherCategory != null) {
                    otherCategory.addItem(item);
                }
            }
            
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Removed category: " + categoryName);
            return true;
        }
        return false;
    }
    
    public List<InventoryCategory> getAllCategories() {
        return new ArrayList<>(categories.values());
    }
    
    public Set<String> getCategoryNames() {
        return new HashSet<>(categories.keySet());
    }
    
    // Utility methods
    private String generateItemKey(String name, String category) {
        return (category != null ? category : "Other") + ":" + name;
    }
    
    public void restockItem(String name, String category, double quantity) {
        String key = generateItemKey(name, category);
        InventoryItem item = inventory.get(key);
        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
            item.setPurchaseDate(LocalDate.now());
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Restocked item: " + name + " (+" + quantity + ")");
        }
    }
    
    public void consumeItem(String name, String category, double quantity) {
        String key = generateItemKey(name, category);
        InventoryItem item = inventory.get(key);
        if (item != null) {
            double newQuantity = Math.max(0, item.getQuantity() - quantity);
            item.setQuantity(newQuantity);
            saveAllData();
            ExceptionLogger.getInstance().logInfo("Consumed item: " + name + " (-" + quantity + ")");
        }
    }
    
    // Sorting and filtering
    public List<InventoryItem> sortItemsByName() {
        return inventory.values().stream()
            .sorted(Comparator.comparing(InventoryItem::getName))
            .collect(Collectors.toList());
    }
    
    public List<InventoryItem> sortItemsByExpiryDate() {
        return inventory.values().stream()
            .filter(item -> item.getExpiryDate() != null)
            .sorted(Comparator.comparing(InventoryItem::getExpiryDate))
            .collect(Collectors.toList());
    }
    
    public List<InventoryItem> sortItemsByQuantity() {
        return inventory.values().stream()
            .sorted(Comparator.comparing(InventoryItem::getQuantity))
            .collect(Collectors.toList());
    }
}