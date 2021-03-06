/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.runner.internal;

/**
 * Validates DeploymentSources before running application.
 *
 * @author andrew00x
 * @see com.codenvy.api.runner.internal.DeploymentSources
 * @see com.codenvy.api.runner.internal.Runner#getDeploymentSourcesValidator()
 */
public interface DeploymentSourcesValidator {
    /**
     * Validates application bundle.
     *
     * @param deployment
     *         application bundle for validation
     * @return {@code true} is application is valid and {@code false} otherwise
     */
    boolean isValid(DeploymentSources deployment);
}
