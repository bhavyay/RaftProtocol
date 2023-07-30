package com.bhavya.tcpcomm;

import java.io.*;
import java.net.Socket;

public class TcpClient {

    private Socket socket;
    private String id;

    public TcpClient(String id) {
        this.id = id;
    }

    public void connect(String destinationHost, int destinationPort) {
        try {
            this.socket = new Socket(destinationHost, destinationPort);
            InputStream inputStream = socket.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            OutputStream outputStream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            System.out.println("Connected to " + destinationHost + ":" + destinationPort);

            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String message = consoleReader.readLine();
                if (message.equalsIgnoreCase("QUIT")) {
                    dataOutputStream.writeBytes("QUIT\n");
//                    objectOutputStream.writeObject(new Message("QUIT", id));
                    socket.close();
                    break;
                }
                dataOutputStream.writeBytes(message + " from " + id + "\n");
                dataOutputStream.flush();
//                objectOutputStream.writeObject(new Message(message, id));
//                Message response = (Message) objectInputStream.readObject();
                String response = bufferedReader.readLine();
                System.out.println("Received response: " + response);
//                System.out.println("Received response: " + response.getMessage() + " from " + response.getSender());
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
