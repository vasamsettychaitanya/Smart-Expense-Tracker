package com.chaitanya.expensetracker;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExpenseDAO {
    public void createTableIfNeeded() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INT PRIMARY KEY AUTO_INCREMENT,
                title VARCHAR(100) NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                category VARCHAR(40) NOT NULL,
                expense_date DATE NOT NULL,
                notes VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    public void add(Expense e) throws SQLException {
        String sql = "INSERT INTO expenses(title, amount, category, expense_date, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setBigDecimal(2, e.getAmount());
            ps.setString(3, e.getCategory());
            ps.setDate(4, Date.valueOf(e.getExpenseDate()));
            ps.setString(5, e.getNotes());
            ps.executeUpdate();
        }
    }

    public List<Expense> findAll() throws SQLException {
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC, id DESC";
        List<Expense> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public List<Expense> findByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE LOWER(category)=LOWER(?) ORDER BY expense_date DESC, id DESC";
        List<Expense> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<Expense> findByDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE expense_date=? ORDER BY id DESC";
        List<Expense> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public Optional<Expense> findById(int id) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public boolean update(Expense e) throws SQLException {
        String sql = "UPDATE expenses SET title=?, amount=?, category=?, expense_date=?, notes=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setBigDecimal(2, e.getAmount());
            ps.setString(3, e.getCategory());
            ps.setDate(4, Date.valueOf(e.getExpenseDate()));
            ps.setString(5, e.getNotes());
            ps.setInt(6, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM expenses WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public BigDecimal monthlyTotal(int year, int month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses WHERE YEAR(expense_date)=? AND MONTH(expense_date)=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("total");
            }
        }
    }

    public List<CategoryTotal> highestSpendingCategories(int year, int month) throws SQLException {
        String sql = """
            SELECT category, SUM(amount) AS total
            FROM expenses
            WHERE YEAR(expense_date)=? AND MONTH(expense_date)=?
            GROUP BY category
            ORDER BY total DESC
            """;
        List<CategoryTotal> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(new CategoryTotal(
                        rs.getString("category"), rs.getBigDecimal("total")));
            }
        }
        return result;
    }

    private Expense map(ResultSet rs) throws SQLException {
        return new Expense(rs.getInt("id"), rs.getString("title"),
                rs.getBigDecimal("amount"), rs.getString("category"),
                rs.getDate("expense_date").toLocalDate(), rs.getString("notes"));
    }

    public record CategoryTotal(String category, BigDecimal total) {}
}
