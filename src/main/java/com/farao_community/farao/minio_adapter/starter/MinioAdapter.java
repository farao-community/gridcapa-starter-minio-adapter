/*
 * Copyright (c) 2022, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class MinioAdapter {
    private static final long FILE_OBJECT_SIZE_UNKNOWN = -1L;
    private static final long FILE_PART_SIZE_IN_BYTES = 50000000L;
    private static final Logger LOGGER = LoggerFactory.getLogger(MinioAdapter.class);

    private final MinioAdapterProperties properties;
    private final MinioClient minioClient;

    public MinioAdapter(MinioAdapterProperties properties, MinioClient minioClient) {
        this.properties = properties;
        this.minioClient = minioClient;
    }

    public MinioAdapterProperties getProperties() {
        return properties;
    }

    public void uploadArtifact(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.ARTIFACT);
    }

    public void uploadArtifactForTimestamp(String path, InputStream inputStream,
                               @Nullable String targetProcess, @Nullable String type, OffsetDateTime timestamp) {
        String validityInterval = generateHourlyValidityInterval(timestamp);
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.ARTIFACT, targetProcess, type, validityInterval);
    }

    public void uploadArtifact(String path, InputStream inputStream,
                               @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.ARTIFACT, targetProcess, type, validityInterval);
    }

    public void uploadInput(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.INPUT);
    }

    public void uploadInputForTimestamp(String path, InputStream inputStream,
                            @Nullable String targetProcess, @Nullable String type, OffsetDateTime timestamp) {
        String validityInterval = generateHourlyValidityInterval(timestamp);
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.INPUT, targetProcess, type, validityInterval);
    }

    public void uploadInput(String path, InputStream inputStream,
                            @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.INPUT, targetProcess, type, validityInterval);
    }

    public void uploadOutput(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.OUTPUT);
    }

    public void uploadOutputForTimestamp(String path, InputStream inputStream,
                             @Nullable String targetProcess, @Nullable String type, OffsetDateTime timestamp) {
        String validityInterval = generateHourlyValidityInterval(timestamp);
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.OUTPUT, targetProcess, type, validityInterval);
    }

    public void uploadOutput(String path, InputStream inputStream,
                             @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.OUTPUT, targetProcess, type, validityInterval);
    }

    public void uploadExtendedOutput(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.EXTENDED_OUTPUT);
    }

    public void uploadExtendedOutputForTimestamp(String path, InputStream inputStream,
                                     @Nullable String targetProcess, @Nullable String type, OffsetDateTime timestamp) {
        String validityInterval = generateHourlyValidityInterval(timestamp);
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.EXTENDED_OUTPUT, targetProcess, type, validityInterval);
    }

    public void uploadExtendedOutput(String path, InputStream inputStream,
                                     @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.EXTENDED_OUTPUT, targetProcess, type, validityInterval);
    }

    public InputStream getFile(String path) {
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String pathDestination = defaultBasePath + "/" + path;
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(pathDestination)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while downloading file: %s, from minio server", pathDestination), e);
        }
    }

    public InputStream getFileFromFullPath(String path) {
        String defaultBucket = properties.getBucket();
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while downloading file: %s, from minio server", path), e);
        }
    }

    public List<String> listFiles(String prefix) {
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String prefixDestination = defaultBasePath + "/" + prefix;
        try {
            return appendIterableInList(
                    minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(defaultBucket)
                                    .prefix(prefixDestination)
                                    .build()
                    ),
                    this::filterDirectoryFiles,
                    this::getFileSubPath
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while listing files in directory: %s, from minio server", prefix), e);
        }
    }

    public boolean fileExists(String filePath) {
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String pathDestination = defaultBasePath + "/" + filePath;
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(pathDestination)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generatePreSignedUrl(String filePath) {
        return generatePreSignedUrl(filePath, MinioAdapterConstants.DEFAULT_PRE_SIGNED_URL_EXPIRY_IN_DAYS);
    }

    public String generatePreSignedUrl(String filePath, int expiryInDays) {
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String pathDestination = defaultBasePath + "/" + filePath;
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(defaultBucket)
                            .object(pathDestination)
                            .expiry(expiryInDays, TimeUnit.DAYS)
                            .method(Method.GET)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while getting pre-signed URL for file: %s, from minio server", filePath), e);
        }
    }

    public String generatePreSignedUrlFromFullMinioPath(String filePath) {
        return generatePreSignedUrlFromFullMinioPath(filePath, MinioAdapterConstants.DEFAULT_PRE_SIGNED_URL_EXPIRY_IN_DAYS);
    }

    public String generatePreSignedUrlFromFullMinioPath(String filePath, int expiryInDays) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(properties.getBucket())
                            .object(filePath)
                            .expiry(expiryInDays, TimeUnit.DAYS)
                            .method(Method.GET)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while getting pre-signed URL for file: %s, from minio server", filePath), e);
        }
    }

    public void deleteFile(String filePath) {
        deleteFiles(Collections.singletonList(filePath));
    }

    public void deleteFiles(List<String> filePathList) {
        String defaultBucket = properties.getBucket();
        List<DeleteObject> deleteObjects = buildDeleteObjects(filePathList);
        try {
            var results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(defaultBucket)
                            .objects(deleteObjects)
                            .build()
            );
            for (Result<DeleteError> deleteErrorResult: results) {
                DeleteError error = deleteErrorResult.get();
                LOGGER.error("Could not delete object " + error.objectName());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Exception occurred while deleting files from minio server", e);
        }
    }

    public Map<String, String> getFileMetadata(String filePath) {
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String pathDestination = defaultBasePath + "/" + filePath;
        try {
            return minioClient.listObjects(ListObjectsArgs.builder()
                            .bucket(defaultBucket)
                            .prefix(pathDestination)
                            .includeUserMetadata(true)
                            .build()
            ).iterator().next().get().userMetadata();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while getting metadata of file %s from minio server", filePath), e);
        }
    }

    public void addMetadataToFile(String filePath, GridcapaFileGroup fileGroup,
                                  @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        InputStream fileContent = getFile(filePath);
        Map<String, String> fileMetadata = new TreeMap<>(getFileMetadata(filePath));
        appendMetadata(fileMetadata, fileGroup, targetProcess, type, validityInterval);
        uploadFileWithMetadata(filePath, fileContent, fileMetadata);
    }

    private String generateHourlyValidityInterval(OffsetDateTime timestamp) {
        return timestamp != null ? timestamp + "/" + timestamp.plusHours(1L) : null;
    }

    private void appendMetadata(Map<String, String> currentMetadata, GridcapaFileGroup fileGroup, String targetProcess, String type, String validityInterval) {
        if (fileGroup != null) {
            currentMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_GROUP_METADATA_KEY, fileGroup.getMetadataValue());
        }
        if (targetProcess != null) {
            currentMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_TARGET_PROCESS_METADATA_KEY, targetProcess);
        }
        if (type != null) {
            currentMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_TYPE_METADATA_KEY, type);
        }
        if (validityInterval != null) {
            currentMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_VALIDITY_INTERVAL_METADATA_KEY, validityInterval);
        }
    }

    private List<DeleteObject> buildDeleteObjects(List<String> filePathList) {
        String defaultBasePath = properties.getBasePath();
        return filePathList.stream()
                .map(filePath -> new DeleteObject(defaultBasePath + "/" + filePath))
                .collect(Collectors.toList());
    }

    private boolean filterDirectoryFiles(Result<Item> itemResult) {
        try {
            return !itemResult.get().isDir();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileSubPath(Result<Item> itemResult) {
        try {
            String fullPath = itemResult.get().objectName();
            String basePath = properties.getBasePath();
            return fullPath.replaceFirst(basePath + "/", "");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void uploadFileInGroup(String path, InputStream inputStream, GridcapaFileGroup fileGroup) {
        uploadFileInGroup(path, inputStream, fileGroup, null, null, null);
    }

    private void uploadFileInGroup(String path, InputStream inputStream, GridcapaFileGroup fileGroup, @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileWithMetadata(path, inputStream, buildMetadataMap(path, fileGroup, targetProcess, type, validityInterval));
    }

    private void uploadFileWithMetadata(String path, InputStream inputStream, Map<String, String> metadata) {
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String pathDestination = defaultBasePath + "/" + path;
        try {
            createBucketIfDoesNotExist();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(pathDestination)
                            .userMetadata(metadata)
                            .stream(inputStream, FILE_OBJECT_SIZE_UNKNOWN, FILE_PART_SIZE_IN_BYTES)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while uploading file: %s, to minio server", pathDestination), e);
        }
    }

    private Map<String, String> buildMetadataMap(String path, @Nullable GridcapaFileGroup fileGroupEnum, @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        String fileName = Paths.get(path).getFileName().toString();
        Map<String, String> userMetadata = new TreeMap<>();
        userMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_NAME_METADATA_KEY, fileName);
        appendMetadata(userMetadata, fileGroupEnum, targetProcess, type, validityInterval);
        return userMetadata;
    }

    private void createBucketIfDoesNotExist() {
        String defaultBucket = properties.getBucket();
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(defaultBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(defaultBucket).build());
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Exception occurred while creating bucket: %s", defaultBucket));
            throw new RuntimeException(String.format("Exception occurred while creating bucket: %s", defaultBucket), e);
        }
    }

    private <D, S> List<D> appendIterableInList(Iterable<S> iterable, Predicate<S> filter, Function<S, D> mapper) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(filter)
                .map(mapper)
                .collect(Collectors.toList());
    }
}
