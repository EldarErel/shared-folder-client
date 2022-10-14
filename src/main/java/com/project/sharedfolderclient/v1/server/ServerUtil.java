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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.project.sharedfolderclient.v1.exception.ErrorMessages.*;
import static java.util.stream.Collectors.joining;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerUtil {
    private final HttpClient client;
    private final ApplicationProperties appProperties;

    public HttpResponse<String> exchange(HttpRequest request) throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String getApiPath() {
        return appProperties.getServer().getUrl() + appProperties.getServer().getApiPath();
    }

    public void assertSuccessfulResponse(HttpResponse<String> response) throws JsonProcessingException {
        if (response == null) {
            log.error(SERVER_UNREACHABLE_ERROR_MESSAGE);
            throw new ServerConnectionError();
        }
        if (!Constants.successCodeRange.contains(response.statusCode())) {
            log.error("status code is {} ", response.statusCode());
            handleErrors(response);
            return;
        }
        if (response.statusCode() == 204) {
            // success with no content
            return;
        }
        if (response.body() == null) {
            log.error(NULL_RESPONSE_BODY_ERROR_MESSAGE);
            throw new ServerConnectionError(NULL_RESPONSE_BODY_ERROR_MESSAGE);
        }
        Response body = JSON.objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        handleErrors(body);

    }

    private void handleErrors(HttpResponse<String> response) throws JsonProcessingException {
        if (response.body() == null) {
            log.error(NULL_RESPONSE_BODY_ERROR_MESSAGE);
            throw new ServerConnectionError(NULL_RESPONSE_BODY_ERROR_MESSAGE);
        }
        Response body = JSON.objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        handleErrors(body);

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
}
