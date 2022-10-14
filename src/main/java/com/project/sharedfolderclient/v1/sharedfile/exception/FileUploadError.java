package com.project.sharedfolderclient.v1.sharedfile.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileUploadError extends BaseError {
    public FileUploadError(String message) {
        super(message);
    }
}
