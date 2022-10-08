package com.project.sharedfolderclient.v1.sharedfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SharedFile {
    private UUID id;
    private String name;
    private String size;
    private String kind;
    private LocalDateTime dateAdded;
    private LocalDateTime dateModified;

}
