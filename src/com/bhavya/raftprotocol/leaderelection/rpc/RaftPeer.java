package com.bhavya.raftprotocol.leaderelection.rpc;

public class RaftPeer {
    private int id;
    private String hostName;
    private int port;

    public RaftPeer(int id, String hostName, int port) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }
}
