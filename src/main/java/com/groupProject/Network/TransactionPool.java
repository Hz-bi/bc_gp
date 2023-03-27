package com.groupProject.Network;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransactionPool {
    private Map<String, Transaction> pool;

    public TransactionPool() {
        pool = new HashMap<String, Transaction>();
    }

    public void addTransaction(Transaction tx) {
        pool.put(tx.getTransactionId(), tx);
    }


    public void removeTransaction(String txId) {
        pool.remove(txId);
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<Transaction>(pool.values());
    }

    public boolean containsTransaction(String txId) {
        return pool.containsKey(txId);
    }

    public Transaction getTransaction(String txId) {
        return pool.get(txId);
    }

    public void clear() {
        pool.clear();
    }
}
