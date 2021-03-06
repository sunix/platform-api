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
package com.codenvy.api.project.server;

import com.codenvy.api.core.ServerException;
import com.codenvy.api.project.shared.dto.ProjectUpdate;

/**
 * Provide possibility for resolving project type for newly projects(maven, ruby, python etc.)
 * @author Evgen Vidolob
 */
public interface ProjectTypeResolver {

    /**
     * Resolve {@code project} type and fill {@code description}.
     *
     * @param project
     *        the project to resolve
     * @param description
     *        the description that need to fill
     * @return {@code true} if this resolver resolve project type and fill description, {@code false} otherwise
     * @throws ServerException
     *         if an error occurs
     */
    boolean resolve(Project project, ProjectUpdate description) throws ServerException;
}
