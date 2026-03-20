package io.github.walnutgeek.bucket;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
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
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List<String> listNames() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void write(String name, String content) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
