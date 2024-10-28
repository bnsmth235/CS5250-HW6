import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

public class RequestTests {
    @Test
    public void testWidgetRequestCreation() {
        WidgetRequest.OtherAttribute attr1 = new WidgetRequest.OtherAttribute();
        attr1.setName("width-unit");
        attr1.setValue("cm");

        WidgetRequest.OtherAttribute attr2 = new WidgetRequest.OtherAttribute();
        attr2.setName("length-unit");
        attr2.setValue("cm");

        WidgetRequest request = new WidgetRequest();
        request.setType("create");
        String requestID = UUID.randomUUID().toString();
        String widgetID = UUID.randomUUID().toString();
        request.setRequestId(requestID);
        request.setWidgetId(widgetID);
        request.setOwner("Mary Matthews");
        request.setLabel("JWJYY");
        request.setDescription("THBRNVNQPYAWNHGRGUKIOWCKXIVNDLWOIQTADHVEVMUAJWDONEPUEAXDITDSHJTDLCMHHSESFXSDZJCBLGIKKPUYAWKQAQI");
        request.setOtherAttributes(Arrays.asList(attr1, attr2));

        assertEquals("create", request.getType());
        assertEquals(requestID, request.getRequestId());
        assertEquals(widgetID, request.getWidgetId());
        assertEquals("Mary Matthews", request.getOwner());
        assertEquals("JWJYY", request.getLabel());
        assertEquals("THBRNVNQPYAWNHGRGUKIOWCKXIVNDLWOIQTADHVEVMUAJWDONEPUEAXDITDSHJTDLCMHHSESFXSDZJCBLGIKKPUYAWKQAQI", request.getDescription());
        assertEquals(2, request.getOtherAttributes().size());
    }

    @Test
    public void testCreateRequestExamples() throws InterruptedException {
        // Iterate through files in sample-requests directory
        String directory = "src/test/resources/sample-requests";
        Path path = Path.of(directory).toAbsolutePath().normalize();
        File[] files = new File(path.toString()).listFiles();
        for (File file : files) {
            testCreateRequestExample(file.getPath());
            Thread.sleep(100); // Wait 100ms before processing the next file
        }
    }

    private void testCreateRequestExample(String filePath) {
    File file = new File(filePath);
    try {
        String fileJson = new String(Files.readAllBytes(file.toPath()));
        if(fileJson.isBlank()) {
            // Skip empty files
            return;
        }
        WidgetRequest request = WidgetRequest.fromJson(fileJson);
        assertNotNull(request, "Request is null for file: " + file.getName());

        // Verify the request object
        assertNotNull(request.getRequestId(), "RequestId is null for request: " + request.toJson() + " in file: " + file.getName());
        assertNotNull(request.getWidgetId(), "WidgetId is null for request: " + request.toJson() + " in file: " + file.getName());
        assertNotNull(request.getOwner(), "Owner is null for request: " + request.toJson() + " in file: " + file.getName());
        assertNotNull(request.getLabel(), "Label is null for request: " + request.toJson() + " in file: " + file.getName());
        assertNotNull(request.getDescription(), "Description is null for request: " + request.toJson() + " in file: " + file.getName());
        assertNotNull(request.getOtherAttributes(), "OtherAttributes are null for request: " + request.toJson() + " in file: " + file.getName());
    } catch (IOException e) {
        fail("Failed to read file: " + file.getName());
    }
}
}