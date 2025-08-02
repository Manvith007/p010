package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents an individual item in a shopping list
 */
public class ShoppingItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String category;
    private double quantity;
    private String unit;
    private boolean isPurchased;
    private String notes;
    private LocalDate dateAdded;
    private double estimatedPrice;
    
    public ShoppingItem() {
        this.dateAdded = LocalDate.now();
        this.isPurchased = false;
    }
    
    public ShoppingItem(String name, double quantity, String unit) {
        this();
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public boolean isPurchased() { return isPurchased; }
    public void setPurchased(boolean purchased) { isPurchased = purchased; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDate getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDate dateAdded) { this.dateAdded = dateAdded; }
    
    public double getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(double estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    
    @Override
    public String toString() {
        String status = isPurchased ? "[✓]" : "[ ]";
        return String.format("%s %s - %.2f %s", status, name, quantity, unit);
    }
}