package tracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.manager.TaskManager;
import tracker.model.Task;

import java.io.IOException;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        Integer id = getId(exchange);

        switch (method) {
            case "GET":
                if (id == null) {
                    // GET /tasks/task/
                    sendOk(exchange, gson.toJson(manager.getTasks()));
                } else {
                    // GET /tasks/task/?id=1
                    Task task = manager.getTask(id);
                    if (task == null) {
                        sendNotFound(exchange, "Задача с id=" + id + " не найдена");
                    } else {
                        sendOk(exchange, gson.toJson(task));
                    }
                }
                break;

            case "POST": {
                // POST /tasks/task/ — тело в JSON
                Task task = gson.fromJson(readBody(exchange), Task.class);
                if (task.getTaskID() == 0) {
                    // id не задан → создаём (менеджер сам назначит id)
                    int newId = manager.addNewTask(task);
                    sendCreated(exchange, gson.toJson(manager.getTask(newId)));
                } else {
                    // id задан → обновляем
                    if (manager.getTask(task.getTaskID()) == null) {
                        sendNotFound(exchange, "Задача с id=" + task.getTaskID() + " не найдена");
                    } else {
                        manager.updateTask(task);
                        sendOk(exchange, gson.toJson(manager.getTask(task.getTaskID())));
                    }
                }
                break;
            }

            case "DELETE":
                if (id == null) {
                    manager.deleteTasks();
                } else {
                    if (manager.getTask(id) == null) {
                        sendNotFound(exchange, "Задача с id=" + id + " не найдена");
                        return;
                    }
                    manager.deleteTask(id);
                }
                sendOk(exchange, "{\"result\":\"ok\"}");
                break;

            default:
                sendMethodNotAllowed(exchange);
        }
    }
}