package com.chaitanya.expensetracker;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private int id;
    private String title;
    private BigDecimal amount;
    private String category;
    private LocalDate expenseDate;
    private String notes;

    public Expense(int id, String title, BigDecimal amount, String category,
                   LocalDate expenseDate, String notes) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate;
        this.notes = notes;
    }

    public Expense(String title, BigDecimal amount, String category,
                   LocalDate expenseDate, String notes) {
        this(0, title, amount, category, expenseDate, notes);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public BigDecimal getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getNotes() { return notes; }

    @Override
    public String toString() {
        return String.format("ID: %d | %s | ₹%s | %s | %s%s",
                id, title, amount.toPlainString(), category, expenseDate,
                notes == null || notes.isBlank() ? "" : " | " + notes);
    }
}
