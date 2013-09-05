/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
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
package com.codenvy.api.resources.shared;

import com.codenvy.api.vfs.shared.Lock;

/** @author <a href="mailto:aparfonov@codenvy.com">Andrey Parfonov</a> */
public class File extends Resource {
    private boolean locked;

    public File(VirtualFileSystemConnector connector, Folder parent, String id, String name) {
        super(connector, parent, "FILE", id, name);
    }

    @Override
    public boolean isFolder() {
        checkValid();
        return false;
    }

    @Override
    public boolean isProject() {
        checkValid();
        return false;
    }

    @Override
    public boolean isFile() {
        checkValid();
        return true;
    }

    public String getContentType() {
        checkValid();
        return (String)getAttributes().getAttribute("vfs:mimeType").getValue();
    }

    public String getContent() {
        checkValid();
        return connector.getContent(this);
    }

    public void setContent(String data, String contentType) {
        checkValid();
        connector.updateContent(this, data, contentType);
    }

    public boolean isLocked() {
        checkValid();
        return locked;
    }

    public Lock lock(long timeout) {
        checkValid();
        Lock lock = connector.lock(this, timeout);
        locked = true;
        return lock;
    }

    public void unlock(String lockToken) {
        checkValid();
        connector.unlock(this, lockToken);
        locked = false;
    }

    @Override
    public File move(Folder newparent) {
        return (File)super.move(newparent);
    }

    @Override
    public File rename(String newname, String contentType) {
        return (File)super.rename(newname, contentType);
    }
}