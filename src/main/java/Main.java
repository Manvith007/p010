import console.ConsoleInterface;
import util.ExceptionLogger;

/**
 * Main entry point for Personal Digital Organizer & Secure Vault Console
 * 
 * This application provides a comprehensive, menu-driven console interface for:
 * - Password Management with encryption
 * - Secure Notes & Documents with tagging
 * - Checklist/To-Do tracking with due dates
 * - Event Scheduling with reminders
 * - Clipboard/History management
 * - Vault Settings and data export
 * 
 * Features implemented using Java standard packages only:
 * - Singleton pattern (VaultManager)
 * - Factory pattern (password generation)
 * - Decorator pattern (note highlighting/urgent marking)
 * - Iterator pattern (checklist sorting/filtering)
 * - Strategy pattern (checklist sorting)
 * - Chain of Responsibility (input validation)
 * - Template Method (vault export)
 * - Command pattern (clipboard undo/redo)
 * 
 * All data is persisted to encrypted files with proper exception handling.
 */
public class Main {
    public static void main(String[] args) {
        ExceptionLogger logger = ExceptionLogger.getInstance();
        
        try {
            // Log application startup
            logger.logSystemEvent("Personal Digital Organizer starting up");
            
            // Initialize and start the console interface
            ConsoleInterface console = new ConsoleInterface();
            console.start();
            
            // Log clean shutdown
            logger.logSystemEvent("Personal Digital Organizer shutting down");
            
        } catch (Exception e) {
            // Log any critical startup errors
            logger.logFatal("Critical error during application startup", e);
            System.err.println("Critical error occurred. Check logs for details.");
            System.exit(1);
        }
    }
}