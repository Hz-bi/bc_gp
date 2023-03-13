import java.io.*;
import java.net.*;
import java.util.*;
public class BlockchainMiner {
    public static void main(String[] args) throws IOException {
        int numPorts = 3; // number of ports to create
        int startingPort = 5000; // starting port number

        // create a thread for each port
        for (int i = 0; i < numPorts; i++) {
            int port = startingPort + i;
            Thread t = new Thread(new Miner(port));
            t.start();
        }
    }
}
class Miner implements Runnable {
    private int port;

    public Miner(int port) {
        this.port = port;
    }

    public void run() {
        try {
            // create server socket and listen for connections
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Miner listening on port " + port);

            // accept connections and start a new thread for each client
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}
class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            // handle client input and output
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // read client input and broadcast to all clients
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Message received on port " + clientSocket.getPort() + ": " + inputLine);
                out.println("Message broadcasted from port " + clientSocket.getPort() + ": " + inputLine);
            }

            // close client connection
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Exception caught when trying to handle client input/output on port " + clientSocket.getPort());
            System.out.println(e.getMessage());
        }
    }
}

