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
 * Copyright 2014-2015 ForgeRock AS.
 */

package org.forgerock.jaspi.runtime;

import org.forgerock.audit.events.AuditEvent;
import org.forgerock.audit.events.AuthenticationAuditEventBuilder;
import org.forgerock.audit.events.AuthenticationAuditEventBuilder.Status;
import org.forgerock.json.fluent.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.forgerock.json.fluent.JsonValue.*;

/**
 * <p>Is responsible for tracking the auditing of an authentication attempt including auditing each of the modules that
 * are executed and the overall result of the authentication.</p>
 *
 * <p>The audit record will include a unique request id, the principal (if authentication was successful) and a session
 * id (if a session was created).</p>
 *
 * @since 1.5.0
 */
public class AuditTrail {

    /**
     * MessageInfo map key for retrieving the audit trail instance.
     */
    public static final String AUDIT_TRAIL_KEY = "org.forgerock.authentication.audit.trail";

    /**
     * MessageInfo map key for setting additional audit information from a module.
     */
    public static final String AUDIT_INFO_KEY = "org.forgerock.authentication.audit.info";

    /**
     * MessageInfo map key for setting the principal that the auth module has identified that will be set in the audit
     * log entry.
     */
    public static final String AUDIT_PRINCIPAL_KEY = JaspiRuntime.ATTRIBUTE_AUTH_PRINCIPAL;

    /**
     * MessageInfo map key for setting the session id for the authentication request.
     */
    public static final String AUDIT_SESSION_ID_KEY = "org.forgerock.authentication.audit.session.id";

    /**
     * MessageInfo map key for setting the reason for the module failure.
     */
    public static final String AUDIT_FAILURE_REASON_KEY = "org.forgerock.authentication.audit.failure.reason";

    private static final String MODULE_ID_KEY = "moduleId";
    private static final String RESULT_KEY = "result";
    private static final String PRINCIPAL_KEY = "principal";
    private static final String CONTEXT_KEY = "context";
    private static final String SESSION_ID_KEY = "sessionId";
    private static final String REQUEST_ID_KEY = "requestId";
    private static final String ENTRIES_KEY = "entries";
    private static final String REASON_KEY = "reason";
    private static final String INFO_KEY = "info";
    private static final String SUCCESSFUL_RESULT = "SUCCESSFUL";
    private static final String FAILED_RESULT = "FAILED";

    /**
     * Support transactionId for commons audit
     */
    private static final String TRANSACTION_ID_KEY = "transactionId";

    private final AuditApi api;
    private final List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();
    private final JsonValue auditMessage = json(object(field(ENTRIES_KEY, entries)));

    /**
     * Constructs a new AuditTrail instance.
     *
     * @param api An instance of the {@code AuditApi}.
     */
    public AuditTrail(AuditApi api, Map<String, Object> contextMap, String transactionId) {
        this.api = api;
        auditMessage.put(CONTEXT_KEY, contextMap);
        if (transactionId == null) {
            auditMessage.put(TRANSACTION_ID_KEY, UUID.randomUUID().toString());
        } else {
            auditMessage.put(TRANSACTION_ID_KEY, transactionId);
        }
    }

    /**
     * Audits a module as having completed successfully.
     *
     * @param moduleId The id of the module.
     * @param info The module audit info map.
     */
    public void auditSuccess(String moduleId, Map<String, Object> info) {
        entries.add(json(object(
                field(MODULE_ID_KEY, moduleId),
                field(RESULT_KEY, SUCCESSFUL_RESULT),
                field(INFO_KEY, info))
        ).asMap());
    }

    /**
     * Audits a module as having completed as a failure.
     *
     * @param moduleId The id of the module.
     * @param reason The reason the module is reporting a failure.
     * @param info The module audit info map.
     */
    public void auditFailure(String moduleId, Map<String, Object> reason, Map<String, Object> info) {
        entries.add(json(object(
                field(MODULE_ID_KEY, moduleId),
                field(RESULT_KEY, FAILED_RESULT),
                field(REASON_KEY, reason),
                field(INFO_KEY, info))
        ).asMap());
    }

    /**
     * Completes the entire audit record as successful.
     *
     * @param principal The principal. Must have been set by the successful module.
     */
    void completeAuditAsSuccessful(String principal) {
        auditMessage.put(RESULT_KEY, "SUCCESSFUL").put(PRINCIPAL_KEY, array(principal));
    }

    /**
     * <p>Completes the entire audit record as a failure.</p>
     *
     * <p>No auth module successfully completed so will check each auth module entry's audit info map for the
     * {@link #AUDIT_PRINCIPAL_KEY} key and will created an ordered list of all non-null principal values.</p>
     */
    void completeAuditAsFailure() {

        List<Object> principals = array();

        for (Map<String, Object> entry : entries) {

            Map<String, Object> auditInfo = (Map<String, Object>) entry.get(INFO_KEY);

            if (auditInfo == null) {
                continue;
            }

            String principal = (String) auditInfo.get(AUDIT_PRINCIPAL_KEY);

            if (principal != null) {
                principals.add(principal);
            }
        }

        auditMessage.put(RESULT_KEY, "FAILED").put(PRINCIPAL_KEY, principals);
    }

    /**
     * Performs the actual audit by calling the {@link AuditApi#audit(JsonValue)} with the audit record.
     */
    void audit() {
        final Status status =
                auditMessage.get(RESULT_KEY).asEnum(Status.class);

        String sessionId = null;
        if (auditMessage.isDefined(SESSION_ID_KEY) && !auditMessage.get(SESSION_ID_KEY).isNull()) {
            sessionId = auditMessage.get(SESSION_ID_KEY).asString();
        }

        final List<String> principals = auditMessage.get(PRINCIPAL_KEY).asList(String.class);
        final String principal = (principals != null && principals.size() >= 1) ? principals.get(0) : "";

        AuditEvent auditEvent = AuthenticationAuditEventBuilder.authenticationEvent()
                .result(status)
                .principal(principals)
                .context(auditMessage.get(CONTEXT_KEY).asMap())
                .authentication(principal)
                .entries(auditMessage.get(ENTRIES_KEY).asList())
                .eventName("authentication")
                .transactionId(auditMessage.get(TRANSACTION_ID_KEY).asString())
                .sessionId(sessionId)
                .toEvent();
        api.audit(auditEvent.getValue());
    }

    /**
     * Sets the session id on the audit record, if a session has been created. Will not set the session id on the audit
     * record if it is {@code null} or an empty {@code String}.
     *
     * @param sessionId The session id.
     */
    public void setSessionId(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            auditMessage.put(SESSION_ID_KEY, sessionId);
        }
    }

    /**
     * Gets the request id.
     *
     * @return The request id.
     *
     * @deprecated use {@link #getTransactionId()} instead.
     */
    @Deprecated
    String getRequestId() {
        return auditMessage.get(REQUEST_ID_KEY).asString();
    }

    /**
     * Gets the transaction id.
     *
     * @return The transaction id.
     */
    String getTransactionId() {
        return auditMessage.get(TRANSACTION_ID_KEY).asString();
    }

    /**
     * Gets the list of failure reasons from each of the module entries.
     *
     * @return A {@code List} of failure reasons as {@code Map}s of {@code String} to {@code Object}s.
     */
    List<Map<String, Object>> getFailureReasons() {

        List<Map<String, Object>> failureReasons = new ArrayList<Map<String, Object>>();

        for (Map<String, Object> entry : entries) {

            Map<String, Object> failureReason = (Map<String, Object>) entry.get(REASON_KEY);

            if (failureReason == null || failureReason.isEmpty()) {
                continue;
            }

            failureReasons.add(failureReason);
        }

        return failureReasons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return auditMessage.toString();
    }
}
