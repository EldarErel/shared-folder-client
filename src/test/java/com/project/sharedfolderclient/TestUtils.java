package com.project.sharedfolderclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.http.*;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.params.HttpParams;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
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

    public static org.apache.http.HttpResponse createHttpResponse(Object body) {
        return new org.apache.http.HttpResponse() {
            @Override
            public StatusLine getStatusLine() {
             return   new StatusLine() {
                    @Override
                    public ProtocolVersion getProtocolVersion() {
                        return null;
                    }

                    @Override
                    public int getStatusCode() {
                        return 200;
                    }

                    @Override
                    public String getReasonPhrase() {
                        return null;
                    }
                };
            }

            @Override
            public void setStatusLine(StatusLine statusline) {
            }

            @Override
            public void setStatusLine(ProtocolVersion ver, int code) {
            }

            @Override
            public void setStatusLine(ProtocolVersion ver, int code, String reason) {
            }

            @Override
            public void setStatusCode(int code) throws IllegalStateException {
            }

            @Override
            public void setReasonPhrase(String reason) throws IllegalStateException {
            }

            @Override
            @SneakyThrows
            public HttpEntity getEntity() {
               return EntityBuilder.create()
                        .setText(JSON.objectMapper.writeValueAsString(body))
                       .build();

            }

            @Override
            public void setEntity(HttpEntity entity) {
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public void setLocale(Locale loc) {
            }

            @Override
            public ProtocolVersion getProtocolVersion() {
                return null;
            }

            @Override
            public boolean containsHeader(String name) {
                return false;
            }

            @Override
            public Header[] getHeaders(String name) {
                return new Header[0];
            }

            @Override
            public Header getFirstHeader(String name) {
                return null;
            }

            @Override
            public Header getLastHeader(String name) {
                return null;
            }

            @Override
            public Header[] getAllHeaders() {
                return new Header[0];
            }

            @Override
            public void addHeader(Header header) {
            }

            @Override
            public void addHeader(String name, String value) {
            }

            @Override
            public void setHeader(Header header) {
            }

            @Override
            public void setHeader(String name, String value) {
            }

            @Override
            public void setHeaders(Header[] headers) {
            }

            @Override
            public void removeHeader(Header header) {
            }

            @Override
            public void removeHeaders(String name) {
            }

            @Override
            public HeaderIterator headerIterator() {
                return null;
            }

            @Override
            public HeaderIterator headerIterator(String name) {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public void setParams(HttpParams params) {
            }
        };
    }
}
