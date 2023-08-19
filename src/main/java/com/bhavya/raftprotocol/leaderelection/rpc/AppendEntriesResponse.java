package com.bhavya.raftprotocol.leaderelection.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AppendEntriesResponse {
    private int term;
    private boolean success;
}
