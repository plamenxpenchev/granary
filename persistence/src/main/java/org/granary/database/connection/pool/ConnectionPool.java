package org.granary.database.connection.pool;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool extends AutoCloseable {
    public Connection getConnection() throws SQLException;
    public Connection getTransactionalConnection() throws SQLException;
}
