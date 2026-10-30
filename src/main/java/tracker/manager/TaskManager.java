package tracker.manager;

import tracker.model.*;
import java.util.List;

public interface TaskManager {
    // получение списков
    List<Task> getTasks();
    List<TaskEpic> getEpics();
    List<TaskSubtask> getSubtasks();

    // получение по id (и добавление в историю!)
    Task getTask(int id);
    TaskEpic getEpic(int id);
    TaskSubtask getSubtask(int id);
    List<TaskSubtask> getEpicSubtasks(int epicId);
    
    // создание — id назначает сам менеджер
    int addNewTask(Task task);
    int addNewEpic(TaskEpic epic);
    int addNewSubtask(TaskSubtask subtask);
    List<Task> getPrioritizedTasks();

    // обновление
    void updateTask(Task task);
    void updateEpic(TaskEpic epic);
    void updateSubtask(TaskSubtask subtask);

    // удаление
    void deleteTask(int id);
    void deleteEpic(int id);
    void deleteSubtask(int id);
    void deleteTasks();
    void deleteEpics();
    void deleteSubtasks();

    List<Task> getHistory();
}