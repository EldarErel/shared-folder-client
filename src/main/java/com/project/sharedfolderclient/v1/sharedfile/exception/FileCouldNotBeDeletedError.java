package com.project.sharedfolderclient.v1.sharedfile.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileCouldNotBeDeletedError extends BaseError {
    public FileCouldNotBeDeletedError(String message) {
        super(message);
    }
}
