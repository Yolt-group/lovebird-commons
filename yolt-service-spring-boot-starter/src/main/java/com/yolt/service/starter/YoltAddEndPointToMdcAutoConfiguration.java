package com.yolt.service.starter;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static nl.ing.lovebird.logging.MDCContextCreator.ENDPOINT_MDC_KEY;
import static nl.ing.lovebird.logging.MDCContextCreator.METHOD_KEY;
import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfiguration
@RequiredArgsConstructor
public class YoltAddEndPointToMdcAutoConfiguration implements WebMvcConfigurer {

    private final AddEndPointToMdcInterceptor addEndPointToMdcInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(addEndPointToMdcInterceptor);
    }

    @Component
    static final class AddEndPointToMdcInterceptor implements AsyncHandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            additionalMdcEntries(request).forEach(MDC::put);
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
            additionalMdcEntries(request).keySet().forEach(MDC::remove);
        }

        private Map<String, String> additionalMdcEntries(final HttpServletRequest request) {
            Map<String, String> additionalEntries = new HashMap<>();
            additionalEntries.put(METHOD_KEY, request.getMethod());
            additionalEntries.put(ENDPOINT_MDC_KEY, geBestMatchingPatternOrRequestUri(request));
            return additionalEntries;
        }

        private String geBestMatchingPatternOrRequestUri(HttpServletRequest request) {
            final String matchedPath = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (matchedPath != null) {
                return matchedPath;
            }
            return request.getRequestURI();
        }
    }

}
