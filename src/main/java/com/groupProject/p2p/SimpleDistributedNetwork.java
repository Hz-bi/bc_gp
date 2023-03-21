package com.groupProject.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import com.groupProject.block.Block;
import com.groupProject.block.Blockchain;
import com.groupProject.utils.RocksDBUtils;

public class SimpleDistributedNetwork {

    private static final int PORT = 8080; // 定义一个端口用于网络通信
    private static List<Node> nodeList = new ArrayList<>(); // 用于保存所有节点信息
    private static List<Block> blockList = new ArrayList<>(); // 用于保存所有区块信息

    public static void main(String[] args) throws IOException {

        // 启动服务器
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server started on port " + PORT);

        // 循环监听客户端连接请求
        while (true) {
            Socket clientSocket = serverSocket.accept(); // 等待客户端连接
            System.out.println("Accepted connection from " + clientSocket);

            // 创建一个新的线程处理客户端请求
            new Thread(() -> {
                try {
                    ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream());
                    ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

                    // 接收客户端消息
                    String message = (String) inputStream.readObject();
                    System.out.println("Received message: " + message);

                    // 处理连接请求
                    if (message.equals("connect")) {
                        // 创建新节点
                        Node newNode = new Node(clientSocket.getInetAddress().toString(), clientSocket.getPort());
                        nodeList.add(newNode);
                        System.out.println("New node connected: " + newNode);

                        // 向新节点发送最新区块链信息
                        outputStream.writeObject(blockList);

                        // 挖掘新的区块
                        Blockchain blockchain = new Blockchain();
//                        Block newBlock = blockchain.mineBlock(blockList.get(blockList.size() - 1).getTransactions());
                        blockchain.mineBlock(blockList.get(blockList.size() - 1).getTransactions());
//                        blockList.add(newBlock);
                        String lastBlockHash = RocksDBUtils.getInstance().getLastBlockHash();
                        Block lastBlock = RocksDBUtils.getInstance().getBlock(lastBlockHash);
                        System.out.println("New block mined: " + lastBlock);

                        // 广播新的区块信息
                        broadcast(lastBlock);
                    }

                    // 关闭连接
                    inputStream.close();
                    outputStream.close();
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // 广播区块信息给所有节点
    private static void broadcast(Block block) throws IOException {
        for (Node node : nodeList) {
            Socket socket = new Socket(node.getHost(), node.getPort());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(block);
            socket.close();
        }
    }

    // 节点类
    private static class Node {
        private String host;
        private int port;

        public Node(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }
    }
}
