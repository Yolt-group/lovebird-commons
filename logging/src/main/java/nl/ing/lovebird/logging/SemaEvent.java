package nl.ing.lovebird.logging;

import org.slf4j.Marker;

/**
 * Use this interface to create your own SEMA events for the SemaEventLogger. These events will end up
 * in a separate ElasticSearch instance (not accessible from Kibana). You'll need to set the information
 * yourself in the form of logging markers. There is 1 marker set automatically: sema_type. The full class
 * name of the event is used for that.
 */
public interface SemaEvent {

    /**
     * A human readable message describing the event. For example: 'userx logged in from ip 123.456.789.000'
     * <p>
     * CAUTION: Do make sure you'll also set the variables within this message as logging markers!
     */
    String getMessage();

    /**
     * The logging markers for this event so they are searchable within ElasticSearch. For example:
     * username=userx
     * remoteAddress=123.456.789.000
     * <p>
     * CAUTION: Do make sure you'll also put the variables in the (human readable) message!
     */
    Marker getMarkers();
}
