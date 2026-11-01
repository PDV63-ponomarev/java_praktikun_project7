package tracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.manager.TaskManager;
import tracker.model.TaskSubtask;

import java.io.IOException;


public class SubtaskHandler extends BaseHttpHandler{
    public SubtaskHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        Integer id = getId(exchange);

        switch (method) {
            case "GET":
                // Эндпоинт «подзадачи эпика»: /tasks/subtask/epic/?id=N
                if (path.endsWith("/epic")) {
                    if (id == null) {
                        sendNotFound(exchange, "Не указан id эпика");
                        return;
                    }
                    if (manager.getEpic(id) == null) {
                        sendNotFound(exchange, "Эпик с id=" + id + " не найден");
                        return;
                    }
                    sendOk(exchange, gson.toJson(manager.getEpicSubtasks(id)));
                    break;
                }

                // Обычный GET /tasks/subtask  и  /tasks/subtask/{id}
                if (id == null) {
                    sendOk(exchange, gson.toJson(manager.getSubtasks()));
                } else {
                    TaskSubtask subtask = manager.getSubtask(id);
                    if (subtask == null) {
                        sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
                    } else {
                        sendOk(exchange, gson.toJson(subtask));
                    }
                }
                break;

            case "POST": {
                TaskSubtask subtask = gson.fromJson(readBody(exchange), TaskSubtask.class);
                if (subtask.getTaskID() == 0) {
                    int newId = manager.addNewSubtask(subtask);
                    sendCreated(exchange, gson.toJson(manager.getSubtask(newId)));
                } else {
                    if (manager.getSubtask(subtask.getTaskID()) == null) {
                        sendNotFound(exchange, "Подзадача с id=" + subtask.getTaskID() + " не найдена");
                    } else {
                        manager.updateSubtask(subtask);
                        sendOk(exchange, gson.toJson(manager.getSubtask(subtask.getTaskID())));
                    }
                }
                break;
            }

            case "DELETE":
                if (id == null) {
                    manager.deleteSubtasks();
                } else {
                    if (manager.getSubtask(id) == null) {
                        sendNotFound(exchange, "Подзадача с id=" + id + " не найдена");
                        return;
                    }
                    manager.deleteSubtask(id);
                }
                sendOk(exchange, "{\"result\":\"ok\"}");
                break;

            default:
                sendMethodNotAllowed(exchange);
        }
    }
}
