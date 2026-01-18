package org.granary.server;

import org.granary.database.DatabaseTest;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GranaryTest extends DatabaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(GranaryTest.class);

    @Test
    public void createTableTest() throws SQLException {
        Connection trConn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            trConn = connectionPool.getTransactionalConnection();

            ps = trConn.prepareStatement("CREATE TABLE test_table(test_field_1 VARCHAR(255), test_field_2 INT)");
            ps.executeUpdate();
            ps.close();

            ps = trConn.prepareStatement("INSERT INTO test_table(test_field_1, test_field_2) " +
                    "VALUES ('val1', 1), ('val2', 2)");
            ps.executeUpdate();
            trConn.commit();
            ps.close();

            ps = trConn.prepareStatement("SELECT COUNT(*) FROM test_table");
            rs = ps.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            assertEquals(2, count);
            LOG.info("Two results found - Success.");
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (trConn != null) {
                trConn.close();
            }
        }
    }
}
