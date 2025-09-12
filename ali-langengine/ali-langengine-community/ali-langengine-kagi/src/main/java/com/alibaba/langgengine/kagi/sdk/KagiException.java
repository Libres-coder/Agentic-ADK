package com.alibaba.langgengine.kagi.sdk;

public class KagiException extends RuntimeException {
    public KagiException(String message) {
        super(message);
    }

    public KagiException(String message, Throwable cause) {
        super(message, cause);
    }
}
