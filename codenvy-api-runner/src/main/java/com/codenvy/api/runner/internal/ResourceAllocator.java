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

import com.codenvy.api.runner.RunnerException;

/**
 * Abstraction for allocation and releasing resources.
 *
 * @author andrew00x
 */
public interface ResourceAllocator {
    /**
     * Allocate type resource managed by this instance.
     *
     * @return this instance
     * @throws RunnerException
     *         if it is not possible to allocate resource
     */
    ResourceAllocator allocate() throws RunnerException;

    /** Release type resource managed by this instance */
    void release();
}
