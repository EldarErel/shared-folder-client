package com.project.sharedfolderclient.v1.server.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;
import com.project.sharedfolderclient.v1.exception.ErrorMessages;

public class ServerConnectionError extends BaseError {

    public ServerConnectionError() {
        super(ErrorMessages.SERVER_CONNECTION_ERROR_MESSAGE);
    }

    public ServerConnectionError(String message) {
        super(message);
    }
}
