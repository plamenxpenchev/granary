package org.granary.database;

import org.granary.annotations.GuardedBy;
import org.granary.properties.Properties;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.granary.properties.PropertyKey;
import org.granary.properties.exception.PropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class ConnectionPoolImpl implements ConnectionPool {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPoolImpl.class);

    private static final String JDBC_DRIVER_VM_ARG_KEY = "jdbc.drivers";
    private static final String JDBC_DRIVER = "org.postgresql.Driver";

    private final boolean jdbcDriverInitialized;
    private final boolean connectionPoolInitialized;

    @GuardedBy("this")
    private PoolingDataSource<PoolableConnection> connectionPoolingDataSource;

    private ConnectionPoolImpl() {
        this.jdbcDriverInitialized = initializeJDBCDriver();
        this.connectionPoolInitialized = initializeConnectionPool();
    }

    /**
     * Checks if the JDBC driver class specified under {@code JDBC_DRIVER} is loaded.
     * If it is not, it attempts to load the JDBC driver class.
     *
     * @return {@code true} if the JDBC driver is successfully initialized.
     */
    private boolean initializeJDBCDriver() {
        if (JDBC_DRIVER.equals(System.getProperty(JDBC_DRIVER_VM_ARG_KEY))) {
            return true;
        } else {
            try {
                Class.forName(JDBC_DRIVER);
                return true;
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }

    /**
     * Reads the DB connection parameters, as well as the connection pool configuration values
     * from the appropriate .prs file, and attempts to initialize the Apache DHCP connection pool.
     *
     * @return {@code true} if the connection pool is successfully initialized.
     */
    private boolean initializeConnectionPool() {
        try {
            char[] dbPass = Properties.getSensitiveCharArray(PropertyKey.DB_PASS_KEY);
            ConnectionFactory connectionFactory =
                    new DriverManagerConnectionFactory(
                            Properties.getString(PropertyKey.DB_URL_KEY),
                            Properties.getString(PropertyKey.DB_USER_KEY),
                            dbPass); // array is cloned in constructor implementation
            Properties.wipeArray(dbPass);

            PoolableConnectionFactory poolableConnectionFactory =
                    new PoolableConnectionFactory(connectionFactory, null);

            GenericObjectPoolConfig<PoolableConnection> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(Properties.getInt(PropertyKey.DB_CONNECTION_POOL_MAX_TOTAL_KEY));
            config.setMaxIdle(Properties.getInt(PropertyKey.DB_CONNECTION_POOL_MAX_IDLE_KEY));
            config.setMinIdle(Properties.getInt(PropertyKey.DB_CONNECTION_POOL_MIN_IDLE_KEY));

            ObjectPool<PoolableConnection> connectionPool =
                    new GenericObjectPool<>(poolableConnectionFactory, config);
            poolableConnectionFactory.setPool(connectionPool);

            synchronized (this) {
                connectionPoolingDataSource = new PoolingDataSource<>(connectionPool);
                LOG.info("Successfully established DB connection pool.");
            }
            return true;
        } catch (PropertyException e) {
            return false;
        }
    }

    @Override
    public void close() throws SQLException {
        synchronized (this) {
            if (connectionPoolingDataSource != null) {
                connectionPoolingDataSource.close();
            }
        }
    }

    public static class ConnectionPoolBuilder {

        public ConnectionPool build() {
            ConnectionPoolImpl pool = new ConnectionPoolImpl();
            if (pool.jdbcDriverInitialized && pool.connectionPoolInitialized) {
                return pool;
            } else {
                try {
                    pool.close();
                } catch (SQLException e) {
                }
                return null;
            }
        }
    }
}
