package com.project.sharedfolderclient.v1.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.project.sharedfolderclient.v1.server.exception.ServerConnectionError;
import com.project.sharedfolderclient.v1.utils.ApplicationProperties;
import com.project.sharedfolderclient.v1.utils.Constants;
import com.project.sharedfolderclient.v1.utils.error.Error;
import com.project.sharedfolderclient.v1.utils.http.context.Context;
import com.project.sharedfolderclient.v1.utils.http.response.Response;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static com.project.sharedfolderclient.v1.exception.ErrorMessages.*;
import static com.project.sharedfolderclient.v1.utils.http.context.Context.REQUEST_ID_HEADER;
import static java.util.stream.Collectors.joining;


/**
 * Util to communicate with the Shared folder server
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServerUtil {
    private final HttpClient client;
    private final ApplicationProperties appProperties;
    private final Context context;

    public HttpResponse<String> exchange(HttpRequest request)  {
        HttpRequest modifiedHttpRequest = addContextToRequest(request);
        try {
            return client.send(modifiedHttpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            log.warn("Could not complete the request: {}", e.getMessage());
        }
        return null;
    }

    /**
     *  Get server full path
     * @return - the server full path
     */
    public String getApiPath() {
        return appProperties.getServer().getUrl() + appProperties.getServer().getApiPath();
    }

    /**
     * Validate response from the server
     * @param response - the response object
     * @throws JsonProcessingException - if cannot deserialize the response object
     */
    @SneakyThrows
    public void assertSuccessfulResponse(HttpResponse<String> response) {
        if (response == null) {
            log.error(SERVER_UNREACHABLE_ERROR_MESSAGE);
            throw new ServerConnectionError();
        }
        if (!Constants.successCodeRange.contains(response.statusCode())) {
            log.debug("status code is {} ", response.statusCode());
            handleErrors(response);
            return;
        }
        if (response.statusCode() == Constants.NO_CONTENT_HTTP_STATUS) {
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

    /**
     * Handle errors comes from the server
     * @param response - the response with errors
     */
    private void handleErrors(HttpResponse<String> response) throws JsonProcessingException {
        if (response.body() == null) {
            log.error(NULL_RESPONSE_BODY_ERROR_MESSAGE);
            throw new ServerConnectionError(NULL_RESPONSE_BODY_ERROR_MESSAGE);
        }
        Response body = JSON.objectMapper.readValue(response.body(), new TypeReference<>() {
        });
        handleErrors(body);

    }

    /**
     * Handle errors in a response object
     * @param body - response object body
     */
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

    /**
     * Adds X-Request-Id header for the http request
     * @param request - http request
     * @return - same http request with extra request id header
     */
    private HttpRequest addContextToRequest(HttpRequest request) {
        HttpRequest.Builder builder =   HttpRequest.newBuilder(request.uri())
                .method(request.method(), request.bodyPublisher().orElse(HttpRequest.BodyPublishers.ofString(StringUtils.EMPTY)));
        for (Map.Entry<String, List<String>> header : request.headers().map().entrySet()) {
            builder.setHeader(header.getKey(),header.getValue().stream().findFirst().orElse(StringUtils.EMPTY));
            log.debug("Adding requestId header");
            builder.setHeader(REQUEST_ID_HEADER,context.getRequestId());
        }
        return builder.build();
    }
}
