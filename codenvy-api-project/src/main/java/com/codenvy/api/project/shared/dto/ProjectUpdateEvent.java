/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.api.project.shared.dto;

import com.codenvy.dto.shared.DTO;

/**
 * @author andrew00x
 */
@DTO
public interface ProjectUpdateEvent {
    String getEventType();

    void setEventType(String eventType);

    ProjectUpdateEvent withEventType(String eventType);

    boolean isFolder();

    void setFolder(boolean isFolder);

    ProjectUpdateEvent withFolder(boolean isFolder);

    String getPath();

    void setPath(String path);

    ProjectUpdateEvent withPath(String path);

    String getOldPath();

    void setOldPath(String path);

    ProjectUpdateEvent withOldPath(String path);

    String getMediaType();

    void setMediaType(String mediaType);

    ProjectUpdateEvent withMediaType(String mediaType);
}
