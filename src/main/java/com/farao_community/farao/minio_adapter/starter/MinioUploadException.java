/*
 * Copyright (c) 2025, RTE (https://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.farao_community.farao.minio_adapter.starter;

/**
 * @author Vincent Bochet {@literal <vincent.bochet at rte-france.com>}
 */
public class MinioUploadException extends RuntimeException {
    public MinioUploadException(final String message, final Exception cause) {
        super(message, cause);
    }
}
