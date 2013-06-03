package com.tooltwist.filemap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class AwsPluginOutputStream extends ByteArrayOutputStream {
	
	private String relativePath;
	private String bucketName;
	private AmazonS3 s3;
	

	@Override
	public void close() {
				
		if (!(s3 instanceof AmazonS3))
			throw new AmazonS3Exception("AmazonS3 object not initialized.");
		
		if (bucketName == null && bucketName.equals(""))
			throw new AmazonS3Exception("bucket not defined!"); 
			
		InputStream io = new ByteArrayInputStream(this.buf);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(this.size());
		metadata.setContentEncoding("utf-8");
		setContentType(relativePath, metadata);
		
		PutObjectRequest por = new PutObjectRequest(bucketName, relativePath, io, metadata);
		por.setCannedAcl(CannedAccessControlList.PublicRead);
		s3.putObject(por);
		
	}
	
	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public AmazonS3 getS3() {
		return s3;
	}

	public void setS3(AmazonS3 s3) {
		this.s3 = s3;
	}
	
	 private void setContentType(String key, ObjectMetadata metadata) {
	        String keyLC = key.toLowerCase().trim();
	        if (keyLC.endsWith(".css"))
	            metadata.setContentType("text/css");
	        else if (keyLC.endsWith(".doc") || keyLC.endsWith(".docx"))
	            metadata.setContentType("application/msword");
	        else if (keyLC.endsWith(".html") || keyLC.endsWith(".htm")  || keyLC.endsWith(".shtml") || keyLC.endsWith(".jsp") || keyLC.endsWith(".php"))
	            metadata.setContentType("text/html");
	        else if (keyLC.endsWith(".gif"))
	            metadata.setContentType("image/gif");
	        else if (keyLC.endsWith(".jpg"))
	            metadata.setContentType("image/jpeg");
	        else if (keyLC.endsWith(".js"))
	            metadata.setContentType("text/javascript");
	        else if (keyLC.endsWith(".pdf"))
	            metadata.setContentType("application/pdf");
	        else if (keyLC.endsWith(".png"))
	            metadata.setContentType("image/png");
	        else if (keyLC.endsWith(".txt"))
	            metadata.setContentType("text/plain");
	        else if (keyLC.endsWith(".zip"))
	            metadata.setContentType("application/zip");
	        else
	            metadata.setContentType("text/plain");
	    }
}
