/*
 * Copyright (c) 2022, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@ConstructorBinding
@ConfigurationProperties("minio-adapter")
public class MinioAdapterProperties {
    /**
     * MinIO bucket to be used for file interactions
     */
    private final String bucket;
    /**
     * Base path in MinIO bucket. Will be removed for any interaction with the bucket content
     */
    private final String basePath;
    /**
     * MinIO instance URL
     */
    private final String url;
    /**
     * MinIO access key for authentication
     */
    private final String accessKey;
    /**
     * MinIO secret key for authentication
     */
    private final String secretKey;

    public MinioAdapterProperties(
            String bucket,
            String basePath,
            String url,
            String accessKey,
            String secretKey) {
        this.bucket = bucket;
        this.basePath = basePath;
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getUrl() {
        return url;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }
}
