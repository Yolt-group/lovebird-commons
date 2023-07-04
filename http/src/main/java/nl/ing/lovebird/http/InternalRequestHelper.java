package nl.ing.lovebird.http;

public class InternalRequestHelper {

    private static final String INTERNAL_DOMAIN_SUFFIX = ".cluster.local";

    static boolean isInternalRequest(final String host) {
        // It's an internal request if ends up with our internal domain suffix or it's a service name, no dots (e.g. 'kyc')
        return host.endsWith(INTERNAL_DOMAIN_SUFFIX) || !host.contains(".");
    }

}
