package com.project.sharedfolderclient.v1.sharedfile.exception;

import com.project.sharedfolderclient.v1.exception.BaseError;

import static com.project.sharedfolderclient.v1.exception.ErrorMessages.GET_FILE_LIST_ERROR_MESSAGE;

public class CouldNotGetFileListError extends BaseError {
    public CouldNotGetFileListError(String message) {
        super(GET_FILE_LIST_ERROR_MESSAGE + message);
    }
}
