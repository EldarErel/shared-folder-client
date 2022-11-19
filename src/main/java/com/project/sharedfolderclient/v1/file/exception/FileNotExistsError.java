package com.project.sharedfolderclient.v1.file.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

import static com.project.sharedfolderclient.v1.exception.ErrorMessages.FILE_NOT_EXIST_ERROR_MESSAGE;

public class FileNotExistsError extends BaseError {
    public FileNotExistsError(String message) {
        super(FILE_NOT_EXIST_ERROR_MESSAGE + message);
    }
}
