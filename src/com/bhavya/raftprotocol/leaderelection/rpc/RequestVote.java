package com.bhavya.raftprotocol.leaderelection.rpc;

public class RequestVote implements RaftMessage {
    private int term;
    private int candidateId;
    private int lastLogIndex;
    private int lastLogTerm;

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.REQUEST_VOTE;
    }
}
