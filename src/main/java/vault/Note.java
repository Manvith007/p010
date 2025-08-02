package vault;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Note - Base class for all notes with attachments, tagging, searching
 * Supports file attachments (simulated via file paths), created/modified timestamps
 */
public class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private String title;
    private String content;
    private Set<String> tags;
    private List<String> attachments; // File paths or byte[] representations
    private LocalDateTime created;
    private LocalDateTime modified;
    private boolean pinned;
    
    public Note(String title, String content, Set<String> tags) {
        this.title = title != null ? title : "";
        this.content = content != null ? content : "";
        this.tags = tags != null ? new HashSet<>(tags) : new HashSet<>();
        this.attachments = new ArrayList<>();
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
        this.pinned = false;
    }
    
    // Content management
    public void updateContent(String newContent) {
        this.content = newContent != null ? newContent : "";
        this.modified = LocalDateTime.now();
    }
    
    public void updateTitle(String newTitle) {
        this.title = newTitle != null ? newTitle : "";
        this.modified = LocalDateTime.now();
    }
    
    // Tag management
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            tags.add(tag.trim().toLowerCase());
            this.modified = LocalDateTime.now();
        }
    }
    
    public void removeTag(String tag) {
        if (tag != null) {
            tags.remove(tag.trim().toLowerCase());
            this.modified = LocalDateTime.now();
        }
    }
    
    public boolean hasTag(String tag) {
        return tag != null && tags.contains(tag.trim().toLowerCase());
    }
    
    // Attachment management (simulated via file paths)
    public void addAttachment(String filePath) {
        if (filePath != null && !filePath.trim().isEmpty()) {
            attachments.add(filePath.trim());
            this.modified = LocalDateTime.now();
        }
    }
    
    public void removeAttachment(String filePath) {
        if (filePath != null) {
            attachments.remove(filePath.trim());
            this.modified = LocalDateTime.now();
        }
    }
    
    // Search functionality
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase();
        
        // Search in title
        if (title.toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Search in content
        if (content.toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Search in tags
        if (tags.stream().anyMatch(tag -> tag.contains(lowerQuery))) {
            return true;
        }
        
        // Search in attachment names
        return attachments.stream()
                .anyMatch(attachment -> attachment.toLowerCase().contains(lowerQuery));
    }
    
    // Pin/Unpin functionality (Decorator pattern for marking as urgent)
    public void pin() {
        this.pinned = true;
        this.modified = LocalDateTime.now();
    }
    
    public void unpin() {
        this.pinned = false;
        this.modified = LocalDateTime.now();
    }
    
    // Getters
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Set<String> getTags() { return new HashSet<>(tags); }
    public List<String> getAttachments() { return new ArrayList<>(attachments); }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getModified() { return modified; }
    public boolean isPinned() { return pinned; }
    
    // Serialization methods
    @Override
    public String toString() {
        String tagsStr = tags.stream().collect(Collectors.joining(","));
        String attachmentsStr = attachments.stream().collect(Collectors.joining(","));
        
        return String.join("|",
            title != null ? title.replace("|", "\\|") : "",
            content != null ? content.replace("|", "\\|").replace("\n", "\\n") : "",
            tagsStr,
            attachmentsStr,
            created != null ? created.format(FORMATTER) : "",
            modified != null ? modified.format(FORMATTER) : "",
            String.valueOf(pinned)
        );
    }
    
    public static Note fromString(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length != 7) {
            throw new IllegalArgumentException("Invalid note format");
        }
        
        String title = parts[0].replace("\\|", "|");
        String content = parts[1].replace("\\|", "|").replace("\\n", "\n");
        
        Set<String> tags = new HashSet<>();
        if (!parts[2].isEmpty()) {
            tags.addAll(Arrays.asList(parts[2].split(",")));
        }
        
        Note note = new Note(title, content, tags);
        
        // Add attachments
        if (!parts[3].isEmpty()) {
            note.attachments.addAll(Arrays.asList(parts[3].split(",")));
        }
        
        // Parse timestamps
        try {
            if (!parts[4].isEmpty()) {
                note.created = LocalDateTime.parse(parts[4], FORMATTER);
            }
            if (!parts[5].isEmpty()) {
                note.modified = LocalDateTime.parse(parts[5], FORMATTER);
            }
        } catch (Exception e) {
            // Use current time if parsing fails
            note.created = LocalDateTime.now();
            note.modified = LocalDateTime.now();
        }
        
        // Parse pinned status
        try {
            note.pinned = Boolean.parseBoolean(parts[6]);
        } catch (Exception e) {
            note.pinned = false;
        }
        
        return note;
    }
    
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pinned ? "[PINNED] " : "");
        sb.append(title);
        if (!tags.isEmpty()) {
            sb.append(" #").append(String.join(" #", tags));
        }
        sb.append("\n").append(content);
        if (!attachments.isEmpty()) {
            sb.append("\nAttachments: ").append(attachments.size());
        }
        sb.append("\nModified: ").append(modified.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Note note = (Note) obj;
        return Objects.equals(title, note.title) && 
               Objects.equals(created, note.created);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, created);
    }
}