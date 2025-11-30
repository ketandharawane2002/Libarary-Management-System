package com.library.service;

import com.library.dao.BookDAO;
import com.library.dao.TransactionDAO;
import com.library.dao.UserDAO;
import com.library.model.Book;
import com.library.model.Transaction;
import com.library.model.User;

import java.time.LocalDate;
import java.util.List;

public class LibraryService {

    private final BookDAO bookDAO;
    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;

    private static final int MAX_BOOKS_STUDENT = 5;
    private static final int MAX_BOOKS_FACULTY = 10;
    private static final int MAX_BOOKS_PUBLIC = 3;

    private static final int BORROW_PERIOD_STUDENT = 14;
    private static final int BORROW_PERIOD_FACULTY = 30;
    private static final int BORROW_PERIOD_PUBLIC = 7;

    public LibraryService() {
        this.bookDAO = new BookDAO();
        this.userDAO = new UserDAO();
        this.transactionDAO = new TransactionDAO();
    }

    // ==================== Book Management ====================

    public boolean addBook(Book book) {
        if (book == null || book.getTitle() == null || book.getIsbn() == null) {
            System.err.println("Invalid book data!");
            return false;
        }

        Book existingBook = bookDAO.getBookByIsbn(book.getIsbn());
        if (existingBook != null) {
            System.err.println("Book with ISBN " + book.getIsbn() + " already exists!");
            return false;
        }

        return bookDAO.addBook(book);
    }

    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookDAO.searchBooksByTitle(title);
    }

    public List<Book> searchBooksByAuthor(String author) {
        return bookDAO.searchBooksByAuthor(author);
    }

    public Book getBookById(int bookId) {
        return bookDAO.getBookById(bookId);
    }

    public boolean updateBook(Book book) {
        if (book == null || book.getBookId() <= 0) {
            System.err.println("Invalid book data!");
            return false;
        }
        return bookDAO.updateBook(book);
    }

    public boolean deleteBook(int bookId) {
        List<Transaction> transactions = transactionDAO.getTransactionsByBookId(bookId);
        for (Transaction transaction : transactions) {
            if (transaction.getStatus() != Transaction.TransactionStatus.RETURNED) {
                System.err.println("Cannot delete book with active borrowings!");
                return false;
            }
        }
        return bookDAO.deleteBook(bookId);
    }

    // ==================== User Management ====================

    public boolean addUser(User user) {
        if (user == null || user.getName() == null || user.getEmail() == null) {
            System.err.println("Invalid user data!");
            return false;
        }

        User existingUser = userDAO.getUserByEmail(user.getEmail());
        if (existingUser != null) {
            System.err.println("User with email " + user.getEmail() + " already exists!");
            return false;
        }

        return userDAO.addUser(user);
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public List<User> searchUsersByName(String name) {
        return userDAO.searchUsersByName(name);
    }

    public User getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    public boolean updateUser(User user) {
        if (user == null || user.getUserId() <= 0) {
            System.err.println("Invalid user data!");
            return false;
        }
        return userDAO.updateUser(user);
    }

    public boolean suspendUser(int userId) {
        return userDAO.updateUserStatus(userId, User.Status.SUSPENDED);
    }

    public boolean activateUser(int userId) {
        return userDAO.updateUserStatus(userId, User.Status.ACTIVE);
    }

    // ==================== Transaction Management ====================

    public boolean borrowBook(int userId, int bookId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            System.err.println("User not found!");
            return false;
        }

        if (user.getStatus() != User.Status.ACTIVE) {
            System.err.println("User is not active!");
            return false;
        }

        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            System.err.println("Book not found!");
            return false;
        }

        if (!bookDAO.isBookAvailable(bookId)) {
            System.err.println("Book is not available!");
            return false;
        }

        if (transactionDAO.hasActiveBorrowing(userId, bookId)) {
            System.err.println("User already has this book borrowed!");
            return false;
        }

        List<Transaction> activeTransactions = transactionDAO.getActiveBorrowingsByUserId(userId);
        int borrowLimit = getBorrowingLimit(user.getMembershipType());

        if (activeTransactions.size() >= borrowLimit) {
            System.err.println("User has reached borrowing limit (" + borrowLimit + " books)!");
            return false;
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(getBorrowingPeriod(user.getMembershipType()));

        Transaction transaction = new Transaction(bookId, userId, borrowDate, dueDate,
                Transaction.TransactionStatus.BORROWED);

        if (transactionDAO.createTransaction(transaction)) {
            int newAvailableCopies = book.getAvailableCopies() - 1;
            bookDAO.updateAvailableCopies(bookId, newAvailableCopies);
            System.out.println("Book borrowed successfully! Due date: " + dueDate);
            return true;
        }

        return false;
    }

    public boolean returnBook(int transactionId) {
        Transaction transaction = transactionDAO.getTransactionById(transactionId);

        if (transaction == null) {
            System.err.println("Transaction not found!");
            return false;
        }

        if (transaction.getStatus() == Transaction.TransactionStatus.RETURNED) {
            System.err.println("Book already returned!");
            return false;
        }

        LocalDate returnDate = LocalDate.now();
        double fine = transactionDAO.calculateFine(transaction.getDueDate(), returnDate);

        if (transactionDAO.updateTransactionStatus(transactionId, Transaction.TransactionStatus.RETURNED,
                returnDate, fine)) {
       
            Book book = bookDAO.getBookById(transaction.getBookId());
            if (book != null) {
                int newAvailableCopies = book.getAvailableCopies() + 1;
                bookDAO.updateAvailableCopies(transaction.getBookId(), newAvailableCopies);
            }

            if (fine > 0) {
                System.out.println("Book returned successfully! Fine: Rs. " + fine);
            } else {
                System.out.println("Book returned successfully! No fine.");
            }
            return true;
        }

        return false;
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    public List<Transaction> getUserTransactions(int userId) {
        return transactionDAO.getTransactionsByUserId(userId);
    }

    public List<Transaction> getActiveBorrowings(int userId) {
        return transactionDAO.getActiveBorrowingsByUserId(userId);
    }

    public List<Transaction> getOverdueTransactions() {
       
        transactionDAO.markOverdueTransactions();
        return transactionDAO.getOverdueTransactions();
    }

    public double getUserTotalFines(int userId) {
        return transactionDAO.getTotalFinesByUserId(userId);
    }

    // ==================== Helper Methods ====================

    private int getBorrowingLimit(User.MembershipType type) {
        switch (type) {
            case STUDENT:
                return MAX_BOOKS_STUDENT;
            case FACULTY:
                return MAX_BOOKS_FACULTY;
            case PUBLIC:
                return MAX_BOOKS_PUBLIC;
            default:
                return MAX_BOOKS_PUBLIC;
        }
    }

    private int getBorrowingPeriod(User.MembershipType type) {
        switch (type) {
            case STUDENT:
                return BORROW_PERIOD_STUDENT;
            case FACULTY:
                return BORROW_PERIOD_FACULTY;
            case PUBLIC:
                return BORROW_PERIOD_PUBLIC;
            default:
                return BORROW_PERIOD_PUBLIC;
        }
    }
}
