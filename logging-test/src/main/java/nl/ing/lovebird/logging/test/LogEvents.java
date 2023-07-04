package nl.ing.lovebird.logging.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * See CaptureLogEventsDemoTest for usage.
 */
public class LogEvents implements Appender<ILoggingEvent> {

    private final Deque<ILoggingEvent> events = new ConcurrentLinkedDeque<>();

    public Stream<ILoggingEvent> stream() {
        return this.events.stream();
    }

    public Stream<ILoggingEvent> stream(Level level) {
        requireNonNull(level);
        return stream().filter(logRecord -> logRecord.getLevel() == level);
    }

    public Stream<ILoggingEvent> stream(Class<?> clazz) {
        requireNonNull(clazz);
        return stream().filter(logRecord -> logRecord.getLoggerName().equals(clazz.getName()));
    }

    public Stream<ILoggingEvent> stream(Class<?> clazz, Level level) {
        requireNonNull(level);
        return stream(clazz).filter(logRecord -> logRecord.getLevel() == level);
    }

    public void clear() {
        this.events.clear();
    }


    @Override
    public void doAppend(ILoggingEvent event) throws LogbackException {
        events.add(event);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void addStatus(Status status) {

    }

    @Override
    public void addInfo(String msg) {

    }

    @Override
    public void addInfo(String msg, Throwable ex) {

    }

    @Override
    public void addWarn(String msg) {

    }

    @Override
    public void addWarn(String msg, Throwable ex) {

    }

    @Override
    public void addError(String msg) {

    }

    @Override
    public void addError(String msg, Throwable ex) {

    }

    @Override
    public void addFilter(Filter<ILoggingEvent> newFilter) {

    }

    @Override
    public void clearAllFilters() {

    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return Collections.emptyList();
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return FilterReply.NEUTRAL;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
