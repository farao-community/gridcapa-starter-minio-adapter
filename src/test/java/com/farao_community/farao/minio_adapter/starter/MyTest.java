/*
 * Copyright (c) 2024, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;

/**
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
class MyTest {

    @Test
    void checkThatAdapterSafelyUploadsOutputInBasePathCorrectlyWhenBucketDoesNotExist() throws InterruptedException {
        final MinioAdapterProperties properties = new MinioAdapterProperties("gridcapa", "tests", "http://minio:9000", "gridcapa", "gridcapa");

        final MinioClient minioClient = MinioClient.builder()
                .endpoint(properties.getUrl())
                .credentials(properties.getAccessKey(), properties.getSecretKey()).build();

        final MinioAdapter minioAdapter = new MinioAdapter(properties, minioClient);

        String uploadedOutputPath = "testOutput.txt";
        String uploadedOutputContent = new Random().ints(97, 123).limit(1024*1024*40).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        String targetProcess = "target-process";
        String fileType = "file-type";
        OffsetDateTime timestamp = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        InputStream inputStream = new ByteArrayInputStream(uploadedOutputContent.getBytes());

        Thread t1 = new Thread(() -> uploadFileToMinio("T1", minioAdapter, uploadedOutputPath, inputStream, targetProcess, fileType, timestamp));
        Thread t2 = new Thread(() -> uploadFileToMinio("T2", minioAdapter, uploadedOutputPath, inputStream, targetProcess, fileType, timestamp));

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    private static void uploadFileToMinio(final String threadName, final MinioAdapter minioAdapter, final String uploadedOutputPath, final InputStream inputStream, final String targetProcess, final String fileType, final OffsetDateTime timestamp) {
        try {
            System.out.println(threadName + " start: " + LocalDateTime.now());
            minioAdapter.safelyUploadOutputForTimestamp(uploadedOutputPath, inputStream, targetProcess, fileType, timestamp);
            System.out.println(threadName + " end: " + LocalDateTime.now());
        } catch (Exception e) {
            System.out.println(threadName + " failed at " + LocalDateTime.now() + ": " + e.getMessage());
        }
    }
}
