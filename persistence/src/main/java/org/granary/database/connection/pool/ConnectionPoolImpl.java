package org.granary.database.connection.pool;

import org.granary.annotations.GuardedBy;
import org.granary.annotations.ThreadSafe;
import org.granary.database.connection.pool.exception.ConnectionPoolInitializationException;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import static java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

@ThreadSafe
public class ConnectionPoolImpl implements ConnectionPool {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionPoolImpl.class);

    private static final String JDBC_DRIVER_VM_ARG_KEY = "jdbc.drivers";
    private static final String JDBC_DRIVER = "org.postgresql.Driver";

    private final boolean jdbcDriverInitialized;
    private final boolean connectionPoolInitialized;

    private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private ReadLock readLock = reentrantReadWriteLock.readLock();
    private WriteLock writeLock = reentrantReadWriteLock.writeLock();

    /**
     * Apache DBCP internally synchronizes concurrent invocations to the pool
     * via the thread-safety guarantees of the {@link GenericObjectPool} implementation.
     *
     * The builder pattern guarantees thread-safety for obtaining connections during after
     * the instantiation of the connection pool.
     *
     * In general, closing the pool does not close the connections that have already been borrowed by the application.
     * It makes it so that new connections cannot be borrowed. When the connections currently borrowed are closed,
     * they will attempt to return to the pool, but when that throws, they will handle the exception
     * by closing the underlying physical JDBC connection.
     *
     * Attempting to borrow a new connection from a closed pool throws {@link IllegalStateException}.
     * From that point of view the handling with read and write locks here can be considered overkill,
     * but the following is true:
     *      the exception in that case is not wrapped in a {@link SQLException}
     *      we want atomicity/null-safety guarantees on accessing this variable
     */
    @GuardedBy("reentrantReadWriteLock")
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
            LOG.info("Successfully loaded JDBC driver class.");
            return true;
        } else {
            try {
                Class.forName(JDBC_DRIVER);
                LOG.info("Successfully loaded JDBC driver class.");
                return true;
            } catch (ClassNotFoundException e) {
                LOG.error(e.getMessage(), e);
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
                            getJDBCUrl(),
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

            connectionPoolingDataSource = new PoolingDataSource<>(connectionPool);
            LOG.info("Successfully established DB connection pool.");
            return true;
        } catch (PropertyException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    public static String getJDBCUrl() throws PropertyException {
        return String.format(
                "jdbc:postgresql://%s:%d/%s",
                Properties.getString(PropertyKey.DB_HOST_KEY),
                Properties.getInt(PropertyKey.DB_PORT_KEY),
                Properties.getString(PropertyKey.DB_NAME_KEY));
    }

    @Override
    public void close() throws SQLException {
        writeLock.lock();
        try {
            if (connectionPoolingDataSource != null) {
                connectionPoolingDataSource.close();
                connectionPoolingDataSource = null;
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(true);
    }

    @Override
    public Connection getTransactionalConnection() throws SQLException {
        return getConnection(false);
    }

    private Connection getConnection(boolean autoCommit) throws SQLException {
        Connection connection;
        readLock.lock();
        try {
            if (connectionPoolingDataSource != null) {
                connection = connectionPoolingDataSource.getConnection();
            } else {
                throw new SQLException("The JDBC connection pool is closed.");
            }
        } finally {
            readLock.unlock();
        }
        if (connection != null) {
            connection.setAutoCommit(autoCommit);
        }
        return connection;
    }

    public static class ConnectionPoolBuilder {

        public ConnectionPool build() throws ConnectionPoolInitializationException {

            ConnectionPoolImpl pool = new ConnectionPoolImpl();
            if (pool.jdbcDriverInitialized && pool.connectionPoolInitialized) {
                return pool;
            } else {
                try {
                    pool.close();
                } catch (SQLException e) {
                }

                List<String> initializationFailures = new ArrayList<>();
                if (!pool.jdbcDriverInitialized) {
                    initializationFailures.add("JDBC Driver");
                }
                if (!pool.connectionPoolInitialized) {
                    initializationFailures.add("Apache DBCP Connection Pool");
                }
                throw new ConnectionPoolInitializationException(
                        "Error building the ConnectionPool instance, could not instantiate the " +
                                String.join(", ", initializationFailures));
            }
        }
    }
}
