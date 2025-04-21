package org.granary.database.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionPool extends AutoCloseable {
    public Connection getConnection() throws SQLException;
    public Connection getTransactionalConnection() throws SQLException;
}
