package com.bhavya.raftprotocol.leaderelection;

import com.bhavya.raftprotocol.leaderelection.rpc.ClusterInfoUpdate;
import com.bhavya.raftprotocol.leaderelection.rpc.RaftMessage;
import com.bhavya.raftprotocol.leaderelection.rpc.RaftMessageType;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RaftCluster {
    private final List<RaftPeer> peers;

    public RaftCluster(List<RaftPeer> peers) {
        this.peers = peers;
    }

    public void updateClusterInfoToServer() {
        for (RaftPeer peer : peers) {
            System.out.println("Updating cluster info to server " + peer.getId());
            TcpConnection connection = new TcpConnection(peer.getHostName(), peer.getPort());
            ClusterInfoUpdate clusterInfoUpdate = new ClusterInfoUpdate("cluster", peers);
            String payload = new Gson().toJson(clusterInfoUpdate);
            RaftMessage raftMessage = new RaftMessage(RaftMessageType.CLUSTER_INFO_UPDATE.toString(), payload);
            String message = new Gson().toJson(raftMessage);
            connection.sendMessage(message);
        }
    }

    public void startRaftServers() {
        for (RaftPeer peer : peers) {
            System.out.println("Starting server " + peer.getId());
            TcpConnection connection = new TcpConnection(peer.getHostName(), peer.getPort());
            RaftMessage raftMessage = new RaftMessage(RaftMessageType.START_SERVER.toString(), "");
            String message = new Gson().toJson(raftMessage);
            connection.sendMessage(message);
        }
    }
}
