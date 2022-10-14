package com.project.sharedfolderclient.v1.sharedfolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.sharedfolderclient.v1.exception.ApplicationEvents;
import com.project.sharedfolderclient.v1.server.ServerUtil;
import com.project.sharedfolderclient.v1.sharedfile.ContentFile;
import com.project.sharedfolderclient.v1.sharedfile.SharedFile;
import com.project.sharedfolderclient.v1.sharedfile.exception.*;
import com.project.sharedfolderclient.v1.utils.FileUtils;
import com.project.sharedfolderclient.v1.utils.http.Response;
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
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new CouldNotGetFileListError(e.getMessage())));
        }
        return null;
    }

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
            log.error("Could not retrieve the file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileDownloadError(e.getMessage())));
        }
        return null;
    }

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
            log.error("Could not retrieve the file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileUploadError(e.getMessage())));
        }
        return null;
    }

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
            log.error("Could not edit the file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileCouldNotBeRenamedError(e.getMessage())));
        }
        return null;
    }

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
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileCouldNotBeDeletedError(e.getMessage())));
        }
        return false;
    }

    private Optional<UUID> convertNameToId(String fileName) {
        return Optional.ofNullable(fileNamesToIdMap.get(fileName));
    }

}
