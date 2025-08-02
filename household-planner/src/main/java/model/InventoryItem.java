package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an inventory item in the household resource planner
 */
public class InventoryItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String category;
    private double quantity;
    private String unit;
    private LocalDate expiryDate;
    private double restockThreshold;
    private LocalDate purchaseDate;
    private String notes;
    private ItemStatus status;
    
    public InventoryItem() {
        this.status = ItemStatus.OK;
        this.purchaseDate = LocalDate.now();
    }
    
    public InventoryItem(String name, String category, double quantity, String unit) {
        this();
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { 
        this.quantity = quantity;
        updateStatus();
    }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { 
        this.expiryDate = expiryDate;
        updateStatus();
    }
    
    public double getRestockThreshold() { return restockThreshold; }
    public void setRestockThreshold(double restockThreshold) { this.restockThreshold = restockThreshold; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    
    /**
     * Updates the item status based on quantity and expiry date
     */
    private void updateStatus() {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            this.status = ItemStatus.EXPIRED;
        } else if (quantity <= restockThreshold) {
            this.status = ItemStatus.LOW;
        } else {
            this.status = ItemStatus.OK;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(name, that.name) && Objects.equals(category, that.category);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, category);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f %s [%s]", 
            name, category, quantity, unit, status);
    }
}