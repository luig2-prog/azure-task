package com.personal.config;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration class for the application.
 * Handles loading configuration from properties file, environment variables, and command line arguments.
 */
@Slf4j
@Data
@Builder
public class AppConfig {
    
    /**
     * Maximum number of retry attempts for failed operations.
     */
    @Builder.Default
    private int maxRetryAttempts = 3;
    
    /**
     * Initial delay in milliseconds between retry attempts.
     */
    @Builder.Default
    private long initialRetryDelayMs = 1000;
    
    /**
     * Maximum delay in milliseconds between retry attempts.
     */
    @Builder.Default
    private long maxRetryDelayMs = 10000;
    
    /**
     * Timeout in milliseconds for HTTP requests.
     */
    @Builder.Default
    private int httpTimeoutMs = 30000;
    
    /**
     * Maximum number of concurrent tasks to process.
     */
    @Builder.Default
    private int maxConcurrentTasks = 5;
    
    /**
     * Path to the CSV file containing task data.
     */
    private String csvFilePath;
    
    /**
     * Whether to validate tasks before processing.
     */
    @Builder.Default
    private boolean validateBeforeProcessing = true;
    
    /**
     * Whether to continue processing if some tasks fail.
     */
    @Builder.Default
    private boolean continueOnError = false;
    
    /**
     * Default configuration values
     */
    public static final String DEFAULT_CSV_FILE = "tasks.csv";
    
    /**
     * Loads configuration from multiple sources in the following order:
     * 1. Command line arguments
     * 2. Environment variables
     * 3. Properties file
     * 4. Default values
     *
     * @param args Command line arguments
     * @return AppConfig object with loaded configuration
     */
    public static AppConfig loadConfig(String[] args) {
        AppConfigBuilder builder = AppConfig.builder();
        
        // Set default values
        builder.csvFilePath(DEFAULT_CSV_FILE)
                .maxRetryAttempts(3)
                .initialRetryDelayMs(1000)
                .maxRetryDelayMs(10000)
                .httpTimeoutMs(30000)
                .maxConcurrentTasks(5)
                .validateBeforeProcessing(true)
                .continueOnError(false);
        
        // Load from properties file if exists
        loadFromPropertiesFile(builder);
        
        // Load from environment variables
        loadFromEnvironmentVariables(builder);
        
        // Load from command line arguments
        loadFromCommandLineArgs(builder, args);
        
        return builder.build();
    }
    
    /**
     * Loads configuration from a properties file.
     *
     * @param builder The AppConfigBuilder to update
     */
    private static void loadFromPropertiesFile(AppConfigBuilder builder) {
        Path configPath = Paths.get("config.properties");
        if (Files.exists(configPath)) {
            try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
                Properties props = new Properties();
                props.load(fis);
                
                if (props.containsKey("csv.file")) {
                    builder.csvFilePath(props.getProperty("csv.file"));
                }
                if (props.containsKey("app.maxRetryAttempts")) {
                    builder.maxRetryAttempts(Integer.parseInt(props.getProperty("app.maxRetryAttempts")));
                }
                if (props.containsKey("app.initialRetryDelayMs")) {
                    builder.initialRetryDelayMs(Long.parseLong(props.getProperty("app.initialRetryDelayMs")));
                }
                if (props.containsKey("app.maxRetryDelayMs")) {
                    builder.maxRetryDelayMs(Long.parseLong(props.getProperty("app.maxRetryDelayMs")));
                }
                if (props.containsKey("app.httpTimeoutMs")) {
                    builder.httpTimeoutMs(Integer.parseInt(props.getProperty("app.httpTimeoutMs")));
                }
                if (props.containsKey("app.maxConcurrentTasks")) {
                    builder.maxConcurrentTasks(Integer.parseInt(props.getProperty("app.maxConcurrentTasks")));
                }
                if (props.containsKey("app.validateBeforeProcessing")) {
                    builder.validateBeforeProcessing(Boolean.parseBoolean(props.getProperty("app.validateBeforeProcessing")));
                }
                if (props.containsKey("app.continueOnError")) {
                    builder.continueOnError(Boolean.parseBoolean(props.getProperty("app.continueOnError")));
                }
                
                log.info("Configuration loaded from properties file");
            } catch (IOException e) {
                log.warn("Error loading properties file: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Loads configuration from environment variables.
     *
     * @param builder The AppConfigBuilder to update
     */
    private static void loadFromEnvironmentVariables(AppConfigBuilder builder) {
        String csvFile = System.getenv("AZURE_TASK_CSV_FILE");
        if (csvFile != null && !csvFile.isEmpty()) {
            builder.csvFilePath(csvFile);
        }
        
        String maxRetryAttempts = System.getenv("AZURE_MAX_RETRY_ATTEMPTS");
        if (maxRetryAttempts != null && !maxRetryAttempts.isEmpty()) {
            try {
                builder.maxRetryAttempts(Integer.parseInt(maxRetryAttempts));
            } catch (NumberFormatException e) {
                log.warn("Invalid value for AZURE_MAX_RETRY_ATTEMPTS: {}", maxRetryAttempts);
            }
        }
        
        String initialRetryDelayMs = System.getenv("AZURE_INITIAL_RETRY_DELAY_MS");
        if (initialRetryDelayMs != null && !initialRetryDelayMs.isEmpty()) {
            try {
                builder.initialRetryDelayMs(Long.parseLong(initialRetryDelayMs));
            } catch (NumberFormatException e) {
                log.warn("Invalid value for AZURE_INITIAL_RETRY_DELAY_MS: {}", initialRetryDelayMs);
            }
        }
        
        String maxRetryDelayMs = System.getenv("AZURE_MAX_RETRY_DELAY_MS");
        if (maxRetryDelayMs != null && !maxRetryDelayMs.isEmpty()) {
            try {
                builder.maxRetryDelayMs(Long.parseLong(maxRetryDelayMs));
            } catch (NumberFormatException e) {
                log.warn("Invalid value for AZURE_MAX_RETRY_DELAY_MS: {}", maxRetryDelayMs);
            }
        }
        
        String httpTimeoutMs = System.getenv("AZURE_HTTP_TIMEOUT_MS");
        if (httpTimeoutMs != null && !httpTimeoutMs.isEmpty()) {
            try {
                builder.httpTimeoutMs(Integer.parseInt(httpTimeoutMs));
            } catch (NumberFormatException e) {
                log.warn("Invalid value for AZURE_HTTP_TIMEOUT_MS: {}", httpTimeoutMs);
            }
        }
        
        String maxConcurrentTasks = System.getenv("AZURE_MAX_CONCURRENT_TASKS");
        if (maxConcurrentTasks != null && !maxConcurrentTasks.isEmpty()) {
            try {
                builder.maxConcurrentTasks(Integer.parseInt(maxConcurrentTasks));
            } catch (NumberFormatException e) {
                log.warn("Invalid value for AZURE_MAX_CONCURRENT_TASKS: {}", maxConcurrentTasks);
            }
        }
        
        String validateBeforeProcessing = System.getenv("AZURE_VALIDATE_BEFORE_PROCESSING");
        if (validateBeforeProcessing != null && !validateBeforeProcessing.isEmpty()) {
            builder.validateBeforeProcessing(Boolean.parseBoolean(validateBeforeProcessing));
        }
        
        String continueOnError = System.getenv("AZURE_CONTINUE_ON_ERROR");
        if (continueOnError != null && !continueOnError.isEmpty()) {
            builder.continueOnError(Boolean.parseBoolean(continueOnError));
        }
    }
    
    /**
     * Loads configuration from command line arguments.
     *
     * @param builder The AppConfigBuilder to update
     * @param args Command line arguments
     */
    private static void loadFromCommandLineArgs(AppConfigBuilder builder, String[] args) {
        if (args == null || args.length == 0) {
            return;
        }
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equals("--csv") || arg.equals("-c")) {
                if (i + 1 < args.length) {
                    builder.csvFilePath(args[++i]);
                }
            } else if (arg.equals("--max-retry-attempts") || arg.equals("-r")) {
                if (i + 1 < args.length) {
                    try {
                        builder.maxRetryAttempts(Integer.parseInt(args[++i]));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for max-retry-attempts: {}", args[i]);
                    }
                }
            } else if (arg.equals("--initial-retry-delay") || arg.equals("-d")) {
                if (i + 1 < args.length) {
                    try {
                        builder.initialRetryDelayMs(Long.parseLong(args[++i]));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for initial-retry-delay: {}", args[i]);
                    }
                }
            } else if (arg.equals("--max-retry-delay") || arg.equals("-m")) {
                if (i + 1 < args.length) {
                    try {
                        builder.maxRetryDelayMs(Long.parseLong(args[++i]));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for max-retry-delay: {}", args[i]);
                    }
                }
            } else if (arg.equals("--http-timeout") || arg.equals("-t")) {
                if (i + 1 < args.length) {
                    try {
                        builder.httpTimeoutMs(Integer.parseInt(args[++i]));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for http-timeout: {}", args[i]);
                    }
                }
            } else if (arg.equals("--max-concurrent-tasks") || arg.equals("-c")) {
                if (i + 1 < args.length) {
                    try {
                        builder.maxConcurrentTasks(Integer.parseInt(args[++i]));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid value for max-concurrent-tasks: {}", args[i]);
                    }
                }
            } else if (arg.equals("--validate-before-processing") || arg.equals("-v")) {
                builder.validateBeforeProcessing(true);
            } else if (arg.equals("--continue-on-error") || arg.equals("-e")) {
                builder.continueOnError(true);
            } else if (arg.equals("--help") || arg.equals("-h")) {
                printHelp();
                System.exit(0);
            }
        }
    }
    
    /**
     * Prints help information for command line arguments.
     */
    private static void printHelp() {
        System.out.println("Azure DevOps Task Manager");
        System.out.println("Usage: java -jar azure-task.jar [options]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -c, --csv FILE           Path to the CSV file (default: tasks.csv)");
        System.out.println("  -r, --max-retry-attempts N Maximum number of retry attempts (default: 3)");
        System.out.println("  -d, --initial-retry-delay MS Initial delay between retries in milliseconds (default: 1000)");
        System.out.println("  -m, --max-retry-delay MS Maximum delay between retries in milliseconds (default: 10000)");
        System.out.println("  -t, --http-timeout MS   Timeout for HTTP requests in milliseconds (default: 30000)");
        System.out.println("  -c, --max-concurrent-tasks N Maximum number of concurrent tasks (default: 5)");
        System.out.println("  -v, --validate-before-processing Validate CSV file before processing");
        System.out.println("  -e, --continue-on-error Continue processing even if some tasks fail");
        System.out.println("  -h, --help               Show this help message");
    }
} 