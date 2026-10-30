package tracker.server;

import com.sun.net.httpserver.HttpExchange;
import tracker.model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllTasksHandler extends BaseHttpHandler {
    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            sendMethodNotAllowed(exchange);
            return;
        }
        List<Task> all = new ArrayList<>();
        all.addAll(manager.getTasks());
        all.addAll(manager.getEpics());
        all.addAll(manager.getSubtasks());
        sendOk(exchange, gson.toJson(all));
    }
}