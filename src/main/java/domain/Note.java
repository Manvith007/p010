package domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Abstract base class for all notes
 * Supports tagging, attachments, and file persistence
 */
public abstract class Note implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected String title;
    protected String content;
    protected LocalDateTime created;
    protected LocalDateTime modified;
    protected Set<String> tags;
    protected List<String> attachments; // File paths or byte[] references
    protected boolean pinned;
    
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
        this.created = LocalDateTime.now();
        this.modified = LocalDateTime.now();
        this.tags = new HashSet<>();
        this.attachments = new ArrayList<>();
        this.pinned = false;
    }
    
    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { 
        this.title = title; 
        updateModified();
    }
    
    public String getContent() { return content; }
    public void setContent(String content) { 
        this.content = content; 
        updateModified();
    }
    
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getModified() { return modified; }
    
    public Set<String> getTags() { return new HashSet<>(tags); }
    public void addTag(String tag) { 
        tags.add(tag.toLowerCase()); 
        updateModified();
    }
    public void removeTag(String tag) { 
        tags.remove(tag.toLowerCase()); 
        updateModified();
    }
    
    public List<String> getAttachments() { return new ArrayList<>(attachments); }
    public void addAttachment(String attachment) { 
        attachments.add(attachment); 
        updateModified();
    }
    public void removeAttachment(String attachment) { 
        attachments.remove(attachment); 
        updateModified();
    }
    
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { 
        this.pinned = pinned; 
        updateModified();
    }
    
    protected void updateModified() {
        this.modified = LocalDateTime.now();
    }
    
    // Search functionality
    public boolean containsText(String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase();
        return title.toLowerCase().contains(lowerSearch) || 
               content.toLowerCase().contains(lowerSearch) ||
               tags.stream().anyMatch(tag -> tag.contains(lowerSearch));
    }
    
    public boolean hasTag(String tag) {
        return tags.contains(tag.toLowerCase());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Note note = (Note) obj;
        return Objects.equals(title, note.title) && Objects.equals(created, note.created);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, created);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (Tags: %s)", 
                           pinned ? "PINNED" : "NOTE", 
                           title, 
                           modified.toString(), 
                           String.join(", ", tags));
    }
}