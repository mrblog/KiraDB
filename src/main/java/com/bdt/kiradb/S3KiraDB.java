package com.bdt.kiradb;

public class S3KiraDB extends Core {

    public S3KiraDB(String indexPath, String awskey, String awsSecret, String awsBucket) {
        super(indexPath);
        setBackingStore(new S3BackingStore(awskey, awsSecret, awsBucket));
    }

    public S3KiraDB(String indexPath, Boolean disableCaching, String awsKey, String awsSecret, String awsBucket) {
        super(indexPath, disableCaching);
        setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
    }

    public S3KiraDB(String indexPath, BackingStore cacheStore, String awsKey, String awsSecret, String awsBucket) {
        super(indexPath, cacheStore);
        setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
    }
}
