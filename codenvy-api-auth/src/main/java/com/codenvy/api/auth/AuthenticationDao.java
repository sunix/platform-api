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
package com.codenvy.api.auth;

import com.codenvy.api.auth.shared.dto.Credentials;
import com.codenvy.api.auth.shared.dto.Token;
import com.codenvy.api.core.ApiException;

/**
 * @author gazarenkov
 */
public interface AuthenticationDao {
    /**
     * Authenticate user by given credential and return authentication token.
     *
     * @param credentials
     *         - username and password
     * @return - authentication token
     * @throws ApiException
     */
    Token login(Credentials credentials) throws ApiException;

    /**
     * Invalidate given token.
     *
     * @param token
     *         - token to invalidate.
     */
    void logout(Token token);
}
