package service;

import model.annotation.Id;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Practical 13 - Generic Data-Access Layer: Repository<T>
 * 
 * A reusable, generic in-memory repository that manages entities of any type T.
 * - Stores records in a HashMap<String, T> keyed by each object's @Id field.
 * - Extracts primary keys dynamically at runtime using Java Reflection.
 * - Provides generic CRUD operations (save, findById, findAll, delete, saveAll).
 * - Features a bounded generic method for statistical operations.
 * 
 * @param <T> the entity type managed by this repository
 */
public class Repository<T> {

    // Requirement 1: Holds a HashMap<String, T>
    private final Map<String, T> storage = new HashMap<>();

    public Repository() {}

    /**
     * Requirement 1 & 2: Saves an entity into the repository.
     * Uses reflection to locate the field annotated with @Id and extracts its value
     * as the primary key.
     * 
     * @param entity the entity to store
     * @return the saved entity
     */
    public T save(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot save null entity into repository.");
        }
        String id = extractId(entity);
        storage.put(id, entity);
        return entity;
    }

    /**
     * Requirement 1: Finds an entity by its primary key.
     * 
     * @param id the entity primary key
     * @return the matching entity, or null if not found
     */
    public T findById(String id) {
        if (id == null) {
            return null;
        }
        return storage.get(id);
    }

    /**
     * Requirement 1: Returns all entities stored in the repository.
     * 
     * @return an ArrayList containing all entities
     */
    public ArrayList<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    /**
     * Requirement 1: Deletes an entity by its primary key identifier.
     * 
     * @param id the entity primary key
     * @return true if an entity was deleted, false otherwise
     */
    public boolean delete(String id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    /**
     * Requirement 3: Saves several objects at once using generic varargs.
     * Marked with @SafeVarargs to guarantee type safety and suppress heap pollution warnings.
     * 
     * @param items varargs of entities to persist
     */
    @SafeVarargs
    public final void saveAll(T... items) {
        if (items != null) {
            for (T item : items) {
                if (item != null) {
                    save(item);
                }
            }
        }
    }

    /**
     * Requirement 4: Generic method with a bounded type parameter (<N extends Number>).
     * Computes the arithmetic average of an array of numeric values.
     * 
     * @param <N> a type bounded by java.lang.Number
     * @param values array of numeric values
     * @return the arithmetic average as a double
     */
    public static <N extends Number> double average(N[] values) {
        if (values == null || values.length == 0) {
            return 0.0;
        }
        double sum = 0.0;
        int count = 0;
        for (N val : values) {
            if (val != null) {
                sum += val.doubleValue();
                count++;
            }
        }
        return count > 0 ? (sum / count) : 0.0;
    }

    /**
     * Requirement 2: Extracts the @Id annotated field value using Java Reflection.
     * Traverses up the class hierarchy to support fields declared in parent classes.
     * 
     * @param entity the object to inspect
     * @return the String value of the primary key field
     */
    private String extractId(T entity) {
        Class<?> clazz = entity.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(entity);
                        if (value == null) {
                            throw new IllegalStateException("Field marked with @Id ('" 
                                    + field.getName() + "') has null value in " + entity);
                        }
                        return String.valueOf(value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Could not access @Id field '" + field.getName() + "'", e);
                    }
                }
            }
            clazz = clazz.getSuperclass(); // Inspect superclass fields (e.g. Account)
        }
        throw new IllegalArgumentException("No field annotated with @Id found in " + entity.getClass().getName());
    }

    /**
     * Returns the number of entities in the repository.
     * 
     * @return entity count
     */
    public int count() {
        return storage.size();
    }
}
