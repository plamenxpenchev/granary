package org.granary.server;

import org.granary.database.ConnectionPool;
import org.granary.database.ConnectionPoolImpl;

public class Granary {
    public static void main(String[] args) {
        try (ConnectionPool connectionPool = new ConnectionPoolImpl.ConnectionPoolBuilder().build()) {
            System.out.println("Hello World!");
        } catch (Exception e) {
        }
    }
}
