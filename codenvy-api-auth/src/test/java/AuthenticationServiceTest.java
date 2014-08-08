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
import com.codenvy.api.auth.AuthenticationDao;
import com.codenvy.api.auth.AuthenticationService;
import com.codenvy.api.auth.shared.dto.Credentials;
import com.codenvy.dto.server.DtoFactory;
import com.jayway.restassured.http.ContentType;

import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.jayway.restassured.RestAssured.given;

@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class AuthenticationServiceTest {

    @Mock
    AuthenticationDao dao;

    @InjectMocks
    AuthenticationService service;

    @Test
    public void shouldFailIfNoBodyOnLogin() {
        given()
                .contentType(ContentType.JSON)
        .then()
                .expect().statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .when()
                .post("/auth/login");
    }

    @Test
    public void shouldFailIfUserNameIsNull() {
        given()
                .contentType(ContentType.JSON)
                .body(
                        DtoFactory.getInstance().createDto(Credentials.class)
                                  .withUsername(null)
                                  .withPassword("secret")
                     )
                .then()
                .expect().statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .when()
                .post("/auth/login");
    }

}