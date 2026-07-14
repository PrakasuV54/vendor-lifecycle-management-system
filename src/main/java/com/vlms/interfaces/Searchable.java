package com.vlms.interfaces;

import java.util.List;

/**
 * Generic search contract for repositories.
 * Demonstrates Interface Segregation and Dependency Inversion.
 *
 * @param <T> the entity type to search
 */
public interface Searchable<T> {

    /**
     * Searches entities by name (case-insensitive partial match).
     */
    List<T> searchByName(String name);

    /**
     * Finds an entity by its unique ID.
     */
    java.util.Optional<T> findById(String id);
}
