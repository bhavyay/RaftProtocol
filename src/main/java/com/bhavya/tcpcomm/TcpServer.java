package com.bhavya.tcpcomm;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    private int port;

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(8000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new TcpHandler(socket).start();
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        TcpServer tcpServer = new TcpServer(8000);
        tcpServer.start();
    }
}
