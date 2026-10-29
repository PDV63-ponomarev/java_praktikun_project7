package tracker.manager;

import tracker.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTasksManager(File file) {
        this.file = file;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(TaskEpic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public int addNewSubtask(TaskSubtask subtask) {
        int id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task)       { super.updateTask(task);       save(); }
    @Override
    public void updateEpic(TaskEpic epic)   { super.updateEpic(epic);       save(); }
    @Override
    public void updateSubtask(TaskSubtask s){ super.updateSubtask(s);       save(); }

    @Override
    public void deleteTask(int id)     { super.deleteTask(id);     save(); }
    @Override
    public void deleteEpic(int id)     { super.deleteEpic(id);     save(); }
    @Override
    public void deleteSubtask(int id)  { super.deleteSubtask(id);  save(); }

    @Override
    public void deleteTasks()     { super.deleteTasks();     save(); }
    @Override
    public void deleteEpics()     { super.deleteEpics();     save(); }
    @Override
    public void deleteSubtasks()  { super.deleteSubtasks();  save(); }

    void save() {
        try (Writer w = new FileWriter(file, StandardCharsets.UTF_8)) {
            w.write("id,type,name,status,description,epic\n");
            for (Task t : getTasks())         w.write(FileTask.toString(t) + "\n");
            for (TaskEpic e : getEpics())     w.write(FileTask.toString(e) + "\n");
            for (TaskSubtask s : getSubtasks()) w.write(FileTask.toString(s) + "\n");
            w.write("\n");
            w.write(FileTask.historyToString(getHistory()));
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager manager = new FileBackedTasksManager(file);
        if (!file.exists() || file.length() == 0) {
            return manager;                      // пустой файл — пустой менеджер
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }

        int cursor = 1;                          // строка 0 — шапка
        int maxId = 0;

        // блок задач: до первой пустой строки (порядок: задачи, эпики, подзадачи)
        while (cursor < lines.size() && !lines.get(cursor).isBlank()) {
            Task task = FileTask.fromString(lines.get(cursor));
            if (task instanceof TaskSubtask sub) {
                manager.subtasks.put(sub.getTaskID(), sub);
                TaskEpic epic = manager.epics.get(sub.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(sub.getTaskID());
                }
            } else if (task instanceof TaskEpic epic) {
                manager.epics.put(epic.getTaskID(), epic);
            } else {
                manager.tasks.put(task.getTaskID(), task);
            }
            maxId = Math.max(maxId, task.getTaskID());
            cursor++;
        }
        manager.nextId = maxId + 1;

        // блок истории: строка после пустой
        cursor++;
        if (cursor < lines.size()) {
            for (int id : FileTask.historyFromString(lines.get(cursor))) {
                Task t = manager.tasks.containsKey(id)    ? manager.tasks.get(id)
                        : manager.epics.containsKey(id)    ? manager.epics.get(id)
                        : manager.subtasks.get(id);
                if (t != null) {
                    manager.historyManager.add(t);
                }
            }
        }
        manager.getTasks().forEach(manager::addToPrioritized);      // или напрямую
        manager.getEpics().forEach(manager::addToPrioritized);
        manager.getSubtasks().forEach(manager::addToPrioritized);
        return manager;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        if (task != null) save();   // просмотр записался в историю — сохраняем
        return task;
    }

    @Override
    public TaskEpic getEpic(int id) {
        TaskEpic epic = super.getEpic(id);
        if (epic != null) save();
        return epic;
    }

    @Override
    public TaskSubtask getSubtask(int id) {
        TaskSubtask sub = super.getSubtask(id);
        if (sub != null) save();
        return sub;
    }
}