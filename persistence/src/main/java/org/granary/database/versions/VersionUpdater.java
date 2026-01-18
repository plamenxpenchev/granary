package org.granary.database.versions;

import org.granary.database.model.Boolean;
import org.granary.database.model.Version;
import org.granary.database.model.constants.RootItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class VersionUpdater {

    private static Logger LOG = LoggerFactory.getLogger(VersionUpdater.class);
    private static final short LATEST_VERSION = 1;

    public static void update(Connection transactionalConnection) throws IOException, SQLException {
        short dbVersion = getVersion(transactionalConnection);
        while (dbVersion < LATEST_VERSION) {
            dbVersion = nextUpdate(transactionalConnection, dbVersion);
        }
        LOG.info("Currently at latest DB version. Nothing to update.");
    }

    private static short nextUpdate(Connection transactionalConnection, short currentDBVersion) throws IOException, SQLException {
        switch (currentDBVersion) {
            case 0:
                createRootItem(transactionalConnection, (short) 1);
                LOG.info("Successfully updated DB to version 1 (root item created).");
                return 1;
            case -1:
                createTables(transactionalConnection, (short) 0);
                LOG.info("Successfully updated DB to version 0 (tables created).");
                return 0;
            default:
                throw new RuntimeException("Unknown DB version.");
        }
    }

    private static void createRootItem(Connection transactionalConnection, short dbVersion) throws SQLException {
        try {
            PreparedStatement createRootItemPS = transactionalConnection.prepareStatement(
                "INSERT INTO items (" +
                        "id, " +
                        "parent_id, " +
                        "hierarchy_level, " +
                        "is_leaf, " +
                        "name) " +
                        "VALUES (?, ?, ?, ?, ?)");

            createRootItemPS.setString(1, RootItem.ID);
            createRootItemPS.setString(2, RootItem.PARENT_ID);
            createRootItemPS.setShort(3, RootItem.HIERARCHY_LEVEL);
            createRootItemPS.setBoolean(4, RootItem.IS_LEAF);
            createRootItemPS.setString(5, RootItem.NAME);

            createRootItemPS.executeUpdate();
            createRootItemPS.close();

            createRootItemPS = transactionalConnection.prepareStatement(
                    "INSERT INTO price_tags (" +
                            "item_id, " +
                            "price, " +
                            "discount_percentage) " +
                            "VALUES (?, ?, ?)");

            createRootItemPS.setString(1, RootItem.ID);
            createRootItemPS.setBigDecimal(2, RootItem.PRICE);
            createRootItemPS.setNull(3, Types.SMALLINT, null);

            createRootItemPS.executeUpdate();
            createRootItemPS.close();

            PreparedStatement dbVersionPS = transactionalConnection.prepareStatement(
                    "UPDATE db_version SET version = ?");
            dbVersionPS.setShort(1, dbVersion);
            dbVersionPS.executeUpdate();
            transactionalConnection.commit();
            dbVersionPS.close();
        } catch (SQLException e) {
            transactionalConnection.rollback();
            throw e;
        }
    }

    private static void createTables(Connection transactionalConnection, short dbVersion) throws IOException, SQLException {

        List<String> sqlStatements =
                SQLScriptParser.parse("0_create_tables.sql");

        List<PreparedStatement> preparedStatements = new ArrayList<>();
        for (String sqlStatement : sqlStatements) {
            preparedStatements.add(transactionalConnection.prepareStatement(sqlStatement));
        }

        PreparedStatement dbVersionPS =
                transactionalConnection.prepareStatement("INSERT INTO db_version (version) VALUES (?)");
        dbVersionPS.setShort(1, dbVersion);
        preparedStatements.add(dbVersionPS);

        try {
            for (PreparedStatement preparedStatement : preparedStatements) {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
            transactionalConnection.commit();
        } catch (SQLException e) {
            transactionalConnection.rollback();
            throw e;
        }
    }

    private static short getVersion(Connection transactionalConnection) throws SQLException {
        PreparedStatement dbVersionPS =
                transactionalConnection.prepareStatement("SELECT EXISTS (SELECT relname FROM pg_class WHERE relname = ?)");
        dbVersionPS.setString(1, "db_version");
        ResultSet rs = dbVersionPS.executeQuery();
        rs.next();
        Boolean result = Boolean.mapRow(rs);

        boolean tableExists = result == null ? false : result.getResult();
        rs.close();
        dbVersionPS.close();
        if (!tableExists) {
            return -1;
        }

        dbVersionPS = transactionalConnection.prepareStatement("SELECT version FROM db_version LIMIT ?");
        dbVersionPS.setInt(1, 1);

        rs = dbVersionPS.executeQuery();
        rs.next();
        Version version = Version.mapRow(rs);
        rs.close();
        dbVersionPS.close();

        return version == null ? -1 : version.getVersion();
    }
}
