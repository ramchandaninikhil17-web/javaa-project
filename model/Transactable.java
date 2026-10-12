package model;

import exception.InsufficientFundsException;
import exception.InvalidAmountException;

public interface Transactable {
    void deposit(long amount) throws InvalidAmountException;
    void withdraw(long amount) throws InsufficientFundsException, InvalidAmountException;
}
