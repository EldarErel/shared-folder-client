package com.project.sharedfolderclient.v1.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class ApplicationProperties {
    private Server server;

    @Data
    public static class Server {
        private String url;
        private String apiPath;
    }
}
