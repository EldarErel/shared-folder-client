package com.project.sharedfolderclient.v1.utils.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
/**
 * Http configurations
 */
public class HttpConfiguration {

    @Bean// http client init
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
