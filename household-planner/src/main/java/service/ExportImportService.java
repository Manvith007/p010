package service;

import util.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service class for exporting and importing application data
 */
public class ExportImportService {
    
    public ExportImportService() {
        // Initialize if needed
    }
    
    // Export functionality
    public String exportAllDataToJSON() {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"exportTimestamp\": \"").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\",\n");
            json.append("  \"version\": \"1.0\",\n");
            
            // Export each data file
            String[] dataFiles = FileOperations.listDataFiles();
            for (int i = 0; i < dataFiles.length; i++) {
                String filename = dataFiles[i];
                String content = FileOperations.readTextFromFile(filename);
                if (content != null) {
                    json.append("  \"").append(filename).append("\": ");
                    json.append("\"").append(escapeJSON(content)).append("\"");
                    if (i < dataFiles.length - 1) {
                        json.append(",");
                    }
                    json.append("\n");
                }
            }
            
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to export data to JSON", e);
            return null;
        }
    }
    
    public boolean exportAllDataToFile(String filename) {
        try {
            String jsonData = exportAllDataToJSON();
            if (jsonData != null) {
                FileOperations.writeTextToFile(jsonData, filename);
                ExceptionLogger.getInstance().logInfo("Exported all data to file: " + filename);
                return true;
            }
            return false;
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to export data to file: " + filename, e);
            return false;
        }
    }
    
    public String createBackup() {
        try {
            String backupPath = FileOperations.createBackup();
            ExceptionLogger.getInstance().logInfo("Created backup: " + backupPath);
            return backupPath;
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to create backup", e);
            return null;
        }
    }
    
    // Import functionality
    public boolean importAllDataFromJSON(String jsonData) {
        try {
            // Create backup before import
            String backupPath = createBackup();
            if (backupPath == null) {
                ExceptionLogger.getInstance().logWarning("Could not create backup before import");
            }
            
            // Parse JSON and extract files (simplified JSON parsing)
            String[] lines = jsonData.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("\"") && line.contains("\": \"") && line.endsWith("\",") || line.endsWith("\"")) {
                    // Extract filename and content
                    int colonIndex = line.indexOf("\": \"");
                    if (colonIndex > 0) {
                        String filename = line.substring(1, colonIndex);
                        String content = line.substring(colonIndex + 4);
                        
                        // Remove trailing comma and quote
                        if (content.endsWith("\",")) {
                            content = content.substring(0, content.length() - 2);
                        } else if (content.endsWith("\"")) {
                            content = content.substring(0, content.length() - 1);
                        }
                        
                        // Skip metadata fields
                        if (!filename.equals("exportTimestamp") && !filename.equals("version")) {
                            String unescapedContent = unescapeJSON(content);
                            FileOperations.writeTextToFile(unescapedContent, filename);
                        }
                    }
                }
            }
            
            ExceptionLogger.getInstance().logInfo("Successfully imported data from JSON");
            return true;
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to import data from JSON", e);
            return false;
        }
    }
    
    public boolean importAllDataFromFile(String filename) {
        try {
            String jsonData = FileOperations.readTextFromFile(filename);
            if (jsonData != null) {
                boolean success = importAllDataFromJSON(jsonData);
                if (success) {
                    ExceptionLogger.getInstance().logInfo("Imported data from file: " + filename);
                }
                return success;
            }
            return false;
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to import data from file: " + filename, e);
            return false;
        }
    }
    
    public boolean restoreFromBackup(String backupPath) {
        try {
            FileOperations.restoreFromBackup(backupPath);
            ExceptionLogger.getInstance().logInfo("Restored data from backup: " + backupPath);
            return true;
        } catch (IOException e) {
            ExceptionLogger.getInstance().logError("Failed to restore from backup: " + backupPath, e);
            return false;
        }
    }
    
    // CSV Export functionality
    public String exportInventoryToCSV(InventoryService inventoryService) {
        StringBuilder csv = new StringBuilder();
        csv.append("Name,Category,Quantity,Unit,Expiry Date,Restock Threshold,Purchase Date,Status,Notes\n");
        
        inventoryService.getAllItems().forEach(item -> {
            csv.append(escapeCSV(item.getName())).append(",");
            csv.append(escapeCSV(item.getCategory())).append(",");
            csv.append(item.getQuantity()).append(",");
            csv.append(escapeCSV(item.getUnit())).append(",");
            csv.append(item.getExpiryDate() != null ? item.getExpiryDate().toString() : "").append(",");
            csv.append(item.getRestockThreshold()).append(",");
            csv.append(item.getPurchaseDate() != null ? item.getPurchaseDate().toString() : "").append(",");
            csv.append(item.getStatus()).append(",");
            csv.append(escapeCSV(item.getNotes() != null ? item.getNotes() : "")).append("\n");
        });
        
        return csv.toString();
    }
    
    public String exportShoppingListsToCSV(ShoppingService shoppingService) {
        StringBuilder csv = new StringBuilder();
        csv.append("List Name,Item Name,Category,Quantity,Unit,Purchased,Notes,List Created,List Completed\n");
        
        shoppingService.getAllShoppingLists().forEach(list -> {
            list.getItems().forEach(item -> {
                csv.append(escapeCSV(list.getName())).append(",");
                csv.append(escapeCSV(item.getName())).append(",");
                csv.append(escapeCSV(item.getCategory())).append(",");
                csv.append(item.getQuantity()).append(",");
                csv.append(escapeCSV(item.getUnit())).append(",");
                csv.append(item.isPurchased()).append(",");
                csv.append(escapeCSV(item.getNotes() != null ? item.getNotes() : "")).append(",");
                csv.append(list.getCreatedDate()).append(",");
                csv.append(list.isCompleted()).append("\n");
            });
        });
        
        return csv.toString();
    }
    
    public String exportChoresToCSV(ChoreService choreService) {
        StringBuilder csv = new StringBuilder();
        csv.append("Description,Assignee,Due Date,Priority,Status,Recurring,Notes,Created Date,Completed Date\n");
        
        choreService.getAllChores().forEach(chore -> {
            csv.append(escapeCSV(chore.getDescription())).append(",");
            csv.append(escapeCSV(chore.getAssignee())).append(",");
            csv.append(chore.getDueDate() != null ? chore.getDueDate().toString() : "").append(",");
            csv.append(chore.getPriority()).append(",");
            csv.append(chore.getStatus()).append(",");
            csv.append(chore.isRecurring()).append(",");
            csv.append(escapeCSV(chore.getNotes() != null ? chore.getNotes() : "")).append(",");
            csv.append(chore.getCreatedDate()).append(",");
            csv.append(chore.getCompletedDate() != null ? chore.getCompletedDate().toString() : "").append("\n");
        });
        
        return csv.toString();
    }
    
    // Template export for empty structure
    public String exportTemplateCSV(String dataType) {
        switch (dataType.toLowerCase()) {
            case "inventory":
                return "Name,Category,Quantity,Unit,Expiry Date,Restock Threshold,Purchase Date,Notes\n" +
                       "Example Item,Food,5.0,kg,2024-12-31,2.0,2024-01-01,Example notes\n";
            
            case "chores":
                return "Description,Assignee,Due Date,Priority,Recurring,Notes\n" +
                       "Example Chore,John,2024-12-31,MEDIUM,false,Example notes\n";
            
            case "bills":
                return "Utility Name,Type,Amount,Due Date,Recurring,Notes\n" +
                       "Electric Bill,electricity,150.00,2024-01-15,true,Monthly electric bill\n";
            
            default:
                return "";
        }
    }
    
    // Utility methods
    private String escapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String unescapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\\\\", "\\")
                   .replace("\\\"", "\"")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
    
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    // File management
    public String[] listBackupFiles() {
        try {
            return java.nio.file.Files.list(java.nio.file.Paths.get("backups"))
                .filter(java.nio.file.Files::isRegularFile)
                .map(path -> path.getFileName().toString())
                .filter(name -> name.endsWith(".zip"))
                .sorted((a, b) -> b.compareTo(a)) // Most recent first
                .toArray(String[]::new);
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to list backup files", e);
            return new String[0];
        }
    }
    
    public boolean deleteBackup(String backupFilename) {
        try {
            java.nio.file.Path backupPath = java.nio.file.Paths.get("backups", backupFilename);
            boolean deleted = java.nio.file.Files.deleteIfExists(backupPath);
            if (deleted) {
                ExceptionLogger.getInstance().logInfo("Deleted backup: " + backupFilename);
            }
            return deleted;
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to delete backup: " + backupFilename, e);
            return false;
        }
    }
    
    // Data validation for imports
    public boolean validateImportData(String jsonData) {
        try {
            // Basic JSON structure validation
            if (!jsonData.trim().startsWith("{") || !jsonData.trim().endsWith("}")) {
                return false;
            }
            
            // Check for required fields
            return jsonData.contains("exportTimestamp") && jsonData.contains("version");
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to validate import data", e);
            return false;
        }
    }
    
    // Cleanup old backups
    public int cleanupOldBackups(int keepCount) {
        try {
            String[] backups = listBackupFiles();
            int deleted = 0;
            
            if (backups.length > keepCount) {
                // Sort by name (which includes timestamp) and keep only the most recent
                java.util.Arrays.sort(backups, java.util.Collections.reverseOrder());
                
                for (int i = keepCount; i < backups.length; i++) {
                    if (deleteBackup(backups[i])) {
                        deleted++;
                    }
                }
            }
            
            if (deleted > 0) {
                ExceptionLogger.getInstance().logInfo("Cleaned up " + deleted + " old backups");
            }
            
            return deleted;
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to cleanup old backups", e);
            return 0;
        }
    }
}