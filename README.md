# Personal Digital Organizer & Secure Vault

A comprehensive, menu-driven console-based Java application for managing passwords, secure notes, checklists, events, and clipboard history with advanced security features.

## Features

### 🔐 Password Manager
- Store, retrieve, update, and delete passwords securely
- Encrypted password storage using simple reversible logic
- Search functionality by site or username
- Strong password generation with customizable options
- Export passwords in multiple formats (CSV, JSON, Text)

### 📝 Secure Notes & Documents
- Create and manage text notes with tagging system
- Full-text search across notes and tags
- Pin/unpin important notes (Decorator pattern)
- File attachment simulation via file paths
- Rich display formatting with timestamps

### ✅ Checklist/To-Do Tracker
- Create and manage checklist items with priorities
- Due date tracking with overdue detection
- Tag-based organization and filtering
- Sort by priority, due date, or title
- Mark items as done/undone with status tracking

### 📅 Event Scheduler & Reminders
- Add one-time and recurring events
- Reminder system with customizable timing
- View upcoming events and conflict detection
- Support for multiple recurrence patterns
- Event tagging and search functionality

### 📋 Clipboard History
- Automatic content type detection (URL, file path, email, etc.)
- Search and restore clipboard items
- Statistics and usage tracking
- Configurable history size limits

### ⚙️ Vault Settings
- PIN-based authentication with lockout protection
- Multiple visual themes (Light, Dark, Colorful)
- Comprehensive export/import functionality
- Auto-lock settings and security options
- Complete vault wipe with confirmation

## Technical Architecture

### Design Patterns Implemented
- **Singleton Pattern**: VaultManager, ExceptionLogger
- **Template Method Pattern**: VaultExporter for different export formats
- **Decorator Pattern**: Note pinning functionality
- **Strategy Pattern**: ChecklistItem sorting strategies
- **Factory Pattern**: Password generation with different options
- **Iterator Pattern**: Event scheduling and reminders
- **Chain of Responsibility Pattern**: Input validation

### Key Technologies
- **Java 8+ Features**: Streams, Lambda expressions, LocalDateTime API
- **Collections Framework**: HashMap, TreeMap, Queue, Deque, Stack
- **File I/O**: NIO.2 for robust file operations
- **Serialization**: Custom serialization for data persistence
- **Exception Handling**: Comprehensive error logging and recovery
- **Multithreading**: Background logging and reminder processing

### Security Features
- PIN-based authentication with SHA-256 hashing
- Password encryption using XOR cipher with Base64 encoding
- Automatic lockout after failed attempts
- Secure random password generation
- Data validation and sanitization

## Installation & Usage

### Prerequisites
- Java 8 or higher
- Linux/Unix environment (for shell scripts)

### Quick Start

1. **Make scripts executable:**
   ```bash
   chmod +x build.sh run.sh
   ```

2. **Build the application:**
   ```bash
   ./build.sh
   ```

3. **Run the application:**
   ```bash
   ./run.sh
   ```

4. **Default PIN:** `1234`

### Manual Compilation
```bash
# Compile
javac -d build/classes -cp src/main/java src/main/java/vault/*.java

# Create JAR
cd build/classes && jar cfm ../vault.jar ../../manifest.txt vault/*.class

# Run
java -jar build/vault.jar
```

## Application Structure

```
src/main/java/vault/
├── VaultConsole.java          # Main console interface and menu system
├── VaultManager.java          # Core singleton managing all data
├── PasswordEntry.java         # Password data model with encryption
├── Note.java                  # Note data model with attachments
├── ChecklistItem.java         # Checklist data model with priorities
├── Event.java                 # Event data model with recurrence
├── VaultSettings.java         # Settings and security management
├── ClipboardHistory.java      # Clipboard management with type detection
├── VaultExporter.java         # Export functionality (Template Method)
├── PasswordGenerator.java     # Strong password generation utility
├── ExceptionLogger.java       # Comprehensive logging system
└── Theme.java                 # Visual theme enumeration
```

## Menu System

### Main Menu
1. **Password Manager** - Complete password management suite
2. **Secure Notes & Documents** - Note creation and management
3. **Checklist/To-Do Tracker** - Task and checklist management
4. **Event Scheduler & Reminders** - Calendar and event management
5. **Quick Clipboard/History** - Clipboard history management
6. **Vault Settings** - Security and application settings
7. **Exit** - Secure application shutdown

### Example Checklist Menu
```
========= CHECKLIST MENU =========
1. Add Checklist
2. Add Checklist Item
3. Mark Item as Done/Undone
4. Sort Items (by date, tag, priority)
5. Search Items (substring, regex)
6. Export Checklist
7. Import Checklist
8. Return to Main Menu
===================================
```

## Data Storage

- **Location**: `vault_data/` directory
- **Format**: Pipe-delimited text files with custom serialization
- **Files**:
  - `passwords.dat` - Encrypted password entries
  - `notes.dat` - Note data with metadata
  - `checklists.dat` - Checklist items with priorities
  - `events.dat` - Event data with recurrence
  - `settings.dat` - Application settings
  - `vault.log` - Application logs with rotation

## Export Formats

The application supports multiple export formats:

- **CSV**: Comma-separated values for spreadsheet import
- **JSON**: Structured data for web applications
- **XML**: Hierarchical data format
- **Text**: Human-readable plain text format
- **Serialized**: Binary Java object format

## Security Considerations

- Passwords are encrypted using XOR cipher with user PIN
- PIN is hashed using SHA-256 before storage
- Automatic account lockout after 3 failed attempts
- Secure random number generation for passwords
- File permissions should be restricted in production use

## Error Handling

- Comprehensive exception logging to file and console
- Graceful degradation when data files are corrupted
- Input validation with user-friendly error messages
- Automatic data backup before risky operations

## Performance Features

- Lazy loading of data collections
- Efficient search using Java 8 Streams
- Background logging to prevent UI blocking
- Memory-efficient clipboard history with size limits

## Extensibility

The application is designed for easy extension:

- New export formats via Template Method pattern
- Additional themes via Theme enumeration
- Custom validation rules via Chain of Responsibility
- New data types following existing model patterns

## Troubleshooting

### Common Issues

1. **Compilation Errors**: Ensure Java 8+ is installed
2. **Permission Denied**: Make shell scripts executable
3. **Data Corruption**: Check `vault_data/vault.log` for details
4. **Authentication Issues**: Default PIN is `1234`

### Log Files

Check `vault_data/vault.log` for detailed error information and application events.

## License

This project is provided as-is for educational and personal use.

## Contributing

This is a comprehensive demonstration of Java programming concepts including:
- Object-oriented design patterns
- File I/O and serialization
- Collections framework usage
- Exception handling
- Security implementation
- Console-based user interfaces

Feel free to extend and modify according to your needs!