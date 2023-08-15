package com.bhavya.raftprotocol.leaderelection.rpc;

import com.bhavya.raftprotocol.leaderelection.RaftPeer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ClusterInfoUpdate {
    private String id;
    private List<RaftPeer> servers;
}
