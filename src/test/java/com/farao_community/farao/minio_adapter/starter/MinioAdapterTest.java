/*
 * Copyright (c) 2022, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

import io.minio.*;
import io.minio.messages.Item;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.farao_community.farao.minio_adapter.starter.MinioAdapterConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
class MinioAdapterTest {
    private static final String MINIO_URL = "https://minio.url/";
    private static final String MINIO_ACCESS_KEY = "gridcapa";
    private static final String MINIO_SECRET_KEY = "gridcapa";
    private static final String BUCKET_NAME = "bucket-for-tests";
    private static final String BASE_PATH = "base/path/for/tests";

    @Test
    void checkThatAdapterUploadsArtifactInBasePathCorrectlyWhenBucketDoesNotExist() throws Exception {
        String uploadedArtifactPath = "testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        checkFileUploadsCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, false);
    }

    @Test
    void checkThatAdapterUploadsArtifactInSubPathFromBasePathCorrectlyWhenBucketExists() throws Exception {
        String uploadedArtifactPath = "sub/path/testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        checkFileUploadsCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, true);
    }

    @Test
    void checkThatAdapterUploadsArtifactCorrectlyWhenAllMetadataSet() throws Exception {
        String uploadedArtifactPath = "testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        String validityInterval = "2022-01-01T00:00Z/2022-01-02T00:00Z";
        checkFileUploadsCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, targetProcess, fileType, validityInterval, true);
    }

    @Test
    void checkThatAdapterUploadsArtifactCorrectlyWhenAllMetadataSetWithTimestamp() throws Exception {
        String uploadedArtifactPath = "testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        OffsetDateTime timestamp = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        checkFileUploadsForTimestampCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, targetProcess, fileType, timestamp, true);
    }

    @Test
    void checkThatAdapterUploadsArtifactCorrectlyWhenNoTargetProcessSet() throws Exception {
        String uploadedArtifactPath = "testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        String fileType = "file-type";
        String validityInterval = "2022-01-01T00:00Z/2022-01-02T00:00Z";
        checkFileUploadsCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, null, fileType, validityInterval, true);
    }

    @Test
    void checkThatAdapterUploadsArtifactCorrectlyWhenNoTypeSet() throws Exception {
        String uploadedArtifactPath = "testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        String targetProcess = "target-process";
        String validityInterval = "2022-01-01T00:00Z/2022-01-02T00:00Z";
        checkFileUploadsCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, targetProcess, null, validityInterval, true);
    }

    @Test
    void checkThatAdapterUploadsArtifactCorrectlyWhenNoValidityIntervalSet() throws Exception {
        String uploadedArtifactPath = "testArtifact";
        String uploadedArtifactContent = "testArtifactContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        checkFileUploadsCorrectly(GridcapaFileGroup.ARTIFACT, uploadedArtifactPath, uploadedArtifactContent, targetProcess, fileType, null, true);
    }

    @Test
    void checkThatAdapterUploadsInputCorrectly() throws Exception {
        String uploadedInputPath = "testInput";
        String uploadedInputContent = "testInputContent";
        checkFileUploadsCorrectly(GridcapaFileGroup.INPUT, uploadedInputPath, uploadedInputContent, true);
    }

    @Test
    void checkThatAdapterUploadsInputCorrectlyWhenAllMetadataSet() throws Exception {
        String uploadedInputPath = "testInput";
        String uploadedInputContent = "testInputContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        String validityInterval = "2022-01-01T00:00Z/2022-01-02T00:00Z";
        checkFileUploadsCorrectly(GridcapaFileGroup.INPUT, uploadedInputPath, uploadedInputContent, targetProcess, fileType, validityInterval, true);
    }

    @Test
    void checkThatAdapterUploadsInputCorrectlyWhenAllMetadataSetForTimestamp() throws Exception {
        String uploadedInputPath = "testInput";
        String uploadedInputContent = "testInputContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        OffsetDateTime timestamp = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        checkFileUploadsForTimestampCorrectly(GridcapaFileGroup.INPUT, uploadedInputPath, uploadedInputContent, targetProcess, fileType, timestamp, true);
    }

    @Test
    void checkThatAdapterUploadsOutputCorrectly() throws Exception {
        String uploadedOutputPath = "testOutput";
        String uploadedOutputContent = "testOutputContent";
        checkFileUploadsCorrectly(GridcapaFileGroup.OUTPUT, uploadedOutputPath, uploadedOutputContent, true);
    }

    @Test
    void checkThatAdapterUploadsOutputCorrectlyWhenAllMetadataSet() throws Exception {
        String uploadedOutputPath = "testOutput";
        String uploadedOutputContent = "testOutputContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        String validityInterval = "2022-01-01T00:00Z/2022-01-02T00:00Z";
        checkFileUploadsCorrectly(GridcapaFileGroup.OUTPUT, uploadedOutputPath, uploadedOutputContent, targetProcess, fileType, validityInterval, true);
    }

    @Test
    void checkThatAdapterUploadsOutputCorrectlyWhenAllMetadataSetWithTimestamp() throws Exception {
        String uploadedOutputPath = "testOutput";
        String uploadedOutputContent = "testOutputContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        OffsetDateTime timestamp = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        checkFileUploadsForTimestampCorrectly(GridcapaFileGroup.OUTPUT, uploadedOutputPath, uploadedOutputContent, targetProcess, fileType, timestamp, true);
    }

    @Test
    void checkThatAdapterUploadsExtendedOutputCorrectly() throws Exception {
        String uploadedExtendedOutputPath = "testExtendedOutput";
        String uploadedExtendedOutputContent = "testExtendedOutputContent";
        checkFileUploadsCorrectly(GridcapaFileGroup.EXTENDED_OUTPUT, uploadedExtendedOutputPath, uploadedExtendedOutputContent, true);
    }

    @Test
    void checkThatAdapterUploadsExtendedOutputCorrectlyWhenAllMetadataSet() throws Exception {
        String uploadedExtendedOutputPath = "testExtendedOutput";
        String uploadedExtendedOutputContent = "testExtendedOutputContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        String validityInterval = "2022-01-01T00:00Z/2022-01-02T00:00Z";
        checkFileUploadsCorrectly(GridcapaFileGroup.EXTENDED_OUTPUT, uploadedExtendedOutputPath, uploadedExtendedOutputContent, targetProcess, fileType, validityInterval, false);
    }

    @Test
    void checkThatAdapterUploadsExtendedOutputCorrectlyWhenAllMetadataSetWithTimestamp() throws Exception {
        String uploadedExtendedOutputPath = "testExtendedOutput";
        String uploadedExtendedOutputContent = "testExtendedOutputContent";
        String targetProcess = "target-process";
        String fileType = "file-type";
        OffsetDateTime timestamp = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        checkFileUploadsForTimestampCorrectly(GridcapaFileGroup.EXTENDED_OUTPUT, uploadedExtendedOutputPath, uploadedExtendedOutputContent, targetProcess, fileType, timestamp, false);
    }

    @Test
    void checkThatAdapterGetFileCorrectly() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "sub/path/testFile";
        String fileContent = "testFileContent";

        Mockito.when(minioClient.getObject(Mockito.argThat(assertGetObjectArgs(filePath))))
                .thenReturn(new GetObjectResponse(null, null, null, null, new ByteArrayInputStream(fileContent.getBytes())));

        InputStream fileInputStream = minioAdapter.getFile(filePath);
        assertEquals(new String(fileInputStream.readAllBytes()), fileContent);

        Mockito.verify(minioClient, Mockito.times(1))
                .getObject(Mockito.argThat(
                        assertGetObjectArgs(filePath)
                ));
    }

    @Test
    void checkThatAdapterGetFileCorrectly1() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filefullPath = "base/path/for/tests/sub/path/testFile";
        String fileContent = "testFileContent";

        Mockito.when(minioClient.getObject(Mockito.argThat(assertGetObjectArgsFromFullPath(filefullPath))))
                .thenReturn(new GetObjectResponse(null, null, null, null, new ByteArrayInputStream(fileContent.getBytes())));

        InputStream fileInputStream = minioAdapter.getFileFromFullPath(filefullPath);
        assertEquals(new String(fileInputStream.readAllBytes()), fileContent);

        Mockito.verify(minioClient, Mockito.times(1))
                .getObject(Mockito.argThat(
                        assertGetObjectArgsFromFullPath(filefullPath)
                ));
    }

    @Test
    void checkThatAdapterListsFilesCorrectlyWhenDirectoryEmpty() {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String prefix = "prefix/to/list/files/into";

        List<String> filePaths = minioAdapter.listFiles(prefix);

        assertTrue(filePaths.isEmpty());
    }

    @Test
    void checkThatAdapterListsFilesCorrectlyWhenDirectoryNotEmpty() {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        List<String> directoryContent = List.of("file1", "file2", "file3");
        Iterable<Result<Item>> fakeResult = getFakeObjectsList(directoryContent);

        String prefix = "prefix/to/list/files/into";

        Mockito.when(minioClient.listObjects(Mockito.argThat(assertListObjectsArgs(prefix))))
                .thenReturn(fakeResult);

        List<String> filePaths = minioAdapter.listFiles(prefix);

        assertLinesMatch(directoryContent, filePaths);

        Mockito.verify(minioClient, Mockito.times(1))
                .listObjects(Mockito.argThat(
                        assertListObjectsArgs(prefix)
                ));
    }

    @Test
    void checkFileExistsReturnTrueWhenFileExists() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "existingFile";

        Mockito.when(minioClient.statObject(Mockito.argThat(assertStatObjectArgs(filePath))))
                .thenReturn(Mockito.mock(StatObjectResponse.class));

        assertTrue(minioAdapter.fileExists(filePath));

        Mockito.verify(minioClient, Mockito.times(1))
                .statObject(Mockito.argThat(
                        assertStatObjectArgs(filePath)
                ));
    }

    @Test
    void checkFileExistsReturnFalseWhenFileDoesNotExists() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "nonExistingFile";

        Mockito.when(minioClient.statObject(Mockito.argThat(assertStatObjectArgs(filePath))))
                        .thenThrow(new RuntimeException());

        assertFalse(minioAdapter.fileExists(filePath));

        Mockito.verify(minioClient, Mockito.times(1))
                .statObject(Mockito.argThat(
                        assertStatObjectArgs(filePath)
                ));
    }

    @Test
    void checkThatPresignedUrlIsGeneratedCorrectlyWithGivenExpiry() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "file";
        int expiryInDays = 2;

        Mockito.when(minioClient.getPresignedObjectUrl(Mockito.argThat(assertGetPresignedObjectUrlArgs(filePath, expiryInDays))))
                .thenReturn("url");

        assertEquals("url", minioAdapter.generatePreSignedUrl(filePath, expiryInDays));

        Mockito.verify(minioClient, Mockito.times(1))
                .getPresignedObjectUrl(Mockito.argThat(
                        assertGetPresignedObjectUrlArgs(filePath, expiryInDays)
                ));
    }

    @Test
    void checkThatPresignedUrlIsGeneratedCorrectlyWithDefaultExpiry() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "file";

        Mockito.when(minioClient.getPresignedObjectUrl(Mockito.argThat(assertGetPresignedObjectUrlArgs(filePath))))
                .thenReturn("url");

        assertEquals("url", minioAdapter.generatePreSignedUrl(filePath));

        Mockito.verify(minioClient, Mockito.times(1))
                .getPresignedObjectUrl(Mockito.argThat(
                        assertGetPresignedObjectUrlArgs(filePath)
                ));
    }

    @Test
    void checkThatPresignedUrlFromFullFilenameIsGeneratedCorrectlyWithGivenExpiry() throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "file";
        int expiryInDays = 2;

        Mockito.when(minioClient.getPresignedObjectUrl(Mockito.argThat(assertGetPresignedObjectUrlFromFullFilenameArgs(filePath, expiryInDays))))
                .thenReturn("url");

        assertEquals("url", minioAdapter.generatePreSignedUrlFromFullMinioPath(filePath, expiryInDays));

        Mockito.verify(minioClient, Mockito.times(1))
                .getPresignedObjectUrl(Mockito.argThat(
                        assertGetPresignedObjectUrlFromFullFilenameArgs(filePath, expiryInDays)
                ));
    }

    @Test
    void checkThatMinioAdapterDeleteFileCorrectly() {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String filePath = "file";

        minioAdapter.deleteFile(filePath);

        Mockito.verify(minioClient, Mockito.times(1))
                .removeObjects(Mockito.argThat(
                        assertRemoveObjectsArgs()
                ));
    }

    @Test
    void checkThatMinioAdapterDeleteMultipleFilesCorrectly() {
        MinioClient minioClient = Mockito.mock(MinioClient.class);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        List<String> filePathList = Arrays.asList("file1", "file2", "file3");

        minioAdapter.deleteFiles(filePathList);

        Mockito.verify(minioClient, Mockito.times(1))
                .removeObjects(Mockito.argThat(
                        assertRemoveObjectsArgs()
                ));
    }

    private Iterable<Result<Item>> getFakeObjectsList(List<String> directoryContent) {
        return directoryContent.stream()
                .map(this::generateFakeItemForContent)
                .map(Result::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private Item generateFakeItemForContent(String content) {
        Item fakeItem = Mockito.mock(Item.class);
        Mockito.when(fakeItem.objectName()).thenReturn(BASE_PATH + "/" + content);
        return fakeItem;
    }

    private void checkFileUploadsCorrectly(GridcapaFileGroup fileGroup, String filePath, String fileContent, String targetProcess, String fileType, String validityInterval, boolean bucketAlreadyExists) throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        Mockito.when(minioClient.bucketExists(Mockito.argThat(assertBucketExistsArgs()))).thenReturn(bucketAlreadyExists);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
        switch (fileGroup) {
            case ARTIFACT:
                minioAdapter.uploadArtifact(filePath, inputStream, targetProcess, fileType, validityInterval);
                break;
            case INPUT:
                minioAdapter.uploadInput(filePath, inputStream, targetProcess, fileType, validityInterval);
                break;
            case OUTPUT:
                minioAdapter.uploadOutput(filePath, inputStream, targetProcess, fileType, validityInterval);
                break;
            case EXTENDED_OUTPUT:
                minioAdapter.uploadExtendedOutput(filePath, inputStream, targetProcess, fileType, validityInterval);
                break;
            default:
                throw new UnsupportedOperationException(String.format("File group %s not supported yet", fileGroup));
        }

        InOrder inOrder = Mockito.inOrder(minioClient);

        inOrder.verify(minioClient, Mockito.times(1))
                .bucketExists(Mockito.argThat(
                        assertBucketExistsArgs()
                ));
        if (!bucketAlreadyExists) {
            inOrder.verify(minioClient, Mockito.times(1))
                    .makeBucket(Mockito.argThat(
                            assertMakeBucketArgs()
                    ));
        }
        inOrder.verify(minioClient, Mockito.times(1))
                .putObject(Mockito.argThat(
                        assertPutObjectArgs(fileGroup, filePath, fileContent, targetProcess, fileType, validityInterval)
                ));
    }

    private void checkFileUploadsForTimestampCorrectly(GridcapaFileGroup fileGroup, String filePath, String fileContent, String targetProcess, String fileType, OffsetDateTime timestamp, boolean bucketAlreadyExists) throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        Mockito.when(minioClient.bucketExists(Mockito.argThat(assertBucketExistsArgs()))).thenReturn(bucketAlreadyExists);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
        switch (fileGroup) {
            case ARTIFACT:
                minioAdapter.uploadArtifactForTimestamp(filePath, inputStream, targetProcess, fileType, timestamp);
                break;
            case INPUT:
                minioAdapter.uploadInputForTimestamp(filePath, inputStream, targetProcess, fileType, timestamp);
                break;
            case OUTPUT:
                minioAdapter.uploadOutputForTimestamp(filePath, inputStream, targetProcess, fileType, timestamp);
                break;
            case EXTENDED_OUTPUT:
                minioAdapter.uploadExtendedOutputForTimestamp(filePath, inputStream, targetProcess, fileType, timestamp);
                break;
            default:
                throw new UnsupportedOperationException(String.format("File group %s not supported yet", fileGroup));
        }

        InOrder inOrder = Mockito.inOrder(minioClient);

        inOrder.verify(minioClient, Mockito.times(1))
                .bucketExists(Mockito.argThat(
                        assertBucketExistsArgs()
                ));
        if (!bucketAlreadyExists) {
            inOrder.verify(minioClient, Mockito.times(1))
                    .makeBucket(Mockito.argThat(
                            assertMakeBucketArgs()
                    ));
        }
        inOrder.verify(minioClient, Mockito.times(1))
                .putObject(Mockito.argThat(
                        assertPutObjectArgs(fileGroup, filePath, fileContent, targetProcess, fileType, timestamp + "/" + timestamp.plusHours(1L))
                ));
    }

    private void checkFileUploadsCorrectly(GridcapaFileGroup fileGroup, String filePath, String fileContent, boolean bucketAlreadyExists) throws Exception {
        MinioClient minioClient = Mockito.mock(MinioClient.class);
        Mockito.when(minioClient.bucketExists(Mockito.argThat(assertBucketExistsArgs()))).thenReturn(bucketAlreadyExists);

        MinioAdapterProperties properties = buildTestProperties();
        MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
        switch (fileGroup) {
            case ARTIFACT:
                minioAdapter.uploadArtifact(filePath, inputStream);
                break;
            case INPUT:
                minioAdapter.uploadInput(filePath, inputStream);
                break;
            case OUTPUT:
                minioAdapter.uploadOutput(filePath, inputStream);
                break;
            case EXTENDED_OUTPUT:
                minioAdapter.uploadExtendedOutput(filePath, inputStream);
                break;
            default:
                throw new UnsupportedOperationException(String.format("File group %s not supported yet", fileGroup));
        }

        InOrder inOrder = Mockito.inOrder(minioClient);

        inOrder.verify(minioClient, Mockito.times(1))
                .bucketExists(Mockito.argThat(
                        assertBucketExistsArgs()
                ));
        if (!bucketAlreadyExists) {
            inOrder.verify(minioClient, Mockito.times(1))
                    .makeBucket(Mockito.argThat(
                            assertMakeBucketArgs()
                    ));
        }
        inOrder.verify(minioClient, Mockito.times(1))
                .putObject(Mockito.argThat(
                        assertPutObjectArgs(fileGroup, filePath, fileContent)
                ));
    }

    private ArgumentMatcher<BucketExistsArgs> assertBucketExistsArgs() {
        return bucketExistsArgs -> bucketExistsArgs.bucket().equals(BUCKET_NAME);
    }

    private ArgumentMatcher<MakeBucketArgs> assertMakeBucketArgs() {
        return makeBucketArgs -> makeBucketArgs.bucket().equals(BUCKET_NAME);
    }

    private ArgumentMatcher<StatObjectArgs> assertStatObjectArgs(String filePath) {
        return statObjectArgs -> statObjectArgs.bucket().equals(BUCKET_NAME) &&
                statObjectArgs.object().equals(BASE_PATH + "/" + filePath);
    }

    private ArgumentMatcher<RemoveObjectsArgs> assertRemoveObjectsArgs() {
        return removeObjectsArgs -> removeObjectsArgs.bucket().equals(BUCKET_NAME);
    }

    private ArgumentMatcher<GetPresignedObjectUrlArgs> assertGetPresignedObjectUrlArgs(String filePath) {
        return assertGetPresignedObjectUrlArgs(filePath, DEFAULT_PRE_SIGNED_URL_EXPIRY_IN_DAYS);
    }

    private ArgumentMatcher<GetPresignedObjectUrlArgs> assertGetPresignedObjectUrlArgs(String filePath, int expiry) {
        return getPresignedObjectUrlArgs -> getPresignedObjectUrlArgs.bucket().equals(BUCKET_NAME) &&
                getPresignedObjectUrlArgs.object().equals(BASE_PATH + "/" + filePath) &&
                getPresignedObjectUrlArgs.expiry() == expiry * 24 * 60 * 60;
    }

    private ArgumentMatcher<GetPresignedObjectUrlArgs> assertGetPresignedObjectUrlFromFullFilenameArgs(String filePath) {
        return assertGetPresignedObjectUrlFromFullFilenameArgs(filePath, DEFAULT_PRE_SIGNED_URL_EXPIRY_IN_DAYS);
    }

    private ArgumentMatcher<GetPresignedObjectUrlArgs> assertGetPresignedObjectUrlFromFullFilenameArgs(String filePath, int expiry) {
        return getPresignedObjectUrlArgs -> getPresignedObjectUrlArgs.bucket().equals(BUCKET_NAME) &&
                getPresignedObjectUrlArgs.object().equals(filePath) &&
                getPresignedObjectUrlArgs.expiry() == expiry * 24 * 60 * 60;
    }

    private ArgumentMatcher<PutObjectArgs> assertPutObjectArgs(
            GridcapaFileGroup fileGroup,
            String filePath,
            String fileContent) {
        return putObjectArgs ->
                putObjectArgs.bucket().equals(BUCKET_NAME) &&
                putObjectArgs.object().equals(BASE_PATH + "/" + filePath) &&
                putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_NAME_METADATA_KEY, Paths.get(filePath).getFileName().toString()) &&
                putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_GROUP_METADATA_KEY, fileGroup.getMetadataValue()) &&
                streamContentEquals(putObjectArgs.stream(), fileContent);
    }

    private ArgumentMatcher<PutObjectArgs> assertPutObjectArgs(
            GridcapaFileGroup fileGroup,
            String filePath,
            String fileContent,
            String targetProcess,
            String fileType,
            String validityInterval) {
        return putObjectArgs ->
                putObjectArgs.bucket().equals(BUCKET_NAME) &&
                putObjectArgs.object().equals(BASE_PATH + "/" + filePath) &&
                putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_NAME_METADATA_KEY, Paths.get(filePath).getFileName().toString()) &&
                putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_GROUP_METADATA_KEY, fileGroup.getMetadataValue()) &&
                (targetProcess == null || putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_TARGET_PROCESS_METADATA_KEY, targetProcess)) &&
                (fileType == null || putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_TYPE_METADATA_KEY, fileType)) &&
                (validityInterval == null || putObjectArgs.userMetadata().containsEntry(DEFAULT_GRIDCAPA_FILE_VALIDITY_INTERVAL_METADATA_KEY, validityInterval)) &&
                streamContentEquals(putObjectArgs.stream(), fileContent);
    }

    private ArgumentMatcher<GetObjectArgs> assertGetObjectArgs(String filePath) {
        return getObjectArgs ->
                getObjectArgs.bucket().equals(BUCKET_NAME) &&
                getObjectArgs.object().equals(BASE_PATH + "/" + filePath);
    }

    private ArgumentMatcher<GetObjectArgs> assertGetObjectArgsFromFullPath(String filePath) {
        return getObjectArgs ->
                getObjectArgs.bucket().equals(BUCKET_NAME) &&
                        getObjectArgs.object().equals(filePath);
    }

    private ArgumentMatcher<ListObjectsArgs> assertListObjectsArgs(String prefix) {
        return listObjectsArgs ->
                listObjectsArgs.bucket().equals(BUCKET_NAME) &&
                        listObjectsArgs.prefix().equals(BASE_PATH + "/" + prefix);
    }

    private boolean streamContentEquals(InputStream inputStream, String expectedContent) {
        try {
            inputStream.mark(-1);
            boolean contentIsAsExpected = new String(inputStream.readAllBytes()).equals(expectedContent);
            inputStream.reset();
            return contentIsAsExpected;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private MinioAdapterProperties buildTestProperties() {
        return new MinioAdapterProperties(
                BUCKET_NAME,
                BASE_PATH,
                MINIO_URL,
                MINIO_ACCESS_KEY,
                MINIO_SECRET_KEY
        );
    }
}
