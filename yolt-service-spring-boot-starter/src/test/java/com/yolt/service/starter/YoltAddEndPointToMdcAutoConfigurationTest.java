package com.yolt.service.starter;

import com.yolt.service.starter.YoltAddEndPointToMdcAutoConfiguration.AddEndPointToMdcInterceptor;
import nl.ing.lovebird.logging.MDCContextCreator;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class YoltAddEndPointToMdcAutoConfigurationTest {

    @Test
    void enabledInWebApplicationContext() {
        WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(YoltAddEndPointToMdcAutoConfiguration.class));
        contextRunner.run(context -> assertThat(context).hasSingleBean(YoltAddEndPointToMdcAutoConfiguration.class));
    }

    @Test
    void disabledWithoutWebApplicationContext() {
        ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(YoltAddEndPointToMdcAutoConfiguration.class));
        contextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(YoltAddEndPointToMdcAutoConfiguration.class));
    }

    @Test
    void configurerRegistersInterceptor() {
        class StubInterceptorRegistry extends InterceptorRegistry {
            @Override
            public List<Object> getInterceptors() {
                return super.getInterceptors();
            }
        }
        StubInterceptorRegistry interceptorRegistry = new StubInterceptorRegistry();

        AddEndPointToMdcInterceptor bean = new AddEndPointToMdcInterceptor();
        YoltAddEndPointToMdcAutoConfiguration configuration = new YoltAddEndPointToMdcAutoConfiguration(bean);
        configuration.addInterceptors(interceptorRegistry);
        assertThat(interceptorRegistry.getInterceptors()).contains(bean);
    }

    @Test
    void interceptorAddsKeysToTheMdc() throws Exception {
        AddEndPointToMdcInterceptor bean = new AddEndPointToMdcInterceptor();

        HttpServletRequest request = new MockHttpServletRequest("PUT", "/example");
        HttpServletResponse response = new MockHttpServletResponse();
        Object handler = new Object();

        assertThat(MDC.get(MDCContextCreator.METHOD_KEY)).isNull();
        assertThat(MDC.get(MDCContextCreator.ENDPOINT_MDC_KEY)).isNull();
        bean.preHandle(request, response, handler);
        assertThat(MDC.get(MDCContextCreator.METHOD_KEY)).isEqualTo("PUT");
        assertThat(MDC.get(MDCContextCreator.ENDPOINT_MDC_KEY)).isEqualTo("/example");
        bean.afterCompletion(request, response, handler, null);
        assertThat(MDC.get(MDCContextCreator.METHOD_KEY)).isNull();
        assertThat(MDC.get(MDCContextCreator.ENDPOINT_MDC_KEY)).isNull();
    }
}
