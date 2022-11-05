package com.project.sharedfolderclient.v1.events;

import com.project.sharedfolderclient.v1.exception.BaseError;
import com.project.sharedfolderclient.v1.utils.error.Error;
import org.springframework.context.ApplicationEvent;


public class ApplicationErrorEvents {

     public static class BaseErrorEvent extends ApplicationEvent {
        public BaseErrorEvent(BaseError error) {
            super(error);
        }
    }

    public static class ErrorEvent extends ApplicationEvent {
        public ErrorEvent(Error error) {
            super(error);
        }
    }
}


