package com.yolt.service.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:application-versions.properties")
public class YoltVersionContributorAutoConfiguration {

}
