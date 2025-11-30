package com.library.dao;

import com.library.model.Transaction;
import com.library.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final double FINE_PER_DAY = 5.0; // Fine amount per day for overdue books

    public boolean createTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (book_id, user_id, borrow_date, due_date, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, transaction.getBookId());
            pstmt.setInt(2, transaction.getUserId());
            pstmt.setDate(3, Date.valueOf(transaction.getBorrowDate()));
            pstmt.setDate(4, Date.valueOf(transaction.getDueDate()));
            pstmt.setString(5, transaction.getStatus().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        transaction.setTransactionId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("Transaction created successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Transaction getTransactionById(int transactionId) {
        String sql = "SELECT t.*, b.title as book_title, u.name as user_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.transaction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractTransactionFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title, u.name as user_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "ORDER BY t.transaction_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByUserId(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title, u.name as user_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.user_id = ? ORDER BY t.transaction_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions by user: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getTransactionsByBookId(int bookId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title, u.name as user_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.book_id = ? ORDER BY t.transaction_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions by book: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

   
    public List<Transaction> getActiveBorrowingsByUserId(int userId) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title, u.name as user_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.user_id = ? AND t.status IN ('BORROWED', 'OVERDUE') " +
                "ORDER BY t.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving active borrowings: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public List<Transaction> getOverdueTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, b.title as book_title, u.name as user_name " +
                "FROM transactions t " +
                "JOIN books b ON t.book_id = b.book_id " +
                "JOIN users u ON t.user_id = u.user_id " +
                "WHERE t.status = 'OVERDUE' OR (t.status = 'BORROWED' AND t.due_date < CURDATE()) " +
                "ORDER BY t.due_date";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(extractTransactionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving overdue transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public boolean updateTransactionStatus(int transactionId, Transaction.TransactionStatus status,
            LocalDate returnDate, double fineAmount) {
        String sql = "UPDATE transactions SET status = ?, return_date = ?, fine_amount = ? " +
                "WHERE transaction_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setDate(2, returnDate != null ? Date.valueOf(returnDate) : null);
            pstmt.setDouble(3, fineAmount);
            pstmt.setInt(4, transactionId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Transaction updated successfully!");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating transaction: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public int markOverdueTransactions() {
        String sql = "UPDATE transactions SET status = 'OVERDUE' " +
                "WHERE status = 'BORROWED' AND due_date < CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            int affectedRows = stmt.executeUpdate(sql);
            if (affectedRows > 0) {
                System.out.println(affectedRows + " transaction(s) marked as overdue.");
            }
            return affectedRows;
        } catch (SQLException e) {
            System.err.println("Error marking overdue transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    public double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        if (returnDate.isAfter(dueDate)) {
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            return daysOverdue * FINE_PER_DAY;
        }
        return 0.0;
    }

    public double getTotalFinesByUserId(int userId) {
        String sql = "SELECT SUM(fine_amount) as total_fine FROM transactions WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_fine");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total fines: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean hasActiveBorrowing(int userId, int bookId) {
        String sql = "SELECT COUNT(*) as count FROM transactions " +
                "WHERE user_id = ? AND book_id = ? AND status IN ('BORROWED', 'OVERDUE')";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking active borrowing: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private Transaction extractTransactionFromResultSet(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getInt("transaction_id"));
        transaction.setBookId(rs.getInt("book_id"));
        transaction.setUserId(rs.getInt("user_id"));

        Date borrowDate = rs.getDate("borrow_date");
        if (borrowDate != null) {
            transaction.setBorrowDate(borrowDate.toLocalDate());
        }

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            transaction.setDueDate(dueDate.toLocalDate());
        }

        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            transaction.setReturnDate(returnDate.toLocalDate());
        }

        transaction.setFineAmount(rs.getDouble("fine_amount"));
        transaction.setStatus(Transaction.TransactionStatus.valueOf(rs.getString("status")));

        // Additional fields
        transaction.setBookTitle(rs.getString("book_title"));
        transaction.setUserName(rs.getString("user_name"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            transaction.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return transaction;
    }
}
