package com.bhavya.raftprotocol.leaderelection.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestVoteResponse {
    private final int term;
    private boolean voteGranted;
    private int voterId;
}
