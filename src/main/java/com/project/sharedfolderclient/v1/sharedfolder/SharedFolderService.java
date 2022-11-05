package com.project.sharedfolderclient.v1.sharedfolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.sharedfolderclient.v1.events.ApplicationErrorEvents;
import com.project.sharedfolderclient.v1.events.ApplicationSuccessEvents;
import com.project.sharedfolderclient.v1.gui.MainFrame;
import com.project.sharedfolderclient.v1.server.ServerUtil;
import com.project.sharedfolderclient.v1.sharedfile.SharedFile;
import com.project.sharedfolderclient.v1.sharedfile.exception.*;
import com.project.sharedfolderclient.v1.utils.FileUtils;
import com.project.sharedfolderclient.v1.utils.http.Response;
import com.project.sharedfolderclient.v1.utils.http.RestUtils;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFolderService {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static Map<String, UUID> fileNamesToIdMap = new HashMap<>();
    private final ApplicationEventPublisher eventBus;
    private final ServerUtil serverUtils;



    /**
     * retrieve list of files
     * @return list of files
     *
     * On Exception - application event will be sent
     */
    public List<SharedFile> list() {
        log.debug("");
        try {
            HttpGet request = RestUtils.createGetRequest(serverUtils.getApiPath());
            HttpResponse restResponse = serverUtils.exchange(request);
            serverUtils.assertSuccessfulResponse(restResponse);
            Response<List<SharedFile>> responseBody = JSON.objectMapper.readValue(restResponse.getEntity().getContent(), new TypeReference<>() {
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
     * On Success - SuccessEvent Class event will be sent
     * On Exception - BaseErrorEvent Class event will be sent
     */
    public void download(String fileName, String downloadPath) {
        log.debug("Downloading {} to {}", fileName, downloadPath);
        executorService.execute(()-> {
            try {
                String requestUrl = serverUtils.getApiPath() + convertNameToId(fileName)
                        .orElseThrow(()-> new FileNotExistsError(fileName));
                HttpResponse restResponse = serverUtils.exchange(RestUtils.createGetRequest(requestUrl));
                serverUtils.assertSuccessfulResponse(restResponse);
                File file = FileUtils.createFile(String.format("%s/%s",downloadPath, fileName));
                FileUtils.writeByteArrayToFile(file, restResponse.getEntity().getContent().readAllBytes());
                log.info(String.format("File %s was successfully downloaded",fileName));
                eventBus.publishEvent(new ApplicationSuccessEvents.SuccessEvent(String.format("file %s was downloaded to %s",fileName, downloadPath)));
            } catch (Exception e) {
                log.error("Could not download file: {}", e.getMessage());
                eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new FileDownloadError(e.getMessage())));
            }
        });
    }

    /**
     *  upload file to the shared folder server
     * @param fileToUpload - the file object to upload
     * @return - future with SharedFile object
     * On Success - SuccessEvent Class event will be sent
     * On Exception - BaseErrorEvent Class event will be sent
     */
    public Future<SharedFile> upload(File fileToUpload) {
        log.debug("Uploading file: {}", fileToUpload);
       return executorService.submit(()-> {
            try {
                if (fileToUpload == null) {
                    log.error("Trying to Upload un exist file");
                    throw new FileNotExistsError("");
                }
                String filename = fileToUpload.getName();
                if (!fileToUpload.exists()) {
                    log.error("Trying to Upload un exist file: {} ", filename);
                    throw new FileNotExistsError(filename);
                }
                eventBus.publishEvent(new ApplicationSuccessEvents.SuccessEvent(String.format("file %s was successfully uploaded",filename)));
                HttpPost requestEntity = RestUtils.createPostRequest(serverUtils.getApiPath(), fileToUpload);
                org.apache.http.HttpResponse restResponse = serverUtils.exchange(requestEntity);
                serverUtils.assertSuccessfulResponse(restResponse);
                log.info(String.format("File %s was successfully uploaded",filename));
                Response<SharedFile> responseBody = JSON.objectMapper.readValue(restResponse.getEntity().getContent(), new TypeReference<>() {
                });
                log.info(String.format("File %s was successfully uploaded",filename));
                return responseBody.getData();
            } catch (Exception e) {
                log.error("Could not upload file: {}", e.getMessage());
                eventBus.publishEvent(new ApplicationErrorEvents.BaseErrorEvent(new FileUploadError(e.getMessage())));
            }
            return null;
        });
    }

    /**
     * rename a file
     * @param currentFileName - the file name to rename
     * @param newFileName - the name file name for that file
     * @return - the file object associate with the updated file
     *
     * On Exception - application event will be sent
     */
    public SharedFile rename(String currentFileName, String newFileName) {
        log.debug("Renaming file name from [{}] to [{}]", currentFileName, newFileName);
        try {
            UUID fileId = convertNameToId(currentFileName)
                    .orElseThrow(() -> new FileNotExistsError(currentFileName));
            SharedFile updatedFile = new SharedFile()
                    .setId(fileId)
                    .setName(newFileName);
            HttpPut request = RestUtils.createPutRequest(serverUtils.getApiPath() + fileId, updatedFile);
            org.apache.http.HttpResponse restResponse = serverUtils.exchange(request);
            serverUtils.assertSuccessfulResponse(restResponse);
            Response<SharedFile> responseBody = JSON.objectMapper.readValue(restResponse.getEntity().getContent(), new TypeReference<>() {
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
    public boolean deleteByName(String fileName) {
        log.info("Deleting [{}] from the shared folder", fileName);
        try {
            UUID fileId = convertNameToId(fileName)
                    .orElseThrow(() -> new FileNotExistsError(fileName));
            HttpResponse restResponse = serverUtils.exchange(RestUtils.createDeleteRequest(serverUtils.getApiPath() + fileId));
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
