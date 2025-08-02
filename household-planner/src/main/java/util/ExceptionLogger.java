package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for logging exceptions and errors
 */
public class ExceptionLogger {
    private static ExceptionLogger instance;
    private static final String LOG_FILE = "household_planner_errors.log";
    private List<String> sessionLogs;
    
    private ExceptionLogger() {
        this.sessionLogs = new ArrayList<>();
    }
    
    public static ExceptionLogger getInstance() {
        if (instance == null) {
            synchronized (ExceptionLogger.class) {
                if (instance == null) {
                    instance = new ExceptionLogger();
                }
            }
        }
        return instance;
    }
    
    public void logError(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logEntry = String.format("[%s] ERROR: %s", timestamp, message);
        
        sessionLogs.add(logEntry);
        writeToFile(logEntry);
        System.err.println(logEntry);
    }
    
    public void logError(String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logEntry = String.format("[%s] ERROR: %s - %s", timestamp, message, throwable.getMessage());
        
        sessionLogs.add(logEntry);
        writeToFile(logEntry);
        System.err.println(logEntry);
        
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
    
    public void logWarning(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logEntry = String.format("[%s] WARNING: %s", timestamp, message);
        
        sessionLogs.add(logEntry);
        writeToFile(logEntry);
        System.out.println(logEntry);
    }
    
    public void logInfo(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logEntry = String.format("[%s] INFO: %s", timestamp, message);
        
        sessionLogs.add(logEntry);
        writeToFile(logEntry);
    }
    
    private void writeToFile(String logEntry) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logEntry);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    public List<String> getSessionLogs() {
        return new ArrayList<>(sessionLogs);
    }
    
    public void clearSessionLogs() {
        sessionLogs.clear();
    }
}