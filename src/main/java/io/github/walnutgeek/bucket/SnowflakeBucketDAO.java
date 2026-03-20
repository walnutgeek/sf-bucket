package io.github.walnutgeek.bucket;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class SnowflakeBucketDAO implements BucketDAO {

    private final DataSource dataSource;

    public SnowflakeBucketDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Bucket create(String createdBy, String description) {
        Objects.requireNonNull(createdBy, "createdBy must not be null");
        UUID id = UUID.randomUUID();
        Instant createdAt = Instant.now();
        String sql = "INSERT INTO buckets (id, created_by, description, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, createdBy);
            ps.setString(3, description);
            ps.setTimestamp(4, Timestamp.from(createdAt));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create bucket", e);
        }
        return new SnowflakeBucket(dataSource, id, createdBy, description, createdAt);
    }

    @Override
    public Bucket open(UUID id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
