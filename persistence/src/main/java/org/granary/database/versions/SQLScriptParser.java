package org.granary.database.versions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLScriptParser {

    private static final Logger LOG = LoggerFactory.getLogger(SQLScriptParser.class);
    private static final Pattern SQL_COMMENT_PATTERN = Pattern.compile("–.*|/\\*(.|[\\r\\n])*?\\*/");

    public static List<String> parse(String resource) throws IOException {
        InputStream stream = SQLScriptParser.class.getResourceAsStream(resource);
        if (stream == null) {
            throw new RuntimeException(String.format("Cannot find resource: %s.", resource));
        }

        List<String> sqlStatements = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder currentStatement = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher commentMatcher = SQL_COMMENT_PATTERN.matcher(line);
                line = commentMatcher.replaceAll("");

                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                currentStatement.append(line).append(" ");

                if (line.endsWith(";")) {
                    sqlStatements.add(currentStatement.toString());
                    LOG.info(currentStatement.toString());
                    currentStatement.setLength(0);
                }
            }
        }

        return sqlStatements;
    }
}