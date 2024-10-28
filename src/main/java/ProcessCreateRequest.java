import com.fasterxml.jackson.databind.ObjectMapper;

public void ProcessCreateRequest(String jsonRequest) {
    ObjectMapper mapper = new ObjectMapper();
    try {
        WidgetRequest request = mapper.readValue(jsonRequest, WidgetRequest.class);

        if (storageStrategy.equals("S3")) {
            storeWidgetInS3(request);
        } else if (storageStrategy.equals("DynamoDB")) {
            storeWidgetInDynamoDB(request);
        }
    } catch (Exception e) {
        System.out.println("Failed to process request: " + e.getMessage());
    }
}
