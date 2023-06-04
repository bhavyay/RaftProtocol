package com.bhavya.raftprotocol.leaderelection.rpc;

public class RequestVote implements RaftMessage {

    private int term;

    private int candidateId;
    private int lastLogIndex;
    private int lastLogTerm;

    public RequestVote(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.REQUEST_VOTE;
    }

    public int getTerm() {
        return term;
    }

    public int getCandidateId() {
        return candidateId;
    }
}
