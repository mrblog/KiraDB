package com.bdt.kiradb;

import com.thoughtworks.xstream.XStream;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class S3BackingStore extends BackingStore {

    private String bucket;

    private S3Service s3Service;
    private AccessControlList bucketAcl;
    private String S3error;

    public S3BackingStore() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/s3.properties");
        if (resourceAsStream == null) {
            throw new RuntimeException(new KiraException("When constructing this S3BackingStore, you must have an /s3.properties on the classpath"));
        }
        Properties properties = new Properties();
        try {
            properties.load(resourceAsStream);
            String awsAccessKey = properties.getProperty("aws.key", "").trim();
            String awsSecretKey = properties.getProperty("aws.secret", "").trim();
            bucket = properties.getProperty("aws.bucket", "").trim();
            if (awsAccessKey.isEmpty() || awsSecretKey.isEmpty() || bucket.isEmpty()) {
                throw new RuntimeException("One or more of AWS System properties aws.[key|secret|bucket] are not set in the classpath /s3.properties.");
            }
            AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);
            s3Service = new RestS3Service(awsCredentials);
            bucketAcl = s3Service.getBucketAcl(bucket);
        } catch (S3ServiceException e) {
            setS3error(e.getErrorMessage());
            s3Service = null;
            throw new RuntimeException(e);
        } catch (ServiceException e) {
            setS3error(e.getMessage());
            s3Service = null;
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public S3BackingStore(String awsAccessKey, String awsSecretKey,
                          String bucket) {

        this.bucket = bucket;

        AWSCredentials awsCredentials = new AWSCredentials(awsAccessKey,
                awsSecretKey);
        try {
            s3Service = new RestS3Service(awsCredentials);
            bucketAcl = s3Service.getBucketAcl(bucket);
        } catch (S3ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            setS3error(e.getErrorMessage());
            s3Service = null;
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            setS3error(e.getMessage());
            s3Service = null;
        }
    }

    private void setS3error(String s3error) {
        this.S3error = s3error;
    }

    public String getS3error() {
        return S3error;
    }

    public void storeObject(XStream xstream, Record r) throws IOException, KiraException {
        String key = makeKey(r);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;

        oos = xstream.createObjectOutputStream(bos);

        oos.writeObject(r);
        // Flush and close the ObjectOutputStream.
        //
        oos.flush();
        oos.close();
        S3Object recordObject;
        try {
            recordObject = new S3Object(key, bos.toString());
        } catch (NoSuchAlgorithmException e1) {
            throw new KiraException("NoSuchAlgorithm " + e1.getMessage());
        }
        recordObject.setContentType("text/xml");
        recordObject.setAcl(bucketAcl);
        try {
            s3Service.putObject(bucket, recordObject);
        } catch (S3ServiceException e) {
            setS3error(e.getErrorMessage());
            throw new KiraException("S3ServiceException " + e.getErrorMessage());
        }

    }

    public Object retrieveObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException {
        String key = makeKey(r, value);

        S3Object objectComplete;
        try {
            objectComplete = s3Service.getObject(bucket, key);
        } catch (S3ServiceException e) {
            setS3error(e.getErrorMessage());
            throw new KiraException("S3ServiceException " + e.getErrorMessage());
        }

        ObjectInputStream ois;
        try {
            ois = xstream.createObjectInputStream(objectComplete.getDataInputStream());
        } catch (ServiceException e) {
            throw new KiraException("ServiceException " + e.getMessage());
        }
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    @Override
    public void removeObject(XStream xstream, Record r, String value)
            throws KiraException, IOException, ClassNotFoundException {
        String key = makeKey(r, value);
        try {
            s3Service.deleteObject(bucket, key);
        } catch (ServiceException e) {
            setS3error(e.getErrorMessage());
            throw new KiraException("S3ServiceException " + e.getErrorMessage());
        }
    }


}
