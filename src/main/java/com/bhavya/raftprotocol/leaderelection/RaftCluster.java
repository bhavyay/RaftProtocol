package com.bhavya.raftprotocol.leaderelection;

import java.util.ArrayList;
import java.util.List;

public class RaftCluster {
    private final List<RaftPeer> peers;

    public RaftCluster(List<RaftPeer> peers) {
        this.peers = peers;
    }

    public void updateClusterInfoToServer() {
        for (RaftPeer peer : peers) {
            TcpConnection connection = new TcpConnection(peer.getHostName(), peer.getPort());
            connection.sendMessage("type=clusterInfo;servers=" + getClusterInfo(peer.getId()));
        }
    }

    private String getClusterInfo(int id) {
        StringBuilder clusterInfo = new StringBuilder();
        for (RaftPeer peer : peers) {
            if (peer.getId() == id) {
                continue;
            }
            clusterInfo.append("id=").append(peer.getId()).append(";");
            clusterInfo.append("hostName=").append(peer.getHostName()).append(";");
            clusterInfo.append("port=").append(peer.getPort()).append(";");
            clusterInfo.append("|");
        }
        return clusterInfo.toString();
    }
}
