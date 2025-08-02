package vault;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * VaultExporter - Template Method pattern implementation
 * For all export types (CSV, text, serialized object) with export/import functionality
 */
public class VaultExporter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Template Method pattern - main export method
    public void exportData(VaultManager vault, String format, String filePath) throws IOException {
        if (vault == null || format == null || filePath == null) {
            throw new IllegalArgumentException("Vault, format, and file path cannot be null");
        }
        
        // Template method steps:
        validateExportParameters(vault, format, filePath);
        String processedPath = preprocessFilePath(filePath, format);
        String exportData = generateExportData(vault, format);
        writeExportData(exportData, processedPath);
        postProcessExport(vault, processedPath);
    }
    
    // Template method steps (can be overridden by subclasses)
    protected void validateExportParameters(VaultManager vault, String format, String filePath) throws IOException {
        if (vault.isLocked()) {
            throw new IllegalStateException("Vault must be unlocked for export");
        }
        
        String[] supportedFormats = {"csv", "json", "txt", "xml", "serialized"};
        if (!Arrays.asList(supportedFormats).contains(format.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
    
    protected String preprocessFilePath(String filePath, String format) {
        // Add timestamp to filename
        String timestamp = DATE_FORMAT.format(new Date());
        String extension = getFileExtension(format);
        
        if (!filePath.endsWith(extension)) {
            // Insert timestamp before extension
            int lastDot = filePath.lastIndexOf('.');
            if (lastDot > 0) {
                return filePath.substring(0, lastDot) + "_" + timestamp + filePath.substring(lastDot);
            } else {
                return filePath + "_" + timestamp + extension;
            }
        }
        
        return filePath;
    }
    
    protected String generateExportData(VaultManager vault, String format) throws IOException {
        switch (format.toLowerCase()) {
            case "csv":
                return generateCSV(vault);
            case "json":
                return generateJSON(vault);
            case "txt":
                return generateText(vault);
            case "xml":
                return generateXML(vault);
            case "serialized":
                return generateSerialized(vault);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }
    
    protected void writeExportData(String data, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, data.getBytes("UTF-8"));
    }
    
    protected void postProcessExport(VaultManager vault, String filePath) {
        // Update last backup path in settings
        vault.getSettings().setLastBackupPath(filePath);
        ExceptionLogger.log("Export completed successfully to: " + filePath, null);
    }
    
    // Specific export format implementations
    public void exportToCSV(VaultManager vault, String filePath) throws IOException {
        exportData(vault, "csv", filePath);
    }
    
    public void exportToJSON(VaultManager vault, String filePath) throws IOException {
        exportData(vault, "json", filePath);
    }
    
    public void exportToText(VaultManager vault, String filePath) throws IOException {
        exportData(vault, "txt", filePath);
    }
    
    public void exportToXML(VaultManager vault, String filePath) throws IOException {
        exportData(vault, "xml", filePath);
    }
    
    public void exportSerialized(VaultManager vault, String filePath) throws IOException {
        exportData(vault, "serialized", filePath);
    }
    
    // Format-specific data generation methods
    private String generateCSV(VaultManager vault) {
        StringBuilder csv = new StringBuilder();
        
        // Export passwords
        csv.append("=== PASSWORDS ===\n");
        csv.append("Type,Site,Username,Note,LastModified\n");
        for (Map.Entry<String, PasswordEntry> entry : vault.getPasswords().entrySet()) {
            PasswordEntry p = entry.getValue();
            csv.append(String.format("PASSWORD,\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    escapeCsv(p.getSite()),
                    escapeCsv(p.getUsername()),
                    escapeCsv(p.getNote()),
                    p.getLastModified().format(DISPLAY_FORMAT)));
        }
        
        // Export notes
        csv.append("\n=== NOTES ===\n");
        csv.append("Type,Title,Content,Tags,Created,Modified,Pinned\n");
        for (Map.Entry<String, Note> entry : vault.getNotes().entrySet()) {
            Note n = entry.getValue();
            csv.append(String.format("NOTE,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                    escapeCsv(n.getTitle()),
                    escapeCsv(n.getContent()),
                    String.join(";", n.getTags()),
                    n.getCreated().format(DISPLAY_FORMAT),
                    n.getModified().format(DISPLAY_FORMAT),
                    n.isPinned()));
        }
        
        // Export checklists
        csv.append("\n=== CHECKLISTS ===\n");
        csv.append("Type,Title,Done,DueDate,Priority,Tags,Notes,Created\n");
        for (Map.Entry<String, ChecklistItem> entry : vault.getChecklists().entrySet()) {
            ChecklistItem c = entry.getValue();
            csv.append(String.format("CHECKLIST,\"%s\",%s,\"%s\",%s,\"%s\",\"%s\",\"%s\"\n",
                    escapeCsv(c.getTitle()),
                    c.isDone(),
                    c.getDueDate() != null ? c.getDueDate().toString() : "",
                    c.getPriority().name(),
                    String.join(";", c.getTags()),
                    escapeCsv(c.getNotes()),
                    c.getCreated().toString()));
        }
        
        // Export events
        csv.append("\n=== EVENTS ===\n");
        csv.append("Type,Description,Start,End,Recurring,Tags,ReminderSet,Created\n");
        for (Map.Entry<String, Event> entry : vault.getEvents().entrySet()) {
            Event e = entry.getValue();
            csv.append(String.format("EVENT,\"%s\",\"%s\",\"%s\",%s,\"%s\",%s,\"%s\"\n",
                    escapeCsv(e.getDescription()),
                    e.getStart() != null ? e.getStart().format(DISPLAY_FORMAT) : "",
                    e.getEnd() != null ? e.getEnd().format(DISPLAY_FORMAT) : "",
                    e.isRecurring(),
                    String.join(";", e.getTags()),
                    e.isReminderSet(),
                    e.getCreated().format(DISPLAY_FORMAT)));
        }
        
        return csv.toString();
    }
    
    private String generateJSON(VaultManager vault) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"exportDate\": \"").append(new Date().toString()).append("\",\n");
        json.append("  \"vaultData\": {\n");
        
        // Passwords
        json.append("    \"passwords\": [\n");
        List<String> passwordEntries = vault.getPasswords().values().stream()
                .map(p -> String.format("      {\"site\": \"%s\", \"username\": \"%s\", \"note\": \"%s\", \"lastModified\": \"%s\"}",
                        escapeJson(p.getSite()), escapeJson(p.getUsername()), 
                        escapeJson(p.getNote()), p.getLastModified().format(DISPLAY_FORMAT)))
                .collect(Collectors.toList());
        json.append(String.join(",\n", passwordEntries));
        json.append("\n    ],\n");
        
        // Notes
        json.append("    \"notes\": [\n");
        List<String> noteEntries = vault.getNotes().values().stream()
                .map(n -> String.format("      {\"title\": \"%s\", \"content\": \"%s\", \"tags\": [%s], \"pinned\": %s, \"created\": \"%s\"}",
                        escapeJson(n.getTitle()), escapeJson(n.getContent()),
                        n.getTags().stream().map(tag -> "\"" + escapeJson(tag) + "\"").collect(Collectors.joining(",")),
                        n.isPinned(), n.getCreated().format(DISPLAY_FORMAT)))
                .collect(Collectors.toList());
        json.append(String.join(",\n", noteEntries));
        json.append("\n    ],\n");
        
        // Checklists
        json.append("    \"checklists\": [\n");
        List<String> checklistEntries = vault.getChecklists().values().stream()
                .map(c -> String.format("      {\"title\": \"%s\", \"done\": %s, \"priority\": \"%s\", \"dueDate\": \"%s\"}",
                        escapeJson(c.getTitle()), c.isDone(), c.getPriority().name(),
                        c.getDueDate() != null ? c.getDueDate().toString() : ""))
                .collect(Collectors.toList());
        json.append(String.join(",\n", checklistEntries));
        json.append("\n    ],\n");
        
        // Events
        json.append("    \"events\": [\n");
        List<String> eventEntries = vault.getEvents().values().stream()
                .map(e -> String.format("      {\"description\": \"%s\", \"start\": \"%s\", \"end\": \"%s\", \"recurring\": %s}",
                        escapeJson(e.getDescription()),
                        e.getStart() != null ? e.getStart().format(DISPLAY_FORMAT) : "",
                        e.getEnd() != null ? e.getEnd().format(DISPLAY_FORMAT) : "",
                        e.isRecurring()))
                .collect(Collectors.toList());
        json.append(String.join(",\n", eventEntries));
        json.append("\n    ]\n");
        
        json.append("  }\n");
        json.append("}");
        
        return json.toString();
    }
    
    private String generateText(VaultManager vault) {
        StringBuilder text = new StringBuilder();
        text.append("PERSONAL DIGITAL ORGANIZER & SECURE VAULT EXPORT\n");
        text.append("Export Date: ").append(new Date().toString()).append("\n");
        text.append("=".repeat(60)).append("\n\n");
        
        // Passwords
        text.append("PASSWORDS:\n");
        text.append("-".repeat(30)).append("\n");
        for (PasswordEntry p : vault.getPasswords().values()) {
            text.append("Site: ").append(p.getSite()).append("\n");
            text.append("Username: ").append(p.getUsername()).append("\n");
            text.append("Note: ").append(p.getNote()).append("\n");
            text.append("Last Modified: ").append(p.getLastModified().format(DISPLAY_FORMAT)).append("\n");
            text.append("\n");
        }
        
        // Notes
        text.append("NOTES:\n");
        text.append("-".repeat(30)).append("\n");
        for (Note n : vault.getNotes().values()) {
            text.append(n.getDisplayString()).append("\n\n");
        }
        
        // Checklists
        text.append("CHECKLISTS:\n");
        text.append("-".repeat(30)).append("\n");
        for (ChecklistItem c : vault.getChecklists().values()) {
            text.append(c.getDisplayString()).append("\n\n");
        }
        
        // Events
        text.append("EVENTS:\n");
        text.append("-".repeat(30)).append("\n");
        for (Event e : vault.getEvents().values()) {
            text.append(e.getDisplayString()).append("\n\n");
        }
        
        return text.toString();
    }
    
    private String generateXML(VaultManager vault) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<vault exportDate=\"").append(new Date().toString()).append("\">\n");
        
        // Passwords
        xml.append("  <passwords>\n");
        for (PasswordEntry p : vault.getPasswords().values()) {
            xml.append("    <password>\n");
            xml.append("      <site>").append(escapeXml(p.getSite())).append("</site>\n");
            xml.append("      <username>").append(escapeXml(p.getUsername())).append("</username>\n");
            xml.append("      <note>").append(escapeXml(p.getNote())).append("</note>\n");
            xml.append("      <lastModified>").append(p.getLastModified().format(DISPLAY_FORMAT)).append("</lastModified>\n");
            xml.append("    </password>\n");
        }
        xml.append("  </passwords>\n");
        
        // Notes
        xml.append("  <notes>\n");
        for (Note n : vault.getNotes().values()) {
            xml.append("    <note pinned=\"").append(n.isPinned()).append("\">\n");
            xml.append("      <title>").append(escapeXml(n.getTitle())).append("</title>\n");
            xml.append("      <content>").append(escapeXml(n.getContent())).append("</content>\n");
            xml.append("      <tags>");
            for (String tag : n.getTags()) {
                xml.append("<tag>").append(escapeXml(tag)).append("</tag>");
            }
            xml.append("</tags>\n");
            xml.append("      <created>").append(n.getCreated().format(DISPLAY_FORMAT)).append("</created>\n");
            xml.append("    </note>\n");
        }
        xml.append("  </notes>\n");
        
        xml.append("</vault>");
        return xml.toString();
    }
    
    private String generateSerialized(VaultManager vault) throws IOException {
        // Create a serializable data container
        VaultExportData exportData = new VaultExportData(
                vault.getPasswords(),
                vault.getNotes(),
                vault.getChecklists(),
                vault.getEvents(),
                new Date()
        );
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(exportData);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
    
    // Utility methods
    private String getFileExtension(String format) {
        switch (format.toLowerCase()) {
            case "csv": return ".csv";
            case "json": return ".json";
            case "txt": return ".txt";
            case "xml": return ".xml";
            case "serialized": return ".ser";
            default: return ".dat";
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private String escapeXml(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                   .replace("\"", "&quot;").replace("'", "&apos;");
    }
    
    // Serializable export data container
    public static class VaultExportData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private final Map<String, PasswordEntry> passwords;
        private final Map<String, Note> notes;
        private final Map<String, ChecklistItem> checklists;
        private final Map<String, Event> events;
        private final Date exportDate;
        
        public VaultExportData(Map<String, PasswordEntry> passwords,
                              Map<String, Note> notes,
                              Map<String, ChecklistItem> checklists,
                              Map<String, Event> events,
                              Date exportDate) {
            this.passwords = new HashMap<>(passwords);
            this.notes = new HashMap<>(notes);
            this.checklists = new HashMap<>(checklists);
            this.events = new HashMap<>(events);
            this.exportDate = exportDate;
        }
        
        // Getters
        public Map<String, PasswordEntry> getPasswords() { return passwords; }
        public Map<String, Note> getNotes() { return notes; }
        public Map<String, ChecklistItem> getChecklists() { return checklists; }
        public Map<String, Event> getEvents() { return events; }
        public Date getExportDate() { return exportDate; }
    }
}