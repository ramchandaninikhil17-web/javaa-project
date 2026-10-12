package exception;

/**
 * Checked exception thrown when an operation refers to a non-existent, null, or untracked account.
 */
public class AccountNotFoundException extends BankException {

    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
