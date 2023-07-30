package com.bhavya.tcpcomm;

public class TcpClient2 {

    public static void main(String[] args) {
        TcpClient tcpClient = new TcpClient("client2");
        tcpClient.connect("localhost", 8000);
    }
}
