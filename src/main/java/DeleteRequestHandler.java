import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.util.HashMap;
import java.util.Map;

public class DeleteRequestHandler extends S3Interactor {
    public DeleteRequestHandler() {
    }

    public void processObjectRequest(WidgetRequest request) {
        if ("S3".equalsIgnoreCase(super.getStorageStrategy())) {
            deleteWidgetFromS3(request);
        } else if ("DynamoDB".equalsIgnoreCase(super.getStorageStrategy())) {
            deleteWidgetFromDynamoDB(request);
        }
    }

    private void deleteWidgetFromS3(WidgetRequest request) {
        String key = String.format("widgets/%s/%s",
            request.getOwner().toLowerCase().replace(" ", "-"),
            request.getWidgetId());

        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(super.BUCKET_NAME)
                .key(key)
                .build();

        super.s3.deleteObject(deleteRequest);
    }

    private void deleteWidgetFromDynamoDB(WidgetRequest request) {
        DynamoDbClient dynamoDb = DynamoDbClient.builder().build();

        Map<String, AttributeValue> key = new HashMap<>();
        key.put("widget_id", AttributeValue.builder().s(request.getWidgetId()).build());
        key.put("owner", AttributeValue.builder().s(request.getOwner()).build());

        DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                .tableName("widgets")
                .key(key)
                .build();

        dynamoDb.deleteItem(deleteRequest);
    }
}