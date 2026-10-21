package model;

/**
 * MenuOption enum defining the actions supported by MiniBank.
 * Expanded to integrate all capstone capabilities:
 * - Account operations (OPEN_ACCOUNT, DEPOSIT, WITHDRAW, TRANSFER, STATEMENT, LIST_ACCOUNTS)
 * - Safe concurrency batch processing (BATCH_PROCESS)
 * - End-of-day NIO analytics (REPORT)
 * - Safe termination and persistence (EXIT)
 */
public enum MenuOption {
    OPEN_ACCOUNT,
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    STATEMENT,
    LIST_ACCOUNTS,
    BATCH_PROCESS,
    REPORT,
    EXIT
}
