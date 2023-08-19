package com.bhavya.raftprotocol.leaderelection.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AppendEntries {
    private int term;
    private int leaderId;
    private int prevLogIndex;
    private int prevLogTerm;
    private String[] entries;
    private int leaderCommit;

    public AppendEntries(int term, int leaderId) {
        this.term = term;
        this.leaderId = leaderId;
    }
}
