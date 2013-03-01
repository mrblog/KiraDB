package com.bdt.kiradb;

import java.io.File;
import java.io.IOException;

/**
 * A KiraDb instance that stores objects in the filesystem.
 */
public class FileSystemKiraDb extends KiraDb {
    /**
     * Construct a Core KiraDB instance with specified indexPath and filesystem storage path
     * @param indexPath the path to the index
     * @param storePath the path to the filesystem storage
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public FileSystemKiraDb(File indexPath, File storePath) throws KiraCorruptIndexException, IOException {
        super(indexPath);
        super.setBackingStore(new FileBackingStore(storePath));
    }

    /**
     * Construct a Core KiraDB instance with specified indexPath, with cache mode, and filesystem
     * storage path
     * @param indexPath the path to the index
     * @param disableCaching
     * @param storePath the path to the filesystem storage
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public FileSystemKiraDb(File indexPath, Boolean disableCaching, File storePath) throws KiraCorruptIndexException, IOException {
        super(indexPath, disableCaching);
        super.setBackingStore(new FileBackingStore(storePath));
    }

    /**
     * Construct a Core KiraDB instance with specified indexPath, with user-supplied caching store,
     * and filesystem storage path.
     * @param indexPath the path to the index
     * @param cacheStore The user-supplied caching BackingStore
     * @param storePath the path to the filesystem storage
     * @throws IOException 
     * @throws KiraCorruptIndexException 
     */
    public FileSystemKiraDb(File indexPath, BackingStore cacheStore, File storePath) throws KiraCorruptIndexException, IOException {
        super(indexPath, cacheStore);
        super.setBackingStore(new FileBackingStore(storePath));
    }
}
