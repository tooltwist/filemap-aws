package com.tooltwist.filemap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.tooltwist.xdata.XDException;
import com.tooltwist.xdata.XSelector;

public class AmazonS3Adaptor implements IFileGroupAdaptor {
	
	private String baseDir = "/tmp";
	private AmazonS3Client s3;
	private String bucketName;

	@Override
	public void init(XSelector config) throws FilemapException {
		
		//check the configuration specified
		String accessKey = "";
		String secretKey = "";
		String region = "";
		
		try {
			bucketName = config.getString("bucketName");
			accessKey = config.getString("accessKey");
			secretKey = config.getString("secretKey");
			region = config.getString("region");
			
		} catch (XDException e) {
			throw new FilemapException("Error parsing config.");
		}
		
		//set s3 credentials 
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		s3 = new AmazonS3Client(awsCredentials);
		
		//initialize region
		try {
			
			if (region == null || "".equals(region))
				region = Regions.US_WEST_2.getName();
			
			Region awsRegion = null;
			for (Regions regions :Regions.values()) {
				if (regions.getName().equals(region)) {
					awsRegion = Region.getRegion(regions);
					break ;
				}
					
			}
			s3.setRegion(awsRegion);
			
		} catch (Exception e) {
			throw new FilemapException("Error setting region.");
		}

	}
	
	@Override
	public boolean exists(String relativePath) throws FileNotFoundException {
		
		boolean exists = false;
		
		ObjectListing listObjects = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(relativePath));
        List<S3ObjectSummary> commonPrefixes = listObjects.getObjectSummaries();
        if (commonPrefixes.size() > 0)
        	exists = true;
		
		return exists;
	}

	@Override
	public boolean isDirectory(String relativePath) throws FileNotFoundException {
		
		boolean isDirectory = true;
	
		return isDirectory;
	}

	@Override
	public boolean isFile(String relativePath) throws FileNotFoundException {
		
		boolean isFile = false;
		
        ObjectListing listObjects = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(relativePath));
        List<S3ObjectSummary> commonPrefixes = listObjects.getObjectSummaries();
        for (S3ObjectSummary s3Obect : commonPrefixes) {
        	String key = s3Obect.getKey();
        	
        	if (relativePath.equals(key)) {
        		isFile = true;
        		break;
        	}
        	
        }

        return isFile;
	}

	@Override
	public boolean mkdir(String relativePath) throws FileNotFoundException {
		
		return true;
		 
	}

	@Override
	public boolean mkdirs(String relativePath) throws FileNotFoundException {
		
		return mkdir(relativePath);
		
	}

	@Override
	public AwsPluginOutputStream openForWriting(String relativePath, boolean append) throws IOException {
			
		createBucketIfNotExist(bucketName);
		
		
		AwsPluginOutputStream outputStream = new AwsPluginOutputStream();
		outputStream.setRelativePath(relativePath);
		outputStream.setS3(s3);
		outputStream.setBucketName(bucketName);
		
		return outputStream;
		
	}

	@Override
	public InputStream openForReading(String relativePath) throws IOException {
		
		createBucketIfNotExist(bucketName);
        
        while (relativePath.startsWith("/"))
        	relativePath = relativePath.substring(1);
        relativePath = relativePath.replace("//", "/");
        S3Object object = s3.getObject(new GetObjectRequest(bucketName, relativePath));
        
        return object.getObjectContent();

	}
	
	@Override
	public Iterable<String> files(String directoryRelativePath) {
		
        LinkedList<String> result = new LinkedList<String>();
        
        ObjectListing listObjects = s3.listObjects(new ListObjectsRequest().withBucketName(bucketName).withPrefix(directoryRelativePath));
        List<S3ObjectSummary> commonPrefixes = listObjects.getObjectSummaries();
        for (S3ObjectSummary s3Obect : commonPrefixes) {
        	String key = s3Obect.getKey();
        	
        	if (directoryRelativePath.endsWith("/"))
        	key = key.substring(directoryRelativePath.length());
        	
        	//remove leading slashes
        	if (key.startsWith("/"))
        		key = key.substring(1);	
        	
        	//include only if obect is not subfolder.
        	if (!key.equals("") && !key.endsWith("/")) {
        		if (!directoryRelativePath.endsWith("/"))
        			directoryRelativePath += "/";
        		result.add(directoryRelativePath + key);
        	}
        		
        	
        }

        return result;
    }
	
	
	 /** Delete an object - if they key has any leading slashes, they are removed.
     */
	public boolean delete(String relativePath) {
		
		if (relativePath.startsWith("/"))
			relativePath = relativePath.substring(1);
		
		s3.deleteObject(bucketName, relativePath);
		
		return true;
	}

	@Override
	public String fileDescription(String relativePath) {
		return s3.getResourceUrl(bucketName, relativePath);
	}
	
	private String fullPath(String relativePath) {
		if (relativePath.startsWith("/"))
			return baseDir + relativePath;
		return baseDir + "/" + relativePath;
	}
	
	/**
	 * Check bucket if already exist, if not create bucket
	 */
	private void createBucketIfNotExist(String bucketName) {
		
		boolean isBucketExist = false;
		List<Bucket> listBuckets = s3.listBuckets();
		for (Bucket bucket :listBuckets) {
			if (bucket.getName().equals(bucketName)) {
				isBucketExist = true;
				break;
			}
		}
		
		
		if (!isBucketExist)
			s3.createBucket(bucketName);
		
	}
	
}
