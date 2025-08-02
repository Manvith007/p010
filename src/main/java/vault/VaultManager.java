package vault;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * VaultManager - Singleton pattern implementation
 * Controls all modules, file I/O, security, export/import
 */
public class VaultManager {
    private static VaultManager instance;
    private static final String DATA_DIR = "vault_data";
    private static final String PASSWORDS_FILE = "passwords.dat";
    private static final String NOTES_FILE = "notes.dat";
    private static final String CHECKLISTS_FILE = "checklists.dat";
    private static final String EVENTS_FILE = "events.dat";
    private static final String SETTINGS_FILE = "settings.dat";
    
    private Map<String, PasswordEntry> passwords;
    private Map<String, Note> notes;
    private Map<String, ChecklistItem> checklists;
    private Map<String, Event> events;
    private VaultSettings settings;
    private ClipboardHistory clipboardHistory;
    private boolean isLocked;
    private String currentPin;
    
    private VaultManager() {
        this.passwords = new ConcurrentHashMap<>();
        this.notes = new ConcurrentHashMap<>();
        this.checklists = new ConcurrentHashMap<>();
        this.events = new ConcurrentHashMap<>();
        this.clipboardHistory = new ClipboardHistory();
        this.settings = new VaultSettings();
        this.isLocked = true;
        initializeDataDirectory();
    }
    
    public static synchronized VaultManager getInstance() {
        if (instance == null) {
            instance = new VaultManager();
        }
        return instance;
    }
    
    private void initializeDataDirectory() {
        try {
            Path dataPath = Paths.get(DATA_DIR);
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
        } catch (IOException e) {
            ExceptionLogger.log("Failed to initialize data directory", e);
        }
    }
    
    public boolean authenticate(String pin) {
        if (settings.validatePin(pin)) {
            this.currentPin = pin;
            this.isLocked = false;
            loadAllData();
            return true;
        }
        return false;
    }
    
    public void lock() {
        this.isLocked = true;
        this.currentPin = null;
        saveAllData();
    }
    
    public boolean isLocked() {
        return isLocked;
    }
    
    // Password Management
    public void addPassword(String site, String username, String password, String note) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        String id = generateId();
        PasswordEntry entry = new PasswordEntry(site, username, password, note);
        passwords.put(id, entry);
        savePasswords();
    }
    
    public List<PasswordEntry> searchPasswords(String query) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        return passwords.values().stream()
                .filter(p -> p.getSite().toLowerCase().contains(query.toLowerCase()) ||
                           p.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public void updatePassword(String id, String site, String username, String password, String note) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        if (passwords.containsKey(id)) {
            PasswordEntry entry = new PasswordEntry(site, username, password, note);
            passwords.put(id, entry);
            savePasswords();
        }
    }
    
    public void deletePassword(String id) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        passwords.remove(id);
        savePasswords();
    }
    
    // Note Management
    public void addNote(String title, String content, Set<String> tags) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        String id = generateId();
        Note note = new Note(title, content, tags);
        notes.put(id, note);
        saveNotes();
    }
    
    public List<Note> searchNotes(String query) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        return notes.values().stream()
                .filter(n -> n.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                           n.getContent().toLowerCase().contains(query.toLowerCase()) ||
                           n.getTags().stream().anyMatch(tag -> tag.toLowerCase().contains(query.toLowerCase())))
                .collect(Collectors.toList());
    }
    
    public void deleteNote(String id) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        notes.remove(id);
        saveNotes();
    }
    
    // Checklist Management
    public void addChecklist(String title, Set<String> tags) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        String id = generateId();
        ChecklistItem checklist = new ChecklistItem(title, tags);
        checklists.put(id, checklist);
        saveChecklists();
    }
    
    public void markChecklistDone(String id, boolean done) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        ChecklistItem item = checklists.get(id);
        if (item != null) {
            item.setDone(done);
            saveChecklists();
        }
    }
    
    public List<ChecklistItem> getChecklistsByStatus(boolean done) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        return checklists.values().stream()
                .filter(c -> c.isDone() == done)
                .collect(Collectors.toList());
    }
    
    // Event Management
    public void addEvent(String description, LocalDateTime start, LocalDateTime end, 
                        boolean recurring, Set<String> tags, boolean reminderSet) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        String id = generateId();
        Event event = new Event(description, start, end, recurring, tags, reminderSet);
        events.put(id, event);
        saveEvents();
    }
    
    public List<Event> getUpcomingEvents(int days) {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);
        
        return events.values().stream()
                .filter(e -> e.getStart().isAfter(now) && e.getStart().isBefore(future))
                .sorted((e1, e2) -> e1.getStart().compareTo(e2.getStart()))
                .collect(Collectors.toList());
    }
    
    // Clipboard Management
    public void addToClipboard(String content) {
        clipboardHistory.add(content);
    }
    
    public List<String> getClipboardHistory() {
        return clipboardHistory.getHistory();
    }
    
    // Settings Management
    public VaultSettings getSettings() {
        return settings;
    }
    
    public void updateSettings(String newPin, Theme theme) {
        settings.setPin(newPin);
        settings.setTheme(theme);
        saveSettings();
    }
    
    // Export/Import functionality
    public void exportAllData(String format, String filePath) throws IOException {
        if (isLocked) throw new IllegalStateException("Vault is locked");
        
        VaultExporter exporter = new VaultExporter();
        switch (format.toLowerCase()) {
            case "csv":
                exporter.exportToCSV(this, filePath);
                break;
            case "json":
                exporter.exportToJSON(this, filePath);
                break;
            case "txt":
                exporter.exportToText(this, filePath);
                break;
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }
    
    // Data persistence methods
    private void loadAllData() {
        loadPasswords();
        loadNotes();
        loadChecklists();
        loadEvents();
        loadSettings();
    }
    
    private void saveAllData() {
        savePasswords();
        saveNotes();
        saveChecklists();
        saveEvents();
        saveSettings();
    }
    
    private void loadPasswords() {
        try {
            Path file = Paths.get(DATA_DIR, PASSWORDS_FILE);
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        PasswordEntry entry = PasswordEntry.fromString(line, currentPin);
                        passwords.put(generateId(), entry);
                    }
                }
            }
        } catch (IOException e) {
            ExceptionLogger.log("Failed to load passwords", e);
        }
    }
    
    private void savePasswords() {
        try {
            Path file = Paths.get(DATA_DIR, PASSWORDS_FILE);
            List<String> lines = passwords.values().stream()
                    .map(p -> p.toString(currentPin))
                    .collect(Collectors.toList());
            Files.write(file, lines);
        } catch (IOException e) {
            ExceptionLogger.log("Failed to save passwords", e);
        }
    }
    
    private void loadNotes() {
        try {
            Path file = Paths.get(DATA_DIR, NOTES_FILE);
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        Note note = Note.fromString(line);
                        notes.put(generateId(), note);
                    }
                }
            }
        } catch (IOException e) {
            ExceptionLogger.log("Failed to load notes", e);
        }
    }
    
    private void saveNotes() {
        try {
            Path file = Paths.get(DATA_DIR, NOTES_FILE);
            List<String> lines = notes.values().stream()
                    .map(Note::toString)
                    .collect(Collectors.toList());
            Files.write(file, lines);
        } catch (IOException e) {
            ExceptionLogger.log("Failed to save notes", e);
        }
    }
    
    private void loadChecklists() {
        try {
            Path file = Paths.get(DATA_DIR, CHECKLISTS_FILE);
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        ChecklistItem item = ChecklistItem.fromString(line);
                        checklists.put(generateId(), item);
                    }
                }
            }
        } catch (IOException e) {
            ExceptionLogger.log("Failed to load checklists", e);
        }
    }
    
    private void saveChecklists() {
        try {
            Path file = Paths.get(DATA_DIR, CHECKLISTS_FILE);
            List<String> lines = checklists.values().stream()
                    .map(ChecklistItem::toString)
                    .collect(Collectors.toList());
            Files.write(file, lines);
        } catch (IOException e) {
            ExceptionLogger.log("Failed to save checklists", e);
        }
    }
    
    private void loadEvents() {
        try {
            Path file = Paths.get(DATA_DIR, EVENTS_FILE);
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        Event event = Event.fromString(line);
                        events.put(generateId(), event);
                    }
                }
            }
        } catch (IOException e) {
            ExceptionLogger.log("Failed to load events", e);
        }
    }
    
    private void saveEvents() {
        try {
            Path file = Paths.get(DATA_DIR, EVENTS_FILE);
            List<String> lines = events.values().stream()
                    .map(Event::toString)
                    .collect(Collectors.toList());
            Files.write(file, lines);
        } catch (IOException e) {
            ExceptionLogger.log("Failed to save events", e);
        }
    }
    
    private void loadSettings() {
        try {
            Path file = Paths.get(DATA_DIR, SETTINGS_FILE);
            if (Files.exists(file)) {
                List<String> lines = Files.readAllLines(file);
                if (!lines.isEmpty()) {
                    settings = VaultSettings.fromString(lines.get(0));
                }
            }
        } catch (IOException e) {
            ExceptionLogger.log("Failed to load settings", e);
        }
    }
    
    private void saveSettings() {
        try {
            Path file = Paths.get(DATA_DIR, SETTINGS_FILE);
            Files.write(file, Collections.singletonList(settings.toString()));
        } catch (IOException e) {
            ExceptionLogger.log("Failed to save settings", e);
        }
    }
    
    private String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    // Getters for data access
    public Map<String, PasswordEntry> getPasswords() { return new HashMap<>(passwords); }
    public Map<String, Note> getNotes() { return new HashMap<>(notes); }
    public Map<String, ChecklistItem> getChecklists() { return new HashMap<>(checklists); }
    public Map<String, Event> getEvents() { return new HashMap<>(events); }
}