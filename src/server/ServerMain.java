package server;

import server.core.LogService;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) {

        int port = 5000;
        LogService logger = new LogService("server.log");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server running on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler =
                        new ClientHandler(clientSocket, logger);
                handler.start();
            }

        } catch (Exception e) {
            System.out.println("SERVER_FATAL: " + e.getMessage());
        }
    }
}
