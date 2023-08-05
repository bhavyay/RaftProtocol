package com.bhavya.tcpcomm;

import java.io.*;
import java.net.Socket;

public class TcpHandler extends Thread {

    private Socket socket;

    public TcpHandler(Socket clientSocket) {
        this.socket = clientSocket;
    }

    public void run() {
        InputStream inp = null;
        ObjectInputStream in = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            in = new ObjectInputStream(inp);
            out = new DataOutputStream(socket.getOutputStream());
//            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }

        String line;
        while (true) {
            try {
                line = brinp.readLine();
                System.out.println("Received " + line);
                if (line == null || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    return;
                } else {
                    out.writeBytes(line + " from server\n");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
