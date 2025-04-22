package com.personal.util;

import com.personal.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for handling retries with exponential backoff.
 */
@Slf4j
public class RetryUtil {

    /**
     * Executes a callable with retry logic.
     *
     * @param <T> The type of the result
     * @param callable The callable to execute
     * @param config The application configuration
     * @return The result of the callable
     * @throws Exception If all retry attempts fail
     */
    public static <T> T executeWithRetry(Callable<T> callable, AppConfig config) throws Exception {
        int attempts = 0;
        long delay = config.getInitialRetryDelayMs();
        
        while (true) {
            try {
                return callable.call();
            } catch (Exception e) {
                attempts++;
                if (attempts >= config.getMaxRetryAttempts()) {
                    log.error("Max retry attempts ({}) reached. Last error: {}", 
                        config.getMaxRetryAttempts(), e.getMessage());
                    throw e;
                }
                
                log.warn("Attempt {} failed. Retrying in {} ms. Error: {}", 
                    attempts, delay, e.getMessage());
                
                try {
                    TimeUnit.MILLISECONDS.sleep(delay);
                    delay = Math.min(delay * 2, config.getMaxRetryDelayMs());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
    }
    
    /**
     * Determines if an exception is retryable.
     *
     * @param e The exception to check
     * @return true if the exception is retryable, false otherwise
     */
    private static boolean isRetryableException(Exception e) {
        // Network-related exceptions
        if (e instanceof IOException) {
            return true;
        }
        
        // HTTP-related exceptions
        if (e instanceof java.net.http.HttpConnectTimeoutException) {
            return true;
        }
        
        // Azure DevOps API rate limiting
        if (e instanceof RuntimeException && e.getMessage() != null && 
                (e.getMessage().contains("429") || e.getMessage().contains("Too Many Requests"))) {
            return true;
        }
        
        // Azure DevOps API temporary errors
        if (e instanceof RuntimeException && e.getMessage() != null && 
                (e.getMessage().contains("503") || e.getMessage().contains("Service Unavailable"))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Executes an HTTP request with retry logic.
     *
     * @param <T> The type of the response
     * @param requestSupplier The supplier function that creates and sends the HTTP request
     * @param config The application configuration
     * @param operationName The name of the operation for logging
     * @return The HTTP response
     * @throws IOException If an IO error occurs
     * @throws InterruptedException If the thread is interrupted
     */
    public static <T> HttpResponse<T> executeHttpRequestWithRetry(
            Supplier<HttpResponse<T>> requestSupplier, AppConfig config, String operationName) 
            throws IOException, InterruptedException {
        try {
            return executeWithRetry(() -> {
                try {
                    return requestSupplier.get();
                } catch (Exception e) {
                    if (!isRetryableException(e)) {
                        throw new RuntimeException("Non-retryable error in " + operationName, e);
                    }
                    throw e;
                }
            }, config);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            if (e instanceof InterruptedException) {
                throw (InterruptedException) e;
            }
            throw new IOException("Error executing " + operationName, e);
        }
    }
} 