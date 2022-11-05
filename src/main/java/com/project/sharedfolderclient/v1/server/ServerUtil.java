package com.project.sharedfolderclient.v1.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.project.sharedfolderclient.v1.server.exception.ServerConnectionError;
import com.project.sharedfolderclient.v1.utils.ApplicationProperties;
import com.project.sharedfolderclient.v1.utils.Constants;
import com.project.sharedfolderclient.v1.utils.error.Error;
import com.project.sharedfolderclient.v1.utils.http.Response;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.http.HttpClient;

import java.util.List;

import static com.project.sharedfolderclient.v1.exception.ErrorMessages.*;
import static java.util.stream.Collectors.joining;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerUtil {
    private final HttpClient client;
    private final ApplicationProperties appProperties;

    public String getApiPath() {
        return appProperties.getServer().getUrl() + appProperties.getServer().getApiPath();
    }


    private void handleErrors(Response body) {
        List<Error> errorList = body.getErrors();
        if (!CollectionUtils.isEmpty(errorList)) {
            log.error("Errors: {}", errorList);
            String errorMessages = errorList.stream()
                    .map(Error::getMessage)
                    .collect(joining(","));
            throw new ServerConnectionError(errorMessages);
        }

    }

    public HttpResponse exchange(HttpUriRequest requestEntity) throws IOException {
        org.apache.http.client.HttpClient client = HttpClients.createDefault();
       return client.execute(requestEntity);

    }

    public void assertSuccessfulResponse(HttpResponse restResponse) throws IOException {
        if (restResponse == null) {
            log.error(SERVER_UNREACHABLE_ERROR_MESSAGE);
            throw new ServerConnectionError();
        }
        if (!Constants.successCodeRange.contains(restResponse.getStatusLine().getStatusCode())) {
            log.debug("status code is {} ", restResponse.getStatusLine().getStatusCode());
            handleErrors(restResponse);
            return;
        }
        if (restResponse.getStatusLine().getStatusCode() == 204) {
            // success with no content
            return;
        }
        if (restResponse.getEntity() == null || restResponse.getEntity().getContent() == null) {
            log.error(NULL_RESPONSE_BODY_ERROR_MESSAGE);
            throw new ServerConnectionError(NULL_RESPONSE_BODY_ERROR_MESSAGE);
        }
        if (restResponse.getStatusLine().getStatusCode() > 299) {
            Response body = JSON.objectMapper.readValue(restResponse.getEntity().getContent(), new TypeReference<>() {
            });
            handleErrors(body);
        }

    }
    private void handleErrors(HttpResponse restResponse) throws IOException {
        if (restResponse == null || restResponse.getEntity() == null || restResponse.getEntity().getContent() == null) {
            log.error(NULL_RESPONSE_BODY_ERROR_MESSAGE);
            throw new ServerConnectionError(NULL_RESPONSE_BODY_ERROR_MESSAGE);
        }
        Response body = JSON.objectMapper.readValue(restResponse.getEntity().getContent(), new TypeReference<>() {
        });
        handleErrors(body);
    }
}
