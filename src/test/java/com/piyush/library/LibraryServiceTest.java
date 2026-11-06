package com.piyush.library;

import com.piyush.library.exception.ItemNotFoundException;
import com.piyush.library.exception.LibraryRuleException;
import com.piyush.library.model.Item;
import com.piyush.library.repository.InMemoryItemRepository;
import com.piyush.library.service.LibraryService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

/** Dependency-free tests: run with `java -cp out com.piyush.library.LibraryServiceTest`. */
public class LibraryServiceTest {
    private static int passed = 0;

    public static void main(String[] args) {
        borrowAndReturnOnTime();
        lateReturnChargesFine();
        magazineHasShorterLoanAndLowerFine();
        cannotBorrowTwice();
        borrowLimitIsEnforced();
        missingItemThrows();
        searchIsCaseInsensitive();
        invalidInputRejected();
        System.out.println("All " + passed + " tests passed");
    }

    private static LibraryService serviceAt(String isoInstant) {
        Clock clock = Clock.fixed(Instant.parse(isoInstant), ZoneOffset.UTC);
        return new LibraryService(new InMemoryItemRepository(), clock);
    }

    /** Service whose clock we can move forward to simulate late returns. */
    private static LibraryService[] serviceWithLaterView(String borrowDay, String returnDay) {
        InMemoryItemRepository repo = new InMemoryItemRepository();
        LibraryService borrowSide = new LibraryService(repo,
                Clock.fixed(Instant.parse(borrowDay), ZoneOffset.UTC));
        LibraryService returnSide = new LibraryService(repo,
                Clock.fixed(Instant.parse(returnDay), ZoneOffset.UTC));
        return new LibraryService[] {borrowSide, returnSide};
    }

    static void borrowAndReturnOnTime() {
        LibraryService s = serviceAt("2026-11-01T00:00:00Z");
        Item book = s.addItem("BOOK", "Clean Code", "Robert Martin");
        s.borrow(book.getId(), "piyush");
        check(!book.isAvailable(), "item should be unavailable after borrow");
        check(book.getDueDate().toString().equals("2026-11-15"), "book due in 14 days");
        check(s.returnItem(book.getId()) == 0.0, "no fine when on time");
        check(book.isAvailable(), "item available after return");
    }

    static void lateReturnChargesFine() {
        LibraryService[] s = serviceWithLaterView("2026-11-01T00:00:00Z", "2026-11-20T00:00:00Z");
        Item book = s[0].addItem("BOOK", "Effective Java", "Joshua Bloch");
        s[0].borrow(book.getId(), "piyush");
        // due 15 Nov, returned 20 Nov -> 5 days late x 2.0
        check(s[1].returnItem(book.getId()) == 10.0, "5 days late should cost 10.0");
    }

    static void magazineHasShorterLoanAndLowerFine() {
        LibraryService[] s = serviceWithLaterView("2026-11-01T00:00:00Z", "2026-11-12T00:00:00Z");
        Item mag = s[0].addItem("MAGAZINE", "Tech Monthly", "Issue 42");
        s[0].borrow(mag.getId(), "piyush");
        check(mag.getDueDate().toString().equals("2026-11-08"), "magazine due in 7 days");
        check(s[1].returnItem(mag.getId()) == 4.0, "4 days late x 1.0");
    }

    static void cannotBorrowTwice() {
        LibraryService s = serviceAt("2026-11-01T00:00:00Z");
        Item book = s.addItem("BOOK", "A", "B");
        s.borrow(book.getId(), "x");
        expect(LibraryRuleException.class, () -> s.borrow(book.getId(), "y"), "double borrow");
    }

    static void borrowLimitIsEnforced() {
        LibraryService s = serviceAt("2026-11-01T00:00:00Z");
        for (int i = 1; i <= 4; i++) s.addItem("BOOK", "Book " + i, "Author");
        s.borrow(1, "piyush");
        s.borrow(2, "piyush");
        s.borrow(3, "piyush");
        expect(LibraryRuleException.class, () -> s.borrow(4, "piyush"), "borrow limit");
    }

    static void missingItemThrows() {
        LibraryService s = serviceAt("2026-11-01T00:00:00Z");
        expect(ItemNotFoundException.class, () -> s.getById(99), "missing item");
    }

    static void searchIsCaseInsensitive() {
        LibraryService s = serviceAt("2026-11-01T00:00:00Z");
        s.addItem("BOOK", "Clean Code", "Robert Martin");
        s.addItem("BOOK", "Effective Java", "Joshua Bloch");
        check(s.search("JAVA").size() == 1, "search by title");
        check(s.search("bloch").size() == 1, "search by author");
        check(s.search("zzz").isEmpty(), "no match");
    }

    static void invalidInputRejected() {
        LibraryService s = serviceAt("2026-11-01T00:00:00Z");
        expect(IllegalArgumentException.class, () -> s.addItem("DVD", "x", "y"), "unknown type");
        expect(IllegalArgumentException.class, () -> s.addItem("BOOK", " ", "y"), "blank title");
        expect(IllegalArgumentException.class, () -> s.addItem("BOOK", "t", null), "missing author");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("FAILED: " + message);
        passed++;
    }

    private static void expect(Class<? extends Throwable> type, Runnable action, String label) {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError("FAILED: " + label + " threw " + t.getClass().getSimpleName());
        }
        throw new AssertionError("FAILED: " + label + " did not throw");
    }
}
