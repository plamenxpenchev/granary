package org.granary.server;

import org.granary.database.connection.pool.ConnectionPool;
import org.granary.database.connection.pool.ConnectionPoolImpl;

import java.sql.Connection;

public class Granary {
    public static void main(String[] args) {
        try (ConnectionPool connectionPool = new ConnectionPoolImpl.ConnectionPoolBuilder().build()) {
            Connection connection = connectionPool.getConnection();
            connection.close();
            System.out.println("Hello World!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
