import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class S3ApplicationInteractor {
    public String BUCKET2_NAME;
    public String BUCKET3_NAME = "usu-cs5250-drummerboy-web";

    public S3Client s3;
    private String storageStrategy;
    public CreateRequestHandler createRequestHandler;
    public DeleteRequestHandler deleteRequestHandler;
    public UpdateRequestHandler updateRequestHandler;
    public Logger logger;

    public S3ApplicationInteractor(){

    }

    public S3ApplicationInteractor(String BUCKET2_NAME, String storageStrategy, Logger logger) {
        this.BUCKET2_NAME = BUCKET2_NAME;
        this.storageStrategy = storageStrategy;
        this.logger = logger;

        s3 = S3Client.builder()
                .region(Region.US_EAST_1)
                .build();

        createRequestHandler = new CreateRequestHandler();
        deleteRequestHandler = new DeleteRequestHandler();
        updateRequestHandler = new UpdateRequestHandler();
    }

    public String getStorageStrategy() {
        return storageStrategy;
    }

    public void pollRequests() {
        long startTime = System.currentTimeMillis();
        while (true) {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(BUCKET2_NAME)
                    .build();
            ListObjectsV2Response listResponse = s3.listObjectsV2(listRequest);

            listResponse.contents().stream().min(Comparator.comparing(S3Object::key)).ifPresent(object -> processRequest(object.key()));

            if (listResponse.contents().isEmpty() && (System.currentTimeMillis() - startTime) > 10000) {
                logger.log(Level.INFO, "No items returned for 10 seconds, stopping the system.");
                break;
            }

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
                .bucket(BUCKET3_NAME)
                .key(key)
                .build();
    }

    private void processRequest(String key) {
        // Retrieve the request
        logger.log(Level.INFO, "Processing request: " + key);
        try{
            String request = s3.getObjectAsBytes(GetObjectRequest.builder().bucket(BUCKET2_NAME).key(key).build()).asUtf8String();
            // Process the request
            processObjectRequest(request);
            // Delete the request
            s3.deleteObject(DeleteObjectRequest.builder().bucket(BUCKET2_NAME).key(key).build());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process request: " + key, e);
        }
    }

    public void processObjectRequest(String jsonRequest) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            WidgetRequest request = mapper.readValue(jsonRequest, WidgetRequest.class);
            switch (request.getType().toLowerCase()) {
                case "create":
                    logger.log(Level.INFO, "Processing create request: " + request);
                    createRequestHandler.processObjectRequest(request);
                    break;
                case "update":
                    logger.log(Level.INFO, "Processing update request: " + request);
                    updateRequestHandler.processObjectRequest(request);
                    break;
                case "delete":
                    logger.log(Level.INFO, "Processing delete request: " + request);
                    deleteRequestHandler.processObjectRequest(request);
                    break;
                default:
                    logger.log(Level.WARNING,"Unknown request type: " + request.getType());
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to process request: " + jsonRequest, e);
        }
    }

}