package com.project.sharedfolderclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class TestUtils {
    @Data
    public static class CaseObject {
        private JsonNode preRequest;
        private JsonNode request;
        private JsonNode response;
    }

    public static CaseObject generateCaseObject(String path, String fileName) throws IOException {
       Resource resource = new ClassPathResource(String.format("%s/%s.json",path,fileName));
      return JSON.objectMapper.readValue(resource.getFile(), CaseObject.class);
    }

    public static HttpResponse<String> createHttpResponse(Object body) {
        return new HttpResponse<String>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @SneakyThrows
            @Override
            public String body() {
                return JSON.objectMapper.writeValueAsString(body);
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }
}
