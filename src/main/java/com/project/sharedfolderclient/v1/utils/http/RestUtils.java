package com.project.sharedfolderclient.v1.utils.http;

import com.project.sharedfolderclient.v1.utils.json.JSON;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;

public class RestUtils {

    public static HttpGet createGetRequest(String url) {
        return new HttpGet(url);

    }

    public static HttpDelete createDeleteRequest(String url) {
       return new HttpDelete(url);
    }

    @SneakyThrows
    public static HttpPut createPutRequest(String url, Object object) {
        HttpPut put =  new HttpPut(url);
        HttpEntity entity = EntityBuilder.create().setText(JSON.objectMapper.writeValueAsString(object))
                .build();
        put.setEntity(entity);
        return put;
    }

    public static HttpPost createPostRequest(String url, File fileToUpload) {
        HttpPost post = new HttpPost(url);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("file", fileToUpload, ContentType.DEFAULT_BINARY, fileToUpload.getName());
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        return post;
        }
}
