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
package com.alibaba.langengine.onedrive.client;

import com.alibaba.langengine.onedrive.OneDriveConfiguration;
import com.alibaba.langengine.onedrive.exception.OneDriveException;
import com.alibaba.langengine.onedrive.model.OneDriveFile;
import com.alibaba.langengine.onedrive.model.OneDriveSearchResult;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.httpcore.HttpClients;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.DriveItemCollectionPage;
import com.microsoft.graph.requests.DriveItemRequestBuilder;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.models.Folder;
import com.microsoft.graph.models.File;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OneDrive 客户端
 * 
 * @author AIDC-AI
 */
@Slf4j
@Data
public class OneDriveClient {
    
    private final OneDriveConfiguration configuration;
    private GraphServiceClient graphServiceClient;
    
    /**
     * 构造函数
     * 
     * @param configuration OneDrive 配置
     */
    public OneDriveClient(OneDriveConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("OneDriveConfiguration cannot be null");
        }
        if (!configuration.isValid()) {
            throw new IllegalArgumentException("Invalid OneDriveConfiguration: " + configuration);
        }
        
        this.configuration = configuration;
        initializeGraphServiceClient();
    }
    
    /**
     * 初始化 Graph 服务客户端
     */
    private void initializeGraphServiceClient() {
        try {
            // 创建认证提供者
            IAuthenticationProvider authProvider = new OneDriveAuthProvider(configuration);
            
            // 创建 Graph 服务客户端
            this.graphServiceClient = GraphServiceClient.builder()
                    .authenticationProvider(authProvider)
                    .buildClient();
            
            log.info("OneDrive Graph service client initialized successfully");
            
        } catch (Exception e) {
            log.error("Failed to initialize OneDrive Graph service client", e);
            throw new OneDriveException("Failed to initialize OneDrive Graph service client", e);
        }
    }
    
    /**
     * 搜索文件
     * 
     * @param query 搜索查询
     * @param pageSize 页面大小
     * @param skipToken 跳过令牌
     * @return 搜索结果
     */
    public OneDriveSearchResult searchFiles(String query, Integer pageSize, String skipToken) {
        try {
            log.info("Searching files with query: {}", query);
            
            // 构建搜索请求
            DriveItemCollectionPage searchResults = graphServiceClient
                    .me()
                    .drive()
                    .root()
                    .search(query)
                    .buildRequest()
                    .get();
            
            List<OneDriveFile> files = new ArrayList<>();
            if (searchResults != null && searchResults.getCurrentPage() != null) {
                files = searchResults.getCurrentPage().stream()
                        .map(this::convertToOneDriveFile)
                        .collect(Collectors.toList());
            }
            
            OneDriveSearchResult result = new OneDriveSearchResult();
            result.setFiles(files);
            result.setNextLink(searchResults != null ? searchResults.getNextPage() != null ? searchResults.getNextPage().getRequestUrl() : null : null);
            result.setTotalFiles(files.size());
            
            log.info("Found {} files", files.size());
            return result;
            
        } catch (Exception e) {
            log.error("Failed to search files", e);
            throw new OneDriveException("Failed to search files: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    public OneDriveFile getFileInfo(String fileId) {
        try {
            log.info("Getting file info for ID: {}", fileId);
            
            DriveItem driveItem = graphServiceClient
                    .me()
                    .drive()
                    .items(fileId)
                    .buildRequest()
                    .get();
            
            if (driveItem == null) {
                throw new OneDriveException("File not found: " + fileId);
            }
            
            OneDriveFile oneDriveFile = convertToOneDriveFile(driveItem);
            log.info("Retrieved file info: {}", oneDriveFile.getName());
            
            return oneDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to get file info for ID: {}", fileId, e);
            throw new OneDriveException("Failed to get file info: " + e.getMessage(), e);
        }
    }
    
    /**
     * 上传文件
     * 
     * @param filePath 本地文件路径
     * @param folderId 目标文件夹ID
     * @return 上传的文件信息
     */
    public OneDriveFile uploadFile(String filePath, String folderId) {
        try {
            log.info("Uploading file: {} to folder: {}", filePath, folderId);
            
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new OneDriveException("File not found: " + filePath);
            }
            
            // 确定目标路径
            String targetPath = StringUtils.isNotBlank(folderId) ? 
                    "/items/" + folderId + ":/" + path.getFileName().toString() + ":/content" :
                    "/items/root:/" + path.getFileName().toString() + ":/content";
            
            // 上传文件
            DriveItem uploadedItem = graphServiceClient
                    .me()
                    .drive()
                    .items(targetPath)
                    .content()
                    .buildRequest()
                    .put(Files.readAllBytes(path));
            
            OneDriveFile oneDriveFile = convertToOneDriveFile(uploadedItem);
            log.info("File uploaded successfully: {}", oneDriveFile.getName());
            
            return oneDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", filePath, e);
            throw new OneDriveException("Failed to upload file: " + e.getMessage(), e);
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
            OneDriveFile fileInfo = getFileInfo(fileId);
            
            // 创建输出文件
            Path outputFilePath = Paths.get(outputPath);
            Files.createDirectories(outputFilePath.getParent());
            
            // 下载文件
            InputStream inputStream = graphServiceClient
                    .me()
                    .drive()
                    .items(fileId)
                    .content()
                    .buildRequest()
                    .get();
            
            try (FileOutputStream outputStream = new FileOutputStream(outputFilePath.toFile())) {
                IOUtils.copy(inputStream, outputStream);
            }
            
            log.info("File downloaded successfully to: {}", outputPath);
            return outputPath;
            
        } catch (Exception e) {
            log.error("Failed to download file ID: {}", fileId, e);
            throw new OneDriveException("Failed to download file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建文件夹
     * 
     * @param folderName 文件夹名称
     * @param parentFolderId 父文件夹ID
     * @return 创建的文件夹信息
     */
    public OneDriveFile createFolder(String folderName, String parentFolderId) {
        try {
            log.info("Creating folder: {} in parent: {}", folderName, parentFolderId);
            
            DriveItem folderItem = new DriveItem();
            folderItem.name = folderName;
            folderItem.folder = new Folder();
            
            DriveItem createdFolder = graphServiceClient
                    .me()
                    .drive()
                    .items(StringUtils.isNotBlank(parentFolderId) ? parentFolderId : "root")
                    .children()
                    .buildRequest()
                    .post(folderItem);
            
            OneDriveFile oneDriveFile = convertToOneDriveFile(createdFolder);
            log.info("Folder created successfully: {}", oneDriveFile.getName());
            
            return oneDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to create folder: {}", folderName, e);
            throw new OneDriveException("Failed to create folder: " + e.getMessage(), e);
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
            
            graphServiceClient
                    .me()
                    .drive()
                    .items(fileId)
                    .buildRequest()
                    .delete();
            
            log.info("File deleted successfully: {}", fileId);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to delete file ID: {}", fileId, e);
            throw new OneDriveException("Failed to delete file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 移动文件
     * 
     * @param fileId 文件ID
     * @param newParentId 新的父文件夹ID
     * @return 移动后的文件信息
     */
    public OneDriveFile moveFile(String fileId, String newParentId) {
        try {
            log.info("Moving file ID: {} to parent: {}", fileId, newParentId);
            
            DriveItem driveItem = new DriveItem();
            driveItem.parentReference = new com.microsoft.graph.models.ItemReference();
            driveItem.parentReference.id = newParentId;
            
            DriveItem movedItem = graphServiceClient
                    .me()
                    .drive()
                    .items(fileId)
                    .buildRequest()
                    .patch(driveItem);
            
            OneDriveFile oneDriveFile = convertToOneDriveFile(movedItem);
            log.info("File moved successfully: {}", oneDriveFile.getName());
            
            return oneDriveFile;
            
        } catch (Exception e) {
            log.error("Failed to move file ID: {}", fileId, e);
            throw new OneDriveException("Failed to move file: " + e.getMessage(), e);
        }
    }
    
    /**
     * 转换 OneDrive DriveItem 对象
     */
    private OneDriveFile convertToOneDriveFile(DriveItem driveItem) {
        OneDriveFile oneDriveFile = new OneDriveFile();
        oneDriveFile.setId(driveItem.id);
        oneDriveFile.setName(driveItem.name);
        oneDriveFile.setSize(driveItem.size);
        oneDriveFile.setCreatedDateTime(driveItem.createdDateTime != null ? driveItem.createdDateTime.toString() : null);
        oneDriveFile.setLastModifiedDateTime(driveItem.lastModifiedDateTime != null ? driveItem.lastModifiedDateTime.toString() : null);
        oneDriveFile.setDownloadUrl(driveItem.downloadUrl);
        oneDriveFile.setWebUrl(driveItem.webUrl);
        oneDriveFile.setFileType(driveItem.file != null ? driveItem.file.mimeType : null);
        oneDriveFile.setMimeType(driveItem.file != null ? driveItem.file.mimeType : null);
        oneDriveFile.setFolder(driveItem.folder != null);
        oneDriveFile.setParentId(driveItem.parentReference != null ? driveItem.parentReference.id : null);
        return oneDriveFile;
    }
}
