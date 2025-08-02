package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shopping list for household items
 */
public class ShoppingList implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<ShoppingItem> items;
    private LocalDate createdDate;
    private boolean isCompleted;
    private String notes;
    
    public ShoppingList() {
        this.items = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.isCompleted = false;
    }
    
    public ShoppingList(String name) {
        this();
        this.name = name;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public List<ShoppingItem> getItems() { return items; }
    public void setItems(List<ShoppingItem> items) { this.items = items; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public void addItem(ShoppingItem item) {
        this.items.add(item);
    }
    
    public boolean removeItem(ShoppingItem item) {
        return this.items.remove(item);
    }
    
    public int getTotalItems() {
        return items.size();
    }
    
    public int getCompletedItems() {
        return (int) items.stream().filter(ShoppingItem::isPurchased).count();
    }
    
    public double getCompletionPercentage() {
        if (items.isEmpty()) return 0.0;
        return (double) getCompletedItems() / getTotalItems() * 100.0;
    }
    
    @Override
    public String toString() {
        return String.format("%s - %d/%d items completed (%.1f%%)", 
            name, getCompletedItems(), getTotalItems(), getCompletionPercentage());
    }
}