package com.bhavya.raftprotocol.leaderelection;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpConnection {
    private String host;
    private int port;

    public TcpConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessage(String message) {
        try (Socket socket = new Socket(host, port)) {
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(message + "\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
