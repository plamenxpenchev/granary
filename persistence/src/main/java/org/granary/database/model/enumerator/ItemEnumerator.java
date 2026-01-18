package org.granary.database.model.enumerator;

import org.granary.database.model.Item;
import org.granary.database.model.mapper.RowsMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemEnumerator implements ResultEnumerator<Item> {

    private final ResultSet rs;
    private final PreparedStatement ps;
    private final Connection connection;

    boolean hasMoreCalled = false;
    boolean nextCalled = false;

    boolean hasMore = false;
    boolean enumeratorExhausted = false;

    public ItemEnumerator(ResultSet rs, PreparedStatement ps, Connection connection) {
        this.rs = rs;
        this.ps = ps;
        this.connection = connection;
    }

    @Override
    public synchronized boolean hasMore() throws SQLException {
        if (hasMoreCalled) {
            return hasMore;
        }
        hasMoreCalled = true;
        nextCalled = false;

        hasMore = rs.next();
        if (!hasMore) {
            enumeratorExhausted = true;
            rs.close();
            ps.close();
            connection.close();
        }

        return hasMore;
    }

    @Override
    public synchronized Item next() throws SQLException {
        if (!hasMoreCalled) {
            hasMore();
        }
        hasMoreCalled = false;
        nextCalled = true;

        if (enumeratorExhausted) {
            return null;
        }

        return RowsMapper.mapRow(rs, Item.class);
    }
}
