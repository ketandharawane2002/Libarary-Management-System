-- Library Management System Database Schema
-- Author: Library Management System
-- Date: 2025-11-30

-- Create database
CREATE DATABASE IF NOT EXISTS library_management;

USE library_management;

-- Drop tables if they exist (for fresh installation)
DROP TABLE IF EXISTS transactions;

DROP TABLE IF EXISTS users;

DROP TABLE IF EXISTS books;

-- Create Books table
CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    publisher VARCHAR(100),
    publication_year INT,
    category VARCHAR(50),
    total_copies INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_copies CHECK (
        available_copies >= 0
        AND available_copies <= total_copies
    )
);

-- Create Users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    address VARCHAR(255),
    membership_type ENUM(
        'STUDENT',
        'FACULTY',
        'PUBLIC'
    ) NOT NULL DEFAULT 'PUBLIC',
    membership_date DATE NOT NULL,
    status ENUM(
        'ACTIVE',
        'SUSPENDED',
        'INACTIVE'
    ) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create Transactions table
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    user_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    status ENUM(
        'BORROWED',
        'RETURNED',
        'OVERDUE'
    ) NOT NULL DEFAULT 'BORROWED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_book_isbn ON books (isbn);

CREATE INDEX idx_book_title ON books (title);

CREATE INDEX idx_user_email ON users (email);

CREATE INDEX idx_transaction_status ON transactions (status);

CREATE INDEX idx_transaction_user ON transactions (user_id);

CREATE INDEX idx_transaction_book ON transactions (book_id);

-- Insert sample data
INSERT INTO
    books (
        title,
        author,
        isbn,
        publisher,
        publication_year,
        category,
        total_copies,
        available_copies
    )
VALUES (
        'Java Programming',
        'Herbert Schildt',
        '978-1260440232',
        'McGraw-Hill',
        2019,
        'Technology',
        6,
        6
    ),
    (
        'Database Systems',
        'Ramez Elmasri',
        '978-0133970777',
        'Pearson',
        2015,
        'Technology',
        4,
        4
    );

INSERT INTO
    users (
        name,
        email,
        phone,
        address,
        membership_type,
        membership_date,
        status
    )
VALUES (
        'John Doe',
        'john.doe@email.com',
        '1234567890',
        '123 Main St, City',
        'STUDENT',
        '2024-01-15',
        'ACTIVE'
    );

-- View to see current borrowed books
CREATE VIEW current_borrowed_books AS
SELECT
    t.transaction_id,
    b.title,
    b.author,
    b.isbn,
    u.name AS borrower_name,
    u.email AS borrower_email,
    t.borrow_date,
    t.due_date,
    DATEDIFF(CURDATE(), t.due_date) AS days_overdue,
    t.fine_amount,
    t.status
FROM
    transactions t
    JOIN books b ON t.book_id = b.book_id
    JOIN users u ON t.user_id = u.user_id
WHERE
    t.status IN ('BORROWED', 'OVERDUE');