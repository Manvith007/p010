package vault;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ChecklistItem - Contains checklist items with title, status, due dates, tags, notes
 * Implements Serializable for persistence
 */
public class ChecklistItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private String title;
    private boolean done;
    private LocalDate dueDate;
    private Set<String> tags;
    private String notes;
    private Priority priority;
    private LocalDate created;
    private LocalDate lastModified;
    
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }
    
    public ChecklistItem(String title, Set<String> tags) {
        this.title = title != null ? title : "";
        this.done = false;
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
        this.notes = "";
        this.priority = Priority.MEDIUM;
        this.created = LocalDate.now();
        this.lastModified = LocalDate.now();
    }
    
    public ChecklistItem(String title, LocalDate dueDate, Set<String> tags, String notes) {
        this(title, tags);
        this.dueDate = dueDate;
        this.notes = notes != null ? notes : "";
    }
    
    // Status management
    public void markDone() {
        this.done = true;
        this.lastModified = LocalDate.now();
    }
    
    public void markUndone() {
        this.done = false;
        this.lastModified = LocalDate.now();
    }
    
    public void setDone(boolean done) {
        this.done = done;
        this.lastModified = LocalDate.now();
    }
    
    // Due date management
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        this.lastModified = LocalDate.now();
    }
    
    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !done;
    }
    
    public boolean isDueSoon(int days) {
        if (dueDate == null || done) return false;
        LocalDate soon = LocalDate.now().plusDays(days);
        return dueDate.isBefore(soon) || dueDate.isEqual(soon);
    }
    
    // Tag management
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            tags.add(tag.trim().toLowerCase());
            this.lastModified = LocalDate.now();
        }
    }
    
    public void removeTag(String tag) {
        if (tag != null) {
            tags.remove(tag.trim().toLowerCase());
            this.lastModified = LocalDate.now();
        }
    }
    
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.trim().toLowerCase());
    }
    
    // Priority management
    public void setPriority(Priority priority) {
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.lastModified = LocalDate.now();
    }
    
    // Search and filtering
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // Search in title
        if (title.toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Search in notes
        if (notes.toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Search in tags
        return tags.stream().anyMatch(tag -> tag.contains(lowerQuery));
    }
    
    // Sorting comparators
    public static Comparator<ChecklistItem> byDueDate() {
        return (a, b) -> {
            if (a.dueDate == null && b.dueDate == null) return 0;
            if (a.dueDate == null) return 1;
            if (b.dueDate == null) return -1;
            return a.dueDate.compareTo(b.dueDate);
        };
    }
    
    public static Comparator<ChecklistItem> byPriority() {
        return (a, b) -> b.priority.ordinal() - a.priority.ordinal(); // Higher priority first
    }
    
    public static Comparator<ChecklistItem> byTitle() {
        return Comparator.comparing(item -> item.title.toLowerCase());
    }
    
    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title != null ? title : "";
        this.lastModified = LocalDate.now();
    }
    
    public boolean isDone() { return done; }
    public LocalDate getDueDate() { return dueDate; }
    public Set<String> getTags() { return new HashSet<>(tags); }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes != null ? notes : "";
        this.lastModified = LocalDate.now();
    }
    
    public Priority getPriority() { return priority; }
    public LocalDate getCreated() { return created; }
    public LocalDate getLastModified() { return lastModified; }
    
    // Serialization methods
    @Override
    public String toString() {
        String tagsStr = tags.stream().collect(Collectors.joining(","));
        
        return String.join("|",
            title.replace("|", "\\|"),
            String.valueOf(done),
            dueDate != null ? dueDate.format(DATE_FORMATTER) : "",
            tagsStr,
            notes.replace("|", "\\|").replace("\n", "\\n"),
            priority.name(),
            created.format(DATE_FORMATTER),
            lastModified.format(DATE_FORMATTER)
        );
    }
    
    public static ChecklistItem fromString(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid checklist item format");
        }
        
        String title = parts[0].replace("\\|", "|");
        boolean done = Boolean.parseBoolean(parts[1]);
        
        LocalDate dueDate = null;
        if (!parts[2].isEmpty()) {
            try {
                dueDate = LocalDate.parse(parts[2], DATE_FORMATTER);
            } catch (Exception e) {
                // Ignore invalid date
            }
        }
        
        Set<String> tags = new HashSet<>();
        if (!parts[3].isEmpty()) {
            tags.addAll(Arrays.asList(parts[3].split(",")));
        }
        
        String notes = parts[4].replace("\\|", "|").replace("\\n", "\n");
        
        ChecklistItem item = new ChecklistItem(title, dueDate, tags, notes);
        item.done = done;
        
        // Parse priority
        try {
            item.priority = Priority.valueOf(parts[5]);
        } catch (Exception e) {
            item.priority = Priority.MEDIUM;
        }
        
        // Parse dates
        try {
            item.created = LocalDate.parse(parts[6], DATE_FORMATTER);
            item.lastModified = LocalDate.parse(parts[7], DATE_FORMATTER);
        } catch (Exception e) {
            item.created = LocalDate.now();
            item.lastModified = LocalDate.now();
        }
        
        return item;
    }
    
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(done ? "[✓] " : "[ ] ");
        sb.append(title);
        
        if (priority != Priority.MEDIUM) {
            sb.append(" [").append(priority.name()).append("]");
        }
        
        if (!tags.isEmpty()) {
            sb.append(" #").append(String.join(" #", tags));
        }
        
        if (dueDate != null) {
            sb.append(" (Due: ").append(dueDate.format(DateTimeFormatter.ofPattern("MMM dd")));
            if (isOverdue()) {
                sb.append(" - OVERDUE");
            }
            sb.append(")");
        }
        
        if (!notes.isEmpty()) {
            sb.append("\n  Notes: ").append(notes);
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ChecklistItem that = (ChecklistItem) obj;
        return Objects.equals(title, that.title) && 
               Objects.equals(created, that.created);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, created);
    }
}