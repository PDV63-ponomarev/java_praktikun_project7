package tracker.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import tracker.manager.Managers;
import tracker.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;

    private final HttpServer server;
    private final TaskManager manager;
    private final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")   // иначе Gson пишет Date как "Oct 30, 2026, 2:00:00 PM"
            .create();

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // регистрируем самые специфичные пути первыми — HttpServer выбирает контекст
        // с самым длинным совпадающим префиксом
        server.createContext("/tasks/task", new TaskHandler(manager, gson));
        server.createContext("/tasks/subtask", new SubtaskHandler(manager, gson));
        server.createContext("/tasks/epic", new EpicHandler(manager, gson));
        server.createContext("/tasks/history", new HistoryHandler(manager, gson));
        server.createContext("/tasks", new AllTasksHandler(manager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на порту " + PORT);
    }

    public void stop() {
        server.stop(0);
    }

    public TaskManager getManager() {
        return manager;
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }
}

