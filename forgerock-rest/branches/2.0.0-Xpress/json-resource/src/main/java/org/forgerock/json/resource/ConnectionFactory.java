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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright 2012 ForgeRock AS. All rights reserved.
 */

package org.forgerock.json.resource;


/**
 * A connection factory provides an interface for obtaining a connection to a
 * JSON resource provider. Connection factories can be used to wrap other
 * connection factories in order to provide enhanced capabilities in a manner
 * which is transparent to the application. For example:
 * <ul>
 * <li>Connection pooling
 * <li>Load balancing
 * <li>Keep alive
 * <li>Logging connections
 * <li>Read-only connections
 * </ul>
 * An application typically obtains a connection from a connection factory,
 * performs one or more operations, and then closes the connection. Applications
 * should aim to close connections as soon as possible in order to avoid
 * resource contention.
 */
public interface ConnectionFactory {

    /**
     * Returns a connection to the JSON resource provider associated with this
     * connection factory. The connection returned by this method can be used
     * immediately.
     *
     * @return A connection to the JSON resource provider associated with this
     *         connection factory.
     * @throws ResourceException
     *             If the connection request failed for some reason.
     */
    Connection getConnection() throws ResourceException;

    /**
     * Asynchronously obtains a connection to the JSON resource provider
     * associated with this connection factory. The returned
     * {@code FutureResult} can be used to retrieve the completed connection.
     * Alternatively, if a {@code ResultHandler} is provided, the handler will
     * be notified when the connection is available and ready for use.
     *
     * @param handler
     *            The completion handler, or {@code null} if no handler is to be
     *            used.
     * @return A future which can be used to retrieve the connection.
     */
    FutureResult<Connection> getConnectionAsync(ResultHandler<Connection> handler);
}
