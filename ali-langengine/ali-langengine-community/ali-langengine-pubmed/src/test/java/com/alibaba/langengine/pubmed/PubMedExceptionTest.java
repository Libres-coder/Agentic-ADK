package com.alibaba.langengine.pubmed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PubMedExceptionTest {

    @Test
    void testMessageConstructor() {
        String message = "Test exception message";
        PubMedException exception = new PubMedException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Test exception message";
        Throwable cause = new RuntimeException("Cause exception");
        PubMedException exception = new PubMedException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testCauseConstructor() {
        String message = "Cause exception message";
        Throwable cause = new RuntimeException("Cause exception");
        PubMedException exception = new PubMedException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}
