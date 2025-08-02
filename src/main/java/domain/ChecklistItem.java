package domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a checklist item with completion status and due dates
 * Extends Note for tagging and attachment support
 */
public class ChecklistItem extends Note {
    private static final long serialVersionUID = 1L;
    
    private boolean done;
    private LocalDate dueDate;
    private Priority priority;
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public ChecklistItem(String title, String description) {
        super(title, description);
        this.done = false;
        this.priority = Priority.MEDIUM;
    }
    
    public ChecklistItem(String title, String description, LocalDate dueDate, Priority priority) {
        super(title, description);
        this.done = false;
        this.dueDate = dueDate;
        this.priority = priority != null ? priority : Priority.MEDIUM;
    }
    
    // Getters and setters
    public boolean isDone() { return done; }
    public void setDone(boolean done) { 
        this.done = done; 
        updateModified();
    }
    
    public void markDone() { setDone(true); }
    public void markUndone() { setDone(false); }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { 
        this.dueDate = dueDate; 
        updateModified();
    }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { 
        this.priority = priority != null ? priority : Priority.MEDIUM; 
        updateModified();
    }
    
    // Status checks
    public boolean isOverdue() {
        return dueDate != null && !done && LocalDate.now().isAfter(dueDate);
    }
    
    public boolean isDueToday() {
        return dueDate != null && !done && LocalDate.now().equals(dueDate);
    }
    
    public boolean isDueSoon(int days) {
        if (dueDate == null || done) return false;
        LocalDate soon = LocalDate.now().plusDays(days);
        return !dueDate.isAfter(soon);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        ChecklistItem that = (ChecklistItem) obj;
        return done == that.done && 
               Objects.equals(dueDate, that.dueDate) && 
               priority == that.priority;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), done, dueDate, priority);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(done ? "[✓] " : "[ ] ");
        sb.append(title);
        
        if (priority == Priority.URGENT) sb.append(" ⚠️");
        else if (priority == Priority.HIGH) sb.append(" ❗");
        
        if (dueDate != null) {
            sb.append(" (Due: ").append(dueDate);
            if (isOverdue()) sb.append(" - OVERDUE");
            else if (isDueToday()) sb.append(" - TODAY");
            sb.append(")");
        }
        
        if (!getTags().isEmpty()) {
            sb.append(" [").append(String.join(", ", getTags())).append("]");
        }
        
        return sb.toString();
    }
}