package com.piyush.library.repository;

import com.piyush.library.model.Item;
import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    int nextId();
    Item save(Item item);
    Optional<Item> findById(int id);
    List<Item> findAll();
    boolean deleteById(int id);
}
