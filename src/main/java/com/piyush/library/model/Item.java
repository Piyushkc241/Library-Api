package com.piyush.library.model;

import java.time.LocalDate;

/** Base class for everything the library can lend (abstraction + inheritance). */
public abstract class Item {
    private final int id;
    private final String title;
    private String borrowedBy;
    private LocalDate dueDate;

    protected Item(int id, String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        this.id = id;
        this.title = title.trim();
    }

    /** Each subclass decides its own lending rules (polymorphism). */
    public abstract int getLoanDays();

    public abstract double getFinePerDay();

    public abstract String getType();

    /** Name shown in search results, e.g. the author or the issue. */
    public abstract String getCreator();

    public boolean isAvailable() {
        return borrowedBy == null;
    }

    public void checkOut(String member, LocalDate today) {
        this.borrowedBy = member;
        this.dueDate = today.plusDays(getLoanDays());
    }

    public void checkIn() {
        this.borrowedBy = null;
        this.dueDate = null;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getBorrowedBy() { return borrowedBy; }
    public LocalDate getDueDate() { return dueDate; }
}
