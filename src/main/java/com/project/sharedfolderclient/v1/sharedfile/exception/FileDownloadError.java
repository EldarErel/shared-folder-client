package com.project.sharedfolderclient.v1.sharedfile.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

public class FileDownloadError extends BaseError {
    public FileDownloadError(String message) {
        super(message);
    }
}
