package tracker.manager;
import org.junit.jupiter.api.io.TempDir;
import tracker.model.*;
import static tracker.model.EnumStatus.*;
import static tracker.model.TaskType.*;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    abstract T createManager();   // каждый наследник создаёт свою реализацию

    @BeforeEach
    void init() {
        taskManager = createManager();
    }

    @Test
    void addNewTaskShouldSaveTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", NEW, TASK,
                60, null);
        final int taskId = taskManager.addNewTask(task);

        final Task savedTask = taskManager.getTask(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void getTasksShouldReturnEmptyListWhenNoTasks() {
        final List<Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "getTasks() не должен возвращать null");
        assertTrue(tasks.isEmpty(), "Список задач должен быть пустым");
     }

   @Test
    void getTaskWithWrongIdShouldReturnNull() {
        assertNull(taskManager.getTask(999),"getTasks() должен возвращать null" );
    }

   @Test
    void addNewSubtaskShouldFailWhenEpicNotExists() {
        TaskSubtask subtask = new TaskSubtask("S", "d", NEW, 999, SUBTASK,
                60, null);

        assertThrows(IllegalArgumentException.class,
                () -> taskManager.addNewSubtask(subtask),
                "Должно быть исключение, если эпика с id=999 нет");
    }

    @Test
    void deleteTaskShouldRemoveTaskAndUpdateState() {

        TaskEpic epic = new TaskEpic("E", "d", DONE, EPIC,
                60, null);
        final int epicId = taskManager.addNewEpic(epic);
        TaskSubtask subtask = new TaskSubtask("S", "d", IN_PROGRESS, epic.getTaskID(), SUBTASK,
                60, null);
        final int subtaskId  = taskManager.addNewSubtask(subtask);

        assertTrue(taskManager.getEpic(epicId).getSubtaskIds().contains(subtaskId),
                "Подзадача должна быть в эпике до удаления");
        assertEquals(IN_PROGRESS, taskManager.getEpic(epicId).getTaskStatus(),
                "Эпик с IN_PROGRESS-подзадачей должен быть IN_PROGRESS");

        taskManager.deleteSubtask(subtaskId);

        assertNull(taskManager.getSubtask(subtaskId), "Задача не удалена");
        assertTrue(taskManager.getEpic(epicId).getSubtaskIds().isEmpty(),
                "Эпик не должен содержать удалённую подзадачу");
        assertEquals(NEW, taskManager.getEpic(epicId).getTaskStatus(),
                "Эпик без подзадач должен быть NEW");
    }

    @Test
    void taskShouldCalculateEndTime() {
        Date start = new Date(1_000_000);
        Task task = new Task("T", "d", NEW, TASK, 90, start);  // 90 минут
        taskManager.addNewTask(task);

        assertEquals(new Date(start.getTime() + 90 * 60_000L), taskManager.getTask(task.getTaskID()).getEndTime());
    }

    @Test
    void epicShouldRecalculateTimeFromSubtasks() {
        TaskEpic epic = new TaskEpic("E", "d", NEW, EPIC, 0, null);
        int epicId = taskManager.addNewEpic(epic);

        Date start1 = new Date(1_000_000);
        Date start2 = new Date(4_600_000); // через час после начала s1
        TaskSubtask s1 = new TaskSubtask("S1", "d", NEW, epicId, SUBTASK, 60, start1);   // 60 мин
        TaskSubtask s2 = new TaskSubtask("S2", "d", NEW, epicId, SUBTASK, 30, start2);   // 30 мин
        taskManager.addNewSubtask(s1);
        taskManager.addNewSubtask(s2);

        TaskEpic saved = taskManager.getEpic(epicId);
        assertEquals(start1, saved.getStartTime(), "Старт эпика = старт самой ранней подзадачи");
        assertEquals(90, saved.getDuration(), "Продолжительность эпика = сумма подзадач");
        assertEquals(new Date(start2.getTime() + 30 * 60_000L), saved.getEndTime(),
                "Конец эпика = конец самой поздней подзадачи");
    }

    // ---------- приоритизация ----------
    @Test
    void prioritizedTasksShouldBeSortedByStartTime() {
        Date early = new Date(1_000_000), late = new Date(5_000_000);
        Task a = new Task("A", "d", NEW, TASK, 60, late);
        Task b = new Task("B", "d", NEW, TASK, 60, early);
        Task c = new Task("C", "d", NEW, TASK, 60, null);   // без даты — в конец
        taskManager.addNewTask(a);
        taskManager.addNewTask(b);
        taskManager.addNewTask(c);

        List<Task> prioritized = taskManager.getPrioritizedTasks();
        assertEquals(b, prioritized.get(0));
        assertEquals(a, prioritized.get(1));
        assertEquals(c, prioritized.get(2), "Задача без startTime должна быть последней");
    }

    // ---------- пересечения ----------
    @Test
    void addNewTaskShouldThrowWhenTimeOverlaps() {
        Date start = new Date(1_000_000);
        taskManager.addNewTask(new Task("A", "d", NEW, TASK, 60, start));
        Task overlapping = new Task("B", "d", NEW, TASK, 60, new Date(start.getTime() + 30 * 60_000L));

        assertThrows(IllegalArgumentException.class, () -> taskManager.addNewTask(overlapping));
    }

    @Test
    void updateTaskShouldAllowKeepingSameTime() {
        Date start = new Date(1_000_000);
        int id = taskManager.addNewTask(new Task("A", "d", NEW, TASK, 60, start));
        Task updated = new Task("A2", "d", IN_PROGRESS, TASK, 60, start);
        updated.setTaskID(id);

        assertDoesNotThrow(() -> taskManager.updateTask(updated), "Своё же время — не пересечение");
    }
}

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }
}

class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    @TempDir Path tempDir;
    @Override
    FileBackedTasksManager createManager() {
        return new FileBackedTasksManager(tempDir.resolve("tasks.csv").toFile());
    }

    @Test
    void saveAndLoadedEmptyTask() throws IOException {
        File file = File.createTempFile("test", ".csv");
        file.deleteOnExit();

        FileBackedTasksManager manager = new FileBackedTasksManager(file);
        manager.save();

        FileBackedTasksManager loaded = FileBackedTasksManager.loadFromFile(file);

        assertTrue(loaded.getTasks().isEmpty(),"Список задач должен быть пустым");
        assertTrue(loaded.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loaded.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loaded.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void saveAndLoadedEpicWithoutSubtask() throws IOException {
        File file = File.createTempFile("test", ".csv");
        file.deleteOnExit();

        TaskEpic epic = new TaskEpic("E", "d", NEW,  EPIC,
                60, null);
        FileBackedTasksManager manager = new FileBackedTasksManager(file);
        manager.addNewEpic(epic);
        manager.save();

        FileBackedTasksManager loaded = FileBackedTasksManager.loadFromFile(file);

        assertEquals(epic, loaded.getEpics().get(0), "Эпик в менеджере не равен эпику в файле");
        assertTrue(loaded.getTasks().isEmpty(),"Список задач должен быть пустым");
        assertTrue(loaded.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loaded.getHistory().isEmpty(), "История должна быть пустой");
    }

    @Test
    void loadedManagerShouldEqualSavedManager() {
        File file = tempDir.resolve("test.csv").toFile();
        FileBackedTasksManager manager = new FileBackedTasksManager(file);

        int taskId = manager.addNewTask(new Task("T", "d", NEW, TASK, 60, new Date(1_000_000)));
        int epicId = manager.addNewEpic(new TaskEpic("E", "d", NEW, EPIC, 0, null));
        manager.addNewSubtask(new TaskSubtask("S", "d", NEW, epicId, SUBTASK, 30, new Date(4_600_000)));
        manager.getTask(taskId);   // наполняем историю просмотрами
        manager.getEpic(epicId);

        FileBackedTasksManager loaded = FileBackedTasksManager.loadFromFile(file);

        assertEquals(manager.getTasks(), loaded.getTasks(), "Задачи не восстановились");
        assertEquals(manager.getEpics(), loaded.getEpics(), "Эпики не восстановились");
        assertEquals(manager.getSubtasks(), loaded.getSubtasks(), "Подзадачи не восстановились");
        assertEquals(manager.getHistory(), loaded.getHistory(), "История не восстановилась");
        assertEquals(manager.getPrioritizedTasks(), loaded.getPrioritizedTasks(),
                "Приоритетный список не восстановился");
    }
}

