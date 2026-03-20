package io.github.walnutgeek.bucket;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class H2TestHelperTest {

    @Test
    void createDataSource_createsTables() throws SQLException {
        DataSource ds = H2TestHelper.createDataSource();
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('BUCKETS', 'BUCKET_ENTRIES')")) {
            assertTrue(rs.next());
            assertEquals(2, rs.getInt(1));
        }
    }
}
