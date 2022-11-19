package com.project.sharedfolderclient.v1.utils.http.context;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
/**
 * Class to manage application context
 */
public class Context {
    public static final String REQUEST_ID = "requestId";

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /**
     * add request id to context
     * @param requestId - the request id to add
     */
    public void setRequestId(String requestId) {
        if (StringUtils.isNotEmpty(requestId)) {
            MDC.put(REQUEST_ID, requestId);
        }
    }

    /**
     * Get current request id in context
     * @return the request id if exist in context, empty string otherwise
     */
    public String getRequestId() {
       return Optional.ofNullable(MDC.get(REQUEST_ID)).orElse(StringUtils.EMPTY);
    }

    /**
     * Clears the context
     */
    public void clear() {
            MDC.clear();
    }
}
