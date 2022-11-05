package com.project.sharedfolderclient.v1.events;

import org.springframework.context.ApplicationEvent;

public class ApplicationSuccessEvents {
     public static class SuccessEvent extends ApplicationEvent {
        public SuccessEvent(String message) {
            super(message);
        }
    }

}


