package vault;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ExceptionLogger - Singleton for logging errors to file or console
 * Supports different log levels, formatting, and asynchronous logging
 */
public class ExceptionLogger {
    private static ExceptionLogger instance;
    private static final String LOG_FILE = "vault_data/vault.log";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_LOG_ENTRIES = 1000;
    
    private final ConcurrentLinkedQueue<LogEntry> logQueue;
    private LogLevel currentLogLevel;
    private boolean logToFile;
    private boolean logToConsole;
    private volatile boolean shutdown;
    
    public enum LogLevel {
        DEBUG(0, "DEBUG"),
        INFO(1, "INFO"),
        WARN(2, "WARN"),
        ERROR(3, "ERROR"),
        FATAL(4, "FATAL");
        
        private final int level;
        private final String name;
        
        LogLevel(int level, String name) {
            this.level = level;
            this.name = name;
        }
        
        public int getLevel() { return level; }
        public String getName() { return name; }
    }
    
    private ExceptionLogger() {
        this.logQueue = new ConcurrentLinkedQueue<>();
        this.currentLogLevel = LogLevel.INFO;
        this.logToFile = true;
        this.logToConsole = true;
        this.shutdown = false;
        
        // Initialize log directory
        initializeLogDirectory();
        
        // Start background logging thread
        startLoggingThread();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    public static synchronized ExceptionLogger getInstance() {
        if (instance == null) {
            instance = new ExceptionLogger();
        }
        return instance;
    }
    
    // Static convenience methods
    public static void log(String message, Exception exception) {
        getInstance().log(LogLevel.ERROR, message, exception);
    }
    
    public static void info(String message) {
        getInstance().log(LogLevel.INFO, message, null);
    }
    
    public static void warn(String message) {
        getInstance().log(LogLevel.WARN, message, null);
    }
    
    public static void error(String message) {
        getInstance().log(LogLevel.ERROR, message, null);
    }
    
    public static void debug(String message) {
        getInstance().log(LogLevel.DEBUG, message, null);
    }
    
    public static void fatal(String message, Exception exception) {
        getInstance().log(LogLevel.FATAL, message, exception);
    }
    
    // Main logging method
    public void log(LogLevel level, String message, Exception exception) {
        if (level.getLevel() < currentLogLevel.getLevel()) {
            return; // Skip if below current log level
        }
        
        LogEntry entry = new LogEntry(level, message, exception, Thread.currentThread().getName());
        
        // Add to queue for background processing
        logQueue.offer(entry);
        
        // Maintain queue size
        while (logQueue.size() > MAX_LOG_ENTRIES) {
            logQueue.poll();
        }
        
        // For fatal errors, log immediately
        if (level == LogLevel.FATAL) {
            processLogEntry(entry);
        }
    }
    
    private void initializeLogDirectory() {
        try {
            Path logPath = Paths.get(LOG_FILE).getParent();
            if (logPath != null && !Files.exists(logPath)) {
                Files.createDirectories(logPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create log directory: " + e.getMessage());
            logToFile = false; // Disable file logging if directory creation fails
        }
    }
    
    private void startLoggingThread() {
        Thread loggingThread = new Thread(() -> {
            while (!shutdown || !logQueue.isEmpty()) {
                try {
                    LogEntry entry = logQueue.poll();
                    if (entry != null) {
                        processLogEntry(entry);
                    } else {
                        Thread.sleep(100); // Brief pause if no entries
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in logging thread: " + e.getMessage());
                }
            }
        }, "VaultLogger");
        
        loggingThread.setDaemon(true);
        loggingThread.start();
    }
    
    private void processLogEntry(LogEntry entry) {
        String formattedMessage = formatLogEntry(entry);
        
        if (logToConsole) {
            writeToConsole(entry.getLevel(), formattedMessage);
        }
        
        if (logToFile) {
            writeToFile(formattedMessage);
        }
    }
    
    private String formatLogEntry(LogEntry entry) {
        StringBuilder sb = new StringBuilder();
        
        // Timestamp
        sb.append("[").append(entry.getTimestamp().format(TIMESTAMP_FORMAT)).append("] ");
        
        // Log level
        sb.append("[").append(entry.getLevel().getName()).append("] ");
        
        // Thread name
        sb.append("[").append(entry.getThreadName()).append("] ");
        
        // Message
        sb.append(entry.getMessage());
        
        // Exception details
        if (entry.getException() != null) {
            sb.append("\n").append(formatException(entry.getException()));
        }
        
        return sb.toString();
    }
    
    private String formatException(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append("Exception: ").append(exception.getClass().getSimpleName());
        sb.append(" - ").append(exception.getMessage()).append("\n");
        
        // Stack trace (limited to first 10 lines for readability)
        StackTraceElement[] stackTrace = exception.getStackTrace();
        int maxLines = Math.min(10, stackTrace.length);
        
        for (int i = 0; i < maxLines; i++) {
            sb.append("    at ").append(stackTrace[i].toString()).append("\n");
        }
        
        if (stackTrace.length > maxLines) {
            sb.append("    ... ").append(stackTrace.length - maxLines).append(" more lines\n");
        }
        
        // Caused by (if present)
        Throwable cause = exception.getCause();
        if (cause != null && cause != exception) {
            sb.append("Caused by: ").append(cause.getClass().getSimpleName());
            sb.append(" - ").append(cause.getMessage()).append("\n");
        }
        
        return sb.toString();
    }
    
    private void writeToConsole(LogLevel level, String message) {
        PrintStream stream = (level.getLevel() >= LogLevel.ERROR.getLevel()) ? System.err : System.out;
        stream.println(message);
    }
    
    private void writeToFile(String message) {
        try {
            Path logPath = Paths.get(LOG_FILE);
            
            // Rotate log file if it gets too large (>1MB)
            if (Files.exists(logPath) && Files.size(logPath) > 1024 * 1024) {
                rotateLogFile(logPath);
            }
            
            // Append to log file
            Files.write(logPath, (message + "\n").getBytes(), 
                       StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
            // Don't disable file logging here to avoid infinite recursion
        }
    }
    
    private void rotateLogFile(Path logPath) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path backupPath = logPath.resolveSibling(logPath.getFileName() + "." + timestamp);
        
        Files.move(logPath, backupPath);
        
        // Keep only last 5 backup files
        cleanupOldLogFiles(logPath.getParent());
    }
    
    private void cleanupOldLogFiles(Path logDir) {
        try {
            Files.list(logDir)
                 .filter(path -> path.getFileName().toString().startsWith("vault.log."))
                 .sorted((p1, p2) -> {
                     try {
                         return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                     } catch (IOException e) {
                         return 0;
                     }
                 })
                 .skip(5) // Keep newest 5
                 .forEach(path -> {
                     try {
                         Files.delete(path);
                     } catch (IOException e) {
                         // Ignore deletion failures
                     }
                 });
        } catch (IOException e) {
            // Ignore cleanup failures
        }
    }
    
    // Configuration methods
    public void setLogLevel(LogLevel level) {
        this.currentLogLevel = level;
    }
    
    public void setLogToFile(boolean logToFile) {
        this.logToFile = logToFile;
    }
    
    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }
    
    public LogLevel getLogLevel() {
        return currentLogLevel;
    }
    
    public boolean isLogToFile() {
        return logToFile;
    }
    
    public boolean isLogToConsole() {
        return logToConsole;
    }
    
    // Shutdown method
    public void shutdown() {
        shutdown = true;
        
        // Process remaining log entries
        LogEntry entry;
        while ((entry = logQueue.poll()) != null) {
            processLogEntry(entry);
        }
    }
    
    // LogEntry inner class
    private static class LogEntry {
        private final LogLevel level;
        private final String message;
        private final Exception exception;
        private final String threadName;
        private final LocalDateTime timestamp;
        
        public LogEntry(LogLevel level, String message, Exception exception, String threadName) {
            this.level = level;
            this.message = message != null ? message : "";
            this.exception = exception;
            this.threadName = threadName;
            this.timestamp = LocalDateTime.now();
        }
        
        public LogLevel getLevel() { return level; }
        public String getMessage() { return message; }
        public Exception getException() { return exception; }
        public String getThreadName() { return threadName; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
    
    // Utility methods for getting log statistics
    public int getQueueSize() {
        return logQueue.size();
    }
    
    public String getLogFilePath() {
        return LOG_FILE;
    }
    
    public boolean isLogFileExists() {
        return Files.exists(Paths.get(LOG_FILE));
    }
    
    public long getLogFileSize() {
        try {
            return Files.size(Paths.get(LOG_FILE));
        } catch (IOException e) {
            return 0;
        }
    }
}