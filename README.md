# sf-bucket

A Java library that provides a flat key-value store abstraction ("Bucket") persisted in Snowflake via JDBC.

## What It Does

Each bucket is a named container of string entries. Keys are string names (may contain slashes — no hierarchy is implied). Values are string content (CSV files, property files, etc.). No versioning — writes overwrite, writing null deletes.

## Usage

```java
// Create a DAO with your Snowflake DataSource
BucketDAO dao = new SnowflakeBucketDAO(dataSource);

// Create a new bucket
Bucket bucket = dao.create("username", "optional description");

// Write entries
bucket.write("config/app.properties", "key=value");
bucket.write("data/report.csv", "col1,col2\n1,2");

// Read entries
String content = bucket.load("config/app.properties"); // "key=value"
String missing = bucket.load("nonexistent");            // null

// List all entry names (sorted alphabetically)
List<String> names = bucket.listNames(); // ["config/app.properties", "data/report.csv"]

// Overwrite
bucket.write("config/app.properties", "new=content");

// Delete (write null)
bucket.write("config/app.properties", null);

// Open an existing bucket by ID
Bucket existing = dao.open(bucket.getId());
```

## Schema

Two Snowflake tables are required. Migration scripts are in `src/main/resources/db/migration/`:

- `V1__create_buckets.sql` — bucket metadata (id, created_by, description, created_at)
- `V2__create_bucket_entries.sql` — bucket entries (bucket_id, name, content)

## Build

```bash
mvn compile    # compile
mvn test       # run tests (uses H2 in-memory database)
mvn package    # build jar
```

Requires Java 17+ and Maven.

## Dependencies

| Dependency | Version | Scope |
|---|---|---|
| `net.snowflake:snowflake-jdbc` | 3.13.6 | provided |
| `com.h2database:h2` | 2.2.224 | test |
| `org.junit.jupiter:junit-jupiter` | 5.10.2 | test |

## Design Decisions

- **Connection-per-operation**: each method call gets its own JDBC connection from the DataSource and closes it when done
- **Portable upsert**: `write()` uses UPDATE-then-INSERT instead of MERGE for H2/Snowflake compatibility
- **Not thread-safe**: callers are responsible for serialization if needed
- **UUID as VARCHAR(36)**: Snowflake has no native UUID type

## Changelog

| Commit | Description |
|---|---|
| `6ff923e` | Implement `Bucket.listNames()` — returns entry names sorted alphabetically |
| `2145a13` | Add test cases for write overwrite and write-null-to-delete |
| `1eafb82` | Implement `Bucket.write()` and `load()` — UPDATE-then-INSERT upsert, null deletes |
| `62df391` | Implement `BucketDAO.open()` — SELECT + IllegalArgumentException if not found |
| `83cbc28` | Implement `BucketDAO.create()` — UUID generation, INSERT, null validation |
| `34fa48c` | Add H2TestHelper — fresh in-memory DB per test method |
| `c465f3e` | Add `Bucket`/`BucketDAO` interfaces and Snowflake/H2 DDL scripts |
| `6a8aa93` | Initialize Maven project scaffold |

## License

MIT
