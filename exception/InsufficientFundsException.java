package exception;

/**
 * Checked exception thrown when an account balance (or credit limit) is insufficient to honor a withdrawal.
 * Carries the specific shortfall amount indicating how much additional balance was needed.
 */
public class InsufficientFundsException extends BankException {
    private final long shortfall;

    public InsufficientFundsException(String message, long shortfall) {
        super(message);
        this.shortfall = shortfall;
    }

    public InsufficientFundsException(String message) {
        this(message, 0);
    }

    public long getShortfall() {
        return shortfall;
    }
}
