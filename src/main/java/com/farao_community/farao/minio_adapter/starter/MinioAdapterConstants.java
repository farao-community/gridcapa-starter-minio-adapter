/*
 * Copyright (c) 2022, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

/**
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public final class MinioAdapterConstants {
    private MinioAdapterConstants() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static final int DEFAULT_PRE_SIGNED_URL_EXPIRY_IN_DAYS = 7;
    public static final String DEFAULT_BASE_PATH = "/";

    /*
     * to avoid that MinIO modifies metadata keys to make them compliant with custom Amazon S3 user metadata,
     * ensure that any metadata key used with MinIO respects identity equality with this method, extracted form MinIO
     * library code base.
     *
     * key -> key.toLowerCase(Locale.US).startsWith("x-amz-meta-") ? "" : "x-amz-meta-") + key
     */
    public static final String DEFAULT_GRIDCAPA_FILE_NAME_METADATA_KEY = "X-Amz-Meta-Gridcapa_file_name";
    public static final String DEFAULT_GRIDCAPA_FILE_VALIDITY_INTERVAL_METADATA_KEY = "X-Amz-Meta-Gridcapa_file_validity_interval";
    public static final String DEFAULT_GRIDCAPA_FILE_TARGET_PROCESS_METADATA_KEY = "X-Amz-Meta-Gridcapa_file_target_process";
    public static final String DEFAULT_GRIDCAPA_FILE_TYPE_METADATA_KEY = "X-Amz-Meta-Gridcapa_file_type";
    public static final String DEFAULT_GRIDCAPA_FILE_GROUP_METADATA_KEY = "X-Amz-Meta-Gridcapa_file_group";

    public static final String DEFAULT_GRIDCAPA_ARTIFACT_GROUP_METADATA_VALUE = "artifact";
    public static final String DEFAULT_GRIDCAPA_INPUT_GROUP_METADATA_VALUE = "input";
    public static final String DEFAULT_GRIDCAPA_OUTPUT_GROUP_METADATA_VALUE = "output";
    public static final String DEFAULT_GRIDCAPA_EXTENDED_OUTPUT_GROUP_METADATA_VALUE = "extended_output";
}
