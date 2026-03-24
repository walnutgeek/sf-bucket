package io.github.walnutgeek.bucket;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

public class H2TestHelper {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    public static DataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test" + COUNTER.incrementAndGet() + ";DB_CLOSE_DELAY=-1");
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = loadResource("h2-init.sql");
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 schema", e);
        }
        return ds;
    }

    private static String loadResource(String name) {
        try (InputStream is = H2TestHelper.class.getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                throw new RuntimeException("Resource not found: " + name);
            }
            byte[] buf = new byte[4096];
            int len;
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            while ((len = is.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + name, e);
        }
    }
}
