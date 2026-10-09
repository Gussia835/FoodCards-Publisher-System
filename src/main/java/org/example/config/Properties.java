package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.filespaths")
@Getter
@Setter
public class Properties {
    private String processDir;
    private String targetDir;
}
