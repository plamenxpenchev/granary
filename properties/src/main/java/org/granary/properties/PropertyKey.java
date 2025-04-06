package org.granary.properties;

public enum PropertyKey {

    DB_URL_KEY("granary.db.url"),
    DB_USER_KEY("granary.db.user"),
    DB_PASS_KEY("granary.db.pass"),
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
