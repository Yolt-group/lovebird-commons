package com.yolt.teamstarter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "yolt.team.starter")
@Data
public class TeamStarterProperties {

    private String environment;
    private String kubernetesContext;
    private String namespace;
    private String applicationName;
    private String containerName;
}
