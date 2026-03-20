# sf-bucket Design Spec

## Overview

A Java library that provides a flat key-value store abstraction ("Bucket") persisted in Snowflake. Keys are string names (may contain slashes but no hierarchy is implied), values are string content (CSV files, property files, etc.). No versioning — writes overwrite, writing null deletes.

## Project Setup

- **Group ID**: `io.github.walnutgeek`
- **Artifact ID**: `sf-bucket`
- **Version**: `0.1.0-SNAPSHOT`
- **Java**: 17
- **Build**: Maven (single module)
- **Package**: `io.github.walnutgeek.bucket`

## Dependencies

| Dependency | Version | Scope |
|---|---|---|
| `net.snowflake:snowflake-jdbc` | 3.13.6 | provided |
| `com.h2database:h2` | latest | test |
| `org.junit.jupiter:junit-jupiter` | latest | test |

## Schema

### `buckets` table

```sql
CREATE TABLE buckets (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    created_by VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    created_at TIMESTAMP_NTZ NOT NULL DEFAULT CURRENT_TIMESTAMP()
);
```

### `bucket_entries` table

```sql
CREATE TABLE bucket_entries (
    bucket_id VARCHAR(36) NOT NULL,
    name VARCHAR(4000) NOT NULL,
    content VARCHAR(16777216),
    PRIMARY KEY (bucket_id, name),
    FOREIGN KEY (bucket_id) REFERENCES buckets(id)
);
```

UUID stored as `VARCHAR(36)` since Snowflake has no native UUID type. Content uses Snowflake's max VARCHAR (16MB) for file-like content.

### Migration Scripts

Located at `src/main/resources/db/migration/`:
- `V1__create_buckets.sql`
- `V2__create_bucket_entries.sql`

Scripts are provided for manual or external application. No migration framework is used.

## Java Interfaces

### Bucket

```java
package io.github.walnutgeek.bucket;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface Bucket {
    UUID getId();
    String getCreatedBy();
    String getDescription();
    Instant getCreatedAt();

    String load(String name);
    List<String> listNames();
    void write(String name, String content); // null content = delete
}
```

### BucketDAO

```java
package io.github.walnutgeek.bucket;

import java.util.UUID;

public interface BucketDAO {
    Bucket create(String createdBy, String description);
    Bucket open(UUID id); // throws if not found
}
```

## Implementation

### Concurrency and Connection Model

- **Not thread-safe**: concurrent writes to the same bucket/key are not supported. Callers are responsible for serialization if needed.
- **Connection-per-operation**: each method call obtains a connection from the DataSource and closes it when done (try-with-resources). No connection reuse across calls.
- **No explicit transactions**: each operation is a single statement (or two for upsert). The upsert's UPDATE-then-INSERT may race under concurrent access, but this is acceptable given the single-threaded contract above.

### Input Validation

- `name` must not be null in `load()` and `write()` — throw `NullPointerException` if null.
- `createdBy` must not be null in `create()` — throw `NullPointerException` if null.
- Empty strings are allowed (they are valid values).
- No other Java-level validation; database constraints provide the backstop.

### SnowflakeBucketDAO

- Constructor takes `javax.sql.DataSource`
- `create(createdBy, description)`: generates UUID, sets `createdAt` to `Instant.now()`, INSERTs all four columns (id, created_by, description, created_at) explicitly into `buckets`, and uses the same `createdAt` value for the returned `SnowflakeBucket`. The DB default for `created_at` is not relied upon.
- `open(id)`: SELECTs from `buckets`, throws `IllegalArgumentException` if not found, returns `SnowflakeBucket`

### SnowflakeBucket

- Package-private constructor takes `DataSource` + bucket metadata (id, createdBy, description, createdAt). Only `SnowflakeBucketDAO` creates instances.
- `load(name)`: `SELECT content FROM bucket_entries WHERE bucket_id = ? AND name = ?` — returns `null` if no row
- `listNames()`: `SELECT name FROM bucket_entries WHERE bucket_id = ? ORDER BY name` — ordered alphabetically
- `write(name, content)`:
  - If content is null: `DELETE FROM bucket_entries WHERE bucket_id = ? AND name = ?`
  - If content is non-null: UPDATE first, check affected rows, INSERT if zero rows updated (portable upsert, no MERGE)

## Project Structure

```
src/main/java/io/github/walnutgeek/bucket/
    Bucket.java
    BucketDAO.java
    SnowflakeBucket.java
    SnowflakeBucketDAO.java

src/main/resources/db/migration/
    V1__create_buckets.sql
    V2__create_bucket_entries.sql

src/test/java/io/github/walnutgeek/bucket/
    SnowflakeBucketDAOTest.java
    SnowflakeBucketTest.java

src/test/resources/
    h2-init.sql
```

## Testing

### Strategy

H2 in-memory database integration tests. **Fresh H2 database per test method** using unique JDBC URLs (`jdbc:h2:mem:<unique>;DB_CLOSE_DELAY=-1`). This avoids cleanup ordering issues with the foreign key constraint.

The same SQL operations work against both H2 and Snowflake since we use portable SQL (no MERGE).

### H2 DDL Adjustments (`h2-init.sql`)

The H2 DDL mirrors the Snowflake schema with these adjustments:
- `TIMESTAMP_NTZ` → `TIMESTAMP`
- `DEFAULT CURRENT_TIMESTAMP()` → `DEFAULT CURRENT_TIMESTAMP` (H2 syntax)
- `VARCHAR(16777216)` → `CLOB` (H2 handles large text better as CLOB, though large VARCHAR also works)
- Foreign key constraint kept as-is (H2 supports it)

### Test Cases

1. `create()` — bucket exists with correct metadata (id, createdBy, description, createdAt)
2. `open()` with valid ID — returns bucket with correct metadata
3. `open()` with unknown ID — throws exception
4. `write()` + `load()` — content round-trips correctly
5. `write()` overwrite — new content replaces old
6. `write(name, null)` — deletes entry, subsequent `load()` returns null
7. `listNames()` — returns all written names
8. `listNames()` on empty bucket — returns empty list
9. `load()` for nonexistent name — returns null
