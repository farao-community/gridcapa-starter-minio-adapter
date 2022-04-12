/*
 * Copyright (c) 2022, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    public void uploadArtifact(String path, InputStream inputStream,
                               @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.ARTIFACT, targetProcess, type, validityInterval);
    }

    public void uploadInput(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.INPUT);
    }

    public void uploadInput(String path, InputStream inputStream,
                            @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.INPUT, targetProcess, type, validityInterval);
    }

    public void uploadOutput(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.OUTPUT);
    }

    public void uploadOutput(String path, InputStream inputStream,
                             @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.OUTPUT, targetProcess, type, validityInterval);
    }

    public void uploadExtendedOutput(String path, InputStream inputStream) {
        uploadFileInGroup(path, inputStream, GridcapaFileGroup.EXTENDED_OUTPUT);
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
        String defaultBucket = properties.getBucket();
        String defaultBasePath = properties.getBasePath();
        String pathDestination = defaultBasePath + "/" + path;
        try {
            createBucketIfDoesNotExist();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(defaultBucket)
                            .object(pathDestination)
                            .userMetadata(buildMetadataMap(path, fileGroup, targetProcess, type, validityInterval))
                            .stream(inputStream, FILE_OBJECT_SIZE_UNKNOWN, FILE_PART_SIZE_IN_BYTES)
                            .build()
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(String.format("Exception occurred while uploading file: %s, to minio server", pathDestination), e);
        }
    }

    private Map<String, String> buildMetadataMap(String path, GridcapaFileGroup fileGroupEnum, @Nullable String targetProcess, @Nullable String type, @Nullable String validityInterval) {
        String fileName = Paths.get(path).getFileName().toString();
        String fileGroup = fileGroupEnum.getMetadataValue();
        Map<String, String> userMetadata = new TreeMap<>();
        userMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_NAME_METADATA_KEY, fileName);
        userMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_GROUP_METADATA_KEY, fileGroup);
        if (targetProcess != null) {
            userMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_TARGET_PROCESS_METADATA_KEY, targetProcess);
        }
        if (type != null) {
            userMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_TYPE_METADATA_KEY, type);
        }
        if (validityInterval != null) {
            userMetadata.put(MinioAdapterConstants.DEFAULT_GRIDCAPA_FILE_VALIDITY_INTERVAL_METADATA_KEY, validityInterval);
        }
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
