package com.bhavya.raftprotocol.leaderelection.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestVote {
    private int term;
    private int candidateId;
    private int lastLogIndex;
    private int lastLogTerm;
}
