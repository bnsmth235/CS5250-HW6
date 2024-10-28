import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.logging.*;

public class Consumer {
    private static final String BUCKET_NAME = "usu-cs5250-drummerboy-requests";
    private static S3Interactor s3Interactor;
    private static final Logger logger = Logger.getLogger(Consumer.class.getName());

    public Consumer(String[] args) {
        main(args);
    }

    public void setS3Interactor(S3Interactor s3Interactor) {
        this.s3Interactor = s3Interactor;
    }

    public static void main(String[] args) {
        setupLogger();

        Options options = new Options();
        Option storageOption = new Option("s", "storage", true, "Storage strategy (S3 or DynamoDB)");
        storageOption.setRequired(true);
        options.addOption(storageOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.severe("Failed to parse command line arguments: " + e.getMessage());
            formatter.printHelp("Consumer", options);
            System.exit(1);
            return;
        }

        String storageStrategy = cmd.getOptionValue("storage");
        logger.info("Storage strategy: " + storageStrategy);

        if (s3Interactor == null) {
            s3Interactor = new S3Interactor(BUCKET_NAME, storageStrategy, logger);
        }
        s3Interactor.pollRequests();
    }

    private static void setupLogger() {
        try {
            LogManager.getLogManager().reset();
            Logger rootLogger = Logger.getLogger("");
            FileHandler fileHandler = new FileHandler("consumer.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.ALL);
        } catch (IOException e) {
            logger.severe("Failed to setup logger: " + e.getMessage());
        }
    }
}