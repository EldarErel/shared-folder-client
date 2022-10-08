package com.project.sharedfolderclient.v1.sharedfolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.project.sharedfolderclient.v1.exception.ApplicationEvents;
import com.project.sharedfolderclient.v1.server.ServerUtil;
import com.project.sharedfolderclient.v1.server.exception.ServerConnectionError;
import com.project.sharedfolderclient.v1.sharedfile.ContentFile;
import com.project.sharedfolderclient.v1.sharedfile.SharedFile;
import com.project.sharedfolderclient.v1.sharedfile.exception.CouldNotGetFileListError;
import com.project.sharedfolderclient.v1.sharedfile.exception.FileCouldNotBeDeletedError;
import com.project.sharedfolderclient.v1.sharedfile.exception.FileNotExistsError;
import com.project.sharedfolderclient.v1.utils.Constants;
import com.project.sharedfolderclient.v1.utils.error.Error;
import com.project.sharedfolderclient.v1.utils.http.Response;
import com.project.sharedfolderclient.v1.utils.http.RestUtils;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;

import static com.project.sharedfolderclient.v1.exception.ErrorMessages.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SharedFolderService {
    private static Map<String, UUID> fileNamesToIdMap = new HashMap<>();
    private final ApplicationEventPublisher eventBus;
    private final ServerUtil serverUtil;

    public List<SharedFile> list() {
        log.debug("");
        try {
            HttpResponse<String> restResponse = serverUtil.exchange(RestUtils.createGetRequest(serverUtil.getApiPath()));
            assertSuccessfulResponse(restResponse);
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
        log.info("Downloading {} to {}", fileName, downloadPath);
        try {
            HttpResponse<String> restResponse = serverUtil.exchange(RestUtils.createGetRequest(serverUtil.getApiPath() + findByName(fileName)));
            assertSuccessfulResponse(restResponse);
            Response<ContentFile> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            ContentFile downloadedFile = responseBody.getData();
            byte[] fileAsBytes = downloadedFile.getContent();
            log.debug("Saving file {} ", downloadedFile.getName());
            File file = createFile(String.format("%s/%s", downloadPath, fileName));
            FileUtils.writeByteArrayToFile(file, fileAsBytes);
            return downloadedFile;
        } catch (Exception e) {
            log.error("Could not retrieve the file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileNotExistsError(e.getMessage())));
        }
        return null;
    }

    public SharedFile upload(File fileToUpload) {
        try {
            byte[] data = FileCopyUtils.copyToByteArray(fileToUpload);
            SharedFile file = new ContentFile()
                    .setContent(data)
                    .setName(fileToUpload.getName());
            HttpResponse<String> restResponse = serverUtil.exchange(RestUtils.creatPostRequest(serverUtil.getApiPath(), file));
            assertSuccessfulResponse(restResponse);
            Response<SharedFile> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            return responseBody.getData();
        } catch (Exception e) {
            log.error("Could not retrieve the file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileNotExistsError(e.getMessage())));
        }
        return null;
    }

    public SharedFile rename(String fileName, String newName) {
        try {
            UUID fileId = findByName(fileName).orElseThrow(() -> new FileNotExistsError(fileName));
            SharedFile updatedFile = new SharedFile()
                    .setId(fileId)
                    .setName(newName);
            HttpResponse<String> restResponse = serverUtil.exchange(RestUtils.createPutRequest(serverUtil.getApiPath() + fileId, updatedFile));
            assertSuccessfulResponse(restResponse);
            Response<SharedFile> responseBody = JSON.objectMapper.readValue(restResponse.body(), new TypeReference<>() {
            });
            return responseBody.getData();
        } catch (Exception e) {
            log.error("Could not edit the file: {}", e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileNotExistsError(e.getMessage())));
        }
        return null;
    }

    private Optional<UUID> findByName(String fileName) {
        return Optional.ofNullable(fileNamesToIdMap.get(fileName));
    }

    public boolean deleteByName(String fileName) {
        log.info("Deleting [{}] from the shared folder", fileName);
        try {
            UUID fileId = findByName(fileName)
                    .orElseThrow(() -> new FileNotExistsError(fileName));
            HttpResponse<String> restResponse = serverUtil.exchange(RestUtils.createDeleteRequest(serverUtil.getApiPath() + fileId));
            assertSuccessfulResponse(restResponse);
            fileNamesToIdMap.remove(fileName);
            return true;
        } catch (Exception e) {
            log.error("Could not delete file {} : {}", fileName, e.getMessage());
            eventBus.publishEvent(new ApplicationEvents.BaseErrorEvent(new FileCouldNotBeDeletedError(e.getMessage())));
        }
        return false;
    }

    private void assertSuccessfulResponse(HttpResponse<String> response) {
        if (response == null) {
            log.error(SERVER_UNREACHABLE_ERROR_MESSAGE);
            throw new ServerConnectionError();
        }
        if (!Constants.successCodeRange.contains(response.statusCode())) {
            log.error("status code is {} ", response.statusCode());
        }
        try {
            Response<?> body = JSON.objectMapper.readValue(response.body(), new TypeReference<>() {
            });
            if (body == null) {
                log.error(NULL_RESPONSE_BODY_ERROR_MESSAGE);
                throw new ServerConnectionError(NULL_RESPONSE_BODY_ERROR_MESSAGE);
            }
            if (!CollectionUtils.isEmpty(body.getErrors())) {
                log.error("Errors: {}", body.getErrors());
                String errorMessage = StringUtils.join(body.getErrors(), ", ");
                throw new ServerConnectionError(errorMessage);
            }
        } catch (Exception e) {
            log.error(RESPONSE_BODY_PARSE_ERROR_MESSAGE + e.getMessage());
            throw new ServerConnectionError(RESPONSE_BODY_PARSE_ERROR_MESSAGE);
        }
    }

    private File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        file.createNewFile();
        return file;
    }
}
