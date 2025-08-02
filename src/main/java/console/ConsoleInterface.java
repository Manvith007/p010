package console;

import core.VaultManager;
import domain.*;
import util.ExceptionLogger;
import util.ValidatorChain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main console interface for the Personal Digital Organizer & Secure Vault
 * Provides menu-driven navigation and user interaction
 */
public class ConsoleInterface {
    private final Scanner scanner;
    private final VaultManager vaultManager;
    private final ExceptionLogger logger;
    private final ValidatorChain validator;
    private boolean running;
    
    // Console colors and formatting
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    
    public ConsoleInterface() {
        this.scanner = new Scanner(System.in);
        this.vaultManager = VaultManager.getInstance();
        this.logger = ExceptionLogger.getInstance();
        this.validator = ValidatorChain.getInstance();
        this.running = true;
        
        // Configure logger for console mode
        logger.setLogLevel(ExceptionLogger.LogLevel.INFO);
    }
    
    public void start() {
        printWelcome();
        
        // Authentication required first
        if (!authenticateUser()) {
            println(RED + "Authentication failed. Exiting..." + RESET);
            return;
        }
        
        logger.logSystemEvent("User authenticated successfully");
        
        // Main application loop
        while (running) {
            try {
                showMainMenu();
                int choice = getIntInput("Enter choice: ", 1, 7);
                handleMainMenuChoice(choice);
            } catch (Exception e) {
                logger.logError("Error in main loop", e);
                println(RED + "An error occurred. Please try again." + RESET);
            }
        }
        
        println(GREEN + "Thank you for using Personal Digital Organizer!" + RESET);
    }
    
    private void printWelcome() {
        clearScreen();
        println(BOLD + CYAN + "==========================================" + RESET);
        println(BOLD + CYAN + "  PERSONAL DIGITAL ORGANIZER & VAULT" + RESET);
        println(BOLD + CYAN + "==========================================" + RESET);
        println(BLUE + "A secure, menu-driven console application" + RESET);
        println(BLUE + "for managing passwords, notes, tasks & more" + RESET);
        println();
    }
    
    private boolean authenticateUser() {
        println(YELLOW + "🔐 Vault Authentication Required" + RESET);
        
        for (int attempts = 0; attempts < 3; attempts++) {
            print("Enter PIN (4-8 digits): ");
            String pin = scanner.nextLine().trim();
            
            if (vaultManager.authenticatePin(pin)) {
                println(GREEN + "✓ Authentication successful!" + RESET);
                return true;
            } else {
                println(RED + "✗ Invalid PIN. " + (2 - attempts) + " attempts remaining." + RESET);
                logger.logSecurityEvent("Failed PIN attempt");
            }
        }
        
        logger.logSecurityEvent("Max PIN attempts exceeded");
        return false;
    }
    
    private void showMainMenu() {
        println();
        println(BOLD + "========== MAIN MENU ==========" + RESET);
        println("1. Password Manager");
        println("2. Secure Notes & Documents");
        println("3. Checklist/To-Do Tracker");
        println("4. Event Scheduler & Reminders");
        println("5. Quick Clipboard/History");
        println("6. Vault Settings");
        println("7. Exit");
        println("================================");
    }
    
    private void handleMainMenuChoice(int choice) {
        switch (choice) {
            case 1: passwordManagerMenu(); break;
            case 2: secureNotesMenu(); break;
            case 3: checklistMenu(); break;
            case 4: eventSchedulerMenu(); break;
            case 5: clipboardMenu(); break;
            case 6: vaultSettingsMenu(); break;
            case 7: 
                running = false;
                vaultManager.lockVault();
                break;
        }
    }
    
    // Password Manager Module
    private void passwordManagerMenu() {
        while (true) {
            println();
            println(BOLD + BLUE + "========== PASSWORD MANAGER ==========" + RESET);
            println("1. Add Password");
            println("2. View All Passwords");
            println("3. Search Passwords");
            println("4. Update Password");
            println("5. Delete Password");
            println("6. Generate Strong Password");
            println("7. Export Passwords");
            println("8. Return to Main Menu");
            println("======================================");
            
            int choice = getIntInput("Enter choice: ", 1, 8);
            
            switch (choice) {
                case 1: addPassword(); break;
                case 2: viewAllPasswords(); break;
                case 3: searchPasswords(); break;
                case 4: updatePassword(); break;
                case 5: deletePassword(); break;
                case 6: generatePassword(); break;
                case 7: exportPasswords(); break;
                case 8: return;
            }
        }
    }
    
    private void addPassword() {
        println(CYAN + "\n--- Add New Password ---" + RESET);
        
        String site = getStringInput("Site/App name: ");
        String username = getStringInput("Username/Email: ");
        
        print("Password (leave empty to generate): ");
        String password = scanner.nextLine().trim();
        
        if (password.isEmpty()) {
            password = generateStrongPassword();
            println(GREEN + "Generated password: " + password + RESET);
        } else {
            ValidatorChain.PasswordStrength strength = validator.validatePasswordStrength(password);
            println("Password strength: " + getStrengthColor(strength) + strength + RESET);
        }
        
        String note = getStringInput("Note (optional): ");
        
        try {
            vaultManager.addPassword(site, username, password, note);
            println(GREEN + "✓ Password saved successfully!" + RESET);
            logger.logUserAction("Added password for " + site);
        } catch (Exception e) {
            println(RED + "✗ Failed to save password: " + e.getMessage() + RESET);
            logger.logError("Failed to add password", e);
        }
    }
    
    private void viewAllPasswords() {
        println(CYAN + "\n--- All Passwords ---" + RESET);
        
        try {
            List<PasswordEntry> passwords = vaultManager.getAllPasswords();
            
            if (passwords.isEmpty()) {
                println(YELLOW + "No passwords stored." + RESET);
                return;
            }
            
            for (int i = 0; i < passwords.size(); i++) {
                PasswordEntry entry = passwords.get(i);
                println(String.format("%d. %s", i + 1, entry.toString()));
            }
            
            int choice = getIntInput("\nSelect password to view details (0 to cancel): ", 0, passwords.size());
            if (choice > 0) {
                showPasswordDetails(passwords.get(choice - 1));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve passwords: " + e.getMessage() + RESET);
            logger.logError("Failed to view passwords", e);
        }
    }
    
    private void showPasswordDetails(PasswordEntry entry) {
        println(CYAN + "\n--- Password Details ---" + RESET);
        println("Site: " + entry.getSite());
        println("Username: " + entry.getUsername());
        println("Note: " + (entry.getNote() != null ? entry.getNote() : "None"));
        println("Last Modified: " + entry.getLastModified());
        
        if (getYesNoInput("Show password? (y/n): ")) {
            println(YELLOW + "Password: " + entry.decryptPassword() + RESET);
            vaultManager.addToClipboard(entry.decryptPassword());
            println(GREEN + "Password copied to clipboard!" + RESET);
        }
    }
    
    private void searchPasswords() {
        String searchTerm = getStringInput("Enter search term: ");
        
        try {
            List<PasswordEntry> results = vaultManager.searchPasswords(searchTerm);
            
            if (results.isEmpty()) {
                println(YELLOW + "No passwords found matching '" + searchTerm + "'" + RESET);
                return;
            }
            
            println(CYAN + "\n--- Search Results ---" + RESET);
            for (int i = 0; i < results.size(); i++) {
                println(String.format("%d. %s", i + 1, results.get(i).toString()));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Search failed: " + e.getMessage() + RESET);
            logger.logError("Password search failed", e);
        }
    }
    
    private void updatePassword() {
        String site = getStringInput("Site name: ");
        String username = getStringInput("Username: ");
        String newPassword = getStringInput("New password: ");
        
        try {
            if (vaultManager.updatePassword(site, username, newPassword)) {
                println(GREEN + "✓ Password updated successfully!" + RESET);
                logger.logUserAction("Updated password for " + site);
            } else {
                println(YELLOW + "Password entry not found." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Failed to update password: " + e.getMessage() + RESET);
            logger.logError("Failed to update password", e);
        }
    }
    
    private void deletePassword() {
        String site = getStringInput("Site name: ");
        String username = getStringInput("Username: ");
        
        if (getYesNoInput("Are you sure you want to delete this password? (y/n): ")) {
            try {
                if (vaultManager.deletePassword(site, username)) {
                    println(GREEN + "✓ Password deleted successfully!" + RESET);
                    logger.logUserAction("Deleted password for " + site);
                } else {
                    println(YELLOW + "Password entry not found." + RESET);
                }
            } catch (Exception e) {
                println(RED + "✗ Failed to delete password: " + e.getMessage() + RESET);
                logger.logError("Failed to delete password", e);
            }
        }
    }
    
    private void generatePassword() {
        int length = getIntInput("Password length (8-32): ", 8, 32);
        boolean includeSymbols = getYesNoInput("Include symbols? (y/n): ");
        
        String password = generateStrongPassword(length, includeSymbols);
        println(GREEN + "Generated password: " + password + RESET);
        
        vaultManager.addToClipboard(password);
        println(GREEN + "Password copied to clipboard!" + RESET);
    }
    
    private void exportPasswords() {
        String format = getStringInput("Export format (csv/json/txt): ").toLowerCase();
        String filename = getStringInput("Export filename: ");
        
        try {
            if (vaultManager.exportVault(filename, format)) {
                println(GREEN + "✓ Passwords exported successfully to " + filename + RESET);
                logger.logUserAction("Exported passwords as " + format);
            } else {
                println(RED + "✗ Export failed. Check format and filename." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Export failed: " + e.getMessage() + RESET);
            logger.logError("Password export failed", e);
        }
    }
    
    // Secure Notes Module
    private void secureNotesMenu() {
        while (true) {
            println();
            println(BOLD + GREEN + "========== SECURE NOTES ==========" + RESET);
            println("1. Add Note");
            println("2. View All Notes");
            println("3. Search Notes");
            println("4. Search by Tag");
            println("5. Update Note");
            println("6. Delete Note");
            println("7. Pin/Unpin Note");
            println("8. Return to Main Menu");
            println("==================================");
            
            int choice = getIntInput("Enter choice: ", 1, 8);
            
            switch (choice) {
                case 1: addNote(); break;
                case 2: viewAllNotes(); break;
                case 3: searchNotes(); break;
                case 4: searchNotesByTag(); break;
                case 5: updateNote(); break;
                case 6: deleteNote(); break;
                case 7: toggleNotePin(); break;
                case 8: return;
            }
        }
    }
    
    private void addNote() {
        println(CYAN + "\n--- Add New Note ---" + RESET);
        
        String title = getStringInput("Note title: ");
        String category = getStringInput("Category (optional): ");
        
        println("Enter note content (type 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        try {
            vaultManager.addNote(title, content.toString().trim(), category);
            println(GREEN + "✓ Note saved successfully!" + RESET);
            logger.logUserAction("Added note: " + title);
        } catch (Exception e) {
            println(RED + "✗ Failed to save note: " + e.getMessage() + RESET);
            logger.logError("Failed to add note", e);
        }
    }
    
    private void viewAllNotes() {
        println(CYAN + "\n--- All Notes ---" + RESET);
        
        try {
            List<SecureNote> notes = vaultManager.getAllNotes();
            
            if (notes.isEmpty()) {
                println(YELLOW + "No notes stored." + RESET);
                return;
            }
            
            // Sort pinned notes first
            notes.sort((a, b) -> Boolean.compare(b.isPinned(), a.isPinned()));
            
            for (int i = 0; i < notes.size(); i++) {
                SecureNote note = notes.get(i);
                println(String.format("%d. %s", i + 1, note.toString()));
            }
            
            int choice = getIntInput("\nSelect note to view details (0 to cancel): ", 0, notes.size());
            if (choice > 0) {
                showNoteDetails(notes.get(choice - 1));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve notes: " + e.getMessage() + RESET);
            logger.logError("Failed to view notes", e);
        }
    }
    
    private void showNoteDetails(SecureNote note) {
        println(CYAN + "\n--- Note Details ---" + RESET);
        println("Title: " + note.getTitle());
        println("Category: " + note.getCategory());
        println("Created: " + note.getCreated());
        println("Modified: " + note.getModified());
        println("Pinned: " + (note.isPinned() ? "Yes" : "No"));
        println("Tags: " + String.join(", ", note.getTags()));
        println("\nContent:");
        println("─".repeat(40));
        println(note.getContent());
        println("─".repeat(40));
    }
    
    private void searchNotes() {
        String searchTerm = getStringInput("Enter search term: ");
        
        try {
            List<SecureNote> results = vaultManager.searchNotes(searchTerm);
            
            if (results.isEmpty()) {
                println(YELLOW + "No notes found matching '" + searchTerm + "'" + RESET);
                return;
            }
            
            println(CYAN + "\n--- Search Results ---" + RESET);
            for (int i = 0; i < results.size(); i++) {
                println(String.format("%d. %s", i + 1, results.get(i).toString()));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Search failed: " + e.getMessage() + RESET);
            logger.logError("Note search failed", e);
        }
    }
    
    private void searchNotesByTag() {
        String tag = getStringInput("Enter tag: ");
        
        try {
            List<SecureNote> results = vaultManager.getNotesByTag(tag);
            
            if (results.isEmpty()) {
                println(YELLOW + "No notes found with tag '" + tag + "'" + RESET);
                return;
            }
            
            println(CYAN + "\n--- Notes with tag '" + tag + "' ---" + RESET);
            for (int i = 0; i < results.size(); i++) {
                println(String.format("%d. %s", i + 1, results.get(i).toString()));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Tag search failed: " + e.getMessage() + RESET);
            logger.logError("Note tag search failed", e);
        }
    }
    
    private void updateNote() {
        String title = getStringInput("Note title to update: ");
        
        println("Enter new content (type 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        try {
            if (vaultManager.updateNote(title, content.toString().trim())) {
                println(GREEN + "✓ Note updated successfully!" + RESET);
                logger.logUserAction("Updated note: " + title);
            } else {
                println(YELLOW + "Note not found." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Failed to update note: " + e.getMessage() + RESET);
            logger.logError("Failed to update note", e);
        }
    }
    
    private void deleteNote() {
        String title = getStringInput("Note title to delete: ");
        
        if (getYesNoInput("Are you sure you want to delete this note? (y/n): ")) {
            try {
                if (vaultManager.deleteNote(title)) {
                    println(GREEN + "✓ Note deleted successfully!" + RESET);
                    logger.logUserAction("Deleted note: " + title);
                } else {
                    println(YELLOW + "Note not found." + RESET);
                }
            } catch (Exception e) {
                println(RED + "✗ Failed to delete note: " + e.getMessage() + RESET);
                logger.logError("Failed to delete note", e);
            }
        }
    }
    
    private void toggleNotePin() {
        String title = getStringInput("Note title to pin/unpin: ");
        
        try {
            List<SecureNote> notes = vaultManager.getAllNotes();
            SecureNote note = notes.stream()
                    .filter(n -> n.getTitle().equals(title))
                    .findFirst()
                    .orElse(null);
            
            if (note != null) {
                note.setPinned(!note.isPinned());
                println(GREEN + "✓ Note " + (note.isPinned() ? "pinned" : "unpinned") + " successfully!" + RESET);
                logger.logUserAction((note.isPinned() ? "Pinned" : "Unpinned") + " note: " + title);
            } else {
                println(YELLOW + "Note not found." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Failed to toggle pin: " + e.getMessage() + RESET);
            logger.logError("Failed to toggle note pin", e);
        }
    }
    
    // Checklist Module
    private void checklistMenu() {
        while (true) {
            println();
            println(BOLD + PURPLE + "========== CHECKLIST TRACKER ==========" + RESET);
            println("1. Add Checklist Item");
            println("2. View All Items");
            println("3. Mark Item Done/Undone");
            println("4. View Overdue Items");
            println("5. Sort Items");
            println("6. Delete Item");
            println("7. Return to Main Menu");
            println("=======================================");
            
            int choice = getIntInput("Enter choice: ", 1, 7);
            
            switch (choice) {
                case 1: addChecklistItem(); break;
                case 2: viewAllChecklistItems(); break;
                case 3: toggleChecklistItem(); break;
                case 4: viewOverdueItems(); break;
                case 5: sortChecklistItems(); break;
                case 6: deleteChecklistItem(); break;
                case 7: return;
            }
        }
    }
    
    private void addChecklistItem() {
        println(CYAN + "\n--- Add Checklist Item ---" + RESET);
        
        String title = getStringInput("Item title: ");
        String description = getStringInput("Description: ");
        
        try {
            vaultManager.addChecklistItem(title, description);
            println(GREEN + "✓ Checklist item added successfully!" + RESET);
            logger.logUserAction("Added checklist item: " + title);
        } catch (Exception e) {
            println(RED + "✗ Failed to add item: " + e.getMessage() + RESET);
            logger.logError("Failed to add checklist item", e);
        }
    }
    
    private void viewAllChecklistItems() {
        println(CYAN + "\n--- All Checklist Items ---" + RESET);
        
        try {
            List<ChecklistItem> items = vaultManager.getAllChecklistItems();
            
            if (items.isEmpty()) {
                println(YELLOW + "No checklist items found." + RESET);
                return;
            }
            
            for (int i = 0; i < items.size(); i++) {
                ChecklistItem item = items.get(i);
                println(String.format("%d. %s", i + 1, item.toString()));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve items: " + e.getMessage() + RESET);
            logger.logError("Failed to view checklist items", e);
        }
    }
    
    private void toggleChecklistItem() {
        String title = getStringInput("Item title to toggle: ");
        
        try {
            if (vaultManager.markItemDone(title)) {
                println(GREEN + "✓ Item status updated!" + RESET);
                logger.logUserAction("Toggled checklist item: " + title);
            } else {
                println(YELLOW + "Item not found." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Failed to update item: " + e.getMessage() + RESET);
            logger.logError("Failed to toggle checklist item", e);
        }
    }
    
    private void viewOverdueItems() {
        println(CYAN + "\n--- Overdue Items ---" + RESET);
        
        try {
            List<ChecklistItem> overdue = vaultManager.getOverdueItems();
            
            if (overdue.isEmpty()) {
                println(GREEN + "No overdue items! 🎉" + RESET);
                return;
            }
            
            for (int i = 0; i < overdue.size(); i++) {
                ChecklistItem item = overdue.get(i);
                println(String.format("%d. %s", i + 1, RED + item.toString() + RESET));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve overdue items: " + e.getMessage() + RESET);
            logger.logError("Failed to view overdue items", e);
        }
    }
    
    private void sortChecklistItems() {
        println("Sort by:");
        println("1. Due Date");
        println("2. Priority");
        println("3. Completion Status");
        println("4. Title");
        
        int choice = getIntInput("Enter sort option: ", 1, 4);
        
        try {
            List<ChecklistItem> items = vaultManager.getAllChecklistItems();
            
            switch (choice) {
                case 1:
                    items.sort(Comparator.comparing(ChecklistItem::getDueDate, 
                                                  Comparator.nullsLast(Comparator.naturalOrder())));
                    break;
                case 2:
                    items.sort(Comparator.comparing(ChecklistItem::getPriority));
                    break;
                case 3:
                    items.sort(Comparator.comparing(ChecklistItem::isDone));
                    break;
                case 4:
                    items.sort(Comparator.comparing(ChecklistItem::getTitle));
                    break;
            }
            
            println(CYAN + "\n--- Sorted Checklist Items ---" + RESET);
            for (int i = 0; i < items.size(); i++) {
                println(String.format("%d. %s", i + 1, items.get(i).toString()));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to sort items: " + e.getMessage() + RESET);
            logger.logError("Failed to sort checklist items", e);
        }
    }
    
    private void deleteChecklistItem() {
        String title = getStringInput("Item title to delete: ");
        
        if (getYesNoInput("Are you sure you want to delete this item? (y/n): ")) {
            try {
                // Implementation would require delete method in VaultManager
                println(YELLOW + "Delete functionality not yet implemented." + RESET);
            } catch (Exception e) {
                println(RED + "✗ Failed to delete item: " + e.getMessage() + RESET);
                logger.logError("Failed to delete checklist item", e);
            }
        }
    }
    
    // Event Scheduler Module
    private void eventSchedulerMenu() {
        while (true) {
            println();
            println(BOLD + YELLOW + "========== EVENT SCHEDULER ==========" + RESET);
            println("1. Add Event");
            println("2. View All Events");
            println("3. View Upcoming Events");
            println("4. Search Events");
            println("5. Delete Event");
            println("6. Return to Main Menu");
            println("=====================================");
            
            int choice = getIntInput("Enter choice: ", 1, 6);
            
            switch (choice) {
                case 1: addEvent(); break;
                case 2: viewAllEvents(); break;
                case 3: viewUpcomingEvents(); break;
                case 4: searchEvents(); break;
                case 5: deleteEvent(); break;
                case 6: return;
            }
        }
    }
    
    private void addEvent() {
        println(CYAN + "\n--- Add New Event ---" + RESET);
        
        String description = getStringInput("Event description: ");
        LocalDateTime start = getDateTimeInput("Start date/time (YYYY-MM-DD HH:MM): ");
        LocalDateTime end = getDateTimeInput("End date/time (YYYY-MM-DD HH:MM): ");
        
        try {
            vaultManager.addEvent(description, start, end);
            println(GREEN + "✓ Event added successfully!" + RESET);
            logger.logUserAction("Added event: " + description);
        } catch (Exception e) {
            println(RED + "✗ Failed to add event: " + e.getMessage() + RESET);
            logger.logError("Failed to add event", e);
        }
    }
    
    private void viewAllEvents() {
        println(CYAN + "\n--- All Events ---" + RESET);
        
        try {
            List<Event> events = vaultManager.getAllEvents();
            
            if (events.isEmpty()) {
                println(YELLOW + "No events scheduled." + RESET);
                return;
            }
            
            // Sort by start time
            events.sort(Comparator.comparing(Event::getStart));
            
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                String color = event.isPast() ? RED : (event.isUpcoming(24) ? GREEN : RESET);
                println(String.format("%d. %s%s%s", i + 1, color, event.toString(), RESET));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve events: " + e.getMessage() + RESET);
            logger.logError("Failed to view events", e);
        }
    }
    
    private void viewUpcomingEvents() {
        int hours = getIntInput("Show events in next how many hours? ", 1, 168);
        
        try {
            List<Event> upcoming = vaultManager.getUpcomingEvents(hours);
            
            if (upcoming.isEmpty()) {
                println(YELLOW + "No upcoming events in the next " + hours + " hours." + RESET);
                return;
            }
            
            println(CYAN + "\n--- Upcoming Events ---" + RESET);
            for (int i = 0; i < upcoming.size(); i++) {
                Event event = upcoming.get(i);
                println(String.format("%d. %s%s%s", i + 1, GREEN, event.toString(), RESET));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve upcoming events: " + e.getMessage() + RESET);
            logger.logError("Failed to view upcoming events", e);
        }
    }
    
    private void searchEvents() {
        String searchTerm = getStringInput("Enter search term: ");
        
        try {
            List<Event> events = vaultManager.getAllEvents();
            List<Event> results = events.stream()
                    .filter(event -> event.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
            
            if (results.isEmpty()) {
                println(YELLOW + "No events found matching '" + searchTerm + "'" + RESET);
                return;
            }
            
            println(CYAN + "\n--- Search Results ---" + RESET);
            for (int i = 0; i < results.size(); i++) {
                println(String.format("%d. %s", i + 1, results.get(i).toString()));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Search failed: " + e.getMessage() + RESET);
            logger.logError("Event search failed", e);
        }
    }
    
    private void deleteEvent() {
        println(YELLOW + "Delete event functionality not yet implemented." + RESET);
    }
    
    // Clipboard Module
    private void clipboardMenu() {
        while (true) {
            println();
            println(BOLD + CYAN + "========== CLIPBOARD MANAGER ==========" + RESET);
            println("1. View Clipboard History");
            println("2. Add to Clipboard");
            println("3. Clear Clipboard");
            println("4. Return to Main Menu");
            println("======================================");
            
            int choice = getIntInput("Enter choice: ", 1, 4);
            
            switch (choice) {
                case 1: viewClipboardHistory(); break;
                case 2: addToClipboard(); break;
                case 3: clearClipboard(); break;
                case 4: return;
            }
        }
    }
    
    private void viewClipboardHistory() {
        println(CYAN + "\n--- Clipboard History ---" + RESET);
        
        try {
            List<String> history = vaultManager.getClipboardHistory();
            
            if (history.isEmpty()) {
                println(YELLOW + "Clipboard is empty." + RESET);
                return;
            }
            
            for (int i = 0; i < history.size(); i++) {
                String item = history.get(i);
                String preview = item.length() > 50 ? item.substring(0, 47) + "..." : item;
                println(String.format("%d. %s", i + 1, preview));
            }
            
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve clipboard: " + e.getMessage() + RESET);
            logger.logError("Failed to view clipboard", e);
        }
    }
    
    private void addToClipboard() {
        String text = getStringInput("Enter text to add to clipboard: ");
        
        try {
            vaultManager.addToClipboard(text);
            println(GREEN + "✓ Text added to clipboard!" + RESET);
            logger.logUserAction("Added text to clipboard");
        } catch (Exception e) {
            println(RED + "✗ Failed to add to clipboard: " + e.getMessage() + RESET);
            logger.logError("Failed to add to clipboard", e);
        }
    }
    
    private void clearClipboard() {
        if (getYesNoInput("Are you sure you want to clear clipboard history? (y/n): ")) {
            try {
                // Implementation would require clear method in VaultManager
                println(YELLOW + "Clear clipboard functionality not yet implemented." + RESET);
            } catch (Exception e) {
                println(RED + "✗ Failed to clear clipboard: " + e.getMessage() + RESET);
                logger.logError("Failed to clear clipboard", e);
            }
        }
    }
    
    // Vault Settings Module
    private void vaultSettingsMenu() {
        while (true) {
            println();
            println(BOLD + RED + "========== VAULT SETTINGS ==========" + RESET);
            println("1. Change PIN");
            println("2. Change Theme");
            println("3. Export All Data");
            println("4. View Logs");
            println("5. Wipe Vault (DANGER)");
            println("6. Return to Main Menu");
            println("===================================");
            
            int choice = getIntInput("Enter choice: ", 1, 6);
            
            switch (choice) {
                case 1: changePin(); break;
                case 2: changeTheme(); break;
                case 3: exportAllData(); break;
                case 4: viewLogs(); break;
                case 5: wipeVault(); break;
                case 6: return;
            }
        }
    }
    
    private void changePin() {
        String oldPin = getStringInput("Enter current PIN: ");
        String newPin = getStringInput("Enter new PIN (4-8 digits): ");
        String confirmPin = getStringInput("Confirm new PIN: ");
        
        if (!newPin.equals(confirmPin)) {
            println(RED + "✗ PINs do not match!" + RESET);
            return;
        }
        
        try {
            if (vaultManager.changePin(oldPin, newPin)) {
                println(GREEN + "✓ PIN changed successfully!" + RESET);
                logger.logSecurityEvent("PIN changed");
            } else {
                println(RED + "✗ Failed to change PIN. Check current PIN and new PIN format." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Failed to change PIN: " + e.getMessage() + RESET);
            logger.logError("Failed to change PIN", e);
        }
    }
    
    private void changeTheme() {
        println("Available themes:");
        println("1. default");
        println("2. dark");
        println("3. light");
        println("4. colorful");
        
        String theme = getStringInput("Enter theme name: ");
        
        try {
            vaultManager.setTheme(theme);
            println(GREEN + "✓ Theme changed to: " + theme + RESET);
            logger.logUserAction("Changed theme to " + theme);
        } catch (Exception e) {
            println(RED + "✗ Failed to change theme: " + e.getMessage() + RESET);
            logger.logError("Failed to change theme", e);
        }
    }
    
    private void exportAllData() {
        String format = getStringInput("Export format (csv/json/txt): ").toLowerCase();
        String filename = getStringInput("Export filename: ");
        
        try {
            if (vaultManager.exportVault(filename, format)) {
                println(GREEN + "✓ All data exported successfully to " + filename + RESET);
                logger.logUserAction("Exported all data as " + format);
            } else {
                println(RED + "✗ Export failed. Check format and filename." + RESET);
            }
        } catch (Exception e) {
            println(RED + "✗ Export failed: " + e.getMessage() + RESET);
            logger.logError("Data export failed", e);
        }
    }
    
    private void viewLogs() {
        println(CYAN + "\n--- System Information ---" + RESET);
        println("Log file: " + logger.getLogFilePath());
        println("Current theme: " + vaultManager.getTheme());
        println("Vault status: " + (vaultManager.isLocked() ? "Locked" : "Unlocked"));
        
        try {
            println("Password count: " + vaultManager.getAllPasswords().size());
            println("Notes count: " + vaultManager.getAllNotes().size());
            println("Checklist items: " + vaultManager.getAllChecklistItems().size());
            println("Events count: " + vaultManager.getAllEvents().size());
            println("Clipboard items: " + vaultManager.getClipboardHistory().size());
        } catch (Exception e) {
            println(RED + "✗ Failed to retrieve counts: " + e.getMessage() + RESET);
        }
    }
    
    private void wipeVault() {
        println(RED + BOLD + "⚠️  WARNING: This will permanently delete ALL vault data!" + RESET);
        
        if (getYesNoInput("Are you absolutely sure? (y/n): ")) {
            String confirmation = getStringInput("Type 'DELETE ALL' to confirm: ");
            
            if ("DELETE ALL".equals(confirmation)) {
                try {
                    vaultManager.wipeVault();
                    println(GREEN + "✓ Vault wiped successfully." + RESET);
                    logger.logSecurityEvent("Vault wiped by user");
                    running = false; // Exit application
                } catch (Exception e) {
                    println(RED + "✗ Failed to wipe vault: " + e.getMessage() + RESET);
                    logger.logError("Failed to wipe vault", e);
                }
            } else {
                println(YELLOW + "Wipe cancelled." + RESET);
            }
        }
    }
    
    // Utility methods
    private String getStringInput(String prompt) {
        print(prompt);
        return scanner.nextLine().trim();
    }
    
    private int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                println(RED + "Please enter a number between " + min + " and " + max + RESET);
            } catch (NumberFormatException e) {
                println(RED + "Please enter a valid number." + RESET);
            }
        }
    }
    
    private boolean getYesNoInput(String prompt) {
        while (true) {
            print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            println(RED + "Please enter 'y' or 'n'." + RESET);
        }
    }
    
    private LocalDateTime getDateTimeInput(String prompt) {
        while (true) {
            try {
                print(prompt);
                String input = scanner.nextLine().trim();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                return LocalDateTime.parse(input, formatter);
            } catch (DateTimeParseException e) {
                println(RED + "Please enter date/time in format: YYYY-MM-DD HH:MM" + RESET);
            }
        }
    }
    
    private String generateStrongPassword() {
        return generateStrongPassword(16, true);
    }
    
    private String generateStrongPassword(int length, boolean includeSymbols) {
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        
        StringBuilder chars = new StringBuilder();
        chars.append(lowercase).append(uppercase).append(digits);
        if (includeSymbols) {
            chars.append(symbols);
        }
        
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one character from each category
        password.append(lowercase.charAt(random.nextInt(lowercase.length())));
        password.append(uppercase.charAt(random.nextInt(uppercase.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        if (includeSymbols) {
            password.append(symbols.charAt(random.nextInt(symbols.length())));
        }
        
        // Fill remaining length
        for (int i = password.length(); i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        // Shuffle the password
        List<Character> passwordChars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(passwordChars);
        
        return passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
    
    private String getStrengthColor(ValidatorChain.PasswordStrength strength) {
        switch (strength) {
            case VERY_WEAK: return RED;
            case WEAK: return RED;
            case MEDIUM: return YELLOW;
            case STRONG: return GREEN;
            case VERY_STRONG: return GREEN + BOLD;
            default: return RESET;
        }
    }
    
    private void print(String message) {
        System.out.print(message);
    }
    
    private void println(String message) {
        System.out.println(message);
    }
    
    private void println() {
        System.out.println();
    }
    
    private void clearScreen() {
        // Simple clear screen (works on most terminals)
        System.out.print("\033[2J\033[H");
    }
}