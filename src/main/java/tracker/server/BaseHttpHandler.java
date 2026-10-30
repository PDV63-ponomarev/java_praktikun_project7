package tracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tracker.manager.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager manager;
    protected final Gson gson;

    protected BaseHttpHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public final void handle(HttpExchange exchange) throws IOException {
        try {
            handleRequest(exchange);
        } catch (IllegalArgumentException e) {
            // твои исключения о пересечении по времени и «эпик не найден»
            send(exchange, 406, "{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            send(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            exchange.close();
        }
    }

    protected abstract void handleRequest(HttpExchange exchange) throws IOException;

    // ---------- хелперы ----------

    protected void send(HttpExchange exchange, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendOk(HttpExchange exchange, String json) throws IOException {
        send(exchange, 200, json);
    }

    protected void sendCreated(HttpExchange exchange, String json) throws IOException {
        send(exchange, 201, json);
    }

    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        send(exchange, 404, "{\"error\":\"" + message + "\"}");
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        send(exchange, 405, "{\"error\":\"Метод не поддерживается\"}");
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    /** id из строки вида "?id=1"; null — если параметра нет */
    protected Integer getId(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals("id")) {
                return Integer.parseInt(kv[1]);
            }
        }
        return null;
    }
}