import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;

public class CreateRequestHandler extends S3Interactor{

    public CreateRequestHandler() {

    }

    public void processObjectRequest(WidgetRequest request) {
        if ("S3".equalsIgnoreCase(super.getStorageStrategy())) {
            storeWidgetInS3(request);
        } else if ("DynamoDB".equalsIgnoreCase(super.getStorageStrategy())) {
            storeWidgetInDynamoDB(request);
        }
    }

    private void storeWidgetInDynamoDB(WidgetRequest request) {
        DynamoDbClient dynamoDb = DynamoDbClient.builder().build();

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("widget_id", AttributeValue.builder().s(request.getWidgetId()).build());
        item.put("owner", AttributeValue.builder().s(request.getOwner()).build());
        item.put("label", AttributeValue.builder().s(request.getLabel()).build());
        item.put("description", AttributeValue.builder().s(request.getDescription()).build());

        // Add otherAttributes to the item map
        if (request.getOtherAttributes() != null) {
            for (WidgetRequest.OtherAttribute attr : request.getOtherAttributes()) {
                item.put(attr.getName(), AttributeValue.builder().s(attr.getValue()).build());
            }
        }

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("widgets")
                .item(item)
                .build();

        dynamoDb.putItem(putItemRequest);
    }

    public void storeWidgetInS3(WidgetRequest request){
        String key = String.format("widgets/%s/%s",
            request.getOwner().toLowerCase().replace(" ", "-"),
            request.getWidgetId());

        PutObjectRequest putRequest = generatePutRequest(key);

        s3.putObject(putRequest, RequestBody.fromString(request.toJson()));
    }
}
