package com.project.sharedfolderclient.v1.utils.error;

import com.project.sharedfolderclient.v1.exception.ApplicationErrorEvents;
import com.project.sharedfolderclient.v1.exception.BaseError;
import com.project.sharedfolderclient.v1.gui.MainFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
/**
 * Listener for error events
 */
public class ErrorHandler {
    private final MainFrame mainFrame;

    @EventListener
    // local application error handler
    public void applicationErrorHandler(ApplicationErrorEvents.BaseErrorEvent errorEvent) {
        BaseError error = (BaseError) errorEvent.getSource();
       mainFrame.printError(error.getMessage());
    }

    @EventListener
    // remote server error handler
    public void remoteServerErrorHandler(ApplicationErrorEvents.ErrorEvent errorEvent) {
        Error error = (Error) errorEvent.getSource();
        mainFrame.printError(error.getMessage());
    }
}
