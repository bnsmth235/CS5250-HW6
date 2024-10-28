import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConsumerTests {

    @Test
    public void testCommandLineParsing() {
        String[] args = {"-s", "S3"};
        Consumer consumer = new Consumer(args);

        // Create an ArgumentCaptor for the storage strategy
        ArgumentCaptor<String> storageStrategyCaptor = ArgumentCaptor.forClass(String.class);

        // Mock the S3Interactor constructor
        try (MockedConstruction<S3Interactor> mocked = mockConstruction(S3Interactor.class, (mock, context) -> {
            when(mock.getStorageStrategy()).thenReturn(context.arguments().get(1).toString());
        })) {
            // Call the main method to trigger the command line parsing
            Consumer.main(args);

            // Verify that the storage strategy was captured correctly
            assertEquals("S3", mocked.constructed().get(0).getStorageStrategy());
        }
    }
}