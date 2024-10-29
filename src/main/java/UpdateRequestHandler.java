import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class UpdateRequestHandler extends S3ApplicationInteractor {

    public UpdateRequestHandler() {
    }

    public void processObjectRequest(WidgetRequest request){
        if ("S3".equalsIgnoreCase(super.getStorageStrategy())) {
            updateWidgetInS3(request);
        } else if ("DynamoDB".equalsIgnoreCase(super.getStorageStrategy())) {
            updateWidgetInDynamoDB(request);
        }
    }

    private void updateWidgetInDynamoDB(WidgetRequest request) {
        DynamoDbClient dynamoDb = DynamoDbClient.builder().build();

        // Retrieve the existing widget
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("widget_id", AttributeValue.builder().s(request.getWidgetId()).build());
        key.put("owner", AttributeValue.builder().s(request.getOwner()).build());

        // Update the widget attributes
        Map<String, AttributeValue> updatedAttributes = new HashMap<>();
        if (request.getLabel() != null) {
            updatedAttributes.put("label", AttributeValue.builder().s(request.getLabel()).build());
        }
        if (request.getDescription() != null) {
            updatedAttributes.put("description", AttributeValue.builder().s(request.getDescription()).build());
        }
        if (request.getOtherAttributes() != null) {
            for (WidgetRequest.OtherAttribute attr : request.getOtherAttributes()) {
                updatedAttributes.put(attr.getName(), AttributeValue.builder().s(attr.getValue()).build());
            }
        }

        // Store the updated widget
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("widgets")
                .item(updatedAttributes)
                .build();

        PutItemResponse response = dynamoDb.putItem(putItemRequest);
        if(response.sdkHttpResponse().isSuccessful()){
            super.logger.log(Level.INFO, String.format("Widget %s updated in DynamoDB", request.getWidgetId()));
        }
        else{
            super.logger.log(Level.WARNING, String.format("Widget %s not updated in DynamoDB", request.getWidgetId()));
        }
    }

    private void updateWidgetInS3(WidgetRequest request) {
        String key = String.format("widgets/%s/%s",
            request.getOwner().toLowerCase().replace(" ", "-"),
            request.getWidgetId());

        // Retrieve the existing widget
        String existingWidgetJson = s3.getObjectAsBytes(GetObjectRequest.builder().bucket(super.BUCKET2_NAME).key(key).build()).asUtf8String();
        ObjectMapper mapper = new ObjectMapper();
        WidgetRequest existingWidget = null;
        try {
            existingWidget = mapper.readValue(existingWidgetJson, WidgetRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Update the widget attributes
        if (request.getLabel() != null) {
            existingWidget.setLabel(request.getLabel());
        }
        if (request.getDescription() != null) {
            existingWidget.setDescription(request.getDescription());
        }
        if (request.getOtherAttributes() != null) {
            existingWidget.setOtherAttributes(request.getOtherAttributes());
        }

        // Store the updated widget
        PutObjectRequest putRequest = generatePutRequest(key);
        PutObjectResponse response = s3.putObject(putRequest, RequestBody.fromString(existingWidget.toJson()));
        if(response.sdkHttpResponse().isSuccessful()){
            super.logger.log(Level.INFO, "Widget %s updated in S3", request.getWidgetId());
        }
        else{
            super.logger.log(Level.WARNING, "Widget %s not updated in S3", request.getWidgetId());
        }
    }
}
