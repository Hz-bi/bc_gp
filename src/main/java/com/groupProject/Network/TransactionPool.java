package com.groupProject.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionPool {
    private Map<String, NetworkTransaction> pool;

    public TransactionPool() {
        pool = new HashMap<String, NetworkTransaction>();
    }

    public String getTransactionId(NetworkTransaction transaction) {
        return transaction.serialize();
    }

    public void addTransaction(NetworkTransaction tx) {
        pool.put(tx.serialize(), tx);
    }

    public void removeTransaction(String txId) {
        pool.remove(txId);
    }

    public List<NetworkTransaction> getTransactions() {
        return new ArrayList<NetworkTransaction>(pool.values());
    }

    public boolean containsTransaction(String txId) {
        return pool.containsKey(txId);
    }

    public NetworkTransaction getTransaction(String txId) {
        return pool.get(txId);
    }

    public void clear() {
        pool.clear();
    }
}
