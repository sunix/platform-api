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
package com.codenvy.api.local;

import com.codenvy.api.auth.AuthenticationDao;
import com.codenvy.api.auth.shared.dto.Credentials;
import com.codenvy.api.auth.shared.dto.Token;
import com.codenvy.api.core.ApiException;
import com.codenvy.dto.server.DtoFactory;

import javax.inject.Singleton;

@Singleton
public class LocalAuthenticationDaoImpl implements AuthenticationDao {
    @Override
    public Token login(Credentials credentials) throws ApiException {
        return DtoFactory.getInstance().createDto(Token.class).withValue("123123");
    }

    @Override
    public void logout(Token token) {
    }
}

