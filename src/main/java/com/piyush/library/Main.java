package com.piyush.library;

import com.piyush.library.api.LibraryServer;
import com.piyush.library.repository.InMemoryItemRepository;
import com.piyush.library.service.LibraryService;
import java.time.Clock;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        LibraryService service = new LibraryService(new InMemoryItemRepository(), Clock.systemDefaultZone());
        // Sample data so the API has something to show on first run
        service.addItem("BOOK", "Clean Code", "Robert C. Martin");
        service.addItem("BOOK", "Effective Java", "Joshua Bloch");
        service.addItem("MAGAZINE", "Tech Monthly", "Issue 42");

        LibraryServer server = new LibraryServer(service, port);
        server.start();
        System.out.println("Library API running at http://localhost:" + server.getPort() + "/items");
    }
}
