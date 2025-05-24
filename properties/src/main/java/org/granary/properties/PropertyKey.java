package org.granary.properties;

public enum PropertyKey {

    DB_HOST_KEY("granary.db.host"),
    DB_NAME_KEY("granary.db.name"),
    DB_USER_KEY("granary.db.user"),
    DB_PASS_KEY("granary.db.pass"),
    DB_PORT_KEY("granary.db.port"),
    DB_CONNECTION_POOL_MAX_TOTAL_KEY("granary.db.connection.pool.max.total"),
    DB_CONNECTION_POOL_MAX_IDLE_KEY("granary.db.connection.pool.max.idle"),
    DB_CONNECTION_POOL_MIN_IDLE_KEY("granary.db.connection.pool.min.idle");

    private final String key;

    PropertyKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return this.key;
    }
}
