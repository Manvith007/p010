package model;

/**
 * Enumeration representing the status of an inventory item
 */
public enum ItemStatus {
    OK("Available"),
    LOW("Low Stock"),
    EXPIRED("Expired");
    
    private final String description;
    
    ItemStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return description;
    }
}