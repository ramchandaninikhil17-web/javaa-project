package exception;

/**
 * Checked exception thrown when an invalid transaction amount is supplied (such as zero or negative values).
 */
public class InvalidAmountException extends BankException {

    public InvalidAmountException(String message) {
        super(message);
    }

    public InvalidAmountException(String message, Throwable cause) {
        super(message, cause);
    }
}
