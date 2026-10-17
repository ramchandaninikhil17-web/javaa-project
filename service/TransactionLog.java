package service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

/**
 * Practical 11 - Requirement 3: TransactionLog
 * 
 * Provides append-only persistent logging for banking transactions.
 * Employs Java NIO Files.write(...) with CREATE and APPEND options.
 */
public class TransactionLog {

    /**
     * Appends a transaction record line to the specified log file.
     * Uses Files.write(...) with StandardOpenOption.CREATE and StandardOpenOption.APPEND.
     * 
     * @param file target log file Path
     * @param line transaction entry string to append
     * @throws IOException if an I/O error occurs during file writing
     */
    public static void append(Path file, String line) throws IOException {
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        Files.write(
            file,
            Collections.singletonList(line),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }
}
