package org.granary.database.model.mapper;

import org.granary.database.model.Boolean;
import org.granary.database.model.Item;
import org.granary.database.model.Result;
import org.granary.database.model.Version;

import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RowsMapper {

    public static <T extends Result> T mapRow(ResultSet resultSet, Class<T> dbTypeClass) throws SQLException {
        if (Item.class.equals(dbTypeClass)) {
            return (T) Item.mapRow(resultSet);
        } else if (Version.class.equals(dbTypeClass)) {
            return (T) Version.mapRow(resultSet);
        } else if (Boolean.class.equals(dbTypeClass)) {
            return (T) Boolean.mapRow(resultSet);
        } else {
            throw new RuntimeException("Unsupported DBResultType for row mapping.");
        }
    }

    public static <T extends Result> List<T> mapRows(ResultSet resultSet, Class<T> dbTypeClass) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            if (Item.class.equals(dbTypeClass)) {
                result.add((T) Item.mapRow(resultSet));
            } else if (Version.class.equals(dbTypeClass)) {
                result.add((T) Version.mapRow(resultSet));
            } else if (Boolean.class.equals(dbTypeClass)) {
                result.add((T) Boolean.mapRow(resultSet));
            } else {
                throw new RuntimeException("Unsupported DBResultType for row mapping.");
            }
        }
        return result;
    }
}
