package com.gateway.socket;

import org.zeromq.ZMQ;

public final class ZmqContextHolder {
    private static ZMQ.Context context;

    private ZmqContextHolder() {}

    public static ZMQ.Context getContext() {
        if (context == null) {
            synchronized (ZmqContextHolder.class) {
                if (context == null) {
                    context = ZMQ.context(1);
                }
            }
        }
        return context;
    }
}
