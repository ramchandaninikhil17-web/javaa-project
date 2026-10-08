package util;

import model.Account;

public class StatementFormatter {
    public static String buildStatement(Account account) {
        if (account == null) {
            return "No account provided.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("----------------------------------------\n");
        sb.append("           ACCOUNT STATEMENT            \n");
        sb.append("----------------------------------------\n");
        sb.append("Account Number : ").append(account.getAccountNumber()).append("\n");
        sb.append("Account Holder : ").append(account.getOwnerName()).append("\n");
        sb.append("Account Status : ").append(account.isActive() ? "Active" : "Inactive").append("\n");
        sb.append("Current Balance: Rs.").append(account.getBalance()).append("\n");
        sb.append("Annual Interest: Rs.").append(account.yearlyInterest()).append("\n");
        sb.append("----------------------------------------");

        return sb.toString();
    }
}
