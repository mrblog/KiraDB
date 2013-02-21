package com.bdt.kiradb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import com.thoughtworks.xstream.XStream;

public class S3BackingStore extends BackingStore {

	
	private String bucket;

	private S3Service s3Service;
	private AccessControlList bucketAcl;
	private String S3error;

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
			setS3error(e.getMessage());
			s3Service = null;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setS3error(e.getMessage());
			s3Service = null;
		}
	}

	public void setS3error(String s3error) {
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
			throw new KiraException("S3ServiceException " + e.getMessage());
		}
        
	}
	
	public Object retrieveObject(XStream xstream, Record r, String value) throws KiraException, IOException, ClassNotFoundException {
		String key = makeKey(r, value);
		
		S3Object objectComplete;
		try {
			objectComplete = s3Service.getObject(bucket, key);
		} catch (S3ServiceException e) {
			throw new KiraException("S3ServiceException " + e.getMessage());
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
	void removeObject(XStream xstream, Record r, String value)
			throws KiraException, IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}
	

}
