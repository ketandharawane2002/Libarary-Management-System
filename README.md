# Libarary-Management-System With JDBC
This Library Management System is a Java-based application that uses JDBC for database connectivity. The system helps manage library operations such as adding new books, registering users, borrowing books, and returning borrowed books. It ensures database consistency through transaction handling. A comprehensive Library Management System developed using Java and JDBC for database connectivity with MySQL. This system provides full functionality for managing books, users, and transactions in a library environment.

📋 Features Book Management ✅ Add new books to the library ✅ View all books with availability status ✅ Search books by title or author ✅ Update book information ✅ Delete books (with validation) ✅ Track total and available copies User Management ✅ Add new users with different membership types (Student, Faculty, Public) ✅ View all users with their details ✅ Search users by name ✅ Update user information ✅ Suspend/Activate user accounts ✅ Different borrowing limits for different membership types Transaction Management ✅ Borrow books with automatic due date calculation ✅ Return books with automatic fine calculation ✅ View user's active borrowings ✅ View complete transaction history ✅ Track overdue books ✅ Automatic fine calculation (Rs. 5 per day) Reports ✅ View all overdue books ✅ View available books ✅ View active users ✅ User-wise fine reports 🛠️ Technology Stack Language: Java 8+ Database: MySQL 8.0+ JDBC Driver: MySQL Connector/J 8.2.0 IDE: Eclipse IDE 📁 Project Structure 🚀 Setup Instructions Prerequisites Java Development Kit (JDK) 8 or higher

Download from: https://www.oracle.com/java/technologies/downloads/ Verify installation: java -version MySQL Server 8.0 or higher

Download from: https://dev.mysql.com/downloads/mysql/ Verify installation: mysql --version Eclipse IDE for Java Developers

Download from: https://www.eclipse.org/downloads/ Step 1: Download MySQL JDBC Driver Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/ Extract the downloaded file Copy mysql-connector-j-8.2.0.jar (or latest version) to the lib/ folder in your project directory Direct Download Link:

https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j-8.2.0.zip Step 2: Setup MySQL Database Start MySQL Server

Windows (PowerShell/CMD)
net start MySQL80

Or use MySQL Workbench to start the server
Create Database

Login to MySQL
mysql -u root -p

Enter your MySQL password when prompted
Run the Schema Script

source d:/Java Programming/codveda/level 3/task 4/database/schema.sql

Or copy and paste the contents of schema.sql in MySQL Workbench
Alternatively, you can use MySQL Workbench:

Open MySQL Workbench Connect to your local MySQL server File → Open SQL Script → Select database/schema.sql Execute the script (⚡ icon or Ctrl+Shift+Enter) Step 3: Configure Database Connection Open src/com/library/util/DatabaseConnection.java Update the database credentials if needed: private static final String USERNAME = "root"; private static final String PASSWORD = "your_mysql_password"; Step 4: Import Project into Eclipse Open Eclipse IDE

Import the Project

File → Import → Existing Projects into Workspace Click "Next" Browse to: d:\Java Programming\codveda\level 3\task 4 Select the project Click "Finish" Add MySQL JDBC Driver to Build Path

Right-click on the project → Properties Select "Java Build Path" Click on "Libraries" tab Click "Add External JARs..." Navigate to lib/ folder and select mysql-connector-j-8.2.0.jar Click "Apply and Close" Note: The .classpath file already references this JAR, but you need to ensure it exists in the lib/ folder

Step 5: Run the Application Navigate to Main Class

In Eclipse Package Explorer, expand: src → com.library Find LibraryManagementApp.java Run the Application

Right-click on LibraryManagementApp.java Select "Run As" → "Java Application" If you see connection errors:

Verify MySQL service is running Check database credentials in DatabaseConnection.java Ensure the library_management database exists Verify MySQL JDBC driver is in the build path 📊 Database Schema Tables

books book_id (INT, Primary Key, Auto Increment) title (VARCHAR) author (VARCHAR) isbn (VARCHAR, Unique) publisher (VARCHAR) publication_year (INT) category (VARCHAR) total_copies (INT) available_copies (INT) created_at, updated_at (TIMESTAMP)
users user_id (INT, Primary Key, Auto Increment) name (VARCHAR) email (VARCHAR, Unique) phone (VARCHAR) address (VARCHAR) membership_type (ENUM: STUDENT, FACULTY, PUBLIC) membership_date (DATE) status (ENUM: ACTIVE, SUSPENDED, INACTIVE) created_at, updated_at (TIMESTAMP)
transactions transaction_id (INT, Primary Key, Auto Increment) book_id (INT, Foreign Key → books) user_id (INT, Foreign Key → users) borrow_date (DATE) due_date (DATE) return_date (DATE, nullable) fine_amount (DECIMAL) status (ENUM: BORROWED, RETURNED, OVERDUE) created_at, updated_at (TIMESTAMP) 💡 Business Rules Borrowing Limits Students: Maximum 5 books simultaneously Faculty: Maximum 10 books simultaneously Public: Maximum 3 books simultaneously Borrowing Periods Students: 14 days Faculty: 30 days Public: 7 days Fine Calculation Late Fee: Rs. 5.00 per day for overdue books Fine is automatically calculated when returning books Validation Rules Users must be ACTIVE to borrow books Books must have available copies to be borrowed Users cannot borrow the same book twice simultaneously Books with active borrowings cannot be deleted 🎯 Usage Examples Adding a Book Select "1. Book Management" from main menu Select "1. Add New Book" Enter book details (title, author, ISBN, etc.) Book is added with a unique ID Borrowing a Book Select "3. Transaction Management" Select "1. Borrow Book" Enter User ID and Book ID System validates and creates transaction with due date Returning a Book Select "3. Transaction Management" Select "2. Return Book" Enter Transaction ID System calculates fine (if overdue) and updates availability Viewing Overdue Books Select "4. View Reports" Select "1. View Overdue Books" System displays all overdue transactions with fine amounts 🐛 Troubleshooting Error: "package java.sql is not accessible" Solution:
Ensure you're using JDK (not JRE) In Eclipse: Window → Preferences → Java → Installed JREs → Make sure a JDK is selected Add JDK to build path if needed Error: "MySQL JDBC Driver not found" Solution:

Download MySQL Connector/J JAR file Place it in the lib/ folder Add to Eclipse build path: Right-click project → Properties → Java Build Path → Libraries → Add JARs Error: "Cannot connect to database" Solution:

Verify MySQL service is running: net start MySQL80 Check credentials in DatabaseConnection.java Ensure database library_management exists Test connection: mysql -u root -p Error: "Communications link failure" Solution:

Check if MySQL is running on port 3306 Verify firewall settings Update connection URL if using different port 📝 Sample Data The schema includes sample data:

5 Books: Fiction and Technology categories 3 Users: One of each membership type (Student, Faculty, Public) Ready to test all features immediately after setup 🔒 Security Notes ⚠️ Important: This is an educational project. In production:

Never hardcode database credentials Use environment variables or configuration files Implement proper authentication and authorization Use prepared statements (already implemented) to prevent SQL injection Add input validation and sanitization 📚 Technologies & Concepts Demonstrated ✅ JDBC Connection Management ✅ CRUD Operations with PreparedStatements ✅ DAO (Data Access Object) Pattern ✅ Service Layer Architecture ✅ Entity/Model Classes ✅ Transaction Management ✅ Database Indexing ✅ Foreign Key Relationships ✅ Business Logic Implementation ✅ Exception Handling ✅ Console-based User Interface
