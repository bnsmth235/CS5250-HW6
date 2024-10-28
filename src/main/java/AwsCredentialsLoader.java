import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class AwsCredentialsLoader {
    public static StaticCredentialsProvider loadCredentials(String filePath) throws IOException {
        Properties properties = new Properties();
        properties.load(Files.newInputStream(Paths.get(filePath)));

        String accessKeyId = properties.getProperty("aws_access_key_id");
        String secretAccessKey = properties.getProperty("aws_secret_access_key");

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return StaticCredentialsProvider.create(awsCreds);
    }
}