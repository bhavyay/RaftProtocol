package com.bhavya.raftprotocol.leaderelection;

import java.io.*;
import java.net.Socket;

public class TcpConnection {
    private String host;
    private int port;

    public TcpConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void sendMessage(String message) {
        try {
            Socket socket = new Socket(host, port);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(message + "\n");
            dataOutputStream.flush();
            System.out.println("Message sent to server " + host + ":" + port + " is " + message);
            String response = bufferedReader.readLine();
            System.out.println("Response from server " + host + ":" + port + " is " + response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
