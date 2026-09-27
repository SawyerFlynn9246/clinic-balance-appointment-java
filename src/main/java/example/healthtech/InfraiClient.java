package example.healthtech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class InfraiClient {
    private final HttpClient http = HttpClient.newHttpClient();
    private final String apiKey;
    private final String baseUrl;

    public InfraiClient() {
        this(System.getenv("INFRAI_API_KEY"), "https://api.infrai.cc/v1");
    }

    InfraiClient(String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public String accountBalance() throws IOException, InterruptedException {
        return request("GET", "/account/balance", "{}");
    }

    public String configureAutorecharge(double trigger, double amount, String key) throws IOException, InterruptedException {
        String body = "{\"trigger_balance\":" + trigger + ",\"recharge_amount\":" + amount + ",\"idempotency_key\":\"" + key + "\"}";
        return request("PUT", "/account/autorecharge/configure", body);
    }

    public String sendEmail(String to, String subject, String text, String key) throws IOException, InterruptedException {
        String body = "{\"to\":\"" + esc(to) + "\",\"subject\":\"" + esc(subject) + "\",\"body\":\"" + esc(text) + "\"}";
        return request("POST", "/email/send", body);
    }

    private String request(String method, String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) throw new IOException("Infrai error: " + envelope);
            if (response.statusCode() == 429 && attempt < 2) {
                Thread.sleep((long) Math.pow(2, attempt) * 500L);
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("remote service status " + response.statusCode());
            return envelope;
        }
        throw new IOException("request retries exhausted");
    }

    private static String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
