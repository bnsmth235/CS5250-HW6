import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class S3Consumer {
    private static final String BUCKET_NAME = "bucket-2";
    private static final String REQUEST_PREFIX = "requests/";

    public void pollRequests() {
        S3Client s3 = S3Client.builder().build();

        while (true) {
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .prefix(REQUEST_PREFIX)
                    .build();
            ListObjectsV2Response listResponse = s3.listObjectsV2(listRequest);

            listResponse.contents().stream().sorted((o1, o2) -> o1.key().compareTo(o2.key()))
                .findFirst().ifPresent(object -> processRequest(s3, object.key()));

            try {
                Thread.sleep(100); // Wait 100ms before polling again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void processRequest(S3Client s3, String key) {
        // Retrieve, process, and delete the request
    }
}
