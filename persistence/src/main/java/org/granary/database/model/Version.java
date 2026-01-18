package org.granary.database.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Version implements Result {

    private final short version;

    private Version(VersionBuilder builder) {
        this.version = builder.version;
    }

    public short getVersion() {
        return version;
    }

    public static Version mapRow(ResultSet resultSet) throws SQLException {
        return new Version.VersionBuilder()
                .withVersion(resultSet.getShort("version"))
                .build();
    }

    public static class VersionBuilder {

        private short version;

        public VersionBuilder withVersion(short version) {
            this.version = version;
            return this;
        }

        public Version build() {
            return new Version(this);
        }
    }
}
