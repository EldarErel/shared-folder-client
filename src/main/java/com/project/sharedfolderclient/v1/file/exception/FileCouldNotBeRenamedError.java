package com.project.sharedfolderclient.v1.file.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileCouldNotBeRenamedError extends BaseError {
    public FileCouldNotBeRenamedError(String message) {
        super(message);
    }
}
