/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.googledrive.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.googledrive.GoogleDriveConfiguration;
import com.alibaba.langengine.googledrive.exception.GoogleDriveException;
import com.alibaba.langengine.googledrive.model.*;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Google Drive 客户端
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class GoogleDriveClient {
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_TOKEN = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    
    private final GoogleDriveConfiguration configuration;
    private Drive driveService;
    
    /**
     * 构造函数
     * 
     * @param configuration Google Drive 配置
     */
    public GoogleDriveClient(GoogleDriveConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("GoogleDriveConfiguration cannot be null");
        }
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid GoogleDriveConfiguration: " + configuration);
        }
        
        this.configuration = configuration;
        initializeDriveService();
    }
    
    /**
     * 初始化 Drive 服务
     */
    private void initializeDriveService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // 创建凭据
            Credential credential = getCredentials(HTTP_TRANSPORT);
            
            // 创建 Drive 服务
            this.driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName(configuration.getApplicationName())
                    .build();
            
            log.info("Google Drive service initialized successfully");
            
        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to initialize Google Drive service", e);
            throw new GoogleDriveException("Failed to initialize Google Drive service", e);
        }
    }
    
    /**
     * 获取凭据
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // 如果配置中有访问令牌，直接使用
        if (StringUtils.isNotBlank(configuration.getAccessToken())) {
            return createCredentialFromAccessToken(configuration.getAccessToken());
        }
        
        // 否则使用刷新令牌
        if (StringUtils.isNotBlank(configuration.getRefreshToken())) {
            return createCredentialFromRefreshToken(HTTP_TRANSPORT);
        }
        
        throw new GoogleDriveException("No valid credentials provided");
    }
    
    /**
     * 从访问令牌创建凭据
     */
    private Credential createCredentialFromAccessToken(String accessToken) {
        return new Credential.Builder(com.google.api.client.auth.oauth2.BearerToken.authorizationHeaderAccessMethod())
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setTokenServerEncodedUrl("https://oauth2.googleapis.com/token")
                .build()
                .setAccessToken(accessToken);
    }
    
    /**
     * 从刷新令牌创建凭据
     */
    private Credential createCredentialFromRefreshToken(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // 这里需要实现刷新令牌的逻辑
        // 为了简化，直接抛出异常
        throw new GoogleDriveException("Refresh token authentication not implemented yet");
    }
    
    /**
     * 搜索文件
     * 
     * @param query 搜索查询
     * @param pageSize 页面大小
     * @param pageToken 页面令牌
     * @return 搜索结果
     */
    public GoogleDriveSearchResult searchFiles(String query, Integer pageSize, String pageToken) {
        try {
            log.info("Searching files with query: {}", query);
            
            Drive.Files.List request = driveService.files().list()
                    .setQ(query)
                    .setFields("nextPageToken, files(id, name, mimeType, size, createdTime, modifiedTime, parents, webViewLink)");
            
            if (pageSize != null && pageSize > 0) {
                request.setPageSize(pageSize);
            }
            
            if (StringUtils.isNotBlank(pageToken)) {
                request.setPageToken(pageToken);
            }
            
            FileList result = request.execute();
            
            List<GoogleDriveFile> files = result.getFiles() != null ? 
                    result.getFiles().stream()
                            .map(this::convertToGoogleDriveFile)
                            .collect(Collectors.toList()) : 
                    new ArrayList<>();
            
            GoogleDriveSearchResult searchResult = new GoogleDriveSearchResult();
            searchResult.setFiles(files);
            searchResult.setNextPageToken(result.getNextPageToken());
            searchResult.setTotalFiles(files.size());
            
            log.info("Found {} files", files.size());
            return searchResult;
            
        } catch (Exception e) {
            log.error("Failed to search files", e);
            throw new GoogleDriveException("Failed to search files: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    public GoogleDriveFile getFileInfo(String fileId) {
        try {
            log.info("Getting file info for ID: {}", fileId);
            
            File file = driveService.files().get(fileId)
                    .setFields("id, name, mimeType, size, createdTime, modifiedTime, parents, webViewLink, webContentLink")
                    .execute();
            
            GoogleDriveFile googleDriveFile = convertToGoogleDriveFile(file);
            log.info("Retrieved file info: {}", googleDriveFile.getName());
            
            return googleDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to get file info for ID: {}", fileId, e);
            throw new GoogleDriveException("Failed to get file info: " + e.getMessage(), e);
        }
    }
    
    /**
     * 上传文件
     * 
     * @param filePath 本地文件路径
     * @param folderId 目标文件夹ID
     * @return 上传的文件信息
     */
    public GoogleDriveFile uploadFile(String filePath, String folderId) {
        try {
            log.info("Uploading file: {} to folder: {}", filePath, folderId);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new GoogleDriveException("File not found: " + filePath);
            }
            
            File fileMetadata = new File();
            fileMetadata.setName(path.getFileName().toString());
            
            if (StringUtils.isNotBlank(folderId)) {
                fileMetadata.setParents(Collections.singletonList(folderId));
            } else if (StringUtils.isNotBlank(configuration.getDefaultFolderId())) {
                fileMetadata.setParents(Collections.singletonList(configuration.getDefaultFolderId()));
            }
            
            String mimeType = Files.probeContentType(path);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            
            FileContent mediaContent = new FileContent(mimeType, path.toFile());
            
            File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, mimeType, size, createdTime, modifiedTime, parents, webViewLink")
                    .execute();
            
            GoogleDriveFile googleDriveFile = convertToGoogleDriveFile(uploadedFile);
            log.info("File uploaded successfully: {}", googleDriveFile.getName());
            
            return googleDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", filePath, e);
            throw new GoogleDriveException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @param outputPath 输出路径
     * @return 下载的文件路径
     */
    public String downloadFile(String fileId, String outputPath) {
        try {
            log.info("Downloading file ID: {} to: {}", fileId, outputPath);
            
            // 获取文件信息
            GoogleDriveFile fileInfo = getFileInfo(fileId);
            
            // 创建输出文件
            Path outputFilePath = Paths.get(outputPath);
            Files.createDirectories(outputFilePath.getParent());
            
            // 下载文件
            OutputStream outputStream = new FileOutputStream(outputFilePath.toFile());
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            outputStream.close();
            
            log.info("File downloaded successfully to: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("Failed to download file ID: {}", fileId, e);
            throw new GoogleDriveException("Failed to download file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建文件夹
     * 
     * @param folderName 文件夹名称
     * @param parentFolderId 父文件夹ID
     * @return 创建的文件夹信息
     */
    public GoogleDriveFile createFolder(String folderName, String parentFolderId) {
        try {
            log.info("Creating folder: {} in parent: {}", folderName, parentFolderId);
            
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setMimeType("application/vnd.google-apps.folder");
            
            if (StringUtils.isNotBlank(parentFolderId)) {
                fileMetadata.setParents(Collections.singletonList(parentFolderId));
            } else if (StringUtils.isNotBlank(configuration.getDefaultFolderId())) {
                fileMetadata.setParents(Collections.singletonList(configuration.getDefaultFolderId()));
            }
            
            File folder = driveService.files().create(fileMetadata)
                    .setFields("id, name, mimeType, size, createdTime, modifiedTime, parents, webViewLink")
                    .execute();
            
            GoogleDriveFile googleDriveFile = convertToGoogleDriveFile(folder);
            log.info("Folder created successfully: {}", googleDriveFile.getName());
            
            return googleDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to create folder: {}", folderName, e);
            throw new GoogleDriveException("Failed to create folder: " + e.getMessage(), e);
        }
    }
    
    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @return 是否删除成功
     */
    public boolean deleteFile(String fileId) {
        try {
            log.info("Deleting file ID: {}", fileId);
            
            driveService.files().delete(fileId).execute();
            
            log.info("File deleted successfully: {}", fileId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to delete file ID: {}", fileId, e);
            throw new GoogleDriveException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 移动文件
     * 
     * @param fileId 文件ID
     * @param newParentId 新的父文件夹ID
     * @return 移动后的文件信息
     */
    public GoogleDriveFile moveFile(String fileId, String newParentId) {
        try {
            log.info("Moving file ID: {} to parent: {}", fileId, newParentId);
            
            // 获取当前文件的父文件夹
            File file = driveService.files().get(fileId)
                    .setFields("parents")
                    .execute();
            
            String previousParents = String.join(",", file.getParents());
            
            // 移动文件
            File updatedFile = driveService.files().update(fileId, null)
                    .setAddParents(newParentId)
                    .setRemoveParents(previousParents)
                    .setFields("id, name, mimeType, size, createdTime, modifiedTime, parents, webViewLink")
                    .execute();
            
            GoogleDriveFile googleDriveFile = convertToGoogleDriveFile(updatedFile);
            log.info("File moved successfully: {}", googleDriveFile.getName());
            
            return googleDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to move file ID: {}", fileId, e);
            throw new GoogleDriveException("Failed to move file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换 Google Drive File 对象
     */
    private GoogleDriveFile convertToGoogleDriveFile(File file) {
        GoogleDriveFile googleDriveFile = new GoogleDriveFile();
        googleDriveFile.setId(file.getId());
        googleDriveFile.setName(file.getName());
        googleDriveFile.setMimeType(file.getMimeType());
        googleDriveFile.setSize(file.getSize() != null ? file.getSize() : 0L);
        googleDriveFile.setCreatedTime(file.getCreatedTime() != null ? file.getCreatedTime().getValue() : null);
        googleDriveFile.setModifiedTime(file.getModifiedTime() != null ? file.getModifiedTime().getValue() : null);
        googleDriveFile.setParents(file.getParents());
        googleDriveFile.setWebViewLink(file.getWebViewLink());
        googleDriveFile.setWebContentLink(file.getWebContentLink());
        return googleDriveFile;
    }
}
