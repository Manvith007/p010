package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a household appliance with maintenance tracking
 */
public class Appliance implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String type;
    private LocalDate purchaseDate;
    private Period maintenanceInterval;
    private LocalDate lastMaintenanceDate;
    private List<String> maintenanceLog;
    private boolean isActive;
    private String notes;
    
    public Appliance() {
        this.maintenanceLog = new ArrayList<>();
        this.isActive = true;
        this.purchaseDate = LocalDate.now();
    }
    
    public Appliance(String name, String type) {
        this();
        this.name = name;
        this.type = type;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Period getMaintenanceInterval() { return maintenanceInterval; }
    public void setMaintenanceInterval(Period maintenanceInterval) { this.maintenanceInterval = maintenanceInterval; }
    
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    
    public List<String> getMaintenanceLog() { return maintenanceLog; }
    public void setMaintenanceLog(List<String> maintenanceLog) { this.maintenanceLog = maintenanceLog; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public void addMaintenanceEntry(String entry) {
        this.maintenanceLog.add(LocalDate.now() + ": " + entry);
        this.lastMaintenanceDate = LocalDate.now();
    }
    
    public LocalDate getNextMaintenanceDate() {
        if (lastMaintenanceDate == null || maintenanceInterval == null) {
            return null;
        }
        return lastMaintenanceDate.plus(maintenanceInterval);
    }
    
    public boolean isMaintenanceDue() {
        LocalDate nextMaintenance = getNextMaintenanceDate();
        return nextMaintenance != null && !nextMaintenance.isAfter(LocalDate.now());
    }
    
    @Override
    public String toString() {
        String status = isMaintenanceDue() ? "[MAINTENANCE DUE]" : "[OK]";
        return String.format("%s %s (%s) %s", status, name, type, 
            getNextMaintenanceDate() != null ? "Next: " + getNextMaintenanceDate() : "");
    }
}