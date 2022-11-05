package com.project.sharedfolderclient.v1.sharedfile;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class ContentFile extends SharedFile implements Serializable {
    private byte[] content;
}
