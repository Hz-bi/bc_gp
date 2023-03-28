package com.groupProject.Network;

import java.util.Base64;

public class Transaction {
    private String sender;
    private String recipient;
    private double amount;
    private String signature;

    public Transaction(String sender, String recipient, double amount, String signature) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.signature = signature;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public double getAmount() {
        return amount;
    }

    public String getSignature() {
        return signature;
    }

    public String toString() {
        return "Transaction(sender=" + sender + ", recipient=" + recipient + ", amount=" + amount + ", signature=" + signature + ")";
    }

    public String serialize() {
        return Base64.getEncoder().encodeToString(toString().getBytes());
    }

    public static Transaction deserialize(String s) {
        byte[] bytes = Base64.getDecoder().decode(s);
        String str = new String(bytes);
        String[] parts = str.substring("Transaction(".length(), str.length() - 1).split(", ");
        String sender = parts[0].substring("sender=".length());
        String recipient = parts[1].substring("recipient=".length());
        double amount = Double.parseDouble(parts[2].substring("amount=".length()));
        String signature = parts[3].substring("signature=".length());
        return new Transaction(sender, recipient, amount, signature);
    }
}
