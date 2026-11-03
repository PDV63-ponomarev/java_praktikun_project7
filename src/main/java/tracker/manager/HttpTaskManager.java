package tracker.manager;


import com.google.gson.*;
import tracker.server.KVTaskClient;
import tracker.model.TaskEpic;
import tracker.model.TaskSubtask;
import tracker.model.Task;

import java.util.List;
import java.util.stream.Collectors;
public class HttpTaskManager extends FileBackedTasksManager {
    private static final Gson gson = new Gson();
    private final KVTaskClient taskClient;

    public HttpTaskManager(HistoryManager historyManager, String url) {
        super(historyManager);              // защищённый конструктор без файла
        this.taskClient = new KVTaskClient(url);
        loadFromServer();                   // состояние поднимается сразу при создании
    }

    @Override
    public void save() {
        taskClient.put("task", gson.toJson(tasks.values()));
        taskClient.put("subtask", gson.toJson(subtasks.values()));
        taskClient.put("epic", gson.toJson(epics.values()));
        List<Integer> historyIds = getHistory().stream()
                .map(Task::getTaskID)
                .collect(Collectors.toList());
        taskClient.put("history", gson.toJson(historyIds));
    }

    private void loadFromServer() {
        loadTasks("task");
        loadTasks("subtask");
        loadTasks("epic");
        loadHistory();
    }

    private void loadTasks(String key) {
        String json = taskClient.load(key);
        if (json == null || json.isBlank()) return;    // первый запуск — на сервере пусто
        for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
            switch (key) {
                case "task" -> {
                    Task task = gson.fromJson(element, Task.class);
                    tasks.put(task.getTaskID(), task);
                    addToPrioritized(task);
                    nextId = Math.max(nextId, task.getTaskID() + 1);   // ← вот это спасает от коллизий
                }
                case "subtask" -> {
                    TaskSubtask sub = gson.fromJson(element, TaskSubtask.class);
                    subtasks.put(sub.getTaskID(), sub);
                    addToPrioritized(sub);
                    nextId = Math.max(nextId, sub.getTaskID() + 1);
                }
                case "epic" -> {
                    TaskEpic epic = gson.fromJson(element, TaskEpic.class);
                    epics.put(epic.getTaskID(), epic);
                    addToPrioritized(epic);
                    nextId = Math.max(nextId, epic.getTaskID() + 1);
                }
            }
        }
    }

    private void loadHistory() {
        String json = taskClient.load("history");
        if (json == null || json.isBlank()) return;
        for (JsonElement element : JsonParser.parseString(json).getAsJsonArray()) {
            int id = element.getAsInt();
            Task t = tasks.containsKey(id) ? tasks.get(id)
                    : epics.containsKey(id) ? epics.get(id)
                    : subtasks.get(id);
            if (t != null) historyManager.add(t);
        }
    }
}