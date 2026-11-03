package tracker.server;

import tracker.manager.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final String serverUrl;
    private final String apiToken;
    private final HttpClient client = HttpClient.newHttpClient();

    public KVTaskClient(String serverUrl) {
        this.serverUrl = serverUrl;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/register"))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Регистрация не удалась, код " + response.statusCode());
            }
            this.apiToken = response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("KVServer недоступен: " + serverUrl
                    + " (запусти его первым!)", e);
        }
    }

    public void put(String key, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/save/" + key + "?API_TOKEN=" + apiToken))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Не сохранился ключ " + key + ", код " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Сетевая ошибка при сохранении " + key, e);
        }
    }

    /** null, если ключа на сервере нет (404) — нормальная ситуация при первом запуске */
    public String load(String key) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/load/" + key + "?API_TOKEN=" + apiToken))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) return null;
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Не загрузился ключ " + key + ", код " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ManagerSaveException("Сетевая ошибка при загрузке " + key, e);
        }
    }
}