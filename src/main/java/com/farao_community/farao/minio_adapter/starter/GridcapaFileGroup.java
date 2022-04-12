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
public enum GridcapaFileGroup {
    ARTIFACT(MinioAdapterConstants.DEFAULT_GRIDCAPA_ARTIFACT_GROUP_METADATA_VALUE),
    INPUT(MinioAdapterConstants.DEFAULT_GRIDCAPA_INPUT_GROUP_METADATA_VALUE),
    OUTPUT(MinioAdapterConstants.DEFAULT_GRIDCAPA_OUTPUT_GROUP_METADATA_VALUE),
    EXTENDED_OUTPUT(MinioAdapterConstants.DEFAULT_GRIDCAPA_EXTENDED_OUTPUT_GROUP_METADATA_VALUE);

    private final String metadataValue;

    GridcapaFileGroup(String metadataValue) {
        this.metadataValue = metadataValue;
    }

    public String getMetadataValue() {
        return metadataValue;
    }
}
