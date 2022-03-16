package com.thord.docusafy.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public final class AWSClient {

    private AmazonS3 s3;

    public static String getS3Region() {
        String region = System.getenv("S3_REGION");
        return region != null ? region : "us-east-1";
    }

    private AmazonS3 getAmazonS3() {
        if (s3 == null) {
            s3 = AmazonS3ClientBuilder.standard().withRegion(getS3Region()).build();
        }
        return s3;
    }

    public File downloadFile(String bucketName, String objectKey) throws IOException {
        System.out.format("Downloading %s from S3 bucket %s...\n", objectKey, bucketName);
        final AmazonS3 s3 = getAmazonS3();
        try {
            S3Object o = s3.getObject(bucketName, objectKey);
            S3ObjectInputStream s3is = o.getObjectContent();
            File download = File.createTempFile(objectKey, "");
            FileOutputStream fos = new FileOutputStream(download);
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
            s3is.close();
            fos.close();
            return download;
        } catch (AmazonServiceException | FileNotFoundException e) {
            System.err.println(e.getMessage());
            throw new IOException("Error downloading file from S3", e);
        }
    }

    public void uploadToS3(String bucketName, String objectName, File file, String contentType) {
        AmazonS3 s3Client = getAmazonS3();
        // Upload a file as a new object with ContentType and title specified.
        PutObjectRequest request = new PutObjectRequest(bucketName, objectName, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        request.setMetadata(metadata);
        s3Client.putObject(request);
    }

}