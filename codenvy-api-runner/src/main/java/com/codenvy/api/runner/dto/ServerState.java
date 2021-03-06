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
package com.codenvy.api.runner.dto;

import com.codenvy.dto.shared.DTO;

/**
 * Describes state of computer.
 *
 * @author andrew00x
 */
@DTO
public interface ServerState {
    int getCpuPercentUsage();

    ServerState withCpuPercentUsage(int cpuPercentUsage);

    void setCpuPercentUsage(int cpuPercentUsage);

    long getTotalMemory();

    ServerState withTotalMemory(long totalMemory);

    void setTotalMemory(long totalMemory);

    long getFreeMemory();

    ServerState withFreeMemory(long freeMemory);

    void setFreeMemory(long freeMemory);

    int getTotalRunningAppsNum();

    ServerState withTotalRunningAppsNum(int num);

    void setTotalRunningAppsNum(int num);
}
