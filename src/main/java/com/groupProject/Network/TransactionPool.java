package com.groupProject.Network;

import com.groupProject.transaction.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class TransactionPool {
//    private Map<String, NetworkTransaction> pool;

    private Queue<Transaction> pool = new LinkedList<>();

    public TransactionPool() {
//        pool = new HashMap<String, NetworkTransaction>();
    }

//    public String getTransactionId(NetworkTransaction transaction) {
//        return transaction.serialize();
//    }

//    public void addTransaction(NetworkTransaction tx) {
//        pool.put(tx.serialize(), tx);
//    }

    public void addTransaction(Transaction tx) {
        pool.offer(tx);
    }

    public void removeTransaction() {
        pool.peek();
    }


//    public List<NetworkTransaction> getTransactions() {
//        return new ArrayList<NetworkTransaction>(pool.values());
//    }
//
//    public boolean containsTransaction(String txId) {
//        return pool.containsKey(txId);
//    }

//    public NetworkTransaction getTransaction(String txId) {
//        return pool.get(txId);
//    }
//
//    public void clear() {
//        pool.clear();
//    }
}
