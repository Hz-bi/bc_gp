package com.groupProject.Network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.utils.RocksDBUtils;

public class Node {

    private String address;
    private int port;
    private static ArrayList<Node> network;
    private TransactionPool transactionPool;
    private Blockchain blockchain;

    public Node(String address, int port) {
        this.address = address;
        this.port = port;
        this.network = new ArrayList<Node>();
        this.transactionPool = new TransactionPool();
    }

    public void joinNetwork(Blockchain blockchain) {
        // Connect to the network
        for (Node node : network) {
            try {
                Socket socket = new Socket(InetAddress.getByName(node.getAddress()), node.getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                // Send the latest block
                String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
                Block lastBlock = RocksDBUtils.getInstance().getBlock(lastBlockHash);
                out.writeObject(lastBlock);
                out.flush();

                // Receive the response
                boolean response = (boolean) in.readObject();

                if (response) {
                    System.out.println("Connected to " + node.getAddress() + ":" + node.getPort());
                }

                // Close the connection
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to connect to " + node.getAddress() + ":" + node.getPort());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        // Add itself to the network
        network.add(this);

        // Save the blockchain reference
        this.blockchain = blockchain;
    }
    public void connectToNode(Node other) {
        // join the network of the other node
        other.joinNetwork(this.blockchain);
        // add the other node to the network
        this.network.add(other);
    }
    public static void broadcastBlock(Block block) {
        // Send the block to all connected nodes
        for (Node node : network) {
            try {
                Socket socket = new Socket(InetAddress.getByName(node.getAddress()), node.getPort());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeObject(block);
                out.flush();

                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to broadcast block to " + node.getAddress() + ":" + node.getPort());
            }
        }
    }




    public Blockchain getBlockchain() {
        return this.blockchain;
    }

    public void stop() {
        // stop the node's blockchain and transaction pool
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public void addBlock(Block block) throws Exception {
        this.blockchain.addBlock(block); // 调用Blockchain实例的addBlock方法
    }

    public TransactionPool getTransactionPool() {
        return transactionPool;
    }
}
