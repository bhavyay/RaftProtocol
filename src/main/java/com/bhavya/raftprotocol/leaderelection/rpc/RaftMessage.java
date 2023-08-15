package com.bhavya.raftprotocol.leaderelection.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
@AllArgsConstructor
@Data
public class RaftMessage {
    private String type;
    private String message;
}
