package com.piyush.library.service;

import com.piyush.library.exception.ItemNotFoundException;
import com.piyush.library.exception.LibraryRuleException;
import com.piyush.library.model.Book;
import com.piyush.library.model.Item;
import com.piyush.library.model.Magazine;
import com.piyush.library.repository.ItemRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class LibraryService {
    public static final int MAX_ITEMS_PER_MEMBER = 3;

    private final ItemRepository repository;
    private final Clock clock;

    public LibraryService(ItemRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Item addItem(String type, String title, String creator) {
        if (type == null) {
            throw new IllegalArgumentException("type is required (BOOK or MAGAZINE)");
        }
        int id = repository.nextId();
        Item item = switch (type.toUpperCase(Locale.ROOT)) {
            case "BOOK" -> new Book(id, title, creator);
            case "MAGAZINE" -> new Magazine(id, title, creator);
            default -> throw new IllegalArgumentException("unknown type: " + type);
        };
        return repository.save(item);
    }

    public List<Item> findAll() {
        return repository.findAll();
    }

    public Item getById(int id) {
        return repository.findById(id).orElseThrow(() -> new ItemNotFoundException(id));
    }

    /** Case-insensitive search over title and author/issue. */
    public List<Item> search(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        return repository.findAll().stream()
                .filter(i -> i.getTitle().toLowerCase(Locale.ROOT).contains(q)
                        || i.getCreator().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    public Item borrow(int id, String member) {
        if (member == null || member.isBlank()) {
            throw new IllegalArgumentException("member is required");
        }
        Item item = getById(id);
        if (!item.isAvailable()) {
            throw new LibraryRuleException("Item " + id + " is already borrowed");
        }
        long current = repository.findAll().stream()
                .filter(i -> member.equals(i.getBorrowedBy()))
                .count();
        if (current >= MAX_ITEMS_PER_MEMBER) {
            throw new LibraryRuleException(member + " has reached the limit of "
                    + MAX_ITEMS_PER_MEMBER + " items");
        }
        item.checkOut(member, LocalDate.now(clock));
        return item;
    }

    /** Returns the fine owed (0 if on time). */
    public double returnItem(int id) {
        Item item = getById(id);
        if (item.isAvailable()) {
            throw new LibraryRuleException("Item " + id + " is not borrowed");
        }
        long daysLate = ChronoUnit.DAYS.between(item.getDueDate(), LocalDate.now(clock));
        double fine = daysLate > 0 ? daysLate * item.getFinePerDay() : 0.0;
        item.checkIn();
        return fine;
    }

    public void delete(int id) {
        Item item = getById(id);
        if (!item.isAvailable()) {
            throw new LibraryRuleException("Cannot delete a borrowed item");
        }
        repository.deleteById(id);
    }
}
