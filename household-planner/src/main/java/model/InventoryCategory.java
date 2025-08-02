package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a category for organizing inventory items
 */
public class InventoryCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String categoryName;
    private Set<InventoryItem> items;
    
    public InventoryCategory() {
        this.items = new HashSet<>();
    }
    
    public InventoryCategory(String categoryName) {
        this();
        this.categoryName = categoryName;
    }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public Set<InventoryItem> getItems() { return items; }
    public void setItems(Set<InventoryItem> items) { this.items = items; }
    
    public void addItem(InventoryItem item) {
        item.setCategory(this.categoryName);
        this.items.add(item);
    }
    
    public boolean removeItem(InventoryItem item) {
        return this.items.remove(item);
    }
    
    public int getItemCount() {
        return items.size();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%d items)", categoryName, items.size());
    }
}