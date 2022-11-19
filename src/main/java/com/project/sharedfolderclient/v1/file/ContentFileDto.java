package com.project.sharedfolderclient.v1.file;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
/**
 *  Represent shared folder file with content
 */
public class ContentFileDto extends FileDto {
    private byte[] content;
}
