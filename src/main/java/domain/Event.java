package domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a scheduled event with recurrence and reminder support
 * Implements Serializable for persistence
 */
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean recurring;
    private RecurrenceType recurrenceType;
    private int recurrenceInterval; // e.g., every 2 weeks
    private Set<String> tags;
    private boolean reminderSet;
    private int reminderMinutes; // minutes before event
    
    public enum RecurrenceType {
        NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    public Event(String description, LocalDateTime start, LocalDateTime end) {
        this.description = description;
        this.start = start;
        this.end = end;
        this.recurring = false;
        this.recurrenceType = RecurrenceType.NONE;
        this.recurrenceInterval = 1;
        this.tags = new HashSet<>();
        this.reminderSet = false;
        this.reminderMinutes = 15; // Default 15 minutes
    }
    
    // Getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    
    public boolean isRecurring() { return recurring; }
    public void setRecurring(boolean recurring) { this.recurring = recurring; }
    
    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public void setRecurrenceType(RecurrenceType recurrenceType) { 
        this.recurrenceType = recurrenceType;
        this.recurring = recurrenceType != RecurrenceType.NONE;
    }
    
    public int getRecurrenceInterval() { return recurrenceInterval; }
    public void setRecurrenceInterval(int interval) { 
        this.recurrenceInterval = Math.max(1, interval); 
    }
    
    public Set<String> getTags() { return new HashSet<>(tags); }
    public void addTag(String tag) { tags.add(tag.toLowerCase()); }
    public void removeTag(String tag) { tags.remove(tag.toLowerCase()); }
    
    public boolean isReminderSet() { return reminderSet; }
    public void setReminderSet(boolean reminderSet) { this.reminderSet = reminderSet; }
    
    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int minutes) { 
        this.reminderMinutes = Math.max(0, minutes); 
    }
    
    // Utility methods
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(start) && now.isBefore(end);
    }
    
    public boolean isUpcoming(int hours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusHours(hours);
        return start.isAfter(now) && start.isBefore(threshold);
    }
    
    public boolean isPast() {
        return LocalDateTime.now().isAfter(end);
    }
    
    public LocalDateTime getReminderTime() {
        return start.minusMinutes(reminderMinutes);
    }
    
    public boolean shouldRemind() {
        if (!reminderSet) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = getReminderTime();
        return now.isAfter(reminderTime) && now.isBefore(start);
    }
    
    // Get next occurrence for recurring events
    public Event getNextOccurrence() {
        if (!recurring || recurrenceType == RecurrenceType.NONE) return null;
        
        LocalDateTime nextStart = start;
        LocalDateTime nextEnd = end;
        
        switch (recurrenceType) {
            case DAILY:
                nextStart = start.plusDays(recurrenceInterval);
                nextEnd = end.plusDays(recurrenceInterval);
                break;
            case WEEKLY:
                nextStart = start.plusWeeks(recurrenceInterval);
                nextEnd = end.plusWeeks(recurrenceInterval);
                break;
            case MONTHLY:
                nextStart = start.plusMonths(recurrenceInterval);
                nextEnd = end.plusMonths(recurrenceInterval);
                break;
            case YEARLY:
                nextStart = start.plusYears(recurrenceInterval);
                nextEnd = end.plusYears(recurrenceInterval);
                break;
        }
        
        Event nextEvent = new Event(description, nextStart, nextEnd);
        nextEvent.setRecurrenceType(recurrenceType);
        nextEvent.setRecurrenceInterval(recurrenceInterval);
        nextEvent.setReminderSet(reminderSet);
        nextEvent.setReminderMinutes(reminderMinutes);
        nextEvent.tags.addAll(this.tags);
        
        return nextEvent;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Event event = (Event) obj;
        return Objects.equals(description, event.description) && 
               Objects.equals(start, event.start);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(description, start);
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        
        sb.append(description);
        sb.append(" | ").append(start.format(formatter));
        if (end != null) {
            sb.append(" - ").append(end.format(formatter));
        }
        
        if (recurring) {
            sb.append(" (").append(recurrenceType.name().toLowerCase());
            if (recurrenceInterval > 1) {
                sb.append(" every ").append(recurrenceInterval);
            }
            sb.append(")");
        }
        
        if (reminderSet) {
            sb.append(" 🔔").append(reminderMinutes).append("min");
        }
        
        return sb.toString();
    }
}