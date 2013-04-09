package com.tooltwist.filemap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.tooltwist.xdata.XSelector;

public class AmazonS3Adaptor implements IFileGroupAdaptor {

	private AmazonS3 s3;
	private String bucketName;

	@Override
	public void init(XSelector config) throws FilemapException {
		
		bucketName = "tr-s3-phil-test";
		String accessKey = "AKIAISNJYSLQCC635S3Q";
		String secretKey = "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";
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		s3 = new AmazonS3Client(awsCredentials);
		
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);

		
		
	}

	@Override
	public OutputStream openForWriting(String relativePath, boolean append) throws IOException {
		return null;
	}

	@Override
	public InputStream openForReading(String relativePath) throws IOException {
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, relativePath));
        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
        S3ObjectInputStream objectContent = object.getObjectContent();
//		displayTextInputStream(objectContent);

		// TODO Auto-generated method stub
		return objectContent;
	}

	@Override
	public boolean exists(String relativePath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDirectory(String relativePath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFile(String relativePath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mkdir(String relativePath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mkdirs(String relativePath) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String fileDescription(String relativePath) {
		return "(Amazon S3: " + relativePath + ")"; // ZZZ Should give details of S3 buckeet, etc.
	}

}
