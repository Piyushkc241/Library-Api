package com.piyush.library.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(int id) {
        super("No item found with id " + id);
    }
}
