package com.project.sharedfolderclient.v1.exception;

import com.project.sharedfolderclient.v1.utils.error.Error;
import org.springframework.context.ApplicationEvent;

/**
 * Application event for error cases
 * BaseError event (from client application)
 * Error event (from shared folder server)
 */
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


