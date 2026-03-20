package io.github.walnutgeek.bucket;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

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
}
