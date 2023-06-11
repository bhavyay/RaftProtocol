package com.bhavya.raftprotocol.leaderelection.rpc;

public class RequestVoteResponse implements RaftMessage {
    private final int term;
    private boolean voteGranted;

    private int voterId;

    public RequestVoteResponse(int voterId, int term) {
        this.voterId = voterId;
        this.term = term;
    }

    @Override
    public RaftMessageType getType() {
        return RaftMessageType.REQUEST_VOTE_RESPONSE;
    }

    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void acceptVote() {
        this.voteGranted = true;
    }

    public void rejectVote() {
        this.voteGranted = false;
    }

    public int getVoterId() {
        return voterId;
    }
}
