package com.chaitanya.expensetracker;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Scanner;

public class ExpenseTrackerApp {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final ExpenseDAO DAO = new ExpenseDAO();

    public static void main(String[] args) {
        try {
            DAO.createTableIfNeeded();
            System.out.println("==================================");
            System.out.println("       SMART EXPENSE TRACKER");
            System.out.println("==================================");
            boolean running = true;
            while (running) {
                showMenu();
                String choice = read("Choose an option: ");
                try {
                    switch (choice) {
                        case "1" -> addExpense();
                        case "2" -> showExpenses(DAO.findAll());
                        case "3" -> searchByCategory();
                        case "4" -> searchByDate();
                        case "5" -> updateExpense();
                        case "6" -> deleteExpense();
                        case "7" -> monthlyReport();
                        case "0" -> { running = false; System.out.println("Thank you for using Smart Expense Tracker!"); }
                        default -> System.out.println("Invalid option. Choose 0-7.");
                    }
                } catch (SQLException e) {
                    System.out.println("Database operation failed: " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input: " + e.getMessage());
                }
                System.out.println();
            }
        } catch (SQLException | IllegalStateException e) {
            System.out.println("Could not start the application: " + e.getMessage());
            System.out.println("Check MySQL is running and db.properties has the correct settings.");
        } finally {
            SCANNER.close();
        }
    }

    private static void showMenu() {
        System.out.println("""
            1. Add expense
            2. View all expenses
            3. Search by category
            4. Search by date
            5. Update expense
            6. Delete expense
            7. Monthly report
            0. Exit
            """);
    }

    private static void addExpense() throws SQLException {
        String title = nonBlank("Expense title: ");
        BigDecimal amount = readAmount("Amount (e.g. 250.50): ");
        String category = chooseCategory();
        LocalDate date = readDate("Date (YYYY-MM-DD): ");
        String notes = read("Notes (optional): ");
        DAO.add(new Expense(title, amount, category, date, notes));
        System.out.println("Expense added successfully.");
    }

    private static void searchByCategory() throws SQLException {
        String category = chooseCategory();
        showExpenses(DAO.findByCategory(category));
    }

    private static void searchByDate() throws SQLException {
        showExpenses(DAO.findByDate(readDate("Date (YYYY-MM-DD): ")));
    }

    private static void updateExpense() throws SQLException {
        int id = readPositiveInt("Enter expense ID to update: ");
        var found = DAO.findById(id);
        if (found.isEmpty()) { System.out.println("No expense found with ID " + id); return; }
        Expense old = found.get();
        System.out.println("Current: " + old);
        String title = nonBlank("New title: ");
        BigDecimal amount = readAmount("New amount: ");
        String category = chooseCategory();
        LocalDate date = readDate("New date (YYYY-MM-DD): ");
        String notes = read("New notes (optional): ");
        boolean updated = DAO.update(new Expense(id, title, amount, category, date, notes));
        System.out.println(updated ? "Expense updated successfully." : "No expense was updated.");
    }

    private static void deleteExpense() throws SQLException {
        int id = readPositiveInt("Enter expense ID to delete: ");
        var found = DAO.findById(id);
        if (found.isEmpty()) { System.out.println("No expense found with ID " + id); return; }
        System.out.println("Selected: " + found.get());
        String confirm = read("Type YES to confirm deletion: ");
        if (confirm.equals("YES")) {
            System.out.println(DAO.delete(id) ? "Expense deleted." : "Expense was not found.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void monthlyReport() throws SQLException {
        String input = read("Month (YYYY-MM), or press Enter for current month: ");
        YearMonth month = input.isBlank() ? YearMonth.now() : YearMonth.parse(input);
        BigDecimal total = DAO.monthlyTotal(month.getYear(), month.getMonthValue());
        System.out.println("\nReport for " + month + ":");
        System.out.println("Total expenses: ₹" + total);
        List<ExpenseDAO.CategoryTotal> categories =
                DAO.highestSpendingCategories(month.getYear(), month.getMonthValue());
        if (categories.isEmpty()) {
            System.out.println("No expenses recorded for this month.");
        } else {
            System.out.println("Spending by category (highest first):");
            for (ExpenseDAO.CategoryTotal c : categories) {
                System.out.println("  " + c.category() + ": ₹" + c.total());
            }
            System.out.println("Highest spending category: " + categories.get(0).category()
                    + " (₹" + categories.get(0).total() + ")");
        }
    }

    private static void showExpenses(List<Expense> expenses) {
        if (expenses.isEmpty()) { System.out.println("No expenses found."); return; }
        System.out.println("\n--- Expenses (" + expenses.size() + ") ---");
        for (Expense e : expenses) System.out.println(e);
    }

    private static String chooseCategory() {
        System.out.println("Categories: Food, Travel, Shopping, Bills, Education, Health, Entertainment, Other");
        String category = nonBlank("Category: ");
        return switch (category.toLowerCase()) {
            case "food" -> "Food";
            case "travel" -> "Travel";
            case "shopping" -> "Shopping";
            case "bills" -> "Bills";
            case "education" -> "Education";
            case "health" -> "Health";
            case "entertainment" -> "Entertainment";
            case "other" -> "Other";
            default -> throw new IllegalArgumentException("Choose one of the listed categories.");
        };
    }

    private static BigDecimal readAmount(String prompt) {
        BigDecimal amount;
        try { amount = new BigDecimal(read(prompt)); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("Amount must be a number."); }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        return amount.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private static LocalDate readDate(String prompt) {
        try { return LocalDate.parse(read(prompt)); }
        catch (Exception e) { throw new IllegalArgumentException("Use date format YYYY-MM-DD, for example 2026-10-21."); }
    }

    private static int readPositiveInt(String prompt) {
        try {
            int n = Integer.parseInt(read(prompt));
            if (n <= 0) throw new NumberFormatException();
            return n;
        } catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a positive whole number."); }
    }

    private static String nonBlank(String prompt) {
        String value = read(prompt).trim();
        if (value.isEmpty()) throw new IllegalArgumentException("This field cannot be empty.");
        return value;
    }

    private static String read(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }
}
