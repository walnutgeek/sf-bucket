package io.github.walnutgeek.bucket;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeBucketTest {

    private Bucket createTestBucket() {
        DataSource ds = H2TestHelper.createDataSource();
        BucketDAO dao = new SnowflakeBucketDAO(ds);
        return dao.create("testuser", "test bucket");
    }

    @Test
    void write_and_load_roundTrip() {
        Bucket bucket = createTestBucket();

        bucket.write("config/app.properties", "key=value");
        String content = bucket.load("config/app.properties");

        assertEquals("key=value", content);
    }

    @Test
    void load_nonexistentName_returnsNull() {
        Bucket bucket = createTestBucket();

        assertNull(bucket.load("does/not/exist.txt"));
    }

    @Test
    void write_overwrite_replacesContent() {
        Bucket bucket = createTestBucket();

        bucket.write("file.txt", "original");
        bucket.write("file.txt", "updated");

        assertEquals("updated", bucket.load("file.txt"));
    }

    @Test
    void write_nullContent_deletesEntry() {
        Bucket bucket = createTestBucket();

        bucket.write("file.txt", "content");
        bucket.write("file.txt", null);

        assertNull(bucket.load("file.txt"));
    }

    @Test
    void listNames_returnsAllNames_sorted() {
        Bucket bucket = createTestBucket();

        bucket.write("zebra.txt", "z");
        bucket.write("alpha.txt", "a");
        bucket.write("middle/path.csv", "m");

        List<String> names = bucket.listNames();

        assertEquals(List.of("alpha.txt", "middle/path.csv", "zebra.txt"), names);
    }

    @Test
    void listNames_emptyBucket_returnsEmptyList() {
        Bucket bucket = createTestBucket();

        List<String> names = bucket.listNames();

        assertTrue(names.isEmpty());
    }
}
