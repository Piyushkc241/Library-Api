package com.piyush.library.repository;

import com.piyush.library.model.Item;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryItemRepository implements ItemRepository {
    private final Map<Integer, Item> store = new LinkedHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override public int nextId() { return counter.incrementAndGet(); }

    @Override
    public synchronized Item save(Item item) {
        store.put(item.getId(), item);
        return item;
    }

    @Override
    public synchronized Optional<Item> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public synchronized List<Item> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public synchronized boolean deleteById(int id) {
        return store.remove(id) != null;
    }
}
