package com.project.sharedfolderclient.v1.utils;

import java.io.File;
import java.io.IOException;

/**
 * File utils
 */
public class FileUtils  extends org.apache.commons.io.FileUtils {
    /**
     * Create file from path
     * @param filePath - the file path
     * @return - the file from file system
     */
    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        return file;
    }
}
