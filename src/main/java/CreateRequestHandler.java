import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class CreateRequestHandler extends S3ApplicationInteractor {

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

        PutItemResponse response = dynamoDb.putItem(putItemRequest);
        if(response.sdkHttpResponse().isSuccessful()){
            super.logger.log(Level.INFO, "Widget %s created in DynamoDB", request.getWidgetId());
        }
        else{
            super.logger.log(Level.WARNING, "Widget %s not stored in DynamoDB", request.getWidgetId());
        }
    }

    public void storeWidgetInS3(WidgetRequest request){
        String key = String.format("widgets/%s/%s",
            request.getOwner().toLowerCase().replace(" ", "-"),
            request.getWidgetId());

        PutObjectRequest putRequest = generatePutRequest(key);

        PutObjectResponse response = s3.putObject(putRequest, RequestBody.fromString(request.toJson()));
        if(response.sdkHttpResponse().isSuccessful()){
            super.logger.log(Level.INFO, "Widget %s created in S3", request.getWidgetId());
        }
        else{
            super.logger.log(Level.WARNING, "Widget %s not stored in S3", request.getWidgetId());
        }
    }
}
