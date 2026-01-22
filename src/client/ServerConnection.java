package client;

import common.Message;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;

    public ServerConnection(String host, int port) throws Exception {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public Message read() throws Exception {
        String line = in.readLine();
        if (line == null) return null;
        return Message.decode(line);
    }

    public void send(Message msg) {
        out.println(msg.encode());
    }

    public void close() {
        try { socket.close(); } catch (Exception ignored) {}
    }
}
