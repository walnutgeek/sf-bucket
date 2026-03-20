CREATE TABLE bucket_entries (
    bucket_id VARCHAR(36) NOT NULL,
    name VARCHAR(4000) NOT NULL,
    content VARCHAR(16777216),
    PRIMARY KEY (bucket_id, name),
    FOREIGN KEY (bucket_id) REFERENCES buckets(id)
);
