package com.yolt.service.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@ConditionalOnProperty(name = "yolt.service.compliance-gdpr.enabled", matchIfMissing = false) // needs explicit enabling
@AutoConfiguration
@ComponentScan("com.yolt.compliance.gdpr")
public class GDPRClientAutoConfiguration {
    // Annotations only
}
