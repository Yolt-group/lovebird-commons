package com.yolt.compliance.gdpr.client.config;

import com.yolt.compliance.gdpr.client.messaging.Messaging;
import com.yolt.compliance.gdpr.client.messaging.consumer.GDPRDataRequestConsumer;
import com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReplyProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackageClasses = Messaging.class,
        useDefaultFilters = false,
        includeFilters = {
                @Filter(type = ASSIGNABLE_TYPE, value = {GDPRDataRequestConsumer.class, GDPRDataReplyProducer.class})
        })
@EnableKafka
@Slf4j
public class GDPRClientConfiguration {

}
