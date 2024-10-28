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
    public void testCreateRequestExamples() {
        // Iterate through files in sample-requests directory
        String directory = "src/test/resources/sample-requests";
        Path path = Path.of(directory).toAbsolutePath().normalize();
        File[] files = new File(path.toString()).listFiles();
        for (File file : files) {
            testCreateRequestExample(file.getPath());

        }
    }

    private void testCreateRequestExample(String filePath) {
        File file = new File(filePath);
        try {
            String fileJson = new String(Files.readAllBytes(file.toPath()));
            WidgetRequest request = WidgetRequest.fromJson(fileJson);
            assertNotNull(request);

            // Verify the request object
            assertTrue(Arrays.asList("create", "delete", "update").contains(request.getType()));
            assertNotNull(request.getRequestId());
            assertNotNull(request.getWidgetId());
            assertNotNull(request.getOwner());
            assertNotNull(request.getLabel());
            assertNotNull(request.getDescription());
            assertNotNull(request.getOtherAttributes());
        } catch (IOException e) {
            fail("Failed to read file: " + file.getName());
        }
    }
}