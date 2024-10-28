import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

public class S3Interactor {
    public String BUCKET_NAME;
    public S3Client s3;
    private String storageStrategy;
    public CreateRequestHandler createRequestHandler;
    public DeleteRequestHandler deleteRequestHandler;
    public UpdateRequestHandler updateRequestHandler;

    public S3Interactor(){

    }

    public S3Interactor(String BUCKET_NAME, String storageStrategy) {
        this.BUCKET_NAME = BUCKET_NAME;
        this.storageStrategy = storageStrategy;

        try {
            StaticCredentialsProvider credentialsProvider = AwsCredentialsLoader.loadCredentials(System.getProperty("user.home") + "/.aws/credentials");
            s3 = S3Client.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(credentialsProvider)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AWS credentials", e);
        }

        createRequestHandler = new CreateRequestHandler();
        deleteRequestHandler = new DeleteRequestHandler();
        updateRequestHandler = new UpdateRequestHandler();
    }

    public String getStorageStrategy() {
        return storageStrategy;
    }

    public void pollRequests() {
        while (true) {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .build();
            ListObjectsV2Response listResponse = s3.listObjectsV2(listRequest);

            listResponse.contents().stream().sorted()
                .findFirst().ifPresent(object -> processRequest(object.key()));

            try {
                Thread.sleep(100); // Wait 100ms before polling again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public PutObjectRequest generatePutRequest(String key) {
        return PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build();
    }

    private void processRequest(String key) {
        // Retrieve the request
        var request = s3.getObjectAsBytes(GetObjectRequest.builder().bucket(BUCKET_NAME).key(key).build()).asUtf8String();

        // Process the request
        processObjectRequest(request);

        // Delete the request
        s3.deleteObject(DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(key).build());
    }

    public void processObjectRequest(String jsonRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            WidgetRequest request = mapper.readValue(jsonRequest, WidgetRequest.class);
            switch (request.getType().toLowerCase()) {
                case "create":
                    createRequestHandler.processObjectRequest(request);
                    break;
                case "update":
                    updateRequestHandler.processObjectRequest(request);
                    break;
                case "delete":
                    deleteRequestHandler.processObjectRequest(request);
                    break;
                default:
                    System.out.println("Unknown request type: " + request.getType());
            }
        } catch (Exception e) {
            System.out.println("Failed to process request: " + e.getMessage());
        }
    }


}