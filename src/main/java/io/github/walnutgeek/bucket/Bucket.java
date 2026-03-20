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
    void write(String name, String content);
}
