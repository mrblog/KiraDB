package com.bdt.kiradb;

import java.io.File;
import java.io.IOException;

/**
 * Construct an Amazon S3-backed instance of KiraDb.  The constructors require
 * the usual Amazon AWS S3 key, secret, and bucket name.
 * @author David Beckemeyer and Mark Petrovic
 */
public class S3KiraDB extends KiraDb {

    /**
     * Construct an S3 KiraDB instance with specified indexPath.  An /s3.properties file is assumed to be on the classpath
     * with keys aws.key, aws.secret, and aws.bucket and approriate values.
     *
     * @param indexPath      The index path
     * @param disableCaching cache enabled
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public S3KiraDB(File indexPath, Boolean disableCaching) throws KiraCorruptIndexException, IOException {
        super(indexPath, disableCaching);
        super.setBackingStore(new S3BackingStore());
    }

    /**
     * Construct an S3 KiraDB instance with specified indexPath, AWS key, secret, and bucket name.
     *
     * @param indexPath The index path
     * @param awsKey    AWS S3 key
     * @param awsSecret AWS S3 secret
     * @param awsBucket AWS S3 bucketname
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public S3KiraDB(File indexPath, String awsKey, String awsSecret, String awsBucket) throws KiraCorruptIndexException, IOException {
        super(indexPath);
        super.setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
    }

    /**
     * Construct an S3 KiraDB instance with specified indexPath, AWS key, secret, and bucket name.  Allow disabling
     * of backing ehcache.
     *
     * @param indexPath      The index path
     * @param disableCaching cache enabled
     * @param awsKey         AWS S3 key
     * @param awsSecret      AWS S3 secret
     * @param awsBucket      AWS S3 bucketname
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public S3KiraDB(File indexPath, Boolean disableCaching, String awsKey, String awsSecret, String awsBucket) throws KiraCorruptIndexException, IOException {
        super(indexPath, disableCaching);
        super.setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
    }

    /**
     * Construct an S3 KiraDB instance with specified indexPath, AWS key, secret, and bucket name.  Allows for user
     * supplied caching in the form of a custom BackingStore instance.
     *
     * @param indexPath  The index path
     * @param cacheStore cacheStore A user supplied BackingStore instance.f
     * @param awsKey     AWS S3 key
     * @param awsSecret  AWS S3 secret
     * @param awsBucket  AWS S3 bucketname
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public S3KiraDB(File indexPath, BackingStore cacheStore, String awsKey, String awsSecret, String awsBucket) throws KiraCorruptIndexException, IOException {
        super(indexPath, cacheStore);
        super.setBackingStore(new S3BackingStore(awsKey, awsSecret, awsBucket));
    }

    /**
     * A no-op method that does nothing.  The backing store will have already been set by virtue of using this class to begin with.
     *
     * @param backingStore
     * @return KiraDb KiraDB instance
     */
    public KiraDb setBackingStore(BackingStore backingStore) {
        return this;
    }
}
