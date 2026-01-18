package org.granary.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Boolean implements Result {

    private final boolean result;

    private Boolean(Boolean.BooleanResultBuilder builder) {
        this.result = builder.result;
    }

    public boolean getResult() {
        return result;
    }

    public static Boolean mapRow(ResultSet resultSet) throws SQLException {
        return new Boolean.BooleanResultBuilder()
                .withResult(resultSet.getBoolean(1))
                .build();
    }

    public static class BooleanResultBuilder {

        private boolean result;

        public BooleanResultBuilder withResult(boolean result) {
            this.result = result;
            return this;
        }

        public Boolean build() {
            return new Boolean(this);
        }
    }
}
