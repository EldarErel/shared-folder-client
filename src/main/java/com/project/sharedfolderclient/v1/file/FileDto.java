package com.project.sharedfolderclient.v1.file;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.UUID;

/**
 *  Represent file in the shared folder
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class FileDto {
    private UUID id;
    private String name;
    private String size;
    private String kind;
    private Instant dateAdded;
    private Instant dateModified;

}
