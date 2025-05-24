package org.granary.database;

import org.granary.database.connection.pool.ConnectionPool;
import org.granary.database.connection.pool.ConnectionPoolImpl;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.granary.database.connection.pool.exception.ConnectionPoolInitializationException;
import org.granary.properties.Properties;
import org.granary.properties.PropertyKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class DatabaseTest {

    @Spy
    public static ConnectionPool connectionPool;

    private static PostgreSQLContainer<?> postgres;

    @BeforeAll
    public static void setupPostgres() throws Exception {
        try {
            int dbPort = Properties.getInt(PropertyKey.DB_PORT_KEY);
            postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.0"))
                    .withDatabaseName(Properties.getString(PropertyKey.DB_NAME_KEY))
                    .withUsername(Properties.getString(PropertyKey.DB_USER_KEY))
                    .withPassword(Properties.getString(PropertyKey.DB_PASS_KEY))
                    .withExposedPorts(dbPort)
                    .withCreateContainerCmdModifier(
                            e -> e.withHostConfig(
                                    HostConfig.newHostConfig()
                                            .withPortBindings(
                                                    new PortBinding(
                                                            Ports.Binding.bindPort(dbPort),
                                                            new ExposedPort(dbPort)))));
            postgres.start();
            try (MockedStatic<ConnectionPoolImpl> connectionPoolMockedStatic = Mockito.mockStatic(ConnectionPoolImpl.class)) {
                connectionPoolMockedStatic.when(ConnectionPoolImpl::getJDBCUrl).thenReturn(postgres.getJdbcUrl());
                connectionPool = new ConnectionPoolImpl.ConnectionPoolBuilder().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            close();
            throw new ConnectionPoolInitializationException(e.getMessage());
        }
    }

    @AfterAll
    public static void tearDownPostgres() throws Exception {
        close();
    }

    private static void close() throws Exception {
        try {
            if (connectionPool != null) {
                connectionPool.close();
                connectionPool = null;
            }
        } finally {
            if (postgres != null) {
                postgres.stop();
                postgres = null;
            }
        }
    }
}
