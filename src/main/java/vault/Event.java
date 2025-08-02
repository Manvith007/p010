package vault;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Event - Contains event information with description, start/end times, 
 * recurring flag, tags, reminder settings
 */
public class Event implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String description;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean recurring;
    private Set<String> tags;
    private boolean reminderSet;
    private Duration reminderOffset; // How long before event to remind
    private RecurrenceType recurrenceType;
    private int recurrenceInterval; // Every N days/weeks/months
    private LocalDateTime created;
    
    public enum RecurrenceType {
        NONE, DAILY, WEEKLY, MONTHLY, YEARLY
    }
    
    public Event(String description, LocalDateTime start, LocalDateTime end, 
                 boolean recurring, Set<String> tags, boolean reminderSet) {
        this.description = description != null ? description : "";
        this.start = start;
        this.end = end;
        this.recurring = recurring;
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
        this.reminderSet = reminderSet;
        this.reminderOffset = Duration.ofMinutes(15); // Default 15 minutes before
        this.recurrenceType = RecurrenceType.NONE;
        this.recurrenceInterval = 1;
        this.created = LocalDateTime.now();
    }
    
    // Event management
    public Duration getDuration() {
        if (start != null && end != null) {
            return Duration.between(start, end);
        }
        return Duration.ZERO;
    }
    
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return start != null && end != null && 
               now.isAfter(start) && now.isBefore(end);
    }
    
    public boolean isUpcoming(Duration timeWindow) {
        if (start == null) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plus(timeWindow);
        return start.isAfter(now) && start.isBefore(windowEnd);
    }
    
    public boolean isPast() {
        return end != null && end.isBefore(LocalDateTime.now());
    }
    
    // Reminder management
    public LocalDateTime getReminderTime() {
        if (!reminderSet || start == null) return null;
        return start.minus(reminderOffset);
    }
    
    public boolean shouldRemindNow() {
        LocalDateTime reminderTime = getReminderTime();
        if (reminderTime == null) return false;
        
        LocalDateTime now = LocalDateTime.now();
        // Remind if within 1 minute of reminder time
        return Math.abs(Duration.between(now, reminderTime).toMinutes()) <= 1;
    }
    
    public void setReminderOffset(Duration offset) {
        this.reminderOffset = offset != null ? offset : Duration.ofMinutes(15);
    }
    
    // Recurrence management
    public void setRecurrence(RecurrenceType type, int interval) {
        this.recurrenceType = type != null ? type : RecurrenceType.NONE;
        this.recurrenceInterval = Math.max(1, interval);
        this.recurring = (type != RecurrenceType.NONE);
    }
    
    public LocalDateTime getNextOccurrence() {
        if (!recurring || recurrenceType == RecurrenceType.NONE || start == null) {
            return null;
        }
        
        LocalDateTime next = start;
        LocalDateTime now = LocalDateTime.now();
        
        while (next.isBefore(now)) {
            switch (recurrenceType) {
                case DAILY:
                    next = next.plusDays(recurrenceInterval);
                    break;
                case WEEKLY:
                    next = next.plusWeeks(recurrenceInterval);
                    break;
                case MONTHLY:
                    next = next.plusMonths(recurrenceInterval);
                    break;
                case YEARLY:
                    next = next.plusYears(recurrenceInterval);
                    break;
                default:
                    return null;
            }
        }
        
        return next;
    }
    
    // Tag management
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            tags.add(tag.trim().toLowerCase());
        }
    }
    
    public void removeTag(String tag) {
        if (tag != null) {
            tags.remove(tag.trim().toLowerCase());
        }
    }
    
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.trim().toLowerCase());
    }
    
    // Search functionality
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // Search in description
        if (description.toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Search in tags
        return tags.stream().anyMatch(tag -> tag.contains(lowerQuery));
    }
    
    // Conflict detection
    public boolean conflictsWith(Event other) {
        if (other == null || start == null || end == null || 
            other.start == null || other.end == null) {
            return false;
        }
        
        return start.isBefore(other.end) && end.isAfter(other.start);
    }
    
    // Getters and setters
    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description != null ? description : "";
    }
    
    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }
    
    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }
    
    public boolean isRecurring() { return recurring; }
    public Set<String> getTags() { return new HashSet<>(tags); }
    public boolean isReminderSet() { return reminderSet; }
    public void setReminderSet(boolean reminderSet) { this.reminderSet = reminderSet; }
    
    public Duration getReminderOffset() { return reminderOffset; }
    public RecurrenceType getRecurrenceType() { return recurrenceType; }
    public int getRecurrenceInterval() { return recurrenceInterval; }
    public LocalDateTime getCreated() { return created; }
    
    // Serialization methods
    @Override
    public String toString() {
        String tagsStr = tags.stream().collect(Collectors.joining(","));
        
        return String.join("|",
            description.replace("|", "\\|"),
            start != null ? start.format(DATETIME_FORMATTER) : "",
            end != null ? end.format(DATETIME_FORMATTER) : "",
            String.valueOf(recurring),
            tagsStr,
            String.valueOf(reminderSet),
            String.valueOf(reminderOffset.toMinutes()),
            recurrenceType.name(),
            String.valueOf(recurrenceInterval),
            created.format(DATETIME_FORMATTER)
        );
    }
    
    public static Event fromString(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 10) {
            throw new IllegalArgumentException("Invalid event format");
        }
        
        String description = parts[0].replace("\\|", "|");
        
        LocalDateTime start = null, end = null, created = null;
        try {
            if (!parts[1].isEmpty()) {
                start = LocalDateTime.parse(parts[1], DATETIME_FORMATTER);
            }
            if (!parts[2].isEmpty()) {
                end = LocalDateTime.parse(parts[2], DATETIME_FORMATTER);
            }
        } catch (Exception e) {
            // Use default values if parsing fails
        }
        
        boolean recurring = Boolean.parseBoolean(parts[3]);
        
        Set<String> tags = new HashSet<>();
        if (!parts[4].isEmpty()) {
            tags.addAll(Arrays.asList(parts[4].split(",")));
        }
        
        boolean reminderSet = Boolean.parseBoolean(parts[5]);
        
        Event event = new Event(description, start, end, recurring, tags, reminderSet);
        
        // Parse reminder offset
        try {
            long minutes = Long.parseLong(parts[6]);
            event.reminderOffset = Duration.ofMinutes(minutes);
        } catch (Exception e) {
            event.reminderOffset = Duration.ofMinutes(15);
        }
        
        // Parse recurrence
        try {
            event.recurrenceType = RecurrenceType.valueOf(parts[7]);
            event.recurrenceInterval = Integer.parseInt(parts[8]);
        } catch (Exception e) {
            event.recurrenceType = RecurrenceType.NONE;
            event.recurrenceInterval = 1;
        }
        
        // Parse created timestamp
        try {
            event.created = LocalDateTime.parse(parts[9], DATETIME_FORMATTER);
        } catch (Exception e) {
            event.created = LocalDateTime.now();
        }
        
        return event;
    }
    
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(description);
        
        if (start != null) {
            sb.append("\nStart: ").append(start.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        }
        
        if (end != null) {
            sb.append("\nEnd: ").append(end.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        }
        
        if (recurring && recurrenceType != RecurrenceType.NONE) {
            sb.append("\nRecurs: Every ");
            if (recurrenceInterval > 1) {
                sb.append(recurrenceInterval).append(" ");
            }
            sb.append(recurrenceType.name().toLowerCase());
            if (recurrenceInterval > 1) {
                sb.append("s");
            }
        }
        
        if (reminderSet) {
            sb.append("\nReminder: ").append(reminderOffset.toMinutes()).append(" minutes before");
        }
        
        if (!tags.isEmpty()) {
            sb.append("\nTags: #").append(String.join(" #", tags));
        }
        
        // Status indicators
        if (isActive()) {
            sb.append("\n[ACTIVE NOW]");
        } else if (isPast()) {
            sb.append("\n[COMPLETED]");
        } else if (isUpcoming(Duration.ofDays(1))) {
            sb.append("\n[UPCOMING]");
        }
        
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Event event = (Event) obj;
        return Objects.equals(description, event.description) && 
               Objects.equals(start, event.start) &&
               Objects.equals(created, event.created);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(description, start, created);
    }
}