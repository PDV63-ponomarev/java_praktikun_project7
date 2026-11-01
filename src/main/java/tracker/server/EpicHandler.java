package tracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.manager.TaskManager;
import tracker.model.TaskEpic;

import java.io.IOException;


public class EpicHandler extends BaseHttpHandler{
    public EpicHandler(TaskManager manager, Gson gson) {
        super(manager, gson);
    }

    @Override
    protected void handleRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        Integer id = getId(exchange);

        switch (method) {
            case "GET":
                if (id == null) {
                    sendOk(exchange, gson.toJson(manager.getEpics()));
                } else {
                    TaskEpic epic = manager.getEpic(id);
                    if (epic == null) {
                        sendNotFound(exchange, "Епик с id=" + id + " не найдена");
                    } else {
                        sendOk(exchange, gson.toJson(epic));
                    }
                }
                break;

            case "POST": {
                // POST /tasks/task/ — тело в JSON
                TaskEpic epic = gson.fromJson(readBody(exchange), TaskEpic.class);
                if (epic.getTaskID() == 0) {
                    // id не задан → создаём (менеджер сам назначит id)
                    int newId = manager.addNewEpic(epic);
                    sendCreated(exchange, gson.toJson(manager.getEpic(newId)));
                } else {
                    // id задан → обновляем
                    if (manager.getEpic(epic.getTaskID()) == null) {
                        sendNotFound(exchange, "Епик с id=" + epic.getTaskID() + " не найдена");
                    } else {
                        manager.updateEpic(epic);
                        sendOk(exchange, gson.toJson(manager.getEpic(epic.getTaskID())));
                    }
                }
                break;
            }

            case "DELETE":
                if (id == null) {
                    manager.deleteEpics();
                } else {
                    if (manager.getEpic(id) == null) {
                        sendNotFound(exchange, "Епик с id=" + id + " не найдена");
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
