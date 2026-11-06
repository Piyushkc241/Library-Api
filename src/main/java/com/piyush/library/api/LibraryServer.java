package com.piyush.library.api;

import com.piyush.library.exception.ItemNotFoundException;
import com.piyush.library.exception.LibraryRuleException;
import com.piyush.library.model.Item;
import com.piyush.library.service.LibraryService;
import com.piyush.library.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** REST layer built on the JDK's built-in HTTP server. */
public class LibraryServer {
    private final LibraryService service;
    private final HttpServer server;

    public LibraryServer(LibraryService service, int port) throws IOException {
        this.service = service;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/items", this::handle);
    }

    public void start() { server.start(); }
    public void stop() { server.stop(0); }
    public int getPort() { return server.getAddress().getPort(); }

    private void handle(HttpExchange ex) throws IOException {
        try {
            route(ex);
        } catch (ItemNotFoundException e) {
            send(ex, 404, Json.message("error", e.getMessage()));
        } catch (LibraryRuleException e) {
            send(ex, 409, Json.message("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            send(ex, 400, Json.message("error", e.getMessage()));
        } catch (Exception e) {
            send(ex, 500, Json.message("error", "Internal server error"));
        }
    }

    private void route(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String[] parts = ex.getRequestURI().getPath().split("/"); // ["", "items", id?, action?]
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());

        if (parts.length == 2) { // /items
            if (method.equals("GET")) {
                String q = query.get("q");
                var items = (q == null || q.isBlank()) ? service.findAll() : service.search(q);
                send(ex, 200, Json.toJson(items));
                return;
            }
            if (method.equals("POST")) {
                Map<String, String> body = Json.parseFlatObject(readBody(ex));
                Item created = service.addItem(body.get("type"), body.get("title"), body.get("creator"));
                send(ex, 201, Json.toJson(created));
                return;
            }
        } else if (parts.length >= 3) { // /items/{id}[/borrow|/return]
            int id = parseId(parts[2]);
            if (parts.length == 3) {
                if (method.equals("GET")) {
                    send(ex, 200, Json.toJson(service.getById(id)));
                    return;
                }
                if (method.equals("DELETE")) {
                    service.delete(id);
                    send(ex, 204, "");
                    return;
                }
            } else if (parts.length == 4 && method.equals("POST")) {
                if (parts[3].equals("borrow")) {
                    send(ex, 200, Json.toJson(service.borrow(id, query.get("member"))));
                    return;
                }
                if (parts[3].equals("return")) {
                    double fine = service.returnItem(id);
                    send(ex, 200, "{\"returned\":true,\"fine\":" + fine + "}");
                    return;
                }
            }
        }
        send(ex, 405, Json.message("error", "Unsupported route or method"));
    }

    private static int parseId(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("id must be a number");
        }
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null) return map;
        for (String pair : raw.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = java.net.URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }

    private static String readBody(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        if (status == 204) {
            ex.sendResponseHeaders(204, -1);
        } else {
            ex.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        }
        ex.close();
    }
}
