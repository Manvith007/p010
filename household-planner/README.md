# Household Resource Planner & Inventory Management Console

A comprehensive console-based Java application for managing household resources, inventory, shopping lists, chores, bills, and user access control.

## Features

### 📦 Inventory Management
- Add, view, update, and delete inventory items with expiry tracking
- Categorize items (Food, Beverages, Cleaning, Personal Care, Medicine, Other)
- Track quantity, restock thresholds, and auto-detect low stock/expired items
- Support for different units (kg, pieces, liters, etc.)
- Auto-status updates (OK, LOW, EXPIRED)

### 🛒 Shopping Lists & Auto Suggestions
- Create multiple named shopping lists
- Auto-generate shopping lists from low stock and expired inventory
- Mark items as purchased with progress tracking
- Export shopping lists as text or CSV

### 🏠 Household Chores & Task Assignment
- Create and assign chores to family members
- Priority levels (LOW, MEDIUM, HIGH, URGENT)
- Due date tracking with overdue notifications
- Recurring chore support
- Chore rotation suggestions for fair distribution

### 💰 Bill Payments & Recurring Reminders
- Track utility bills (electricity, water, gas, internet, etc.)
- Recurring bill management with auto-cycling
- Overdue bill notifications and payment reminders
- Financial summaries and statistics

### 👥 Multi-User Access & Permissions
- Role-based access control (ADMIN, MEMBER, GUEST)
- Secure PIN-based authentication with encryption
- User activity tracking and session management
- Default admin account (username: `admin`, PIN: `1234`)

### 💾 Export/Import & Backup
- Complete data export/import in JSON format
- CSV export for inventory, shopping lists, and chores
- Automatic backup creation with restore functionality
- Backup cleanup and management

### 🔒 Security & Data Management
- PIN encryption using SHA-256 with salt
- Data validation with chain of responsibility pattern
- Comprehensive error logging and exception handling
- Secure data storage with serialization

## Project Structure

```
household-planner/
├── src/main/java/
│   ├── model/                     # Data model classes
│   │   ├── ItemStatus.java        # Enum for item status
│   │   ├── InventoryItem.java     # Inventory item model
│   │   ├── InventoryCategory.java # Category management
│   │   ├── ShoppingList.java      # Shopping list model
│   │   ├── ShoppingItem.java      # Shopping item model
│   │   ├── Chore.java            # Chore management model
│   │   ├── BillUtility.java      # Bill tracking model
│   │   ├── Appliance.java        # Appliance maintenance model
│   │   └── UserProfile.java      # User account model
│   ├── manager/                   # Singleton manager
│   │   └── ResourcePlannerManager.java
│   ├── service/                   # Business logic services
│   │   ├── InventoryService.java  # Inventory operations
│   │   ├── ShoppingService.java   # Shopping list operations
│   │   ├── ChoreService.java      # Chore management
│   │   ├── BillService.java       # Bill management
│   │   ├── UserService.java       # User management
│   │   └── ExportImportService.java # Data export/import
│   ├── util/                      # Utility classes
│   │   ├── ValidationChain.java   # Input validation
│   │   ├── EncryptionUtil.java    # Security utilities
│   │   ├── ExceptionLogger.java   # Error logging
│   │   └── FileOperations.java    # File I/O operations
│   └── ConsoleApp.java           # Main application entry point
├── data/                         # Data storage (auto-created)
├── backups/                      # Backup storage (auto-created)
└── README.md
```

## Getting Started

### Prerequisites
- Java 8 or higher
- Console/Terminal access

### Installation
1. Clone or download the project
2. Navigate to the project directory
3. Compile the Java files:
   ```bash
   cd household-planner/src/main/java
   javac -d ../../../target *.java model/*.java manager/*.java service/*.java util/*.java
   ```
4. Run the application:
   ```bash
   cd ../../../target
   java ConsoleApp
   ```

### First-Time Setup
1. The application creates a default admin user:
   - **Username:** `admin`
   - **PIN:** `1234`
   - ⚠️ **Important:** Change this PIN immediately after first login!

2. The application automatically creates necessary directories:
   - `data/` - For storing application data
   - `backups/` - For automatic backups

## User Guide

### Login
Use the default admin credentials or any user account you've created.

### Main Menu Options
1. **Inventory Management** - Manage household items and supplies
2. **Shopping Lists & Auto Suggestions** - Create and manage shopping lists
3. **Maintenance & Appliance Schedules** - Track appliance maintenance (coming soon)
4. **Bill Payments & Recurring Reminders** - Manage utility bills
5. **Household Chores & Task Assignment** - Assign and track chores
6. **Multi-User Access & Permissions** - Manage user accounts
7. **Export/Backup All Data** - Export and backup your data
8. **Import/Restore Data** - Import or restore from backups
9. **Vault Settings** - Security and system settings

### Quick Start Example

1. **Add Inventory Items:**
   - Go to Inventory Management → Add Inventory Item
   - Enter details like name, category, quantity, expiry date

2. **Create Shopping List:**
   - Go to Shopping Lists → Create Shopping List
   - Auto-generate from low stock items or manually add items

3. **Add Chores:**
   - Go to Household Chores → Add Chore
   - Assign to family members with due dates and priorities

4. **Track Bills:**
   - Go to Bill Payments → Add Bill
   - Set up recurring bills for automatic tracking

## Design Patterns Used

- **Singleton Pattern:** ResourcePlannerManager for centralized control
- **Chain of Responsibility:** ValidationChain for input validation
- **Strategy Pattern:** Different export formats (JSON, CSV)
- **Template Method:** Service base patterns for data operations
- **Factory Pattern:** User role and permission initialization

## Technical Features

- **Java 8+ Features:** Streams, lambdas, time API
- **Collections:** HashMap, ArrayList, HashSet for data storage
- **Serialization:** Object persistence to files
- **Security:** PIN hashing with SHA-256 and salt
- **Exception Handling:** Comprehensive error logging
- **File I/O:** Backup/restore with ZIP compression

## Data Storage

All data is stored locally in the `data/` directory using Java serialization:
- `inventory.dat` - Inventory items and categories
- `shopping_lists.dat` - Shopping lists and items
- `chores.dat` - Household chores
- `bills.dat` - Utility bills
- `users.dat` - User accounts and permissions

## Security Notes

- PINs are hashed using SHA-256 with random salt
- Default admin PIN should be changed immediately
- User sessions are managed securely
- Data is stored locally with no network transmission

## Contributing

This is a comprehensive household management system. Future enhancements could include:
- Appliance maintenance scheduling (partially implemented)
- Mobile app integration
- Cloud synchronization
- Advanced reporting and analytics
- Recipe management integration

## License

This project is designed for educational and personal use.

---

**Default Login Credentials:**
- Username: `admin`
- PIN: `1234`

**Remember to change the default PIN after first login!**