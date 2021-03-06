/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014 ForgeRock AS.
 */

package org.forgerock.caf.authn;

import org.forgerock.caf.authn.test.modules.AuthModuleOne;
import org.forgerock.caf.authn.test.modules.SessionAuthModule;
import org.hamcrest.Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.forgerock.caf.authn.AuditParameters.auditParams;
import static org.forgerock.caf.authn.AuthModuleParameters.moduleArray;
import static org.forgerock.caf.authn.AuthModuleParameters.moduleParams;
import static org.forgerock.caf.authn.BodyMatcher.resourceMatcher;
import static org.forgerock.caf.authn.TestFramework.runTest;
import static org.forgerock.caf.authn.TestFramework.setUpConnection;
import static org.forgerock.caf.authn.test.modules.AuthModuleOne.AUTH_MODULE_ONE_CONTEXT_ENTRY;
import static org.forgerock.caf.authn.test.modules.AuthModuleOne.AUTH_MODULE_ONE_PRINCIPAL;
import static org.forgerock.caf.authn.test.modules.SessionAuthModule.*;
import static org.forgerock.json.fluent.JsonValue.object;

/**
 * Functional tests for the JASPI runtime when the protected resource causes the response to be committed.
 *
 * @since 1.5.0
 */
@Test(testName = "CommittedResponse")
public class CommittedResponseIT {

    private final Logger logger = LoggerFactory.getLogger(CommittedResponseIT.class);

    @BeforeClass
    public void setUp() {
        setUpConnection();
    }

    @DataProvider(name = "sessionModuleOnlyData")
    private Object[][] sessionModuleOnlyData() {
        return new Object[][]{
            /**
             * Session Module Only - SUCCESS:SEND_SUCCESS
             *
             * Set up:
             * * Session Module configured
             * * No Auth Modules configured
             * * Session Module #validateRequest will return SUCCESS
             * * Response will not be committed after calling resource
             * * Session Module #secureResponse will return SEND_SUCCESS
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             *
             * Expected Result:
             * * HTTP 201 status
             * * HTTP response from resource
//                 * * Does not audit Session Module success
             * * Does not audit overall result as success
//                 * * No state cookie on response
             * * Requested resource called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Session Module Only - SUCCESS:SEND_SUCCESS",
                moduleParams(SessionAuthModule.class, "SESSION", SUCCESS_AUTH_STATUS, SEND_SUCCESS_AUTH_STATUS),
                moduleArray(), 201, true, resourceMatcher(SESSION_MODULE_PRINCIPAL, SESSION_MODULE_CONTEXT_ENTRY),
                null
            },
            /**
             * Session Module Only - SUCCESS:SEND_FAILURE
             *
             * Set up:
             * * Session Module configured
             * * No Auth Modules configured
             * * Session Module #validateRequest will return SUCCESS
             * * Response will not be committed after calling resource
             * * Session Module #secureResponse will return SEND_FAILURE
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             *
             * Expected Result:
             * * HTTP 201 status
             * * No HTTP response detailing the cause of the failure
             * * HTTP response from resource
//                 * * Does not audit Session Module success
             * * Does not audit overall result as success
//                 * * No state cookie on response
             * * Requested resource called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Session Module Only - SUCCESS:SEND_FAILURE",
                moduleParams(SessionAuthModule.class, "SESSION", SUCCESS_AUTH_STATUS, SEND_FAILURE_AUTH_STATUS),
                moduleArray(), 201, true,
                resourceMatcher(SESSION_MODULE_PRINCIPAL, SESSION_MODULE_CONTEXT_ENTRY),
                null
            },
            /**
             * Session Module Only - SUCCESS:AuthException
             *
             * Set up:
             * * Session Module configured
             * * No Auth Modules configured
             * * Session Module #validateRequest will return SUCCESS
             * * Response will not be committed after calling resource
             * * Session Module #secureResponse will throw AuthException
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             *
             * Expected Result:
             * * HTTP 201 status
             * * No HTTP response detailing the cause of the failure
             * * HTTP response from resource
//                 * * Does not audit Session Module success
             * * Does not audit overall result as success
//                 * * No state cookie on response
             * * Requested resource called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Session Module Only - SUCCESS:AuthException",
                moduleParams(SessionAuthModule.class, "SESSION", SUCCESS_AUTH_STATUS, null),
                moduleArray(), 201, true,
                resourceMatcher(SESSION_MODULE_PRINCIPAL, SESSION_MODULE_CONTEXT_ENTRY),
                null
            },
        };
    }

    @DataProvider(name = "authModuleOnlyData")
    private Object[][] authModuleOnlyData() {
        return new Object[][]{
            /**
             * Single Auth Module Only - SUCCESS:SEND_SUCCESS
             *
             * Set up:
             * * No Session Module configured
             * * Single Auth Module configured
             * * Auth Module #validateRequest will return SUCCESS
             * * Response will not be committed after calling resource
             * * Auth Module #secureResponse will return SEND_SUCCESS
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             *
             * Expected Result:
             * * HTTP 201 status
             * * HTTP response from resource
//                 * * Audit Auth Module success
             * * Audit overall result as success
//                 * * No state cookie on response
             * * Requested resource called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Single Auth Module Only - SUCCESS:SEND_SUCCESS",
                null, moduleArray(
                    moduleParams(AuthModuleOne.class, "AUTH-MODULE-ONE", SUCCESS_AUTH_STATUS,
                        SEND_SUCCESS_AUTH_STATUS)),
                201, true,
                resourceMatcher(AUTH_MODULE_ONE_PRINCIPAL, AUTH_MODULE_ONE_CONTEXT_ENTRY),
                auditParams("SUCCESS")
            },
            /**
             * Single Auth Module Only - SUCCESS:SEND_FAILURE
             *
             * Set up:
             * * No Session Module configured
             * * Single Auth Module configured
             * * Auth Module #validateRequest will return SUCCESS
             * * Response will not be committed after calling resource
             * * Auth Module #secureResponse will return SEND_FAILURE
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             *
             * Expected Result:
             * * HTTP 201 status
             * * No HTTP response detailing the cause of the failure
             * * HTTP response from resource
//                 * * Audit Auth Module success
             * * Audit overall result as success
//                 * * No state cookie on response
             * * Requested resource called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Single Auth Module Only - SUCCESS:SEND_FAILURE",
                null, moduleArray(
                    moduleParams(AuthModuleOne.class, "AUTH-MODULE-ONE", SUCCESS_AUTH_STATUS,
                        SEND_FAILURE_AUTH_STATUS)),
                201, true,
                resourceMatcher(AUTH_MODULE_ONE_PRINCIPAL, AUTH_MODULE_ONE_CONTEXT_ENTRY),
                auditParams("SUCCESS")
            },
            /**
             * Single Auth Module Only - SUCCESS:AuthException
             *
             * Set up:
             * * No Session Module configured
             * * Single Auth Module configured
             * * Auth Module #validateRequest will return SUCCESS
             * * Response will not be committed after calling resource
             * * Auth Module #secureResponse will throw AuthException
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             *
             * Expected Result:
             * * HTTP 201 status
             * * No HTTP response detailing the cause of the failure
             * * HTTP response from resource
//                 * * Audit Auth Module success
             * * Audit overall result as success
//                 * * No state cookie on response
             * * Requested resource called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Single Auth Module Only - SUCCESS:AuthException",
                null, moduleArray(
                    moduleParams(AuthModuleOne.class, "AUTH-MODULE-ONE", SUCCESS_AUTH_STATUS, null)), 201, true,
                resourceMatcher(AUTH_MODULE_ONE_PRINCIPAL, AUTH_MODULE_ONE_CONTEXT_ENTRY),
                auditParams("SUCCESS")
            },
        };
    }

    @DataProvider(name = "sessionModuleAndAuthModuleData")
    private Object[][] sessionModuleAndAuthModuleData() {
        return new Object[][]{
            /**
             * Session Module and single Auth Module - SUCCESS:SEND_SUCCESS & AuthException:AuthException
             *
             * Set up:
             * * Session Module configured
             * * Single Auth Module configured
             * * Session Module #validateRequest will return SUCCESS
             * * Session Module #secureResponse will return SEND_SUCCESS
             * * Auth Module One #validateRequest will throw AuthException (but should not be called)
             * * Auth Module One #secureResponse will throw AuthException (but should not be called)
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             * Expected Result:
             * * HTTP 201 status
//                 * * Does not audit Session Module success
             * * Does not audit overall result as success
//                 * * No state cookie on response
             * * Requested resource will be called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Session Module and Single Auth Module - S:SS & AE:AE",
                moduleParams(SessionAuthModule.class, "SESSION", SUCCESS_AUTH_STATUS, SEND_SUCCESS_AUTH_STATUS),
                    moduleArray(
                        moduleParams(AuthModuleOne.class, "AUTH-MODULE-ONE", null, null)
                ), 201, true, object(), null},
            /**
             * Session Module and single Auth Module - SEND_FAILURE:SEND_SUCCESS & SUCCESS:SEND_SUCCESS
             *
             * Set up:
             * * Session Module configured
             * * Single Auth Module configured
             * * Session Module #validateRequest will return SUCCESS
             * * Session Module #secureResponse will return SEND_SUCCESS
             * * Auth Module One #validateRequest will SUCCESS
             * * Auth Module One #secureResponse will return SEND_SUCCESS
             * * Resource will set response status to 201, set header and cause response to be committed
             *
             * Expected Result:
             * * HTTP 201 status
//                 * * Audit Session Module success
//                 * * Audit Auth Module One success
             * * Audit overall result as success
//                 * * No state cookie on response
             * * Requested resource will be called (resource will set header 'RESOURCE_CALLED':true on response)
             *
             */
            {"Session Module and Single Auth Module - SF:SS & S:SS",
                moduleParams(SessionAuthModule.class, "SESSION", SEND_FAILURE_AUTH_STATUS,
                    SEND_SUCCESS_AUTH_STATUS),
                moduleArray(
                    moduleParams(AuthModuleOne.class, "AUTH-MODULE-ONE", SUCCESS_AUTH_STATUS,
                        SEND_SUCCESS_AUTH_STATUS)
                ), 201, true, object(), auditParams("SUCCESS")},
        };
    }

    @Test (dataProvider = "sessionModuleOnlyData")
    public void sessionModuleOnlyWithResourceCommittingResponse(String dataName,
            AuthModuleParameters sessionModuleParams, List<AuthModuleParameters> authModuleParametersList,
            int expectedResponseStatus, boolean expectResourceToBeCalled, Map<String, Matcher<?>> expectedBody,
            AuditParameters auditParams) {
        logger.info("Running sessionModuleOnlyWithResourceCommittingResponse test with data set: " + dataName);
        runTest("/protected/resource/committing", sessionModuleParams, authModuleParametersList,
                expectedResponseStatus, expectResourceToBeCalled, expectedBody, auditParams);
    }

    @Test (dataProvider = "authModuleOnlyData")
    public void authModuleOnlyWithResourceCommittingResponse(String dataName,
            AuthModuleParameters sessionModuleParams, List<AuthModuleParameters> authModuleParametersList,
            int expectedResponseStatus, boolean expectResourceToBeCalled, Map<String, Matcher<?>> expectedBody,
            AuditParameters auditParams) {
        logger.info("Running authModuleOnlyWithResourceCommittingResponse test with data set: " + dataName);
        runTest("/protected/resource/committing", sessionModuleParams, authModuleParametersList,
                expectedResponseStatus, expectResourceToBeCalled, expectedBody, auditParams);
    }

    @Test (dataProvider = "sessionModuleAndAuthModuleData")
    public void sessionModuleAndAuthModuleWithResourceCommittingResponse(String dataName,
            AuthModuleParameters sessionModuleParams, List<AuthModuleParameters> authModuleParametersList,
            int expectedResponseStatus, boolean expectResourceToBeCalled, Map<String, Matcher<?>> expectedBody,
            AuditParameters auditParams) {
        logger.info("Running sessionModuleAndAuthModuleWithResourceCommittingResponse test with data set: " + dataName);
        runTest("/protected/resource/committing", sessionModuleParams, authModuleParametersList,
                expectedResponseStatus, expectResourceToBeCalled, expectedBody, auditParams);
    }
}