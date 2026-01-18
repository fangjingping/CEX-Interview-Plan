package com.cex.exchange.demo.persistence;

public interface EventLog {
    void append(Event event);

    EventLogReadResult readAll();
}
