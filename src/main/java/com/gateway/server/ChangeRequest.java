package com.gateway.server;

import java.nio.channels.SocketChannel;

class ChangeRequest {
    static final int REGISTER = 1;
    static final int CHANGER = 2;

    private SocketChannel socket;
    private int type;
    private int ops;

    ChangeRequest(SocketChannel socket, int type, int ops) {
        this.socket = socket;
        this.type = type;
        this.ops = ops;
    }

    SocketChannel getSocket() {
        return socket;
    }

    int getType() {
        return type;
    }

    int getOps() {
        return ops;
    }
}
