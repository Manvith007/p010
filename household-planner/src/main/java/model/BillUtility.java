package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;

/**
 * Represents a utility bill with recurring payment tracking
 */
public class BillUtility implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String utilityName;
    private String type; // electricity, water, gas, internet, etc.
    private double amount;
    private LocalDate dueDate;
    private Period recurringInterval;
    private boolean isPaid;
    private boolean isRecurring;
    private String notes;
    private LocalDate lastPaidDate;
    
    public BillUtility() {
        this.isPaid = false;
        this.isRecurring = true;
        this.recurringInterval = Period.ofMonths(1); // Default monthly
    }
    
    public BillUtility(String utilityName, String type, double amount, LocalDate dueDate) {
        this();
        this.utilityName = utilityName;
        this.type = type;
        this.amount = amount;
        this.dueDate = dueDate;
    }
    
    public String getUtilityName() { return utilityName; }
    public void setUtilityName(String utilityName) { this.utilityName = utilityName; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public Period getRecurringInterval() { return recurringInterval; }
    public void setRecurringInterval(Period recurringInterval) { this.recurringInterval = recurringInterval; }
    
    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }
    
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDate getLastPaidDate() { return lastPaidDate; }
    public void setLastPaidDate(LocalDate lastPaidDate) { this.lastPaidDate = lastPaidDate; }
    
    public void markAsPaid() {
        this.isPaid = true;
        this.lastPaidDate = LocalDate.now();
        if (isRecurring) {
            this.dueDate = this.dueDate.plus(recurringInterval);
            this.isPaid = false; // Reset for next cycle
        }
    }
    
    public boolean isOverdue() {
        return !isPaid && dueDate.isBefore(LocalDate.now());
    }
    
    public long getDaysUntilDue() {
        return LocalDate.now().until(dueDate).getDays();
    }
    
    @Override
    public String toString() {
        String status = isPaid ? "[PAID]" : isOverdue() ? "[OVERDUE]" : "[DUE]";
        return String.format("%s %s (%s) - $%.2f due %s", 
            status, utilityName, type, amount, dueDate);
    }
}