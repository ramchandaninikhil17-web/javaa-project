package exception;

/**
 * Base checked exception for all banking and account-related failures.
 * Forms the root of the MiniBank custom exception hierarchy.
 */
public class BankException extends Exception {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
