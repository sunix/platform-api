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

import junit.framework.Assert;

import com.codenvy.api.auth.AuthenticationDao;
import com.codenvy.api.auth.AuthenticationService;
import com.codenvy.api.auth.server.dto.DtoServerImpls;
import com.codenvy.api.auth.shared.dto.Credentials;
import com.codenvy.api.auth.shared.dto.Token;
import com.codenvy.api.core.ApiException;
import com.codenvy.dto.server.DtoFactory;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.mapper.ObjectMapper;

import org.everrest.assured.EverrestJetty;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Mockito.verify;

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

    @Test
    public void shouldFailIfUserNameIsEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(
                        DtoFactory.getInstance().createDto(Credentials.class)
                                  .withUsername("")
                                  .withPassword("secret")
                     )
                .then()
                .expect().statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .when()
                .post("/auth/login");
    }

    @Test
    public void shouldFailIfPasswordIsEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(
                        DtoFactory.getInstance().createDto(Credentials.class)
                                  .withUsername("User")
                                  .withPassword("")
                     )
                .then()
                .expect().statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .when()
                .post("/auth/login");
    }

    @Test
    public void shouldFailIfPasswordIsNull() {
        given()
                .contentType(ContentType.JSON)
                .body(
                        DtoFactory.getInstance().createDto(Credentials.class)
                                  .withUsername("User")
                                  .withPassword(null)
                     )
                .then()
                .expect().statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .when()
                .post("/auth/login");
    }


    @Test
    public void shouldReturnToken() throws ApiException {
        //given
        Token expected = DtoFactory.getInstance().createDto(Token.class).withValue("v1");
        Mockito.when(dao.login(Mockito.any(Credentials.class))).thenReturn(expected);
        //when

        Token actual = given()
                .contentType(ContentType.JSON)
                .body(
                        DtoFactory.getInstance().createDto(Credentials.class)
                                  .withUsername("User")
                                  .withPassword("password")
                     )
                .then()
                .expect().statusCode(200)
                .when()
                .post("/auth/login").as(DtoServerImpls.TokenImpl.class, ObjectMapper.GSON);
        //then
        Assert.assertEquals(expected, actual);
        ArgumentCaptor<Credentials> argument = ArgumentCaptor.forClass(Credentials.class);
        verify(dao).login(argument.capture());
        Assert.assertEquals("User", argument.getValue().getUsername());
        Assert.assertEquals("password", argument.getValue().getPassword());

    }

    @Test
    public void shouldFailToLogoutIfTokenIsNull() {
        given()
                .then()
                .expect().statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .when()
                .post("/auth/logout");
    }

    @Test
    public void shouldCallLogoutOnDao() {
        //given
        //when
        given()
                .then()
                .expect().statusCode(204)
                .when()
                .post("/auth/logout?token=er00349");
        ArgumentCaptor<Token> argument = ArgumentCaptor.forClass(Token.class);
        //then
        verify(dao).logout(argument.capture());

    }

}