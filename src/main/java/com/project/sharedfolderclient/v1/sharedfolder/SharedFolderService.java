package com.project.sharedfolderclient.v1.sharedfolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.sharedfolderclient.v1.exception.ApplicationErrorEvents;
import com.project.sharedfolderclient.v1.server.ServerUtil;
import com.project.sharedfolderclient.v1.sharedfile.ContentFile;
import com.project.sharedfolderclient.v1.sharedfile.SharedFile;
import com.project.sharedfolderclient.v1.sharedfile.exception.*;
import com.project.sharedfolderclient.v1.utils.FileUtils;
import com.project.sharedfolderclient.v1.utils.http.context.Context;
import com.project.sharedfolderclient.v1.utils.http.context.ContextEnabled;
import com.project.sharedfolderclient.v1.utils.http.response.Response;
import com.project.sharedfolderclient.v1.utils.http.RestUtils;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFolderService {
    private static Map<String, UUID> fileNamesToIdMap = new HashMap<>();
    private final ApplicationEventPublisher eventBus;
    private final ServerUtil serverUtils;

    private final Context context;

    /**
     * retrieve list of files
     * @return list of files
     *
     * On Exception - application event will be sent
     */
    @ContextEnabled
    public List<SharedFile> list() {
        log.debug("");
        try {
            HttpRequest request = RestUtils.createGetRequest(serverUtils.getApiPath());
            HttpResponse<String> restResponse = serverUtils.exchange(request);
            serverUtils.assertSuccessfulResponse(restResponse);
            Response<List<SharedFile>> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            List<SharedFile> files = responseBody.getData();
            log.debug("Received {} files", files.size());
            fileNamesToIdMap = files.stream().collect(Collectors.toMap(SharedFile::getName, SharedFile::getId));
            return files;
        } catch (Exception e) {
            log.error("Could not retrieve the file list: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new CouldNotGetFileListError(e.getMessage())));
        }
        return null;
    }

    /**
     * download file from the shared folder
     * @param fileName - the name od the file
     * @param downloadPath - the download path
     * @return - the file object associate with this file
     *
     * On Exception - application event will be sent
     */
    @ContextEnabled
    public SharedFile download(String fileName, String downloadPath) {
        log.debug("Downloading {} to {}", fileName, downloadPath);
        try {
            String requestUrl = serverUtils.getApiPath() + convertNameToId(fileName)
                    .orElseThrow(()-> new FileNotExistsError(fileName));
            HttpResponse<String> restResponse = serverUtils.exchange(RestUtils.createGetRequest(requestUrl));
            serverUtils.assertSuccessfulResponse(restResponse);
            Response<ContentFile> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            ContentFile downloadedFile = responseBody.getData();
            byte[] fileAsBytes = downloadedFile.getContent();
            log.debug("Saving file {} ", downloadedFile.getName());
            File file = FileUtils.createFile(String.format("%s/%s",downloadPath, fileName));
            FileUtils.writeByteArrayToFile(file, fileAsBytes);
            log.info(String.format("File %s was successfully downloaded",fileName));
            return downloadedFile;
        } catch (Exception e) {
            log.error("Could not download file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new FileDownloadError(e.getMessage())));
        }
        return null;
    }

    /**
     *  upload file to the shared folder server
     * @param fileToUpload - the file object to upload
     * @return - the file object associate with the uploaded file\
     *
     * On Exception - application event will be sent
     */
    @ContextEnabled
    public SharedFile upload(File fileToUpload) {
        try {
            log.debug("Uploading file: {}", fileToUpload);
            if (fileToUpload == null) {
                log.error("Trying to Upload un exist file");
                throw new FileNotExistsError("");
            }
            String filename = fileToUpload.getName();
            if (!fileToUpload.exists()) {
                log.error("Trying to Upload un exist file: {} ", filename);
                throw new FileNotExistsError(filename);
            }
            byte[] data = FileCopyUtils.copyToByteArray(fileToUpload);
            SharedFile file = new ContentFile()
                    .setContent(data)
                    .setName(filename);
            HttpRequest request = RestUtils.creatPostRequest(serverUtils.getApiPath(), file);
            HttpResponse<String> restResponse = serverUtils.exchange(request);
            serverUtils.assertSuccessfulResponse(restResponse);
            Response<SharedFile> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            log.info(String.format("File %s was successfully uploaded",filename));
            return responseBody.getData();
        } catch (Exception e) {
            log.error("Could not upload file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new FileUploadError(e.getMessage())));
        }
        return null;
    }

    /**
     * rename a file
     * @param currentFileName - the file name to rename
     * @param newFileName - the name file name for that file
     * @return - the file object associate with the updated file
     *
     * On Exception - application event will be sent
     */
    @ContextEnabled
    public SharedFile rename(String currentFileName, String newFileName) {
        log.debug("Renaming file name from [{}] to [{}]", currentFileName, newFileName);
        try {
            UUID fileId = convertNameToId(currentFileName)
                    .orElseThrow(() -> new FileNotExistsError(currentFileName));
            SharedFile updatedFile = new SharedFile()
                    .setId(fileId)
                    .setName(newFileName);
            HttpRequest request = RestUtils.createPutRequest(serverUtils.getApiPath() + fileId, updatedFile);
            HttpResponse<String> restResponse = serverUtils.exchange(request);
            serverUtils.assertSuccessfulResponse(restResponse);
            Response<SharedFile> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            return responseBody.getData();
        } catch (Exception e) {
            log.error("Could not edit file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new FileCouldNotBeRenamedError(e.getMessage())));
        }
        return null;
    }

    /**
     * delete file from the shares folder by his name
     * @param fileName - the file name
     * @return - true of the file was deleted, false otherwise
     *
     * On Exception - application event will be sent
     */
    @ContextEnabled
    public boolean deleteByName(String fileName) {
        log.info("Deleting [{}] from the shared folder", fileName);
        try {
            UUID fileId = convertNameToId(fileName)
                    .orElseThrow(() -> new FileNotExistsError(fileName));
            HttpResponse<String> restResponse = serverUtils.exchange(RestUtils.createDeleteRequest(serverUtils.getApiPath() + fileId));
            serverUtils.assertSuccessfulResponse(restResponse);
            fileNamesToIdMap.remove(fileName);
            return true;
        } catch (Exception e) {
            log.error("Could not delete file {} : {}", fileName, e.getMessage());
            eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new FileCouldNotBeDeletedError(e.getMessage())));
        }
        return false;
    }

    private Optional<UUID> convertNameToId(String fileName) {
        return Optional.ofNullable(fileNamesToIdMap.get(fileName));
    }

}
