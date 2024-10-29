import java.io.IOException;
import java.util.logging.*;

public class Consumer {
    private static final String BUCKET2_NAME = "usu-cs5250-drummerboy-requests";
    private static S3ApplicationInteractor s3ApplicationInteractor;
    private static final Logger logger = Logger.getLogger(Consumer.class.getName());

    public Consumer(String[] args) {
        main(args);
    }

    public static void main(String[] args) {
        boolean verbose = false;
        String storageStrategy = "default";

        for (String arg : args) {
            if (arg.equalsIgnoreCase("-v")) {
                verbose = true;
            } else {
                storageStrategy = arg;
            }
        }

        setupLogger(verbose);

        if (!storageStrategy.equalsIgnoreCase("s3") && !storageStrategy.equalsIgnoreCase("dynamodb")) {
            System.out.println("Invalid storage strategy. Please use 's3' or 'dynamodb'.");
            System.out.println("Usage: java -jar consumer.jar [-v] [s3|dynamodb]");
            System.exit(1);
        }
        logger.info("Storage strategy: " + storageStrategy);

        if (s3ApplicationInteractor == null) {
            s3ApplicationInteractor = new S3ApplicationInteractor(BUCKET2_NAME, storageStrategy, logger);
        }
        System.out.println("Polling for requests...");
        try{
            s3ApplicationInteractor.pollRequests();
        }
        catch (Exception e){
            logger.severe("Error processing requests: " + e.getMessage());
        }
        System.out.println("Exiting consumer, no more requests to process.");
    }

    private static void setupLogger(boolean verbose) {
        try {
            // Reset the logger configuration
            LogManager.getLogManager().reset();

            // Configure the custom logger
            logger.setLevel(Level.ALL);
            FileHandler fileHandler = new FileHandler("consumer.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            if (verbose) {
                ConsoleHandler consoleHandler = new ConsoleHandler();
                consoleHandler.setLevel(Level.ALL);
                consoleHandler.setFormatter(new SimpleFormatter());
                logger.addHandler(consoleHandler);
            }

            // Optionally, remove handlers from the root logger to avoid capturing all system logs
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
        } catch (IOException e) {
            logger.severe("Failed to setup logger: " + e.getMessage());
        }
    }
}