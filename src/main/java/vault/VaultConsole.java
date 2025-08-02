package vault;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * VaultConsole - Main console interface with comprehensive menu system
 * Handles user input and provides access to all vault functionality
 */
public class VaultConsole {
    private final Scanner scanner;
    private final VaultManager vault;
    private boolean running;
    private Theme currentTheme;
    
    public VaultConsole() {
        this.scanner = new Scanner(System.in);
        this.vault = VaultManager.getInstance();
        this.running = true;
        this.currentTheme = vault.getSettings().getTheme();
    }
    
    public void start() {
        displayWelcome();
        
        // Authentication loop
        if (!authenticate()) {
            System.out.println("Authentication failed. Exiting...");
            return;
        }
        
        // Main application loop
        while (running) {
            try {
                displayMainMenu();
                handleMainMenuChoice();
            } catch (Exception e) {
                ExceptionLogger.error("Error in main menu: " + e.getMessage());
                System.out.println("An error occurred. Please try again.");
            }
        }
        
        shutdown();
    }
    
    private void displayWelcome() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("PERSONAL DIGITAL ORGANIZER & SECURE VAULT"));
        System.out.println();
        System.out.println("Welcome to your secure digital vault!");
        System.out.println("Please authenticate to continue.");
        System.out.println();
    }
    
    private boolean authenticate() {
        int maxAttempts = 3;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (vault.getSettings().isLockedOut()) {
                long remainingTime = vault.getSettings().getRemainingLockoutTime();
                System.out.printf("Account locked. Please wait %d seconds.\n", remainingTime / 1000);
                return false;
            }
            
            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine();
            
            if (vault.authenticate(pin)) {
                System.out.println(currentTheme.getSuccessSymbol() + " Authentication successful!");
                return true;
            } else {
                int remaining = maxAttempts - attempt;
                if (remaining > 0) {
                    System.out.println(currentTheme.getErrorSymbol() + 
                                     " Invalid PIN. " + remaining + " attempts remaining.");
                } else {
                    System.out.println(currentTheme.getErrorSymbol() + " Authentication failed.");
                }
            }
        }
        
        return false;
    }
    
    private void displayMainMenu() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("MAIN MENU"));
        System.out.println();
        
        String[] menuItems = {
            "Password Manager",
            "Secure Notes & Documents", 
            "Checklist/To-Do Tracker",
            "Event Scheduler & Reminders",
            "Quick Clipboard/History",
            "Vault Settings (PIN, Theme, Export, Import)",
            "Exit"
        };
        
        for (int i = 0; i < menuItems.length; i++) {
            System.out.println(currentTheme.formatMenuItem(i + 1, menuItems[i], false));
        }
        
        System.out.println();
        System.out.println(currentTheme.getMenuSeparator().repeat(50));
        System.out.print("Enter choice: ");
    }
    
    private void handleMainMenuChoice() {
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1: passwordManagerMenu(); break;
                case 2: notesMenu(); break;
                case 3: checklistMenu(); break;
                case 4: eventSchedulerMenu(); break;
                case 5: clipboardMenu(); break;
                case 6: settingsMenu(); break;
                case 7: 
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    pause();
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            pause();
        }
    }
    
    // Password Manager Menu
    private void passwordManagerMenu() {
        boolean inPasswordMenu = true;
        
        while (inPasswordMenu) {
            clearScreen();
            System.out.println(currentTheme.formatTitle("PASSWORD MANAGER"));
            System.out.println();
            
            String[] items = {
                "Add Password",
                "View/Search Passwords", 
                "Update Password",
                "Delete Password",
                "Generate Strong Password",
                "Export Passwords",
                "Return to Main Menu"
            };
            
            for (int i = 0; i < items.length; i++) {
                System.out.println(currentTheme.formatMenuItem(i + 1, items[i], false));
            }
            
            System.out.print("\nEnter choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1: addPassword(); break;
                    case 2: searchPasswords(); break;
                    case 3: updatePassword(); break;
                    case 4: deletePassword(); break;
                    case 5: generatePassword(); break;
                    case 6: exportPasswords(); break;
                    case 7: inPasswordMenu = false; break;
                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                pause();
            }
        }
    }
    
    private void addPassword() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("ADD PASSWORD"));
        System.out.println();
        
        System.out.print("Site/App: ");
        String site = scanner.nextLine().trim();
        
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        System.out.print("Note (optional): ");
        String note = scanner.nextLine().trim();
        
        try {
            vault.addPassword(site, username, password, note);
            System.out.println(currentTheme.getSuccessSymbol() + " Password added successfully!");
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to add password: " + e.getMessage());
        }
        
        pause();
    }
    
    private void searchPasswords() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("SEARCH PASSWORDS"));
        System.out.println();
        
        System.out.print("Search query (site or username): ");
        String query = scanner.nextLine().trim();
        
        try {
            List<PasswordEntry> results = vault.searchPasswords(query);
            
            if (results.isEmpty()) {
                System.out.println("No passwords found matching: " + query);
            } else {
                System.out.println("\nFound " + results.size() + " password(s):");
                System.out.println(currentTheme.getMenuSeparator().repeat(60));
                
                for (int i = 0; i < results.size(); i++) {
                    PasswordEntry entry = results.get(i);
                    System.out.printf("%d. Site: %s\n", i + 1, entry.getSite());
                    System.out.printf("   Username: %s\n", entry.getUsername());
                    System.out.printf("   Note: %s\n", entry.getNote());
                    System.out.printf("   Last Modified: %s\n", 
                                    entry.getLastModified().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Search failed: " + e.getMessage());
        }
        
        pause();
    }
    
    private void updatePassword() {
        // Implementation similar to addPassword but with selection logic
        System.out.println("Update password functionality - implementation similar to add/search");
        pause();
    }
    
    private void deletePassword() {
        // Implementation with search and confirmation
        System.out.println("Delete password functionality - implementation with confirmation");
        pause();
    }
    
    private void generatePassword() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("GENERATE PASSWORD"));
        System.out.println();
        
        System.out.print("Password length (8-50): ");
        try {
            int length = Integer.parseInt(scanner.nextLine().trim());
            length = Math.max(8, Math.min(50, length));
            
            String password = PasswordGenerator.generate(length);
            System.out.println("Generated password: " + password);
            
            vault.addToClipboard(password);
            System.out.println(currentTheme.getInfoSymbol() + " Password copied to clipboard history");
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid length. Using default length of 12.");
            String password = PasswordGenerator.generate(12);
            System.out.println("Generated password: " + password);
        }
        
        pause();
    }
    
    private void exportPasswords() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("EXPORT PASSWORDS"));
        System.out.println();
        
        System.out.println("Select export format:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. Text");
        System.out.print("Choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            String format = "";
            
            switch (choice) {
                case 1: format = "csv"; break;
                case 2: format = "json"; break;
                case 3: format = "txt"; break;
                default:
                    System.out.println("Invalid choice.");
                    pause();
                    return;
            }
            
            System.out.print("Export file path: ");
            String filePath = scanner.nextLine().trim();
            
            vault.exportAllData(format, filePath);
            System.out.println(currentTheme.getSuccessSymbol() + " Export completed successfully!");
            
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Export failed: " + e.getMessage());
        }
        
        pause();
    }
    
    // Notes Menu
    private void notesMenu() {
        boolean inNotesMenu = true;
        
        while (inNotesMenu) {
            clearScreen();
            System.out.println(currentTheme.formatTitle("SECURE NOTES & DOCUMENTS"));
            System.out.println();
            
            String[] items = {
                "Add Note",
                "View/Search Notes",
                "Edit Note", 
                "Delete Note",
                "Pin/Unpin Note",
                "Add Tags",
                "Return to Main Menu"
            };
            
            for (int i = 0; i < items.length; i++) {
                System.out.println(currentTheme.formatMenuItem(i + 1, items[i], false));
            }
            
            System.out.print("\nEnter choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1: addNote(); break;
                    case 2: searchNotes(); break;
                    case 3: editNote(); break;
                    case 4: deleteNote(); break;
                    case 5: pinNote(); break;
                    case 6: addNoteTags(); break;
                    case 7: inNotesMenu = false; break;
                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                pause();
            }
        }
    }
    
    private void addNote() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("ADD NOTE"));
        System.out.println();
        
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        
        System.out.println("Content (enter 'END' on a new line to finish):");
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            content.append(line).append("\n");
        }
        
        System.out.print("Tags (comma-separated, optional): ");
        String tagsInput = scanner.nextLine().trim();
        Set<String> tags = new HashSet<>();
        if (!tagsInput.isEmpty()) {
            tags.addAll(Arrays.asList(tagsInput.split(",\\s*")));
        }
        
        try {
            vault.addNote(title, content.toString().trim(), tags);
            System.out.println(currentTheme.getSuccessSymbol() + " Note added successfully!");
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to add note: " + e.getMessage());
        }
        
        pause();
    }
    
    private void searchNotes() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("SEARCH NOTES"));
        System.out.println();
        
        System.out.print("Search query: ");
        String query = scanner.nextLine().trim();
        
        try {
            List<Note> results = vault.searchNotes(query);
            
            if (results.isEmpty()) {
                System.out.println("No notes found matching: " + query);
            } else {
                System.out.println("\nFound " + results.size() + " note(s):");
                System.out.println(currentTheme.getMenuSeparator().repeat(60));
                
                for (int i = 0; i < results.size(); i++) {
                    Note note = results.get(i);
                    System.out.printf("%d. %s\n", i + 1, note.getDisplayString());
                    System.out.println(currentTheme.getMenuSeparator().repeat(40));
                }
            }
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Search failed: " + e.getMessage());
        }
        
        pause();
    }
    
    private void editNote() {
        System.out.println("Edit note functionality - implementation with selection and editing");
        pause();
    }
    
    private void deleteNote() {
        System.out.println("Delete note functionality - implementation with confirmation");
        pause();
    }
    
    private void pinNote() {
        System.out.println("Pin/Unpin note functionality - Decorator pattern implementation");
        pause();
    }
    
    private void addNoteTags() {
        System.out.println("Add tags to note functionality");
        pause();
    }
    
    // Checklist Menu
    private void checklistMenu() {
        boolean inChecklistMenu = true;
        
        while (inChecklistMenu) {
            clearScreen();
            System.out.println(currentTheme.formatTitle("CHECKLIST/TO-DO TRACKER"));
            System.out.println();
            
            String[] items = {
                "Add Checklist Item",
                "View Pending Items",
                "View Completed Items", 
                "Mark Item Done/Undone",
                "Set Due Date",
                "Set Priority",
                "Search Items",
                "Export Checklist",
                "Return to Main Menu"
            };
            
            for (int i = 0; i < items.length; i++) {
                System.out.println(currentTheme.formatMenuItem(i + 1, items[i], false));
            }
            
            System.out.print("\nEnter choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1: addChecklistItem(); break;
                    case 2: viewPendingItems(); break;
                    case 3: viewCompletedItems(); break;
                    case 4: toggleItemStatus(); break;
                    case 5: setDueDate(); break;
                    case 6: setPriority(); break;
                    case 7: searchChecklistItems(); break;
                    case 8: exportChecklist(); break;
                    case 9: inChecklistMenu = false; break;
                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                pause();
            }
        }
    }
    
    private void addChecklistItem() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("ADD CHECKLIST ITEM"));
        System.out.println();
        
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        
        System.out.print("Tags (comma-separated, optional): ");
        String tagsInput = scanner.nextLine().trim();
        Set<String> tags = new HashSet<>();
        if (!tagsInput.isEmpty()) {
            tags.addAll(Arrays.asList(tagsInput.split(",\\s*")));
        }
        
        try {
            vault.addChecklist(title, tags);
            System.out.println(currentTheme.getSuccessSymbol() + " Checklist item added successfully!");
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to add item: " + e.getMessage());
        }
        
        pause();
    }
    
    private void viewPendingItems() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("PENDING ITEMS"));
        System.out.println();
        
        try {
            List<ChecklistItem> items = vault.getChecklistsByStatus(false);
            
            if (items.isEmpty()) {
                System.out.println("No pending items found.");
            } else {
                // Sort by priority and due date
                items.sort(ChecklistItem.byPriority().thenComparing(ChecklistItem.byDueDate()));
                
                for (int i = 0; i < items.size(); i++) {
                    ChecklistItem item = items.get(i);
                    System.out.printf("%d. %s\n", i + 1, item.getDisplayString());
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to load items: " + e.getMessage());
        }
        
        pause();
    }
    
    private void viewCompletedItems() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("COMPLETED ITEMS"));
        System.out.println();
        
        try {
            List<ChecklistItem> items = vault.getChecklistsByStatus(true);
            
            if (items.isEmpty()) {
                System.out.println("No completed items found.");
            } else {
                for (int i = 0; i < items.size(); i++) {
                    ChecklistItem item = items.get(i);
                    System.out.printf("%d. %s\n", i + 1, item.getDisplayString());
                    System.out.println();
                }
            }
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to load items: " + e.getMessage());
        }
        
        pause();
    }
    
    private void toggleItemStatus() {
        System.out.println("Toggle item status functionality - implementation with item selection");
        pause();
    }
    
    private void setDueDate() {
        System.out.println("Set due date functionality - with date parsing");
        pause();
    }
    
    private void setPriority() {
        System.out.println("Set priority functionality - with priority selection");
        pause();
    }
    
    private void searchChecklistItems() {
        System.out.println("Search checklist items functionality");
        pause();
    }
    
    private void exportChecklist() {
        System.out.println("Export checklist functionality");
        pause();
    }
    
    // Event Scheduler Menu
    private void eventSchedulerMenu() {
        boolean inEventMenu = true;
        
        while (inEventMenu) {
            clearScreen();
            System.out.println(currentTheme.formatTitle("EVENT SCHEDULER & REMINDERS"));
            System.out.println();
            
            String[] items = {
                "Add Event",
                "View Upcoming Events",
                "View All Events", 
                "Set Reminder",
                "Edit Event",
                "Delete Event",
                "Return to Main Menu"
            };
            
            for (int i = 0; i < items.length; i++) {
                System.out.println(currentTheme.formatMenuItem(i + 1, items[i], false));
            }
            
            System.out.print("\nEnter choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1: addEvent(); break;
                    case 2: viewUpcomingEvents(); break;
                    case 3: viewAllEvents(); break;
                    case 4: setEventReminder(); break;
                    case 5: editEvent(); break;
                    case 6: deleteEvent(); break;
                    case 7: inEventMenu = false; break;
                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                pause();
            }
        }
    }
    
    private void addEvent() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("ADD EVENT"));
        System.out.println();
        
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        
        LocalDateTime start = null, end = null;
        
        try {
            System.out.print("Start date/time (yyyy-MM-dd HH:mm): ");
            String startInput = scanner.nextLine().trim();
            start = LocalDateTime.parse(startInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            System.out.print("End date/time (yyyy-MM-dd HH:mm): ");
            String endInput = scanner.nextLine().trim();
            end = LocalDateTime.parse(endInput, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
        } catch (DateTimeParseException e) {
            System.out.println(currentTheme.getErrorSymbol() + " Invalid date format. Use yyyy-MM-dd HH:mm");
            pause();
            return;
        }
        
        System.out.print("Recurring event? (y/n): ");
        boolean recurring = scanner.nextLine().trim().toLowerCase().startsWith("y");
        
        System.out.print("Set reminder? (y/n): ");
        boolean reminder = scanner.nextLine().trim().toLowerCase().startsWith("y");
        
        System.out.print("Tags (comma-separated, optional): ");
        String tagsInput = scanner.nextLine().trim();
        Set<String> tags = new HashSet<>();
        if (!tagsInput.isEmpty()) {
            tags.addAll(Arrays.asList(tagsInput.split(",\\s*")));
        }
        
        try {
            vault.addEvent(description, start, end, recurring, tags, reminder);
            System.out.println(currentTheme.getSuccessSymbol() + " Event added successfully!");
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to add event: " + e.getMessage());
        }
        
        pause();
    }
    
    private void viewUpcomingEvents() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("UPCOMING EVENTS"));
        System.out.println();
        
        try {
            List<Event> events = vault.getUpcomingEvents(30); // Next 30 days
            
            if (events.isEmpty()) {
                System.out.println("No upcoming events in the next 30 days.");
            } else {
                for (int i = 0; i < events.size(); i++) {
                    Event event = events.get(i);
                    System.out.printf("%d. %s\n", i + 1, event.getDisplayString());
                    System.out.println(currentTheme.getMenuSeparator().repeat(40));
                }
            }
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Failed to load events: " + e.getMessage());
        }
        
        pause();
    }
    
    private void viewAllEvents() {
        System.out.println("View all events functionality");
        pause();
    }
    
    private void setEventReminder() {
        System.out.println("Set event reminder functionality");
        pause();
    }
    
    private void editEvent() {
        System.out.println("Edit event functionality");
        pause();
    }
    
    private void deleteEvent() {
        System.out.println("Delete event functionality");
        pause();
    }
    
    // Clipboard Menu
    private void clipboardMenu() {
        boolean inClipboardMenu = true;
        
        while (inClipboardMenu) {
            clearScreen();
            System.out.println(currentTheme.formatTitle("CLIPBOARD HISTORY"));
            System.out.println();
            
            String[] items = {
                "View History",
                "Search History",
                "Add to History", 
                "Clear History",
                "View Statistics",
                "Return to Main Menu"
            };
            
            for (int i = 0; i < items.length; i++) {
                System.out.println(currentTheme.formatMenuItem(i + 1, items[i], false));
            }
            
            System.out.print("\nEnter choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1: viewClipboardHistory(); break;
                    case 2: searchClipboardHistory(); break;
                    case 3: addToClipboardHistory(); break;
                    case 4: clearClipboardHistory(); break;
                    case 5: viewClipboardStats(); break;
                    case 6: inClipboardMenu = false; break;
                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                pause();
            }
        }
    }
    
    private void viewClipboardHistory() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("CLIPBOARD HISTORY"));
        System.out.println();
        
        List<String> history = vault.getClipboardHistory();
        
        if (history.isEmpty()) {
            System.out.println("Clipboard history is empty.");
        } else {
            System.out.println("Recent clipboard items:");
            System.out.println(currentTheme.getMenuSeparator().repeat(60));
            
            for (int i = 0; i < Math.min(20, history.size()); i++) {
                String item = history.get(i);
                String preview = item.length() > 80 ? item.substring(0, 80) + "..." : item;
                System.out.printf("%d. %s\n", i + 1, preview);
            }
            
            if (history.size() > 20) {
                System.out.printf("... and %d more items\n", history.size() - 20);
            }
        }
        
        pause();
    }
    
    private void searchClipboardHistory() {
        System.out.println("Search clipboard history functionality");
        pause();
    }
    
    private void addToClipboardHistory() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("ADD TO CLIPBOARD"));
        System.out.println();
        
        System.out.print("Enter text to add: ");
        String text = scanner.nextLine();
        
        vault.addToClipboard(text);
        System.out.println(currentTheme.getSuccessSymbol() + " Added to clipboard history!");
        
        pause();
    }
    
    private void clearClipboardHistory() {
        System.out.println("Clear clipboard history functionality - with confirmation");
        pause();
    }
    
    private void viewClipboardStats() {
        System.out.println("View clipboard statistics functionality");
        pause();
    }
    
    // Settings Menu
    private void settingsMenu() {
        boolean inSettingsMenu = true;
        
        while (inSettingsMenu) {
            clearScreen();
            System.out.println(currentTheme.formatTitle("VAULT SETTINGS"));
            System.out.println();
            
            String[] items = {
                "Change PIN",
                "Change Theme",
                "Export All Data", 
                "Import Data",
                "Auto-lock Settings",
                "View System Info",
                "Wipe Vault (DANGER)",
                "Return to Main Menu"
            };
            
            for (int i = 0; i < items.length; i++) {
                System.out.println(currentTheme.formatMenuItem(i + 1, items[i], false));
            }
            
            System.out.print("\nEnter choice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1: changePin(); break;
                    case 2: changeTheme(); break;
                    case 3: exportAllData(); break;
                    case 4: importData(); break;
                    case 5: autoLockSettings(); break;
                    case 6: viewSystemInfo(); break;
                    case 7: wipeVault(); break;
                    case 8: inSettingsMenu = false; break;
                    default:
                        System.out.println("Invalid choice.");
                        pause();
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
                pause();
            }
        }
    }
    
    private void changePin() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("CHANGE PIN"));
        System.out.println();
        
        System.out.print("Enter new PIN (4-20 digits): ");
        String newPin = scanner.nextLine().trim();
        
        if (vault.getSettings().isValidPin(newPin)) {
            System.out.print("Confirm new PIN: ");
            String confirmPin = scanner.nextLine().trim();
            
            if (newPin.equals(confirmPin)) {
                try {
                    vault.getSettings().setPin(newPin);
                    System.out.println(currentTheme.getSuccessSymbol() + " PIN changed successfully!");
                } catch (Exception e) {
                    System.out.println(currentTheme.getErrorSymbol() + " Failed to change PIN: " + e.getMessage());
                }
            } else {
                System.out.println(currentTheme.getErrorSymbol() + " PINs do not match.");
            }
        } else {
            System.out.println(currentTheme.getErrorSymbol() + " Invalid PIN format. Use 4-20 digits only.");
        }
        
        pause();
    }
    
    private void changeTheme() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("CHANGE THEME"));
        System.out.println();
        
        Theme[] themes = Theme.values();
        for (int i = 0; i < themes.length; i++) {
            System.out.printf("%d. %s - %s\n", i + 1, themes[i].getDisplayName(), themes[i].getDescription());
        }
        
        System.out.print("\nSelect theme: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice >= 1 && choice <= themes.length) {
                Theme newTheme = themes[choice - 1];
                vault.updateSettings(null, newTheme); // null means don't change PIN
                this.currentTheme = newTheme;
                System.out.println(newTheme.getSuccessSymbol() + " Theme changed to " + newTheme.getDisplayName());
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
        }
        
        pause();
    }
    
    private void exportAllData() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("EXPORT ALL DATA"));
        System.out.println();
        
        System.out.println("Select export format:");
        System.out.println("1. CSV");
        System.out.println("2. JSON");
        System.out.println("3. XML");
        System.out.println("4. Text");
        System.out.println("5. Serialized");
        System.out.print("Choice: ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            String format = "";
            
            switch (choice) {
                case 1: format = "csv"; break;
                case 2: format = "json"; break;
                case 3: format = "xml"; break;
                case 4: format = "txt"; break;
                case 5: format = "serialized"; break;
                default:
                    System.out.println("Invalid choice.");
                    pause();
                    return;
            }
            
            System.out.print("Export file path: ");
            String filePath = scanner.nextLine().trim();
            
            vault.exportAllData(format, filePath);
            System.out.println(currentTheme.getSuccessSymbol() + " Export completed successfully!");
            
        } catch (Exception e) {
            System.out.println(currentTheme.getErrorSymbol() + " Export failed: " + e.getMessage());
        }
        
        pause();
    }
    
    private void importData() {
        System.out.println("Import data functionality - with file selection and format detection");
        pause();
    }
    
    private void autoLockSettings() {
        System.out.println("Auto-lock settings functionality");
        pause();
    }
    
    private void viewSystemInfo() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("SYSTEM INFORMATION"));
        System.out.println();
        
        System.out.println(vault.getSettings().getDisplayString());
        System.out.println();
        
        // Display statistics
        System.out.println("Data Statistics:");
        System.out.println("Passwords: " + vault.getPasswords().size());
        System.out.println("Notes: " + vault.getNotes().size());
        System.out.println("Checklist Items: " + vault.getChecklists().size());
        System.out.println("Events: " + vault.getEvents().size());
        System.out.println("Clipboard Items: " + vault.getClipboardHistory().size());
        
        pause();
    }
    
    private void wipeVault() {
        clearScreen();
        System.out.println(currentTheme.formatTitle("WIPE VAULT - DANGER"));
        System.out.println();
        
        System.out.println(currentTheme.getWarningSymbol() + " WARNING: This will delete ALL vault data!");
        System.out.println("This action cannot be undone.");
        System.out.println();
        System.out.print("Type 'WIPE VAULT' to confirm: ");
        
        String confirmation = scanner.nextLine().trim();
        
        if ("WIPE VAULT".equals(confirmation)) {
            vault.getSettings().wipeVault();
            System.out.println(currentTheme.getSuccessSymbol() + " Vault wiped successfully.");
            System.out.println("Application will now exit.");
            running = false;
        } else {
            System.out.println("Wipe cancelled.");
        }
        
        pause();
    }
    
    // Utility methods
    private void clearScreen() {
        // Simple clear screen simulation
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }
    
    private void pause() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void shutdown() {
        vault.lock();
        scanner.close();
        ExceptionLogger.getInstance().shutdown();
    }
    
    // Main method
    public static void main(String[] args) {
        VaultConsole console = new VaultConsole();
        console.start();
    }
}