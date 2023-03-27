package com.groupProject.Network;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class P2PNetwork {

    private List<Node> nodes;
    private int numNodes;
    private ExecutorService executor;

    public P2PNetwork(int numNodes) {
        this.numNodes = numNodes;
        this.nodes = new ArrayList<Node>();
        this.executor = Executors.newFixedThreadPool(numNodes);

        // create and add nodes to the network
        for (int i = 0; i < numNodes; i++) {
            Node node = new Node("localhost", 8000 + i);
            nodes.add(node);
        }

        // connect each node to all other nodes
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    nodes.get(i).connectToNode(nodes.get(j));
                }
            }
        }

    }

    public void start() {
        // start each node in its own thread
        for (Node node : nodes) {
            executor.execute((Runnable) node);
        }
    }

    public void stop() {
        // stop all nodes
        for (Node node : nodes) {
            node.stop();
        }

        // shutdown the thread pool
        executor.shutdown();
    }

    public List<Node> getNodes() {
        return nodes;
    }
    public void addNode(Node node) {
        // add node to the network
        nodes.add(node);
        // connect new node to all other nodes
        for (Node other : nodes) {
            if (node != other) {
                node.connectToNode(other);
            }
        }
    }

    public int getNumNodes() {
        return numNodes;
    }
}
