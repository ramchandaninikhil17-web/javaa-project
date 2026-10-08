package model;

public interface InterestBearing {
    double interestRate();
    long getBalance();

    // Default method calculating annual interest using interestRate() and balance
    default double yearlyInterest() {
        return (getBalance() * interestRate()) / 100.0;
    }
}
