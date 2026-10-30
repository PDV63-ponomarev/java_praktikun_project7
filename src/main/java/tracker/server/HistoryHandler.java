package tracker.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendMethodNotAllowed(exchange);
            return;
        }
        sendOk(exchange, gson.toJson(manager.getHistory()));
    }
}