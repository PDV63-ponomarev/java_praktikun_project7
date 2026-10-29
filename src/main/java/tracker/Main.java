package tracker;

import tracker.manager.FileBackedTasksManager;
import tracker.manager.TaskManager;
import tracker.model.*;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static TaskManager manager;

    public static void main(String[] args) {
        manager = FileBackedTasksManager.loadFromFile(new File("tasks.csv"));

        while (true) {
            printMenu();
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> printAllTasks();
                case "2" -> deleteAllDialog();
                case "3" -> openTaskById();
                case "4" -> createNewTask();
                case "5" -> updateTaskDialog();
                case "6" -> deleteOneTask();
                case "7" -> printHistory();
                case "8" -> { System.out.println("Выход"); return; }
                default -> System.out.println("Ошибка: введите число от 1 до 8");
            }
        }
    }

    private static void printMenu() {
        System.out.println("""
                Введите команду:
                1 - Получить список задач
                2 - Удалить все задачи
                3 - Открыть задачу
                4 - Создать новую задачу
                5 - Редактировать задачу
                6 - Удалить задачу
                7 - Отобразить историю просмотра задач
                8 - Выход""");
    }

    private static void printAllTasks() {
        List<Task> list = manager.getPrioritizedTasks();
        if (list.isEmpty()) {
            System.out.println("Список приоритетных задач пуст");
            return;
        }
        for (Task t : list) System.out.println(t);
    }

    private static void openTaskById() {
        int id;
        while (true) {
            System.out.print("Введите ID: ");
            String input = scanner.nextLine().trim();
            try {
                id = Integer.parseInt(input);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Неверный формат ID. Попробуйте снова.");
            }
        }

        Task task = manager.getTask(id);
        if (task == null) task = manager.getEpic(id);
        if (task == null) task = manager.getSubtask(id);
        System.out.println(task == null ? "Задача не найдена" : task);
    }

    private static void createNewTask() {
        String type;
        while (true) {
            System.out.println("Тип: 1 - обычная, 2 - эпик, 3 - подзадача");
            type = scanner.nextLine().trim();
            if (type.equals("1") || type.equals("2") || type.equals("3")) {
                break;
            }
            System.out.println("Неверный тип. Введите 1, 2 или 3.");
        }

        if (type.equals("3") && manager.getEpics().isEmpty()) {
            System.out.println("Нет эпиков — сначала создайте эпик.");
            return;
        }
        System.out.print("Название: ");
        String name = scanner.nextLine().trim();
        System.out.print("Описание: ");
        String desc = scanner.nextLine().trim();
        EnumStatus status = readStatus();
        int duration = readDuration();
        Date startTime = readStartTime();

        switch (type) {
            case "1" -> {
                try {
                    int id = manager.addNewTask(new Task(name, desc, status, TaskType.TASK, duration, startTime));
                    System.out.println("Создана задача, ID: " + id);
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
            case "2" -> {
                int id = manager.addNewEpic(new TaskEpic(name, desc, status, TaskType.EPIC, duration, startTime));
                System.out.println("Создан эпик, ID: " + id);
            }
            case "3" -> {
                for (TaskEpic e : manager.getEpics()) {
                    System.out.println("ID: " + e.getTaskID() + " - " + e.getTaskName());
                }
                System.out.print("ID эпика: ");
                int epicId = Integer.parseInt(scanner.nextLine().trim());

                try {
                    int id = manager.addNewSubtask(
                            new TaskSubtask(name, desc, status, epicId, TaskType.SUBTASK, duration, startTime));
                    System.out.println("Создана подзадача, ID: " + id);
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
            }
        }
    }

    private static void updateTaskDialog() {
        System.out.print("ID задачи для обновления: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Неверный ID.");
            return;
        }

        System.out.println("1 - Имя, 2 - Описание, 3 - Статус, 4 - Продолжительность, 5 - Дата старта");
        String choice = scanner.nextLine().trim();

        Task task = manager.getTask(id);
        TaskEpic epic = manager.getEpic(id);
        TaskSubtask sub = manager.getSubtask(id);

        if (task == null && epic == null && sub == null) {
            System.out.println("Задача не найдена");
            return;
        }

        switch (choice) {
            case "1" -> {
                System.out.print("Новое имя: ");
                String value = scanner.nextLine().trim();
                if (task != null) { task.setTaskName(value); manager.updateTask(task); }
                else if (epic != null) { epic.setTaskName(value); manager.updateEpic(epic); }
                else { sub.setTaskName(value); manager.updateSubtask(sub); }
            }
            case "2" -> {
                System.out.print("Новое описание: ");
                String value = scanner.nextLine().trim();
                if (task != null) { task.setTaskDescription(value); manager.updateTask(task); }
                else if (epic != null) { epic.setTaskDescription(value); manager.updateEpic(epic); }
                else { sub.setTaskDescription(value); manager.updateSubtask(sub); }
            }
            case "3" -> {
                if (epic != null) {
                    System.out.println("Статус эпика меняется автоматически");
                    return;
                }
                EnumStatus status = readStatus();
                if (task != null) { task.setTaskStatus(status); manager.updateTask(task); }
                else { sub.setTaskStatus(status); manager.updateSubtask(sub); }
            }
            case "4" -> {
                if (epic != null) {
                    System.out.println("Продолжительность эпика рассчитывается из подзадач");
                    return;
                }
                int duration = readDuration();
                if (task != null) { task.setDuration(duration); manager.updateTask(task); }
                else { sub.setDuration(duration); manager.updateSubtask(sub); }
            }
            case "5" -> {
                if (epic != null) {
                    System.out.println("Дата старта эпика рассчитывается из подзадач");
                    return;
                }
                Date startTime = readStartTime();
                if (task != null) {
                    task.setStartTime(startTime); manager.updateTask(task);
                } else {
                    sub.setStartTime(startTime); manager.updateSubtask(sub);
                }
            }
            default -> System.out.println("Неверный пункт меню");
        }
    }

    private static void deleteOneTask() {
        System.out.print("ID задачи для удаления: ");
        int id = Integer.parseInt(scanner.nextLine().trim());
        if (manager.getTask(id) != null)      manager.deleteTask(id);
        else if (manager.getEpic(id) != null) manager.deleteEpic(id);
        else if (manager.getSubtask(id) != null) manager.deleteSubtask(id);
        else { System.out.println("Задача не найдена"); return; }
        System.out.println("Удалено");
    }

    private static void deleteAllDialog() {
        System.out.println("Удалить всё? 1 - Да, 2 - Нет");
        if (scanner.nextLine().trim().equals("1")) {
            manager.deleteTasks();
            manager.deleteEpics();
            manager.deleteSubtasks();
            System.out.println("Все задачи удалены");
        }
    }

    private static void printHistory() {
        for (Task t : manager.getHistory()) System.out.println(t);
    }

    private static EnumStatus readStatus() {
        while (true) {
            System.out.print("Статус (1 - NEW, 2 - IN_PROGRESS, 3 - DONE): ");
            switch (scanner.nextLine().trim()) {
                case "1" -> { return EnumStatus.NEW; }
                case "2" -> { return EnumStatus.IN_PROGRESS; }
                case "3" -> { return EnumStatus.DONE; }
                default -> System.out.println("Введите 1, 2 или 3");
            }
        }
    }

    private static int readDuration() {
        while (true) {
            System.out.print("Продолжительность в минутах (0, если неизвестно): ");
            String input = scanner.nextLine().trim();
            try {
                int d = Integer.parseInt(input);
                if (d < 0) {
                    System.out.println("Продолжительность не может быть отрицательной.");
                    continue;
                }
                return d;
            } catch (NumberFormatException e) {
                System.out.println("Введите целое число.");
            }
        }
    }

    private static Date readStartTime() {
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        fmt.setLenient(false);
        while (true) {
            System.out.print("Дата старта (dd.MM.yyyy HH:mm, пусто — без даты): ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null;
            }
            try {
                return fmt.parse(input);
            } catch (ParseException e) {
                System.out.println("Неверный формат. Пример: 01.05.2024 10:30");
            }
        }
    }
}