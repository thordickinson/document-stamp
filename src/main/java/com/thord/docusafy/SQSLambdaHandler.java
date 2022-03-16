package com.thord.docusafy;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.MessageAttribute;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.itextpdf.text.DocumentException;
import com.thord.docusafy.processor.HashProcessor;
import com.thord.docusafy.processor.SignInfo;
import com.thord.docusafy.util.AWSClient;

public class SQSLambdaHandler implements RequestHandler<SQSEvent, Void> {

    private final HashProcessor processor = new HashProcessor(getSalt());
    private AWSClient aws = new AWSClient();

    public Void handleRequest(SQSEvent event, Context ctx) {
        event.getRecords().forEach(this::handleRecord);
        return null;
    }

    private String getSalt() {
        String salt = System.getenv("HASH_PROCESSOR_SALT");
        return salt == null ? "5a99bf2e-a5e1-4da4-b8fe-28e61c9b5c91" : salt;
    }

    private void handleRecord(SQSMessage message) {
        try {
            File source = downloadFile(message);
            File target = File.createTempFile("watermarked", ".pdf");
            processor.sign(source.getAbsolutePath(), target.getAbsolutePath(), getSignInfo(message));
            System.out.println("File is signed");
            upload(message, target);
        } catch (IOException | DocumentException ex) {
            throw new RuntimeException("Error singing file", ex);
        }
    }

    private SignInfo getSignInfo(SQSMessage message) {
        Map<String, MessageAttribute> attrs = message.getMessageAttributes();
        return new SignInfo(attrs.get("documentName").getStringValue(), attrs.get("subject").getStringValue(),
                LocalDate.now());
    }

    private void upload(SQSMessage message, File file) {
        Map<String, MessageAttribute> messageAttributes = message.getMessageAttributes();
        String bucket = messageAttributes.get("bucket").getStringValue();
        System.out.println("Uploading file");
        String body = new String(message.getBody());
        aws.uploadToS3(bucket, body + ".watermarked", file, "application/pdf");
    }

    private File downloadFile(SQSMessage message) throws IOException {
        Map<String, MessageAttribute> messageAttributes = message.getMessageAttributes();
        String bucket = messageAttributes.get("bucket").getStringValue();
        String body = new String(message.getBody());
        File downloaded = aws.downloadFile(bucket, body);
        return downloaded;
    }

}
