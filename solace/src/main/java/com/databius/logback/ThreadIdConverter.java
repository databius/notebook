package com.databius.logback;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ThreadIdConverter extends ClassicConverter {
    private static final ThreadLocal<String> threadId = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return String.format("%05d", Thread.currentThread().getId());
        }
    };

    @Override
    public String convert(ILoggingEvent event) {
        return threadId.get();
    }
}