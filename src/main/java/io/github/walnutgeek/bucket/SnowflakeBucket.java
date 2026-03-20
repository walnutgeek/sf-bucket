package io.github.walnutgeek.bucket;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

class SnowflakeBucket implements Bucket {

    private final DataSource dataSource;
    private final UUID id;
    private final String createdBy;
    private final String description;
    private final Instant createdAt;

    SnowflakeBucket(DataSource dataSource, UUID id, String createdBy, String description, Instant createdAt) {
        this.dataSource = dataSource;
        this.id = id;
        this.createdBy = createdBy;
        this.description = description;
        this.createdAt = createdAt;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public String load(String name) {
        Objects.requireNonNull(name, "name must not be null");
        String sql = "SELECT content FROM bucket_entries WHERE bucket_id = ? AND name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("content");
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load entry: " + name, e);
        }
    }

    @Override
    public List<String> listNames() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void write(String name, String content) {
        Objects.requireNonNull(name, "name must not be null");
        if (content == null) {
            delete(name);
            return;
        }
        String updateSql = "UPDATE bucket_entries SET content = ? WHERE bucket_id = ? AND name = ?";
        String insertSql = "INSERT INTO bucket_entries (bucket_id, name, content) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, content);
                ps.setString(2, id.toString());
                ps.setString(3, name);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    return;
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, id.toString());
                ps.setString(2, name);
                ps.setString(3, content);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write entry: " + name, e);
        }
    }

    private void delete(String name) {
        String sql = "DELETE FROM bucket_entries WHERE bucket_id = ? AND name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.setString(2, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entry: " + name, e);
        }
    }
}
