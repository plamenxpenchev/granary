package org.granary.database.model.enumerator;

import org.granary.database.model.Result;

import java.sql.SQLException;

public interface ResultEnumerator<T extends Result> {
    public boolean hasMore() throws SQLException;
    public T next() throws SQLException;
}
