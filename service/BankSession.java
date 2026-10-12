package service;

/**
 * A banking session context representing an active operator or teller connection.
 * Implements AutoCloseable to guarantee proper cleanup and resource release via try-with-resources.
 */
public class BankSession implements AutoCloseable {
    private final String sessionUser;
    private boolean active;

    public BankSession(String sessionUser) {
        this.sessionUser = sessionUser;
        this.active = true;
        System.out.println("  [BankSession] Logged in user: " + sessionUser + " (Session initialized)");
    }

    public void logOperation(String description) {
        if (!active) {
            throw new IllegalStateException("Cannot log operations in an inactive session.");
        }
        System.out.println("  [BankSession] Audited: " + description);
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void close() {
        this.active = false;
        System.out.println("  [BankSession] Session for " + sessionUser + " closed safely (Resources released).");
    }
}
