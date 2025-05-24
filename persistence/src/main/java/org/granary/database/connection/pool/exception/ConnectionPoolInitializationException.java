package org.granary.database.connection.pool.exception;

import java.io.Serial;

public class ConnectionPoolInitializationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConnectionPoolInitializationException(String message) {
        super(message);
    }
}
