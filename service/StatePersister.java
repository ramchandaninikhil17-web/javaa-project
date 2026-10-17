package service;

import model.Account;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Practical 11 - Requirement 2: StatePersister
 * 
 * Provides binary object serialization for saving and restoring arrays of Account objects.
 * Utilizes ObjectOutputStream and ObjectInputStream inside try-with-resources blocks.
 */
public class StatePersister {

    /**
     * Serializes an array of Account objects to the specified file Path.
     * Uses ObjectOutputStream inside try-with-resources.
     * 
     * @param accounts array of Account objects to serialize
     * @param file target file path
     * @throws IOException if an I/O error occurs during serialization
     */
    public static void save(Account[] accounts, Path file) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(accounts);
        }
    }

    /**
     * Deserializes an array of Account objects from the specified file Path.
     * Uses ObjectInputStream inside try-with-resources.
     * 
     * @param file source file path
     * @return deserialized array of Account objects
     * @throws IOException if an I/O error occurs during deserialization
     * @throws ClassNotFoundException if an account class cannot be found
     */
    public static Account[] load(Path file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
            return (Account[]) ois.readObject();
        }
    }
}
