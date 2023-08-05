package com.bhavya.tcpcomm;

public class TcpClient1 {

    public static void main(String[] args) {
        TcpClient tcpClient = new TcpClient("client1");
        tcpClient.connect("localhost", 8000);
    }
}

