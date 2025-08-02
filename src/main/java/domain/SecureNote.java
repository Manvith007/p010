package domain;

/**
 * Concrete implementation of Note for secure text storage
 * Inherits all functionality from Note including tagging and attachments
 */
public class SecureNote extends Note {
    private static final long serialVersionUID = 1L;
    
    private boolean urgent;
    private String category;
    
    public SecureNote(String title, String content) {
        super(title, content);
        this.urgent = false;
        this.category = "General";
    }
    
    public SecureNote(String title, String content, String category) {
        super(title, content);
        this.urgent = false;
        this.category = category != null ? category : "General";
    }
    
    // Getters and setters
    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { 
        this.urgent = urgent; 
        updateModified();
    }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category != null ? category : "General"; 
        updateModified();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(urgent ? "[URGENT] " : "");
        sb.append(isPinned() ? "[PINNED] " : "");
        sb.append(getTitle());
        sb.append(" [").append(category).append("]");
        
        if (!getTags().isEmpty()) {
            sb.append(" Tags: ").append(String.join(", ", getTags()));
        }
        
        sb.append(" - ").append(getModified().toString());
        
        return sb.toString();
    }
}