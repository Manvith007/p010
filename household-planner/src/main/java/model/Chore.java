package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a household chore or task
 */
public class Chore implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Priority {
        LOW("Low"), MEDIUM("Medium"), HIGH("High"), URGENT("Urgent");
        
        private final String description;
        Priority(String description) { this.description = description; }
        public String getDescription() { return description; }
        @Override
        public String toString() { return description; }
    }
    
    public enum Status {
        PENDING("Pending"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), CANCELLED("Cancelled");
        
        private final String description;
        Status(String description) { this.description = description; }
        public String getDescription() { return description; }
        @Override
        public String toString() { return description; }
    }
    
    private String description;
    private String assignee;
    private LocalDate dueDate;
    private Priority priority;
    private boolean isRecurring;
    private Status status;
    private String notes;
    private LocalDateTime completedDate;
    private LocalDate createdDate;
    
    public Chore() {
        this.status = Status.PENDING;
        this.priority = Priority.MEDIUM;
        this.createdDate = LocalDate.now();
        this.isRecurring = false;
    }
    
    public Chore(String description, String assignee, LocalDate dueDate) {
        this();
        this.description = description;
        this.assignee = assignee;
        this.dueDate = dueDate;
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    
    public void markAsCompleted() {
        this.status = Status.COMPLETED;
        this.completedDate = LocalDateTime.now();
    }
    
    public boolean isOverdue() {
        return status != Status.COMPLETED && dueDate != null && dueDate.isBefore(LocalDate.now());
    }
    
    @Override
    public String toString() {
        String overdue = isOverdue() ? "[OVERDUE] " : "";
        return String.format("%s%s - %s [%s] (%s) Due: %s", 
            overdue, description, assignee, priority, status, dueDate);
    }
}