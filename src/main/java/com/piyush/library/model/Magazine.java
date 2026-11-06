package com.piyush.library.model;

public class Magazine extends Item {
    private final String issue;

    public Magazine(int id, String title, String issue) {
        super(id, title);
        this.issue = (issue == null || issue.isBlank()) ? "N/A" : issue.trim();
    }

    @Override public int getLoanDays() { return 7; }
    @Override public double getFinePerDay() { return 1.0; }
    @Override public String getType() { return "MAGAZINE"; }
    @Override public String getCreator() { return issue; }
}
