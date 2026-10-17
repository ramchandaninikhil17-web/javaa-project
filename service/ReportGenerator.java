package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Practical 11 - Requirement 4: ReportGenerator
 * 
 * Walks the transaction logs directory using Java NIO Files.newDirectoryStream.
 * Inspects each file's metadata with Files.readAttributes (size, last-modified),
 * reads entries line-by-line using a BufferedReader, aggregates total deposits
 * and withdrawals, and writes an end-of-day summary report.
 */
public class ReportGenerator {

    /**
     * Walks the specified logs folder, calculates transaction totals, writes
     * the report to the target report file, and returns the generated report content.
     * 
     * @param logsFolder the directory path containing transaction logs
     * @param reportFile the destination path for the generated summary report
     * @return the generated report text
     * @throws IOException if an I/O error occurs
     */
    public static String generateReport(Path logsFolder, Path reportFile) throws IOException {
        if (!Files.exists(logsFolder) || !Files.isDirectory(logsFolder)) {
            throw new IllegalArgumentException("Logs directory does not exist or is not a directory: " + logsFolder);
        }

        long totalDeposits = 0;
        long totalWithdrawals = 0;
        long depositCount = 0;
        long withdrawalCount = 0;
        int fileCount = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append("               MINIBANK END-OF-DAY SUMMARY REPORT                \n");
        sb.append("=================================================================\n");
        sb.append("Generated On : ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("Logs Folder  : ").append(logsFolder.toAbsolutePath().normalize()).append("\n");
        sb.append("-----------------------------------------------------------------\n");
        sb.append("INSPECTED TRANSACTION LOG FILES:\n");

        // Requirement 4: Walk the logs directory using Files.newDirectoryStream
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsFolder)) {
            for (Path logPath : stream) {
                if (Files.isRegularFile(logPath)) {
                    fileCount++;

                    // Requirement 4: Read file attributes (size, last-modified) using Files.readAttributes
                    BasicFileAttributes attrs = Files.readAttributes(logPath, BasicFileAttributes.class);
                    long sizeBytes = attrs.size();
                    FileTime lastModified = attrs.lastModifiedTime();

                    sb.append(String.format("  * %-26s | Size: %6d bytes | Last Modified: %s%n",
                            logPath.getFileName().toString(), sizeBytes, lastModified.toString()));

                    // Requirement 4: Read each file line by line with a BufferedReader
                    try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String trimmed = line.trim();
                            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                                continue;
                            }

                            String upper = trimmed.toUpperCase();
                            boolean isDeposit = upper.contains("DEPOSIT");
                            boolean isWithdraw = upper.contains("WITHDRAW");

                            if (isDeposit || isWithdraw) {
                                String[] parts = trimmed.split("[,|\\s]+");
                                for (int i = parts.length - 1; i >= 0; i--) {
                                    try {
                                        long amount = Long.parseLong(parts[i]);
                                        if (isDeposit) {
                                            totalDeposits += amount;
                                            depositCount++;
                                        } else {
                                            totalWithdrawals += amount;
                                            withdrawalCount++;
                                        }
                                        break;
                                    } catch (NumberFormatException ignored) {
                                        // continue inspecting parts for the numeric amount
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        long netFlow = totalDeposits - totalWithdrawals;

        sb.append("-----------------------------------------------------------------\n");
        sb.append("TRANSACTION TOTALS & RECONCILIATION:\n");
        sb.append(String.format("  Total Log Files Processed : %d%n", fileCount));
        sb.append(String.format("  Total Deposits Count      : %d transactions%n", depositCount));
        sb.append(String.format("  Total Deposits Sum        : Rs.%,d%n", totalDeposits));
        sb.append(String.format("  Total Withdrawals Count   : %d transactions%n", withdrawalCount));
        sb.append(String.format("  Total Withdrawals Sum     : Rs.%,d%n", totalWithdrawals));
        sb.append(String.format("  Net Bank Cash Flow        : Rs.%,d (%s)%n", 
                netFlow, netFlow >= 0 ? "+SURPLUS" : "-DEFICIT"));
        sb.append("=================================================================\n");

        String reportText = sb.toString();

        // Write summary report to file using Java NIO
        if (reportFile.getParent() != null) {
            Files.createDirectories(reportFile.getParent());
        }
        Files.write(
            reportFile,
            Collections.singletonList(reportText),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE
        );

        return reportText;
    }
}
