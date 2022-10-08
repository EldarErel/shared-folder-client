package com.project.sharedfolderclient.v1.sharedfile;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ContentFile extends SharedFile {
    private byte[] content;
}
