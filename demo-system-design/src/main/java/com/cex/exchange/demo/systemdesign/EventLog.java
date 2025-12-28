package com.cex.exchange.demo.systemdesign;

import java.util.List;

/**
 * EventLog 接口定义。
 */
public interface EventLog {
    StoredEvent append(DomainEvent event);

    List<StoredEvent> readFrom(long sequenceExclusive);
}
