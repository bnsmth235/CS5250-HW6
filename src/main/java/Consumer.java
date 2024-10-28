import org.apache.commons.cli.*;

public class Consumer {
    private static final String BUCKET_NAME = "usu-cs5250-drummerboy-requests";

    public static void main(String[] args) {
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
            System.out.println(e.getMessage());
            formatter.printHelp("Consumer", options);
            System.exit(1);
            return;
        }

        String storageStrategy = cmd.getOptionValue("storage");
        S3Interactor s3Interactor = new S3Interactor(BUCKET_NAME, storageStrategy);
        s3Interactor.pollRequests();
    }
}