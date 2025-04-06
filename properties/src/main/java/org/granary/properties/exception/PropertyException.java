package org.granary.properties.exception;

import java.io.Serial;

public class PropertyException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public PropertyException(String message) {
        super(message);
    }

    public PropertyException(Throwable throwable) {
        super(throwable);
    }
}
