import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class S3InteractorTests {

    @Test
    void constructorInitializesFieldsCorrectly() {
        String bucketName = "test-bucket";
        String storageStrategy = "S3";
        S3Interactor s3Interactor = new S3Interactor(bucketName, storageStrategy);

        assertEquals(bucketName, s3Interactor.BUCKET_NAME);
        assertEquals(storageStrategy, s3Interactor.getStorageStrategy());
    }

    @Test
    void generatePutRequestCreatesCorrectRequest() {
        String bucketName = "test-bucket";
        String key = "test-key";
        S3Interactor s3Interactor = new S3Interactor(bucketName, "S3");

        PutObjectRequest request = s3Interactor.generatePutRequest(key);

        assertEquals(bucketName, request.bucket());
        assertEquals(key, request.key());
    }

    @Test
    void processObjectRequestHandlesCreateRequest() {
        String bucketName = "test-bucket";
        String storageStrategy = "S3";
        S3Interactor s3Interactor = new S3Interactor(bucketName, storageStrategy);

        CreateRequestHandler mockCreateHandler = mock(CreateRequestHandler.class);
        s3Interactor.createRequestHandler = mockCreateHandler;

        String jsonRequest = "{\"type\":\"create\"}";
        s3Interactor.processObjectRequest(jsonRequest);

        verify(mockCreateHandler).processObjectRequest(any(WidgetRequest.class));
    }

    @Test
    void processObjectRequestHandlesUpdateRequest() {
        String bucketName = "test-bucket";
        String storageStrategy = "S3";
        S3Interactor s3Interactor = new S3Interactor(bucketName, storageStrategy);

        UpdateRequestHandler mockUpdateHandler = mock(UpdateRequestHandler.class);
        s3Interactor.updateRequestHandler = mockUpdateHandler;

        String jsonRequest = "{\"type\":\"update\"}";
        s3Interactor.processObjectRequest(jsonRequest);

        verify(mockUpdateHandler).processObjectRequest(any(WidgetRequest.class));
    }

    @Test
    void processObjectRequestHandlesDeleteRequest() {
        String bucketName = "test-bucket";
        String storageStrategy = "S3";
        S3Interactor s3Interactor = new S3Interactor(bucketName, storageStrategy);

        DeleteRequestHandler mockDeleteHandler = mock(DeleteRequestHandler.class);
        s3Interactor.deleteRequestHandler = mockDeleteHandler;

        String jsonRequest = "{\"type\":\"delete\"}";
        s3Interactor.processObjectRequest(jsonRequest);

        verify(mockDeleteHandler).processObjectRequest(any(WidgetRequest.class));
    }

    @Test
    void processObjectRequestHandlesUnknownRequestType() {
        String bucketName = "test-bucket";
        String storageStrategy = "S3";
        S3Interactor s3Interactor = new S3Interactor(bucketName, storageStrategy);

        String jsonRequest = "{\"type\":\"unknown\"}";
        s3Interactor.processObjectRequest(jsonRequest);

        // No exception should be thrown, and no handler should be called
    }

    @Test
    void pollRequestsProcessesRequests() {
        String bucketName = "test-bucket";
        String storageStrategy = "S3";
        S3Interactor s3Interactor = new S3Interactor(bucketName, storageStrategy);

        S3Client mockS3Client = mock(S3Client.class);
        s3Interactor.s3 = mockS3Client;

        ListObjectsV2Response mockResponse = mock(ListObjectsV2Response.class);
        when(mockS3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(mockResponse);
        when(mockResponse.contents()).thenReturn(List.of(S3Object.builder().key("test-key").build()));

        // Mock the getObjectAsBytes method to return a valid ResponseBytes object
        ResponseBytes<GetObjectResponse> mockResponseBytes = mock(ResponseBytes.class);
        when(mockResponseBytes.asUtf8String()).thenReturn("{\"type\":\"delete\"}");
        when(mockS3Client.getObjectAsBytes(any(GetObjectRequest.class))).thenReturn(mockResponseBytes);

        // Mock the deleteObject method to return a DeleteObjectResponse
        DeleteObjectResponse mockDeleteResponse = mock(DeleteObjectResponse.class);
        when(mockS3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mockDeleteResponse);

        Thread pollThread = new Thread(() -> s3Interactor.pollRequests());
        pollThread.start();

        try {
            Thread.sleep(200); // Allow some time for polling
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pollThread.interrupt();

        verify(mockS3Client, atLeastOnce()).listObjectsV2(any(ListObjectsV2Request.class));
        verify(mockS3Client, atLeastOnce()).getObjectAsBytes(any(GetObjectRequest.class));
    }
}