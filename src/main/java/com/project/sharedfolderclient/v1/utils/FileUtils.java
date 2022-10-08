package com.project.sharedfolderclient.v1.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils  extends org.apache.commons.io.FileUtils {
    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        return file;
    }
}
