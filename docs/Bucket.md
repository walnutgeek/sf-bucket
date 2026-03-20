# Bucket 

Bucket{
    UUID4 getId();
    String load(String name);
    List<String> listNames();
    void write(String name, String content);
}


BucketDAO {
    /* persist in snowflake */
    Bucket create();
    Bucket open(UUID4 id);
}

