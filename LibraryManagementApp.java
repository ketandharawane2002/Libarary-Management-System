package com.library;

import com.library.model.Book;
import com.library.model.Transaction;
import com.library.model.User;
import com.library.service.LibraryService;
import com.library.util.DatabaseConnection;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unused")
public class LibraryManagementApp {

    private static final LibraryService libraryService = new LibraryService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║   LIBRARY MANAGEMENT SYSTEM                       ║");
        System.out.println("║                                                   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();

        //  database connection
        if (!DatabaseConnection.testConnection()) {
            System.err.println("Failed to connect to database! Please check your configuration.");
            System.err.println("Make sure MySQL is running and database 'library_management' exists.");
            return;
        }

        System.out.println("✓ Database connection successful!\n");

        boolean running = true;

        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    bookManagementMenu();
                    break;
                case 2:
                    userManagementMenu();
                    break;
                case 3:
                    transactionManagementMenu();
                    break;
                case 4:
                    viewReportsMenu();
                    break;
                case 0:
                    running = false;
                    System.out.println("\n✓ Thank you for using Library Management System!");
                    break;
                default:
                    System.out.println("✗ Invalid choice! Please try again.\n");
            }
        }

        scanner.close();
    }

    // ==================== MAIN MENU ====================

    private static void displayMainMenu() {
        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.println("│              MAIN MENU                          │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  1. Book Management                             │");
        System.out.println("│  2. User Management                             │");
        System.out.println("│  3. Transaction Management                      │");
        System.out.println("│  4. View Reports                                │");
        System.out.println("│  0. Exit                                        │");
        System.out.println("└─────────────────────────────────────────────────┘");
    }

    // ==================== BOOK MANAGEMENT ====================

    private static void bookManagementMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n┌─────────────────────────────────────────────────┐");
            System.out.println("│          BOOK MANAGEMENT                        │");
            System.out.println("├─────────────────────────────────────────────────┤");
            System.out.println("│  1. Add New Book                                │");
            System.out.println("│  2. View All Books                              │");
            System.out.println("│  3. Search Book by Title                        │");
            System.out.println("│  4. Search Book by Author                       │");
            System.out.println("│  5. Update Book                                 │");
            System.out.println("│  6. Delete Book                                 │");
            System.out.println("│  0. Back to Main Menu                           │");
            System.out.println("└─────────────────────────────────────────────────┘");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addNewBook();
                    break;
                case 2:
                    viewAllBooks();
                    break;
                case 3:
                    searchBookByTitle();
                    break;
                case 4:
                    searchBookByAuthor();
                    break;
                case 5:
                    updateBook();
                    break;
                case 6:
                    deleteBook();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("✗ Invalid choice!\n");
            }
        }
    }

    private static void addNewBook() {
        System.out.println("\n--- Add New Book ---");

        String title = getStringInput("Enter title: ");
        String author = getStringInput("Enter author: ");
        String isbn = getStringInput("Enter ISBN: ");
        String publisher = getStringInput("Enter publisher: ");
        int year = getIntInput("Enter publication year: ");
        String category = getStringInput("Enter category: ");
        int totalCopies = getIntInput("Enter total copies: ");

        Book book = new Book(title, author, isbn, publisher, year, category, totalCopies, totalCopies);

        if (libraryService.addBook(book)) {
            System.out.println("✓ Book added successfully! (ID: " + book.getBookId() + ")\n");
        } else {
            System.out.println("✗ Failed to add book!\n");
        }
    }

    private static void viewAllBooks() {
        System.out.println("\n--- All Books ---");
        List<Book> books = libraryService.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books found!\n");
            return;
        }

        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-5s %-30s %-25s %-20s %-12s%n", "ID", "Title", "Author", "Category", "Available");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");

        for (Book book : books) {
            System.out.printf("%-5d %-30s %-25s %-20s %d/%d%n",
                    book.getBookId(),
                    truncate(book.getTitle(), 30),
                    truncate(book.getAuthor(), 25),
                    truncate(book.getCategory(), 20),
                    book.getAvailableCopies(),
                    book.getTotalCopies());
        }
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────\n");
    }

    private static void searchBookByTitle() {
        String title = getStringInput("\nEnter book title to search: ");
        List<Book> books = libraryService.searchBooksByTitle(title);

        if (books.isEmpty()) {
            System.out.println("✗ No books found!\n");
            return;
        }

        System.out.println("\nSearch Results:");
        displayBookList(books);
    }

    private static void searchBookByAuthor() {
        String author = getStringInput("\nEnter author name to search: ");
        List<Book> books = libraryService.searchBooksByAuthor(author);

        if (books.isEmpty()) {
            System.out.println("✗ No books found!\n");
            return;
        }

        System.out.println("\nSearch Results:");
        displayBookList(books);
    }

    private static void updateBook() {
        int bookId = getIntInput("\nEnter Book ID to update: ");
        Book book = libraryService.getBookById(bookId);

        if (book == null) {
            System.out.println("✗ Book not found!\n");
            return;
        }

        System.out.println("Current details: " + book);
        System.out.println("Enter new details (press Enter to keep current value):");

        String input = getStringInput("Title [" + book.getTitle() + "]: ");
        if (!input.isEmpty())
            book.setTitle(input);

        input = getStringInput("Author [" + book.getAuthor() + "]: ");
        if (!input.isEmpty())
            book.setAuthor(input);

        input = getStringInput("Publisher [" + book.getPublisher() + "]: ");
        if (!input.isEmpty())
            book.setPublisher(input);

        input = getStringInput("Category [" + book.getCategory() + "]: ");
        if (!input.isEmpty())
            book.setCategory(input);

        if (libraryService.updateBook(book)) {
            System.out.println("✓ Book updated successfully!\n");
        } else {
            System.out.println("✗ Failed to update book!\n");
        }
    }

    private static void deleteBook() {
        int bookId = getIntInput("\nEnter Book ID to delete: ");
        Book book = libraryService.getBookById(bookId);

        if (book == null) {
            System.out.println("✗ Book not found!\n");
            return;
        }

        System.out.println("Book: " + book);
        String confirm = getStringInput("Are you sure you want to delete this book? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            if (libraryService.deleteBook(bookId)) {
                System.out.println("✓ Book deleted successfully!\n");
            } else {
                System.out.println("✗ Failed to delete book!\n");
            }
        } else {
            System.out.println("✗ Delete operation cancelled!\n");
        }
    }

    // ==================== USER MANAGEMENT ====================

    private static void userManagementMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n┌─────────────────────────────────────────────────┐");
            System.out.println("│          USER MANAGEMENT                        │");
            System.out.println("├─────────────────────────────────────────────────┤");
            System.out.println("│  1. Add New User                                │");
            System.out.println("│  2. View All Users                              │");
            System.out.println("│  3. Search User by Name                         │");
            System.out.println("│  4. Update User                                 │");
            System.out.println("│  5. Suspend User                                │");
            System.out.println("│  6. Activate User                               │");
            System.out.println("│  0. Back to Main Menu                           │");
            System.out.println("└─────────────────────────────────────────────────┘");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    addNewUser();
                    break;
                case 2:
                    viewAllUsers();
                    break;
                case 3:
                    searchUserByName();
                    break;
                case 4:
                    updateUser();
                    break;
                case 5:
                    suspendUser();
                    break;
                case 6:
                    activateUser();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("✗ Invalid choice!\n");
            }
        }
    }

    private static void addNewUser() {
        System.out.println("\n--- Add New User ---");

        String name = getStringInput("Enter name: ");
        String email = getStringInput("Enter email: ");
        String phone = getStringInput("Enter phone: ");
        String address = getStringInput("Enter address: ");

        System.out.println("Membership Type: 1=STUDENT, 2=FACULTY, 3=PUBLIC");
        int typeChoice = getIntInput("Enter membership type: ");
        User.MembershipType membershipType;
        switch (typeChoice) {
            case 1:
                membershipType = User.MembershipType.STUDENT;
                break;
            case 2:
                membershipType = User.MembershipType.FACULTY;
                break;
            default:
                membershipType = User.MembershipType.PUBLIC;
                break;
        }

        LocalDate membershipDate = LocalDate.now();
        User.Status status = User.Status.ACTIVE;

        User user = new User(name, email, phone, address, membershipType, membershipDate, status);

        if (libraryService.addUser(user)) {
            System.out.println("✓ User added successfully! (ID: " + user.getUserId() + ")\n");
        } else {
            System.out.println("✗ Failed to add user!\n");
        }
    }

    private static void viewAllUsers() {
        System.out.println("\n--- All Users ---");
        List<User> users = libraryService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found!\n");
            return;
        }

        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-5s %-25s %-25s %-15s %-10s%n", "ID", "Name", "Email", "Type", "Status");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");

        for (User user : users) {
            System.out.printf("%-5d %-25s %-25s %-15s %-10s%n",
                    user.getUserId(),
                    truncate(user.getName(), 25),
                    truncate(user.getEmail(), 25),
                    user.getMembershipType(),
                    user.getStatus());
        }
        System.out.println("─────────────────────────────────────────────────────────────────────────────────\n");
    }

    private static void searchUserByName() {
        String name = getStringInput("\nEnter user name to search: ");
        List<User> users = libraryService.searchUsersByName(name);

        if (users.isEmpty()) {
            System.out.println("✗ No users found!\n");
            return;
        }

        System.out.println("\nSearch Results:");
        for (User user : users) {
            System.out.println(user);
        }
        System.out.println();
    }

    private static void updateUser() {
        int userId = getIntInput("\nEnter User ID to update: ");
        User user = libraryService.getUserById(userId);

        if (user == null) {
            System.out.println("✗ User not found!\n");
            return;
        }

        System.out.println("Current details: " + user);
        System.out.println("Enter new details (press Enter to keep current value):");

        String input = getStringInput("Name [" + user.getName() + "]: ");
        if (!input.isEmpty())
            user.setName(input);

        input = getStringInput("Email [" + user.getEmail() + "]: ");
        if (!input.isEmpty())
            user.setEmail(input);

        input = getStringInput("Phone [" + user.getPhone() + "]: ");
        if (!input.isEmpty())
            user.setPhone(input);

        input = getStringInput("Address [" + user.getAddress() + "]: ");
        if (!input.isEmpty())
            user.setAddress(input);

        if (libraryService.updateUser(user)) {
            System.out.println("✓ User updated successfully!\n");
        } else {
            System.out.println("✗ Failed to update user!\n");
        }
    }

    private static void suspendUser() {
        int userId = getIntInput("\nEnter User ID to suspend: ");
        if (libraryService.suspendUser(userId)) {
            System.out.println("✓ User suspended successfully!\n");
        } else {
            System.out.println("✗ Failed to suspend user!\n");
        }
    }

    private static void activateUser() {
        int userId = getIntInput("\nEnter User ID to activate: ");
        if (libraryService.activateUser(userId)) {
            System.out.println("✓ User activated successfully!\n");
        } else {
            System.out.println("✗ Failed to activate user!\n");
        }
    }

    // ==================== TRANSACTION MANAGEMENT ====================

    private static void transactionManagementMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n┌─────────────────────────────────────────────────┐");
            System.out.println("│       TRANSACTION MANAGEMENT                    │");
            System.out.println("├─────────────────────────────────────────────────┤");
            System.out.println("│  1. Borrow Book                                 │");
            System.out.println("│  2. Return Book                                 │");
            System.out.println("│  3. View User's Active Borrowings              │");
            System.out.println("│  4. View User's Transaction History            │");
            System.out.println("│  5. View All Transactions                       │");
            System.out.println("│  0. Back to Main Menu                           │");
            System.out.println("└─────────────────────────────────────────────────┘");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    borrowBook();
                    break;
                case 2:
                    returnBook();
                    break;
                case 3:
                    viewUserActiveBorrowings();
                    break;
                case 4:
                    viewUserTransactionHistory();
                    break;
                case 5:
                    viewAllTransactions();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("✗ Invalid choice!\n");
            }
        }
    }

    private static void borrowBook() {
        System.out.println("\n--- Borrow Book ---");
        int userId = getIntInput("Enter User ID: ");
        int bookId = getIntInput("Enter Book ID: ");

        if (libraryService.borrowBook(userId, bookId)) {
            System.out.println("✓ Book borrowed successfully!\n");
        } else {
            System.out.println("✗ Failed to borrow book!\n");
        }
    }

    private static void returnBook() {
        System.out.println("\n--- Return Book ---");
        int transactionId = getIntInput("Enter Transaction ID: ");

        if (libraryService.returnBook(transactionId)) {
            System.out.println("✓ Book returned successfully!\n");
        } else {
            System.out.println("✗ Failed to return book!\n");
        }
    }

    private static void viewUserActiveBorrowings() {
        int userId = getIntInput("\nEnter User ID: ");
        List<Transaction> transactions = libraryService.getActiveBorrowings(userId);

        if (transactions.isEmpty()) {
            System.out.println("✗ No active borrowings found!\n");
            return;
        }

        System.out.println("\n--- Active Borrowings ---");
        displayTransactionList(transactions);
    }

    private static void viewUserTransactionHistory() {
        int userId = getIntInput("\nEnter User ID: ");
        List<Transaction> transactions = libraryService.getUserTransactions(userId);

        if (transactions.isEmpty()) {
            System.out.println("✗ No transactions found!\n");
            return;
        }

        System.out.println("\n--- Transaction History ---");
        displayTransactionList(transactions);

        double totalFines = libraryService.getUserTotalFines(userId);
        System.out.println("Total Fines: Rs. " + totalFines + "\n");
    }

    private static void viewAllTransactions() {
        System.out.println("\n--- All Transactions ---");
        List<Transaction> transactions = libraryService.getAllTransactions();

        if (transactions.isEmpty()) {
            System.out.println("No transactions found!\n");
            return;
        }

        displayTransactionList(transactions);
    }

    // ==================== REPORTS ====================

    private static void viewReportsMenu() {
        boolean back = false;

        while (!back) {
            System.out.println("\n┌─────────────────────────────────────────────────┐");
            System.out.println("│            REPORTS                              │");
            System.out.println("├─────────────────────────────────────────────────┤");
            System.out.println("│  1. View Overdue Books                          │");
            System.out.println("│  2. View Available Books                        │");
            System.out.println("│  3. View Active Users                           │");
            System.out.println("│  0. Back to Main Menu                           │");
            System.out.println("└─────────────────────────────────────────────────┘");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    viewOverdueBooks();
                    break;
                case 2:
                    viewAvailableBooks();
                    break;
                case 3:
                    viewActiveUsers();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    System.out.println("✗ Invalid choice!\n");
            }
        }
    }

    private static void viewOverdueBooks() {
        System.out.println("\n--- Overdue Books ---");
        List<Transaction> transactions = libraryService.getOverdueTransactions();

        if (transactions.isEmpty()) {
            System.out.println("No overdue books!\n");
            return;
        }

        displayTransactionList(transactions);
    }

    private static void viewAvailableBooks() {
        System.out.println("\n--- Available Books ---");
        List<Book> books = libraryService.getAllBooks();

        List<Book> availableBooks = new java.util.ArrayList<>();
        for (Book book : books) {
            if (book.getAvailableCopies() > 0) {
                availableBooks.add(book);
            }
        }

        if (availableBooks.isEmpty()) {
            System.out.println("No available books!\n");
            return;
        }

        displayBookList(availableBooks);
    }

    private static void viewActiveUsers() {
        System.out.println("\n--- Active Users ---");
        List<User> users = libraryService.getAllUsers();

        List<User> activeUsers = new java.util.ArrayList<>();
        for (User user : users) {
            if (user.getStatus() == User.Status.ACTIVE) {
                activeUsers.add(user);
            }
        }

        if (activeUsers.isEmpty()) {
            System.out.println("No active users!\n");
            return;
        }

        System.out.println("─────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-5s %-25s %-25s %-15s%n", "ID", "Name", "Email", "Type");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────");

        for (User user : activeUsers) {
            System.out.printf("%-5d %-25s %-25s %-15s%n",
                    user.getUserId(),
                    truncate(user.getName(), 25),
                    truncate(user.getEmail(), 25),
                    user.getMembershipType());
        }
        System.out.println("─────────────────────────────────────────────────────────────────────────────────\n");
    }

    // ==================== UTILITY METHODS ====================

    private static void displayBookList(List<Book> books) {
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-5s %-30s %-25s %-20s %-12s%n", "ID", "Title", "Author", "Category", "Available");
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────");

        for (Book book : books) {
            System.out.printf("%-5d %-30s %-25s %-20s %d/%d%n",
                    book.getBookId(),
                    truncate(book.getTitle(), 30),
                    truncate(book.getAuthor(), 25),
                    truncate(book.getCategory(), 20),
                    book.getAvailableCopies(),
                    book.getTotalCopies());
        }
        System.out.println("─────────────────────────────────────────────────────────────────────────────────────\n");
    }

    private static void displayTransactionList(List<Transaction> transactions) {
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("%-5s %-25s %-20s %-12s %-12s %-12s %-10s%n",
                "ID", "Book", "User", "Borrow", "Due", "Return", "Fine");
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────────────────────────");

        for (Transaction t : transactions) {
            System.out.printf("%-5d %-25s %-20s %-12s %-12s %-12s %.2f%n",
                    t.getTransactionId(),
                    truncate(t.getBookTitle(), 25),
                    truncate(t.getUserName(), 20),
                    t.getBorrowDate(),
                    t.getDueDate(),
                    t.getReturnDate() != null ? t.getReturnDate().toString() : "-",
                    t.getFineAmount());
        }
        System.out.println(
                "─────────────────────────────────────────────────────────────────────────────────────────────\n");
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("✗ Invalid input! Please enter a number.");
            }
        }
    }

    private static String truncate(String str, int maxLength) {
        if (str == null)
            return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}
