package com.project.sharedfolderclient.v1.file.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileCouldNotBeDeletedError extends BaseError {
    public FileCouldNotBeDeletedError(String message) {
        super(message);
    }
}
