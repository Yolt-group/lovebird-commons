package nl.ing.lovebird.logging;

/**
 * This class generates the initial MDC context. It snoops the headers from the http-request and puts the important stuff on the MDC context.
 */
public class MDCContextCreator {

    public static final String USER_ID_HEADER_NAME = "user-id";
    /**
     * @deprecated will be dropped with sleuth 2.1.0. That will just propagate the 'propagation-keys' on both a sleuth threadlocal and to the
     * MDC. That will be just 1 key, and not a different key for an MDC property compared to what's on the span-baggage.
     */
    @Deprecated
    public static final String USER_ID_MDC_KEY = USER_ID_HEADER_NAME;

    /**
     * @deprecated will be dropped with sleuth 2.1.0. That will just propagate the 'propagation-keys' on both a sleuth threadlocal and to the
     * MDC. That will be just 1 key, and not a different key for an MDC property compared to what's on the span-baggage.
     */
    @Deprecated
    public static final String CLIENT_USER_ID_MDC_KEY = "client_user_id";
    public static final String CLIENT_USER_ID_HEADER_NAME = "client-user-id";

    /**
     * @deprecated will be dropped with sleuth 2.1.0. That will just propagate the 'propagation-keys' on both a sleuth threadlocal and to the
     * MDC. That will be just 1 key, and not a different key for an MDC property compared to what's on the span-baggage.
     */
    @Deprecated
    public static final String CLIENT_ID_MDC_KEY = "client_id";
    public static final String CLIENT_ID_HEADER_NAME = "client-id";

    /**
     * @deprecated will be dropped with sleuth 2.1.0. That will just propagate the 'propagation-keys' on both a sleuth threadlocal and to the
     * MDC. That will be just 1 key, and not a different key for an MDC property compared to what's on the span-baggage.
     */
    @Deprecated
    public static final String PROFILE_ID_MDC_KEY = "profile_id";
    public static final String PROFILE_ID_HEADER_NAME = "cbms-profile-id";

    public static final String APP_VERSION_HEADER_NAME_AND_MDC_KEY = "app_version";
    public static final String ENDPOINT_MDC_KEY = "endpoint";
    public static final String METHOD_KEY = "method";

    public static final String USER_SITE_ID_MDC_KEY = "user_site_id";
    public static final String SITE_ID_MDC_KEY = "site_id";

}
