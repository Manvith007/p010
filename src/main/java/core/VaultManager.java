package core;

import domain.*;
import util.ExceptionLogger;
import util.ValidatorChain;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Singleton VaultManager - Controls all modules, file I/O, security, export/import
 * Implements all major design patterns and handles vault operations
 */
public class VaultManager {
    private static volatile VaultManager instance;
    private static final Object lock = new Object();
    
    // Core data storage
    private Map<String, PasswordEntry> passwords;
    private Map<String, SecureNote> notes;
    private Map<String, ChecklistItem> checklists;
    private Map<String, Event> events;
    private Deque<String> clipboardHistory;
    
    // Vault settings
    private String vaultPin;
    private boolean locked;
    private String theme;
    private Path vaultDirectory;
    private int maxClipboardSize;
    private int pinRetryCount;
    private static final int MAX_PIN_ATTEMPTS = 3;
    
    // File paths
    private static final String VAULT_DIR = "vault_data";
    private static final String PASSWORDS_FILE = "passwords.dat";
    private static final String NOTES_FILE = "notes.dat";
    private static final String CHECKLISTS_FILE = "checklists.dat";
    private static final String EVENTS_FILE = "events.dat";
    private static final String SETTINGS_FILE = "settings.dat";
    private static final String CLIPBOARD_FILE = "clipboard.dat";
    
    private VaultManager() {
        initializeVault();
    }
    
    // Singleton pattern with double-checked locking
    public static VaultManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new VaultManager();
                }
            }
        }
        return instance;
    }
    
    private void initializeVault() {
        try {
            // Initialize collections with thread-safe implementations
            passwords = new ConcurrentHashMap<>();
            notes = new ConcurrentHashMap<>();
            checklists = new ConcurrentHashMap<>();
            events = new ConcurrentHashMap<>();
            clipboardHistory = new ArrayDeque<>();
            
            // Default settings
            locked = true;
            theme = "default";
            maxClipboardSize = 50;
            pinRetryCount = 0;
            
            // Create vault directory
            vaultDirectory = Paths.get(VAULT_DIR);
            if (!Files.exists(vaultDirectory)) {
                Files.createDirectories(vaultDirectory);
            }
            
            // Load existing data
            loadVaultData();
            
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to initialize vault", e);
            throw new RuntimeException("Vault initialization failed", e);
        }
    }
    
    // PIN management with retry logic (Chain of Responsibility pattern)
    public boolean authenticatePin(String inputPin) {
        if (pinRetryCount >= MAX_PIN_ATTEMPTS) {
            ExceptionLogger.getInstance().logError("Max PIN attempts exceeded", null);
            return false;
        }
        
        if (vaultPin == null) {
            // First time setup
            if (ValidatorChain.getInstance().validatePin(inputPin)) {
                vaultPin = hashPin(inputPin);
                locked = false;
                pinRetryCount = 0;
                saveSettings();
                return true;
            }
        } else {
            // Existing PIN verification
            if (vaultPin.equals(hashPin(inputPin))) {
                locked = false;
                pinRetryCount = 0;
                return true;
            }
        }
        
        pinRetryCount++;
        return false;
    }
    
    public boolean changePin(String oldPin, String newPin) {
        if (locked || !vaultPin.equals(hashPin(oldPin))) {
            return false;
        }
        
        if (ValidatorChain.getInstance().validatePin(newPin)) {
            vaultPin = hashPin(newPin);
            saveSettings();
            return true;
        }
        
        return false;
    }
    
    private String hashPin(String pin) {
        // Simple hash for demonstration (in production, use BCrypt or similar)
        return String.valueOf(pin.hashCode() * 31 + 42);
    }
    
    public void lockVault() {
        locked = true;
        // Clear sensitive data from memory
        passwords.clear();
        notes.clear();
        checklists.clear();
        events.clear();
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    // Password Manager operations
    public void addPassword(String site, String username, String password, String note) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        String key = site + ":" + username;
        PasswordEntry entry = new PasswordEntry(site, username, password, note);
        passwords.put(key, entry);
        savePasswords();
    }
    
    public List<PasswordEntry> getAllPasswords() {
        if (locked) throw new IllegalStateException("Vault is locked");
        return new ArrayList<>(passwords.values());
    }
    
    public List<PasswordEntry> searchPasswords(String searchTerm) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        return passwords.values().stream()
                .filter(p -> p.getSite().toLowerCase().contains(searchTerm.toLowerCase()) ||
                           p.getUsername().toLowerCase().contains(searchTerm.toLowerCase()) ||
                           (p.getNote() != null && p.getNote().toLowerCase().contains(searchTerm.toLowerCase())))
                .collect(Collectors.toList());
    }
    
    public boolean updatePassword(String site, String username, String newPassword) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        String key = site + ":" + username;
        PasswordEntry entry = passwords.get(key);
        if (entry != null) {
            entry.updatePassword(newPassword);
            savePasswords();
            return true;
        }
        return false;
    }
    
    public boolean deletePassword(String site, String username) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        String key = site + ":" + username;
        boolean removed = passwords.remove(key) != null;
        if (removed) {
            savePasswords();
        }
        return removed;
    }
    
    // Secure Notes operations
    public void addNote(String title, String content, String category) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        SecureNote note = new SecureNote(title, content, category);
        notes.put(title, note);
        saveNotes();
    }
    
    public List<SecureNote> getAllNotes() {
        if (locked) throw new IllegalStateException("Vault is locked");
        return new ArrayList<>(notes.values());
    }
    
    public List<SecureNote> searchNotes(String searchTerm) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        return notes.values().stream()
                .filter(note -> note.containsText(searchTerm))
                .collect(Collectors.toList());
    }
    
    public List<SecureNote> getNotesByTag(String tag) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        return notes.values().stream()
                .filter(note -> note.hasTag(tag))
                .collect(Collectors.toList());
    }
    
    public boolean updateNote(String title, String newContent) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        SecureNote note = notes.get(title);
        if (note != null) {
            note.setContent(newContent);
            saveNotes();
            return true;
        }
        return false;
    }
    
    public boolean deleteNote(String title) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        boolean removed = notes.remove(title) != null;
        if (removed) {
            saveNotes();
        }
        return removed;
    }
    
    // Checklist operations
    public void addChecklistItem(String title, String description) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        ChecklistItem item = new ChecklistItem(title, description);
        checklists.put(title, item);
        saveChecklists();
    }
    
    public List<ChecklistItem> getAllChecklistItems() {
        if (locked) throw new IllegalStateException("Vault is locked");
        return new ArrayList<>(checklists.values());
    }
    
    public List<ChecklistItem> getOverdueItems() {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        return checklists.values().stream()
                .filter(ChecklistItem::isOverdue)
                .collect(Collectors.toList());
    }
    
    public boolean markItemDone(String title) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        ChecklistItem item = checklists.get(title);
        if (item != null) {
            item.markDone();
            saveChecklists();
            return true;
        }
        return false;
    }
    
    // Event operations
    public void addEvent(String description, LocalDateTime start, LocalDateTime end) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        Event event = new Event(description, start, end);
        events.put(description + ":" + start.toString(), event);
        saveEvents();
    }
    
    public List<Event> getAllEvents() {
        if (locked) throw new IllegalStateException("Vault is locked");
        return new ArrayList<>(events.values());
    }
    
    public List<Event> getUpcomingEvents(int hours) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        return events.values().stream()
                .filter(event -> event.isUpcoming(hours))
                .collect(Collectors.toList());
    }
    
    // Clipboard operations using Deque/Stack
    public void addToClipboard(String text) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        clipboardHistory.addFirst(text);
        
        // Limit clipboard size
        while (clipboardHistory.size() > maxClipboardSize) {
            clipboardHistory.removeLast();
        }
        
        saveClipboard();
    }
    
    public List<String> getClipboardHistory() {
        if (locked) throw new IllegalStateException("Vault is locked");
        return new ArrayList<>(clipboardHistory);
    }
    
    public String getLastClipboardItem() {
        if (locked) throw new IllegalStateException("Vault is locked");
        return clipboardHistory.isEmpty() ? null : clipboardHistory.peekFirst();
    }
    
    // Settings management
    public void setTheme(String theme) {
        this.theme = theme;
        saveSettings();
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setMaxClipboardSize(int size) {
        this.maxClipboardSize = Math.max(10, Math.min(size, 200));
        saveSettings();
    }
    
    // File I/O operations
    private void loadVaultData() {
        loadPasswords();
        loadNotes();
        loadChecklists();
        loadEvents();
        loadClipboard();
        loadSettings();
    }
    
    @SuppressWarnings("unchecked")
    private void loadPasswords() {
        try {
            Path file = vaultDirectory.resolve(PASSWORDS_FILE);
            if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    passwords = (Map<String, PasswordEntry>) ois.readObject();
                }
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load passwords", e);
            passwords = new ConcurrentHashMap<>();
        }
    }
    
    private void savePasswords() {
        try {
            Path file = vaultDirectory.resolve(PASSWORDS_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(passwords);
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to save passwords", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadNotes() {
        try {
            Path file = vaultDirectory.resolve(NOTES_FILE);
            if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    notes = (Map<String, SecureNote>) ois.readObject();
                }
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load notes", e);
            notes = new ConcurrentHashMap<>();
        }
    }
    
    private void saveNotes() {
        try {
            Path file = vaultDirectory.resolve(NOTES_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(notes);
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to save notes", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadChecklists() {
        try {
            Path file = vaultDirectory.resolve(CHECKLISTS_FILE);
            if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    checklists = (Map<String, ChecklistItem>) ois.readObject();
                }
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load checklists", e);
            checklists = new ConcurrentHashMap<>();
        }
    }
    
    private void saveChecklists() {
        try {
            Path file = vaultDirectory.resolve(CHECKLISTS_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(checklists);
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to save checklists", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadEvents() {
        try {
            Path file = vaultDirectory.resolve(EVENTS_FILE);
            if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    events = (Map<String, Event>) ois.readObject();
                }
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load events", e);
            events = new ConcurrentHashMap<>();
        }
    }
    
    private void saveEvents() {
        try {
            Path file = vaultDirectory.resolve(EVENTS_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(events);
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to save events", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadClipboard() {
        try {
            Path file = vaultDirectory.resolve(CLIPBOARD_FILE);
            if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    clipboardHistory = (Deque<String>) ois.readObject();
                }
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load clipboard", e);
            clipboardHistory = new ArrayDeque<>();
        }
    }
    
    private void saveClipboard() {
        try {
            Path file = vaultDirectory.resolve(CLIPBOARD_FILE);
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(clipboardHistory);
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to save clipboard", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadSettings() {
        try {
            Path file = vaultDirectory.resolve(SETTINGS_FILE);
            if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    Map<String, Object> settings = (Map<String, Object>) ois.readObject();
                    vaultPin = (String) settings.get("pin");
                    theme = (String) settings.getOrDefault("theme", "default");
                    maxClipboardSize = (Integer) settings.getOrDefault("clipboardSize", 50);
                }
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to load settings", e);
        }
    }
    
    private void saveSettings() {
        try {
            Path file = vaultDirectory.resolve(SETTINGS_FILE);
            Map<String, Object> settings = new HashMap<>();
            settings.put("pin", vaultPin);
            settings.put("theme", theme);
            settings.put("clipboardSize", maxClipboardSize);
            
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                oos.writeObject(settings);
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to save settings", e);
        }
    }
    
    // Export/Import functionality (Template Method pattern)
    public boolean exportVault(String exportPath, String format) {
        if (locked) throw new IllegalStateException("Vault is locked");
        
        try {
            switch (format.toLowerCase()) {
                case "csv":
                    return exportToCsv(exportPath);
                case "json":
                    return exportToJson(exportPath);
                case "txt":
                    return exportToText(exportPath);
                default:
                    return false;
            }
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Export failed", e);
            return false;
        }
    }
    
    private boolean exportToCsv(String exportPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(exportPath)))) {
            // Export passwords
            writer.println("Type,Site,Username,Password,Note,LastModified");
            for (PasswordEntry entry : passwords.values()) {
                writer.printf("Password,%s,%s,%s,%s,%s%n",
                    entry.getSite(),
                    entry.getUsername(),
                    entry.decryptPassword(),
                    entry.getNote(),
                    entry.getLastModified());
            }
            
            // Export notes
            writer.println("\nType,Title,Content,Category,Tags,Created");
            for (SecureNote note : notes.values()) {
                writer.printf("Note,%s,%s,%s,%s,%s%n",
                    note.getTitle(),
                    note.getContent().replace("\n", "\\n"),
                    note.getCategory(),
                    String.join(";", note.getTags()),
                    note.getCreated());
            }
            
            return true;
        }
    }
    
    private boolean exportToJson(String exportPath) throws IOException {
        // Simple JSON export without external libraries
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(exportPath)))) {
            writer.println("{");
            writer.println("  \"passwords\": [");
            
            List<PasswordEntry> passwordList = new ArrayList<>(passwords.values());
            for (int i = 0; i < passwordList.size(); i++) {
                PasswordEntry entry = passwordList.get(i);
                writer.printf("    {\"site\":\"%s\",\"username\":\"%s\",\"password\":\"%s\",\"note\":\"%s\"}%s%n",
                    entry.getSite(),
                    entry.getUsername(),
                    entry.decryptPassword(),
                    entry.getNote(),
                    i < passwordList.size() - 1 ? "," : "");
            }
            
            writer.println("  ],");
            writer.println("  \"notes\": [");
            
            List<SecureNote> noteList = new ArrayList<>(notes.values());
            for (int i = 0; i < noteList.size(); i++) {
                SecureNote note = noteList.get(i);
                writer.printf("    {\"title\":\"%s\",\"content\":\"%s\",\"category\":\"%s\"}%s%n",
                    note.getTitle(),
                    note.getContent().replace("\"", "\\\"").replace("\n", "\\n"),
                    note.getCategory(),
                    i < noteList.size() - 1 ? "," : "");
            }
            
            writer.println("  ]");
            writer.println("}");
            
            return true;
        }
    }
    
    private boolean exportToText(String exportPath) throws IOException {
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(exportPath)))) {
            writer.println("=== PERSONAL DIGITAL ORGANIZER EXPORT ===");
            writer.println("Export Date: " + LocalDateTime.now());
            writer.println();
            
            writer.println("=== PASSWORDS ===");
            for (PasswordEntry entry : passwords.values()) {
                writer.println("Site: " + entry.getSite());
                writer.println("Username: " + entry.getUsername());
                writer.println("Password: " + entry.decryptPassword());
                writer.println("Note: " + entry.getNote());
                writer.println("Last Modified: " + entry.getLastModified());
                writer.println("---");
            }
            
            writer.println("\n=== NOTES ===");
            for (SecureNote note : notes.values()) {
                writer.println("Title: " + note.getTitle());
                writer.println("Category: " + note.getCategory());
                writer.println("Content: " + note.getContent());
                writer.println("Tags: " + String.join(", ", note.getTags()));
                writer.println("Created: " + note.getCreated());
                writer.println("---");
            }
            
            return true;
        }
    }
    
    // Secure vault wipe
    public void wipeVault() {
        try {
            // Clear memory
            passwords.clear();
            notes.clear();
            checklists.clear();
            events.clear();
            clipboardHistory.clear();
            
            // Delete files
            Files.deleteIfExists(vaultDirectory.resolve(PASSWORDS_FILE));
            Files.deleteIfExists(vaultDirectory.resolve(NOTES_FILE));
            Files.deleteIfExists(vaultDirectory.resolve(CHECKLISTS_FILE));
            Files.deleteIfExists(vaultDirectory.resolve(EVENTS_FILE));
            Files.deleteIfExists(vaultDirectory.resolve(CLIPBOARD_FILE));
            Files.deleteIfExists(vaultDirectory.resolve(SETTINGS_FILE));
            
            // Reset settings
            vaultPin = null;
            locked = true;
            theme = "default";
            pinRetryCount = 0;
            
        } catch (Exception e) {
            ExceptionLogger.getInstance().logError("Failed to wipe vault", e);
        }
    }
}