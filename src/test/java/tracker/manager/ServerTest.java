package tracker.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServerTest {
    private HttpTaskServer server;
    private  final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL= "http://localhost:8080/tasks";


    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @BeforeEach
    void startServer() throws IOException {
        server = new HttpTaskServer();
        server.start();
    }

    @AfterEach
    void closeServer(){
        // на случай если start() выбросил исключение
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void serverShouldGetCode200() throws IOException, InterruptedException {
        assertEquals(200, get("").statusCode());
    }

    @Test
    void taskShouldGetCode200() throws IOException, InterruptedException {
        assertEquals(200, get("/task").statusCode());
    }

    @Test
    void subtaskShouldGetCode200() throws IOException, InterruptedException {
        assertEquals(200, get("/subtask").statusCode());
    }

    @Test
    void epicShouldGetCode200() throws IOException, InterruptedException {
        assertEquals(200, get("/epic").statusCode());
    }

    @Test
    void historyShouldGetCode200() throws IOException, InterruptedException {
        assertEquals(200, get("/history").statusCode());
    }


}
