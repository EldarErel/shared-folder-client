package com.project.sharedfolderclient.v1.sharedfile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class SharedFile implements Serializable {
    private UUID id;
    private String name;
    private String size;
    private String kind;
    private Instant dateAdded;
    private Instant dateModified;

}
