package org.granary.properties.exception;

import java.io.Serial;

public class PropertyNotFoundException extends PropertyException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PropertyNotFoundException(String property) {
        super(String.format("Property '%s' not found.", property));
    }
}
