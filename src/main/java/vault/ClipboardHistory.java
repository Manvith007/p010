package vault;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClipboardHistory - Maintains clipboard text history and recent file/URL history
 * Uses Stack/Deque for LIFO buffer, with search and restore functionality
 */
public class ClipboardHistory {
    private static final int MAX_HISTORY_SIZE = 100;
    private static final int MAX_ITEM_LENGTH = 1000; // Truncate very long items
    
    private final Deque<ClipboardItem> history;
    private final Set<String> duplicateTracker; // For quick duplicate detection
    
    public ClipboardHistory() {
        this.history = new ArrayDeque<>();
        this.duplicateTracker = new HashSet<>();
    }
    
    // Add item to clipboard history
    public void add(String content) {
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        
        String trimmedContent = content.length() > MAX_ITEM_LENGTH ? 
                               content.substring(0, MAX_ITEM_LENGTH) + "..." : content;
        
        // Remove existing duplicate if present
        if (duplicateTracker.contains(trimmedContent)) {
            history.removeIf(item -> item.getContent().equals(trimmedContent));
            duplicateTracker.remove(trimmedContent);
        }
        
        // Add new item to front (most recent)
        ClipboardItem item = new ClipboardItem(trimmedContent, determineType(trimmedContent));
        history.addFirst(item);
        duplicateTracker.add(trimmedContent);
        
        // Maintain size limit
        while (history.size() > MAX_HISTORY_SIZE) {
            ClipboardItem removed = history.removeLast();
            duplicateTracker.remove(removed.getContent());
        }
    }
    
    // Get history as list (most recent first)
    public List<String> getHistory() {
        return history.stream()
                .map(ClipboardItem::getContent)
                .collect(Collectors.toList());
    }
    
    // Get history with metadata
    public List<ClipboardItem> getHistoryItems() {
        return new ArrayList<>(history);
    }
    
    // Search history
    public List<ClipboardItem> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getHistoryItems();
        }
        
        String lowerQuery = query.toLowerCase();
        return history.stream()
                .filter(item -> item.getContent().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
    
    // Get items by type
    public List<ClipboardItem> getByType(ClipboardType type) {
        return history.stream()
                .filter(item -> item.getType() == type)
                .collect(Collectors.toList());
    }
    
    // Get most recent N items
    public List<ClipboardItem> getRecent(int count) {
        return history.stream()
                .limit(count)
                .collect(Collectors.toList());
    }
    
    // Clear history
    public void clear() {
        history.clear();
        duplicateTracker.clear();
    }
    
    // Remove specific item
    public boolean remove(String content) {
        boolean removed = history.removeIf(item -> item.getContent().equals(content));
        if (removed) {
            duplicateTracker.remove(content);
        }
        return removed;
    }
    
    // Get statistics
    public ClipboardStats getStats() {
        Map<ClipboardType, Long> typeCounts = history.stream()
                .collect(Collectors.groupingBy(ClipboardItem::getType, Collectors.counting()));
        
        return new ClipboardStats(history.size(), typeCounts);
    }
    
    // Determine content type
    private ClipboardType determineType(String content) {
        if (content == null || content.trim().isEmpty()) {
            return ClipboardType.TEXT;
        }
        
        String trimmed = content.trim().toLowerCase();
        
        // URL detection
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || 
            trimmed.startsWith("ftp://") || trimmed.startsWith("file://")) {
            return ClipboardType.URL;
        }
        
        // File path detection (simple heuristic)
        if ((trimmed.startsWith("/") || trimmed.matches("^[a-zA-Z]:\\\\.*")) && 
            trimmed.contains(".") && !trimmed.contains(" ")) {
            return ClipboardType.FILE_PATH;
        }
        
        // Email detection
        if (trimmed.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            return ClipboardType.EMAIL;
        }
        
        // Number detection
        if (trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
            return ClipboardType.NUMBER;
        }
        
        // Code detection (simple heuristic)
        if (content.contains("{") && content.contains("}") || 
            content.contains("function") || content.contains("class") ||
            content.contains("import") || content.contains("#include")) {
            return ClipboardType.CODE;
        }
        
        return ClipboardType.TEXT;
    }
    
    // Inner classes
    public static class ClipboardItem {
        private final String content;
        private final ClipboardType type;
        private final Date timestamp;
        
        public ClipboardItem(String content, ClipboardType type) {
            this.content = content;
            this.type = type;
            this.timestamp = new Date();
        }
        
        public String getContent() { return content; }
        public ClipboardType getType() { return type; }
        public Date getTimestamp() { return timestamp; }
        
        public String getDisplayString() {
            String preview = content.length() > 50 ? 
                           content.substring(0, 50) + "..." : content;
            return String.format("[%s] %s (%s)", 
                               type.getDisplayName(), 
                               preview.replace("\n", "\\n"), 
                               new java.text.SimpleDateFormat("HH:mm:ss").format(timestamp));
        }
        
        @Override
        public String toString() {
            return getDisplayString();
        }
    }
    
    public enum ClipboardType {
        TEXT("Text"),
        URL("URL"),
        FILE_PATH("File Path"),
        EMAIL("Email"),
        NUMBER("Number"),
        CODE("Code");
        
        private final String displayName;
        
        ClipboardType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public static class ClipboardStats {
        private final int totalItems;
        private final Map<ClipboardType, Long> typeCounts;
        
        public ClipboardStats(int totalItems, Map<ClipboardType, Long> typeCounts) {
            this.totalItems = totalItems;
            this.typeCounts = new HashMap<>(typeCounts);
        }
        
        public int getTotalItems() { return totalItems; }
        public Map<ClipboardType, Long> getTypeCounts() { return new HashMap<>(typeCounts); }
        
        public String getDisplayString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Clipboard Statistics:\n");
            sb.append("Total items: ").append(totalItems).append("\n");
            
            for (ClipboardType type : ClipboardType.values()) {
                long count = typeCounts.getOrDefault(type, 0L);
                if (count > 0) {
                    sb.append(type.getDisplayName()).append(": ").append(count).append("\n");
                }
            }
            
            return sb.toString();
        }
    }
    
    // Utility methods for limit buffer size, search and restore
    public void setMaxSize(int maxSize) {
        // Note: This would require refactoring to make MAX_HISTORY_SIZE non-final
        // For now, it's a fixed constant
    }
    
    public boolean isEmpty() {
        return history.isEmpty();
    }
    
    public int size() {
        return history.size();
    }
    
    public String getMostRecent() {
        return history.isEmpty() ? null : history.peekFirst().getContent();
    }
    
    // Restore functionality - move item to front
    public boolean restore(String content) {
        ClipboardItem found = null;
        for (ClipboardItem item : history) {
            if (item.getContent().equals(content)) {
                found = item;
                break;
            }
        }
        
        if (found != null) {
            history.remove(found);
            history.addFirst(found);
            return true;
        }
        
        return false;
    }
}