package com.bhavya.raftprotocol.leaderelection.rpc;

import java.io.Serializable;

public interface RaftMessage extends Serializable {
    public RaftMessageType getType();
}
