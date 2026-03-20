CREATE TABLE buckets (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bucket_entries (
    bucket_id VARCHAR(36) NOT NULL,
    name VARCHAR(4000) NOT NULL,
    content CLOB,
    PRIMARY KEY (bucket_id, name),
    FOREIGN KEY (bucket_id) REFERENCES buckets(id)
);
