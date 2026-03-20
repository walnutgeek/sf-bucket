package io.github.walnutgeek.bucket;

import java.util.UUID;

public interface BucketDAO {
    Bucket create(String createdBy, String description);
    Bucket open(UUID id);
}
