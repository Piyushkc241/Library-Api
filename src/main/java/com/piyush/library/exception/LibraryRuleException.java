package com.piyush.library.exception;

/** Thrown when a business rule is broken (already borrowed, borrow limit reached, ...). */
public class LibraryRuleException extends RuntimeException {
    public LibraryRuleException(String message) {
        super(message);
    }
}
