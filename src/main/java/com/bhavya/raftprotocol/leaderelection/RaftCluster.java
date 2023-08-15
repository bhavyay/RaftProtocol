package com.bhavya.raftprotocol.leaderelection;

import com.bhavya.raftprotocol.leaderelection.rpc.ClusterInfoUpdate;
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
            ClusterInfoUpdate clusterInfoUpdate = new ClusterInfoUpdate("CLUSTER_INFO_UPDATE", peers);
            String message = new Gson().toJson(clusterInfoUpdate);
            connection.sendMessage(message);
        }
    }
}
