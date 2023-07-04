package nl.ing.lovebird.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YoltProxySelectorTest {

    private YoltProxySelector selector;
    private URI proxyUri;
    private URI nonProxyUri;

    @BeforeEach
    void before() throws Exception {
        this.proxyUri = new URI("https://external.example.com");
        this.nonProxyUri = new URI("https://internal.cluster.local");

        selector = new YoltProxySelector("proxy", 12345);
    }

    @Test
    void testNoProxyRequired() {
        final List<Proxy> proxies = selector.select(nonProxyUri);
        assertThat(proxies).containsExactly(Proxy.NO_PROXY);
    }

    @Test
    void testNullProxyConfig() {
        selector = new YoltProxySelector(null, null);
        final List<Proxy> proxies = selector.select(proxyUri);
        assertThat(proxies).containsExactly(Proxy.NO_PROXY);
    }

    @Test
    void testEmptyProxyConfig() {
        selector = new YoltProxySelector("", 0);
        final List<Proxy> proxies = selector.select(proxyUri);
        assertThat(proxies).containsExactly(Proxy.NO_PROXY);
    }

    @Test
    void testProxyRequired() {
        final List<Proxy> proxies = selector.select(proxyUri);
        assertThat(proxies).hasSize(1);
        final Proxy proxy = proxies.get(0);
        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);
        InetSocketAddress address = (InetSocketAddress) proxy.address();
        assertThat(address.getHostName()).isEqualTo("proxy");
        assertThat(address.getPort()).isEqualTo(12345);
    }
}