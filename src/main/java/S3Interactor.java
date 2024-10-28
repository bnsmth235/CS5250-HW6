import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.HashMap;
import java.util.Map;

public class S3Interactor {
    private final String BUCKET_NAME;
    public final S3Client s3 = S3Client.builder().build();
    private final String storageStrategy;

    public S3Interactor(String BUCKET_NAME, String storageStrategy) {
        this.BUCKET_NAME = BUCKET_NAME;
        this.storageStrategy = storageStrategy;
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
        processCreateRequest(request);

        // Delete the request
        s3.deleteObject(DeleteObjectRequest.builder().bucket(BUCKET_NAME).key(key).build());
    }

    public void processCreateRequest(String jsonRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            WidgetRequest request = mapper.readValue(jsonRequest, WidgetRequest.class);
            switch (request.getType().toLowerCase()) {
                case "create":
                    if ("S3".equalsIgnoreCase(storageStrategy)) {
                        storeWidgetInS3(request);
                    } else if ("DynamoDB".equalsIgnoreCase(storageStrategy)) {
                        storeWidgetInDynamoDB(request);
                    }
                    break;
                case "update":
                    // Implement update logic here
                    break;
                case "delete":
                    // Implement delete logic here
                    break;
                default:
                    System.out.println("Unknown request type: " + request.getType());
            }
        } catch (Exception e) {
            System.out.println("Failed to process request: " + e.getMessage());
        }
    }

    private void storeWidgetInDynamoDB(WidgetRequest request) {
        DynamoDbClient dynamoDb = DynamoDbClient.builder().build();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("widget_id", AttributeValue.builder().s(request.getWidgetId()).build());
        item.put("owner", AttributeValue.builder().s(request.getOwner()).build());
        item.put("label", AttributeValue.builder().s(request.getLabel()).build());
        item.put("description", AttributeValue.builder().s(request.getDescription()).build());

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("widgets")
                .item(item)
                .build();

        dynamoDb.putItem(putItemRequest);
    }

    public void storeWidgetInS3(WidgetRequest request) throws JsonProcessingException {
        String key = String.format("widgets/%s/%s",
            request.getOwner().toLowerCase().replace(" ", "-"),
            request.getWidgetId());

        PutObjectRequest putRequest = generatePutRequest(key);

        s3.putObject(putRequest, RequestBody.fromString(request.toJson()));
    }
}