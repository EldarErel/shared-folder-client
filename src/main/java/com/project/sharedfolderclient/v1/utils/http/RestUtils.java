package com.project.sharedfolderclient.v1.utils.http;

import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.http.HttpRequest;

public class RestUtils {
    @SneakyThrows
    public static HttpRequest createGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();
    }

    @SneakyThrows
    public static HttpRequest createDeleteRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(new URI(url))
                .DELETE()
                .build();
    }

    @SneakyThrows
    public static HttpRequest createPutRequest(String url, Object object) {
        return HttpRequest.newBuilder()
                .uri(new URI(url))
                .PUT(HttpRequest.BodyPublishers.ofString(JSON.objectMapper.writeValueAsString(object)))
                .build();
    }

    @SneakyThrows
    public static HttpRequest creatPostRequest(String url, Object object) {
        return HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(JSON.objectMapper.writeValueAsString(object)))
                .build();
    }
}
