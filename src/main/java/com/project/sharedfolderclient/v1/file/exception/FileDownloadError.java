package com.project.sharedfolderclient.v1.file.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileDownloadError extends BaseError {
    public FileDownloadError(String message) {
        super(message);
    }
}
