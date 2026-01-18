package org.granary.server;

import org.granary.database.connection.pool.ConnectionPool;
import org.granary.database.connection.pool.ConnectionPoolImpl;
import org.granary.database.versions.VersionUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Granary {

    private static final Logger LOG = LoggerFactory.getLogger(Granary.class);

    public static void main(String[] args) {
        Connection connection = null;
        try (ConnectionPool connectionPool = new ConnectionPoolImpl.ConnectionPoolBuilder().build()) {
            connection = connectionPool.getTransactionalConnection();
            VersionUpdater.update(connection);
            LOG.info("Verified database version.");
        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOG.error(e.getMessage());
            }
        }
    }
}
