package com.piyush.library.model;

public class Book extends Item {
    private final String author;

    public Book(int id, String title, String author) {
        super(id, title);
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("author is required for a book");
        }
        this.author = author.trim();
    }

    @Override public int getLoanDays() { return 14; }
    @Override public double getFinePerDay() { return 2.0; }
    @Override public String getType() { return "BOOK"; }
    @Override public String getCreator() { return author; }
}
