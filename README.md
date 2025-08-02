# Personal Digital Organizer & Secure Vault Console

A comprehensive, menu-driven console application built in Java for managing passwords, secure notes, checklists, events, and more. This project implements advanced OOP design patterns, secure data handling, and uses only standard Java packages.

## 🚀 Features

### 1. Password Manager
- **Secure Storage**: Passwords encrypted with transient fields
- **CRUD Operations**: Add, view, search, update, delete passwords
- **Password Generation**: Strong password generator with customizable options
- **Password Strength Analysis**: Real-time strength validation
- **Export Options**: CSV, JSON, TXT formats

### 2. Secure Notes & Documents
- **Rich Text Storage**: Support for long-form content with categories
- **Tagging System**: Organize notes with searchable tags
- **Pin/Unpin**: Priority marking for important notes
- **Search Functionality**: Full-text search across title and content
- **Attachment Support**: File path references for documents

### 3. Checklist/To-Do Tracker
- **Task Management**: Create, complete, and organize tasks
- **Due Date Tracking**: Set deadlines with overdue detection
- **Priority Levels**: LOW, MEDIUM, HIGH, URGENT priorities
- **Sorting Options**: Sort by date, priority, status, or title
- **Progress Tracking**: Visual completion indicators

### 4. Event Scheduler & Reminders
- **Event Management**: Schedule one-time and recurring events
- **Recurrence Patterns**: Daily, weekly, monthly, yearly repetition
- **Reminder System**: Configurable reminder notifications
- **Upcoming Events**: View events within specified timeframes
- **Event Search**: Find events by description or tags

### 5. Quick Clipboard/History
- **History Tracking**: Maintain clipboard history with configurable size
- **Stack/Deque Implementation**: LIFO access to recent items
- **Search & Restore**: Find and restore previous clipboard items
- **Auto-Copy**: Automatic clipboard integration for passwords

### 6. Vault Settings
- **PIN Management**: Secure PIN authentication with retry limits
- **Theme Support**: Multiple console color themes
- **Data Export**: Comprehensive export in multiple formats
- **Vault Wipe**: Secure deletion of all data
- **Logging System**: Comprehensive activity and error logging

## 🏗️ Architecture & Design Patterns

### Design Patterns Implemented
- **Singleton**: `VaultManager`, `ExceptionLogger`, `ValidatorChain`
- **Factory**: Password generation with different strategies
- **Decorator**: Note highlighting and urgent marking
- **Iterator**: Checklist sorting and filtering
- **Strategy**: Multiple sorting algorithms for checklists
- **Chain of Responsibility**: Input validation pipeline
- **Template Method**: Export functionality with different formats
- **Command**: Clipboard operations (future undo/redo support)

### SOLID Principles
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Extensible design for new features
- **Liskov Substitution**: Proper inheritance hierarchies
- **Interface Segregation**: Focused interfaces
- **Dependency Inversion**: Abstractions over concrete implementations

### Key Components
```
src/main/java/
├── Main.java                 # Application entry point
├── core/
│   └── VaultManager.java     # Singleton vault controller
├── domain/
│   ├── PasswordEntry.java    # Password entity with encryption
│   ├── Note.java            # Abstract base for all notes
│   ├── SecureNote.java      # Concrete note implementation
│   ├── ChecklistItem.java   # Task/checklist entity
│   └── Event.java           # Calendar event entity
├── util/
│   ├── ValidatorChain.java  # Input validation chain
│   └── ExceptionLogger.java # Logging system
└── console/
    └── ConsoleInterface.java # Main UI controller
```

## 🛡️ Security Features

### Data Protection
- **Encryption**: Password encryption using transient fields
- **PIN Authentication**: Secure vault access with retry limits
- **Session Management**: Automatic vault locking
- **Secure Deletion**: Memory clearing on vault lock
- **Audit Logging**: Security event tracking

### Input Validation
- **PIN Validation**: Format and strength checking
- **File Path Validation**: Secure file operations
- **Content Validation**: Length and format restrictions
- **SQL Injection Prevention**: Parameterized operations (future DB support)

## 📋 Usage

### Prerequisites
- Java 8 or higher
- Terminal/Command prompt with color support (optional)

### Running the Application
```bash
# Compile the application
javac -d bin src/main/java/**/*.java

# Run the application
java -cp bin Main
```

### First Time Setup
1. Launch the application
2. Set up your vault PIN (4-8 digits)
3. Start using the various modules

### Main Menu Navigation
```
========== MAIN MENU ==========
1. Password Manager
2. Secure Notes & Documents
3. Checklist/To-Do Tracker
4. Event Scheduler & Reminders
5. Quick Clipboard/History
6. Vault Settings
7. Exit
================================
```

## 📁 Data Storage

### File Structure
```
vault_data/
├── passwords.dat    # Encrypted password entries
├── notes.dat       # Secure notes and documents
├── checklists.dat  # Checklist items and tasks
├── events.dat      # Calendar events and reminders
├── clipboard.dat   # Clipboard history
└── settings.dat    # Vault configuration

logs/
└── vault_YYYY-MM-DD.log  # Daily log files
```

### Export Formats
- **CSV**: Comma-separated values for spreadsheet import
- **JSON**: Structured data for API integration
- **TXT**: Human-readable plain text format

## 🔧 Technical Implementation

### Java API Usage
- **Collections**: `List`, `Set`, `Map`, `Queue`, `Deque`, `Stack`
- **Streams**: Lambda expressions for filtering and sorting
- **Date/Time**: `LocalDate`, `LocalDateTime`, `Period`, `Duration`
- **I/O**: `File`, `Path`, `Files`, object serialization
- **Concurrency**: Thread-safe collections and locks
- **Generics**: Type-safe collections and methods
- **Exception Handling**: Comprehensive error management

### Advanced Features
- **Serialization**: Custom object persistence
- **Regex Patterns**: Input validation and parsing
- **Functional Interfaces**: Lambda expressions and method references
- **Stream Processing**: Data transformation and filtering
- **Comparators**: Multiple sorting strategies

## 🎨 Console Interface

### Color Coding
- 🔵 **Blue**: Password Manager
- 🟢 **Green**: Secure Notes
- 🟣 **Purple**: Checklist Tracker
- 🟡 **Yellow**: Event Scheduler
- 🔵 **Cyan**: Clipboard Manager
- 🔴 **Red**: Vault Settings & Warnings

### User Experience
- **Menu-driven Navigation**: Intuitive number-based selection
- **Input Validation**: Real-time feedback on user input
- **Progress Indicators**: Visual status updates
- **Error Handling**: User-friendly error messages
- **Confirmation Dialogs**: Safety prompts for destructive operations

## 🚧 Future Enhancements

### Planned Features
- **Database Integration**: SQLite support for larger datasets
- **Encryption Upgrades**: AES encryption for enhanced security
- **Import Functionality**: Support for importing from other password managers
- **Backup & Sync**: Cloud backup integration
- **Plugin System**: Extensible module architecture
- **GUI Version**: JavaFX-based graphical interface
- **Multi-user Support**: User profiles and permissions
- **Advanced Search**: Full-text indexing and search

### Technical Improvements
- **Performance Optimization**: Lazy loading and caching
- **Memory Management**: Improved garbage collection
- **Configuration System**: External configuration files
- **Internationalization**: Multi-language support
- **Unit Testing**: Comprehensive test coverage
- **Documentation**: JavaDoc API documentation

## 📝 License

This project is created for educational purposes, demonstrating advanced Java programming concepts, design patterns, and software engineering principles.

## 🤝 Contributing

This is an educational project showcasing Java development best practices. Feel free to study the code and adapt it for your own learning purposes.

---

**Note**: This application is designed for educational purposes and demonstrates various Java programming concepts. For production use, consider additional security measures and professional security auditing.