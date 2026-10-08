public class CommandParser {
    public static Command parse(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("Command line cannot be empty");
        }

        // Split by whitespace
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid command format. Expected: <TYPE> <ACCOUNT_NUMBER> <AMOUNT>");
        }

        TransactionType type = TransactionType.valueOf(parts[0].toUpperCase());
        String accountNumber = parts[1];
        long amount = Long.parseLong(parts[2]);

        return new Command(type, accountNumber, amount);
    }
}
