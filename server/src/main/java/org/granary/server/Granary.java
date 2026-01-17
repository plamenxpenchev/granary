package org.granary.server;

import org.granary.database.connection.pool.ConnectionPool;
import org.granary.database.connection.pool.ConnectionPoolImpl;

import java.sql.Connection;
import java.sql.SQLException;

public class Granary {
    public static void main(String[] args) {
        Connection connection = null;
        try (ConnectionPool connectionPool = new ConnectionPoolImpl.ConnectionPoolBuilder().build()) {
            connection = connectionPool.getConnection();
            System.out.println("Hello World!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
