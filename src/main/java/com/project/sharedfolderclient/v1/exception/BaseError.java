package com.project.sharedfolderclient.v1.exception;

import lombok.Data;

@Data
public class BaseError  extends RuntimeException{
    private String message;

    public BaseError(String message) {
        super(message);
        this.message = message;
    }


}
