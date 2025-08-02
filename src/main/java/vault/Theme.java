package vault;

/**
 * Theme - Enum for console coloring/symbols simulation
 * Provides different visual themes for the console interface
 */
public enum Theme {
    LIGHT("Light", "Simple light theme with minimal symbols"),
    DARK("Dark", "Dark theme with enhanced contrast"),
    COLORFUL("Colorful", "Colorful theme with rich symbols and indicators");
    
    private final String displayName;
    private final String description;
    
    Theme(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    // Theme-specific symbols and formatting
    public String getCheckmark() {
        switch (this) {
            case LIGHT: return "[X]";
            case DARK: return "[✓]";
            case COLORFUL: return "✅";
            default: return "[X]";
        }
    }
    
    public String getEmptyCheckbox() {
        switch (this) {
            case LIGHT: return "[ ]";
            case DARK: return "[ ]";
            case COLORFUL: return "☐";
            default: return "[ ]";
        }
    }
    
    public String getWarningSymbol() {
        switch (this) {
            case LIGHT: return "[!]";
            case DARK: return "[⚠]";
            case COLORFUL: return "⚠️";
            default: return "[!]";
        }
    }
    
    public String getErrorSymbol() {
        switch (this) {
            case LIGHT: return "[X]";
            case DARK: return "[✗]";
            case COLORFUL: return "❌";
            default: return "[X]";
        }
    }
    
    public String getInfoSymbol() {
        switch (this) {
            case LIGHT: return "[i]";
            case DARK: return "[ℹ]";
            case COLORFUL: return "ℹ️";
            default: return "[i]";
        }
    }
    
    public String getSuccessSymbol() {
        switch (this) {
            case LIGHT: return "[OK]";
            case DARK: return "[✓]";
            case COLORFUL: return "✅";
            default: return "[OK]";
        }
    }
    
    public String getLockSymbol() {
        switch (this) {
            case LIGHT: return "[LOCKED]";
            case DARK: return "[🔒]";
            case COLORFUL: return "🔒";
            default: return "[LOCKED]";
        }
    }
    
    public String getUnlockSymbol() {
        switch (this) {
            case LIGHT: return "[UNLOCKED]";
            case DARK: return "[🔓]";
            case COLORFUL: return "🔓";
            default: return "[UNLOCKED]";
        }
    }
    
    public String getMenuBorder() {
        switch (this) {
            case LIGHT: return "=";
            case DARK: return "═";
            case COLORFUL: return "═";
            default: return "=";
        }
    }
    
    public String getMenuSeparator() {
        switch (this) {
            case LIGHT: return "-";
            case DARK: return "─";
            case COLORFUL: return "─";
            default: return "-";
        }
    }
    
    public String getPrioritySymbol(ChecklistItem.Priority priority) {
        if (priority == null) return "";
        
        switch (this) {
            case LIGHT:
                switch (priority) {
                    case LOW: return "[L]";
                    case MEDIUM: return "[M]";
                    case HIGH: return "[H]";
                    case URGENT: return "[!]";
                    default: return "";
                }
            case DARK:
                switch (priority) {
                    case LOW: return "⬇";
                    case MEDIUM: return "➡";
                    case HIGH: return "⬆";
                    case URGENT: return "🔥";
                    default: return "";
                }
            case COLORFUL:
                switch (priority) {
                    case LOW: return "🟢";
                    case MEDIUM: return "🟡";
                    case HIGH: return "🟠";
                    case URGENT: return "🔴";
                    default: return "";
                }
            default:
                return "[" + priority.name().charAt(0) + "]";
        }
    }
    
    public String getStatusSymbol(String status) {
        if (status == null) return "";
        
        switch (status.toLowerCase()) {
            case "active":
            case "running":
                switch (this) {
                    case LIGHT: return "[ACTIVE]";
                    case DARK: return "[▶]";
                    case COLORFUL: return "▶️";
                    default: return "[ACTIVE]";
                }
            case "completed":
            case "done":
                return getSuccessSymbol();
            case "overdue":
                return getWarningSymbol();
            case "upcoming":
                switch (this) {
                    case LIGHT: return "[SOON]";
                    case DARK: return "[⏰]";
                    case COLORFUL: return "⏰";
                    default: return "[SOON]";
                }
            default:
                return "";
        }
    }
    
    // Color simulation for console (returns ANSI codes in real implementation)
    public String colorText(String text, String color) {
        // In a real implementation, this would return ANSI color codes
        // For now, we'll just return the text with brackets to indicate color
        switch (this) {
            case LIGHT:
                return text; // No color formatting in light theme
            case DARK:
            case COLORFUL:
                return String.format("[%s]%s[/%s]", color, text, color);
            default:
                return text;
        }
    }
    
    public String formatTitle(String title) {
        String border = getMenuBorder().repeat(title.length() + 4);
        String separator = getMenuSeparator().repeat(title.length() + 4);
        
        switch (this) {
            case LIGHT:
                return String.format("%s\n  %s  \n%s", border, title, border);
            case DARK:
            case COLORFUL:
                return String.format("%s\n║ %s ║\n%s", border, title, border);
            default:
                return title;
        }
    }
    
    public String formatMenuItem(int number, String item, boolean selected) {
        String prefix = selected ? "> " : "  ";
        String symbol = selected ? getInfoSymbol() + " " : "";
        
        return String.format("%s%d. %s%s", prefix, number, symbol, item);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}