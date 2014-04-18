/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.runner.internal;

import com.codenvy.api.core.ApiException;
import com.codenvy.api.runner.dto.RunRequest;

/**
 * Factory for RunnerConfiguration.
 *
 * @author andrew00x
 * @see com.codenvy.api.runner.internal.RunnerConfiguration
 * @see Runner#getRunnerConfigurationFactory()
 */
public interface RunnerConfigurationFactory {
    RunnerConfiguration createRunnerConfiguration(RunRequest request) throws ApiException;
}
