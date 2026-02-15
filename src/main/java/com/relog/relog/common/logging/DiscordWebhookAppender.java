package com.relog.relog.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class DiscordWebhookAppender extends AppenderBase<ILoggingEvent> {

    private String webhookUrl;
    private String applicationName = "Spring Boot App";
    private String environment = "unknown";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        String payload = buildPayload(event);
        executor.submit(() -> sendToDiscord(payload));
    }

    private String buildPayload(ILoggingEvent event) {
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Seoul"))
                .format(Instant.ofEpochMilli(event.getTimeStamp()));

        String loggerName = event.getLoggerName();
        if (loggerName.length() > 40) {
            String[] parts = loggerName.split("\\.");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                sb.append(parts[i].charAt(0)).append(".");
            }
            sb.append(parts[parts.length - 1]);
            loggerName = sb.toString();
        }

        String message = escapeJson(event.getFormattedMessage());
        if (message.length() > 300) {
            message = message.substring(0, 300) + "...";
        }

        String stackTrace = extractStackTrace(event);

        StringBuilder json = new StringBuilder();
        json.append("{\"embeds\":[{");
        json.append("\"title\":\"🚨 Error Alert\",");
        json.append("\"color\":16711680,");
        json.append("\"fields\":[");
        json.append("{\"name\":\"📌 Application\",\"value\":\"").append(escapeJson(applicationName)).append("\",\"inline\":true},");
        json.append("{\"name\":\"🌍 Environment\",\"value\":\"").append(escapeJson(environment)).append("\",\"inline\":true},");
        json.append("{\"name\":\"⏰ Timestamp\",\"value\":\"").append(timestamp).append("\",\"inline\":true},");
        json.append("{\"name\":\"📂 Logger\",\"value\":\"`").append(escapeJson(loggerName)).append("`\",\"inline\":false},");
        json.append("{\"name\":\"💬 Message\",\"value\":\"```").append(message).append("```\",\"inline\":false}");

        if (!stackTrace.isEmpty()) {
            json.append(",{\"name\":\"📋 Stack Trace\",\"value\":\"```java\\n").append(stackTrace).append("```\",\"inline\":false}");
        }

        json.append("],");
        json.append("\"footer\":{\"text\":\"Error Logger\"},");
        json.append("\"timestamp\":\"").append(Instant.ofEpochMilli(event.getTimeStamp()).toString()).append("\"");
        json.append("}]}");

        return json.toString();
    }

    private String extractStackTrace(ILoggingEvent event) {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append(throwableProxy.getClassName()).append(": ").append(throwableProxy.getMessage()).append("\n");

        StackTraceElementProxy[] stackTrace = throwableProxy.getStackTraceElementProxyArray();
        int limit = Math.min(stackTrace.length, 8);
        for (int i = 0; i < limit; i++) {
            sb.append("  at ").append(stackTrace[i].getSTEAsString()).append("\n");
        }
        if (stackTrace.length > limit) {
            sb.append("  ... ").append(stackTrace.length - limit).append(" more");
        }

        String result = escapeJson(sb.toString());
        if (result.length() > 900) {
            result = result.substring(0, 900) + "\\n...truncated";
        }
        return result;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("`", "\\`");
    }

    private void sendToDiscord(String payload) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 429) {
                Thread.sleep(1000);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to send Discord webhook: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        super.stop();
    }
}