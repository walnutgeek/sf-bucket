package io.github.walnutgeek.bucket;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeBucketDAOTest {

    @Test
    void create_returnsBucketWithCorrectMetadata() {
        DataSource ds = H2TestHelper.createDataSource();
        BucketDAO dao = new SnowflakeBucketDAO(ds);

        Bucket bucket = dao.create("testuser", "my test bucket");

        assertNotNull(bucket.getId());
        assertEquals("testuser", bucket.getCreatedBy());
        assertEquals("my test bucket", bucket.getDescription());
        assertNotNull(bucket.getCreatedAt());
    }

    @Test
    void create_withNullCreatedBy_throwsNPE() {
        DataSource ds = H2TestHelper.createDataSource();
        BucketDAO dao = new SnowflakeBucketDAO(ds);

        assertThrows(NullPointerException.class, () -> dao.create(null, "desc"));
    }

    @Test
    void open_withValidId_returnsBucketWithCorrectMetadata() {
        DataSource ds = H2TestHelper.createDataSource();
        BucketDAO dao = new SnowflakeBucketDAO(ds);
        Bucket created = dao.create("testuser", "my bucket");

        Bucket opened = dao.open(created.getId());

        assertEquals(created.getId(), opened.getId());
        assertEquals("testuser", opened.getCreatedBy());
        assertEquals("my bucket", opened.getDescription());
        assertNotNull(opened.getCreatedAt());
    }

    @Test
    void open_withUnknownId_throwsException() {
        DataSource ds = H2TestHelper.createDataSource();
        BucketDAO dao = new SnowflakeBucketDAO(ds);

        assertThrows(IllegalArgumentException.class,
            () -> dao.open(UUID.randomUUID()));
    }
}
