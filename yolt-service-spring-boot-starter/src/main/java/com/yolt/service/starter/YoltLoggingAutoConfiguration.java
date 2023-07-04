package com.yolt.service.starter;

import nl.ing.lovebird.logging.AuditLogger;
import nl.ing.lovebird.logging.SemaEventLogger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class YoltLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SemaEventLogger.class)
    public SemaEventLogger semaEventLogger() {
        return new SemaEventLogger();
    }

    @Bean
    @ConditionalOnMissingBean(AuditLogger.class)
    public AuditLogger auditLogger() {
        return new AuditLogger();
    }


}
