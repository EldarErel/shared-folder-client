package com.project.sharedfolderclient.v1.sharedfile.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileCouldNotBeRenamedError extends BaseError {
    public FileCouldNotBeRenamedError(String message) {
        super(message);
    }
}
