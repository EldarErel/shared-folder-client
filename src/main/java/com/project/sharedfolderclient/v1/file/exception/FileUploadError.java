package com.project.sharedfolderclient.v1.file.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileUploadError extends BaseError {
    public FileUploadError(String message) {
        super(message);
    }
}
