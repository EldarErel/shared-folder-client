package com.project.sharedfolderclient.v1.sharedfolder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.project.sharedfolderclient.TestUtils;
import com.project.sharedfolderclient.v1.exception.ApplicationEvents;
import com.project.sharedfolderclient.v1.exception.BaseError;
import com.project.sharedfolderclient.v1.server.ServerUtil;
import com.project.sharedfolderclient.v1.server.exception.ServerConnectionError;
import com.project.sharedfolderclient.v1.sharedfile.ContentFile;
import com.project.sharedfolderclient.v1.sharedfile.SharedFile;
import com.project.sharedfolderclient.v1.sharedfile.exception.CouldNotGetFileListError;
import com.project.sharedfolderclient.v1.sharedfile.exception.FileCouldNotBeDeletedError;
import com.project.sharedfolderclient.v1.sharedfile.exception.FileNotExistsError;
import com.project.sharedfolderclient.v1.sharedfile.exception.FileUploadError;
import com.project.sharedfolderclient.v1.utils.http.Response;
import com.project.sharedfolderclient.v1.utils.http.RestUtils;
import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
class SharedFolderServiceTest {

    private final static String CASE_PATH = "/cases/shared-folder";
    private TestUtils.CaseObject caseObject;
    @Autowired
    private SharedFolderService sharedFolderService;
    @Autowired
    private ServerUtil serverUtil;
    @MockBean
    private HttpClient httpClient;
    @Captor
    private ArgumentCaptor<ApplicationEvents.BaseErrorEvent> eventArgumentCaptor;
    @MockBean
    private TestErrorEventListener testListener;

    @BeforeAll
    public static void setUpHeadlessMode() {
        System.setProperty("java.awt.headless", "false");
    }

    @BeforeEach
    void cleanUp() {
        caseObject = null;
    }


    @Nested
    class Edit {
        @Nested
        class Rename {
            @Test
            @DisplayName("Success: rename file")
            void successRenameFile() throws IOException, InterruptedException {
                caseObject = TestUtils.generateCaseObject(CASE_PATH, "success-rename-file");
                String currentFileName = caseObject.getPreRequest().get("data").get(0).get("name").asText();
                HttpResponse<String> createResponse =  TestUtils.createHttpResponse(caseObject.getPreRequest());
                Mockito.doReturn(createResponse).when(httpClient).send(
                        RestUtils.createGetRequest(serverUtil.getApiPath()),
                        HttpResponse.BodyHandlers.ofString());
                SharedFile updatedFile = JSON.objectMapper.convertValue(caseObject.getRequest(), new TypeReference<>() {
                });
                JsonNode expectedResultAsJson = caseObject.getResponse();
                JsonNode data = expectedResultAsJson.get("data");
                SharedFile expectedResult = JSON.objectMapper.convertValue(data, new TypeReference<>() {
                });
                HttpResponse<String> response = TestUtils.createHttpResponse(expectedResultAsJson);
                Mockito.doReturn(response).when(httpClient).send(
                        RestUtils.createPutRequest(serverUtil.getApiPath() + updatedFile.getId(), updatedFile),
                        HttpResponse.BodyHandlers.ofString());
                sharedFolderService.list();

                SharedFile actualResult = sharedFolderService.rename(currentFileName, updatedFile.getName());

                assertNotNull(actualResult, "expect to have a file");
                assertEquals(expectedResult, actualResult
                        , () -> String.format("expect the same file, expected file: %s\n actual file: %s", expectedResult, actualResult));
            }
        }

    }
    @Nested
    class Get {
        @Test
        @DisplayName("Success: get list of shared files")
        void successListOfSharedFiles() throws IOException, InterruptedException {
            caseObject = TestUtils.generateCaseObject(CASE_PATH, "success-get-list");
            JsonNode expectedResultAsJson = caseObject.getResponse();
            JsonNode data = expectedResultAsJson.get("data");
            List<SharedFile> expectedResult = JSON.objectMapper.convertValue(data, new TypeReference<>() {
            });
            HttpResponse<String> response = TestUtils.createHttpResponse(expectedResultAsJson);
            Mockito.doReturn(response).when(httpClient).send(
                    any(HttpRequest.class),
                    any());

            List<SharedFile> actualResult = sharedFolderService.list();

            assertEquals(expectedResult, actualResult, "expect the same list");
        }


        @Test
        @DisplayName("Fail: connection refused")
        void FailConnectionRefused() {
            BaseError expectedError = new CouldNotGetFileListError(new ServerConnectionError().getMessage());

            List<SharedFile> actualResult = sharedFolderService.list();

            verify(testListener).errorEvent(eventArgumentCaptor.capture());
            BaseError actualError = (BaseError) eventArgumentCaptor.getValue().getSource();
            assertEquals(expectedError.getMessage(), actualError.getMessage()
                    , "error message expected to be: " + expectedError.getMessage());
            assertNull(actualResult, "expect no data");
        }

    }

    @Nested
    class Download {
        @Test
        @DisplayName("Success: download file")
        void successDownloadFile() throws IOException, InterruptedException {
            String testFileName = "test.pdf";
            Resource resource = new ClassPathResource("downloadLocation");
            String testFilePath = resource.getURI().getPath();
            caseObject = TestUtils.generateCaseObject(CASE_PATH, "success-download-file");
            JsonNode expectedResultAsJson = caseObject.getResponse();
            JsonNode data = expectedResultAsJson.get("data");
            ContentFile expectedResult = JSON.objectMapper.convertValue(data, new TypeReference<>() {
            });
            HttpResponse<String> response = TestUtils.createHttpResponse(expectedResultAsJson);
            Mockito.doReturn(response).when(httpClient).send(
                    any(HttpRequest.class),
                    any());
            when(serverUtil.exchange(RestUtils.createGetRequest(serverUtil.getApiPath())))
                    .thenReturn(TestUtils.createHttpResponse(new Response<>(caseObject.getPreRequest().get("data"))));
            sharedFolderService.list();

            ContentFile actualResult = (ContentFile) sharedFolderService.download(testFileName, testFilePath);

            assertNotNull(actualResult, "expect to have a file");
            assertEquals(expectedResult, actualResult
                    , () -> String.format("expect the same file, expected file: %s\n actual file: %s", expectedResult, actualResult));
            File downloadedFile = new File(testFilePath + "/" + testFileName);
            assertTrue(downloadedFile.exists(), "File should be exists");
            assertTrue(downloadedFile.delete(), "File was not deleted");
        }
    }

    @Nested
    class Upload {
        @Test
        @DisplayName("Success: upload file")
        void successUpload() throws IOException, InterruptedException {
            String uploadFileCaseName = "success-upload-file";
            caseObject = TestUtils.generateCaseObject(CASE_PATH, uploadFileCaseName);
            File fileToUpload = new File(uploadFileCaseName);
            try {
                assumeTrue(fileToUpload.createNewFile(), "File already exists, canceling the test");
                byte[] fileAsBytes = JSON.objectMapper.convertValue(caseObject.getRequest().get("content"), new TypeReference<>() {
                });
                FileUtils.writeByteArrayToFile(fileToUpload,fileAsBytes);
                JsonNode expectedResultAsJson = caseObject.getResponse();
                SharedFile expectedResult = JSON.objectMapper.convertValue(caseObject.getResponse().get("data"), new TypeReference<>() {
                });
                HttpResponse<String> response = TestUtils.createHttpResponse(expectedResultAsJson);
                Mockito.doReturn(response).when(httpClient).send(
                        any(HttpRequest.class),
                        any());

                SharedFile actualResult = sharedFolderService.upload(fileToUpload);

                assertNotNull(actualResult, "expect to have a file");
                assertEquals(expectedResult, actualResult
                        , () -> String.format("expect the same file, expected file: %s\n actual file: %s", expectedResult, actualResult));
            } finally {
                fileToUpload.delete();
            }
        }
        @Test
        @DisplayName("Fail: file not exists")
        void failUploadFileNotExists() {
            String notExistsFileName = "NotExistsFileName";
            File fileToUpload = new File(notExistsFileName);
            assumeFalse(fileToUpload.exists(), "File should not be exists, canceling the test");

            SharedFile actualResult = sharedFolderService.upload(fileToUpload);

            FileUploadError expectedError = new FileUploadError(new FileNotExistsError(notExistsFileName).getMessage());
            assertNull(actualResult, "file should not be present");
            verify(testListener).errorEvent(eventArgumentCaptor.capture());
            FileUploadError actualError = (FileUploadError) eventArgumentCaptor.getValue().getSource();
            assertEquals(expectedError.getMessage(),actualError.getMessage(),"expect the same error");
        }
    }

    @Nested
    class Delete {
        @Test
        @DisplayName("Fail: file not found")
        void failDeleteFileNotFound()  {
            String FILE_NAME = "notExistsFileName.pdf";
            FileCouldNotBeDeletedError expectedError = new FileCouldNotBeDeletedError(new FileNotExistsError(FILE_NAME).getMessage());

            boolean isDeleted = sharedFolderService.deleteByName(FILE_NAME);

            verify(testListener).errorEvent(eventArgumentCaptor.capture());
            FileCouldNotBeDeletedError actualError = (FileCouldNotBeDeletedError) eventArgumentCaptor.getValue().getSource();
            assertEquals(expectedError,actualError,"expect the same error");
            assertFalse(isDeleted, "file should not be deleted");
        }

        @Test
        @DisplayName("Fail: file found but server not responding")
        void failServerNotResponding() throws IOException, InterruptedException {
            caseObject = TestUtils.generateCaseObject(CASE_PATH, "success-get-list");
            JsonNode expectedResultAsJson = caseObject.getResponse();
            String existFileName = expectedResultAsJson.get("data").get(0).get("name").asText();
            HttpResponse<String> response = TestUtils.createHttpResponse(expectedResultAsJson);
            Mockito.doReturn(response).when(httpClient).send(
                    RestUtils.createGetRequest(serverUtil.getApiPath()),
                    HttpResponse.BodyHandlers.ofString());
            sharedFolderService.list();
            BaseError expectedError = new ServerConnectionError();

            boolean isDeleted = sharedFolderService.deleteByName(existFileName);

            verify(testListener).errorEvent(eventArgumentCaptor.capture());
            BaseError actualError = (BaseError) eventArgumentCaptor.getValue().getSource();
            assertEquals(expectedError.getMessage(), actualError.getMessage()
                    , "error message expected to be: " + expectedError.getMessage());
            assertFalse(isDeleted, "file should not be deleted");
        }

        @Test
        @DisplayName("Success: delete file")
        void successDelete() throws IOException, InterruptedException {
            caseObject = TestUtils.generateCaseObject(CASE_PATH, "success-get-list");
            JsonNode expectedResultAsJson = caseObject.getResponse();
            String existFileName = expectedResultAsJson.get("data").get(0).get("name").asText();
            HttpResponse<String> response = TestUtils.createHttpResponse(expectedResultAsJson);
            Mockito.doReturn(response).when(httpClient).send(
                    any(HttpRequest.class),
                   any());
            sharedFolderService.list();

            boolean isDeleted = sharedFolderService.deleteByName(existFileName);

            assertTrue(isDeleted, "file should be deleted");
        }
    }

    @TestComponent
    private static class TestErrorEventListener {
        @EventListener
        public void errorEvent(ApplicationEvents.BaseErrorEvent errorEvent) {
        }


    }

}
