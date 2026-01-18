package org.granary.database.versions;

import org.granary.database.DatabaseTest;
import org.granary.database.model.Item;

import org.granary.database.model.constants.RootItem;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VersionsUpdaterTest extends DatabaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(VersionsUpdaterTest.class);

    @Test
    public void versionsUpdateShouldCreateRootItem() throws SQLException, IOException {
        Connection trConn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            trConn = connectionPool.getTransactionalConnection();
            VersionUpdater.update(trConn);
            ps = trConn.prepareStatement(
                "SELECT " +
                    "i.id, " +
                    "i.parent_id, " +
                    "i.hierarchy_level, " +
                    "i.is_leaf, " +
                    "i.name, " +
                    "pt.price, " +
                    "pt.discount_percentage " +
                "FROM items i " +
                "INNER JOIN price_tags pt " +
                "ON i.id = pt.item_id"
            );
            rs = ps.executeQuery();
            rs.next();

            Item rootItem = Item.mapRow(rs);
            assertNotNull(rootItem);
            assertEquals(RootItem.ID, rootItem.getId());
            assertEquals(RootItem.PARENT_ID, rootItem.getParentId());
            assertEquals(RootItem.HIERARCHY_LEVEL, rootItem.getHierarchyLevel());
            assertEquals(RootItem.IS_LEAF, rootItem.getIsLeaf());
            assertEquals(RootItem.NAME, rootItem.getName());
            assertEquals(RootItem.PRICE, rootItem.getPrice());
            assertEquals(RootItem.DISCOUNT_PERCENTAGE, rootItem.getDiscountPercentage());

            LOG.info("Versions updated, root item found.");
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

