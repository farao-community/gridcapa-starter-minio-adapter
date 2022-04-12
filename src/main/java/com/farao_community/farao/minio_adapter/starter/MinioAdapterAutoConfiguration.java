/*
 * Copyright (c) 2022, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
@Configuration
@EnableConfigurationProperties(MinioAdapterProperties.class)
public class MinioAdapterAutoConfiguration {
    private final MinioAdapterProperties properties;

    public MinioAdapterAutoConfiguration(MinioAdapterProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getUrl())
                .credentials(properties.getAccessKey(), properties.getSecretKey()).build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioAdapter minioAdapter(MinioClient minioClient) {
        return new MinioAdapter(properties, minioClient);
    }

}
