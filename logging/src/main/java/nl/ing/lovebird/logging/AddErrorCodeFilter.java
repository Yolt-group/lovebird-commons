package nl.ing.lovebird.logging;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import net.logstash.logback.marker.LogstashMarker;
import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;

public class AddErrorCodeFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(final ILoggingEvent event) {

        LoggingEvent loggingEvent = (LoggingEvent) event;

        if (event.getLevel() == Level.ERROR) {
            LogstashMarker errorHashMarker = createMarker(event);
            Marker existingMarker = event.getMarker();
            if (existingMarker == null) {
                loggingEvent.setMarker(errorHashMarker);
            } else {
                existingMarker.add(errorHashMarker);
            }
        }

        return FilterReply.ACCEPT;
    }

    private LogstashMarker createMarker(final ILoggingEvent event) {
        String messageTemplate = event.getMessage();
        String unhashedCode = createUnhashedCode(event, messageTemplate);
        String errorHash = Integer.toHexString(unhashedCode.hashCode());
        return Markers.append("error_code", errorHash);
    }

    private String createUnhashedCode(final ILoggingEvent event, final String messageTemplate) {
        final String unhashedCode;
        if (event.getThrowableProxy() != null) {
            String exceptionClassName = event.getThrowableProxy().getClassName();
            unhashedCode = messageTemplate + exceptionClassName;
        } else {
            unhashedCode = messageTemplate;
        }
        return unhashedCode;
    }

}
