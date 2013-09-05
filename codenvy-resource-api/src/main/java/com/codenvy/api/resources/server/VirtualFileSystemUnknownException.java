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
package com.codenvy.api.resources.server;

/**
 * Throwing when cannot access remote Virtual File System API or when get a response from remote Virtual File System which is not
 * understandable.
 *
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
@SuppressWarnings("serial")
public final class VirtualFileSystemUnknownException extends RuntimeException {
    public VirtualFileSystemUnknownException(String message) {
        super(message);
    }

    public VirtualFileSystemUnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public VirtualFileSystemUnknownException(Throwable cause) {
        super(cause);
    }
}