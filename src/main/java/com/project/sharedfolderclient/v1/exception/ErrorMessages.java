package com.project.sharedfolderclient.v1.exception;

public abstract class ErrorMessages {

    public final static String SERVER_CONNECTION_ERROR_MESSAGE = "Could not connect to the server, Please try again later.";
    public final static String NULL_RESPONSE_BODY_ERROR_MESSAGE = "actual body from server is null";
    public final static String RESPONSE_BODY_PARSE_ERROR_MESSAGE = "could not parse the response body: ";
    public final static String SERVER_UNREACHABLE_ERROR_MESSAGE = "Unable to reach the server";
    public final static String FILE_NOT_EXIST_ERROR_MESSAGE = "The file is not exists: ";

    public final static String GET_FILE_LIST_ERROR_MESSAGE = "Could not get file list:  ";




}
