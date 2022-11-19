package com.project.sharedfolderclient.v1.utils.error;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
/**
 * Describe an error from the server folder server
 */
public class Error {

    private String name;
    private String message;
}
