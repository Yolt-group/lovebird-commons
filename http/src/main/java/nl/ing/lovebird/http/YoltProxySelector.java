package nl.ing.lovebird.http;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * We don't want to use the proxy for internal requests. This class makes sure we don't. It checks if
 * a request not internal, and if so adds the proxy.
 */
@Slf4j
public class YoltProxySelector extends ProxySelector {

    private final List<Proxy> noProxyList;
    private final List<Proxy> proxyList;


    public YoltProxySelector(final String ispProxyHost, final Integer ispProxyPort) {
        noProxyList = Collections.singletonList(Proxy.NO_PROXY);
        if (ispProxyHost != null && ispProxyHost.trim().length() > 0 && ispProxyPort != null && ispProxyPort > 0) {
            proxyList = Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ispProxyHost, ispProxyPort)));
        } else {
            log.warn("No proxy configured for external connections");
            proxyList = Collections.singletonList(Proxy.NO_PROXY);
        }
    }

    @Override
    public List<Proxy> select(final URI uri) {
        if (InternalRequestHelper.isInternalRequest(uri.getHost())) {
            return noProxyList;
        } else {
            return proxyList;
        }
    }

    @Override
    public void connectFailed(final URI uri, final SocketAddress sa, final IOException e) {
        log.error("Connection failed.", e);
    }
}
