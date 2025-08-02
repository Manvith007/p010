package util;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton ExceptionLogger for logging errors to file or console
 * Thread-safe implementation with different log levels
 */
public class ExceptionLogger {
    private static volatile ExceptionLogger instance;
    private static final Object lock = new Object();
    
    private final ReentrantLock fileLock = new ReentrantLock();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private Path logFile;
    private LogLevel currentLogLevel;
    private boolean logToConsole;
    private boolean logToFile;
    
    public enum LogLevel {
        DEBUG(0), INFO(1), WARN(2), ERROR(3), FATAL(4);
        
        private final int level;
        LogLevel(int level) { this.level = level; }
        public int getLevel() { return level; }
    }
    
    private ExceptionLogger() {
        try {
            // Default configuration
            currentLogLevel = LogLevel.ERROR;
            logToConsole = true;
            logToFile = true;
            
            // Create logs directory
            Path logDir = Paths.get("logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            // Create log file with current date
            String fileName = "vault_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
            logFile = logDir.resolve(fileName);
            
        } catch (Exception e) {
            // Fallback to console only if file logging fails
            logToFile = false;
            System.err.println("Failed to initialize file logging: " + e.getMessage());
        }
    }
    
    public static ExceptionLogger getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ExceptionLogger();
                }
            }
        }
        return instance;
    }
    
    // Configuration methods
    public void setLogLevel(LogLevel level) {
        this.currentLogLevel = level;
    }
    
    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }
    
    public void setLogToFile(boolean logToFile) {
        this.logToFile = logToFile;
    }
    
    // Main logging methods
    public void logDebug(String message) {
        log(LogLevel.DEBUG, message, null);
    }
    
    public void logInfo(String message) {
        log(LogLevel.INFO, message, null);
    }
    
    public void logWarning(String message) {
        log(LogLevel.WARN, message, null);
    }
    
    public void logError(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }
    
    public void logFatal(String message, Throwable throwable) {
        log(LogLevel.FATAL, message, throwable);
    }
    
    // Core logging method
    private void log(LogLevel level, String message, Throwable throwable) {
        if (level.getLevel() < currentLogLevel.getLevel()) {
            return; // Skip logging if below current level
        }
        
        String timestamp = LocalDateTime.now().format(formatter);
        String logEntry = formatLogEntry(timestamp, level, message, throwable);
        
        // Log to console
        if (logToConsole) {
            if (level.getLevel() >= LogLevel.ERROR.getLevel()) {
                System.err.println(logEntry);
            } else {
                System.out.println(logEntry);
            }
        }
        
        // Log to file
        if (logToFile && logFile != null) {
            writeToFile(logEntry);
        }
    }
    
    private String formatLogEntry(String timestamp, LogLevel level, String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(timestamp).append("] ");
        sb.append("[").append(level.name()).append("] ");
        sb.append(message);
        
        if (throwable != null) {
            sb.append("\n").append("Exception: ").append(throwable.getClass().getSimpleName());
            sb.append(" - ").append(throwable.getMessage());
            
            // Add stack trace for ERROR and FATAL levels
            if (level.getLevel() >= LogLevel.ERROR.getLevel()) {
                sb.append("\nStack Trace:\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                sb.append(sw.toString());
            }
        }
        
        return sb.toString();
    }
    
    private void writeToFile(String logEntry) {
        fileLock.lock();
        try {
            Files.write(logFile, (logEntry + System.lineSeparator()).getBytes(), 
                       StandardOpenOption.CREATE, 
                       StandardOpenOption.APPEND);
        } catch (IOException e) {
            // If file logging fails, at least log to console
            if (logToConsole) {
                System.err.println("Failed to write to log file: " + e.getMessage());
                System.err.println("Original log entry: " + logEntry);
            }
        } finally {
            fileLock.unlock();
        }
    }
    
    // Utility methods for common logging scenarios
    public void logVaultOperation(String operation, boolean success) {
        if (success) {
            logInfo("Vault operation successful: " + operation);
        } else {
            logWarning("Vault operation failed: " + operation);
        }
    }
    
    public void logSecurityEvent(String event) {
        logWarning("SECURITY EVENT: " + event);
    }
    
    public void logUserAction(String action) {
        logDebug("User action: " + action);
    }
    
    public void logSystemEvent(String event) {
        logInfo("System event: " + event);
    }
    
    // File management
    public void rotateLogs() {
        if (!logToFile || logFile == null) return;
        
        fileLock.lock();
        try {
            // Create new log file for today
            String fileName = "vault_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".log";
            Path newLogFile = logFile.getParent().resolve(fileName);
            
            if (!logFile.equals(newLogFile)) {
                logFile = newLogFile;
                logInfo("Log file rotated to: " + logFile.getFileName());
            }
            
        } catch (Exception e) {
            logError("Failed to rotate log file", e);
        } finally {
            fileLock.unlock();
        }
    }
    
    public void clearOldLogs(int daysToKeep) {
        if (!logToFile || logFile == null) return;
        
        try {
            Path logDir = logFile.getParent();
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            
            Files.list(logDir)
                 .filter(path -> path.getFileName().toString().startsWith("vault_"))
                 .filter(path -> path.getFileName().toString().endsWith(".log"))
                 .filter(path -> {
                     try {
                         return Files.getLastModifiedTime(path).toInstant()
                                     .isBefore(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant());
                     } catch (IOException e) {
                         return false;
                     }
                 })
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                         logInfo("Deleted old log file: " + path.getFileName());
                     } catch (IOException e) {
                         logWarning("Failed to delete old log file: " + path.getFileName());
                     }
                 });
                 
        } catch (Exception e) {
            logError("Failed to clear old logs", e);
        }
    }
    
    // Get log file path for external access
    public String getLogFilePath() {
        return logFile != null ? logFile.toString() : "No log file configured";
    }
    
    // Emergency logging when normal logging fails
    public void emergencyLog(String message) {
        System.err.println("[EMERGENCY] " + LocalDateTime.now().format(formatter) + " - " + message);
    }
}