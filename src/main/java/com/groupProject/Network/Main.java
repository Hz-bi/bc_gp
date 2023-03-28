package com.groupProject.Network;

import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.groupProject.Network.Node;
import com.groupProject.Network.P2PNetwork;
import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.Network.NetworkTransaction;
import com.groupProject.Network.TransactionPool;
import com.groupProject.utils.RocksDBUtils;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static P2PNetwork network;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
        Blockchain blockchain = Blockchain.newBlockchain(RocksDBUtils.getInstance().getLastBlockHash());
        TransactionPool transactionPool = new TransactionPool();

        // create a P2P network with 5 nodes
        network = new P2PNetwork(5);

        // start mining blocks
        executor.execute(() -> {
            while (true) {
                // create a new block
                Block block = null;
                try {
                    block = blockchain.mineBlock(transactionPool.getTransactions());

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                // add the block to the blockchain
                try {
                    blockchain.addBlock(block);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // clear the transaction pool
                transactionPool.clear();

                // broadcast the new block to the network
                Node.broadcastBlock(block);

            }
        });

        // start accepting new transactions
        executor.execute(() -> {
            while (true) {
                System.out.print("Enter transaction amount: ");
                double amount = scanner.nextDouble();
                NetworkTransaction transaction = new NetworkTransaction("Alice", "Bob", amount);
                transactionPool.addTransaction(transaction);
                System.out.println("Transaction added to pool.");
            }
        });

        // start listening for incoming connections
        executor.execute(() -> {
            network.start();
        });

        // connect to new node
        while (true) {
            System.out.println("Enter command: ");
            String command = scanner.nextLine();
            if (command.equals("connect")) {
                Node node = new Node("localhost", network.getNumNodes());
                node.joinNetwork(blockchain);
                network.addNode(node);
            }
        }
    }
}
